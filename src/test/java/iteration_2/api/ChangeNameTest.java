package iteration_2.api;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.*;
import api.requests.steps.DataBaseSteps;
import iteration_1.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;


public class ChangeNameTest extends BaseTest {
    static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of(InvalidChangeNameCase.THREE_WORDS),
                Arguments.of(InvalidChangeNameCase.DIGITS),
                Arguments.of(InvalidChangeNameCase.SPECIAL_CHARACTERS));
    }

    @Test
    public void userCanChangePersonalInfoWithValidDataTest() {
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

        UpdateProfileRequest newName = UserSteps.generateValidName();
        CreateUserResponse updateProfileResponse =
                UserSteps.changeNameValid(user.getUsername(), user.getPassword(), newName);

        softly.assertThat(updateProfileResponse.getName()).isEqualTo(newName.getName());
        softly.assertThat(updateProfileResponse.getUsername()).isEqualTo(user.getUsername());
        softly.assertThat(updateProfileResponse.getRole()).isEqualTo(user.getRole());

        CreateUserResponse userProfile = userSteps.getProfileInfo();
        softly.assertThat(userProfile.getName()).isEqualTo(newName.getName());

        UserDao userDao = DataBaseSteps.getUserByUsername(user.getUsername());
        DaoAndModelAssertions.assertThat(userProfile, userDao).match();
    }

    @MethodSource("invalidNames")
    @ParameterizedTest
    public void userCanNotChangePersonalInfoWithInvalidDataTest(InvalidChangeNameCase invalidName) {
        CreateUserRequest user = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        UpdateProfileRequest newName = UserSteps.generateInvalidName(invalidName);
        UserSteps.changeNameInvalid(user.getUsername(), user.getPassword(), newName);

        CreateUserResponse userProfile = userSteps.getProfileInfo();
        softly.assertThat(userProfile.getName()).isEqualTo(null);

        assertNull(DataBaseSteps.getUserByUsername(userProfile.getName()));
    }
}
