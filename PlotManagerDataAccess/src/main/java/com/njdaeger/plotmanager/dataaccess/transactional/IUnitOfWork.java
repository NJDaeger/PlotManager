package com.njdaeger.plotmanager.dataaccess.transactional;

import com.njdaeger.plotmanager.dataaccess.IRepository;

public interface IUnitOfWork extends AutoCloseable {

    AbstractDatabaseTransaction<?> getTransaction();

    <R extends IRepository> R repo(Class<R> repositoryClass);

    void commit();

    void abort();

}
