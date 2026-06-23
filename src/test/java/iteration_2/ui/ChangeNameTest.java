package iteration_2.ui;

import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.InvalidChangeNameCase;
import api.models.UpdateProfileRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Selenide;
import iteration_1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlerts;
import ui.pages.EditProfile;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest extends BaseUiTest {
    @Test
    public void userCanChangeNameTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет имя
        new UserDashboard().open().moveToEditProfile().checkPageTitle("✏\uFE0F Edit Profile");
        UpdateProfileRequest newName = UserSteps.generateValidName();
        new EditProfile().changeName(newName.getName());

        // ШАГ 5: проверка, что имя сменилось на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.NAME_UPDATED.getMessage());
        Selenide.refresh();
        new EditProfile().checkNameTextContains(newName.getName(), true).goToHomePage();

        // ШАГ 6: юзер переходит на главную страницу и проверяет велком сообщение с новым именем
        new UserDashboard().checkWelcomeTextName(newName.getName());

        // ШАГ 7: проверка, что имя сменено на API
        CreateUserResponse userProfile = UserSteps.getProfileInfo(user.getUsername(), user.getPassword());
        assertThat(userProfile.getName()).isEqualTo(newName.getName());
    }

    @Test
    public void userCanNotChangeNameForInvalidTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет имя
        new UserDashboard().open().moveToEditProfile().checkPageTitle("✏\uFE0F Edit Profile");
        UpdateProfileRequest newName = UserSteps.generateInvalidName(InvalidChangeNameCase.THREE_WORDS);
        new EditProfile().changeName(newName.getName());

        // ШАГ 5: проверка, что имя НЕ сменилось на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.ENTER_VALID_NAME.getMessage());
        Selenide.refresh();
        new EditProfile().checkNameTextContains(newName.getName(), false).goToHomePage();

        // ШАГ 6: юзер переходит на главную страницу и проверяет велком сообщение со старым именем
        new UserDashboard().checkWelcomeTextName("noname");

        // ШАГ 7: проверка, что имя НЕ сменено на API
        CreateUserResponse userProfile = UserSteps.getProfileInfo(user.getUsername(), user.getPassword());
        assertThat(userProfile.getName()).isEqualTo(null);
    }
}
