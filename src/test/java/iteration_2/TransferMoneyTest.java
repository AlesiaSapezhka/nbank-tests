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

public class TransferMoneyTest {
    @BeforeAll
    public static void setUpRestAssured() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(Arguments.of(-500), Arguments.of(10000.01), Arguments.of(2000));
    }

    @Test
    public void userCanTransferValidAmountOfMoneyToValidAccountTest() {
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
                    "balance": 10000
                }
                """.formatted(accountId)).post("http://localhost:4111/api/v1/accounts/deposit").then().assertThat().statusCode(HttpStatus.SC_OK).body("id", Matchers.equalTo(accountId)).body("balance", Matchers.equalTo(10000.0f));
        // transfer money
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                  "senderAccountId": %s,
                  "receiverAccountId": 3,
                  "amount": 0.01
                }
                """.formatted(accountId)).post("http://localhost:4111/api/v1/accounts/transfer").then().assertThat().statusCode(HttpStatus.SC_OK).body("senderAccountId", Matchers.equalTo(accountId)).body("message", Matchers.equalTo("Transfer successful")).body("amount", Matchers.equalTo(0.01f)).body("receiverAccountId", Matchers.equalTo(3));
        // get all transactions and check existing
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/accounts/{accountId}/transactions", accountId).then().body("amount", hasItem(0.01f)).body("type", hasItem("TRANSFER_OUT"));
    }

    @Test
    public void userCanTransferValidAmountOfMoneyToTheSameAccountTest() {
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
                    "balance": 1000
                }
                """.formatted(accountId)).post("http://localhost:4111/api/v1/accounts/deposit").then().assertThat().statusCode(HttpStatus.SC_OK).body("id", Matchers.equalTo(accountId)).body("balance", Matchers.equalTo(1000.0f));
        // transfer money
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                  "senderAccountId": %s,
                  "receiverAccountId": %s,
                  "amount": 100
                }
                """.formatted(accountId, accountId)).post("http://localhost:4111/api/v1/accounts/transfer").then().assertThat().statusCode(HttpStatus.SC_OK).body("senderAccountId", Matchers.equalTo(accountId)).body("message", Matchers.equalTo("Transfer successful")).body("amount", Matchers.equalTo(100.0f)).body("receiverAccountId", Matchers.equalTo(accountId));
        // get all transactions and check existing
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/accounts/{accountId}/transactions", accountId).then().body("amount", hasItem(100f)).body("type", hasItem("DEPOSIT"));
    }

    @Test
    public void userCanNotTransferValidAmountOfMoneyToInvalidAccountTest() {
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
                    "balance": 1000
                }
                """.formatted(accountId)).post("http://localhost:4111/api/v1/accounts/deposit").then().assertThat().statusCode(HttpStatus.SC_OK).body("id", Matchers.equalTo(accountId)).body("balance", Matchers.equalTo(1000.0f));
        // transfer money
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                  "senderAccountId": %s,
                  "receiverAccountId": 123,
                  "amount": 199
                }
                """.formatted(accountId)).post("http://localhost:4111/api/v1/accounts/transfer").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
        // get all transactions and check not existing
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/accounts/{accountId}/transactions", accountId).then().body("amount", not(hasItem(199f)));
    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    public void userCanNotTransferInvalidAmountOfMoneyToValidAccountTest(double transferAmount) {
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
                    "balance": 1000
                }
                """.formatted(accountId)).post("http://localhost:4111/api/v1/accounts/deposit").then().assertThat().statusCode(HttpStatus.SC_OK).body("id", Matchers.equalTo(accountId)).body("balance", Matchers.equalTo(1000.0f));
        // transfer money
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                  "senderAccountId": %s,
                  "receiverAccountId": 3,
                  "amount": %s
                }
                """.formatted(accountId, transferAmount)).post("http://localhost:4111/api/v1/accounts/transfer").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
        // get all transactions and check not existing
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/accounts/{accountId}/transactions", accountId).then().body("amount", not(hasItem(transferAmount)));
    }
}
