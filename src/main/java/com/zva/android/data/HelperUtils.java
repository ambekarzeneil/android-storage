package com.zva.android.data;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.zva.android.data.annotations.Table;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
class HelperUtils {

    public static Set<Class<?>> getCoreStorageClasses(Configuration configuration) {
        throw new IllegalStateException("Method incomplete");
    }

    public static String getTableName(Class<?> classObject) {

        Table annotation = classObject.getAnnotation(Table.class);

        if (annotation == null || annotation.name().isEmpty())
            return StringUtils.replace(StringUtils.capitalize(classObject.getName()), ".", "_");

        return annotation.name();

    }

}
