package com.zva.android.commonLib.utils;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
public class ObjectWrapper<T> {

    private T containedObject;

    public ObjectWrapper() {
    }

    public ObjectWrapper(T containedObject) {
        this.containedObject = containedObject;
    }

    public T getContainedObject() {
        return containedObject;
    }

    public ObjectWrapper setContainedObject(T containedObject) {
        this.containedObject = containedObject;
        return this;
    }

    @Override
    public String toString() {
        return "ObjectWrapper{" + "containedObject=" + containedObject + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ObjectWrapper<?> that = (ObjectWrapper<?>) o;

        return !(containedObject != null ? !containedObject.equals(that.containedObject) : that.containedObject != null);

    }

    @Override
    public int hashCode() {
        return containedObject != null ? containedObject.hashCode() : 0;
    }
}
