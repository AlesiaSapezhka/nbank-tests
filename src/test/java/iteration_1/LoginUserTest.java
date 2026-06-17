package iteration_1;

import models.*;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;

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
