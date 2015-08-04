package com.zva.android.commonLib.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
public class DeviceUtils {
    @Contract(pure = true)
    @Nullable
    public static String getDeviceUniqueIdentifier(@NotNull Context applicationContext) {

        TelephonyManager telephonyManager = (TelephonyManager) applicationContext.getSystemService(Context.TELEPHONY_SERVICE);

        String[] candidateStrings = new String[] { Build.SERIAL, telephonyManager != null ? telephonyManager.getDeviceId() : null, Settings.Secure.ANDROID_ID };

        for (String targetString : candidateStrings)
            if (!StringUtils.isEmpty(targetString) && !targetString.equalsIgnoreCase("unknown") && !targetString.equalsIgnoreCase("android_id"))
                return targetString;

        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(CipherUtils.hexStringDigest(applicationContext.getPackageName().getBytes()).substring(0, 10),
                Context.MODE_PRIVATE);

        String devId = sharedPreferences.getString("devId", null);

        if (devId == null) {
            devId = StringUtils.getUuid(32);
            sharedPreferences.edit().putString("devId", devId).apply();
        }

        return devId;

    }
}
