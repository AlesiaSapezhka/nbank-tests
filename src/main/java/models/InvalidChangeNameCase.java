package models;

import lombok.Getter;

@Getter
public enum InvalidChangeNameCase {

    THREE_WORDS(
            "[A-Za-z]{5,10} [A-Za-z]{5,10} [A-Za-z]{5,10}",
            "Name should consist of two words separated by space"
    ),

    DIGITS(
            "[0-9]{3} [0-9]{2} [0-9]{4} [0-9]{4}",
            "Name should consist of two words separated by space"
    ),

    SPECIAL_CHARACTERS(
            "[!@#$%^&*]{5,10}",
            "Name should consist of two words separated by space"
    );

    private final String regex;
    private final String errorMessage;

    InvalidChangeNameCase(String regex, String errorMessage) {
        this.regex = regex;
        this.errorMessage = errorMessage;
    }
}