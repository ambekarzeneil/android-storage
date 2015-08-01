package com.zva.android.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Contract;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.zva.android.commonLib.serialization.SerializationService;
import com.zva.android.commonLib.serialization.exception.SerializationException;
import com.zva.android.commonLib.serialization.impl.Serializers;
import com.zva.android.commonLib.utils.PackageUtils;
import com.zva.android.data.interfaces.CoreStorageDao;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
public class CoreStorageHelper {

    private static final String                         TAG = "DataHelper";

    private volatile boolean                            isReady;

    private Map<String, Map<String, QueryPropertyType>> classNameToQueryPropertyNameToQueryPropertyTypeMap;
    private Map<String, String>                         classNameToPrimaryKeyFieldNameMap;
    private Map<String, Method>                         classNameToPrimaryKeyGetterMap;
    private Map<String, Method>                         classNameToPrimaryKeySetterMap;
    private Context                                     applicationContext;
    private long                                        version;

    private CoreStorageHelper() {
        /* Required Private Constructor to prevent initiation outside getInstance()*/
    }

    public static void init(Context applicationContext, Configuration configuration) {

        synchronized (SingletonHolder.singletonInstance) {
            if (SingletonHolder.singletonInstance.isReady)
                return;

            SingletonHolder.singletonInstance.applicationContext = applicationContext;

            if (inspectApplicationContext())
                return;

            scanClasses(configuration);

            createDatabase();

            storeSettings();

            SingletonHolder.singletonInstance.isReady = true;

        }

    }

    private static void storeSettings() {

        Context applicationContext = SingletonHolder.singletonInstance.applicationContext;

        try {
            byte[] serializedObject = Serializers.getJavaSerializer().serialize(new Store(SingletonHolder.singletonInstance));

            FileOutputStream outputStream = applicationContext.openFileOutput("dataHelperMetaDataStore", Context.MODE_PRIVATE);

            outputStream.write(serializedObject);

        } catch (IOException | SerializationException e) {
            Log.e(TAG, "DataHelper meta-data could not be stored: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        //@formatter:off
        applicationContext.getSharedPreferences("DataHelper", Context.MODE_PRIVATE).edit().putBoolean("init", true).commit();
        //@formatter:on

    }

    private static void createDatabase() {

        throw new IllegalStateException("Method incomplete");

    }

    private static void scanClasses(Configuration configuration) {

        throw new IllegalStateException("Method incomplete");

    }

    @Contract(pure = true)
    public static boolean hello() {
        return true;
    }

    private static boolean inspectApplicationContext() {

        Context applicationContext = SingletonHolder.singletonInstance.applicationContext;

        SharedPreferences preferences = applicationContext.getSharedPreferences("DataHelper", Context.MODE_PRIVATE);

        if (!preferences.getBoolean("init", false))
            return false;

        Store store;

        try {
            SerializationService serializer = Serializers.getJavaSerializer();
            byte[] bytes = IOUtils.toByteArray(applicationContext.openFileInput("dataHelperMetaDataStore"));

            store = serializer.inflate(bytes, Store.class);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("DataHelper: Could not find buffer file");
        } catch (SerializationException | IOException e) {
            //Assume buffer file is corrupt
            return false;
        }

        if (PackageUtils.getApplicationMetaData(applicationContext, "VERSION", 0L) > store.version)
            return false;

        store.populateHelper(SingletonHolder.singletonInstance);

        return true;

    }

    @Contract(pure = true)
    public static CoreStorageHelper getInstance() {

        if (!SingletonHolder.singletonInstance.isReady)
            throw new IllegalStateException("CoreStorageHelper getInstance() is called without init()");

        return SingletonHolder.singletonInstance;

    }

    public <T extends CoreStorageDao> T getDao(Class<? extends T> daoClass) {
        throw new IllegalStateException("Method incomplete");
    }

    private static class SingletonHolder {

        private static final CoreStorageHelper singletonInstance = new CoreStorageHelper();

    }

    public static class Configuration {

        private List<String> basePackageNames;
        private Class<?>     basePackageClasses;

        public List<String> getBasePackageNames() {
            return basePackageNames;
        }

        public Configuration setBasePackageNames(List<String> basePackageNames) {
            this.basePackageNames = basePackageNames;
            return this;
        }

        public Class<?> getBasePackageClasses() {
            return basePackageClasses;
        }

        public Configuration setBasePackageClasses(Class<?> basePackageClasses) {
            this.basePackageClasses = basePackageClasses;
            return this;
        }

        @Override
        public String toString() {
            return "Configuration{" + "basePackageNames=" + basePackageNames + ", basePackageClasses=" + basePackageClasses + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Configuration that = (Configuration) o;

            return !(basePackageNames != null ? !basePackageNames.equals(that.basePackageNames) : that.basePackageNames != null)
                    && !(basePackageClasses != null ? !basePackageClasses.equals(that.basePackageClasses) : that.basePackageClasses != null);

        }

        @Override
        public int hashCode() {
            int result = basePackageNames != null ? basePackageNames.hashCode() : 0;
            result = 31 * result + (basePackageClasses != null ? basePackageClasses.hashCode() : 0);
            return result;
        }
    }

    private static class Store {

        private final Map<String, Map<String, QueryPropertyType>> classNameToQueryPropertyNameToQueryPropertyTypeMap;
        private final Map<String, String>                         classNameToPrimaryKeyFieldNameMap;
        private final Map<String, Method>                         classNameToPrimaryKeyGetterMap;
        private final Map<String, Method>                         classNameToPrimaryKeySetterMap;
        private final long                                        version;

        public Store(Map<String, Map<String, QueryPropertyType>> classNameToQueryPropertyNameToQueryPropertyTypeMap, Map<String, String> classNameToPrimaryKeyFieldNameMap,
                Map<String, Method> classNameToPrimaryKeyGetterMap, Map<String, Method> classNameToPrimaryKeySetterMap, long version) {
            this.classNameToQueryPropertyNameToQueryPropertyTypeMap = classNameToQueryPropertyNameToQueryPropertyTypeMap;
            this.classNameToPrimaryKeyFieldNameMap = classNameToPrimaryKeyFieldNameMap;
            this.classNameToPrimaryKeyGetterMap = classNameToPrimaryKeyGetterMap;
            this.classNameToPrimaryKeySetterMap = classNameToPrimaryKeySetterMap;
            this.version = version;
        }

        public Store(CoreStorageHelper singletonInstance) {
            this(singletonInstance.classNameToQueryPropertyNameToQueryPropertyTypeMap, singletonInstance.classNameToPrimaryKeyFieldNameMap,
                    singletonInstance.classNameToPrimaryKeyGetterMap, singletonInstance.classNameToPrimaryKeySetterMap, singletonInstance.version);
        }

        public void populateHelper(CoreStorageHelper dataHelper) {

            dataHelper.classNameToPrimaryKeyFieldNameMap = Collections.unmodifiableMap(classNameToPrimaryKeyFieldNameMap);
            dataHelper.classNameToPrimaryKeyGetterMap = Collections.unmodifiableMap(classNameToPrimaryKeyGetterMap);
            dataHelper.classNameToPrimaryKeySetterMap = Collections.unmodifiableMap(classNameToPrimaryKeySetterMap);
            dataHelper.classNameToQueryPropertyNameToQueryPropertyTypeMap = Collections.unmodifiableMap(classNameToQueryPropertyNameToQueryPropertyTypeMap);
            dataHelper.version = version;

        }

    }

}
