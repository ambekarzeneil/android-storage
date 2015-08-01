package com.focus.example.corestoragetester.app;

import android.app.Application;
import android.test.ApplicationTestCase;
import com.focus.example.corestoragetester.app.testObjects.SimpleDao;
import com.zva.android.data.CoreStorageHelper;

import java.util.Date;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testHello() throws Exception {
        assertTrue(CoreStorageHelper.hello());
    }

    public void testDao() throws Exception {
        SimpleDao simpleDao = CoreStorageHelper.getInstance().getDao(SimpleDao.class);
        simpleDao.findByCreatedAtGreaterThan(new Date());
    }

}