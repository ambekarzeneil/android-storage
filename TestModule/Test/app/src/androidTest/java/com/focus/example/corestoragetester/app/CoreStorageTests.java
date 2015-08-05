package com.focus.example.corestoragetester.app;

import android.app.Application;
import android.test.ApplicationTestCase;
import com.focus.example.corestoragetester.app.testObjects.SimpleTestObject;
import com.zva.android.data.CoreStorageHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright CoreStorageTester 2015 Created by zeneilambekar on 05/08/15.
 */
public class CoreStorageTests extends ApplicationTestCase<Application> {

    private static final String TAG = "CoreStorageTests";
    private CoreStorageHelper helper;

    public CoreStorageTests() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        CoreStorageHelper.init(getApplication().getApplicationContext());
        helper = CoreStorageHelper.getInstance();
        helper.removeAll(SimpleTestObject.class);
    }

    public void testComplexRetrieve() {
        SimpleTestObject simpleTestObject = getFirstSimpleTestObject();

        Map<String, List<Map<String, Class<?>>>> complexDataStructure = new HashMap<>();
        List<Map<String, Class<?>>> exampleList = new ArrayList<>();

        Map<String, Class<?>> someMap = new HashMap<>();

        someMap.put("EF", SimpleTestObject.class);
        exampleList.add(someMap);
        complexDataStructure.put("ABCD", exampleList);

        simpleTestObject.setComplexDataStructure(complexDataStructure);

        helper.save(simpleTestObject);

        SimpleTestObject one = helper.findOne(SimpleTestObject.class, simpleTestObject.getKeyField());

        assertEquals(simpleTestObject, one);

    }

    public void testSave() {

        SimpleTestObject simpleTestObject = getFirstSimpleTestObject();

        long count = helper.getCount(SimpleTestObject.class);

        helper.save(simpleTestObject);

        assertEquals(helper.getCount(SimpleTestObject.class), count + 1);


    }

    public void testSaveSet() {

        Set<SimpleTestObject> simpleTestObjects = new LinkedHashSet<>();

        for (int i = 0; i < 10; i++) simpleTestObjects.add(getFirstSimpleTestObject());

        long count = helper.getCount(SimpleTestObject.class);

        helper.save(simpleTestObjects);

        assertEquals(helper.getCount(SimpleTestObject.class), count + simpleTestObjects.size());

    }

    public void testRetrieve() {

        SimpleTestObject simpleTestObject = getFirstSimpleTestObject();
        helper.save(simpleTestObject);

        SimpleTestObject object = helper.findOne(SimpleTestObject.class, simpleTestObject.getKeyField());

        assertEquals(object, simpleTestObject);

    }

    public void testDelete() {

        SimpleTestObject object = getFirstSimpleTestObject();
        helper.save(object);

        long count = helper.getCount(SimpleTestObject.class);

        helper.remove(object);

        assertEquals(helper.getCount(SimpleTestObject.class), count - 1);

        assertNull(helper.findOne(SimpleTestObject.class, object.getKeyField()));

    }

    public void testSpecialQuery() {

//        helper.removeAll(SimpleTestObject.class);
//
//        SimpleTestObject simpleObject = getFirstSimpleTestObject();
//
//        helper.save(simpleObject);
//
//        QueryGroup queryGroup = QueryGroup.withQueries(new LinkedHashSet<Query<?>>(), true);

    }

    public SimpleTestObject getFirstSimpleTestObject() {
        return new SimpleTestObject().setCreatedAt(new Date()).setCustomString("ExampleQuery").setKeyField("Hello" +
                                                                                                                   new Date().getTime());
    }

}
