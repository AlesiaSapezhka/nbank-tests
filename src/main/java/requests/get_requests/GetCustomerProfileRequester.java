package requests.get_requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class GetCustomerProfileRequester extends GetRequest<Void>{
    public GetCustomerProfileRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get(Void unused) {
        return given()
                .spec(requestSpecification)
                .when()
                .get("/api/v1/customer/profile")
                .then()
                .spec(responseSpecification);
    }
}
