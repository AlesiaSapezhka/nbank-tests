package iteration_2.ui;

import api.models.CreateUserResponse;
import api.models.InvalidChangeNameCase;
import api.models.UpdateProfileRequest;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Selenide;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration_1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlerts;
import ui.pages.EditProfile;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanChangeNameTest() {
        new UserDashboard().open().moveToEditProfile().checkPageTitle(UserDashboard.editProfileTitle);
        UpdateProfileRequest newName = UserSteps.generateValidName();
        new EditProfile().changeName(newName.getName());

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.NAME_UPDATED.getMessage());
        Selenide.refresh();
        new EditProfile().checkNameTextContains(newName.getName(), true).goToHomePage();

        new UserDashboard().checkWelcomeTextName(newName.getName());

        CreateUserResponse userProfile = SessionStorage.getSteps().getProfileInfo();
        assertThat(userProfile.getName()).isEqualTo(newName.getName());
    }

    @Test
    @UserSession
    public void userCanNotChangeNameForInvalidTest() {
        new UserDashboard().open().moveToEditProfile().checkPageTitle(UserDashboard.editProfileTitle);
        UpdateProfileRequest newName = UserSteps.generateInvalidName(InvalidChangeNameCase.THREE_WORDS);
        new EditProfile().changeName(newName.getName());

        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.ENTER_VALID_NAME.getMessage());
        Selenide.refresh();
        new EditProfile().checkNameTextContains(newName.getName(), false).goToHomePage();

        new UserDashboard().checkWelcomeTextName(UserDashboard.defaultUserName);

        CreateUserResponse userProfile = SessionStorage.getSteps().getProfileInfo();
        assertThat(userProfile.getName()).isEqualTo(null);
    }
}
