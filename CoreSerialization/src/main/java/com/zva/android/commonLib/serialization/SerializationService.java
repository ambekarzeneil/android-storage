package com.zva.android.commonLib.serialization;

import com.zva.android.commonLib.serialization.exception.SerializationException;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
public interface SerializationService {

    <T> T inflate(byte[] serializedObject, Class<? extends T> objectClass) throws SerializationException;
    <T> byte[] serialize(T inflatedObject) throws SerializationException;

}
