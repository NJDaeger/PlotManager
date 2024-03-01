package com.njdaeger.serviceprovider;

public interface IServiceProvider {

    /**
     * Get a service from the service provider.
     * @param serviceClass The class of the service to get.
     * @param <S> The type of the service to get.
     * @return The service if it exists, null otherwise.
     */
    <S> S getService(Class<S> serviceClass);

    /**
     * Get a service from the service provider.
     * @param serviceClass The class of the service to get.
     * @param <S> The type of the service to get.
     * @return The service. Throws an exception if the service does not exist.
     */
    <S> S getRequiredService(Class<S> serviceClass);

    /**
     * Initialize a class with the service provider. (Does not add it to the service provider, just initializes it providing any dependencies it needs from the service provider.)
     * @param clazz The class to initialize.
     * @param <C> The type of the class to initialize.
     * @return The initialized class.
     */
    <C> C initialize(Class<C> clazz);

}
