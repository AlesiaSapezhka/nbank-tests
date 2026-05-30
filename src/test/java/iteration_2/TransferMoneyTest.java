package iteration_2;

import generators.RandomData;
import iteration_1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.get_requests.GetTransactionsRequester;
import requests.post_requests.AdminCreateUserRequester;
import requests.post_requests.CreateAccountRequester;
import requests.post_requests.DepositRequester;
import requests.post_requests.TransferRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static specs.ResponseSpecs.entityWasCreated;

public class TransferMoneyTest extends BaseTest {
    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(Arguments.of(-500), Arguments.of(10000.01), Arguments.of(2000));
    }

    @Test
    public void userCanTransferValidAmountOfMoneyToValidAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), entityWasCreated()).post(userRequest);

        // create account and take it id
        int senderAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post(null).extract().path("id");
        // create receiver account and take it id
        int receiverAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post(null).extract().path("id");

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createDepositRequest).extract().as(CreateDepositResponse.class);

        // transfer money
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(RandomData.getRandomAmount(100, 500)).build();
        CreateTransferResponse transferResponse = new TransferRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createTransferRequest).extract().as(CreateTransferResponse.class);

        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getAmount()).isEqualTo(createTransferRequest.getAmount());
        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");

        // get all transactions
        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(senderAccountId).extract().jsonPath().getList("", GetTransactionsResponse.class);

        // Find current transaction
        GetTransactionsResponse transferTransaction = transactions.stream().filter(t -> t.getType() == TransactionsTypes.TRANSFER_OUT).findFirst().orElseThrow(() -> new AssertionError("Transfer transaction not found"));

        // Take all transactions via Get and check existing ours
        new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsTransactionsDetails(transferTransaction.getAmount(), transferTransaction.getType())).get(senderAccountId);
    }

    @Test
    public void userCanNotTransferValidAmountOfMoneyToInvalidAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), entityWasCreated()).post(userRequest);

        // create account and take it id
        int senderAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post(null).extract().path("id");

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 3000)).build();
        CreateDepositResponse createDepositResponse = new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createDepositRequest).extract().as(CreateDepositResponse.class);

        // transfer money
        int InvalidReceiverId = 987;
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(InvalidReceiverId).amount(RandomData.getRandomAmount(100, 500)).build();
        new TransferRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsBadRequestWithoutKey("Invalid transfer: insufficient funds or invalid accounts")).post(createTransferRequest);

    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferInvalidAmountOfMoneyToValidAccountTest(double transferAmount) {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), entityWasCreated()).post(userRequest);

        // create account and take it id
        int senderAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post(null).extract().path("id");
        // create receiver account and take it id
        int receiverAccountId = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post(null).extract().path("id");

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(100, 200)).build();
        CreateDepositResponse createDepositResponse = new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createDepositRequest).extract().as(CreateDepositResponse.class);

        // transfer money
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(transferAmount).build();
        new TransferRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsBadRequestWithoutKey("Invalid transfer: insufficient funds or invalid accounts")).post(createTransferRequest);

        // get all transactions
        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(senderAccountId).extract().jsonPath().getList("", GetTransactionsResponse.class);

        // Take all transactions and check Not existing ours
        softly.assertThat(transactions).noneMatch(t -> t.getAmount() == transferAmount && t.getType() == TransactionsTypes.TRANSFER_OUT && t.getRelatedAccountId() == receiverAccountId);
    }
}
