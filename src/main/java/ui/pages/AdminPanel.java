package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import ui.element.UserBadge;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Getter

public class AdminPanel extends BasePage<AdminPanel> {
    private final SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));
    private final SelenideElement addUserButton = $(Selectors.byText("Add User"));


    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password) {
        usernameInput.sendKeys(username);
        userPasswordInput.sendKeys(password);
        addUserButton.click();
        return this;
    }

    public List<UserBadge> getAllUsers() {
        ElementsCollection elementsCollection =  $(Selectors.byText("All Users")).parent().findAll("li");
        return generatePageElements(elementsCollection, UserBadge::new);
    }

}
