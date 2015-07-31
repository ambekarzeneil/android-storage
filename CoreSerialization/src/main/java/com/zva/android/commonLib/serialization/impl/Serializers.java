package com.zva.android.commonLib.serialization.impl;

import org.jetbrains.annotations.Contract;

import com.zva.android.commonLib.serialization.SerializationService;
import org.jetbrains.annotations.NotNull;

/**
 * Copyright CoreStorage 2015
 * Created by zeneilambekar on 31/07/15.
 */
public class Serializers {

    @Contract(pure = true)
    @NotNull
    public static SerializationService getJsonSerializer() {
        return JavaSerializationService.getInstance();
    }

    @Contract(pure = true)
    @NotNull
    public static SerializationService getJavaSerializer() {
        return JsonSerializationService.getInstance();
    }

    @Contract(pure = true)
    @NotNull
    public static SerializationService getProtostuffSerializer() {
        return ProtostuffSerializationService.getInstance();
    }

}
