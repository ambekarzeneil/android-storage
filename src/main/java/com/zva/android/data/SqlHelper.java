package com.zva.android.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zva.android.commonLib.serialization.exception.SerializationException;
import com.zva.android.commonLib.utils.CollectionUtils;
import com.zva.android.commonLib.utils.ObjectWrapper;
import com.zva.android.commonLib.utils.core.Transformer;
import com.zva.android.data.core.QueryGroup;
import com.zva.android.data.exception.CoreStorageReadException;
import com.zva.android.data.exception.CoreStorageWriteException;

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

    public <T, ID extends Serializable> T get(final ID key, final String tableName) throws SerializationException, CoreStorageReadException {

        final String keyFieldName = sqlHelperDelegate.getPrimaryKeyFieldName(tableName);
        final ObjectWrapper<T> objectWrapper = new ObjectWrapper<>();

        try {
            runInTransaction(false, new TransactionTask() {
                @Override
                public void run() throws Exception {
                    Cursor cursor = targetDb.query(tableName, new String[] { "serialized_object" }, String.format("%s = ?", keyFieldName), new String[] { key.toString() }, null,
                            null, null);

                    if (cursor.moveToFirst())
                        objectWrapper.setContainedObject(sqlHelperDelegate.<T> inflateObject(tableName, cursor.getBlob(cursor.getColumnIndex("serialized_object"))));

                }
            });
        } catch (CoreStorageWriteException e) {
            throw new IllegalStateException("Write exception during get", e);
        }

        return objectWrapper.getContainedObject();

    }

    public <T> Set<T> get(QueryGroup queryGroup, String tableName) throws SerializationException {
        throw new IllegalStateException("Method incomplete");
    }

    public <T> Set<T> getAll(String tableName) {
        final Set<T> coreStorageObjects = new LinkedHashSet<>();
        final String keyFieldName = sqlHelperDelegate.getPrimaryKeyFieldName(tableName);

        try {
            Cursor cursor = getReadableDatabase().query(tableName, new String[] { keyFieldName, "serialized_object" }, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    coreStorageObjects.add(sqlHelperDelegate.<T> inflateObject(tableName, cursor.getBlob(cursor.getColumnIndex("serialized_object"))));
                    cursor.moveToNext();
                }
            }

            cursor.close();
        } catch (SerializationException ignored) {
        }

        return coreStorageObjects;
    }

    public long delete(QueryGroup queryGroup, String tableName) {
        throw new IllegalStateException("Method incomplete");
    }

    public <ID extends Serializable> boolean delete(final ID key, final String tableName) throws CoreStorageWriteException {

        final boolean[] count = { false };
        try {
            runInTransaction(true, new TransactionTask() {
                @Override
                public void run() throws Exception {
                    count[0] = targetDb.delete(tableName, String.format("%s = ?", sqlHelperDelegate.getPrimaryKeyFieldName(tableName)), new String[] { key.toString() }) > 0;
                }
            });
        } catch (CoreStorageReadException e) {
            throw new IllegalStateException("Read Exception while deleting", e);
        }

        return count[0];

    }

    public <ID extends Serializable> long delete(Set<ID> keys, final String tableName) throws CoreStorageWriteException {

        List<? extends String> keyStrings = CollectionUtils.map(keys, new Transformer<ID, String>() {
            @Override
            public String transform(ID input) {
                return input.toString();
            }
        });

        final String[] params = keyStrings.toArray(new String[keyStrings.size()]);
        final long result[] = new long[]{0};
        final String placeholders = makePlaceholders(params.length);
        final String primaryKeyFieldName = sqlHelperDelegate.getPrimaryKeyFieldName(tableName);

        try {
            runInTransaction(true, new TransactionTask() {
                @Override
                public void run() throws Exception {
                    result[0] = targetDb.delete(tableName, String.format("%s IN (%s)", primaryKeyFieldName,placeholders), params);
                }
            });
        } catch (CoreStorageReadException e) {
            throw new IllegalStateException("Read Exception during deleting", e);
        }

        return result[0];

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

    @Contract(pure = true)
    @NotNull
    private String convertToString(String tableName, String fieldName, QueryPropertyType queryPropertyType) {
        switch (queryPropertyType) {
            case STRING:
                return "VARCHAR(120)";

            case INTEGER:
                return "INTEGER";

            case LONG:
                return "BIGINT";

            case BOOLEAN:
                return "TINYINT";

            case DATE:
                return "DATETIME";

            case CUSTOM:
                return sqlHelperDelegate.getSqlType(tableName, fieldName);
        }

        throw new IllegalStateException(String.format("Unknown QueryColumnType '%s'", queryPropertyType));

    }

    public long getObjectCount(final String tableName) {
        final long count[] = new long[] { -1 };

        try {
            runInTransaction(false, new TransactionTask() {
                @Override
                public void run() throws SerializationException {
                    count[0] = targetDb.compileStatement("SELECT COUNT(*) FROM " + tableName).simpleQueryForLong();
                }
            });
        } catch (CoreStorageWriteException | CoreStorageReadException e) {
            throw new IllegalStateException(e);
        }

        return count[0];

    }

    /**
     * Makes a {@link TransactionTask} run inside a transaction.
     *
     * @param write Boolean specifying whether the transaction needs read (false) or read/write (true) access
     * @param task the to execute
     * @throws CoreStorageWriteException if the write to database failed because of any IO related access / transfer
     *             exception
     * @throws CoreStorageReadException if the read to database failed because of any IO related access / transfer
     *             exception
     */
    public void runInTransaction(boolean write, TransactionTask task) throws CoreStorageWriteException, CoreStorageReadException {
        SQLiteDatabase targetDatabase = write ? getWritableDatabase() : getReadableDatabase();
        task.setTargetDb(targetDatabase);

        boolean transactionCleared = false;

        while (!transactionCleared && !Thread.currentThread().isInterrupted()) {
            try {
                targetDatabase.beginTransaction();
            } catch (RuntimeException ignore) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    return;
                }
                continue;
            }

            try {
                task.run();
            } catch (Exception e) {
                if (write)
                    throw new CoreStorageWriteException(e);
                else
                    throw new CoreStorageReadException(e);
            } finally {
                targetDatabase.setTransactionSuccessful();
                targetDatabase.endTransaction();
            }

            transactionCleared = true;
        }
    }

    public <T> void save(T coreStorageObject, final String tableName) throws CoreStorageWriteException, SerializationException {

        List<String> queryColumnNames = new ArrayList<>(sqlHelperDelegate.getQueryColumnNames(tableName));

        final List<Object> valueObjects = new ArrayList<>();

        final String queryString = "INSERT OR REPLACE INTO %s (%s) VALUES (%s);";

        final StringBuilder propertyNames = new StringBuilder();
        final StringBuilder valueSpaces = new StringBuilder();

        for (String columnName : queryColumnNames) {
            propertyNames.append(columnName).append(",");

            valueObjects.add(sqlHelperDelegate.getQueryColumnValue(coreStorageObject, tableName, columnName));

            valueSpaces.append("?").append(",");
        }

        //Add Object Serialization
        propertyNames.append("serialized_object");
        valueObjects.add(sqlHelperDelegate.serializeObject(coreStorageObject));
        valueSpaces.append("?");

        try {
            runInTransaction(false, new TransactionTask() {
                @Override
                public void run() throws Exception {
                    targetDb.execSQL(String.format(queryString, tableName, propertyNames.toString(), valueSpaces.toString()), valueObjects.toArray(new Object[valueObjects.size()]));
                }
            });
        } catch (CoreStorageReadException e) {
            throw new IllegalStateException("Read Exception while writing object", e);
        }

    }

    public boolean truncate(final String tableName) throws CoreStorageWriteException {
        final boolean[] result = { false };
        try {
            runInTransaction(true, new TransactionTask() {
                @Override
                public void run() throws Exception {
                    result[0] = targetDb.delete(tableName, "1", new String[] {}) > 0;
                }
            });
        } catch (CoreStorageReadException e) {
            throw new IllegalStateException("Read Exception during deleting", e);
        }

        return result[0];

    }

    private String makePlaceholders(int size) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            stringBuilder.append("?,");
        }

        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    public static class Builder implements Cloneable {

        private Context            applicationContext;

        private String             databaseName;

        private int                databaseVersion;

        private String             databasePath;

        private boolean            privateDatabase;

        private ISqlHelperDelegate sqlHelperDelegate;

        public SqlHelper build() {
            return new SqlHelper(this);
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

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public Builder copy() {

            Builder builder;

            try {
                Object clone = super.clone();
                if (clone instanceof Builder) {
                    builder = (Builder) clone;
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
