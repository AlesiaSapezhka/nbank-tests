package iteration_2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateDepositTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.100.137:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true, "enableLog", true));
    }

    @Test
    public void userCanCreateDepositTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");
        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = accountData.getAccountNumber();
        int accountId = accountData.getId();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер добавляет депозит
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        Selenide.refresh();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $("select.account-selector").click();
        $("select.account-selector option").shouldBe(Condition.exist);
        $("select").selectOptionContainingText(accountNumber);

        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(1000, 5000)).build();

        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue((String.valueOf(deposit.getBalance())));
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // ШАГ 6: проверка, что баланс пополнился на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully deposited $" + deposit.getBalance() + " to account " + accountNumber + "!");
        alert.accept();

        // ШАГ 7: проверка, что депозит пополнен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), accountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(deposit.getBalance());
        assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.DEPOSIT);
    }

    @Test
    public void userCanNotCreateDepositMoraThanLimitTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");
        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = accountData.getAccountNumber();
        int accountId = accountData.getId();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер добавляет депозит
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        Selenide.refresh();
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);
        $("select.account-selector").click();
        $("select.account-selector option").shouldBe(Condition.exist);
        $("select").selectOptionContainingText(accountNumber);

        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(5001, 6000)).build();

        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue((String.valueOf(deposit.getBalance())));
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // ШАГ 6: проверка, что баланс НЕ пополнился на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please deposit less or equal to 5000$.");
        alert.accept();

        // ШАГ 7: проверка, что депозит НЕ пополнен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), accountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(deposit.getBalance());
    }

    @Test
    public void userCanNotCreateDepositWithoutSelectingAccountTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер добавляет депозит
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(10, 600)).build();

        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue((String.valueOf(deposit.getBalance())));
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // ШАГ 6: проверка, что баланс НЕ пополнился на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please select an account.");
        alert.accept();
    }
}
