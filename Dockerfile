# базовый докер образ
# каждый раз с нуля строить базовый образ (java, mvn, git)
# голый докер образ и устанавлить java, maven
# ЕСЛИ МОЖНО СОЗДАТЬ ОБРАЗ ПОВЕРХ ДРУГОГО ОБРАЗА, ГДЕ ВСЕ УЖЕ УСТАНОВЛЕНО
# МАРКЕТПЛЕЙС ВСЕХ ДОКЕР ОБРАЗОВ - docker hub
FROM maven:3.9-eclipse-temurin-25

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

# Переменные окружения для контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

# работаем из папки /app
WORKDIR /app

# копируем помник
COPY pom.xml .

# загружаем зависимости и кешируем
RUN mvn dependency:go-offline

# копируем весь проект
COPY . .

# теперь внутри есть зависимости, есть весь проект и мы готовы запускать тесты
USER root

# JSON form avoids JSONArgsRecommended warning; tee keeps console + file log
CMD ["bash", "-c", "set -o pipefail; mkdir -p /app/logs /app/target/surefire-reports /app/target/site; { echo \">>> Running tests with profile: ${TEST_PROFILE}\"; mvn test -P ${TEST_PROFILE}; echo \">>> Running surefire-report:report\"; mvn -DskipTests=true surefire-report:report; } 2>&1 | tee /app/logs/run.log"]