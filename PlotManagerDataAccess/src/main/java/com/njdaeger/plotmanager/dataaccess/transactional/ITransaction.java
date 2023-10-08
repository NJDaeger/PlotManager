package com.njdaeger.plotmanager.dataaccess.transactional;

import com.njdaeger.plotmanager.dataaccess.Identifiable;

import java.util.List;
import java.util.Map;

public interface ITransaction<T> extends AutoCloseable {

    /**
     * Execute a query
     * @param query The query to execute
     * @param params The parameters to use in the query
     * @return The id of the inserted, updated, or deleted row, or -1 if the query did not affect any rows
     * @throws Exception If an error occurs while executing the query
     */
    int execute(String query, Map<String, Object> params) throws Exception;

    /**
     * Execute a query
     * @param query The query to execute
     * @return The id of the inserted, updated, or deleted row, or -1 if the query did not affect any rows
     * @throws Exception If an error occurs while executing the query
     */
    default int execute(String query) throws Exception {
        return execute(query, Map.of());
    }

    /**
     * Execute a query
     * @param query The query to execute
     * @param clazz The class to map the results to
     * @param <R> The type of the class to map the results to
     * @return A list of results
     * @throws Exception If an error occurs while executing the query
     */
    default <R extends Identifiable> List<R> query(String query, Class<R> clazz) throws Exception {
        return query(query, Map.of(), clazz);
    }

    /**
     * Execute a query
     * @param query The query to execute
     * @param params The parameters to use in the query
     * @param clazz The class to map the results to
     * @param <R> The type of the class to map the results to
     * @return A list of results
     * @throws Exception If an error occurs while executing the query
     */
    <R extends Identifiable> List<R> query(String query, Map<String, Object> params, Class<R> clazz) throws Exception;

    /**
     * Execute a query and return a single result
     * @param query The query to execute
     * @param clazz The class to map the result to
     * @param <R> The type of the class to map the result to
     * @return The result
     * @throws Exception If an error occurs while executing the query
     */
    default <R extends Identifiable> R queryScalar(String query, Class<R> clazz) throws Exception {
        return queryScalar(query, Map.of(), clazz);
    }

    /**
     * Execute a query and return a single result
     * @param query The query to execute
     * @param params The parameters to use in the query
     * @param clazz The class to map the result to
     * @param <R> The type of the class to map the result to
     * @return The result
     * @throws Exception If an error occurs while executing the query
     */
    default <R extends Identifiable> R queryScalar(String query, Map<String, Object> params, Class<R> clazz) throws Exception {
        var results = query(query, params, clazz);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get the transaction
     * @return The transaction
     */
    T getTransaction();

}
