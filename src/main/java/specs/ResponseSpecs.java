package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import models.TransactionsTypes;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import static org.hamcrest.Matchers.*;

public class ResponseSpecs {

    private ResponseSpecs() {
    }

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_CREATED).build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorKey, String errorValue) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_BAD_REQUEST).expectBody(errorKey, equalTo(errorValue)).build();
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

    public static ResponseSpecification profileWasUpdated(String message, String value) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).expectBody(message, equalTo(value)).build();
    }

    public static ResponseSpecification requestReturnsAccountIdAndNumber(int accountIdValue, String accountNumberValue) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).expectBody("id", hasItem(accountIdValue)).expectBody("accountNumber", hasItem(accountNumberValue)).build();
    }

    public static ResponseSpecification requestReturnsDepositDetails(double amount, TransactionsTypes type) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).expectBody("[0].amount", equalTo((float) amount)).expectBody("[0].type", equalTo(TransactionsTypes.DEPOSIT.name())).build();
    }

    public static ResponseSpecification requestReturnsTransactionsDetails(double amount, TransactionsTypes type) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).expectBody("[0].amount", equalTo((float) amount)).expectBody("[0].type", equalTo(TransactionsTypes.TRANSFER_OUT.name())).build();
    }

    public static ResponseSpecification requestReturnsUsersList(String userName, String role) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).expectBody("username", hasItem(userName)).expectBody("role", hasItem(role)).build();
    }

    public static ResponseSpecification requestReturnsUserProfile(String name) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).expectBody("name", equalTo(name)).build();
    }

    public static ResponseSpecification requestReturnsNull() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).expectBody("name", nullValue()).build();
    }

    public static ResponseSpecification requestReturnsUsersListWithoutUser(String userName) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).expectBody("username", Matchers.not(hasItem(userName))).build();
    }
}
