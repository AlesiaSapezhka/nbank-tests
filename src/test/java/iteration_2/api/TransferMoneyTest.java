package iteration_2.api;

import api.generators.RandomData;
import api.models.*;
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

public class TransferMoneyTest extends BaseTest {
    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(Arguments.of(-500), Arguments.of(10000.01), Arguments.of(2000));
    }

    @Test
    public void userCanTransferValidAmountOfMoneyToValidAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountData = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());
        int receiverAccountId = receiverAccountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(RandomData.getRandomAmount(100, 500)).build();
        CreateTransferResponse transferResponse = UserSteps.createTransfer(userRequest.getUsername(), userRequest.getPassword(), createTransferRequest);

        ModelAssertions.assertThatModels(createTransferRequest, transferResponse).match();
        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(userRequest.getUsername(), userRequest.getPassword(), senderAccountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(createTransferRequest.getAmount());
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.TRANSFER_OUT);
    }

    @Test
    public void userCanNotTransferValidAmountOfMoneyToInvalidAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountData = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());
        int senderAccountId = accountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        UserSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);

        int InvalidReceiverId = 987;
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(InvalidReceiverId).amount(RandomData.getRandomAmount(100, 500)).build();
        UserSteps.createTransferWithInvalidCases(userRequest.getUsername(), userRequest.getPassword(), createTransferRequest);

        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(userRequest.getUsername(), userRequest.getPassword(), senderAccountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(createTransferRequest.getAmount());

    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferInvalidAmountOfMoneyToValidAccountTest(double transferAmount) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountData = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());
        int senderAccountId = accountData.getId();

        CreateAccountResponse receiverAccountData = UserSteps.createAccount(userRequest.getUsername(), userRequest.getPassword());
        int receiverAccountId = receiverAccountData.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 2000)).build();
        UserSteps.createDeposit(userRequest.getUsername(), userRequest.getPassword(), createDepositRequest);

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(transferAmount).build();
        UserSteps.createTransferWithInvalidCases(userRequest.getUsername(), userRequest.getPassword(), createTransferRequest);

        List<GetTransactionsResponse> transactions = UserSteps.getAllTransactionsList(userRequest.getUsername(), userRequest.getPassword(), senderAccountId);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transferAmount);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).doesNotContain(TransactionsTypes.TRANSFER_OUT);
    }
}
