package com.zva.android.commonLib.utils.core;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 04/08/15.
 */
public interface Transformer<InputType, OutputType> {
    OutputType transform(InputType input);
}
