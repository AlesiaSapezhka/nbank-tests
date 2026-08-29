package api.requests.skelethon.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequests;
import api.requests.skelethon.interfaces.CrudEndpointInterface;

import java.util.List;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequests implements CrudEndpointInterface {
    private final CrudRequester crudRequester;

    public ValidatedCrudRequester(
            RequestSpecification requestSpecification, Endpoint endpoint,
            ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T post() {
        return (T) crudRequester.post().extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get() {
        return (T) crudRequester.get().extract().as(endpoint.getResponseModel());
    }

    @Override
    public T get(Integer id) {
        return (T) crudRequester.get(id).extract().as(endpoint.getResponseModel());
    }

    public List<T> getList() {
        return crudRequester.get().extract().jsonPath().getList("", (Class<T>) endpoint.getResponseModel());
    }

    public List<T> getList(Integer id) {
        return crudRequester.get(id).extract().jsonPath().getList("", (Class<T>) endpoint.getResponseModel());
    }

    @Override
    public T update(BaseModel model) {
        return (T) crudRequester.update(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public Object delete(int id) {
        return crudRequester.delete(id);
    }
}
