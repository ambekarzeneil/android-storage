package com.zva.android.data;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.Context;

import com.zva.android.commonLib.utils.ClassLoaderUtils;
import com.zva.android.commonLib.utils.CollectionUtils;
import com.zva.android.commonLib.utils.StringUtils;
import com.zva.android.data.annotations.CoreStorageEntity;
import com.zva.android.data.annotations.Table;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
class HelperUtils {

    public static Set<Class<?>> getCoreStorageClasses(Configuration configuration, Context applicationContext) throws IOException {

        if (!CollectionUtils.isEmpty(configuration.getBasePackageNames()))
            return ClassLoaderUtils.find(configuration.getBasePackageNames().toArray(new String[configuration.getBasePackageNames().size()]), applicationContext,
                    CoreStorageEntity.class);

        if (!CollectionUtils.isEmpty(configuration.getBasePackageClasses())) {
            Set<String> basePackageNames = new LinkedHashSet<>();

            for (Class<?> basePackageClass : configuration.getBasePackageClasses())
                basePackageNames.add(StringUtils.getBasePackageName(basePackageClass));

            return ClassLoaderUtils.find(basePackageNames.toArray(new String[basePackageNames.size()]), applicationContext, CoreStorageEntity.class);

        }

        return ClassLoaderUtils.find(applicationContext, CoreStorageEntity.class);

    }

    public static String getTableName(Class<?> classObject) {

        Table annotation = classObject.getAnnotation(Table.class);

        if (annotation == null || annotation.name().isEmpty())
            return StringUtils.replace(classObject.getName().toUpperCase(), ".", "_");

        return annotation.name();

    }

    public static Class<?> getClassFromType(QueryPropertyTypeWrapper queryPropertyTypeWrapper) {
        switch (queryPropertyTypeWrapper.getQueryPropertyType()) {
            case DATE:
                return Date.class;

            case LONG:
                return Long.class;

            case INTEGER:
                return Integer.class;

            case BOOLEAN:
                return Boolean.class;

            case STRING:
                return String.class;

            case CUSTOM:
                return queryPropertyTypeWrapper.getCustomClass();
        }

        throw new IllegalArgumentException(String.format("Could not parse query property type: '%s'", queryPropertyTypeWrapper.getQueryPropertyType()));
    }
}
