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

public class TransferMoneyTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.100.137:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true, "enableLog", true));
    }

    @Test
    public void userCanTransferMoneyPositiveTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").click();
        $("select.account-selector option").shouldBe(Condition.exist);
        $("select").selectOptionContainingText(String.valueOf(senderAccountId));

        long transfer = RandomData.getRandomAmount(100, 500);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue("Ivan");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(receiverAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(transfer));
        $("#confirmCheck").setSelected(true);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: проверка, что перевод осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully transferred $" + transfer + " to account " + receiverAccountNumber + "!");
        alert.accept();

        // ШАГ 8: проверка, что перевод осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains((double) transfer);
        assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.TRANSFER_OUT);
    }

    @Test
    public void userCanTransferMoneyToTheSameAccountTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").click();
        $("select.account-selector option").shouldBe(Condition.exist);
        $("select").selectOptionContainingText(String.valueOf(senderAccountId));

        long transfer = RandomData.getRandomAmount(100, 500);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue("Ivan");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(senderAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(transfer));
        $("#confirmCheck").setSelected(true);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: проверка, что перевод осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully transferred $" + transfer + " to account " + senderAccountNumber + "!");
        alert.accept();

        // ШАГ 8: проверка, что перевод осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains((double) transfer);
        assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.TRANSFER_IN);
    }

    @Test
    public void userCanNotTransferMoneyWithoutDataConfirmationTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").click();
        $("select.account-selector option").shouldBe(Condition.exist);
        $("select").selectOptionContainingText(String.valueOf(senderAccountId));

        long transfer = RandomData.getRandomAmount(100, 500);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue("Ivan");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(receiverAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(transfer));
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: проверка, что перевод НЕ осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("Please fill all fields and confirm.");
        alert.accept();

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain((double) transfer);
    }

    @Test
    public void userCanNotTransferMoneyToIncorrectAccountTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").click();
        $("select.account-selector option").shouldBe(Condition.exist);
        $("select").selectOptionContainingText(String.valueOf(senderAccountId));

        long transfer = RandomData.getRandomAmount(100, 500);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue("Ivan");
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(transfer));
        $("#confirmCheck").setSelected(true);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: проверка, что перевод НЕ осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please fill all fields and confirm.");
        alert.accept();

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain((double) transfer);
    }
    // Успешный кейс, возможно имя не обязательно для заполнения
    @Test
    public void userCanNotTransferMoneyToIncorrectRecipientNameTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").click();
        $("select.account-selector option").shouldBe(Condition.exist);
        $("select").selectOptionContainingText(String.valueOf(senderAccountId));

        long transfer = RandomData.getRandomAmount(100, 500);

        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(receiverAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(transfer));
        $("#confirmCheck").setSelected(true);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: проверка, что перевод НЕ осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please fill all fields and confirm.");
        alert.accept();

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain((double) transfer);
    }

    @Test
    public void userCanNotTransferMoneyWithoutSelectingSenderAccountTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер переводит деньги
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        long transfer = RandomData.getRandomAmount(100, 500);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue("Ivan");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(receiverAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(transfer));
        $("#confirmCheck").setSelected(true);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 6: проверка, что перевод НЕ осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("Please fill all fields and confirm.");
        alert.accept();

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain((double) transfer);
    }

    @Test
    public void userCanNotTransferMoneyValueIncreasingBalanceTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(100, 500)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);
        $("select.account-selector").click();
        $("select.account-selector option").shouldBe(Condition.exist);
        $("select").selectOptionContainingText(String.valueOf(senderAccountId));

        long transfer = RandomData.getRandomAmount(4000, 5000);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue("Ivan");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(receiverAccountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(String.valueOf(transfer));
        $("#confirmCheck").setSelected(true);
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // ШАГ 7: проверка, что перевод НЕ осуществлен на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts");
        alert.accept();

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain((double) transfer);
    }
}
