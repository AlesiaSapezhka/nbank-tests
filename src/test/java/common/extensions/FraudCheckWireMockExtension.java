package common.extensions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class FraudCheckWireMockExtension implements BeforeEachCallback, AfterEachCallback {

    private WireMockServer wireMockServer;

    @Override
    public void beforeEach(ExtensionContext context) {
        // Find the FraudCheckMock annotation on the test method or class
        FraudCheckMock mockConfig = context.getTestMethod()
                .map(method -> method.getAnnotation(FraudCheckMock.class))
                .orElseGet(() -> context.getTestClass()
                        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
                        .orElse(null));

        if (mockConfig != null) {
            setupWireMock(mockConfig);
        }
    }

    private void setupWireMock(FraudCheckMock config) {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(config.port()));
        wireMockServer.start();
        configureFor("localhost", config.port());

        // Create the response body based on annotation parameters
        String responseBody = String.format("{\n" +
                        "  \"status\": \"%s\",\n" +
                        "  \"decision\": \"%s\",\n" +
                        "  \"riskScore\": %.1f,\n" +
                        "  \"reason\": \"%s\",\n" +
                        "  \"requiresManualReview\": %s,\n" +
                        "  \"additionalVerificationRequired\": %s\n" +
                        "}",
                config.status(),
                config.decision(),
                config.riskScore(),
                config.reason(),
                config.requiresManualReview(),
                config.additionalVerificationRequired());

        // Mock the fraud detection service endpoint
        stubFor(post(urlPathMatching(config.endpoint()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        System.out.println("🔧 WireMock configured with fraud check response:");
        System.out.println("   Status: " + config.status());
        System.out.println("   Decision: " + config.decision());
        System.out.println("   Risk Score: " + config.riskScore());
        System.out.println("   Reason: " + config.reason());
        System.out.println("   Requires Manual Review: " + config.requiresManualReview());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    public String getBaseUrl() {
        return wireMockServer != null ? "http://localhost:" + wireMockServer.port() : null;
    }
}