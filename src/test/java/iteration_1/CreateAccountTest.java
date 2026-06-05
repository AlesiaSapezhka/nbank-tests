package iteration_1;

import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest {
    @Test
    public void userCanCreateAccountTest() {
        // Create user
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);
        // login as User
        new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build()).header("Authorization", Matchers.notNullValue());

        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

       // get all accounts and check existing of account created above
        new CrudRequester
                (RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.CUSTOMER_ACCOUNTS,
                        ResponseSpecs.requestReturnsAccountIdAndNumber(accountResponse.getId(), accountResponse.getAccountNumber()))
                .get(null);
    }
}
