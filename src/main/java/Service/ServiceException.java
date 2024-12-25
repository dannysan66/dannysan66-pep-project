package Service;

/**
 * Custom exception for the service layer.
 * <p>
 * ServiceException is used to handle and encapsulate exceptions occurring in the
 * service layer, providing meaningful messages and context for error handling.
 * </p>
 */
public class ServiceException extends RuntimeException {

    /**
     * Constructs a ServiceException with a custom error message.
     *
     * @param message the error message associated with this exception
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a ServiceException with the original cause of the error.
     *
     * @param cause the underlying exception that caused this ServiceException
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a ServiceException with both a custom error message and the
     * original cause of the error.
     *
     * @param message the error message associated with this exception
     * @param cause   the underlying exception that caused this ServiceException
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
