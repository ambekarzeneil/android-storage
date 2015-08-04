package com.zva.android.data;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 04/08/15.
 */
class QueryPropertyTypeWrapper {

    private QueryPropertyType queryPropertyType;

    private String            customSqlProperty;

    public QueryPropertyTypeWrapper(QueryPropertyType queryPropertyType) {
        this.queryPropertyType = queryPropertyType;
    }

    public QueryPropertyTypeWrapper(String customSqlProperty) {
        queryPropertyType = QueryPropertyType.CUSTOM;
        this.customSqlProperty = customSqlProperty;
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
}
