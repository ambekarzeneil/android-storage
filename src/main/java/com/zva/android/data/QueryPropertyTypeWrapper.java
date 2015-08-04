package com.zva.android.data;

import java.io.Serializable;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 04/08/15.
 */
class QueryPropertyTypeWrapper implements Serializable {

    private static final long serialVersionUID = 1340390683692239794L;

    private QueryPropertyType queryPropertyType;

    private String            customSqlProperty;

    private Class<?>          customClass;

    public QueryPropertyTypeWrapper(QueryPropertyType queryPropertyType) {
        this.queryPropertyType = queryPropertyType;
    }

    public QueryPropertyTypeWrapper(String customSqlProperty, Class<?> type) {
        queryPropertyType = QueryPropertyType.CUSTOM;
        this.customSqlProperty = customSqlProperty;
        this.customClass = type;
    }

    public QueryPropertyType getQueryPropertyType() {
        return queryPropertyType;
    }

    public QueryPropertyTypeWrapper setQueryPropertyType(QueryPropertyType queryPropertyType) {
        this.queryPropertyType = queryPropertyType;
        return this;
    }

    public String getCustomSqlProperty() {
        return customSqlProperty;
    }

    public QueryPropertyTypeWrapper setCustomSqlProperty(String customSqlProperty) {
        this.customSqlProperty = customSqlProperty;
        return this;
    }

    @Override
    public String toString() {
        return "QueryPropertyTypeWrapper{" + "queryPropertyType=" + queryPropertyType + ", customSqlProperty='" + customSqlProperty + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        QueryPropertyTypeWrapper that = (QueryPropertyTypeWrapper) o;

        return queryPropertyType == that.queryPropertyType && !(customSqlProperty != null ? !customSqlProperty.equals(that.customSqlProperty) : that.customSqlProperty != null);

    }

    @Override
    public int hashCode() {
        int result = queryPropertyType != null ? queryPropertyType.hashCode() : 0;
        result = 31 * result + (customSqlProperty != null ? customSqlProperty.hashCode() : 0);
        return result;
    }

    public Class<?> getCustomClass() {
        return customClass;
    }

    public void setCustomClass(Class<?> customClass) {
        this.customClass = customClass;
    }
}
