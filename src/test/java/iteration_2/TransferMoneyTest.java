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
        CreateAccountResponse senderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        int senderAccountId = senderAccountResponse.getId();

        // create receiver account and take it id
        CreateAccountResponse receiverAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        int receiverAccountId = receiverAccountResponse.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 5000)).build();
        new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createDepositRequest).extract().as(CreateDepositResponse.class);

        // transfer money
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(RandomData.getRandomAmount(100, 500)).build();
        CreateTransferResponse transferResponse = new TransferRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createTransferRequest).extract().as(CreateTransferResponse.class);

        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(senderAccountId);
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(receiverAccountId);
        softly.assertThat(transferResponse.getAmount()).isEqualTo(createTransferRequest.getAmount());
        softly.assertThat(transferResponse.getMessage()).isEqualTo(ResponseSpecs.TRANSFER_SUCCESSFUL);

        // get all transactions
        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(senderAccountId).extract().jsonPath().getList("", GetTransactionsResponse.class);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).contains(createTransferRequest.getAmount());
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).contains(TransactionsTypes.TRANSFER_OUT);

    }

    @Test
    public void userCanNotTransferValidAmountOfMoneyToInvalidAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse accountResponse1 = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        int senderAccountId = accountResponse1.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(1000, 3000)).build();
        CreateDepositResponse createDepositResponse = new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createDepositRequest).extract().as(CreateDepositResponse.class);

        // transfer money
        int InvalidReceiverId = 987;
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(InvalidReceiverId).amount(RandomData.getRandomAmount(100, 500)).build();
        new TransferRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsBadRequestWithoutKey(ResponseSpecs.INVALID_TRANSFER)).post(createTransferRequest);

        // get all transactions
        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(senderAccountId).extract().jsonPath().getList("", GetTransactionsResponse.class);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(createTransferRequest.getAmount());

    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferInvalidAmountOfMoneyToValidAccountTest(double transferAmount) {
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), entityWasCreated()).post(userRequest);

        // create account and take it id
        CreateAccountResponse senderAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        int senderAccountId = senderAccountResponse.getId();

        // create receiver account and take it id
        CreateAccountResponse receiverAccountResponse = new CreateAccountRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), entityWasCreated()).post().extract().as(CreateAccountResponse.class);
        int receiverAccountId = receiverAccountResponse.getId();

        // add deposit
        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().id(senderAccountId).balance(RandomData.getRandomAmount(100, 200)).build();
        CreateDepositResponse createDepositResponse = new DepositRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).post(createDepositRequest).extract().as(CreateDepositResponse.class);

        // transfer money
        CreateTransferRequest createTransferRequest = CreateTransferRequest.builder().senderAccountId(senderAccountId).receiverAccountId(receiverAccountId).amount(transferAmount).build();
        new TransferRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsBadRequestWithoutKey(ResponseSpecs.TRANSFER_UNSUCCESSFUL)).post(createTransferRequest);

        // get all transactions and check Not existing ours
        List<GetTransactionsResponse> transactions = new GetTransactionsRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsOK()).get(senderAccountId).extract().jsonPath().getList("", GetTransactionsResponse.class);
        softly.assertThat(transactions).extracting(GetTransactionsResponse::getAmount).doesNotContain(transferAmount);

        softly.assertThat(transactions).extracting(GetTransactionsResponse::getType).doesNotContain(TransactionsTypes.TRANSFER_OUT);
    }
}
