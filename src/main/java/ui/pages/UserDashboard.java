package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage <UserDashboard> {
    public SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    public SelenideElement createNewAccount =$(Selectors.byText("➕ Create New Account"));
    public SelenideElement depositMoney =$(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    public SelenideElement transferMoney =$(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    public SelenideElement editProfile = $(Selectors.byAttribute("class", "user-info"));
    public static String defaultUserName = "noname";
    public static String editProfileTitle = "✏\uFE0F Edit Profile";
    public static String depositMoneyTitle = "\uD83D\uDCB0 Deposit Money";
    public static String makeTransferTitle = "\uD83D\uDD04 Make a Transfer";

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard depositMoney() {
        depositMoney.click();
        return this;
    }

    public UserDashboard transferMoney() {
        transferMoney.click();
        return this;
    }

    public UserDashboard moveToEditProfile() {
        editProfile.click();
        return this;
    }

    public UserDashboard checkWelcomeTextName(String name) {
        welcomeText.shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + name + "!"));
        return this;
    }
}
