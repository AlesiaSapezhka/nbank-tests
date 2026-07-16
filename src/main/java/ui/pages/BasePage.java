package ui.pages;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement userPasswordInput = $(Selectors.byAttribute("placeholder", "Password"));
    protected SelenideElement homePage = $(Selectors.byText("\uD83C\uDFE0 Home"));
    private SelenideElement pageTitle(String title) {
        return $(Selectors.byText(title));
    }
    public abstract String url();

    public T open(){
       return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage (Class <T> pageClass){ return Selenide.page(pageClass);}

    public T checkAlertMessageAndAccept(String bankAlert){
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return (T) this;
    }

    public T goToHomePage() {
        homePage.click();
        return (T) this;
    }

    public void checkPageTitle(String title) {
        pageTitle(title).shouldBe(Condition.visible);
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public static void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }
}
