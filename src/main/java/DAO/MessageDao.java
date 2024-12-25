package DAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Model.Message;
import Util.ConnectionUtil;

/**
 * DAO implementation for managing CRUD operations on the Message table
 * in the SocialMedia database.
 */
public class MessageDao implements BaseDao<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDao.class);

    /**
     * Handles SQLExceptions by logging details and throwing a DaoException.
     *
     * @param e            The SQLException encountered.
     * @param sql          The SQL query or statement executed.
     * @param errorMessage The custom error message for the exception.
     */
    private void handleSQLException(SQLException e, String sql, String errorMessage) {
        LOGGER.error("Error executing SQL: {}\nMessage: {}\nState: {}\nCode: {}", sql, e.getMessage(), e.getSQLState(), e.getErrorCode());
        throw new DaoException(errorMessage, e);
    }

    @Override
    public Optional<Message> getById(int id) {
        String sql = "SELECT * FROM message WHERE message_id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Failed to retrieve message with ID: " + id);
        }
        return Optional.empty();
    }

    @Override
    public List<Message> getAll() {
        String sql = "SELECT * FROM message";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Failed to retrieve all messages.");
        }
        return messages;
    }

    public List<Message> getMessagesByAccountId(int accountId) {
        String sql = "SELECT * FROM message WHERE posted_by = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Failed to retrieve messages for account ID: " + accountId);
        }
        return new ArrayList<>();
    }

    @Override
    public Message insert(Message message) {
        String sql = "INSERT INTO message(posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new Message(id, message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Failed to insert message.");
        }
        throw new DaoException("Message insertion failed.");
    }

    @Override
    public boolean update(Message message) {
        String sql = "UPDATE message SET posted_by = ?, message_text = ?, time_posted_epoch = ? WHERE message_id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());
            ps.setInt(4, message.getMessage_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e, sql, "Failed to update message with ID: " + message.getMessage_id());
        }
        return false;
    }

    @Override
    public boolean delete(Message message) {
        String sql = "DELETE FROM message WHERE message_id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getMessage_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e, sql, "Failed to delete message with ID: " + message.getMessage_id());
        }
        return false;
    }

    /**
     * Maps a single ResultSet row to a Message object.
     *
     * @param rs The ResultSet to map.
     * @return A Message object representing the current row in the ResultSet.
     * @throws SQLException If an error occurs while reading the ResultSet.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("message_id"),
                rs.getInt("posted_by"),
                rs.getString("message_text"),
                rs.getLong("time_posted_epoch")
        );
    }

    /**
     * Maps a ResultSet to a list of Message objects.
     *
     * @param rs The ResultSet to map.
     * @return A list of Message objects.
     * @throws SQLException If an error occurs while reading the ResultSet.
     */
    private List<Message> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Message> messages = new ArrayList<>();
        while (rs.next()) {
            messages.add(mapResultSetToMessage(rs));
        }
        return messages;
    }
}
