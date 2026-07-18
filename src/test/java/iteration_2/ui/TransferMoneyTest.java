package iteration_2.ui;

import api.generators.RandomData;
import api.models.*;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration_1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlerts;
import ui.pages.MakeTransfer;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {
    @Test
    public void userCanTransferMoneyPositiveTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        new UserDashboard().open();
        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        // ШАГ 7: проверка, что перевод осуществлен на UI
        new UserDashboard().checkAlertMessageAndAccept(
                String.format(
                        "%s%s to account %s!",
                        BankAlerts.TRANSFER_SUCCESSFUL.getMessage(),
                        transfer,
                        receiverAccountNumber
                )
        );
        // ШАГ 8: проверка, что перевод осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(transfer);
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
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        new UserDashboard().open();
        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterRecipientAccount(senderAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        // ШАГ 7: проверка, что перевод осуществлен на UI
        new UserDashboard().checkAlertMessageAndAccept(
                String.format(
                        "%s%s to account %s!",
                        BankAlerts.TRANSFER_SUCCESSFUL.getMessage(),
                        transfer,
                        senderAccountNumber
                )
        );
        // ШАГ 8: проверка, что перевод осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(transfer);
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
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        new UserDashboard().open();
        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).sendTransfer();

        // ШАГ 7: проверка, что перевод НЕ осуществлен на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }

    @Test
    public void userCanNotTransferMoneyToMissedAccountValueTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        new UserDashboard().open();
        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterAmount(transfer).setCheckbox(true).sendTransfer();

        // ШАГ 7: проверка, что перевод осуществлен на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }

    // Успешный кейс, возможно имя не обязательно для заполнения
    @Test
    public void userCanNotTransferMoneyToMissedRecipientNameTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        new UserDashboard().open();
        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        // ШАГ 7: проверка, что перевод осуществлен на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }

    @Test
    public void userCanNotTransferMoneyWithoutSelectingSenderAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт отправителя и получателя
        // ШАГ 5: юзер пополняет баланс отправителя
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        new UserDashboard().open();
        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().enterRecipientName("Ivan").enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        // ШАГ 7: проверка, что перевод НЕ осуществлен на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
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
        authAsUser(user);

        CreateAccountResponse accountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(100, 500)).build();
        UserSteps.createDeposit(user.getUsername(), user.getPassword(), createDepositRequest);

        new UserDashboard().open();
        // ШАГИ ТЕСТА
        // ШАГ 6: юзер переводит деньги
        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(4000, 5000);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        // ШАГ 7: проверка, что перевод НЕ осуществлен на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_INCREASED_BALANCE.getMessage());

        // ШАГ 8: проверка, что перевод НЕ осуществлен на API
        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(user.getUsername(), user.getPassword(), senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }
}
