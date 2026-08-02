package api.requests.steps;

import api.models.*;
import api.generators.RandomModelGenerator;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.github.curiousoddman.rgxgen.RgxGen;
import common.storage.CreatedUsersStorage;
import io.restassured.response.ValidatableResponse;

import java.util.List;

public class AdminSteps {
    public static LoginUserRequest createAdmin() {
        return LoginUserRequest.builder().username("admin").password("admin").build();
    }

    public static LoginUserResponse login(String username, String password) {
        return new ValidatedCrudRequester<LoginUserResponse>(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(username).password(password).build());
    }

    public static CreateUserRequest buildUserValid() {
        return RandomModelGenerator.generate(CreateUserRequest.class);
    }

    public static CreateUserRequest buildUserInvalidName(InvalidUsernameCase invalidCase) {
        CreateUserRequest request = RandomModelGenerator.generate(CreateUserRequest.class);
        request.setUsername(new RgxGen(invalidCase.getRegex()).generate());
        return request;
    }
    public static CreateUserRequest buildUserInvalidPassword(InvalidUserPasswordCase invalidCase) {
        CreateUserRequest request = RandomModelGenerator.generate(CreateUserRequest.class);
        if (invalidCase == InvalidUserPasswordCase.BLANK) {
            request.setPassword("");
        } else {
            request.setPassword(new RgxGen(invalidCase.getRegex()).generate());
        }
        return request;
    }

    public static CreateUserResponse createUserValid(CreateUserRequest request) {
        CreateUserResponse response = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(request);
        CreatedUsersStorage.add((int) response.getId());
        return response;
    }

    public static ValidatableResponse createUserInvalidName(CreateUserRequest request, InvalidUsernameCase invalidCase) {
        return new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.requestReturnsBadRequest(invalidCase.getField(), invalidCase.getErrorMessage())).post(request);
    }

    public static ValidatableResponse createUserInvalidPassword(CreateUserRequest request, InvalidUserPasswordCase invalidCase) {
        return new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.requestReturnsBadRequest(invalidCase.getField(), invalidCase.getErrorMessage())).post(request);
    }


    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);
        CreateUserResponse response = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);
        CreatedUsersStorage.add((int) response.getId());
        return userRequest;
    }

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.requestReturnsOK()).getList();
    }

    public static ValidatableResponse deleteUser(int userId) {
        return new CrudRequester(RequestSpecs.adminSpec(), Endpoint.DELETE, ResponseSpecs.requestReturnsOK()).delete(userId);
    }
}