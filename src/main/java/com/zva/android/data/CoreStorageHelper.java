package com.zva.android.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import com.zva.android.data.core.QueryGroup;
import com.zva.android.data.exception.CoreStorageClassNotCachedException;
import com.zva.android.data.exception.CoreStorageFindException;
import com.zva.android.data.exception.CoreStorageReadException;
import com.zva.android.data.exception.CoreStorageRemoveException;
import com.zva.android.data.exception.CoreStorageSaveException;
import com.zva.android.data.exception.CoreStorageWriteException;
import com.zva.android.data.exception.MalformedClassGetterException;
import com.zva.android.data.interfaces.CoreStorageDao;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
public class CoreStorageHelper {

    private static final String                                TAG                                                       = "DataHelper";
    private final SerializationService                         serializationService                                      = Serializers.getProtostuffSerializer();
    private final PrivateSqlDelegate                           sqlDelegate                                               = new PrivateSqlDelegate();
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
            else
                loadGettersAndSetters();

            createDatabase(configuration);

            if (!previouslyInitialized)
                storeSettings();

            SingletonHolder.singletonInstance.isReady = true;

        }

    }

    @Contract(pure = true)
    public static CoreStorageHelper getInstance() {

        synchronized (SingletonHolder.singletonInstance) {
            if (!SingletonHolder.singletonInstance.isReady)
                throw new IllegalStateException("CoreStorageHelper getInstance() is called without init()");
        }

        return SingletonHolder.singletonInstance;

    }

    private static void storeSettings() {

        Context applicationContext = SingletonHolder.singletonInstance.applicationContext;

        try {
            byte[] serializedObject = Serializers.getJavaSerializer().serialize(new Store(SingletonHolder.singletonInstance));

            FileOutputStream outputStream = applicationContext.openFileOutput("dataHelperMetaDataStore", Context.MODE_PRIVATE);

            outputStream.write(serializedObject);

            outputStream.flush();

            outputStream.close();

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
                configuration.getDatabasePath()).setDatabaseVersion(instance.version).setSqlHelperDelegate(instance.sqlDelegate);

        SqlHelper.Builder publicDatabaseBuilder = privateDatabaseBuilder.copy();

        publicDatabaseBuilder.setPrivateDatabase(false);
        privateDatabaseBuilder.setPrivateDatabase(true);

        instance.privateDatabaseSqlHelper = privateDatabaseBuilder.build();

        if (configuration.isEnablePublicDatabase())
            instance.publicDatabaseSqlHelper = publicDatabaseBuilder.build();

    }

    private static void scanClasses(Configuration configuration) {

        Set<Class<?>> potentialCoreStorageClasses;
        try {
            potentialCoreStorageClasses = HelperUtils.getCoreStorageClasses(configuration, SingletonHolder.singletonInstance.applicationContext);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read .dex file for package scanning");
        }

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
                            queryPropertyTypeWrapper = new QueryPropertyTypeWrapper(sqlType, field.getType());
                    }

                    if (queryPropertyTypeWrapper == null)
                        queryPropertyTypeWrapper = new QueryPropertyTypeWrapper(getQueryPropertyType(field.getType()));

                    propertyMap.put(fieldName, queryPropertyTypeWrapper);
                    propertyGetterMap.put(fieldName, getMethodForProperty(storageClass, fieldName, null, true));
                }

            }

        }

    }

    private static void loadGettersAndSetters() {

        CoreStorageHelper instance = SingletonHolder.singletonInstance;

        for (Class<?> storageClass : instance.classToTableNameMap.keySet()) {
            String tableName = instance.classToTableNameMap.get(storageClass);

            Map<String, QueryPropertyTypeWrapper> propertyMap = instance.classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap.get(tableName);

            HashMap<String, Method> propertyGetterMap = new HashMap<>();

            instance.classNameToPropertyNameToGetterMap.put(tableName, propertyGetterMap);

            for (String fieldName : propertyMap.keySet()) {
                try {

                    Method getterMethod = getMethodForProperty(storageClass, fieldName, null, true);

                    propertyGetterMap.put(fieldName, getterMethod);

                    if (fieldName.equals(instance.classNameToPrimaryKeyFieldNameMap.get(tableName))) {
                        instance.classNameToPrimaryKeyGetterMap.put(tableName, getterMethod);
                        instance.classNameToPrimaryKeySetterMap.put(
                                tableName,
                                getMethodForProperty(storageClass, fieldName,
                                        HelperUtils.getClassFromType(instance.classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap.get(tableName).get(fieldName)), false));
                    }

                } catch (NoSuchMethodException e) {
                    //TODO: Make this fail-safe
                    throw new IllegalStateException(String.format("Could not load meta data for class '%s", storageClass.getName()));
                }
            }

        }

    }

    private static QueryPropertyType getQueryPropertyType(Class<?> type) {
        if (type.isAssignableFrom(String.class))
            return QueryPropertyType.STRING;

        if (type.isAssignableFrom(Date.class))
            return QueryPropertyType.DATE;

        if (type.isAssignableFrom(Long.class))
            return QueryPropertyType.LONG;

        if (type.isAssignableFrom(Integer.class))
            return QueryPropertyType.INTEGER;

        if (type.isAssignableFrom(Boolean.class))
            return QueryPropertyType.BOOLEAN;

        throw new IllegalArgumentException(String.format("Cannot accept QueryPropertyType of class '%s'", type.getName()));

    }

    private static Method getMethodForProperty(Class<?> concernedClass, String fieldName, Class<?> type, boolean getter) throws NoSuchMethodException {

        Log.i(TAG, (getter ? "get" : "set") + StringUtils.capitalize(fieldName));

        if (!getter)
            return concernedClass.getMethod("set" + StringUtils.capitalize(fieldName), type);
        else
            return concernedClass.getMethod("get" + StringUtils.capitalize(fieldName));

    }

    private static boolean inspectApplicationContext() {

        CoreStorageHelper instance = SingletonHolder.singletonInstance;

        Context applicationContext = instance.applicationContext;
        instance.version = PackageUtils.getApplicationMetaData(applicationContext, "VERSION", 1);

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

    public long getCount(Class<?> coreStorageClass) {
        String tableName = getTableName(coreStorageClass);
        return getDatabaseHelper(tableName).getObjectCount(tableName);
    }

    public <T> T findOne(Class<? extends T> coreStorageClass, String key) {
        String tableName = getTableName(coreStorageClass);

        try {
            return getDatabaseHelper(tableName).get(key, tableName);
        } catch (CoreStorageReadException | SerializationException e) {
            throw new CoreStorageFindException(e);
        }
    }

    public <T> Set<T> find(Class<? extends T> coreStorageClass, QueryGroup queryGroup) {
        String tableName = getTableName(coreStorageClass);

        try {
            return getDatabaseHelper(tableName).get(queryGroup, tableName);
        } catch (SerializationException e) {
            throw new CoreStorageFindException(e);
        }

    }

    public <T> Set<T> findAll(Class<?> coreStorageClass) {
        String tableName = getTableName(coreStorageClass);
        return getDatabaseHelper(tableName).getAll(tableName);
    }

    public <T extends CoreStorageDao> T getDao(Class<? extends T> daoClass) {
        throw new IllegalStateException("Method incomplete");
    }

    public <T> void save(T coreStorageObject) {

        String tableName = classToTableNameMap.get(coreStorageObject.getClass());
        try {
            getDatabaseHelper(tableName).save(coreStorageObject, tableName);
        } catch (SerializationException | CoreStorageWriteException e) {
            throw new CoreStorageSaveException(e);
        }

    }

    public <T> void save(Iterable<T> coreStorageObjects) {

        String tableName = null;
        SqlHelper databaseHelper = null;

        for (T coreStorageObject : coreStorageObjects) {
            if (tableName == null) {
                tableName = classToTableNameMap.get(coreStorageObject.getClass());
                databaseHelper = getDatabaseHelper(tableName);
            }
            try {
                databaseHelper.save(coreStorageObject, tableName);
            } catch (CoreStorageWriteException | SerializationException e) {
                throw new CoreStorageSaveException(e);
            }
        }

    }

    public <T> boolean remove(T coreStorageObject) {

        String tableName = classToTableNameMap.get(coreStorageObject.getClass());
        Object keyValue;
        try {
            keyValue = classNameToPrimaryKeyGetterMap.get(tableName).invoke(coreStorageObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new MalformedClassGetterException(coreStorageObject.getClass(), classNameToPrimaryKeyGetterMap.get(tableName), e);
        }

        return remove(keyValue.toString(), tableName);
    }

    public <T> long remove(Class<? extends T> coreStorageObjectClass, Iterable<T> coreStorageObjects) {
        throw new IllegalStateException("Method incomplete");
    }

    public <T> long remove(Class<? extends T> coreStorageObjectClass, QueryGroup queryGroup) {
        throw new IllegalStateException("Method incomplete");
    }

    public <T> boolean remove(String key, Class<? extends T> coreStorageObjectClass) {
        return remove(key, classToTableNameMap.get(coreStorageObjectClass));
    }

    public <T> boolean removeAll(Class<? extends T> coreStorageObjectClass) {
        String tableName = classToTableNameMap.get(coreStorageObjectClass);
        try {
            return getDatabaseHelper(tableName).truncate(tableName);
        } catch (CoreStorageWriteException e) {
            throw new CoreStorageRemoveException(e);
        }
    }

    private boolean remove(String key, String tableName) {
        try {
            return getDatabaseHelper(tableName).delete(key, tableName);
        } catch (CoreStorageWriteException e) {
            throw new CoreStorageRemoveException(e);
        }
    }

    private String getTableName(Class<?> coreStorageClass) {
        String tableName = classToTableNameMap.get(coreStorageClass);

        if (com.zva.android.commonLib.utils.StringUtils.isEmpty(tableName))
            throw new CoreStorageClassNotCachedException(coreStorageClass);

        return tableName;
    }

    private SqlHelper getDatabaseHelper(String tableName) {
        return (classNameToDatabaseTypeMap.get(tableName) == DatabaseType.PRIVATE ? privateDatabaseSqlHelper : publicDatabaseSqlHelper);
    }

    //Delegate Methods

    @Override
    public String toString() {
        return "CoreStorageHelper{" + "isReady=" + isReady + ", " + "classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap="
                + classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap + ", classNameToPrimaryKeyFieldNameMap=" + classNameToPrimaryKeyFieldNameMap
                + ", classNameToPrimaryKeyGetterMap=" + classNameToPrimaryKeyGetterMap + ", classNameToPropertyNameToGetterMap=" + classNameToPropertyNameToGetterMap
                + ", classToTableNameMap=" + classToTableNameMap + ", " + "classNameToPrimaryKeySetterMap=" + classNameToPrimaryKeySetterMap + ", classNameToDatabaseTypeMap="
                + classNameToDatabaseTypeMap + ", applicationContext=" + applicationContext + ", version=" + version + ", privateDatabaseSqlHelper=" + privateDatabaseSqlHelper
                + ", publicDatabaseSqlHelper=" + publicDatabaseSqlHelper + '}';
    }

    private static class SingletonHolder {
        private static final CoreStorageHelper singletonInstance = new CoreStorageHelper();
    }

    private static class Store implements Serializable {

        private static final long                                        serialVersionUID = 7380622423164594109L;

        private final Map<String, Map<String, QueryPropertyTypeWrapper>> classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap;
        private final Map<Class<?>, String>                              classToTableNameMap;
        private final Map<String, DatabaseType>                          classNameToDatabaseTypeMap;
        private final Map<String, String>                                classNameToPrimaryKeyFieldNameMap;
        private final int                                                version;

        public Store(CoreStorageHelper singletonInstance) {
            classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap = singletonInstance.classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap;
            classToTableNameMap = singletonInstance.classToTableNameMap;
            classNameToDatabaseTypeMap = singletonInstance.classNameToDatabaseTypeMap;
            classNameToPrimaryKeyFieldNameMap = singletonInstance.classNameToPrimaryKeyFieldNameMap;
            version = singletonInstance.version;
        }

        public void populateHelper(CoreStorageHelper dataHelper) {
            dataHelper.classNameToPrimaryKeyFieldNameMap = Collections.unmodifiableMap(classNameToPrimaryKeyFieldNameMap);
            dataHelper.classToTableNameMap = Collections.unmodifiableMap(classToTableNameMap);
            dataHelper.classNameToDatabaseTypeMap = Collections.unmodifiableMap(classNameToDatabaseTypeMap);
            dataHelper.classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap = Collections.unmodifiableMap(classNameToQueryPropertyNameToQueryPropertyTypeWrapperMap);
            dataHelper.version = version;
        }

    }

    private class PrivateSqlDelegate implements ISqlHelperDelegate {

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

        @Override
        public <T> T inflateObject(final String tableName, byte[] serializedObject) throws SerializationException {
            try {
                //noinspection unchecked
                return serializationService.inflate(serializedObject, (Class<? extends T>) CollectionUtils.find(classToTableNameMap.keySet(), new Filter<Class<?>>() {
                    @Override
                    public boolean test(Class<?> object) {
                        return classToTableNameMap.get(object).equals(tableName);
                    }
                }));
            } catch (ClassCastException exception) {
                throw new SerializationException(exception);
            }
        }

        @Override
        public <T> byte[] serializeObject(T coreStorageObject) throws SerializationException {
            return serializationService.serialize(coreStorageObject);
        }

        @Override
        public <T> Object getQueryColumnValue(T coreStorageObject, String tableName, String columnName) {
            try {
                return classNameToPropertyNameToGetterMap.get(tableName).get(columnName).invoke(coreStorageObject);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MalformedClassGetterException(coreStorageObject.getClass().getName(), columnName, e);
            }
        }
    }

}
