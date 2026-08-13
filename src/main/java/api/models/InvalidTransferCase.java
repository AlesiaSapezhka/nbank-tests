package api.models;

import lombok.Getter;

@Getter
public enum InvalidTransferCase {

    NEGATIVE(
            -500,
            "Invalid transfer: insufficient funds or invalid accounts"
    ),
    NOT_EXISTING_ACCOUNT(
            -500,
            "Invalid transfer: insufficient funds or invalid accounts"
    ),

    EXCEED_LIMIT(
            10000.1,
            "Transfer amount cannot exceed 10000"
    ),

    MORE_THAN_BALANCE(
            2000,
            "Invalid transfer: insufficient funds or invalid accounts"
    );

    private final double transferAmount;
    private final String errorMessage;

    InvalidTransferCase(double transferAmount, String errorMessage) {
        this.transferAmount = transferAmount;
        this.errorMessage = errorMessage;
    }
}
