package iteration_2.api;

import api.dao.TransactionsDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.RandomData;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import iteration_1.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().accountId(accountId).amount(deposit).build();
        CreateDepositResponse createDepositResponse = userSteps.createDeposit(createDepositRequest);

        ModelAssertions.assertThatModels(createDepositRequest, createDepositResponse).match();
        softly.assertThat(createDepositResponse.getTransactions().getFirst().getType()).isEqualTo(TransactionsTypes.DEPOSIT);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(deposit);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.DEPOSIT);

        GetTransactionsResponse transferIn = transactions.stream()
                .filter(t -> t.getType() == TransactionsTypes.DEPOSIT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("DEPOSIT transaction not found in API response"));

        List<TransactionsDao> transactionsDao = DataBaseSteps.getTransactionsByAccountId(accountId);
        TransactionsDao transferInDao = transactionsDao.stream()
                .filter(t -> t.getType() == TransactionsTypes.DEPOSIT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("DEPOSIT transaction not found in DB"));

        DaoAndModelAssertions.assertThat(transferIn, transferInDao).match();
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotCreateDepositWithInvalidDataTest(InvalidDepositCase invalidCase) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(userRequest.getUsername(), userRequest.getPassword());

        CreateAccountResponse accountData = userSteps.createAccount();
        int accountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().accountId(accountId).amount(invalidCase.getDepositAmount()).build();
        UserSteps.createDepositInvalidData(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest, invalidCase);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(invalidCase.getDepositAmount());

        List<TransactionsDao> depositDao = DataBaseSteps.getTransactionsByAccountId(accountId);
        assertThat(depositDao)
                .extracting(TransactionsDao::getType)
                .doesNotContain(TransactionsTypes.DEPOSIT);
    }

    @Test
    public void userCanNotCreateDepositForNotExistingAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(userRequest.getUsername(), userRequest.getPassword());

        CreateAccountResponse accountData = userSteps.createAccount();
        int accountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().accountId(RequestSpecs.INVALID_ACCOUNT_ID).amount(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDepositInvalidAccount(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(accountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(createDepositRequest.getAmount());

        List<TransactionsDao> depositDao = DataBaseSteps.getTransactionsByAccountId(accountId);
        assertThat(depositDao)
                .extracting(TransactionsDao::getType)
                .doesNotContain(TransactionsTypes.DEPOSIT);
    }
}