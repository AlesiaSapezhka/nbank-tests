package iteration_1.ui;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;


public class LoginUserTest extends BaseUiTest {
    @Test
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        new LoginPage().open().login(admin.getUsername(), admin.getPassword()).checkPageTitle(AdminPanel.adminPanelTitle);
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser();
        new LoginPage().open().login(user.getUsername(), user.getPassword());
        new UserDashboard().checkWelcomeTextName(UserDashboard.defaultUserName);
    }
}
