package com.zva.android.commonLib.serialization.impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.zva.android.commonLib.serialization.SerializationService;

/**
 * Copyright CoreStorage 2015
 * Created by zeneilambekar on 31/07/15.
 */
public class Serializers {

    @Contract(pure = true)
    @NotNull
    public static SerializationService getJsonSerializer() {
        return JsonSerializationService.getInstance();
    }

    @Contract(pure = true)
    @NotNull
    public static SerializationService getJavaSerializer() {
        return JavaSerializationService.getInstance();
    }

    @Contract(pure = true)
    @NotNull
    public static SerializationService getProtostuffSerializer() {
        return ProtostuffSerializationService.getInstance();
    }

}
