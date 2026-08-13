package api.requests.steps;

import api.dao.AccountDao;
import api.dao.TransactionsDao;
import api.dao.UserDao;
import api.database.Condition;
import api.database.DBRequest;
import api.configs.Config;
import common.helpers.StepLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public final class DataBaseSteps {

    private DataBaseSteps() { }

    public enum Table {
        CUSTOMERS("customers"),
        ACCOUNTS("accounts"),
        TRANSACTIONS("transactions");

        Table(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }
    }

    public enum TableFields {
        ID("id"),
        USERNAME("username"),
        ROLE("role"),
        ACCOUNT_NUMBER("account_number"),
        CUSTOMER_ID("customer_id"),
        BALANCE("balance"),
        ACCOUNT_ID("account_id");

        TableFields(String name) {
            this.name = name;
        }

        private String name;

        public String getField() {
            return name;
        }
    }

    public static UserDao getUserByUsername(String username) {
        return StepLogger.log("Get user from database by username: " + username, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.CUSTOMERS.getName())
                    .where(Condition.equalTo(TableFields.USERNAME.getField(), username))
                    .extractAs(UserDao.class);
        });
    }

    public static UserDao getUserById(Long id) {
        return StepLogger.log("Get user from database by ID: " + id, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.CUSTOMERS.getName())
                    .where(Condition.equalTo(TableFields.ID.getField(), id))
                    .extractAs(UserDao.class);
        });
    }

    public static UserDao getUserByRole(String role) {
        return StepLogger.log("Get user from database by role: " + role, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.CUSTOMERS.getName())
                    .where(Condition.equalTo(TableFields.ROLE.getField(), role))
                    .extractAs(UserDao.class);
        });
    }

    public static AccountDao getAccountByAccountNumber(String accountNumber) {
        return StepLogger.log("Get account from database by account number: " + accountNumber, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                     .table(Table.ACCOUNTS.getName())
                    .where(Condition.equalTo(TableFields.ACCOUNT_NUMBER.getField(), accountNumber))
                    .extractAs(AccountDao.class);
        });
    }

    public static AccountDao getAccountById(Long id) {
        return StepLogger.log("Get account from database by ID: " + id, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                     .table(Table.ACCOUNTS.getName())
                    .where(Condition.equalTo(TableFields.ID.getField(), id))
                    .extractAs(AccountDao.class);
        });
    }

    public static AccountDao getAccountByCustomerId(Long customerId) {
        return StepLogger.log("Get account from database by customer ID: " + customerId, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.CUSTOMERS.getName())
                    .where(Condition.equalTo(TableFields.CUSTOMER_ID.getField(), customerId))
                    .extractAs(AccountDao.class);
        });
    }

    public static AccountDao getAccountByBalance(double balance) {
        return StepLogger.log("Get account from database by Balance value: " + balance, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                     .table(Table.ACCOUNTS.getName())
                    .where(Condition.equalTo(TableFields.BALANCE.getField(), balance))
                    .extractAs(AccountDao.class);
        });
    }

    public static void updateAccountBalance(Long accountId, Double newBalance) {
        StepLogger.log("Update account balance in database for account ID: " + accountId + " to: " + newBalance, () -> {
            try (Connection connection = DriverManager.getConnection(
                    Config.getProperty("db.url"),
                    Config.getProperty("db.username"),
                    Config.getProperty("db.password"))) {

                String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setDouble(1, newBalance);
                    statement.setLong(2, accountId);
                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected == 0) {
                        throw new RuntimeException("No account found with ID: " + accountId);
                    }

                    return rowsAffected;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update account balance", e);
            }
        });
    }

    public static List<TransactionsDao> getTransactionsByAccountId(int accountId) {
        return StepLogger.log("Get transactions from database by account id: " + accountId, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(Table.TRANSACTIONS.getName())
                    .where(Condition.equalTo(TableFields.ACCOUNT_ID.getField(), accountId))
                    .extractAsList(TransactionsDao.class);
        });
    }
}
