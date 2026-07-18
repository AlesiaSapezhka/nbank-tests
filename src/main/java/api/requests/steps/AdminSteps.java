package api.requests.steps;

import api.models.*;
import com.github.curiousoddman.rgxgen.RgxGen;
import api.generators.RandomModelGenerator;
import io.restassured.response.ValidatableResponse;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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
        request.setPassword(new RgxGen(invalidCase.getRegex()).generate());
        return request;
    }

    public static CreateUserResponse createUserValid(CreateUserRequest request) {
        return new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(request);
    }

    public static ValidatableResponse createUserInvalidName(CreateUserRequest request, InvalidUsernameCase invalidCase) {
        return new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.requestReturnsBadRequest(invalidCase.getField(), invalidCase.getErrorMessage())).post(request);
    }

    public static ValidatableResponse createUserInvalidPassword(CreateUserRequest request, InvalidUserPasswordCase invalidCase) {
        return new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.requestReturnsBadRequest(invalidCase.getField(), invalidCase.getErrorMessage())).post(request);
    }


    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);
        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);
        return userRequest;
    }

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.requestReturnsOK()).getList();
    }
}