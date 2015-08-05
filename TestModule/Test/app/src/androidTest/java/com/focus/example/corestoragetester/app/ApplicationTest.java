package com.focus.example.corestoragetester.app;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.zva.android.data.CoreStorageHelper;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private static final String TAG = "ApplicationTest";

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPackageWideLoading() throws Exception {
        CoreStorageHelper.init(getApplication().getApplicationContext());
        Log.i(TAG, CoreStorageHelper.getInstance().toString());
    }

    public void testDao() throws Exception {
//        SimpleDao simpleDao = CoreStorageHelper.getInstance().getDao(SimpleDao.class);
//        simpleDao.findByCreatedAtGreaterThan(new Date());
    }

}