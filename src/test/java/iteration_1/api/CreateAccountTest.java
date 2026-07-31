package iteration_1.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;

import java.util.List;

public class CreateAccountTest extends BaseTest {
    @Test
    public void userCanCreateAccountTest() {

        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(
                userRequest.getUsername(),
                userRequest.getPassword()
        );
        CreateAccountResponse createAccountResponse = userSteps.createAccount();

        List<CreateAccountResponse> accounts = userSteps.getAllAccountsList();

        softly.assertThat(accounts).extracting(CreateAccountResponse::getId).contains(createAccountResponse.getId());
        softly.assertThat(accounts).extracting(CreateAccountResponse::getAccountNumber).contains(createAccountResponse.getAccountNumber());

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(createAccountResponse, accountDao).match();
    }
}
