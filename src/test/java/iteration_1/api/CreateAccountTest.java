package iteration_1.api;

import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.List;

public class CreateAccountTest extends BaseTest {
    @Test
    public void userCanCreateAccountTest() {

        CreateUserRequest userRequest = AdminSteps.createUser();
        CreateAccountResponse createAccountResponse = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());

        List<CreateAccountResponse> accounts = UserSteps.getAllAccountsList(userRequest.getUsername(), userRequest.getPassword());

        softly.assertThat(accounts).extracting(CreateAccountResponse::getId).contains(createAccountResponse.getId());
        softly.assertThat(accounts).extracting(CreateAccountResponse::getAccountNumber).contains(createAccountResponse.getAccountNumber());
    }
}
