package iteration_1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import common.annotations.AdminSession;
import org.junit.jupiter.api.Test;
import ui.element.UserBadge;
import ui.pages.AdminPanel;
import ui.pages.BankAlerts;

import java.util.List;

import static api.requests.steps.AdminSteps.getAllUsers;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserTest extends BaseUiTest {
    @Test
    @AdminSession
    public void adminCanCreateUserWithCorrectDataTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        UserBadge newUserBadge = new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword()).checkAlertMessageAndAccept(BankAlerts.USER_CREATED_SUCCESSFULLY.getMessage())
                .findUserByUsername(newUser.getUsername());

        assertThat(newUserBadge).as("User badge should exist on dashboard after creation").isNotNull();

        List<CreateUserResponse> users = getAllUsers();
        assertThat(users).anySatisfy(user -> ModelAssertions.assertThatModels(newUser, user).match());
    }

    @Test
    @AdminSession
    public void adminCanNotCreateUserWithIncorrectDataTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("al");
        assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword()).checkAlertMessageAndAccept(BankAlerts.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().stream().noneMatch(userBadge-> userBadge.getUsername().equals(newUser.getUsername())));

        List<CreateUserResponse> users = getAllUsers();
        assertThat(users).noneSatisfy(user -> ModelAssertions.assertThatModels(newUser, user).match());
    }
}
