package com.zva.android.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QueryColumn {
    String name() default "";

    boolean indexed() default false;

    String sqlType() default "";
}
