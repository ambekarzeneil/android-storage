package com.zva.android.data;

import java.util.List;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 04/08/15.
 */
public class Configuration {

    private static final Configuration DEFAULT_CONFIGURATION;

    static {
        DEFAULT_CONFIGURATION = new Configuration();
        DEFAULT_CONFIGURATION.setDatabaseName("coreStorageDatabase");
        DEFAULT_CONFIGURATION.setEnablePublicDatabase(false);
    }

    private List<String>               basePackageNames;
    private List<Class<?>>             basePackageClasses;
    private String                     databaseName;
    private String                     databasePath;
    private int                        databaseVersion;
    private boolean                    enablePublicDatabase;

    public static Configuration getDefaultConfiguration() {
        return DEFAULT_CONFIGURATION;
    }

    public List<String> getBasePackageNames() {
        return basePackageNames;
    }

    public Configuration setBasePackageNames(List<String> basePackageNames) {
        this.basePackageNames = basePackageNames;
        return this;
    }

    public List<Class<?>> getBasePackageClasses() {
        return basePackageClasses;
    }

    public Configuration setBasePackageClasses(List<Class<?>> basePackageClasses) {
        this.basePackageClasses = basePackageClasses;
        return this;
    }

    @Override
    public String toString() {
        return "Configuration{" + "basePackageNames=" + basePackageNames + ", basePackageClasses=" + basePackageClasses + ", databaseName='" + databaseName + '\''
                + ", databasePath='" + databasePath + '\'' + ", databaseVersion=" + databaseVersion + ", enablePublicDatabase=" + enablePublicDatabase + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Configuration that = (Configuration) o;

        return databaseVersion == that.databaseVersion && enablePublicDatabase == that.enablePublicDatabase
                && !(basePackageNames != null ? !basePackageNames.equals(that.basePackageNames) : that.basePackageNames != null)
                && !(basePackageClasses != null ? !basePackageClasses.equals(that.basePackageClasses) : that.basePackageClasses != null)
                && !(databaseName != null ? !databaseName.equals(that.databaseName) : that.databaseName != null)
                && !(databasePath != null ? !databasePath.equals(that.databasePath) : that.databasePath != null);

    }

    @Override
    public int hashCode() {
        int result = basePackageNames != null ? basePackageNames.hashCode() : 0;
        result = 31 * result + (basePackageClasses != null ? basePackageClasses.hashCode() : 0);
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + (databasePath != null ? databasePath.hashCode() : 0);
        result = 31 * result + databaseVersion;
        result = 31 * result + (enablePublicDatabase ? 1 : 0);
        return result;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(int databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public boolean isEnablePublicDatabase() {
        return enablePublicDatabase;
    }

    public void setEnablePublicDatabase(boolean enablePublicDatabase) {
        this.enablePublicDatabase = enablePublicDatabase;
    }
}
