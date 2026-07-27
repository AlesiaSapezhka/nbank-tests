package common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TimingExtension.class);
    private static final String START_TIME = "startTime";

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        String testName = testName(extensionContext);
        extensionContext.getStore(NAMESPACE).put(START_TIME, System.currentTimeMillis());
        System.out.println("Thread " + Thread.currentThread().getName() + ": Test started " + testName);
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        String testName = testName(extensionContext);
        Long startTime = extensionContext.getStore(NAMESPACE).remove(START_TIME, Long.class);
        long testDuration = System.currentTimeMillis() - startTime;
        System.out.println("Thread " + Thread.currentThread().getName() + ": Test finished " + testName
                + ", test duration " + testDuration + " ms");
    }

    private static String testName(ExtensionContext extensionContext) {
        return extensionContext.getRequiredTestClass().getName() + "." + extensionContext.getDisplayName();
    }
}
