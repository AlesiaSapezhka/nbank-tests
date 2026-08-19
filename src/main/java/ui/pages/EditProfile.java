package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;

public class EditProfile extends BasePage<EditProfile> {

    public SelenideElement newNameField = $(Selectors.byAttribute("placeholder", "Enter new name"));
    public SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
    public SelenideElement userName = $(".user-name");

    @Override
    public String url() {
        return "/dashboard";
    }

    public void changeName(String newName) {
        newNameField.shouldBe(Condition.visible);
        Selenide.Wait().until(driver -> {
            newNameField.setValue(newName);
            return newNameField.is(Condition.value(newName));
        });
        saveChangesButton.shouldBe(Condition.enabled).click();
    }

    public EditProfile checkNameTextContains(String newName, boolean contains) {
        if (contains) {
            userName.shouldHave(exactText(newName));
        } else {
            userName.shouldNotHave(exactText(newName));
        }
        return this;
    }
}
