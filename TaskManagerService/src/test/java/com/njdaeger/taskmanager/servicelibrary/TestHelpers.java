package com.njdaeger.taskmanager.servicelibrary;

import org.mockito.Mockito;

import java.util.function.Consumer;

public class TestHelpers {

    public static <T> T mock(Class<T> clazz, Consumer<T> handlers) {
        var mock = Mockito.mock(clazz);
        handlers.accept(mock);
        return mock;
    }

    public static <T> T mock(Class<T> clazz) {
        return Mockito.mock(clazz);
    }

    public static <T> T spy(T object) {
        return Mockito.spy(object);
    }



}
