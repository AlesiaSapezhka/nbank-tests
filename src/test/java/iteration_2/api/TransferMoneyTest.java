package iteration_2.api;

import api.dao.comparison.DaoAndModelAssertions;
import api.dao.TransactionsDao;
import api.generators.RandomData;
import api.models.*;
import api.requests.steps.DataBaseSteps;
import iteration_1.api.BaseTest;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseTest {
    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(Arguments.of(InvalidTransferCase.NEGATIVE), Arguments.of(InvalidTransferCase.EXCEED_LIMIT), Arguments.of(InvalidTransferCase.MORE_THAN_BALANCE));
    }

    @Test
    public void userCanTransferValidAmountOfMoneyToValidAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(
                userRequest.getUsername(),
                userRequest.getPassword()
        );

        CreateAccountResponse accountData = userSteps.createAccount();
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = userSteps.createAccount();
        int receiverAccountId = receiverAccountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        userSteps.createDeposit(createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(RandomData.getRandomAmount(100, 500)).build();
        CreateTransferResponse transferResponse = UserSteps.createTransfer(userRequest.getUsername(), userRequest.getPassword(), createTransferRequest);

        ModelAssertions.assertThatModels(createTransferRequest, transferResponse).match();
        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(senderAccountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(createTransferRequest.getAmount());
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.TRANSFER_OUT);

        GetTransactionsResponse transferOut = transactions.stream()
                .filter(t -> t.getType() == TransactionsTypes.TRANSFER_OUT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("TRANSFER_OUT transaction not found in API response"));

        List<TransactionsDao> transactionsDao = DataBaseSteps.getTransactionsByAccountId(senderAccountId);
        TransactionsDao transferOutDao = transactionsDao.stream()
                .filter(t -> t.getType() == TransactionsTypes.TRANSFER_OUT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("TRANSFER_OUT transaction not found in DB"));

        DaoAndModelAssertions.assertThat(transferOut, transferOutDao).match();
    }

    @Test
    public void userCanNotTransferValidAmountOfMoneyToInvalidAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(
                userRequest.getUsername(),
                userRequest.getPassword()
        );

        CreateAccountResponse accountData = userSteps.createAccount();
        int senderAccountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        userSteps.createDeposit(createDepositRequest);

        int InvalidReceiverId = 987;
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(InvalidReceiverId).amount(RandomData.getRandomAmount(100, 500)).build();
        UserSteps. createTransferWithInvalidAccount(userRequest.getUsername(), userRequest.getPassword(), createTransferRequest);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(senderAccountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(createTransferRequest.getAmount());

        List<TransactionsDao> transactionsDao = DataBaseSteps.getTransactionsByAccountId(senderAccountId);
        assertThat(transactionsDao)
                .extracting(TransactionsDao::getType)
                .doesNotContain(TransactionsTypes.TRANSFER_OUT);

    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferInvalidAmountOfMoneyToValidAccountTest(InvalidTransferCase invalidCase) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(
                userRequest.getUsername(),
                userRequest.getPassword()
        );

        CreateAccountResponse accountData = userSteps.createAccount();
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = userSteps.createAccount();
        int receiverAccountId = receiverAccountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 2000)).build();
        userSteps.createDeposit(createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(invalidCase.getTransferAmount()).build();
        UserSteps.createTransferWithInvalidCases(userRequest.getUsername(), userRequest.getPassword(), createTransferRequest,invalidCase);

        List<GetTransactionsResponse> transactions = userSteps.getAllTransactionsList(senderAccountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(invalidCase.getTransferAmount());
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).doesNotContain(TransactionsTypes.TRANSFER_OUT);

        List<TransactionsDao> transactionsDao = DataBaseSteps.getTransactionsByAccountId(senderAccountId);
        assertThat(transactionsDao)
                .extracting(TransactionsDao::getType)
                .doesNotContain(TransactionsTypes.TRANSFER_OUT);
    }
}
