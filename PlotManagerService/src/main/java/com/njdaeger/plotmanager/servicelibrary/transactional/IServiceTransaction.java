package com.njdaeger.plotmanager.servicelibrary.transactional;

import com.njdaeger.plotmanager.dataaccess.transactional.IUnitOfWork;

public interface IServiceTransaction extends AutoCloseable {

    <S extends ITransactionalService> S getService(Class<S> serviceClass);

    IUnitOfWork getUnitOfWork();

    void commit();

    void abort();

}
