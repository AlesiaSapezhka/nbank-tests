package iteration_2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.100.137:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of("enableVNC", true, "enableLog", true));
    }

    @Test
    public void userCanChangeNameTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет имя
        $(Selectors.byAttribute("class", "user-info")).click();
        UpdateProfileRequest newName = UserSteps.generateValidName();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName.getName());
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 5: проверка, что имя сменилось на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Name updated successfully!");
        alert.accept();
        Selenide.refresh();
        $(".user-name").shouldHave(exactText(newName.getName()));

        // ШАГ 6: юзер переходит на главную страницу и проверяет велком сообщение с новым именем

        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + newName.getName() + "!"));

        // ШАГ 7: проверка, что имя сменено на API
        CreateUserResponse userProfile = UserSteps.getProfileInfo(user.getUsername(), user.getPassword());
        assertThat(userProfile.getName()).isEqualTo(newName.getName());
    }

    @Test
    public void userCanNotChangeNameForInvalidTest() {

        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK()).post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build()).extract().header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет имя
        $(Selectors.byAttribute("class", "user-info")).click();
        UpdateProfileRequest newName = UserSteps.generateInvalidName(InvalidChangeNameCase.THREE_WORDS);

        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName.getName());
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // ШАГ 5: проверка, что имя НЕ сменилось на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("❌ Please enter a valid name.");
        alert.accept();
        Selenide.refresh();
        $(".user-name").shouldNotHave(exactText(newName.getName()));

        // ШАГ 6: юзер переходит на главную страницу и проверяет велком сообщение со старым именем
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 7: проверка, что имя НЕ сменено на API
        CreateUserResponse userProfile = UserSteps.getProfileInfo(user.getUsername(), user.getPassword());
        assertThat(userProfile.getName()).isEqualTo(null);
    }
}
