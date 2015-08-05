package com.zva.android.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 04/08/15.
 */
abstract class TransactionTask {
    protected SQLiteDatabase targetDb;

    public SQLiteDatabase getTargetDb() {
        return targetDb;
    }

    public TransactionTask setTargetDb(SQLiteDatabase targetDb) {
        this.targetDb = targetDb;
        return this;
    }

    public abstract void run() throws Exception;
}
