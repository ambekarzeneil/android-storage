package com.zva.android.data.core;

import java.util.Collections;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
public class Query<PropertyType> implements IQueryResolver {

    private final PropertyType propertyValue;

    private final String       queryString;

    private final boolean      isLike;

    private Query(Builder<PropertyType> builder) {
        propertyValue = builder.propertyValue;
        queryString = builder.queryString;
        isLike = builder.isLike;
    }

    public PropertyType getPropertyValue() {
        return propertyValue;
    }

    public String getQueryString() {
        return queryString;
    }

    @Override
    public ResolvedQuery resolveQuery() {
        return new ResolvedQuery(queryString, Collections.singletonList(isLike ? (Object) propertyValue : propertyValue + "%"));
    }

    public static class Builder<PropertyType> {

        private String       propertyName;
        private PropertyType propertyValue;
        private String       queryString;
        private boolean      isLike;

        public String getPropertyName() {
            return propertyName;
        }

        public Builder<PropertyType> setPropertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        public PropertyType getPropertyValue() {
            return propertyValue;
        }

        public Builder<PropertyType> setPropertyValue(PropertyType propertyValue) {
            this.propertyValue = propertyValue;
            return this;
        }

        public Builder<PropertyType> equal() {
            return equal(false);
        }

        public Builder<PropertyType> equal(boolean isString) {
            return operator("=", isString);
        }

        public Builder<PropertyType> like() {
            queryString = propertyName + " like ?";
            isLike = true;
            return this;
        }

        public Builder<PropertyType> greaterThan() {
            return greaterThan(false);
        }

        public Builder<PropertyType> greaterThan(boolean isString) {
            return operator(">", isString);
        }

        public Builder<PropertyType> lesserThan() {
            return lesserThan(false);
        }

        public Builder<PropertyType> lesserThan(boolean isString) {
            return operator("<", isString);
        }

        public Builder<PropertyType> operator(String operator, boolean isString) {
            queryString = String.format("%s %s %s", propertyName, operator, isString ? "'?'" : "?");
            return this;
        }

        public Query<PropertyType> build() {
            return new Query<>(this);
        }

    }

}
