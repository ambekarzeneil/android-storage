package com.zva.android.commonLib.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
    public static <InputType, OutputType> List<OutputType> map(Iterable<? extends InputType> inputList, Transformer<? super InputType, ? extends OutputType> transformer) {

        List<OutputType> resultList = new ArrayList<>();

        if (inputList == null || transformer == null)
            return null;

        for (InputType inputObject : inputList) {
            OutputType outputObject = transformer.transform(inputObject);
            if (outputObject != null)
                resultList.add(outputObject);
        }

        return resultList;

    }

    @Contract(pure = true, value = "null,_->null; _,null->null")
    public static <T> List<? extends T> filter(Iterable<? extends T> inputList, Filter<? super T> filter) {

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
    public static <T> List<? extends T> apply(Iterable<T> inputList, Transformer<T, T> transformer) {
        return map(inputList, transformer);
    }

    @Contract(pure = true, value = "null -> true")
    public static <T> boolean isEmpty(Iterable<? extends T> iterable) {

        if (iterable != null) {
            Iterator<? extends T> iterator = iterable.iterator();
            if (iterator != null)
                return iterator.hasNext();
        }

        return true;
    }

    @Contract(pure = true, value = "null, _->null; _, null->null")
    public static <T> T find(Iterable<? extends T> objects, Filter<? super T> filter) {
        if (objects == null || filter == null) {
            return null;
        }

        for (T object : objects) {
            if (filter.test(object))
                return object;
        }

        return null;
    }

    @SafeVarargs
    @Contract(pure = true, value = "null, _ -> false; _, null -> false")
    public static <T> boolean containsOnly(Iterable<T> iterable, T... containedElements) {

        if (iterable == null || containedElements == null)
            return false;

        final HashSet<T> remainingContainedElements = new HashSet<>(Arrays.asList(containedElements));

        for (T t : iterable) {
            if (remainingContainedElements.contains(t)) {
                remainingContainedElements.remove(t);
                continue;
            }
            return false;
        }

        return remainingContainedElements.size() == 0;
    }

}
