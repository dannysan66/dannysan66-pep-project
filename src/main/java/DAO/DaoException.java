package DAO;

/**
 * DaoException is a custom unchecked exception for handling errors
 * specific to the DAO layer. By extending RuntimeException, it avoids the need
 * to declare or catch it explicitly, promoting cleaner code when working with
 * data access operations.
 */
public class DaoException extends RuntimeException {

    private static final long serialVersionUID = 1L; // Serialization version ID for class compatibility.

    /**
     * Constructs a new DaoException with a detailed error message.
     *
     * @param message A description of the error, retrievable via {@link #getMessage()}.
     */
    public DaoException(String message) {
        super(message);
    }

    /**
     * Constructs a new DaoException with a detailed error message and a cause.
     *
     * @param message A description of the error, retrievable via {@link #getMessage()}.
     * @param cause   The root cause of the exception, retrievable via {@link #getCause()}.
     *                A null value indicates that the cause is nonexistent or unknown.
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
