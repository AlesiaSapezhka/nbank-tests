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

public class CreateDepositTest {
    @BeforeAll
    public static void setUpRestAssured() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));

    }

    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(Arguments.of(-500.0), Arguments.of(50000.0), Arguments.of(500.7));
    }

    @Test
    public void userCanCreateDepositWithValidDataTest() {
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "username":"Alex-18",
                        "password":"Alex_000#"
                                }
                """).post("http://localhost:4111/api/v1/auth/login").then().assertThat().statusCode(HttpStatus.SC_OK).extract().header("Authorization");
        // create account and take it id
        int accountId = given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).post("http://localhost:4111/api/v1/accounts").then().assertThat().statusCode(HttpStatus.SC_CREATED).extract().path("id");
        // add deposit
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                    "id": %s,
                    "balance": 100
                }
                """.formatted(accountId)).post("http://localhost:4111/api/v1/accounts/deposit").then().assertThat().statusCode(HttpStatus.SC_OK).body("id", Matchers.equalTo(accountId)).body("balance", Matchers.equalTo(100.0f));
    }

    // В условиях сказано что нельзя пополнить баланс более чем на 5000, негативное значение, а про дробное значение не сказано в условиях
    @MethodSource("depositInvalidData")
    @ParameterizedTest
    public void userCanNotCreateDepositWithInvalidDataTest(double deposit) {
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "username":"Alex-18",
                        "password":"Alex_000#"
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
    }

    @Test
    public void userCanNotCreateDepositForNotExistingAccountTest() {
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "username":"Alex-18",
                        "password":"Alex_000#"
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