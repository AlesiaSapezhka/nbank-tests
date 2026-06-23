package api.models;

import lombok.Getter;

@Getter
public enum InvalidUsernameCase {

    BLANK(
            "\\s",
            "username",
            "Username cannot be blank"
    ),

    TOO_SHORT(
            "[A-Za-z0-9._-]{1,2}",
            "username",
            "Username must be between 3 and 15 characters"
    ),

    TOO_LONG(
            "[A-Za-z0-9._-]{16,20}",
            "username",
            "Username must be between 3 and 15 characters"
    ),

    INVALID_CHAR(
            "[A-Za-z0-9]{3,5}\\$[A-Za-z0-9]{2,5}",
            "username",
            "Username must contain only letters, digits, dashes, underscores, and dots"
    );

    private final String regex;
    private final String field;
    private final String errorMessage;

    InvalidUsernameCase(String regex, String field, String errorMessage) {
        this.regex = regex;
        this.field = field;
        this.errorMessage = errorMessage;
    }
}