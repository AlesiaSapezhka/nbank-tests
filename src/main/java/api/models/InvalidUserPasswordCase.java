package api.models;

import lombok.Getter;

@Getter
public enum InvalidUserPasswordCase {
    BLANK(
            "\\s",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"
    ),

    TOO_SHORT(
            "[A-Za-z0-9$%&]{1,7}",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"
    ),

    NO_DIGIT(
            "[A-Za-z]{8,12}",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"
    ),
    NO_SPECIAL_CHAR(
            "[A-Za-z0-9]{8,12}",
            "password",
                    "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"
    ),

    NO_UPPERCASE(
            "[a-z0-9$%&]{8,12}",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"
    ),

    NO_LOWERCASE(
            "[A-Z0-9$%&]{8,12}",
            "password",
            "Password must contain at least one digit, one lower case, one upper case, one special character, no spaces, and be at least 8 characters long"
    );

    private final String regex;
    private final String field;

    private final String errorMessage;

    InvalidUserPasswordCase(String regex, String field, String errorMessage) {
        this.regex = regex;
        this.field = field;
        this.errorMessage = errorMessage;
    }
}