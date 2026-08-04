package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class ResponseSpecs {

    private ResponseSpecs() {
    }

    public static final String PROFILE_UPDATED =
            "Profile updated successfully";

    public static final String TRANSFER_SUCCESSFUL =
            "Transfer successful";

    public static final String TRANSFER_UNSUCCESSFUL =
            "Invalid transfer: insufficient funds or invalid accounts";

    public static final String UNAUTHORIZED_ACCESS =
            "Unauthorized access to account";

    public static final String FRAUD_APPROVED_STATUS =
            "APPROVED";

    public static final String FRAUD_APPROVED_MESSAGE =
            "Transfer approved and processed immediately";

    public static final String  FRAUD_APPROVED_REASON =
            "Low risk transaction";

    public static final String FRAUD_BLOCKED_STATUS =
            "BLOCKED";

    public static final String FRAUD_BLOCKED_MESSAGE =
            "Transfer blocked due to fraud detection";

    public static final String FRAUD_REVIEW_REQUIRED_STATUS =
            "MANUAL_REVIEW_REQUIRED";

    public static final String FRAUD_REVIEW_REQUIRED_MESSAGE =
            "Transfer requires manual review";

    public static final String FRAUD_VERIFICATION_REQUIRED_STATUS =
            "VERIFICATION_REQUIRED";

    public static final String FRAUD_VERIFICATION_REQUIRED_MESSAGE =
            "Additional verification required";

    public static final String FRAUD_SERVICE_UNAVAILABLE_REASON =
            "Fraud detection service is currently unavailable";

    public static final String FRAUD_SERVICE_HTTP_ERROR_REASON =
            "Unexpected error during fraud check: 500 Server Error: \"{\"error\":\"fraud service error\"}\"";

    public static final String FRAUD_SERVICE_TIMEOUT_REASON =
            "Unexpected error during fraud check: Cannot invoke \"java.lang.Boolean.booleanValue()\" because the return value of \"me.nobugs.bank.services.FraudDetectionClientService$FraudCheckResponse.getRequiresManualReview()\" is null";




    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_CREATED).build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorKey, List<String> errorValues) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.containsInAnyOrder(errorValues.toArray(String[]::new)))
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequestWithoutKey(String errorMessage) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_BAD_REQUEST).expectBody("message", equalTo(errorMessage)).build();
    }

    public static ResponseSpecification requestReturnsBadRequestWithoutMessage() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_BAD_REQUEST).build();
    }

    public static ResponseSpecification requestReturnsForbiddenRequestWithoutKey(String errorMessage) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_FORBIDDEN).expectBody(equalTo(errorMessage)).build();
    }
}
