package com.zva.android.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zva.android.commonLib.serialization.exception.SerializationException;
import com.zva.android.commonLib.utils.ObjectWrapper;
import com.zva.android.data.core.QueryGroup;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
class SqlHelper extends SQLiteOpenHelper {

    private final ISqlHelperDelegate sqlHelperDelegate;

    private final DatabaseType       databaseType;

    private SqlHelper(Builder builder) {
        super(new CoreStorageContextWrapper(builder.applicationContext, builder.databasePath, builder.privateDatabase), builder.databaseName, null, builder.databaseVersion);
        sqlHelperDelegate = builder.sqlHelperDelegate;
        databaseType = builder.isPrivateDatabase() ? DatabaseType.PRIVATE : DatabaseType.SHARED;
    }

    @Contract(pure = true)
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        runCreateIfNotExistsQuery(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        runCreateIfNotExistsQuery(sqLiteDatabase);
    }

    private void runCreateIfNotExistsQuery(SQLiteDatabase sqLiteDatabase) {

        for (String tableName : sqlHelperDelegate.getTableNames(databaseType)) {

            StringBuilder stringBuilder = new StringBuilder();

            String keyTypeName = null;
            String primaryKeyName = sqlHelperDelegate.getPrimaryKeyFieldName(tableName);

            for (String queryColumnName : sqlHelperDelegate.getQueryColumnNames(tableName)) {
                if (queryColumnName.equals(primaryKeyName))
                    keyTypeName = convertToString(tableName, queryColumnName, sqlHelperDelegate.getQueryColumnType(tableName, queryColumnName));
                else
                    stringBuilder.append(queryColumnName).append(" ").append(
                            convertToString(tableName, queryColumnName, sqlHelperDelegate.getQueryColumnType(tableName, queryColumnName))).append(", ");
            }

            if (keyTypeName == null)
                throw new IllegalStateException(String.format("Primary Key Type for table %s is unknown", tableName));

            sqLiteDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s %s PRIMARY KEY, %s serialized_object BLOB);", tableName, primaryKeyName, keyTypeName,
                    stringBuilder.toString()));
        }

    }

    public <T, ID extends Serializable> T get(ID key, String tableName) throws SerializationException {

        final String keyFieldName = sqlHelperDelegate.getPrimaryKeyFieldName(tableName);
        final ObjectWrapper<T> objectWrapper = new ObjectWrapper<>();

        throw new IllegalStateException("Method incomplete");

    }

    public <T> Iterable<T> get(QueryGroup queryGroup, String tableName) throws SerializationException {
        throw new IllegalStateException("Method incomplete");
    }

    public long delete(QueryGroup queryGroup, String tableName) {
        throw new IllegalStateException("Method incomplete");
    }

    public <ID extends Serializable> boolean delete(ID key, String tableName) {
        throw new IllegalStateException("Method incomplete");
    }

    @Contract(pure = true)
    @NotNull
    private String convertToString(String tableName, String fieldName, QueryPropertyType queryPropertyType) {
        switch (queryPropertyType) {
            case STRING:
                return "VARCHAR(120)";

            case NUMBER:
                return "INTEGER";

            case BOOLEAN:
                return "TINYINT";

            case DATE:
                return "DATETIME";

            case CUSTOM:
                return sqlHelperDelegate.getSqlType(tableName, fieldName);
        }

        throw new IllegalStateException(String.format("Unknown QueryColumnType '%s'", queryPropertyType));

    }

    public static class Builder {
        private Map<String, String>                         tableNameToPrimaryKeyNameMap              = new HashMap<>();

        private Context                                     applicationContext;

        private String                                      databaseName;

        private int                                         databaseVersion;

        private String                                      databasePath;

        private boolean                                     privateDatabase;

        private Map<String, Map<String, QueryPropertyType>> tableNameToQueryColumnNameToColumnTypeMap = new HashMap<>();

        private ISqlHelperDelegate                          sqlHelperDelegate;

        public SqlHelper build() {
            return new SqlHelper(this);
        }

        public Builder addTable(String tableName, String primaryKeyName, Map<String, QueryPropertyType> queryColumnNameToQueryPropertyTypeMap) {
            tableNameToPrimaryKeyNameMap.put(tableName, primaryKeyName);
            tableNameToQueryColumnNameToColumnTypeMap.put(tableName, queryColumnNameToQueryPropertyTypeMap);
            return this;
        }

        public Context getApplicationContext() {
            return applicationContext;
        }

        public Builder setApplicationContext(Context applicationContext) {
            this.applicationContext = applicationContext;
            return this;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public Builder setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder copy() {

            Builder builder;

            try {
                Object clone = super.clone();
                if (clone instanceof Builder) {
                    builder = (Builder) clone;
                    builder.tableNameToPrimaryKeyNameMap = new HashMap<>();
                    builder.tableNameToQueryColumnNameToColumnTypeMap = new HashMap<>();
                } else {
                    throw new IllegalStateException("Cloned object is not a builder object");
                }
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException("Could not clone builder object");
            }

            return builder;
        }

        public int getDatabaseVersion() {
            return databaseVersion;
        }

        public Builder setDatabaseVersion(int databaseVersion) {
            this.databaseVersion = databaseVersion;
            return this;
        }

        public String getDatabasePath() {
            return databasePath;
        }

        public Builder setDatabasePath(String databasePath) {
            this.databasePath = databasePath;
            return this;
        }

        public boolean isPrivateDatabase() {
            return privateDatabase;
        }

        public Builder setPrivateDatabase(boolean privateDatabase) {
            this.privateDatabase = privateDatabase;
            return this;
        }

        public ISqlHelperDelegate getSqlHelperDelegate() {
            return sqlHelperDelegate;
        }

        public Builder setSqlHelperDelegate(ISqlHelperDelegate sqlHelperDelegate) {
            this.sqlHelperDelegate = sqlHelperDelegate;
            return this;
        }
    }

}
