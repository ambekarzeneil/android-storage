package com.zva.android.commonLib.serialization.impl;

import com.zva.android.commonLib.serialization.SerializationService;
import com.zva.android.commonLib.serialization.exception.SerializationException;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.jetbrains.annotations.Contract;

import java.io.ByteArrayOutputStream;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
public class ProtostuffSerializationService implements SerializationService {

    //Makes one instance for each thread, avoiding reallocation thrashing
    private static final ThreadLocal<LinkedBuffer> linkedBufferHolder = new ThreadLocal<LinkedBuffer>() {
                                                                          @Override
                                                                          protected LinkedBuffer initialValue() {
                                                                              return LinkedBuffer.allocate();
                                                                          }
                                                                      };

    private Schema getSchema(Object serializableObject) {
        return RuntimeSchema.getSchema(serializableObject.getClass());
    }

    @Override
    public <T> T inflate(byte[] serializedObject, Class<? extends T> objectClass) throws SerializationException {

        T returnObject;

        try {
            returnObject = objectClass.newInstance();
            ProtostuffIOUtil.mergeFrom(serializedObject, returnObject, getSchema(returnObject));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SerializationException(e);
        }

        return returnObject;

    }

    @Override
    public <T> byte[] serialize(T inflatedObject) throws SerializationException {
        Schema schema = getSchema(inflatedObject);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        linkedBufferHolder.get().clear();
        try {
            ProtostuffIOUtil.writeTo(byteStream, inflatedObject, schema, linkedBufferHolder.get());
        } catch (Exception e) {
            throw new SerializationException(e);
        }

        return byteStream.toByteArray();

    }

    private ProtostuffSerializationService() {
        /* Required Private Constructor to prevent initiation outside getInstance()*/
    }

    @Contract(pure = true)
    public static ProtostuffSerializationService getInstance() {

        return SingletonHolder.singletonInstance;

    }

    private static class SingletonHolder {

        private static final ProtostuffSerializationService singletonInstance = new ProtostuffSerializationService();

    }

}
