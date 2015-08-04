package com.zva.android.data;

import java.util.Set;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
interface ISqlHelperDelegate {

    String getSqlType(String tableName, String fieldName);

    Set<String> getTableNames(DatabaseType databaseType);

    String getPrimaryKeyFieldName(String tableName);

    Set<String> getQueryColumnNames(String tableName);

    QueryPropertyType getQueryColumnType(String tableName, String queryColumnName);

}
