package iteration_2;

import generators.RandomData;
import iteration_1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;

import java.util.List;
import java.util.stream.Stream;

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
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountData = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());
        int accountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(accountId).balance(deposit).build();
        CreateDepositResponse createDepositResponse = UserSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);

        ModelAssertions.assertThatModels(createDepositRequest, createDepositResponse).match();
        softly.assertThat(createDepositResponse.getTransactions().get(0).getType()).isEqualTo(TransactionsTypes.DEPOSIT);

        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(userRequest.getUsername(), userRequest.getPassword(), accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(deposit);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.DEPOSIT);
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotCreateDepositWithInvalidDataTest(double deposit) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountData = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());
        int accountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(accountId).balance(deposit).build();
        UserSteps.createDepositInvalidData(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);

        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(userRequest.getUsername(), userRequest.getPassword(), accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(deposit);
    }

    @Test
    public void userCanNotCreateDepositForNotExistingAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountData = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());
        int accountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(RequestSpecs.INVALID_ACCOUNT_ID).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDepositInvalidAccount(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);

        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(userRequest.getUsername(), userRequest.getPassword(), accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(createDepositRequest.getBalance());
    }
}