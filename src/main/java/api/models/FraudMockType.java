package api.models;

/**
 * How WireMock should simulate the fraud-detection HTTP dependency.
 */
public enum FraudMockType {
    /** Return a successful JSON fraud-check body. */
    SUCCESS_RESPONSE,
    /** Return a non-200 HTTP status. */
    HTTP_ERROR,
    /** Delay the response long enough to trigger a client timeout. */
    TIMEOUT,
    /** Stop WireMock so the app gets a connection error. */
    CONNECTION_ERROR
}
