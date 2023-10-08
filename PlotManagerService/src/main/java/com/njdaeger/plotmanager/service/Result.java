package com.njdaeger.plotmanager.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class Result<T> {

    private final T result;
    private final boolean successful;
    private final String message;

    public Result(T result, boolean successful, String message) {
        this.result = result;
        this.successful = successful;
        this.message = message;
    }

    /**
     * Create a new good result.
     * @param result The result.
     * @param <T> The type of the result.
     * @return The result.
     */
    public static <T> Result<T> good(T result) {
        return new Result<>(result, true, "");
    }

    /**
     * Create a new bad result.
     * @param message The message of the result.
     * @param <T> The type of the result.
     * @return The result.
     */
    public static <T> Result<T> bad(String message) {
        return new Result<>(null, false, message);
    }

    /**
     * Get the result.
     * @return The result.
     */
    @Nullable
    public T get() {
        return result;
    }

    /**
     * Get the result or a default value.
     * @param other The default value.
     * @return The result or the default value.
     */
    public T getOr(T other) {
        return successful ? result : other;
    }

    public <R> R getOr(Function<T, R> map, R other) {
        return successful ? map.apply(result) : other;
    }

    /**
     * Check if this result was successful.
     * @return True if this result was successful, false otherwise.
     */
    public boolean successful() {
        return false;
    }

    /**
     * Get the message of this result.
     * @return The message of this result.
     */
    @Nonnull
    public String message() {
        return "";
    }

}
