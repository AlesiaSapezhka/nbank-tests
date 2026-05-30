package iteration_1;

import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.post_requests.AdminCreateUserRequester;
import requests.post_requests.CreateAccountRequester;
import requests.get_requests.GetAccountsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest {
    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();

        LoginUserRequest loginUserRequest = LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated()).post(userRequest);

        CreateAccountResponse accountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated()).post(null).extract().as(CreateAccountResponse.class);
        // get all accounts and check existing of account created above
        new GetAccountsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsAccountIdAndNumber(accountResponse.getId(), accountResponse.getAccountNumber())).get(null);
    }

}
