package iteration_2.ui;

import api.generators.RandomData;
import api.models.*;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration_1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlerts;
import ui.pages.MakeTransfer;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanTransferMoneyPositiveTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = SessionStorage.getSteps().createAccount();
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(createDepositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_SUCCESSFUL.getMessage() + transfer + " to account " + receiverAccountNumber + "!");

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(transfer);
        assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.TRANSFER_OUT);
    }

    @Test
    @UserSession
    public void userCanTransferMoneyToTheSameAccountTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(createDepositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterRecipientAccount(senderAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_SUCCESSFUL.getMessage() + transfer + " to account " + senderAccountNumber + "!");

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(transfer);
        assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.TRANSFER_IN);
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyWithoutDataConfirmationTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = SessionStorage.getSteps().createAccount();
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(createDepositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyToMissedAccountValueTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(createDepositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterAmount(transfer).setCheckbox(true).sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }

    // Успешный кейс, возможно имя не обязательно для заполнения
    @Test
    @UserSession
    public void userCanNotTransferMoneyToMissedRecipientNameTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = SessionStorage.getSteps().createAccount();
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(createDepositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyWithoutSelectingSenderAccountTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = SessionStorage.getSteps().createAccount();
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(createDepositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer().enterRecipientName("Ivan").enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyValueIncreasingBalanceTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = SessionStorage.getSteps().createAccount();
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(100, 500)).build();
        SessionStorage.getSteps().createDeposit(createDepositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(4000, 5000);
        new MakeTransfer().selectAccount(senderAccountNumber).enterRecipientName("Ivan").enterRecipientAccount(receiverAccountNumber).enterAmount(transfer).setCheckbox(true).sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_INCREASED_BALANCE.getMessage());

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }
}
