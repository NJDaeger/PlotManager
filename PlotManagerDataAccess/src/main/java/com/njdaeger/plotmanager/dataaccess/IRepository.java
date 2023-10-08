package com.njdaeger.plotmanager.dataaccess;

import java.util.concurrent.CompletableFuture;

public interface IRepository {

    /**
     * Initialize the repository. This will create the tables if they do not exist.
     * @return True if the repository was initialized successfully.
     */
    CompletableFuture<Boolean> initializeRepository();

}
