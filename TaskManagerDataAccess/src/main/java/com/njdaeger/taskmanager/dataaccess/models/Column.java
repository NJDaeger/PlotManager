package com.njdaeger.taskmanager.dataaccess.models;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String dbName() default "";

}
