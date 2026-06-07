package requests.get_requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public abstract class GetRequest<T> {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public GetRequest(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    public abstract ValidatableResponse get(T request);
    public ValidatableResponse get(){
        return get(null);
    };
}
