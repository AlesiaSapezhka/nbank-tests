package iteration_1.api;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.InvalidUserPasswordCase;
import api.models.InvalidUsernameCase;
import api.models.comparison.ModelAssertions;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.steps.AdminSteps;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;

public class CreateUserTest extends BaseTest {

    static Stream<Arguments> invalidUserNames() {
        return Stream.of(Arguments.of(InvalidUsernameCase.BLANK), Arguments.of(InvalidUsernameCase.TOO_SHORT), Arguments.of(InvalidUsernameCase.TOO_LONG), Arguments.of(InvalidUsernameCase.INVALID_CHAR));
    }
    static Stream<Arguments> invalidUserPasswords() {
        return Stream.of(Arguments.of(InvalidUserPasswordCase.BLANK), Arguments.of(InvalidUserPasswordCase.TOO_SHORT), Arguments.of(InvalidUserPasswordCase.NO_DIGIT), Arguments.of(InvalidUserPasswordCase.NO_SPECIAL_CHAR), Arguments.of(InvalidUserPasswordCase.NO_UPPERCASE), Arguments.of(InvalidUserPasswordCase.NO_LOWERCASE));
    }

    @Test
    public void adminCanCreateUserWithValidDataTest() {
        CreateUserRequest userRequest = AdminSteps.buildUserValid();
        CreateUserResponse createUserResponse = AdminSteps.createUserValid(userRequest);

        softly.assertThat(createUserResponse.getUsername()).isEqualTo(userRequest.getUsername());
        ModelAssertions.assertThatModels(userRequest, createUserResponse).match();

        List<CreateUserResponse> users = AdminSteps.getAllUsers();
        softly.assertThat(users).anySatisfy(user -> ModelAssertions.assertThatModels(userRequest, user).match());

        UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
        DaoAndModelAssertions.assertThat(createUserResponse, userDao).match();
    }

    @MethodSource("invalidUserNames")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidUserNameTest(InvalidUsernameCase invalidCase) {
        CreateUserRequest createUserRequest = AdminSteps.buildUserInvalidName(invalidCase);
        AdminSteps.createUserInvalidName(createUserRequest, invalidCase);

        List<CreateUserResponse> users = AdminSteps.getAllUsers();
        softly.assertThat(users).noneSatisfy(user -> ModelAssertions.assertThatModels(createUserRequest, user).match());

        assertNull(DataBaseSteps.getUserByUsername(createUserRequest.getUsername()));
    }

    @MethodSource("invalidUserPasswords")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidUserPassword(InvalidUserPasswordCase invalidCase) {
        CreateUserRequest createUserRequest = AdminSteps.buildUserInvalidPassword(invalidCase);
        AdminSteps.createUserInvalidPassword(createUserRequest, invalidCase);

        List<CreateUserResponse> users = AdminSteps.getAllUsers();
        softly.assertThat(users).noneSatisfy(user -> ModelAssertions.assertThatModels(createUserRequest, user).match());

        assertNull(DataBaseSteps.getUserByUsername(createUserRequest.getUsername()));
    }
}