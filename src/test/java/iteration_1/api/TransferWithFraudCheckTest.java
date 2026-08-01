package iteration1.api;

import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import common.extensions.TimingExtension;
import iteration1.api.FraudCheckWireMockExtension;
import common.annotations.FraudCheckMock;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

    private CreateUserRequest user1;
    private CreateUserRequest user2;
    private CreateAccountResponse account1;
    private CreateAccountResponse account2;
    private DepositResponse depositResponse;
    private TransferResponse transferResponse;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

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
        // Step 1: Create user1
        user1 = AdminSteps.createUser();

        // Step 2: Create account1 for user1
        AccountSteps accountSteps1 = new AccountSteps(user1.getUsername(), user1.getPassword());
        account1 = accountSteps1.createAccount();

        // Step 3: Deposit random amount between 0.1 and 5000 to account1
        double depositAmount = Math.random() * 4999.9 + 0.1; // Random between 0.1 and 5000
        System.out.println("💰 Random deposit amount: " + String.format("%.2f", depositAmount));
        depositResponse = accountSteps1.depositToAccount(account1.getId(), depositAmount);

        // Step 4: Create user2
        user2 = AdminSteps.createUser();
        // Step 5: Create account2 for user2
        AccountSteps accountSteps2 = new AccountSteps(user2.getUsername(), user2.getPassword());
        account2 = accountSteps2.createAccount();
        // Step 6: Send POST to transfer-with-fraud-check endpoint
        // Transfer random amount (less than deposit) from account1 to account2
        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1; // Random between 0.1 and depositAmount
        System.out.println("💸 Random transfer amount: " + String.format("%.2f", transferAmount));
        transferResponse = accountSteps1.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                transferAmount
        );

        // Step 7: Assert that POST returned success using model comparison
        softly.assertThat(transferResponse).isNotNull();

        // Create expected response model for comparison
        // Using the same values as configured in @FraudCheckMock annotation
        TransferResponse expectedResponse = TransferResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)  // Dynamic transfer amount
                .senderAccountId(account1.getId())
                .receiverAccountId(account2.getId())
                .fraudRiskScore(0.2)  // From @FraudCheckMock(riskScore = 0.2)
                .fraudReason("Low risk transaction")  // From @FraudCheckMock(reason = "Low risk transaction")
                .requiresManualReview(false)  // From @FraudCheckMock(requiresManualReview = false)
                .requiresVerification(false)  // From @FraudCheckMock(additionalVerificationRequired = false)
                .build();

        // Use ModelAssertions for comprehensive model comparison
        // This will use the comparison rules from model-comparison.properties
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();

    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}