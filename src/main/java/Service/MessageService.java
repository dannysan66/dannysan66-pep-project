package Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.MessageDao;
import DAO.DaoException;
import Model.Account;
import Model.Message;
import io.javalin.http.NotFoundResponse;

/**
 * MessageService contains the business logic for Message objects,
 * bridging the web layer (controller) and persistence layer (DAO).
 */
public class MessageService {

    private final MessageDao messageDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
    private static final String DB_ACCESS_ERROR_MSG = "Error accessing the database";

    // Default constructor initializes the DAO
    public MessageService() {
        this(new MessageDao());
    }

    // Constructor for dependency injection, useful for testing
    public MessageService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    /**
     * Retrieves a Message by its ID.
     * 
     * @param id The ID of the Message.
     * @return Optional containing the Message.
     * @throws ServiceException If the Message is not found or a DAO exception occurs.
     */
    public Optional<Message> getMessageById(int id) {
        LOGGER.info("Fetching message with ID: {}", id);
        try {
            return messageDao.getById(id)
                    .or(() -> {
                        LOGGER.warn("Message with ID {} not found", id);
                        throw new ServiceException("Message not found");
                    });
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Retrieves all messages.
     * 
     * @return List of Messages.
     * @throws ServiceException If a DAO exception occurs.
     */
    public List<Message> getAllMessages() {
        LOGGER.info("Fetching all messages");
        try {
            return messageDao.getAll();
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Retrieves messages posted by a specific account.
     * 
     * @param accountId The ID of the account.
     * @return List of Messages.
     * @throws ServiceException If a DAO exception occurs.
     */
    public List<Message> getMessagesByAccountId(int accountId) {
        LOGGER.info("Fetching messages posted by account ID: {}", accountId);
        try {
            return messageDao.getMessagesByAccountId(accountId);
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Creates a new Message.
     * 
     * @param message The Message to create.
     * @param account The Account creating the Message.
     * @return The created Message.
     * @throws ServiceException If validation or DAO exceptions occur.
     */
    public Message createMessage(Message message, Optional<Account> account) {
        LOGGER.info("Creating message: {}", message);

        if (account.isEmpty()) {
            throw new ServiceException("Account must exist when posting a new message");
        }

        validateMessage(message);
        checkAccountPermission(account.get(), message.getPosted_by());

        try {
            return messageDao.insert(message);
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Updates an existing Message.
     * 
     * @param message The Message to update.
     * @return The updated Message.
     * @throws ServiceException If validation or DAO exceptions occur.
     */
    public Message updateMessage(Message message) {
        LOGGER.info("Updating message with ID: {}", message.getMessage_id());

        Message existingMessage = getMessageById(message.getMessage_id())
                .orElseThrow(() -> new ServiceException("Message not found"));

        existingMessage.setMessage_text(message.getMessage_text());
        validateMessage(existingMessage);

        try {
            messageDao.update(existingMessage);
            return existingMessage;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Deletes a Message.
     * 
     * @param message The Message to delete.
     * @throws ServiceException If the Message is not found or a DAO exception occurs.
     */
    public void deleteMessage(Message message) {
        LOGGER.info("Deleting message with ID: {}", message.getMessage_id());
        try {
            if (!messageDao.delete(message)) {
                throw new NotFoundResponse("Message to delete not found");
            }
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Validates a Message.
     * 
     * @param message The Message to validate.
     * @throws ServiceException If the Message is invalid.
     */
    private void validateMessage(Message message) {
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new ServiceException("Message text cannot be null or empty");
        }
        if (message.getMessage_text().length() > 254) {
            throw new ServiceException("Message text cannot exceed 254 characters");
        }
    }

    /**
     * Checks if the account has permission to modify the Message.
     * 
     * @param account  The Account performing the action.
     * @param postedBy The ID of the account that posted the Message.
     * @throws ServiceException If the Account is not authorized.
     */
    private void checkAccountPermission(Account account, int postedBy) {
        if (account.getAccount_id() != postedBy) {
            throw new ServiceException("Account not authorized to modify this message");
        }
    }
}
