package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import api.models.*;
@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER (
            "/admin/users", CreateUserRequest.class, CreateUserResponse.class
    ),
    ACCOUNTS (
            "/accounts", BaseModel.class, CreateAccountResponse.class
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts", BaseModel.class, CreateAccountResponse.class
    ),
    CUSTOMER_PROFILE(
            "/customer/profile", CreateUserRequest.class, CreateUserResponse.class
    ),
    LOGIN(
            "/auth/login", LoginUserRequest.class,LoginUserResponse.class
    ),
    DEPOSIT(
            "/accounts/deposit", CreateDepositRequest.class, CreateDepositResponse.class
    ),
    TRANSACTIONS(
            "accounts/{accountId}/transactions",BaseModel.class,GetTransactionsResponse.class
    ),
    TRANSFER(
            "/accounts/transfer", CreateTransferRequest.class, CreateTransferResponse.class
    ),
    UPDATE_PROFILE(
            "/customer/profile", UpdateProfileRequest.class, UpdateProfileResponse.class
    ),

    TRANSFER_WITH_FRAUD_CHECK(
            "/accounts/transfer-with-fraud-check", CreateTransferRequest.class, CreateTransferResponse.class
    ),

    DELETE(
            "/admin/users/{id}", CreateUserRequest.class, CreateUserResponse.class
    ),

    FRAUD_CHECK_STATUS(
            "/api/v1/accounts/fraud-check/{transactionId}", BaseModel.class, FraudCheckResponse.class
    );


    private final String Url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;

}
