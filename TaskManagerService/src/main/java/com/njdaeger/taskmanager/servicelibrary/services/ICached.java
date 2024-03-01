package com.njdaeger.taskmanager.servicelibrary.services;

import java.util.concurrent.CompletableFuture;

public interface ICached {

    void clearCache();

    CompletableFuture<Void> initializeCache();

}
