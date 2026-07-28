package iteration_2.api;

import api.generators.RandomData;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import iteration_1.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class CreateDepositTest extends BaseTest {

    static Stream<Arguments> depositInvalidData() {
        return Stream.of(Arguments.of(InvalidDepositCase.NEGATIVE), Arguments.of(InvalidDepositCase.EXCEED_LIMIT), Arguments.of(InvalidDepositCase.ZERO));
    }

    public static Stream<Arguments> depositValidData() {
        return Stream.of(Arguments.of(0.01), Arguments.of(5000), Arguments.of(4999.99));
    }

    @MethodSource("depositValidData")
    @ParameterizedTest
    public void userCanCreateDepositWithValidDataTest(double deposit) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(userRequest.getUsername(), userRequest.getPassword());

        CreateAccountResponse accountData = userSteps.createAccount();
        int accountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(accountId).balance(deposit).build();
        CreateDepositResponse createDepositResponse = userSteps.createDeposit(createDepositRequest);

        ModelAssertions.assertThatModels(createDepositRequest, createDepositResponse).match();
        softly.assertThat(createDepositResponse.getTransactions().getFirst().getType()).isEqualTo(TransactionsTypes.DEPOSIT);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(deposit);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.DEPOSIT);
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotCreateDepositWithInvalidDataTest(InvalidDepositCase invalidCase) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(userRequest.getUsername(), userRequest.getPassword());

        CreateAccountResponse accountData = userSteps.createAccount();
        int accountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(accountId).balance(invalidCase.getDepositAmount()).build();
        UserSteps.createDepositInvalidData(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest, invalidCase);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(invalidCase.getDepositAmount());
    }

    @Test
    public void userCanNotCreateDepositForNotExistingAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(userRequest.getUsername(), userRequest.getPassword());

        CreateAccountResponse accountData = userSteps.createAccount();
        int accountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(RequestSpecs.INVALID_ACCOUNT_ID).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDepositInvalidAccount(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(createDepositRequest.getBalance());
    }
}