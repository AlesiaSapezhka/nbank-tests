package iteration_1.api;

import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import api.models.UserRole;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;

import static org.assertj.core.api.Assertions.assertThat;


public class LoginUserTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = AdminSteps.createAdmin();
        LoginUserResponse userResponse = AdminSteps.login(userRequest.getUsername(), userRequest.getPassword());

        assertThat(userResponse.getRole()).isEqualTo(UserRole.ADMIN.toString());
        assertThat(userResponse.getUsername()).isEqualTo(userRequest.getUsername());
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest user = AdminSteps.createUser();
        LoginUserResponse userResponse = AdminSteps.login(user.getUsername(), user.getPassword());

        assertThat(userResponse.getRole()).isEqualTo(UserRole.USER.toString());
        assertThat(userResponse.getUsername()).isEqualTo(user.getUsername());
    }
}
