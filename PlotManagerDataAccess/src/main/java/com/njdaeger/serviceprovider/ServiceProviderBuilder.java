package com.njdaeger.serviceprovider;

import com.njdaeger.pdk.utils.Pair;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceProviderBuilder {

    private final Map<Class<?>, Pair<Class<?>, Function<IServiceProvider, ?>>> transients = new HashMap<>();
    private final Map<Class<?>, Pair<Class<?>, Function<IServiceProvider, ?>>> singletons = new HashMap<>();

    /**
     * Creates a new service provider builder.
     * @return The service provider builder.
     */
    public static ServiceProviderBuilder builder() {
        return new ServiceProviderBuilder();
    }

    /**
     * Adds a singleton service to the service provider. This means that the service will only be instantiated once and will be shared across all classes that request it.
     * @param serviceInterfaceClass The interface of the service.
     * @param generatorFunction A function that will generate the service.
     * @param <S> The service interface
     * @param <I> The service implementation
     * @return The service provider builder.
     */
    public <S, I extends S> ServiceProviderBuilder addSingleton(Class<S> serviceInterfaceClass, Function<IServiceProvider, I> generatorFunction) {
        singletons.put(serviceInterfaceClass, Pair.of(null, generatorFunction));
        return this;
    }

    /**
     * Adds a singleton service to the service provider. This means that the service will only be instantiated once and will be shared across all classes that request it.
     * @param serviceInterfaceClass The interface of the service.
     * @param serviceImplementationClass The implementation of the service.
     * @param <S> The service interface
     * @param <I> The service implementation
     * @return The service provider builder.
     */
    public <S, I extends S> ServiceProviderBuilder addSingleton(Class<S> serviceInterfaceClass, Class<I> serviceImplementationClass) {
        singletons.put(serviceInterfaceClass, Pair.of(serviceImplementationClass, generationFunction(serviceImplementationClass)));
        return this;
    }

    /**
     * Adds a transient service to the service provider. This means that the service will be instantiated every time it is requested.
     * @param serviceInterfaceClass The interface of the service.
     * @param generatorFunction A function that will generate the service.
     * @param <S> The service interface
     * @return The service provider builder.
     */
    public <S> ServiceProviderBuilder addTransient(Class<S> serviceInterfaceClass, Function<IServiceProvider, S> generatorFunction) {
        transients.put(serviceInterfaceClass, Pair.of(null, generatorFunction));
        return this;
    }

    /**
     * Adds a transient service to the service provider. This means that the service will be instantiated every time it is requested.
     * @param serviceInterfaceClass The interface of the service.
     * @param serviceImplementationClass The implementation of the service.
     * @param <S> The service interface
     * @param <I> The service implementation
     * @return
     */
    public <S, I extends S> ServiceProviderBuilder addTransient(Class<S> serviceInterfaceClass, Class<I> serviceImplementationClass) {
        transients.put(serviceInterfaceClass, Pair.of(serviceImplementationClass, generationFunction(serviceImplementationClass)));
        return this;
    }

    /**
     * Builds the service provider.
     * @return The built service provider.
     */
    public IServiceProvider build(Plugin plugin) {
        return new ServiceProvider(plugin, orderSingletonsInLoadOrder(), orderTransientsInLoadOrder());
    }

    /**
     * Orders the transients in load order. This is done by checking the constructors of each scope and seeing if all the parameters can be loaded from the singletons or scopes that will have already been loaded.
     * @return  A map of scopes in load order.
     */
    private Map<Class<?>, Function<IServiceProvider, ?>> orderTransientsInLoadOrder() {
        var merged = new HashSet<>(singletons.keySet());
        var ordered = new HashMap<Class<?>, Function<IServiceProvider, ?>>();
        var toLoad = new Stack<Class<?>>();
        toLoad.addAll(transients.keySet());

        Class<?> recursiveIndicator = null;
        while (!toLoad.isEmpty()) {
            var intf = toLoad.pop();
            var pair = transients.get(intf);
            var impl = pair.getFirst();
            if (impl == null) {
                ordered.put(intf, pair.getSecond());
                continue;
            }
            var constructor = Arrays.stream(impl.getConstructors()).max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow(() -> new RuntimeException("Cannot instantiate " + impl.getSimpleName() + " because it has no public constructors."));
            var params = constructor.getParameters();

            if (params.length == 0 || canBeLoaded(params, merged)) {
                ordered.put(intf, pair.getSecond());
                merged.add(intf);
            } else {
                //if we were unable to be loaded with currently loaded classes, check if we can be loaded AT ALL. Checking the transient classes here can show if we are missing a scoped dependency.
                if (!canBeLoaded(params, transients.keySet())) throw new RuntimeException("Cannot instantiate " + impl.getSimpleName() + " because there are missing scoped dependencies. Unable to find " + Stream.of(params).map(Parameter::getType).filter(type -> !transients.containsKey(type)).map(Class::getSimpleName).reduce((s1, s2) -> s1 + ", " + s2).orElse("no dependencies") + ".");
                //if we have already tried to load this class and STILL failed, then we have a circular dependency.
                if (recursiveIndicator == intf) throw new RuntimeException("Cannot instantiate " + impl.getSimpleName() + " because it has a circular dependency.");
                toLoad.add(0, intf);
                recursiveIndicator = intf;
            }
        }
        return ordered;
    }

    /**
     * Orders the singletons in load order. This is done by checking the constructors of each singleton and seeing if all the parameters can be loaded from the singletons that will have already been loaded.
     * @return A map of singletons in load order.
     */
    private Stack<Pair<Class<?>, Function<IServiceProvider, ?>>> orderSingletonsInLoadOrder() {
        var ordered = new Stack<Pair<Class<?>, Function<IServiceProvider, ?>>>();
        var toLoad = new Stack<Class<?>>();
        toLoad.addAll(singletons.keySet());

        Class<?> recursiveIndicator = null;
        while (!toLoad.isEmpty()) {
            var intf = toLoad.pop();
            var pair = singletons.get(intf);
            var impl = pair.getFirst();
            if (impl == null) {
                ordered.push(Pair.of(intf, pair.getSecond()));
                continue;
            }

            var constructor = Arrays.stream(impl.getConstructors()).max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow(() -> new RuntimeException("Cannot instantiate " + impl.getSimpleName() + " because it has no public constructors."));
            var params = constructor.getParameters();
            if (canBeLoaded(params, ordered.stream().map(Pair::getFirst).collect(Collectors.toSet()))) {
                ordered.push(Pair.of(intf, pair.getSecond()));
            } else {
                if (!canBeLoaded(params, singletons.keySet())) throw new RuntimeException("Cannot instantiate " + impl.getSimpleName() + " because there are missing singleton dependencies. Unable to find " + Stream.of(params).map(Parameter::getType).filter(type -> !singletons.containsKey(type)).map(Class::getSimpleName).reduce((s1, s2) -> s1 + ", " + s2).orElse("no dependencies") + ".");
                if (recursiveIndicator == intf) throw new RuntimeException("Cannot instantiate " + intf.getSimpleName() + " because it has a circular dependency.");
                toLoad.add(0, intf);
                recursiveIndicator = intf;
            }
        }
        return ordered;
    }

    /**
     * Checks if all the parameters can be loaded from the given set of classes.
     * @param params The parameters to check.
     * @param loadedBefore The classes that will have already been loaded.
     * @return True if all the parameters can be loaded from the given set of classes.
     */
    private static boolean canBeLoaded(Parameter[] params, Set<Class<?>> loadedBefore) {
        var temp = new HashSet<>(loadedBefore);
        temp.add(IServiceProvider.class);//service provider can always be provided.
        temp.add(Plugin.class);//plugin can always be provided.
        return Stream.of(params).map(Parameter::getType).allMatch(temp::contains);
    }

    /**
     * Generates a function that will instantiate a class with a constructor that has parameters derived from the service provider.
     * @param serviceImplementation The class to instantiate.
     * @param <S> The service interface
     * @param <I> The service implementation
     * @return A function that will instantiate the class.
     */
    private static <S, I extends S> Function<IServiceProvider, S> generationFunction(Class<I> serviceImplementation) {
        var constructor = Arrays.stream(serviceImplementation.getConstructors()).max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow(() -> new RuntimeException("Cannot instantiate " + serviceImplementation.getSimpleName() + " because it has no public constructors."));
        return (s) -> {
            var params = constructor.getParameters();
            var args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                var param = params[i];
                var paramType = param.getType();
                args[i] = s.getRequiredService(paramType);
            }
            try {
                return (S) constructor.newInstance(args);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate " + serviceImplementation.getSimpleName() + " because of an exception.", e);
            }
        };
    }


}
