package iteration_1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlerts;

import java.util.List;

import static api.requests.steps.AdminSteps.getAllUsers;
import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CreateUserTest extends BaseUiTest {
    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword()).checkAlertMessageAndAccept(BankAlerts.USER_CREATED_SUCCESSFULLY.getMessage()).getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

        // Отображается на API
        List<CreateUserResponse> users = getAllUsers();
        assertThat(users).anySatisfy(user -> ModelAssertions.assertThatModels(newUser, user).match());
    }

    @Test
    public void adminCanNotCreateUserWithIncorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("al");
        new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword()).checkAlertMessageAndAccept(BankAlerts.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage()).getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.visible);


        // НЕ Отображается на UI
        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        // НЕ отображается на API
        List<CreateUserResponse> users = getAllUsers();
        assertThat(users).noneSatisfy(user -> ModelAssertions.assertThatModels(newUser, user).match());
    }
}
