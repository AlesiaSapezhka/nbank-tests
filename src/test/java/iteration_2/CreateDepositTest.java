package iteration_2;

import generators.RandomData;
import iteration_1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse accountData = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.ACCOUNTS, entityWasCreated()).post(null);

        int accountId = accountData.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(accountId).balance(deposit).build();

        CreateDepositResponse createDepositResponse = new ValidatedCrudRequester<CreateDepositResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsOK()).post(createDepositRequest);

        softly.assertThat(createDepositResponse.getId()).isEqualTo(accountId);
        softly.assertThat(createDepositResponse.getBalance()).isEqualTo(createDepositRequest.getBalance());
        softly.assertThat(createDepositResponse.getTransactions().get(0).getType()).isEqualTo(TransactionsTypes.DEPOSIT);

        // get all transactions and check existing
//        new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsDepositDetails(createDepositResponse.getTransactions().get(0).getAmount(), createDepositResponse.getTransactions().get(0).getType())).get(accountId);

    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotCreateDepositWithInvalidDataTest(double deposit) {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse accountData = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.ACCOUNTS, entityWasCreated()).post(null);
        int accountId = accountData.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(accountId).balance(deposit).build();

        new CrudRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsBadRequestWithoutKey("Invalid account or amount")).post(createDepositRequest);
    }

    @Test
    public void userCanNotCreateDepositForNotExistingAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);

        // create account
        CreateAccountResponse accountData = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.ACCOUNTS, entityWasCreated()).post(null);

        // add deposit to unexisting Id
        int invalidAccountId = 134;
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(invalidAccountId).balance(RandomData.getRandomAmount(100, 200)).build();

        new CrudRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsForbiddenRequestWithoutKey("Unauthorized access to account")).post(createDepositRequest);
    }
}