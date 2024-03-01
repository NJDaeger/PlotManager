package com.njdaeger.taskmanager.servicelibrary.transactional;

import com.njdaeger.taskmanager.dataaccess.transactional.IUnitOfWork;

import java.util.UUID;

public interface IServiceTransaction extends AutoCloseable {

    <S extends ITransactionalService> S getService(Class<S> serviceClass);

    IUnitOfWork getUnitOfWork();

    void commit();

    void abort();

    UUID getTransactionId();

    void use();

    void release();


}
