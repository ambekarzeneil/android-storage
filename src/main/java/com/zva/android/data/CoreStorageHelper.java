package com.zva.android.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.zva.android.commonLib.serialization.SerializationService;
import com.zva.android.commonLib.serialization.exception.SerializationException;
import com.zva.android.commonLib.serialization.impl.Serializers;
import com.zva.android.commonLib.utils.CollectionUtils;
import com.zva.android.commonLib.utils.PackageUtils;
import com.zva.android.commonLib.utils.core.Filter;
import com.zva.android.data.annotations.CoreStorageEntity;
import com.zva.android.data.annotations.PrimaryKey;
import com.zva.android.data.annotations.QueryColumn;
import com.zva.android.data.interfaces.CoreStorageDao;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
public class CoreStorageHelper implements ISqlHelperDelegate {

    private static final String                                TAG                                                       = "DataHelper";

    private volatile boolean                                   isReady;

    private Map<String, Map<String, QueryPropertyTypeWrapper>> classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap = new HashMap<>();

    private Map<String, String>                                classNameToPrimaryKeyFieldNameMap                         = new HashMap<>();

    private Map<String, Method>                                classNameToPrimaryKeyGetterMap                            = new HashMap<>();

    private Map<String, Map<String, Method>>                   classNameToPropertyNameToGetterMap                        = new HashMap<>();

    private Map<Class<?>, String>                              classToTableNameMap                                       = new HashMap<>();

    private Map<String, Method>                                classNameToPrimaryKeySetterMap                            = new HashMap<>();

    private Map<String, DatabaseType>                          classNameToDatabaseTypeMap                                = new HashMap<>();

    private Context                                            applicationContext;

    private int                                                version;

    private SqlHelper                                          privateDatabaseSqlHelper;

    private SqlHelper                                          publicDatabaseSqlHelper;

    private CoreStorageHelper() {
        /* Required Private Constructor to prevent initiation outside getInstance()*/
    }

    public static void init(Context applicationContext) {
        init(applicationContext, Configuration.getDefaultConfiguration());
    }

    public static void init(Context applicationContext, Configuration configuration) {

        synchronized (SingletonHolder.singletonInstance) {
            if (SingletonHolder.singletonInstance.isReady)
                return;

            SingletonHolder.singletonInstance.applicationContext = applicationContext;

            boolean previouslyInitialized = inspectApplicationContext();

            if (!previouslyInitialized)
                scanClasses(configuration);

            createDatabase(configuration);

            if (!previouslyInitialized)
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

    private static void createDatabase(Configuration configuration) {

        CoreStorageHelper instance = SingletonHolder.singletonInstance;

        SqlHelper.Builder privateDatabaseBuilder = SqlHelper.builder().setApplicationContext(instance.applicationContext).setDatabaseName(configuration.getDatabaseName()).setDatabasePath(
                configuration.getDatabasePath()).setDatabaseVersion(instance.version).setSqlHelperDelegate(instance);

        SqlHelper.Builder publicDatabaseBuilder = privateDatabaseBuilder.copy();

        publicDatabaseBuilder.setPrivateDatabase(false);
        privateDatabaseBuilder.setPrivateDatabase(true);

        instance.privateDatabaseSqlHelper = privateDatabaseBuilder.build();

        if (configuration.isEnablePublicDatabase())
            instance.publicDatabaseSqlHelper = publicDatabaseBuilder.build();

    }

    private static void scanClasses(Configuration configuration) {

        Set<Class<?>> potentialCoreStorageClasses = HelperUtils.getCoreStorageClasses(configuration);

        for (Class<?> potentialStorageClass : potentialCoreStorageClasses) {
            CoreStorageEntity coreStorageEntityAnnotation = potentialStorageClass.getAnnotation(CoreStorageEntity.class);
            if (coreStorageEntityAnnotation != null)
                try {
                    loadClass(potentialStorageClass, coreStorageEntityAnnotation);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(String.format("Invalid CoreStorageEntity class '%s'", potentialStorageClass.getName()), e);

                }
        }

    }

    private static void loadClass(Class<?> storageClass, CoreStorageEntity coreStorageEntityAnnotation) throws NoSuchMethodException {

        String effectiveTableName = HelperUtils.getTableName(storageClass);

        CoreStorageHelper instance = SingletonHolder.singletonInstance;

        instance.classToTableNameMap.put(storageClass, effectiveTableName);

        instance.classNameToDatabaseTypeMap.put(effectiveTableName, coreStorageEntityAnnotation.shared() ? DatabaseType.SHARED : DatabaseType.PRIVATE);

        HashMap<String, QueryPropertyTypeWrapper> propertyMap = new HashMap<>();
        instance.classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap.put(effectiveTableName, propertyMap);

        HashMap<String, Method> propertyGetterMap = new HashMap<>();
        instance.classNameToPropertyNameToGetterMap.put(effectiveTableName, propertyGetterMap);

        //Methods:

        Field[] declaredFields = storageClass.getDeclaredFields();

        for (Field field : declaredFields) {
            String fieldName = field.getName();
            for (Annotation annotation : field.getAnnotations()) {

                if (annotation instanceof PrimaryKey) {
                    instance.classNameToPrimaryKeyFieldNameMap.put(effectiveTableName, fieldName);
                    instance.classNameToPrimaryKeyGetterMap.put(effectiveTableName, getMethodForProperty(storageClass, fieldName, null, true));
                    instance.classNameToPrimaryKeySetterMap.put(effectiveTableName, getMethodForProperty(storageClass, fieldName, field.getType(), false));
                }

                if (annotation instanceof PrimaryKey || annotation instanceof QueryColumn) {

                    QueryPropertyTypeWrapper queryPropertyTypeWrapper = null;

                    if (annotation instanceof QueryColumn) {
                        QueryColumn queryColumnAnnotation = (QueryColumn) annotation;
                        String sqlType = queryColumnAnnotation.sqlType();
                        if (!com.zva.android.commonLib.utils.StringUtils.isEmpty(sqlType))
                            queryPropertyTypeWrapper = new QueryPropertyTypeWrapper(sqlType);

                    }

                    if (queryPropertyTypeWrapper == null)
                        queryPropertyTypeWrapper = new QueryPropertyTypeWrapper(getQueryPropertyType(field.getType()));

                    propertyMap.put(fieldName, queryPropertyTypeWrapper);
                    propertyGetterMap.put(fieldName, getMethodForProperty(storageClass, fieldName, null, true));
                }

            }

        }

    }

    private static QueryPropertyType getQueryPropertyType(Class<?> type) {
        if (type.isAssignableFrom(String.class))
            return QueryPropertyType.STRING;

        if (type.isAssignableFrom(Date.class))
            return QueryPropertyType.DATE;

        if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Long.class))
            return QueryPropertyType.NUMBER;

        throw new IllegalArgumentException(String.format("Cannot accept QueryPropertyType of class '%s'", type.getName()));

    }

    private static Method getMethodForProperty(Class<?> concernedClass, String fieldName, Class<?> type, boolean getter) throws NoSuchMethodException {
        return concernedClass.getMethod((getter ? "get" : "set") + StringUtils.capitalize(fieldName), getter ? null : type);
    }

    private static boolean inspectApplicationContext() {

        CoreStorageHelper instance = SingletonHolder.singletonInstance;

        Context applicationContext = instance.applicationContext;
        instance.version = PackageUtils.getApplicationMetaData(applicationContext, "VERSION", 0);

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

        if (instance.version > store.version)
            return false;

        store.populateHelper(instance);

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

    @Override
    public String getSqlType(String tableName, String fieldName) {
        return classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap.get(tableName).get(fieldName).getCustomSqlProperty();
    }

    @Override
    public Set<String> getTableNames(final DatabaseType databaseType) {
        return new LinkedHashSet<>(CollectionUtils.filter(classNameToDatabaseTypeMap.keySet(), new Filter<String>() {
            @Override
            public boolean test(String object) {
                return classNameToDatabaseTypeMap.get(object) == databaseType;
            }
        }));
    }

    @Override
    public String getPrimaryKeyFieldName(String tableName) {
        return classNameToPrimaryKeyFieldNameMap.get(tableName);
    }

    @Override
    public Set<String> getQueryColumnNames(String tableName) {
        return classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap.get(tableName).keySet();
    }

    @Override
    public QueryPropertyType getQueryColumnType(String tableName, String queryColumnName) {
        return classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap.get(tableName).get(queryColumnName).getQueryPropertyType();
    }

    private static class SingletonHolder {

        private static final CoreStorageHelper singletonInstance = new CoreStorageHelper();

    }

    private static class Store {

        private final Map<String, Map<String, QueryPropertyTypeWrapper>> classNameToQueryPropertyNameToQueryPropertyTypeMap;
        private final Map<String, String>                                classNameToPrimaryKeyFieldNameMap;
        private final Map<String, Method>                                classNameToPrimaryKeyGetterMap;
        private final Map<String, Method>                                classNameToPrimaryKeySetterMap;
        private final int                                                version;

        public Store(Map<String, Map<String, QueryPropertyTypeWrapper>> classNameToQueryPropertyNameToQueryPropertyTypeMap, Map<String, String> classNameToPrimaryKeyFieldNameMap,
                Map<String, Method> classNameToPrimaryKeyGetterMap, Map<String, Method> classNameToPrimaryKeySetterMap, int version) {
            this.classNameToQueryPropertyNameToQueryPropertyTypeMap = classNameToQueryPropertyNameToQueryPropertyTypeMap;
            this.classNameToPrimaryKeyFieldNameMap = classNameToPrimaryKeyFieldNameMap;
            this.classNameToPrimaryKeyGetterMap = classNameToPrimaryKeyGetterMap;
            this.classNameToPrimaryKeySetterMap = classNameToPrimaryKeySetterMap;
            this.version = version;
        }

        public Store(CoreStorageHelper singletonInstance) {
            this(singletonInstance.classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap, singletonInstance.classNameToPrimaryKeyFieldNameMap,
                    singletonInstance.classNameToPrimaryKeyGetterMap, singletonInstance.classNameToPrimaryKeySetterMap, singletonInstance.version);
        }

        public void populateHelper(CoreStorageHelper dataHelper) {

            dataHelper.classNameToPrimaryKeyFieldNameMap = Collections.unmodifiableMap(classNameToPrimaryKeyFieldNameMap);
            dataHelper.classNameToPrimaryKeyGetterMap = Collections.unmodifiableMap(classNameToPrimaryKeyGetterMap);
            dataHelper.classNameToPrimaryKeySetterMap = Collections.unmodifiableMap(classNameToPrimaryKeySetterMap);
            dataHelper.classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap = Collections.unmodifiableMap(classNameToQueryPropertyNameToQueryPropertyTypeMap);
            dataHelper.version = version;

        }

    }

}
