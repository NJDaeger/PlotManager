package com.njdaeger.plotmanager.servicelibrary.services;

import java.util.concurrent.CompletableFuture;

public interface ICached {

    void clearCache();

    CompletableFuture<Void> initializeCache();

}
