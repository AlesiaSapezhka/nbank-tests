package requests.get_requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.*;

public class GetAccountsRequester extends GetRequest<Void> {

    public GetAccountsRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get(Void unused) {
        return given()
                .spec(requestSpecification)
                .when()
                .get("/api/v1/customer/accounts")
                .then()
                .spec(responseSpecification);
    }
}
