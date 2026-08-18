package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import lombok.Getter;
import ui.element.UserBadge;
import ui.utils.RetryUtils;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    public static String invalidUserName = "al";
    public static String adminPanelTitle = "Admin Panel";
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
        return StepLogger.log("GET all users from dashboard", () -> {
            ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");
            return generatePageElements(elementsCollection, UserBadge::new);
        });
    }

    public UserBadge findUserByUsername(String username) {
        return RetryUtils.retry("Find user by username " + username,
                () -> getAllUsers().stream().filter(it -> it.getUsername().equals(username)).findAny().orElse(null),
                result -> result != null,
                3,
                1000
        );
    }
}
