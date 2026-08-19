package api.database;

import api.configs.Config;
import api.dao.AccountDao;
import api.dao.TransactionsDao;
import api.dao.UserDao;
import api.models.TransactionsTypes;
import lombok.Builder;
import lombok.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DBRequest {
    private RequestType requestType;
    private String table;
    private List<Condition> conditions;
    private Class<?> extractAsClass;

    public enum RequestType {
        SELECT, INSERT, UPDATE, DELETE
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T mapRow(ResultSet resultSet) throws SQLException;
    }

    private static final Map<Class<?>, RowMapper<?>> ROW_MAPPERS = Map.of(
            UserDao.class, DBRequest::mapUserRow,
            AccountDao.class, DBRequest::mapAccountRow,
            TransactionsDao.class, DBRequest::mapTransactionRow
    );

    public <T> T extractAs(Class<T> clazz) {
        this.extractAsClass = clazz;
        return execute(clazz, false).stream().findFirst().orElse(null);
    }

    public <T> List<T> extractAsList(Class<T> clazz) {
        this.extractAsClass = clazz;
        return execute(clazz, true);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> execute(Class<T> clazz, boolean allRows) {
        RowMapper<T> mapper = (RowMapper<T>) ROW_MAPPERS.get(clazz);
        if (mapper == null) {
            throw new UnsupportedOperationException("Mapping for " + clazz.getSimpleName() + " not implemented");
        }

        String sql = buildSQL();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindConditions(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(mapper.mapRow(resultSet));
                    if (!allRows) {
                        break;
                    }
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed", e);
        }
    }

    private void bindConditions(PreparedStatement statement) throws SQLException {
        if (conditions == null) {
            return;
        }
        for (int i = 0; i < conditions.size(); i++) {
            statement.setObject(i + 1, conditions.get(i).getValue());
        }
    }

    private static UserDao mapUserRow(ResultSet resultSet) throws SQLException {
        return UserDao.builder()
                .id(resultSet.getLong("id"))
                .username(resultSet.getString("username"))
                .password(resultSet.getString("password"))
                .role(resultSet.getString("role"))
                .name(resultSet.getString("name"))
                .build();
    }

    private static AccountDao mapAccountRow(ResultSet resultSet) throws SQLException {
        return AccountDao.builder()
                .id(resultSet.getLong("id"))
                .accountNumber(resultSet.getString("account_number"))
                .balance(resultSet.getDouble("balance"))
                .customerId(resultSet.getLong("customer_id"))
                .build();
    }

    private static TransactionsDao mapTransactionRow(ResultSet resultSet) throws SQLException {
        long relatedAccountId = resultSet.getLong("related_account_id");
        return TransactionsDao.builder()
                .id((int) resultSet.getLong("id"))
                .amount(resultSet.getDouble("amount"))
                .type(TransactionsTypes.valueOf(resultSet.getString("type")))
                .relatedAccountId(resultSet.wasNull() ? 0 : (int) relatedAccountId)
                .build();
    }

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();

        switch (requestType) {
            case SELECT:
                sql.append("SELECT * FROM ").append(table);
                if (conditions != null && !conditions.isEmpty()) {
                    sql.append(" WHERE ");
                    for (int i = 0; i < conditions.size(); i++) {
                        if (i > 0) {
                            sql.append(" AND ");
                        }
                        sql.append(conditions.get(i).getColumn())
                                .append(" ")
                                .append(conditions.get(i).getOperator())
                                .append(" ?");
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException("Request type " + requestType + " not implemented");
        }

        return sql.toString();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Config.getProperty("db.url"),
                Config.getProperty("db.username"),
                Config.getProperty("db.password")
        );
    }

    public static DBRequestBuilder builder() {
        return new DBRequestBuilder();
    }

    public static class DBRequestBuilder {
        private RequestType requestType;
        private String table;
        private List<Condition> conditions = new ArrayList<>();
        private Class<?> extractAsClass;

        public DBRequestBuilder requestType(RequestType type) {
            this.requestType = type;
            return this;
        }

        public DBRequestBuilder where(Condition condition) {
            this.conditions.add(condition);
            return this;
        }

        public DBRequestBuilder table(String tableName) {
            this.table = tableName;
            return this;
        }

        public <T> T extractAs(Class<T> clazz) {
            this.extractAsClass = clazz;
            return buildRequest().extractAs(clazz);
        }

        public <T> List<T> extractAsList(Class<T> clazz) {
            this.extractAsClass = clazz;
            return buildRequest().extractAsList(clazz);
        }

        private DBRequest buildRequest() {
            return DBRequest.builder()
                    .requestType(requestType)
                    .table(table)
                    .conditions(conditions)
                    .extractAsClass(extractAsClass)
                    .build();
        }
    }
}
