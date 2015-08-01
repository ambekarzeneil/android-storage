package com.focus.example.corestoragetester.app.testObjects;

import com.zva.android.data.interfaces.CoreStorageDao;

import java.util.Date;
import java.util.List;

/**
 * Copyright CoreStorageTester 2015
 * Created by zeneilambekar on 01/08/15.
 */
public interface SimpleDao extends CoreStorageDao<SimpleTestObject, String> {

    List<SimpleTestObject> findByCreatedAtGreaterThan(Date compareTo);

}
