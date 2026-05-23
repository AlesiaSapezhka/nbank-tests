package iteration_1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateAccountTest {
    @BeforeAll
    public static void setUpRestAssured() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));

    }

    @Test
    public void userCanCreateAccountTest() {
        // create user
        given().contentType(ContentType.JSON).accept(ContentType.JSON).header("Authorization", "Basic YWRtaW46YWRtaW4=").body("""
                {
                        "username": "Alex-18",
                        "password": "Alex_000#",
                        "role": "USER"
                        }
                """).post("http://localhost:4111/api/v1/admin/users").then().assertThat().statusCode(HttpStatus.SC_CREATED);
        // take User Token
        String userAuthHeader = given().contentType(ContentType.JSON).accept(ContentType.JSON).body("""
                {
                        "username":"Alex-18",
                        "password":"Alex_000#"
                                }
                """).post("http://localhost:4111/api/v1/auth/login").then().assertThat().statusCode(HttpStatus.SC_OK).extract().header("Authorization");
        // create account
        given().header("Authorization", userAuthHeader).contentType(ContentType.JSON).accept(ContentType.JSON).post("http://localhost:4111/api/v1/accounts").then().assertThat().statusCode(HttpStatus.SC_CREATED);
        //запросить все аккаунты и проверить что он там есть
        given().header("Authorization", userAuthHeader).get("http://localhost:4111/api/v1/customer/accounts").then().assertThat().body("[0].id", equalTo(8)).body("[0].accountNumber", equalTo("ACC8"));
    }

}
