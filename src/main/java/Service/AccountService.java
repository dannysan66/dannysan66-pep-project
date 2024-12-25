package Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.AccountDao;
import DAO.DaoException;
import Model.Account;

public class AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
    private final AccountDao accountDao;

    // Constructor with default DAO instance
    public AccountService() {
        this(new AccountDao());
    }

    // Constructor with injected DAO instance (useful for testing)
    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    // Fetches an account by ID
    public Optional<Account> getAccountById(int id) {
        LOGGER.info("Fetching account with ID: {}", id);
        return execute(() -> accountDao.getById(id), "fetching account");
    }

    // Fetches all accounts
    public List<Account> getAllAccounts() {
        LOGGER.info("Fetching all accounts");
        return execute(accountDao::getAll, "fetching all accounts");
    }

    // Finds an account by username
    public Optional<Account> findAccountByUsername(String username) {
        LOGGER.info("Finding account by username: {}", username);
        return execute(() -> accountDao.findAccountByUsername(username), "finding account by username");
    }

    // Validates login credentials
    public Optional<Account> validateLogin(Account account) {
        LOGGER.info("Validating login for username: {}", account.getUsername());
        return execute(() -> accountDao.validateLogin(account.getUsername(), account.getPassword()), "validating login");
    }

    // Creates a new account
    public Account createAccount(Account account) {
        LOGGER.info("Creating account: {}", account);
        validateAccount(account);
        if (findAccountByUsername(account.getUsername()).isPresent()) {
            throw new ServiceException("Account with the same username already exists");
        }
        return execute(() -> accountDao.insert(account), "creating account");
    }

    // Updates an existing account
    public boolean updateAccount(Account account) {
        LOGGER.info("Updating account: {}", account);
        validateAccount(account);
        return execute(() -> accountDao.update(account), "updating account");
    }

    // Deletes an existing account
    public boolean deleteAccount(Account account) {
        LOGGER.info("Deleting account: {}", account);
        if (account.getAccount_id() <= 0) {
            throw new IllegalArgumentException("Account ID must be valid");
        }
        return execute(() -> accountDao.delete(account), "deleting account");
    }

    // Checks if an account exists by ID
    public boolean accountExists(int accountId) {
        LOGGER.info("Checking existence of account with ID: {}", accountId);
        return getAccountById(accountId).isPresent();
    }

    // Validates an account's business logic
    private void validateAccount(Account account) {
        LOGGER.info("Validating account: {}", account);

        String username = Optional.ofNullable(account.getUsername()).orElse("").trim();
        String password = Optional.ofNullable(account.getPassword()).orElse("").trim();

        if (username.isEmpty()) {
            throw new ServiceException("Username cannot be empty");
        }
        if (password.isEmpty()) {
            throw new ServiceException("Password cannot be empty");
        }
        if (password.length() < 4) {
            throw new ServiceException("Password must be at least 4 characters long");
        }
        if (accountDao.doesUsernameExist(username)) {
            throw new ServiceException("The username must be unique");
        }
    }

    // Utility method for executing DAO operations with error handling
    private <T> T execute(DaoOperation<T> operation, String operationDescription) {
        try {
            return operation.execute();
        } catch (DaoException e) {
            String errorMessage = "Error occurred during " + operationDescription;
            LOGGER.error(errorMessage, e);
            throw new ServiceException(errorMessage, e);
        }
    }

    @FunctionalInterface
    private interface DaoOperation<T> {
        T execute() throws DaoException;
    }
}
