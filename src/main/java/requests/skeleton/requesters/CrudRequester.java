package requests.skeleton.requesters;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpRequests;
import requests.skeleton.interfaces.CrudEndpointInterface;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequests implements CrudEndpointInterface {
    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        var body = model == null ? "" : model;
        return given().spec(requestSpecification).body(body).post(endpoint.getUrl()).then().spec(responseSpecification);
    }

    @Override
    public Object get(Integer id) {
        RequestSpecification request = given().spec(requestSpecification);

        if (id != null) {
            request.pathParam("accountId", id);
        }

        return request.when().get(endpoint.getUrl()).then().spec(responseSpecification);
    }

    @Override
    public Object update( BaseModel model) {

        var body = model == null ? "" : model;
        return given().spec(requestSpecification).body(body).put(endpoint.getUrl()).then().spec(responseSpecification);
    }

    @Override
    public Object delete(int id) {
        return null;
    }
}

