package com.njdaeger.plotmanager.service;

import com.njdaeger.plotmanager.service.models.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IUserService {

    CompletableFuture<Result<User>> getUserByUuid(UUID userId);

    CompletableFuture<Result<List<User>>> getUserByName(String username);

    CompletableFuture<Result<User>> createUser(UUID uuid, String username);

    CompletableFuture<Result<User>> updateUserName(UUID uuid, String username);

    CompletableFuture<Result<User>> deleteUser(UUID uuid);

}
