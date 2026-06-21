package iteration_2.api;

import iteration_1.api.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.stream.Stream;

import static specs.ResponseSpecs.PROFILE_UPDATED;


public class ChangeNameTest extends BaseTest {
    static Stream<Arguments> invalidNames() {
        return Stream.of(Arguments.of(InvalidChangeNameCase.THREE_WORDS), Arguments.of(InvalidChangeNameCase.DIGITS), Arguments.of(InvalidChangeNameCase.SPECIAL_CHARACTERS));
    }

    @Test
    public void userCanChangePersonalInfoWithValidDataTest() {
        CreateUserRequest user = AdminSteps.createUser();

        UpdateProfileRequest newName = UserSteps.generateValidName();
        UpdateProfileResponse updateProfileResponse = UserSteps.changeNameValid(user.getUsername(), user.getPassword(), newName);

        softly.assertThat(updateProfileResponse.getCustomer().getName()).isEqualTo(newName.getName());
        softly.assertThat(updateProfileResponse.getMessage()).isEqualTo(PROFILE_UPDATED);

        CreateUserResponse userProfile = UserSteps.getProfileInfo(user.getUsername(), user.getPassword());
        softly.assertThat(userProfile.getName()).isEqualTo(newName.getName());
    }


    // Получилось поменять имя на невалидные кейсы
    @MethodSource("invalidNames")
    @ParameterizedTest
    public void userCanNotChangePersonalInfoWithInvalidDataTest(InvalidChangeNameCase invalidName) {
        CreateUserRequest user = AdminSteps.createUser();

        UpdateProfileRequest newName = UserSteps.generateInvalidName(invalidName);
        UserSteps.changeNameInvalid(user.getUsername(), user.getPassword(), newName);

        CreateUserResponse userProfile = UserSteps.getProfileInfo(user.getUsername(), user.getPassword());
        softly.assertThat(userProfile.getName()).isEqualTo(null);
    }
}
