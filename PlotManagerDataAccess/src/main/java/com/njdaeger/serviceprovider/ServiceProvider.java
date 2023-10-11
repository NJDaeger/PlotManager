package com.njdaeger.serviceprovider;

import com.njdaeger.pdk.utils.Pair;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ServiceProvider implements IServiceProvider {

    private final Plugin plugin;
    private final Map<Class<?>, Object> singletons;
    private final Map<Class<?>, Function<IServiceProvider, ?>> generatorFunctions;

    public ServiceProvider(Plugin plugin, Stack<Pair<Class<?>, Function<IServiceProvider, ?>>> singletons, Map<Class<?>, Function<IServiceProvider, ?>> generatorFunctions) {
        this.singletons = new ConcurrentHashMap<>();
        this.generatorFunctions = new ConcurrentHashMap<>(generatorFunctions);
        this.plugin = plugin;
        loadSingletons(singletons);
    }

    private void loadSingletons(Stack<Pair<Class<?>, Function<IServiceProvider, ?>>> singletons) {
        var retryStack = new Stack<Pair<Class<?>, Function<IServiceProvider, ?>>>();
        singletons.forEach((pair) -> {
            var intf = pair.getFirst();
            var init = pair.getSecond();
            try {
                var obj = init.apply(this);
                this.singletons.put(intf, obj);
            } catch (Exception e) {
                retryStack.add(0, pair);
            }
        });
        if (retryStack.size() > 0) {
            if (retryStack.size() == singletons.size()) {
                throw new RuntimeException("Failed to initialize singletons because they all tried to grab a service that has not been loaded.");
            }
            loadSingletons(retryStack);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S getService(Class<S> serviceClass) {
        if (serviceClass == null) throw new IllegalArgumentException("Service class cannot be null.");
        if (serviceClass == IServiceProvider.class) return (S) this;
        if (serviceClass == Plugin.class) return (S) plugin;
        if (singletons.containsKey(serviceClass)) return (S) singletons.get(serviceClass);
        if (generatorFunctions.containsKey(serviceClass)) return (S) generatorFunctions.get(serviceClass).apply(this);
        return null;
    }

    @Override
    public <S> S getRequiredService(Class<S> serviceClass) {
        var service = getService(serviceClass);
        if (service == null) throw new RuntimeException("Unknown service requested. " + serviceClass.getSimpleName() + " is not registered.");
        return service;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> C initialize(Class<C> clazz) {
        if (clazz == null) throw new IllegalArgumentException("Class cannot be null.");
        var constructor = Arrays.stream(clazz.getConstructors()).max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow(() -> new RuntimeException("Cannot instantiate " + clazz.getSimpleName() + " because it has no public constructors."));
        var params = constructor.getParameters();
        var args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            var param = params[i];
            var paramType = param.getType();
            args[i] = getRequiredService(paramType);
        }
        try {
            return (C) constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName() + " because of an exception.", e);
        }
    }
}
