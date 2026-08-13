package api.models;

import lombok.Getter;

import java.util.List;

@Getter
public enum InvalidUsernameCase {

    BLANK(
            "\\s",
            "username",
            List.of(
                    "Username must be between 3 and 15 characters",
                    "Username cannot be blank",
                    "Username must contain only letters, digits, dashes, underscores, and dots"
            )
    ),

    TOO_SHORT(
            "[A-Za-z0-9._-]{1,2}",
            "username",
            List.of("Username must be between 3 and 15 characters")
    ),

    TOO_LONG(
            "[A-Za-z0-9._-]{16,20}",
            "username",
            List.of( "Username must be between 3 and 15 characters")
    ),

    INVALID_CHAR(
            "[A-Za-z0-9]{3,5}\\$[A-Za-z0-9]{2,5}",
            "username",
            List.of("Username must contain only letters, digits, dashes, underscores, and dots")
    );

    private final String regex;
    private final String field;
    private final List<String> errorMessage;

    InvalidUsernameCase(String regex, String field, List<String> errorMessage) {
        this.regex = regex;
        this.field = field;
        this.errorMessage = errorMessage;
    }
}