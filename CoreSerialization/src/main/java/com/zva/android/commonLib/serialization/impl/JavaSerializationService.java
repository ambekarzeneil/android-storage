package com.zva.android.commonLib.serialization.impl;

import com.zva.android.commonLib.serialization.SerializationService;
import com.zva.android.commonLib.serialization.exception.SerializationException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
public class JavaSerializationService implements SerializationService {
    public <T> T inflate(byte[] serializedObject, Class<? extends T> objectClass) throws SerializationException {

        ByteArrayInputStream byteStream = new ByteArrayInputStream(serializedObject);
        try {
            Object readObject = new ObjectInputStream(byteStream).readObject();
            if(readObject.getClass().isAssignableFrom(objectClass))
                //noinspection unchecked
                return (T) readObject;

            throw new SerializationException(new Exception(String.format("Class '%s' is not assignable from '%s'", objectClass, readObject.getClass())));

        } catch (ClassNotFoundException | IOException e) {
            throw new SerializationException(e);
        }

    }

    public <T> byte[] serialize(T inflatedObject) throws SerializationException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            new ObjectOutputStream(stream).writeObject(inflatedObject);
        } catch (IOException e) {
            throw new SerializationException(e);
        }

        return stream.toByteArray();
    }

    private JavaSerializationService() {
        /* Required Private Constructor to prevent initiation outside getInstance()*/
    }

    @Contract(pure = true)
    @NotNull
    public static JavaSerializationService getInstance() {

        return SingletonHolder.singletonInstance;

    }

    private static class SingletonHolder {

        private static final JavaSerializationService singletonInstance = new JavaSerializationService();

    }

}
