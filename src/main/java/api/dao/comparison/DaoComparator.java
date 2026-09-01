package api.dao.comparison;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;

public class DaoComparator {

    private final DaoComparisonConfigLoader configLoader;

    public DaoComparator() {
        this.configLoader = new DaoComparisonConfigLoader("dao-comparison.properties");
    }

    public void compare(Object apiResponse, Object dao) {
        DaoComparisonConfigLoader.DaoComparisonRule rule = configLoader.getRuleFor(apiResponse.getClass());

        if (rule == null) {
            throw new RuntimeException("No comparison rule found for " + apiResponse.getClass().getSimpleName());
        }

        Map<String, String> fieldMappings = rule.getFieldMappings();

        for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
            String apiFieldName = mapping.getKey();
            String daoFieldName = mapping.getValue();

            Object apiValue = getFieldValue(apiResponse, apiFieldName);
            Object daoValue = getFieldValue(dao, daoFieldName);

            if (!valuesEqual(apiValue, daoValue)) {
                throw new AssertionError(String.format(
                        "Field mismatch for %s: API=%s, DAO=%s",
                        apiFieldName, apiValue, daoValue));
            }
        }
    }

    private boolean valuesEqual(Object apiValue, Object daoValue) {
        if (apiValue == null || daoValue == null) {
            return Objects.equals(apiValue, daoValue);
        }

        if (apiValue instanceof BigDecimal || daoValue instanceof BigDecimal) {
            return new BigDecimal(apiValue.toString())
                    .compareTo(new BigDecimal(daoValue.toString())) == 0;
        }

        if (apiValue instanceof Number && daoValue instanceof Number) {
            return new BigDecimal(apiValue.toString())
                    .compareTo(new BigDecimal(daoValue.toString())) == 0;
        }

        return Objects.equals(apiValue, daoValue);
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }

    private static class Objects {
        public static boolean equals(Object a, Object b) {
            return (a == b) || (a != null && a.equals(b));
        }
    }
}
