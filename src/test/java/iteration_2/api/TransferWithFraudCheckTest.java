package iteration_2.api;

import api.generators.RandomData;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;
import common.annotations.FraudCheckMock;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import iteration_1.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )

    public void testTransferWithFraudCheck() {
        CreateUserRequest user1 = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(user1.getUsername(), user1.getPassword());

        CreateAccountResponse account1Data = userSteps.createAccount();
        int account_1_Id = account1Data.getId();

        CreateDepositRequest createDepositRequest = CreateDepositRequest.builder().accountId(account_1_Id).amount(RandomData.getRandomAmount(1000, 5000)).build();
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
        CreateTransferResponse transferResponse = UserSteps.createTransferWithFraudCheck(user1.getUsername(), user1.getPassword(), createTransferRequest);

        CreateTransferResponse expectedResponse = CreateTransferResponse.builder()
                .status(ResponseSpecs.FRAUD_APPROVED_STATUS)
                .message(ResponseSpecs.FRAUD_APPROVED_MESSAGE)
                .amount(createTransferRequest.getAmount())
                .senderAccountId((long) account_1_Id)
                .receiverAccountId((long) account_2_Id)
                .fraudRiskScore(0.2).fraudReason(ResponseSpecs.FRAUD_APPROVED_REASON)
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }
}