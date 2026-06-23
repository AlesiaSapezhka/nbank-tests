package iteration_2.ui;

import api.generators.RandomData;
import api.models.*;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Selenide;
import iteration_1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlerts;
import ui.pages.DepositMoney;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateDepositTest extends BaseUiTest {
    @Test
    public void userCanCreateDepositTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = accountData.getAccountNumber();
        int accountId = accountData.getId();
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер добавляет депозит
        new UserDashboard().open().depositMoney().checkPageTitle("\uD83D\uDCB0 Deposit Money");
        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(1000, 5000)).build();
        new DepositMoney().selectAccount(accountNumber).enterAmount(deposit.getBalance()).clickDeposit();

        // ШАГ 6: проверка, что баланс пополнился на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.DEPOSIT_SUCCESSFUL.getMessage() + deposit.getBalance() + " to account " + accountNumber + "!");

        // ШАГ 7: проверка, что депозит пополнен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), accountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(deposit.getBalance());
        assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.DEPOSIT);
    }

    @Test
    public void userCanNotCreateDepositMoreThanLimitTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = accountData.getAccountNumber();
        int accountId = accountData.getId();
        Selenide.open("/dashboard");
        // ШАГИ ТЕСТА
        // ШАГ 5: юзер добавляет депозит
        new UserDashboard().open().depositMoney().checkPageTitle("\uD83D\uDCB0 Deposit Money");
        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(5001, 6000)).build();
        new DepositMoney().selectAccount(accountNumber).enterAmount(deposit.getBalance()).clickDeposit();

        // ШАГ 6: проверка, что баланс НЕ пополнился на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.DEPOSIT_UNSUCCESSFUL.getMessage());

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
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        Selenide.open("/dashboard");
        // ШАГИ ТЕСТА
        // ШАГ 5: юзер добавляет депозит
        new UserDashboard().open().depositMoney().checkPageTitle("\uD83D\uDCB0 Deposit Money");
        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(10, 600)).build();
        new DepositMoney().enterAmount(deposit.getBalance()).clickDeposit();

        // ШАГ 6: проверка, что баланс НЕ пополнился на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.DEPOSIT_WITHOUT_SELECTING_ACCOUNT.getMessage());
    }
}
