package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlerts {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    NAME_UPDATED("✅ Name updated successfully!"),
    ENTER_VALID_NAME("Name must contain two words with letters only"),
    DEPOSIT_SUCCESSFUL("✅ Successfully deposited $"),
    DEPOSIT_UNSUCCESSFUL("❌ Please deposit less or equal to 5000$."),
    DEPOSIT_WITHOUT_SELECTING_ACCOUNT("❌ Please select an account."),
    TRANSFER_SUCCESSFUL("✅ Successfully transferred $"),
    TRANSFER_MISSED_FIELDS("❌ Please fill all fields and confirm."),
    TRANSFER_INCREASED_BALANCE("Invalid transfer: insufficient funds or invalid accounts"),
    TRANSFER_TO_THE_SAME_ACCOUNT("❌ You cannot transfer money to the same account.");

    private final String message;

    BankAlerts(String message) {
        this.message = message;
    }
}
