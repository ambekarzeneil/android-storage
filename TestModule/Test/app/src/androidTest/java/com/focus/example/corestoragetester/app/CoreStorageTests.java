package com.focus.example.corestoragetester.app;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;
import com.focus.example.corestoragetester.app.testObjects.SimpleTestObject;
import com.zva.android.data.CoreStorageHelper;

import java.util.Date;

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

    public void testSave() {

        SimpleTestObject simpleTestObject = getFirstSimpleTestObject();

        long count = helper.getCount(SimpleTestObject.class);

        helper.save(simpleTestObject);

        assertEquals(helper.getCount(SimpleTestObject.class), count + 1);

        Log.w(TAG, "testSave OK !");

    }

    public void testRetrieve() {

        SimpleTestObject simpleTestObject = getFirstSimpleTestObject();
        helper.save(simpleTestObject);

        SimpleTestObject object = helper.findOne(SimpleTestObject.class, simpleTestObject.getKeyField());

        assertEquals(object, simpleTestObject);

        Log.w(TAG, "testRetrieve OK !");
    }

    public void testDelete() {

        SimpleTestObject object = getFirstSimpleTestObject();
        helper.save(object);

        long count = helper.getCount(SimpleTestObject.class);

        helper.remove(object);

        assertEquals(helper.getCount(SimpleTestObject.class), count - 1);

        Log.w(TAG, "testDelete OK !");
    }

    public SimpleTestObject getFirstSimpleTestObject() {
        return new SimpleTestObject().setCreatedAt(new Date()).setCustomString("Custom String").setKeyField("Hello" + new Date().getTime());
    }

}
