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
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_BAD_REQUEST).expectBody(equalTo(errorMessage)).build();
    }

    public static ResponseSpecification requestReturnsBadRequestWithoutMessage() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_BAD_REQUEST).build();
    }

    public static ResponseSpecification requestReturnsForbiddenRequestWithoutKey(String errorMessage) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_FORBIDDEN).expectBody(equalTo(errorMessage)).build();
    }
}
