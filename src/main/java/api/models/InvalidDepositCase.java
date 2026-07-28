package api.models;

import lombok.Getter;

@Getter
public enum InvalidDepositCase {

    NEGATIVE(
            -34,
            "Invalid account or amount"
    ),

    EXCEED_LIMIT(
            5000.1,
            "Deposit amount exceeds the 5000 limit"
    ),

    ZERO(
            0,
            "Invalid account or amount"
    );

    private final double depositAmount;
    private final String errorMessage;

    InvalidDepositCase(double depositAmount, String errorMessage) {
        this.depositAmount = depositAmount;
        this.errorMessage = errorMessage;
    }
}
