package iteration_1;

import models.CreateUserRequest;
import models.CreateUserResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    public static Stream<Arguments> userValidData() {
        return Stream.of(Arguments.of("Alex.1", "Alex_16&#", "USER", 201), Arguments.of("Alex-1", "Alex_16&#", "USER", 201), Arguments.of("Alice_1", "Alex_16&#", "USER", 201));
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(Arguments.of(" ", "Alex_17&#", "USER", "username", "Username cannot be blank"), Arguments.of("ab", "Alex_17&#", "USER", "username", "Username must be between 3 and 15 characters"), Arguments.of("ab1_@6", "Alex_17&#", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"), Arguments.of("ab1_$56", "Alex_17&#", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"));
    }

    @MethodSource("userValidData")
    @ParameterizedTest
    public void adminCanCreateUserWithValidDataTest(String username, String password, String role, int statusCode) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder().username(username).password(password).role(role).build();

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());

        // get all users and check existing of user created above
         new CrudRequester
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.requestReturnsUsersList(username, role))
                .get(null);
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidDataTest(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder().username(username).password(password).role(role).build();
        new CrudRequester(RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);

        // get all users and check NOT existing of user created above
        new CrudRequester
                (RequestSpecs.adminSpec(),
                        Endpoint.ADMIN_USER,
                        ResponseSpecs.requestReturnsUsersListWithoutUser(createUserRequest.getUsername()))
                .get(null);
    }
}