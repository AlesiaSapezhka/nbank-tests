package common.extensions;

import api.models.FraudTransferCase;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

/**
 * Starts a single WireMock instance and reuses it across tests.
 * Port 8080 must not be bound twice — use {@code @ResourceLock(FRAUD_WIREMOCK_LOCK)}
 * on classes that use this extension when parallel execution is enabled.
 */
public class FraudCheckWireMockExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    public static final String FRAUD_WIREMOCK_LOCK = "wiremock-port-8080";

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_ENDPOINT = "/fraud-check";
    /** Incomplete body: app has no short read-timeout, so delay alone hangs; null flags trigger fallback. */
    private static final int TIMEOUT_DELAY_MS = 100;
    private static final Object SERVER_LOCK = new Object();

    private static WireMockServer wireMockServer;

    @Override
    public void beforeAll(ExtensionContext context) {
        ensureServerStarted(DEFAULT_PORT);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        FraudCheckMock mockConfig = context.getTestMethod()
                .map(method -> method.getAnnotation(FraudCheckMock.class))
                .orElseGet(() -> context.getTestClass()
                        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
                        .orElse(null));

        if (mockConfig != null) {
            configureSuccessResponse(
                    mockConfig.status(),
                    mockConfig.decision(),
                    mockConfig.riskScore(),
                    mockConfig.reason(),
                    mockConfig.requiresManualReview(),
                    mockConfig.additionalVerificationRequired(),
                    mockConfig.port(),
                    mockConfig.endpoint()
            );
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        stopServer();
    }

    public void configure(FraudTransferCase fraudCase) {
        switch (fraudCase.getMockType()) {
            case SUCCESS_RESPONSE -> configureSuccessResponse(
                    fraudCase.getMockStatus(),
                    fraudCase.getMockDecision(),
                    fraudCase.getRiskScore(),
                    fraudCase.getReason(),
                    fraudCase.isRequiresManualReview(),
                    fraudCase.isAdditionalVerificationRequired(),
                    DEFAULT_PORT,
                    DEFAULT_ENDPOINT
            );
            case HTTP_ERROR -> configureHttpError(DEFAULT_PORT, DEFAULT_ENDPOINT, 500);
            case TIMEOUT -> configureTimeout(DEFAULT_PORT, DEFAULT_ENDPOINT, TIMEOUT_DELAY_MS);
            case CONNECTION_ERROR -> configureConnectionError();
            default -> throw new IllegalArgumentException("Unknown mock type: " + fraudCase.getMockType());
        }
    }

    public void configureSuccessResponse(
            String status,
            String decision,
            double riskScore,
            String reason,
            boolean requiresManualReview,
            boolean additionalVerificationRequired,
            int port,
            String endpoint
    ) {
        synchronized (SERVER_LOCK) {
            ensureServerStarted(port);
            wireMockServer.resetAll();
            configureFor("localhost", port);

            String responseBody = String.format("{\n"
                            + "  \"status\": \"%s\",\n"
                            + "  \"decision\": \"%s\",\n"
                            + "  \"riskScore\": %.1f,\n"
                            + "  \"reason\": \"%s\",\n"
                            + "  \"requiresManualReview\": %s,\n"
                            + "  \"additionalVerificationRequired\": %s\n"
                            + "}",
                    status,
                    decision,
                    riskScore,
                    reason,
                    requiresManualReview,
                    additionalVerificationRequired);

            stubFor(post(urlPathMatching(endpoint))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseBody)));

            logConfig("SUCCESS_RESPONSE", status, decision, riskScore, reason, requiresManualReview);
        }
    }

    private void configureHttpError(int port, String endpoint, int httpStatus) {
        synchronized (SERVER_LOCK) {
            ensureServerStarted(port);
            wireMockServer.resetAll();
            configureFor("localhost", port);

            stubFor(post(urlPathMatching(endpoint))
                    .willReturn(aResponse()
                            .withStatus(httpStatus)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\":\"fraud service error\"}")));

            System.out.println("🔧 WireMock configured with HTTP error: " + httpStatus);
        }
    }

    private void configureTimeout(int port, String endpoint, int delayMs) {
        synchronized (SERVER_LOCK) {
            ensureServerStarted(port);
            wireMockServer.resetAll();
            configureFor("localhost", port);

            stubFor(post(urlPathMatching(endpoint))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withFixedDelay(delayMs)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"status\":\"SUCCESS\",\"decision\":\"APPROVED\"}")));

            System.out.println("🔧 WireMock configured with timeout delay: " + delayMs + "ms");
        }
    }

    private void configureConnectionError() {
        synchronized (SERVER_LOCK) {
            stopServer();
            System.out.println("🔧 WireMock stopped to simulate connection error");
        }
    }

    public String getBaseUrl() {
        return wireMockServer != null ? "http://localhost:" + wireMockServer.port() : null;
    }

    private static void ensureServerStarted(int port) {
        synchronized (SERVER_LOCK) {
            if (wireMockServer == null || !wireMockServer.isRunning()) {
                wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port));
                wireMockServer.start();
            }
        }
    }

    private static void stopServer() {
        synchronized (SERVER_LOCK) {
            if (wireMockServer != null) {
                wireMockServer.stop();
                wireMockServer = null;
            }
        }
    }

    private static void logConfig(
            String type,
            String status,
            String decision,
            double riskScore,
            String reason,
            boolean requiresManualReview
    ) {
        System.out.println("🔧 WireMock configured with fraud check response:");
        System.out.println("   Type: " + type);
        System.out.println("   Status: " + status);
        System.out.println("   Decision: " + decision);
        System.out.println("   Risk Score: " + riskScore);
        System.out.println("   Reason: " + reason);
        System.out.println("   Requires Manual Review: " + requiresManualReview);
    }
}
