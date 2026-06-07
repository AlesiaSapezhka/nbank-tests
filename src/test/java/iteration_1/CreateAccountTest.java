package iteration_1;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import requests.post_requests.AdminCreateUserRequester;
import requests.post_requests.CreateAccountRequester;
import requests.get_requests.GetAccountsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class CreateAccountTest extends BaseTest{
    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();

        LoginUserRequest loginUserRequest = LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated()).post(userRequest);

        CreateAccountResponse accountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.entityWasCreated()).post(null).extract().as(CreateAccountResponse.class);
        // get all accounts and check existing of account created above

        List<CreateAccountResponse> accounts = new GetAccountsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(null).extract().jsonPath().getList("", CreateAccountResponse.class);;
        softly.assertThat(accounts)
                .extracting(CreateAccountResponse::getId)
                .contains(accountResponse.getId());

        softly.assertThat(accounts)
                .extracting(CreateAccountResponse::getAccountNumber)
                .contains(accountResponse.getAccountNumber());

    }

}
