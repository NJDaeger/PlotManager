package com.njdaeger.plotmanager.dataaccess;

import com.njdaeger.plotmanager.dataaccess.transactional.ITransaction;

public interface IDatabase<T extends ITransaction<T2>, T2> {

    /**
     * Get the procedures for this database.
     * @return The procedures for this database.
     */
    IProcedure getProcedures();

    /**
     * Initialize a connection to the database.
     */
    T2 createConnection() throws Exception;

    /**
     * Create a database.
     */
    void createDatabases();

    /**
     * Get the type of database.
     * @return The type of database.
     */
    DatabaseType getType();

    /**
     * Create a transaction.
     * @return A new transaction.
     */
    T createTransaction() throws Exception;

    /**
     * Abort all changes to the database.
     */
    void rollbackTransaction(T transaction);

    /**
     * Commit a transaction.
     * @param transaction The transaction to commit.
     * @return True if the transaction was committed successfully.
     */
    boolean commitTransaction(T transaction);

}
