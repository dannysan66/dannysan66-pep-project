package DAO;

import Model.Account;
import Util.ConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AccountDao implements BaseDao<Account> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDao.class);

    // Utility method to log and rethrow SQLException
    private void logAndThrow(SQLException e, String sql, String errorMessage) {
        LOGGER.error("SQL Error in query: {}\nMessage: {}\nSQL State: {}\nError Code: {}",
                     sql, e.getMessage(), e.getSQLState(), e.getErrorCode());
        throw new DaoException(errorMessage, e);
    }

    // Extracts an Account from a ResultSet
    private Account extractAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("account_id"),
                rs.getString("username"),
                rs.getString("password")
        );
    }

    @Override
    public Optional<Account> getById(int id) {
        String sql = "SELECT * FROM account WHERE account_id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractAccount(rs));
                }
            }
        } catch (SQLException e) {
            logAndThrow(e, sql, "Error retrieving account with ID: " + id);
        }
        return Optional.empty();
    }

    @Override
    public List<Account> getAll() {
        String sql = "SELECT * FROM account";
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(extractAccount(rs));
            }
        } catch (SQLException e) {
            logAndThrow(e, sql, "Error retrieving all accounts.");
        }
        return accounts;
    }

    public Optional<Account> findAccountByUsername(String username) {
        String sql = "SELECT * FROM account WHERE username = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractAccount(rs));
                }
            }
        } catch (SQLException e) {
            logAndThrow(e, sql, "Error finding account with username: " + username);
        }
        return Optional.empty();
    }

    public Optional<Account> validateLogin(String username, String password) {
        return findAccountByUsername(username).filter(account -> Objects.equals(password, account.getPassword()));
    }

    public boolean doesUsernameExist(String username) {
        String sql = "SELECT 1 FROM account WHERE username = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logAndThrow(e, sql, "Error checking existence of username: " + username);
        }
        return false;
    }

    @Override
    public Account insert(Account account) {
        String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Account(generatedKeys.getInt(1), account.getUsername(), account.getPassword());
                }
            }
        } catch (SQLException e) {
            logAndThrow(e, sql, "Error inserting account.");
        }
        throw new DaoException("Failed to insert account; no ID generated.");
    }

    @Override
    public boolean update(Account account) {
        String sql = "UPDATE account SET username = ?, password = ? WHERE account_id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ps.setInt(3, account.getAccount_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logAndThrow(e, sql, "Error updating account with ID: " + account.getAccount_id());
        }
        return false;
    }

    @Override
    public boolean delete(Account account) {
        String sql = "DELETE FROM account WHERE account_id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, account.getAccount_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logAndThrow(e, sql, "Error deleting account with ID: " + account.getAccount_id());
        }
        return false;
    }
}
