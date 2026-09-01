package api.requests.skelethon;

import api.models.BaseModel;
import api.models.CreateAccountResponse;
import api.models.CreateDepositRequest;
import api.models.CreateDepositResponse;
import api.models.CreateTransferRequest;
import api.models.CreateTransferResponse;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.FraudCheckResponse;
import api.models.GetTransactionsResponse;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import api.models.UpdateProfileRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users", CreateUserRequest.class, CreateUserResponse.class
    ),
    ACCOUNTS(
            "/accounts", BaseModel.class, CreateAccountResponse.class
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts", BaseModel.class, CreateAccountResponse.class
    ),
    CUSTOMER_PROFILE(
            "/customer/profile", CreateUserRequest.class, CreateUserResponse.class
    ),
    LOGIN(
            "/auth/login", LoginUserRequest.class, LoginUserResponse.class
    ),
    DEPOSIT(
            "/accounts/deposit", CreateDepositRequest.class, CreateDepositResponse.class
    ),
    TRANSACTIONS(
            "accounts/{accountId}/transactions", BaseModel.class, GetTransactionsResponse.class
    ),
    TRANSFER(
            "/accounts/transfer", CreateTransferRequest.class, CreateTransferResponse.class
    ),
    UPDATE_PROFILE(
            "/customer/profile", UpdateProfileRequest.class, CreateUserResponse.class
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


    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;

}
