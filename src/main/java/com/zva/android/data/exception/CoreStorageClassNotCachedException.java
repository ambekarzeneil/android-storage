package com.zva.android.data.exception;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 04/08/15.
 */
public class CoreStorageClassNotCachedException extends RuntimeException {
    private static final long serialVersionUID = -2243120496052651315L;

    public CoreStorageClassNotCachedException(Class<?> coreStorageClass) {
        super(String.format("Class '%s' not annotated with @CoreStorageEntity", coreStorageClass.getName()));
    }
}
