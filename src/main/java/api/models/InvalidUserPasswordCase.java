package api.models;

import lombok.Getter;

import java.util.List;

@Getter
public enum InvalidUserPasswordCase {
    BLANK(
            "\\s",
            "password",
            List.of(
                    Msg.VALIDATION,
                    "Password cannot be blank"
            )
    ),

    TOO_SHORT(
            "[A-Za-z0-9$%&]{1,7}",
            "password",
            List.of(Msg.VALIDATION)
    ),

    NO_DIGIT(
            "[A-Za-z]{8,12}",
            "password",
            List.of(Msg.VALIDATION)
    ),
    NO_SPECIAL_CHAR(
            "[A-Za-z0-9]{8,12}",
            "password",
            List.of(Msg.VALIDATION)
    ),

    NO_UPPERCASE(
            "[a-z0-9$%&]{8,12}",
            "password",
            List.of(Msg.VALIDATION)
    ),

    NO_LOWERCASE(
            "[A-Z0-9$%&]{8,12}",
            "password",
            List.of(Msg.VALIDATION)
    );

    private final String regex;
    private final String field;

    private final List<String> errorMessage;

    InvalidUserPasswordCase(String regex, String field, List<String> errorMessage) {
        this.regex = regex;
        this.field = field;
        this.errorMessage = errorMessage;
    }

    private static final class Msg {
        private static final String VALIDATION =
                "Password must contain at least one digit, one lower case, one upper case, "
                        + "one special character, no spaces, and be at least 8 characters long";

        private Msg() { }
    }
}
