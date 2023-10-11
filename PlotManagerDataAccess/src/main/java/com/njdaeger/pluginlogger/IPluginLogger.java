package com.njdaeger.pluginlogger;

import java.io.Closeable;

public interface IPluginLogger extends Closeable {

    /**
     * Log an exception. Will be saved to the error file if enabled.
     * @param exception The exception to log.
     * @param additionalInfo Additional information to log with the exception.
     */
    void exception(Exception exception, String... additionalInfo);

    /**
     * Log an exception. Will be saved to the error file if enabled.
     * @param exception The exception to log.
     */
    void exception(Exception exception);

    /**
     * Log a warning message.
     * @param message The message to log.
     * @param additionalInfo Additional information to log with the message.
     */
    void warning(String message, String... additionalInfo);

    /**
     * Log a warning message.
     * @param message The message to log.
     */
    void warning(String message);

    /**
     * Log an info message.
     * @param message The message to log.
     * @param additionalInfo Additional information to log with the message.
     */
    void info(String message, String... additionalInfo);

    /**
     * Log an info message.
     * @param message The message to log.
     */
    void info(String message);

    /**
     * Log a debug message.
     * @param message The message to log.
     * @param additionalInfo Additional information to log with the message.
     */
    void debug(String message, String... additionalInfo);

    /**
     * Log a debug message.
     * @param message The message to log.
     */
    void debug(String message);

}
