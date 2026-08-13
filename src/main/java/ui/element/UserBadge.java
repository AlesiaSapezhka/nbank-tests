package ui.element;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class UserBadge extends BaseElements {
    private String username;
    private String role;

    public UserBadge(SelenideElement element) {
        super(element);
        username = element.getText().split("\n")[0];
        role = element.getText().split("\n")[1];
    }
}
