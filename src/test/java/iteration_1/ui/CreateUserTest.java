package iteration_1.ui;

import com.codeborne.selenide.*;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;

import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.100.137:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();
        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();
        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);


        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        Alert alert = switchTo().alert();
        assertEquals(alert.getText(), "✅ User created successfully!");
        alert.accept();

        // Отображается на UI
        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\n USER")).shouldBe(Condition.visible);

        // Отображается на API
        List<CreateUserResponse> users = AdminSteps.getAllUsers();
        assertThat(users).anySatisfy(user -> ModelAssertions.assertThatModels(newUser, user).match());
    }

    @Test
    public void adminCanNotCreateUserWithIncorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();
        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();
        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);


        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("al");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Username must be between 3 and 15 characters");
        alert.accept();

        // НЕ Отображается на UI
        ElementsCollection allUsersFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUsersFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        // НЕ отображается на API
        List<CreateUserResponse> users = AdminSteps.getAllUsers();
        assertThat(users).noneSatisfy(user -> ModelAssertions.assertThatModels(newUser, user).match());
    }
}
