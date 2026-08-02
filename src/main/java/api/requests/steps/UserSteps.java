package api.requests.steps;

import api.models.*;
import com.github.curiousoddman.rgxgen.RgxGen;
import api.generators.RandomModelGenerator;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

import static api.specs.ResponseSpecs.requestReturnsOK;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public CreateDepositResponse createDeposit(CreateDepositRequest createDepositRequest) {
        return new ValidatedCrudRequester<CreateDepositResponse>(RequestSpecs.authAsUserSpec(username, password), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsOK()).post(createDepositRequest);
    }

    public static void createDepositInvalidData(String username, String password, CreateDepositRequest createDepositRequest, InvalidDepositCase invalidDepositCase) {
        new CrudRequester(RequestSpecs.authAsUserSpec(username, password), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsBadRequestWithoutKey(invalidDepositCase.getErrorMessage())).post(createDepositRequest);
    }

    public static void createDepositInvalidAccount(String username, String password, CreateDepositRequest createDepositRequest) {
        new CrudRequester(RequestSpecs.authAsUserSpec(username, password), Endpoint.DEPOSIT, ResponseSpecs.requestReturnsForbiddenRequestWithoutKey(ResponseSpecs.UNAUTHORIZED_ACCESS)).post(createDepositRequest);
    }

    public static CreateTransferResponse createTransfer(String username, String password, CreateTransferRequest createTransferRequest) {
        return new ValidatedCrudRequester<CreateTransferResponse>(RequestSpecs.authAsUserSpec(username, password), Endpoint.TRANSFER, ResponseSpecs.requestReturnsOK()).post(createTransferRequest);
    }

    public static CreateTransferResponse createTransferWithFraudCheck(String username, String password, CreateTransferRequest createTransferRequest) {
        return new ValidatedCrudRequester<CreateTransferResponse>(RequestSpecs.authAsUserSpec(username, password), Endpoint.TRANSFER_WITH_FRAUD_CHECK, ResponseSpecs.requestReturnsOK()).post(createTransferRequest);
    }

    public static void createTransferWithInvalidCases(String username, String password, CreateTransferRequest createTransferRequest, InvalidTransferCase invalidTransferCase) {
        new CrudRequester(RequestSpecs.authAsUserSpec(username, password), Endpoint.TRANSFER, ResponseSpecs.requestReturnsBadRequestWithoutKey(invalidTransferCase.getErrorMessage())).post(createTransferRequest);
    }

    public static void createTransferWithInvalidAccount(String username, String password, CreateTransferRequest createTransferRequest) {
        new CrudRequester(RequestSpecs.authAsUserSpec(username, password), Endpoint.TRANSFER, ResponseSpecs.requestReturnsBadRequestWithoutKey(ResponseSpecs.TRANSFER_UNSUCCESSFUL)).post(createTransferRequest);
    }

    public CreateAccountResponse createAccount() {
        return new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(username, password), Endpoint.ACCOUNTS, ResponseSpecs.entityWasCreated()).post();
    }

    public static UpdateProfileRequest generateValidName() {
        return RandomModelGenerator.generate(UpdateProfileRequest.class);
    }

    public static UpdateProfileRequest generateInvalidName(InvalidChangeNameCase invalidCase) {
        UpdateProfileRequest request = RandomModelGenerator.generate(UpdateProfileRequest.class);
        request.setName(new RgxGen(invalidCase.getRegex()).generate());
        return request;
    }

    public static UpdateProfileResponse changeNameValid(String username, String password, BaseModel newName) {
        return new ValidatedCrudRequester<UpdateProfileResponse>(RequestSpecs.authAsUserSpec(username, password), Endpoint.UPDATE_PROFILE, requestReturnsOK()).update(newName);
    }

    public static void changeNameInvalid(String username, String password, BaseModel newName) {
        new CrudRequester(RequestSpecs.authAsUserSpec(username, password), Endpoint.UPDATE_PROFILE, ResponseSpecs.requestReturnsBadRequestWithoutMessage()).update(newName);
    }

    public List<CreateAccountResponse> getAllAccountsList() {
        return new ValidatedCrudRequester<CreateAccountResponse>(RequestSpecs.authAsUserSpec(username, password), Endpoint.CUSTOMER_ACCOUNTS, requestReturnsOK()).getList();
    }

    public List<GetTransactionsResponse> getAllTransactionsList(Integer accountId) {
        return new ValidatedCrudRequester<GetTransactionsResponse>(RequestSpecs.authAsUserSpec(username, password), Endpoint.TRANSACTIONS, ResponseSpecs.requestReturnsOK()).getList(accountId);
    }

    public CreateUserResponse getProfileInfo() {
        return new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.authAsUserSpec(username, password), Endpoint.CUSTOMER_PROFILE, ResponseSpecs.requestReturnsOK()).get();
    }
}
