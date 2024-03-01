package com.njdaeger.taskmanager.dataaccess.transactional;

import com.njdaeger.taskmanager.dataaccess.IRepository;

public interface IUnitOfWork extends AutoCloseable {

    AbstractDatabaseTransaction<?> getTransaction();

    <R extends IRepository> R repo(Class<R> repositoryClass);

    void commit();

    void abort();

}
