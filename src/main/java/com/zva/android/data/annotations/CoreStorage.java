package com.zva.android.data.annotations;

import com.zva.android.commonLib.serialization.SerializationService;
import com.zva.android.commonLib.serialization.impl.Serializers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyright CoreStorage 2015
 * Created by zeneilambekar on 31/07/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CoreStorage {
    SerializationService defaultSerializer = Serializers.getProtostuffSerializer();
}
