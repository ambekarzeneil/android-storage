package com.zva.android.commonLib.serialization.impl;

import com.zva.android.commonLib.serialization.SerializationService;
import com.zva.android.commonLib.serialization.exception.SerializationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Copyright CoreStorage 2015
 * Created by zeneilambekar on 31/07/15.
 */
public class JsonSerializationService implements SerializationService {

    private final ThreadLocal<ObjectMapper> mapperThreadLocal = ThreadLocal.withInitial(new Supplier<ObjectMapper>() {
        @Override
        public ObjectMapper get() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            return objectMapper;
        }
    });

    private JsonSerializationService() {

    }

    public <T> T inflate(byte[] serializedObject, Class<? extends T> objectClass) throws SerializationException {
        try {
            return mapperThreadLocal.get().readValue(serializedObject, objectClass);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    public <T> byte[] serialize(T inflatedObject) throws SerializationException {
        try {
            return mapperThreadLocal.get().writeValueAsBytes(inflatedObject);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    private static class SingletonHolder {
        private static final JsonSerializationService INSTANCE = new JsonSerializationService();
    }

    @Contract(pure = true)
    @NotNull
    public static SerializationService getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
