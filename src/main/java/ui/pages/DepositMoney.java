package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositMoney extends BasePage<DepositMoney> {
    public SelenideElement selectAccountDropDown = $("select.account-selector");
    public SelenideElement selectAccountOptions = $("select.account-selector option");
    public SelenideElement selectAccount = $("select");
    public SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
    public SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public DepositMoney selectAccount(String accountNumber) {
        selectAccountDropDown.click();
        selectAccountOptions.shouldBe(Condition.exist);
        selectAccount.selectOptionContainingText(accountNumber);
        return this;
    }

    public DepositMoney enterAmount(double deposit) {
        enterAmount.setValue(String.valueOf(deposit));
        return this;
    }

    public DepositMoney clickDeposit() {
        depositButton.click();
        return this;
    }
}
