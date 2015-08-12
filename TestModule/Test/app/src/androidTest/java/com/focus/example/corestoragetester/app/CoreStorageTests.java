package com.focus.example.corestoragetester.app;

import android.app.Application;
import android.test.ApplicationTestCase;
import com.focus.example.corestoragetester.app.testObjects.SimpleTestObject;
import com.zva.android.commonLib.utils.CollectionUtils;
import com.zva.android.data.CoreStorageHelper;
import com.zva.android.data.core.Query;
import com.zva.android.data.core.QueryGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright CoreStorageTester 2015 Created by zeneilambekar on 05/08/15.
 */
public class CoreStorageTests extends ApplicationTestCase<Application> {

    private static final String TAG = "CoreStorageTests";
    private CoreStorageHelper   helper;

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

        for (int i = 0; i < 10; i++)
            simpleTestObjects.add(getFirstSimpleTestObject());

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

    public void testSimpleQuery() throws Exception {

        helper.removeAll(SimpleTestObject.class);

        helper.save(getFirstSimpleTestObject());

        Thread.sleep(1000);

        helper.save(getFirstSimpleTestObject());

        Thread.sleep(1000);

        final Date anchorDate = new Date();

        Thread.sleep(1000);

        helper.save(getFirstSimpleTestObject());

        Thread.sleep(1000);

        helper.save(getFirstSimpleTestObject());

        final Query<Date> createdAt = new Query.Builder<Date>().setPropertyName("createdAt").setPropertyValue(anchorDate).greaterThan().build();

        final Set<Query<?>> simpleQuery = new HashSet<>();
        simpleQuery.add(createdAt);

        assertEquals(helper.find(SimpleTestObject.class, QueryGroup.withQueries(simpleQuery, false)).size(), 2);

    }

    public void testComplexQuery() throws Exception {

        helper.removeAll(SimpleTestObject.class);

        final SimpleTestObject abc = getSimpleTestObject("abc");

        helper.save(abc);

        final Date anchorDate = new Date();

        Thread.sleep(1000);

        helper.save(getSimpleTestObject("def"));

        final SimpleTestObject otherAbc = getSimpleTestObject("abc");

        helper.save(otherAbc);

        final Query<String> stringAbc = new Query.Builder<String>().setPropertyName("simpleString").setPropertyValue("abc").like().build();


        final Query<Date> createdAt = new Query.Builder<Date>().setPropertyName("createdAt").setPropertyValue
                (anchorDate).lesserThan().build();

        //Test a simpler query

        final Set<Query<?>> simpleQuery = new HashSet<>();
        simpleQuery.add(stringAbc);

        Set<SimpleTestObject> simpleTestObjects = helper.find(SimpleTestObject.class, QueryGroup.withQueries
                (simpleQuery, true));

        assertEquals(simpleTestObjects.size(), 2);

        assertTrue(CollectionUtils.containsOnly(simpleTestObjects, abc, otherAbc));

        //Test a more complex query

        simpleQuery.add(createdAt);

        simpleTestObjects = helper.find(SimpleTestObject.class, QueryGroup.withQueries(simpleQuery, true));

        assertEquals(simpleTestObjects.size(), 1);

        assertTrue(CollectionUtils.containsOnly(simpleTestObjects, abc));

        //Test Or

        simpleTestObjects = helper.find(SimpleTestObject.class, QueryGroup.withQueries(simpleQuery, false));

        assertEquals(simpleTestObjects.size(), 2);

        assertTrue(CollectionUtils.containsOnly(simpleTestObjects, abc, otherAbc));

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

    private SimpleTestObject getFirstSimpleTestObject() {
        return new SimpleTestObject().setCreatedAt(new Date()).setCustomString("ExampleQuery").setKeyField("Hello" + new Date().getTime());
    }

    private SimpleTestObject getSimpleTestObject(String simpleString) {
        return getFirstSimpleTestObject().setSimpleString(simpleString);
    }

}
