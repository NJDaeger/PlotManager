package com.njdaeger.exceptionpublisher;

import java.io.Closeable;

public interface IExceptionPublisher extends Closeable {

    void publishException(Exception exception, String message, String... additionalInfo);

    void publishException(Exception exception, String message);

    void publishException(Exception exception);

    void publishMessage(String message, String... additionalInfo);

    void publishMessage(String message);

}
