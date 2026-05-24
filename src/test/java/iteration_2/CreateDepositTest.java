package iteration_2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

public class CreateDepositTest {
    @BeforeAll
    public static void setUpRestAssured() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));

    }

    public static Stream<Arguments> depositValidData() {
        return Stream.of(Arguments.of(0.01), Arguments.of(5000), Arguments.of(4999.99));
    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(Arguments.of(-500.0), Arguments.of(5000.01));
    }

    @MethodSource("depositValidData")
    @ParameterizedTest
    public void userCanCreateDepositWithValidDataTest(double deposit) {
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                    "username":"mike-1998",
                    "password":"verysTRongPassword33$"
                }
                """).post("http://localhost:4111/api/v1/auth/login").then().statusCode(HttpStatus.SC_OK).extract().header("Authorization");
        // create account and take it id
        int accountId = given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).post("http://localhost:4111/api/v1/accounts").then().statusCode(HttpStatus.SC_CREATED).extract().path("id");
        // add deposit
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                    "id": %s,
                    "balance": %s
                }
                """.formatted(accountId, deposit)).post("http://localhost:4111/api/v1/accounts/deposit").then().statusCode(HttpStatus.SC_OK).body("id", Matchers.equalTo(accountId)).body("balance", Matchers.equalTo((float) deposit));
        // get all transactions and check existing
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/accounts/{accountId}/transactions", accountId).then().body("amount.flatten()", hasItem((float) deposit)).body("type", hasItem("DEPOSIT"));
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotCreateDepositWithInvalidDataTest(double deposit) {
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "username":"mike-1998",
                        "password":"verysTRongPassword33$"
                                }
                """).post("http://localhost:4111/api/v1/auth/login").then().assertThat().statusCode(HttpStatus.SC_OK).extract().header("Authorization");
        // create account and take it id
        int accountId = given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).post("http://localhost:4111/api/v1/accounts").then().assertThat().statusCode(HttpStatus.SC_CREATED).extract().path("id");
        // add deposit
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                    "id": %s,
                    "balance":  %s
                }
                """.formatted(accountId, deposit)).post("http://localhost:4111/api/v1/accounts/deposit").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).body(Matchers.equalTo("Invalid account or amount"));
        // get all transactions and check not existing
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/accounts/{accountId}/transactions", accountId).then().body("amount.flatten()", not(hasItem((float) deposit)));
    }

    @Test
    public void userCanNotCreateDepositForNotExistingAccountTest() {
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "username":"mike-1998",
                        "password":"verysTRongPassword33$"
                                }
                """).post("http://localhost:4111/api/v1/auth/login").then().assertThat().statusCode(HttpStatus.SC_OK).extract().header("Authorization");
        // add deposit
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                    "id": 134,
                    "balance":  688
                }
                """).post("http://localhost:4111/api/v1/accounts/deposit").then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN).body(Matchers.equalTo("Unauthorized access to account"));
    }
}