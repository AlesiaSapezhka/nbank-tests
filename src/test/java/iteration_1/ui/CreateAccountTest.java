package iteration_1.ui;

import api.models.CreateAccountResponse;
import common.annotations.Browsers;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlerts;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    @UserSession
    @Browsers("chrome")
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();
        List<CreateAccountResponse> accounts = SessionStorage.getSteps().getAllAccountsList();
        assertThat(accounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.NEW_ACCOUNT_CREATED.getMessage() + accounts.getFirst().getAccountNumber());
        assertThat(accounts.getFirst().getBalance()).isZero();
    }
}

