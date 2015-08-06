package com.zva.android.data.core;

import java.util.Collections;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 01/08/15.
 */
public class Query<PropertyType> implements IQueryResolver {

    private final PropertyType propertyValue;

    private final String       queryString;

    private Query(Builder builder) {
        propertyValue = builder.propertyValue;
        queryString = builder.queryString;
    }

    public PropertyType getPropertyValue() {
        return propertyValue;
    }

    public String getQueryString() {
        return queryString;
    }

    @Override
    public ResolvedQuery resolveQuery() {
        return new ResolvedQuery(queryString, Collections.singletonList((Object) propertyValue));
    }

    public class Builder {

        private String       propertyName;
        private PropertyType propertyValue;
        private String       queryString;

        public String getPropertyName() {
            return propertyName;
        }

        public Builder setPropertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        public PropertyType getPropertyValue() {
            return propertyValue;
        }

        public Builder setPropertyValue(PropertyType propertyValue) {
            this.propertyValue = propertyValue;
            return this;
        }

        public Builder equal() {
            return equal(false);
        }

        public Builder equal(boolean isString) {
            return operator("=", isString);
        }

        public Builder like() {
            queryString = propertyName + " like '?%'";
            return this;
        }

        public Builder greaterThan() {
            return greaterThan(false);
        }

        public Builder greaterThan(boolean isString) {
            return operator(">", isString);
        }

        public Builder lesserThan() {
            return lesserThan(false);
        }

        public Builder lesserThan(boolean isString) {
            return operator("<", isString);
        }

        public Builder operator(String operator, boolean isString) {
            queryString = String.format("%s %s %s", propertyName, operator, isString ? "'?'" : "?");
            return this;
        }

    }

}
