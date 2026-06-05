package iteration_2;

import generators.RandomData;
import iteration_1.BaseTest;
import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.put_requests.UpdateProfileRequester;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static specs.ResponseSpecs.profileWasUpdated;
import static specs.ResponseSpecs.requestReturnsBadRequestWithoutMessage;

public class ChangeNameTest extends BaseTest {
    public static Stream<Arguments> invalidNames() {
        return Stream.of(Arguments.of("Alice Ivanova Petrovna"), Arguments.of("4567 8790"), Arguments.of("&*^%$"));
    }

    @Test
    public void userCanChangePersonalInfoWithValidDataTest() {
        // create User
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);
        // login as User
        new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build()).header("Authorization", Matchers.notNullValue());

        // change name
        UpdateProfileRequest newName = UpdateProfileRequest.builder().name(RandomData.getUserName()).build();
        UpdateProfileResponse updateProfileResponse = new UpdateProfileRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), profileWasUpdated("message", "Profile updated successfully")).put(newName).extract().as(UpdateProfileResponse.class);
        softly.assertThat(updateProfileResponse.getCustomer().getName()).isEqualTo(newName.getName());

        //request profile info and check that name was updated
        new CrudRequester
                (RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                        Endpoint.CUSTOMER_PROFILE,
                        ResponseSpecs.requestReturnsUserProfile(newName.getName()))
                .get(null);
    }


    // Получилось поменять имя на невалидные кейсы
    @MethodSource("invalidNames")
    @ParameterizedTest
    public void userCanNotChangePersonalInfoWithInvalidDataTest(UpdateProfileRequest invalidName) {
        // create User
        CreateUserRequest userRequest = CreateUserRequest.builder().username(RandomData.getUserName()).password(RandomData.getUserPassword()).role(UserRole.USER.toString()).build();
        new CrudRequester(RequestSpecs.adminSpec(), Endpoint.ADMIN_USER, ResponseSpecs.entityWasCreated()).post(userRequest);

        // change name
        UpdateProfileResponse updateProfileResponse = new UpdateProfileRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), requestReturnsBadRequestWithoutMessage()).put(invalidName).extract().as(UpdateProfileResponse.class);
        softly.assertThat(updateProfileResponse.getCustomer().getName()).isNull();

        //request all users and check that name was not updated (returns initial null value)
//        new GetCustomerProfileRequester(RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()), ResponseSpecs.requestReturnsNull()).get(null);
    }
}
