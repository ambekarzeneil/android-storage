package com.zva.android.commonLib.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;


/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
public class PackageUtils {

    /**
     * Gets application meta data.
     *
     * @param applicationContext the application context
     * @return the application meta data
     */
    @Contract(value = "null -> null", pure = true)
    public static Bundle getApplicationMetaData(@NotNull Context applicationContext) {

        Bundle metaData = null;

        try {

            metaData = applicationContext.getPackageManager().getApplicationInfo(applicationContext.getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return metaData;
    }

    /**
     * Checks if the specified permissions have been acquired by the current android app
     *
     * @param applicationContext the application context
     * @param requiredPermissionNames the required permission names
     */
    public static void checkPermissions(@NotNull Context applicationContext, @NotNull Permissions... requiredPermissionNames) {

        for (Permissions permissionName : requiredPermissionNames) {

            applicationContext.enforceCallingOrSelfPermission(permissionName.getName(), String.format("Missing permission in manifest: '%s'", permissionName));

        }

    }

    /**
     * Gets application meta data given a metaData key
     *
     * @param applicationContext the application context
     * @param metaDataKey the meta data key
     * @param defaultValue the default value
     * @return the application meta data
     */
    @Contract(value = "_, _, !null -> !null", pure = true)
    public static <T> T getApplicationMetaData(@NotNull Context applicationContext, @NotNull String metaDataKey, @Nullable T defaultValue) throws ClassCastException {

        Bundle metaData = getApplicationMetaData(applicationContext);

        if (metaData != null)
            //noinspection unchecked
            return (T) metaData.get(metaDataKey);

        return defaultValue;
    }

    /**
     * The enum containing a list of android permissions
     */
    public enum Permissions {

        ACCESS_NETWORK_STATE("android.permission.ACCESS_NETWORK_STATE"), ACCESS_COARSE_LOCATION("android.permission.ACCESS_COARSE_LOCATION"), ACCESS_FINE_LOCATION(
                "android.permission.ACCESS_FINE_LOCATION"), CHANGE_WIFI_STATE("android.permission.CHANGE_WIFI_STATE"), ACCESS_WIFI_STATE("android.permission.ACCESS_WIFI_STATE");

        private final String name;

        Permissions(String name) {

            this.name = name;

        }

        public String getName() {
            return name;
        }
    }
}
