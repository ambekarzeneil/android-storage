package com.zva.android.commonLib.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Contract;

import com.zva.android.commonLib.utils.core.Filter;
import com.zva.android.commonLib.utils.core.Transformer;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 04/08/15.
 */
public class CollectionUtils {


    @Contract(pure = true, value = "null,_-> null; _,null->null")
    public static <InputType, OutputType> List<OutputType> map(Iterable<InputType> inputList, Transformer<InputType, OutputType> transformer) {

        List<OutputType> resultList = new ArrayList<>();

        if(inputList == null || transformer == null)
            return null;

        for (InputType inputObject : inputList) {
            OutputType outputObject = transformer.transform(inputObject);
            if (outputObject != null)
                resultList.add(outputObject);
        }

        return resultList;

    }

    @Contract(pure = true, value = "null,_->null; _,null->null")
    public static <T> List<T> filter(Iterable<T> inputList, Filter<T> filter) {

        List<T> resultList = new ArrayList<>();

        if (inputList == null || filter == null)
            return null;

        for (T object : inputList) {
            if (filter.test(object))
                resultList.add(object);
        }

        return resultList;

    }

    @Contract(pure = true, value = "null,_->null; _,null->null")
    public static <T> List<T> apply(Iterable<T> inputList, Transformer<T, T> transformer) {
        return map(inputList, transformer);
    }

    @Contract(pure = true, value = "null -> true")
    public static <T> boolean isEmpty(Iterable<T> iterable) {

        if (iterable != null) {
            Iterator<T> iterator = iterable.iterator();
            if (iterator != null)
                return iterator.hasNext();
        }

        return true;
    }
}
