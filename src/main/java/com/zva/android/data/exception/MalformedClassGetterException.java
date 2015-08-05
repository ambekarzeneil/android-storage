package com.zva.android.data.exception;

import java.lang.reflect.Method;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 05/08/15.
 */
public class MalformedClassGetterException extends RuntimeException {

    private static final long   serialVersionUID = -31888911511940416L;

    private static final String FORMAT_MESSAGE   = "Class '%s' has either a non-public or malformed getter method: '%s'";

    public MalformedClassGetterException(Class<?> coreStorageObjectClass, Method getterMethod, Throwable cause) {
        super(String.format(FORMAT_MESSAGE, coreStorageObjectClass.getName(), getterMethod.getName()), cause);
    }

    public MalformedClassGetterException(String className, String tableName, Throwable cause) {
        super(String.format(FORMAT_MESSAGE, className, tableName), cause);
    }

}
