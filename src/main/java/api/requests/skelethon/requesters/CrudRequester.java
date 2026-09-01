package api.requests.skelethon.requesters;

import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequests;
import api.requests.skelethon.interfaces.CrudEndpointInterface;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequests implements CrudEndpointInterface {
    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification)
                .body(body)
                .post(endpoint.getUrl())
                .then()
                .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse post() {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> given()
            .spec(requestSpecification)
            .post(endpoint.getUrl())
            .then()
            .spec(responseSpecification));
    }

    @Override
    public ValidatableResponse get() {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> given()
                .spec(requestSpecification)
                .when()
                .get(endpoint.getUrl())
                .then()
                .spec(responseSpecification)
        );
    }

    @Override
    public ValidatableResponse get(Integer accountId) {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> given()
                .spec(requestSpecification)
                .pathParam("accountId", accountId)
                .when()
                .get(endpoint.getUrl())
                .then()
                .spec(responseSpecification)
        );
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        return StepLogger.log("PUT request to " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .when()
                    .put(endpoint.getUrl())
                    .then()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse delete(int id) {
        return StepLogger.log("DELETE request to " + endpoint.getUrl(), () -> given()
                .spec(requestSpecification)
                .pathParam("id", id)
                .when()
                .delete(endpoint.getUrl())
                .then()
                .spec(responseSpecification)
        );
    }
}

