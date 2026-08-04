# nbank-tests

Учебный фреймворк автоматизации для приложения **nbank**: API (Rest Assured), UI (Selenide + Selenoid), проверки в PostgreSQL, мок fraud-сервиса (WireMock).
---

# 0. Полный инвентарь стека

## 0.1. Библиотеки (`pom.xml`)

| Библиотека | Артефакт | Версия | Зачем |
| Java / Amazon Corretto | — | **25** | Язык; в IDEA — Corretto 25 |
| Rest Assured | `io.rest-assured:rest-assured` | **5.3.0** | HTTP API-тесты |
| Jackson | `jackson-databind` | **2.19.0** | JSON ↔ Java DTO |
| JUnit 5 | `junit-jupiter` | **5.10.2** | Тесты, параметризация, extensions |
| JUnit Platform | `junit-platform-commons` | **1.10.2** | Platform API |
| Lombok | `lombok` | **1.18.40** | `@Data`, `@Builder`; нужен annotation processor (JDK 23+) |
| AssertJ | `assertj-core` | **3.27.3** | SoftAssertions + кастомные assert'ы |
| RgxGen | `rgxgen` | **1.3** | Генерация строк по regex (`@GeneratingRule`) |
| Selenide | `selenide` | **7.9.1** | UI (поверх Selenium ~4.31) |
| SLF4J | `slf4j-simple` | **2.0.17** | Логи |
| PostgreSQL JDBC | `postgresql` | **42.7.2** | Проверки в БД |
| Allure | `allure-junit5`, `allure-rest-assured`, `allure-selenide` | **2.29.1** | Отчётность (deps есть; фильтры RA/Selenide почти не подключены) |
| WireMock | `wiremock-standalone` | **3.4.2** | Мок fraud-сервиса `:8080` |

## 0.2. Собственные аннотации и JUnit-расширения

**Аннотации** — `src/main/java/common/annotations/`:
| Аннотация | Назначение |
| `@UserSession(value, auth)` | Создать N пользователей, UI-auth под индексом `auth` |
| `@AdminSession` | Залогинить admin через localStorage |
| `@Browsers({"chrome"})` | Включить тест только для указанных браузеров |
| `@FraudCheckMock(...)` | Поднять WireMock-stub fraud-check |

**Extensions** — `src/main/java/common/extensions/` (+ test):
| Extension | Когда | Что делает |
| `UserSessionExtension` | BeforeEach | Юзеры → `SessionStorage` + `authAsUser` |
| `AdminSessionExtension` | BeforeEach | Auth как admin |
| `BrowserMatchExtension` | Condition | Skip, если браузер не совпал |
| `DeleteUsersExtension` | AfterEach | Удаляет ID из `CreatedUsersStorage` |
| `TimingExtension` | Around | Лог длительности теста |
| `FraudCheckWireMockExtension` | Before/After | Старт/стоп WireMock по `@FraudCheckMock` |

Подключение: `@ExtendWith` на `BaseTest` / `BaseUiTest` / `TransferWithFraudCheckTest`.

## 0.3. IDE, инструменты, порты

| Сервис | Адрес |
| API | `http://localhost:4111` (`/api/v1/`) |
| Postgres | `localhost:5432` / `nbank` / `postgres`/`postgres` |
| Selenoid hub | `http://localhost:4444/wd/hub` |
| UI (из контейнера браузера) | `http://host.docker.internal:3000` |
| WireMock fraud | `http://host.docker.internal:8080` |


# 1. Эволюция: как это делалось (Junior → Mock)
Порядок зависимостей в pom по этапам:

1. Junior: Rest Assured + JUnit  
2. Middle: + Jackson, Lombok, AssertJ  
3. Senior API: + RgxGen  
4. UI: + Selenide, SLF4J  
5. Database: + PostgreSQL
6. Mock: + WireMock
---

## 1.1. Junior-API 

**Уровень:** Junior  
**Цель:** научиться писать API-тесты Rest Assured «в лоб».

**Что подняли:** приложение nbank на `:4111` (тогда без compose в репо).  
**pom:** только `rest-assured` 5.3.0 + `junit-jupiter` 5.10.2.  
**Классы:** только тесты в `src/test/java` — `CreateUserTest`, `CreateAccountTest`, `LogicUserTest`.

**Как делали руками:**

1. Создали Maven-проект, добавили Rest Assured + JUnit.  
2. Открыли API через `.http` / Postman, списали URL и Basic auth.  
3. В тесте написали `given()…post(hardcodedUrl)` + JSON строкой (`String.format` / литерал).  
4. Assert через Hamcrest (`body("username", equalTo(...))`).  
5. Добавили parameterized valid/invalid и финальный GET.

**Было (стиль):**

given().contentType(ContentType.JSON)
  .header("Authorization", "Basic YWRtaW46YWRtaW4=")
  .body(requestBody)
  .post("http://localhost:4111/api/v1/admin/users")
  .then().statusCode(201)
  .body("username", Matchers.equalTo(username));

**Почему плохо на масштабе:** дубли URL/auth/JSON, хрупкие строки, нельзя переиспользовать.

---

## 1.2. Middle-API

**Уровень:** Middle  
**Цель:** убрать дубли, типизировать JSON.

**pom (+):** `jackson-databind` 2.19.0, `lombok` 1.18.38, `assertj-core` 3.27.3.

**Порядок классов (как повторить):**

1. Модели: `CreateUserRequest`/`Response`, `LoginUser*`, deposit/transfer, `BaseModel`, `UserRole` — Lombok `@Builder`.  
2. `RequestSpecs` / `ResponseSpecs` — общий base URL, admin/user auth, ожидаемые статусы.  
3. Интерфейсы/базы `PostRequest`/`GetRequest`/`PutRequest`.  
4. **Отдельный requester на каждый endpoint:** `AdminCreateUserRequester`, `LoginUserRequester`, …  
5. `RandomData` для простых данных.  
6. `BaseTest` с SoftAssertions.  
7. Переписать тесты: model → requester → AssertJ.

**Стало:**

CreateUserRequest req = CreateUserRequest.builder()
    .username(username).password(password).role(role).build();
CreateUserResponse resp = new AdminCreateUserRequester(
    RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
    .post(req).extract().as(CreateUserResponse.class);
softly.assertThat(req.getUsername()).isEqualTo(resp.getUsername());

**Плюсы:** DRY по specs, type-safety, soft asserts.  
**Минус:** на каждый endpoint — новый класс (взрыв классов).
---

## 1.3. Senior-API

**Уровень:** Senior  
**Цель:** один CRUD-слой + бизнес-steps + генераторы + сравнение моделей.

**pom (+):** `rgxgen` 1.3.  
**Удалены** per-endpoint requesters Middle.

**Порядок классов:**

1. `config.properties` + `Config.java`.  
2. Enum `Endpoint` (path + request/response class).  
3. `HttpRequests` → `CrudRequester` → `ValidatedCrudRequester`.  
4. `AdminSteps` / `UserSteps`.  
5. `@GeneratingRule` + `RandomModelGenerator`.  
6. `model-comparison.properties` + `ModelComparator` + `ModelAssertions`.  
7. Enum'ы negative cases (`InvalidUsernameCase`, …).  
8. Тесты = сценарии через Steps.

**Стало:**

CreateUserRequest userRequest = AdminSteps.buildUserValid();
CreateUserResponse createUserResponse = AdminSteps.createUserValid(userRequest);
ModelAssertions.assertThatModels(userRequest, createUserResponse).match();

**Почему хорошая практика:** новый endpoint = новая константа в `Endpoint`, не новый requester-класс; тест читается как бизнес-язык.

**Важно:** отдельных классов `RequestBuilder`/`ResponseBuilder` **нет** — только RestAssured `RequestSpecBuilder`/`ResponseSpecBuilder` внутри specs.

---

## 1.4. UI-Junior

**Уровень:** Junior UI  
**pom (+):** `selenide` 7.9.1, `slf4j-simple` 2.0.17.

**Что подняли руками:** Selenoid `:4444`, фронт на IP хоста `:3000`.  
**Классы:** UI-тесты с `$()` прямо в тесте; API вынесли в `iteration_*/api/`; API-steps уже переиспользуются.

**Стиль:**
Configuration.remote = "http://localhost:4444/wd/hub";
Configuration.baseUrl = "http://192.168.100.137:3000";
Selenide.open("/login");
$(Selectors.byAttribute("placeholder", "Username")).sendKeys(...);
$("button").click();

**Проблема:** локаторы и клики в тесте — дубли и ломкость.

---

## 1.5. UI-Middle 

**Уровень:** Middle UI  
**pom:** без новых deps.

**Порядок:**

1. Пакеты `api.*` / `ui.*`.  
2. `BasePage<T>` (fluent `open()`, alerts).  
3. Pages: `LoginPage`, `AdminPanel`, `UserDashboard`, `DepositMoney`, `MakeTransfer`, `EditProfile`, `BankAlerts`.  
4. `BaseUiTest` — Selenoid из `Config`.  
5. Bypass login: JWT в `localStorage` через API (`authAsUser`).  
6. Переписать UI-тесты на POM.

**Стало:**
authAsUser(CreateUserRequest.getAdmin());
new AdminPanel().open()
  .createUser(newUser.getUsername(), newUser.getPassword())
  .checkAlertMessageAndAccept(...);

**DriverFactory нет** — remote/baseUrl задаются в `BaseUiTest`.

---

## 1.6. Senior-UI

**Уровень:** Senior UI
**Порядок:**

1. Аннотации `@AdminSession`, `@UserSession`, `@Browsers`.  
2. Extensions: `AdminSessionExtension`, `UserSessionExtension`, `BrowserMatchExtension`.  
3. `SessionStorage` (сначала обычный singleton).  
4. `BaseElements` / `UserBadge`.  
5. `RetryUtils` для поиска юзера в админке.  
6. Тесты почти без setup — только `@AdminSession` / `@UserSession`.

**Стало:** тест = сценарий; setup в extension.

---

## 1.7. Parallel-tests

**Что сделали:**

1. `junit-platform.properties`: parallel ON, fixed parallelism **= 2**.  
2. `SessionStorage` → **ThreadLocal**.  
3. UI: `@Execution(SAME_THREAD)` + `closeWebDriver()` после каждого теста.  
4. `TimingExtension`, maven-buildtime-profiler.

**Почему:** shared singleton ломается при concurrent API; UI-браузеры не гоняли параллельно в одном классе.
---

## 1.8. Database 

**pom (+):** `postgresql` 42.7.2, Allure 2.29.1 (junit5/ra/selenide).
**Порядок:**

1. `infra/docker_compose/docker-compose.yml` — Postgres 15 + nbank.  
2. `DBRequest` + `Condition` (fluent SELECT).  
3. DAO: `UserDao`, `AccountDao`, `TransactionsDao`.  
4. `dao-comparison.properties` + `DaoComparator` + `DaoAndModelAssertions`.  
5. `DataBaseSteps`.  
6. `StepLogger` → `Allure.step`.  
7. В `CreateUserTest`: assert API **и** DB.

UserDao userDao = DataBaseSteps.getUserByUsername(...);
DaoAndModelAssertions.assertThat(createUserResponse, userDao).match();

---

## 1.9. Mock — merge 

**pom (+):** `wiremock-standalone` 3.4.2.
**Порядок:**

1. `@FraudCheckMock` + `FraudCheckWireMockExtension`.  
2. В compose: `FRAUD_DETECTION_SERVICE_URL=http://host.docker.internal:8080`.  
3. Endpoint `TRANSFER_WITH_FRAUD_CHECK`, модели, steps.  
4. `TransferWithFraudCheckTest` + `DeleteUsersExtension` / `CreatedUsersStorage`.

@FraudCheckMock(status = "SUCCESS", decision = "APPROVED", riskScore = 0.2, ...)
public void testTransferWithFraudCheck() { ... }
---

# 2. Архитектура

## Структура проекта
nbank-tests/
├── pom.xml
├── requests/                      # HTTP Client пробы
├── infra/docker_compose/          # Postgres + nbank API
├── src/main/java/
│   ├── api/
│   │   ├── configs/               # Config
│   │   ├── specs/                 # RequestSpecs / ResponseSpecs
│   │   ├── models/                # DTO, invalid cases, model comparison
│   │   ├── generators/            # RandomModelGenerator, @GeneratingRule
│   │   ├── requests/skelethon/    # Endpoint, CrudRequester, ValidatedCrudRequester
│   │   ├── requests/steps/        # AdminSteps, UserSteps, DataBaseSteps
│   │   ├── database/              # DBRequest, Condition
│   │   └── dao/                   # UserDao, AccountDao, TransactionsDao
│   ├── ui/
│   │   ├── pages/                 # Page Objects (BasePage, LoginPage, …)
│   │   ├── element/               # BaseElements, UserBadge
│   │   └── utils/                 # RetryUtils
│   └── common/
│       ├── annotations/           # @UserSession, @AdminSession, @Browsers, @FraudCheckMock
│       ├── extensions/            # JUnit 5 extensions
│       ├── storage/               # SessionStorage, CreatedUsersStorage (ThreadLocal)
│       └── helpers/               # StepLogger (Allure.step)
└── src/test/java/
    ├── iteration_1/{api,ui}/
    ├── iteration_2/{api,ui}/
    └── common/extensions/         # FraudCheckWireMockExtension


## Пакеты (финал)

| Слой | Пакет | Ответственность |
| Config | `api.configs` | `config.properties` |
| Specs | `api.specs` | Request/Response specs |
| Models | `api.models` | DTO + invalid enums + comparison |
| Generators | `api.generators` | Random models |
| Skeleton | `api.requests.skelethon` | Endpoint + CRUD requesters |
| Steps | `api.requests.steps` | Бизнес-фасады |
| DB | `api.database` + `api.dao` | JDBC + DAO compare |
| Common | `common.*` | Annotations, extensions, storage |
| UI | `ui.pages`, `ui.element` | POM + components |
| Tests | `iteration_1\|2` / `api\|ui` | Сценарии по итерациям |

## Принципы

- **SRP:** requester шлёт HTTP, steps — сценарий, page — UI, DAO — строка БД.  
- **OCP:** новый API = константа `Endpoint`, не правка всех тестов.  
- **DRY:** specs, steps, generators, comparison properties.  
- **KISS:** нет лишних абстракций вроде DriverFactory.  
- **Composition > inheritance:** `ValidatedCrudRequester` оборачивает `CrudRequester`; pages наследуют только `BasePage` для fluent/open/alert.  
- **Читаемость:** тест ≈ бизнес-язык (`AdminSteps.createUserValid`).


# 3. API Framework подробно

**Поток:**  
`Endpoint` → `RequestSpecs` + `ResponseSpecs` → `CrudRequester` / `ValidatedCrudRequester` → Jackson DTO.

| Тема | Как в проекте |
| Rest Assured | `given().spec().body().post(url).then().spec()` |
| Jackson | `extract().as(Class)` / `getList` |
| Модели | Lombok + `BaseModel` + `@JsonIgnoreProperties` |
| Specs | unauth / admin Basic / user JWT (кэш по username) |
| Skeleton | один CRUD на все endpoints |
| Steps | `AdminSteps`, `UserSteps` |
| Generators | `@GeneratingRule` + RgxGen |
| Assertions | SoftAssertions + `ModelAssertions.match()` |
| Negative / param | enum cases + `@ParameterizedTest` |
| Logging | RestAssured request/response log в specs |
| Allure RA filter | **не подключён** (только `StepLogger`) |

Отдельных Request/Response Builder-классов **нет**.

Типичный вызов:

new ValidatedCrudRequester<CreateUserResponse>(
    RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()
).post(request);

### Generators

- `@GeneratingRule(regex = "...")` — field-level, RUNTIME.  
- `RandomModelGenerator.generate(Class<T>)` — reflection + RgxGen / type defaults.  
- `RandomData` — random amount / username helpers.  
- Invalid cases в steps подменяют сгенерированные значения.

### Assertions

- **SoftAssertions** в `BaseTest` (`@BeforeEach` / `assertAll` в `@AfterEach`).  
- **`ModelAssertions`** — `assertThatModels(request, response).match()` по `model-comparison.properties`.  
- **`DaoAndModelAssertions`** — `assertThat(apiModel, daoModel).match()` по `dao-comparison.properties`.

### Steps

| Класс | Роль |
| **AdminSteps** | Build valid/invalid users; create (tracks ID); login; list; delete |
| **UserSteps** | Account/deposit/transfer/profile; instance держит credentials |
| **DataBaseSteps** | SELECT через `DBRequest`; update balance через JDBC |

# 4. UI Framework подробно

| Тема | Как в проекте |
| POM | `BasePage` + конкретные pages |
| Elements | `BaseElements`, `UserBadge` |
| Locators | Selenide `$`, `Selectors.byAttribute/byText` |
| Waits | встроенные Selenide + `shouldBe(visible/enabled)` |
| Browser | Selenoid remote в `BaseUiTest` |
| DriverFactory | **нет** |
| Auth | API token → `localStorage.authToken` |
| Forms | fluent methods (`changeName`, deposit, transfer) |
| Sessions | `@UserSession` / `@AdminSession` |
| Parallel UI | `@Execution(SAME_THREAD)`, close driver afterEach |

### Pages

| Page | Path / responsibility |
| `LoginPage` | `/login` — username/password + submit |
| `AdminPanel` | `/admin` — add user, list `UserBadge`s with retry |
| `UserDashboard` | `/dashboard` — nav to account/deposit/transfer/profile |
| `DepositMoney` | deposit form on dashboard |
| `MakeTransfer` | transfer form + confirm checkbox |
| `EditProfile` | change name + assert display name |
| `BankAlerts` | expected browser-alert texts |

### `BaseUiTest` (Selenoid)

- `@Execution(SAME_THREAD)` для UI-класса.  
- Extensions: `AdminSession`, `UserSession`, `BrowserMatch`.  
- `@BeforeAll setupSelenoid()`: `Configuration.remote` = `uiRemote`, `baseUrl` = `uiBaseUrl`, browser chrome, size 1920x1080, timeouts, `selenoid:options` (VNC/log).  
- `@AfterEach` `Selenide.closeWebDriver()`.  

Senior-практики: session extensions, component objects, API+UI hybrid auth, retry поиска в админке.

---

# 5. База данных

| Часть | Файл / роль |
| JDBC builder | `DBRequest` + `Condition` (SELECT) |
| DAO | `UserDao`, `AccountDao`, `TransactionsDao` |
| Steps | `DataBaseSteps` |
| Compare API↔DB | `DaoAndModelAssertions` + `dao-comparison.properties` |
| Infra | docker-compose Postgres 15 |

**Зачем:** ловит расхождения «API сказал OK, в БД другое» (маппинг, транзакции, дефолты).  
`init-scripts/01-init-db.sql` есть, но в compose **не примонтирован** (схема, скорее всего, из образа приложения).

`DBRequest`: builder `requestType(SELECT).table(...).where(Condition.equalTo(...)).extractAs(UserDao.class)`.  
Только SELECT реализован; update balance — отдельный JDBC в steps.

---

# 6. Параллельность

| Механизм | Деталь |
| `junit-platform.properties` | parallel=true, fixed=2 |
| `SessionStorage` | ThreadLocal |
| `CreatedUsersStorage` | ThreadLocal |
| UI | SAME_THREAD + closeWebDriver |
| TimingExtension | лог времени/потока |

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=2
```

**Зачем:** ускорение API, изоляция данных между потоками.  
---

# 7. Моки

Только **WireMock**

- Аннотация задаёт JSON ответа fraud-сервиса.  
- Extension поднимает сервер на `:8080`, stubs POST `/fraud-check`.  
- Приложение в Docker ходит на `host.docker.internal:8080`.  
- Сценарий: `TransferWithFraudCheckTest`.

**`@FraudCheckMock` defaults:** status SUCCESS, decision APPROVED, riskScore 0.2, port **8080**, path `/fraud-check`.

**Где ещё пригодились бы:** платежный шлюз, SMS/email, внешний KYC — тот же annotation+extension паттерн.

---

# 8. Инфраструктура

| Инструмент | Зачем |
| Maven | Сборка, зависимости, surefire |
| Docker Compose | Воспроизводимый Postgres + API |
| PostgreSQL | Источник истины для DAO-проверок |
| Selenoid | Стабильные remote-браузеры + VNC |
| Allure | Задел под отчёты (`StepLogger`) |
| WireMock | Контроль внешнего fraud API |

### Docker Compose (`infra/docker_compose/`)

- **postgres:15** → `5432`, DB `nbank`  
- **nobugsme/nbank:with_fraud_check_with_transfer_fix** → `4111`, fraud URL → host `8080`  
- Healthchecks; named volume `postgres_data`

```bash
cd infra/docker_compose
docker compose up -d
docker compose ps
docker compose logs -f nbank
docker compose down
```

### `config.properties`

```properties
apiBaseUrl = http://localhost:4111
apiVersion = /api/v1/
admin.username=admin
admin.password=admin
uiRemote = http://localhost:4444/wd/hub
uiBaseUrl = http://host.docker.internal:3000
browser = chrome
browserSize = 1920x1080
db.url=jdbc:postgresql://localhost:5432/nbank
db.username=postgres
db.password=postgres
```

---

# 9. Паттерны (только реальные)

| Паттерн | Где |
| Page Object | `ui.pages.*` |
| Component Object | `UserBadge` / `BaseElements` |
| Facade / Steps | `AdminSteps`, `UserSteps`, `DataBaseSteps` |
| Builder | Lombok models, `DBRequest.builder()` |
| Singleton | `Config` |
| Factory | `RandomModelGenerator`, `RequestSpecs.*Spec()` |
| Adapter/Decorator | `ValidatedCrudRequester` над `CrudRequester` |
| Fluent Interface | pages, RestAssured |
| Specification | Request/Response specs |
| Strategy (enum) | `Endpoint`, `Invalid*Case` |
| Custom AssertJ | `ModelAssertions`, `DaoAndModelAssertions` |
| ThreadLocal context | Session/CreatedUsers storage |
| JUnit Extension | sessions, cleanup, WireMock, timing |
| Retry | `RetryUtils` |

---

# 10. Примеры тестовых потоков

### API — create user (happy path)

1. `AdminSteps.buildUserValid()` → random `CreateUserRequest`  
2. `AdminSteps.createUserValid(...)` → POST `/admin/users` → store id for cleanup  
3. Soft-assert username; `ModelAssertions` request↔response  
4. GET all users; match again  
5. `DataBaseSteps.getUserByUsername` → `DaoAndModelAssertions` response↔DB  
6. After test: `DeleteUsersExtension` deletes created users  

### UI — create deposit

1. `@UserSession` создаёт user + ставит `authToken` в browser  
2. `SessionStorage.getSteps().createAccount()` через API  
3. Open dashboard → Deposit Money page  
4. Select account, enter amount, submit → assert alert  
5. Verify transactions via API (`getAllTransactionsList`)  

### API — fraud transfer

1. `@FraudCheckMock(...)` starts WireMock stub  
2. Create users/accounts/deposit via steps  
3. POST `/accounts/transfer-with-fraud-check`  
4. Compare expected fraud fields via `ModelAssertions`  

---

# 11. Рассказ для технического интервью (от первого лица)

Я делала учебный фреймворк для банковского приложения nbank и специально вела его по ступеням Junior → Middle → Senior, чтобы понимать *зачем* каждая абстракция.

Начала с **Junior API**: Rest Assured прямо в тесте, hardcoded URL, Basic auth и JSON строкой. Так быстро научилась HTTP-сценариям, но при росте числа тестов всё дублировалось и ломалось от малейшего изменения контракта.

На **Middle** вынесла модели (Jackson + Lombok), общие Request/Response specs и отдельные requester-классы на endpoint. Тесты стали типизированными, появился SoftAssertions. Но requester на каждый URL раздувал код.

На **Senior API** сделала один CRUD-skeleton: enum `Endpoint` + `CrudRequester`/`ValidatedCrudRequester`, поверх — `AdminSteps`/`UserSteps`. Данные генерирую через `@GeneratingRule` и RgxGen, сравнения request↔response — через конфигурируемый `ModelAssertions`. Тест читается как бизнес-сценарий; новый endpoint почти не трогает старые тесты.

Потом добавила **UI**: сначала Selenide-селекторы в тесте (Junior), затем Page Object и `BasePage` (Middle), затем session-extensions `@UserSession`/`@AdminSession`, component `UserBadge` и auth через JWT в localStorage, чтобы не гонять логин UI в каждом тесте (Senior). Браузеры — remote Selenoid.

Когда включила **параллельный запуск** (JUnit parallel=2), shared `SessionStorage` начал конфликтовать между потоками — перевела на ThreadLocal. UI оставила `@Execution(SAME_THREAD)` и закрываю драйвер после теста.

Отдельно научилась **проверять БД**: Docker Compose с Postgres, лёгкий JDBC-builder, DAO и сравнение API↔DB. Так ловлю баги, которые API-ответ маскирует. Подключила Allure на уровне step-логгера.

В финале слила ветки и добавила **WireMock**: fraud-сервис мокаю аннотацией `@FraudCheckMock` и extension'ом, а приложение в Docker смотрит на `host.docker.internal:8080`. Mockito не использовала — мокала именно HTTP-зависимость системы.

На собеседовании я подчёркиваю не «я знаю библиотеки», а эволюцию: сначала работающий тест, потом устранение боли (дубли, хрупкость, скорость, изоляция, внешние сервисы), и осознанный выбор паттернов — Page Object, Steps/Facade, Specs, ThreadLocal, JUnit Extensions. Честно говорю, чего ещё нет: CI, полного Allure wiring, DriverFactory, thread-safe auth cache — и как бы довела дальше.

**Стек одной фразой:** Java 25, Maven, JUnit 5, Rest Assured, Jackson, Lombok, AssertJ, RgxGen, Selenide + Selenoid, PostgreSQL JDBC, Allure, WireMock, Docker Compose.