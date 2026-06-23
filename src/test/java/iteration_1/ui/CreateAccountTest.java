package iteration_1.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlerts;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    public void userCanCreateAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер создает аккаунт
        new UserDashboard().open().createNewAccount();
        List<CreateAccountResponse> accounts = UserSteps.getAllAccountsList(user.getUsername(), user.getPassword());
        assertThat(accounts).hasSize(1);

        // ШАГ 5: проверка, что аккаунт создался на UI
        new UserDashboard().checkAlertMessageAndAccept(BankAlerts.NEW_ACCOUNT_CREATED.getMessage() + accounts.getFirst().getAccountNumber());
        assertThat(accounts.getFirst().getBalance()).isZero();
    }
}

