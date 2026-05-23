package iteration_1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class CreateUserTest {
    @BeforeAll
    public static void setUpRestAssured() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));

    }

    public static Stream<Arguments> userValidData() {
        return Stream.of(Arguments.of("Alex.10", "Alex_16&#", "USER", 201), Arguments.of("Alex-10", "Alex_16&#", "USER", 201), Arguments.of("Alice_10", "Alex_16&#", "USER", 201));
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(Arguments.of(" ", "Alex_17&#", "USER", "username", "Username cannot be blank"), Arguments.of("ab", "Alex_17&#", "USER", "username", "Username must be between 3 and 15 characters"), Arguments.of("ab1_@6", "Alex_17&#", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"), Arguments.of("ab1_$56", "Alex_17&#", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"));
    }

    @MethodSource("userValidData")
    @ParameterizedTest
    public void adminCanCreateUserWithValidDataTest(String username, String password, String role, int statusCode) {
        String requestBody = String.format("""
                {
                "username": "%s",
                "password": "%s",
                "role": "%s"
                }
                """, username, password, role);
        given().contentType(ContentType.JSON).accept(ContentType.JSON).header("Authorization", "Basic YWRtaW46YWRtaW4=").body(requestBody).post("http://localhost:4111/api/v1/admin/users").then().assertThat().statusCode(statusCode).body("username", Matchers.equalTo(username)).body("password", Matchers.not(Matchers.equalTo(password))).body("role", Matchers.equalTo(role));
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidDataTest(String username, String password, String role, String errorKey, String errorValue) {
        String requestBody = String.format("""
                {
                "username": "%s",
                "password": "%s",
                "role": "%s"
                }
                """, username, password, role);
        given().contentType(ContentType.JSON).accept(ContentType.JSON).header("Authorization", "Basic YWRtaW46YWRtaW4=").body(requestBody).post("http://localhost:4111/api/v1/admin/users").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).body(errorKey, Matchers.equalTo(errorValue));
    }
}