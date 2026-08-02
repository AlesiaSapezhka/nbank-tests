package common.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CreatedUsersStorage {
    private static final ThreadLocal<List<Integer>> USER_IDS =
            ThreadLocal.withInitial(ArrayList::new);

    private CreatedUsersStorage() {
    }

    public static void add(int userId) {
        USER_IDS.get().add(userId);
    }

    public static List<Integer> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(USER_IDS.get()));
    }

    public static void clear() {
        USER_IDS.get().clear();
    }
}
