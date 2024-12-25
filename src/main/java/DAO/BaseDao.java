package DAO;

import java.util.List;
import java.util.Optional;

/**
 * A generic Data Access Object (DAO) interface that defines CRUD operations
 * for any model object. Implementing classes can use this interface to provide
 * database interaction logic for specific model types.
 *
 * @param <T> The type of the model object.
 */
public interface BaseDao<T> {

    /**
     * Retrieves an object by its ID.
     *
     * @param id The unique identifier of the object to retrieve.
     * @return An Optional containing the object if found, or an empty Optional if not.
     */
    Optional<T> getById(int id);

    /**
     * Retrieves all objects of the specified type from the database.
     *
     * @return A List containing all instances of the model object T.
     */
    List<T> getAll();

    /**
     * Inserts a new object into the database.
     *
     * @param t The object to insert.
     * @return The inserted object, potentially updated with database-generated values (e.g., ID).
     */
    T insert(T t);

    /**
     * Updates an existing object in the database.
     *
     * @param t The object to update.
     * @return true if the update was successful; false if the object does not exist.
     */
    boolean update(T t);

    /**
     * Deletes an object from the database.
     *
     * @param t The object to delete.
     * @return true if the deletion was successful; false if the object does not exist.
     */
    boolean delete(T t);
}
