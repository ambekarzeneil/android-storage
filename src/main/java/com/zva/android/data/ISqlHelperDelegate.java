package com.zva.android.data;

import java.util.Set;

import com.zva.android.commonLib.serialization.exception.SerializationException;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
interface ISqlHelperDelegate {

    String getSqlType(String tableName, String fieldName);

    Set<String> getTableNames(DatabaseType databaseType);

    String getPrimaryKeyFieldName(String tableName);

    Set<String> getQueryColumnNames(String tableName);

    QueryPropertyType getQueryColumnType(String tableName, String queryColumnName);

    <T> T inflateObject(String tableName, byte[] serializedObject) throws SerializationException;

    <T> Object getQueryColumnValue(T coreStorageObject, String tableName, String columnName);

    <T> byte[] serializeObject(T coreStorageObject) throws SerializationException;
}
