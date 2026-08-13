package api.models;

import api.specs.ResponseSpecs;
import lombok.Getter;

@Getter
public enum FraudTransferCase {

    APPROVED(
            FraudMockType.SUCCESS_RESPONSE,
            "SUCCESS",
            "APPROVED",
            0.2,
            "Low risk transaction",
            false,
            false,
            ResponseSpecs.FRAUD_APPROVED_STATUS,
            ResponseSpecs.FRAUD_APPROVED_MESSAGE
    ),
    BLOCKED(
            FraudMockType.SUCCESS_RESPONSE,
            "SUCCESS",
            "BLOCKED",
            0.2,
            "Low risk transaction",
            false,
            false,
            ResponseSpecs.FRAUD_BLOCKED_STATUS,
            ResponseSpecs.FRAUD_BLOCKED_MESSAGE
    ),
    REVIEW_REQUIRED(
            FraudMockType.SUCCESS_RESPONSE,
            "SUCCESS",
            "REVIEW_REQUIRED",
            0.2,
            "Low risk transaction",
            true,
            false,
            ResponseSpecs.FRAUD_REVIEW_REQUIRED_STATUS,
            ResponseSpecs.FRAUD_REVIEW_REQUIRED_MESSAGE
    ),
    VERIFICATION_REQUIRED(
            FraudMockType.SUCCESS_RESPONSE,
            "SUCCESS",
            "VERIFICATION_REQUIRED",
            0.2,
            "Low risk transaction",
            false,
            true,
            ResponseSpecs.FRAUD_VERIFICATION_REQUIRED_STATUS,
            ResponseSpecs.FRAUD_VERIFICATION_REQUIRED_MESSAGE
    ),
    SERVICE_HTTP_ERROR(
            FraudMockType.HTTP_ERROR,
            null,
            null,
            0.5,
            ResponseSpecs.FRAUD_SERVICE_HTTP_ERROR_REASON,
            true,
            false,
            ResponseSpecs.FRAUD_REVIEW_REQUIRED_STATUS,
            ResponseSpecs.FRAUD_REVIEW_REQUIRED_MESSAGE
    ),
    SERVICE_TIMEOUT(
            FraudMockType.TIMEOUT,
            null,
            null,
            0.5,
            ResponseSpecs.FRAUD_SERVICE_TIMEOUT_REASON,
            true,
            false,
            ResponseSpecs.FRAUD_REVIEW_REQUIRED_STATUS,
            ResponseSpecs.FRAUD_REVIEW_REQUIRED_MESSAGE
    ),
    SERVICE_CONNECTION_ERROR(
            FraudMockType.CONNECTION_ERROR,
            null,
            null,
            0.5,
            ResponseSpecs.FRAUD_SERVICE_UNAVAILABLE_REASON,
            true,
            false,
            ResponseSpecs.FRAUD_REVIEW_REQUIRED_STATUS,
            ResponseSpecs.FRAUD_REVIEW_REQUIRED_MESSAGE
    );

    private final FraudMockType mockType;
    private final String mockStatus;
    private final String mockDecision;
    private final double riskScore;
    private final String reason;
    private final boolean requiresManualReview;
    private final boolean additionalVerificationRequired;
    private final String expectedStatus;
    private final String expectedMessage;

    FraudTransferCase(
            FraudMockType mockType,
            String mockStatus,
            String mockDecision,
            double riskScore,
            String reason,
            boolean requiresManualReview,
            boolean additionalVerificationRequired,
            String expectedStatus,
            String expectedMessage
    ) {
        this.mockType = mockType;
        this.mockStatus = mockStatus;
        this.mockDecision = mockDecision;
        this.riskScore = riskScore;
        this.reason = reason;
        this.requiresManualReview = requiresManualReview;
        this.additionalVerificationRequired = additionalVerificationRequired;
        this.expectedStatus = expectedStatus;
        this.expectedMessage = expectedMessage;
    }

    public CreateTransferResponse expectedResponse(double amount, long senderAccountId, long receiverAccountId) {
        return CreateTransferResponse.builder()
                .status(expectedStatus)
                .message(expectedMessage)
                .amount(amount)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(riskScore)
                .fraudReason(reason)
                .requiresManualReview(requiresManualReview)
                .requiresVerification(additionalVerificationRequired)
                .build();
    }
}
