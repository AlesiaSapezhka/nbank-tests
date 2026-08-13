package common.extensions;

import api.requests.steps.AdminSteps;
import common.storage.CreatedUsersStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DeleteUsersExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        for (Integer userId : CreatedUsersStorage.getAll()) {
            try {
                AdminSteps.deleteUser(userId);
            } catch (AssertionError | RuntimeException e) {
                System.out.println("Failed to delete user id=" + userId + ": " + e.getMessage());
            }
        }
        CreatedUsersStorage.clear();
    }
}
