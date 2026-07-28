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
import ui.pages.DepositMoney;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateDepositTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateDepositTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        String accountNumber = accountData.getAccountNumber();
        int accountId = accountData.getId();

        new UserDashboard().open().depositMoney().checkPageTitle(UserDashboard.depositMoneyTitle);
        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(1000, 5000)).build();

        new DepositMoney()
                .selectAccount(accountNumber)
                .enterAmount(deposit.getBalance())
                .clickDeposit()
                .shouldHaveDepositSuccessAlert(deposit.getBalance(), accountNumber);

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(accountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(deposit.getBalance());
        assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.DEPOSIT);
    }

    @Test
    @UserSession
    public void userCanNotCreateDepositMoreThanLimitTest() {
        CreateAccountResponse accountData = SessionStorage.getSteps().createAccount();
        String accountNumber = accountData.getAccountNumber();
        int accountId = accountData.getId();

        new UserDashboard().open().depositMoney().checkPageTitle(UserDashboard.depositMoneyTitle);
        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(5001, 6000)).build();
        new DepositMoney().selectAccount(accountNumber).enterAmount(deposit.getBalance()).clickDeposit();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.DEPOSIT_UNSUCCESSFUL.getMessage());

        List<GetTransactionsResponse> transactions = SessionStorage.getSteps().getAllTransactionsList(accountId);
        assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(deposit.getBalance());
    }

    @Test
    @UserSession
    public void userCanNotCreateDepositWithoutSelectingAccountTest() {
        new UserDashboard().open().depositMoney().checkPageTitle(UserDashboard.depositMoneyTitle);
        CreateDepositRequest deposit = CreateDepositRequest.builder().balance(RandomData.getRandomAmount(10, 600)).build();
        new DepositMoney().enterAmount(deposit.getBalance()).clickDeposit();

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.DEPOSIT_WITHOUT_SELECTING_ACCOUNT.getMessage());
    }
}
