package com.zva.android.commonLib.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 09/08/15.
 */
public class IoUtils {

    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        return IOUtils.toByteArray(inputStream);
    }

}
