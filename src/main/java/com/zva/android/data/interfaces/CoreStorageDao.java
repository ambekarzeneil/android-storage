package com.zva.android.data.interfaces;

import java.io.Serializable;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
public interface CoreStorageDao<Type, KeyType extends Serializable> {

    <S extends Type> S save(S entity);

    <S extends Type> Iterable<S> save(Iterable<S> entities);

    Type findOne(KeyType key);

    boolean exists(KeyType key);

    Iterable<Type> findAll();

    Iterable<Type> findAll(Iterable<KeyType> keys);

    long count();

    void delete(KeyType key);

    void delete(Type entity);

    void delete(Iterable<? extends Type> entities);

    void deleteAll();

}
