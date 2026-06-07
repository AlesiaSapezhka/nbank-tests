package requests.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

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
            "/customer/profile", BaseModel.class, CreateAccountResponse.class
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
    );


    private final String Url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;

}
