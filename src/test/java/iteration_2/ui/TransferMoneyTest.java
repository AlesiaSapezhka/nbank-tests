package iteration_2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateDepositRequest;
import api.models.GetTransactionsResponse;
import api.models.TransactionsTypes;
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

        CreateDepositRequest depositRequest = CreateDepositRequest.builder()
                .accountId(senderAccountId).amount(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(depositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer()
                .selectAccount(senderAccountNumber)
                .enterRecipientName(RandomData.getUserName())
                .enterRecipientAccount(receiverAccountNumber)
                .enterAmount(transfer)
                .setCheckbox(true)
                .sendTransfer()
                .shouldHaveDepositSuccessAlert(transfer, receiverAccountNumber);

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

        CreateDepositRequest depositRequest = CreateDepositRequest.builder()
                .accountId(senderAccountId).amount(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(depositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 600);
        new MakeTransfer()
                .selectAccount(senderAccountNumber)
                .enterRecipientName(RandomData.getUserName())
                .enterRecipientAccount(senderAccountNumber)
                .enterAmount(transfer)
                .setCheckbox(true)
                .sendTransfer()
                .shouldHaveDepositSuccessAlert(transfer, senderAccountNumber);

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

        CreateDepositRequest depositRequest = CreateDepositRequest.builder()
                .accountId(senderAccountId).amount(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(depositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer()
                .selectAccount(senderAccountNumber)
                .enterRecipientName(RandomData.getUserName())
                .enterRecipientAccount(receiverAccountNumber)
                .enterAmount(transfer)
                .sendTransfer();

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

        CreateDepositRequest depositRequest = CreateDepositRequest.builder()
                .accountId(senderAccountId).amount(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(depositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer()
                .selectAccount(senderAccountNumber)
                .enterRecipientName(RandomData.getUserName())
                .enterAmount(transfer)
                .setCheckbox(true)
                .sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_MISSED_FIELDS.getMessage());

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyToMissedRecipientNameTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();
        String senderAccountNumber = accountData.getAccountNumber();

        CreateAccountResponse receiverAccountData = SessionStorage.getSteps().createAccount();
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest depositRequest = CreateDepositRequest.builder()
                .accountId(senderAccountId).amount(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(depositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer()
                .selectAccount(senderAccountNumber)
                .enterRecipientAccount(receiverAccountNumber)
                .enterAmount(transfer)
                .setCheckbox(true)
                .sendTransfer()
                .shouldHaveDepositSuccessAlert(transfer, receiverAccountNumber);

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(transfer);
        assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.TRANSFER_OUT);
    }

    @Test
    @UserSession
    public void userCanNotTransferMoneyWithoutSelectingSenderAccountTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = SessionStorage.getSteps().createAccount();
        String receiverAccountNumber = receiverAccountData.getAccountNumber();

        CreateDepositRequest depositRequest = CreateDepositRequest.builder()
                .accountId(senderAccountId).amount(RandomData.getRandomAmount(1000, 5000)).build();
        SessionStorage.getSteps().createDeposit(depositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(100, 500);
        new MakeTransfer()
                .enterRecipientName(RandomData.getUserName())
                .enterRecipientAccount(receiverAccountNumber)
                .enterAmount(transfer)
                .setCheckbox(true)
                .sendTransfer();

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

        CreateDepositRequest depositRequest = CreateDepositRequest.builder()
                .accountId(senderAccountId).amount(RandomData.getRandomAmount(100, 500)).build();
        SessionStorage.getSteps().createDeposit(depositRequest);

        new UserDashboard().open().transferMoney().checkPageTitle(UserDashboard.makeTransferTitle);
        double transfer = RandomData.getRandomAmount(4000, 5000);
        new MakeTransfer()
                .selectAccount(senderAccountNumber)
                .enterRecipientName(RandomData.getUserName())
                .enterRecipientAccount(receiverAccountNumber)
                .enterAmount(transfer)
                .setCheckbox(true)
                .sendTransfer();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.TRANSFER_INCREASED_BALANCE.getMessage());

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(senderAccountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transfer);
    }
}
