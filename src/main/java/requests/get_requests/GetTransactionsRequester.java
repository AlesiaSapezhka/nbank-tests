package requests.get_requests;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.*;

public class GetTransactionsRequester extends GetRequest<Integer> {
    public GetTransactionsRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get(Integer accountId) {
        return given()
                .spec(requestSpecification)
                .pathParam("accountId", accountId)
                .when()
                .get("/api/v1/accounts/{accountId}/transactions")
                .then()
                .spec(responseSpecification);
    }
}
