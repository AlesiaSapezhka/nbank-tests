package iteration_1;

import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.post_requests.AdminCreateUserRequester;
import requests.get_requests.AdminGetUsersRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    public static Stream<Arguments> userValidData() {
        return Stream.of(Arguments.of("Alex.10", "Alex_16&#", "USER", 201), Arguments.of("Alex-10", "Alex_16&#", "USER", 201), Arguments.of("Alice_10", "Alex_16&#", "USER", 201));
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(Arguments.of(" ", "Alex_17&#", "USER", "username", "Username cannot be blank"), Arguments.of("ab", "Alex_17&#", "USER", "username", "Username must be between 3 and 15 characters"), Arguments.of("ab1_@6", "Alex_17&#", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"), Arguments.of("ab1_$56", "Alex_17&#", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"));
    }

    @MethodSource("userValidData")
    @ParameterizedTest
    public void adminCanCreateUserWithValidDataTest(String username, String password, String role, int statusCode) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated()).post(createUserRequest).extract().as(CreateUserResponse.class);

        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());

        // get all users and check existing of user created above
        new AdminGetUsersRequester(RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsUsersList(createUserRequest.getUsername(), createUserRequest.getRole())).get(null);

    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidDataTest(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder().username(username).password(password).role(role).build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue)).post(createUserRequest);

        // get all users and check NOT existing of user created above
        new AdminGetUsersRequester(RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsUsersListWithoutUser(createUserRequest.getUsername())).get(null);
    }
}