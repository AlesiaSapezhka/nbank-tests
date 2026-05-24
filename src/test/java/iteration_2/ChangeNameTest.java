package iteration_2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ChangeNameTest {
    @BeforeAll
    public static void setUpRestAssured() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    public static Stream<Arguments> invalidNames() {
        return Stream.of(Arguments.of("Alice Ivanova Petrovna"), Arguments.of("4567 8790"), Arguments.of("&*^%$"));
    }

    @Test
    public void userCanChangePersonalInfoWithValidDataTest() {
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "username":"mike-1998",
                        "password":"verysTRongPassword33$"
                                }
                """).post("http://localhost:4111/api/v1/auth/login").then().assertThat().statusCode(HttpStatus.SC_OK).extract().header("Authorization");
        // change name
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "name":"Ivan Nikolaev"
                }
                """).put("http://localhost:4111/api/v1/customer/profile").then().assertThat().statusCode(HttpStatus.SC_OK).body("message", equalTo("Profile updated successfully"));
        //request all users and check that name was updated
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/customer/profile").then().assertThat().body("name", equalTo("Ivan Nikolaev"));
    }

    @MethodSource("invalidNames")
    @ParameterizedTest
    public void userCanNotChangePersonalInfoWithInvalidDataTest(String invalidName) {
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "username":"mike-1998",
                        "password":"verysTRongPassword33$"
                                }
                """).post("http://localhost:4111/api/v1/auth/login").then().assertThat().statusCode(HttpStatus.SC_OK).extract().header("Authorization");
        // change name
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "name":%s
                }
                """.formatted(invalidName)).put("http://localhost:4111/api/v1/customer/profile").then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
        //request all users and check that there name was not updated
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/customer/profile").then().assertThat().body("name", not(equalTo(invalidName)));
    }
}
