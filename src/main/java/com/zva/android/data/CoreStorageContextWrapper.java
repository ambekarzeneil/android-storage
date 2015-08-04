package com.zva.android.data;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.zva.android.commonLib.utils.CipherUtils;
import com.zva.android.commonLib.utils.DeviceUtils;
import com.zva.android.commonLib.utils.StringUtils;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
class CoreStorageContextWrapper extends ContextWrapper {

    private static final String TAG = "CoreStorageContextWrapper";

    private String              databasePath;

    private boolean             privateDatabase;

    public CoreStorageContextWrapper(Context applicationContext, String databasePath, boolean privateDatabase) {
        super(applicationContext);
        this.databasePath = databasePath;
        this.privateDatabase = privateDatabase;
    }

    @Override
    public File getDatabasePath(String name) {
        if (!privateDatabase) {
            Log.d(TAG, "External Storage not found. Cannot create shared database");
            throw new IllegalStateException("External Storage missing. Cannot create shared database");
        }

        String secureLocation = CipherUtils.hexStringDigest((DeviceUtils.getDeviceUniqueIdentifier(getApplicationContext()) + "CoreStorageContextWrapper").getBytes()).toLowerCase();

        secureLocation = StringUtils.distributeSubstringAcrossString("/.", "." + secureLocation, 23);

        File dbParentFolder = new File(StringUtils.isEmpty(databasePath) ? String.format("%s%s%s", privateDatabase ? getBaseContext().getFilesDir().getAbsolutePath()
                : Environment.getExternalStorageDirectory().getAbsolutePath(), File.separator, secureLocation) : databasePath);

        if (!dbParentFolder.getParentFile().exists())
            if (!dbParentFolder.mkdirs())
                throw new IllegalStateException("Could not create external storage directory");

        return new File(String.format("%s%s%s.db", dbParentFolder.getAbsolutePath(), File.separator, name));
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return openOrCreateDatabase(name, mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openDatabase(getDatabasePath(name).getPath(), factory, SQLiteDatabase.CREATE_IF_NECESSARY);
    }

}
