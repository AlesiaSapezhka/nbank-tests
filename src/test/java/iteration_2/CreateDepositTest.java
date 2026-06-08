package iteration_2;

import generators.RandomData;
import iteration_1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.post_requests.AdminCreateUserRequester;
import requests.post_requests.CreateAccountRequester;
import requests.post_requests.DepositRequester;
import requests.get_requests.GetTransactionsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static specs.ResponseSpecs.entityWasCreated;

public class CreateDepositTest extends BaseTest {

    public static Stream<Arguments> depositValidData() {
        return Stream.of(Arguments.of(0.01), Arguments.of(5000), Arguments.of(4999.99));
    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(Arguments.of(-500.0), Arguments.of(5000.01), Arguments.of(0));
    }

    @MethodSource("depositValidData")
    @ParameterizedTest
    public void userCanCreateDepositWithValidDataTest(double deposit) {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse accountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        int accountId = accountResponse.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(accountId).balance(deposit).build();

        CreateDepositResponse createDepositResponse = new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createDepositRequest).extract().as(CreateDepositResponse.class);

        softly.assertThat(createDepositResponse.getId()).isEqualTo(accountId);
        softly.assertThat(createDepositResponse.getBalance()).isEqualTo(createDepositRequest.getBalance());
        softly.assertThat(createDepositResponse.getTransactions().get(0).getType()).isEqualTo(TransactionsTypes.DEPOSIT);

        // get all transactions and check existing
        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(accountId).extract().jsonPath().getList("", GetTransactionsResponse.class);;

        softly.assertThat(transactions)
                .extracting(GetTransactionsResponse::getAmount)
                .contains(deposit);

        softly.assertThat(transactions)
                .extracting(GetTransactionsResponse::getType)
                .contains(TransactionsTypes.DEPOSIT);

    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotCreateDepositWithInvalidDataTest(double deposit) {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse accountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        int accountId = accountResponse.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(accountId).balance(deposit).build();
        new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsBadRequestWithoutKey(ResponseSpecs.INVALID_ACCOUNT)).post(createDepositRequest);

        // get all transactions and check Not existing
        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(accountId).extract().jsonPath().getList("", GetTransactionsResponse.class);;

        softly.assertThat(transactions)
                .extracting(GetTransactionsResponse::getAmount)
                .doesNotContain(deposit);
    }

    @Test
    public void userCanNotCreateDepositForNotExistingAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), entityWasCreated()).post(userRequest);

        // create account
        CreateAccountResponse accountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        int accountId = accountResponse.getId();

        // add deposit to unexisting Id
        int invalidAccountId = 134;
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(invalidAccountId).balance(RandomData.getRandomAmount(100, 200)).build();
        new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsForbiddenRequestWithoutKey(ResponseSpecs.UNAUTHORIZED_ACCESS)).post(createDepositRequest);

        // get all transactions and check Not existing
        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(accountId).extract().jsonPath().getList("", GetTransactionsResponse.class);;

        softly.assertThat(transactions)
                .extracting(GetTransactionsResponse::getAmount)
                .doesNotContain(createDepositRequest.getBalance());
    }
}