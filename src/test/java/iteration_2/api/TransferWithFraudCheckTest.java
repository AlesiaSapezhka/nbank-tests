package iteration_2.api;

import api.generators.RandomData;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import iteration_1.api.BaseTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static common.extensions.FraudCheckWireMockExtension.FRAUD_WIREMOCK_LOCK;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = FRAUD_WIREMOCK_LOCK, mode = ResourceAccessMode.READ_WRITE)
@ExtendWith(TimingExtension.class)
public class TransferWithFraudCheckTest extends BaseTest {

    @RegisterExtension
    static final FraudCheckWireMockExtension fraudMock = new FraudCheckWireMockExtension();

    static Stream<Arguments> fraudTransferCases() {
        return Stream.of(
                Arguments.of(FraudTransferCase.APPROVED),
                Arguments.of(FraudTransferCase.BLOCKED),
                Arguments.of(FraudTransferCase.REVIEW_REQUIRED),
                Arguments.of(FraudTransferCase.VERIFICATION_REQUIRED),
                Arguments.of(FraudTransferCase.SERVICE_HTTP_ERROR),
                Arguments.of(FraudTransferCase.SERVICE_TIMEOUT),
                Arguments.of(FraudTransferCase.SERVICE_CONNECTION_ERROR)
        );
    }

    @MethodSource("fraudTransferCases")
    @ParameterizedTest
    public void testTransferWithFraudCheck(FraudTransferCase fraudCase) {
        fraudMock.configure(fraudCase);

        CreateUserRequest user1 = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(user1.getUsername(), user1.getPassword());

        CreateAccountResponse account1Data = userSteps.createAccount();
        int account_1_Id = account1Data.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder()
                .accountId(account_1_Id)
                .amount(RandomData.getRandomAmount(1000, 5000))
                .build();
        userSteps.createDeposit(createDepositRequest);

        CreateUserRequest user2 = AdminSteps.createUser();
        UserSteps user2Steps = new UserSteps(user2.getUsername(), user2.getPassword());

        CreateAccountResponse account2Data = user2Steps.createAccount();
        int account_2_Id = account2Data.getId();

        CreateTransferRequest createTransferRequest = CreateTransferRequest
                .builder()
                .senderAccountId(account_1_Id)
                .receiverAccountId(account_2_Id)
                .amount(RandomData.getRandomAmount(100, 500))
                .build();
        CreateTransferResponse transferResponse = UserSteps.createTransferWithFraudCheck(
                user1.getUsername(), user1.getPassword(), createTransferRequest);

        CreateTransferResponse expectedResponse = fraudCase.expectedResponse(
                createTransferRequest.getAmount(),
                account_1_Id,
                account_2_Id
        );

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }
}
