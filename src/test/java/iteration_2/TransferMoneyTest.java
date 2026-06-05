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

public class TransferMoneyTest extends BaseTest {
    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(Arguments.of(-500), Arguments.of(10000.01), Arguments.of(2000));
    }

    @Test
    public void userCanTransferValidAmountOfMoneyToValidAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse accountData = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.ACCOUNTS, entityWasCreated()).post(null);

        int senderAccountId = accountData.getId();

        // create receiver account and take it id
        CreateAccountResponse receiverAccountData = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.ACCOUNTS, entityWasCreated()).post(null);

        int receiverAccountId = receiverAccountData.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        CreateDepositResponse createDepositResponse = new ValidatedCrudRequester<CreateDepositResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsOK()).post(createDepositRequest);

        // transfer money
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(RandomData.getRandomAmount(100, 500)).build();
        CreateTransferResponse transferResponse = new ValidatedCrudRequester<CreateTransferResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.TRANSFER, ResponseSpecs.requestReturnsOK()).post(createTransferRequest);

        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getAmount()).isEqualTo(createTransferRequest.getAmount());
        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");

        // get all transactions
//        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(senderAccountId).extract().jsonPath().getList("", GetTransactionsResponse.class);

        // Find current transaction
//        GetTransactionsResponse transferTransaction = transactions.stream().filter(t -> t.getType() == TransactionsTypes.TRANSFER_OUT).findFirst().orElseThrow(() -> new AssertionError("Transfer transaction not found"));

        // Take all transactions via Get and check existing ours
//        new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsTransactionsDetails(transferTransaction.getAmount(), transferTransaction.getType())).get(senderAccountId);
    }

    @Test
    public void userCanNotTransferValidAmountOfMoneyToInvalidAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse accountData = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.ACCOUNTS, entityWasCreated()).post(null);

        int senderAccountId = accountData.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        CreateDepositResponse createDepositResponse = new ValidatedCrudRequester<CreateDepositResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsOK()).post(createDepositRequest);

        // transfer money
        int InvalidReceiverId = 987;

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(InvalidReceiverId).amount(RandomData.getRandomAmount(100, 500)).build();
        new CrudRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.TRANSFER, ResponseSpecs.requestReturnsBadRequestWithoutKey("Invalid transfer: insufficient funds or invalid accounts")).post(createTransferRequest);
    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferInvalidAmountOfMoneyToValidAccountTest(double transferAmount) {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse accountData = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.ACCOUNTS, entityWasCreated()).post(null);

        int senderAccountId = accountData.getId();

        // create receiver account and take it id
        CreateAccountResponse receiverAccountData = new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.ACCOUNTS, entityWasCreated()).post(null);

        int receiverAccountId = receiverAccountData.getId();
        // add deposit

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        CreateDepositResponse createDepositResponse = new ValidatedCrudRequester<CreateDepositResponse>(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsOK()).post(createDepositRequest);

        // transfer money

        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(transferAmount).build();
        new CrudRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), Endpoint.TRANSFER, ResponseSpecs.requestReturnsBadRequestWithoutKey("Invalid transfer: insufficient funds or invalid accounts")).post(createTransferRequest);

        // get all transactions
//        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(senderAccountId).extract().jsonPath().getList("", GetTransactionsResponse.class);
//
//        // Take all transactions and check Not existing ours
//        softly.assertThat(transactions).noneMatch(t -> t.getAmount() == transferAmount && t.getType() == TransactionsTypes.TRANSFER_OUT && t.getRelatedAccountId() == receiverAccountId);
    }
}
