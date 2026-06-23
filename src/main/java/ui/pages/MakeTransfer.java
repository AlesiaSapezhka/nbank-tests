package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class MakeTransfer extends BasePage <MakeTransfer>{
    public SelenideElement selectAccountDropDown = $("select.account-selector");
    public SelenideElement selectAccountOptions = $("select.account-selector option");
    public SelenideElement selectAccount = $("select");
    public SelenideElement recipientName = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    public SelenideElement recipientAccountNumber =  $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    public SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
    public SelenideElement confirmCheckbox =  $("#confirmCheck");
    public SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public MakeTransfer selectAccount(String accountNumber) {
        selectAccountDropDown.click();
        selectAccountOptions.shouldBe(Condition.exist);
        selectAccount.selectOptionContainingText(accountNumber);
        return this;
    }

    public MakeTransfer enterRecipientName(String name) {
        recipientName.setValue(name);
        return this;
    }

    public MakeTransfer enterRecipientAccount(String accountNumber) {
        recipientAccountNumber.setValue(accountNumber);
        return this;
    }

    public MakeTransfer enterAmount(double deposit) {
        enterAmount.setValue(String.valueOf(deposit));
        return this;
    }

    public MakeTransfer setCheckbox(Boolean setCheckbox) {
        confirmCheckbox.setSelected(setCheckbox);
        return this;
    }

    public MakeTransfer sendTransfer() {
        sendTransferButton.click();
        return this;
    }

}
