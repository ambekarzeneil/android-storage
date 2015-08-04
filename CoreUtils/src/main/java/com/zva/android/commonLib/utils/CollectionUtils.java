package com.zva.android.commonLib.utils;

import java.util.ArrayList;
import java.util.List;

import com.zva.android.commonLib.utils.core.Filter;
import com.zva.android.commonLib.utils.core.Transformer;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 04/08/15.
 */
public class CollectionUtils {

    public static <T> List<T> filter(Iterable<T> inputList, Filter<T> filter) {

        List<T> resultList = new ArrayList<>();

        for (T object : inputList) {
            if (filter.test(object))
                resultList.add(object);
        }

        return resultList;

    }

    public static <InputType, OutputType> List<OutputType> map(Iterable<InputType> inputList, Transformer<InputType, OutputType> transformer) {

        List<OutputType> resultList = new ArrayList<>();

        for (InputType inputObject : inputList) {
            OutputType outputObject = transformer.transform(inputObject);
            if (outputObject != null)
                resultList.add(outputObject);
        }

        return resultList;

    }

    public static <T> List<T> apply(Iterable<T> inputList, Transformer<T, T> transformer) {
        return map(inputList, transformer);
    }

}
