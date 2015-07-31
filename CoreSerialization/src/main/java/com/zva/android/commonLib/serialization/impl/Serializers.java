package com.zva.android.commonLib.serialization.impl;

import com.zva.android.commonLib.serialization.SerializationService;

/**
 * Copyright CoreStorage 2015
 * Created by zeneilambekar on 31/07/15.
 */
public class Serializers {

    public SerializationService getJsonSerializer() {
        return JavaSerializationService.getInstance();
    }

    public SerializationService getJavaSerializer() {
        return JsonSerializationService.getInstance();
    }

}
