package requests.post_requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CreateTransferRequest;

import static io.restassured.RestAssured.given;

public class TransferRequester extends PostRequest<CreateTransferRequest> {

    public TransferRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateTransferRequest model) {
        return given().spec(requestSpecification).body(model).post("/api/v1/accounts/transfer").then().assertThat().spec(responseSpecification);
    }
}