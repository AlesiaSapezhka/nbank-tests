package api.specs;

import api.configs.Config;
import api.models.LoginUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.OUTPUT_DIRECTORY;

public final class RequestSpecs {
    public static final int INVALID_ACCOUNT_ID = 134;
    private static final Map<String, String> AUTH_HEADERS = new HashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter(),
                        new SwaggerCoverageRestAssured(
                                new FileSystemOutputWriter(Paths.get("target/" + OUTPUT_DIRECTORY))),
                        new AllureRestAssured()))
                .setBaseUri(Config.getProperty("apiBaseUrl"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .addHeader("Authorization", AUTH_HEADERS.get("admin"))
                .build();
    }

    public static RequestSpecification authAsUserSpec(String username, String password) {
        return defaultRequestBuilder()
                .addHeader("Authorization", getUserAuthHeader(username, password))
                .build();
    }

    public static String getUserAuthHeader(String username, String password) {
        String userAuthHeader;

        if (!AUTH_HEADERS.containsKey(username)) {
            userAuthHeader = new CrudRequester(
                    RequestSpecs.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpecs.requestReturnsOK())
                    .post(LoginUserRequest.builder().username(username).password(password).build())
                    .extract()
                    .header("Authorization");

            AUTH_HEADERS.put(username, userAuthHeader);
        } else {
            userAuthHeader = AUTH_HEADERS.get(username);
        }

        return userAuthHeader;
    }
}
