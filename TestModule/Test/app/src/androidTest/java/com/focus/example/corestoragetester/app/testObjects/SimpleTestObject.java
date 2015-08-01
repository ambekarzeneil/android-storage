package com.focus.example.corestoragetester.app.testObjects;

import com.zva.android.data.annotations.CoreStorageEntity;
import com.zva.android.data.annotations.Ignored;
import com.zva.android.data.annotations.PrimaryKey;
import com.zva.android.data.annotations.QueryColumn;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Copyright CoreStorageTester 2015 Created by zeneilambekar on 31/07/15.
 */
@CoreStorageEntity
public class SimpleTestObject {

    @PrimaryKey
    private String                                   keyField;

    @QueryColumn
    private String                                   simpleString;

    @QueryColumn
    private Boolean                                  simpleBoolean;

    @QueryColumn
    private Long                                     simpleLong;

    private String                                   nonQueryColumn;

    @QueryColumn
    private Date                                     createdAt;

    @Ignored
    private String                                   transientData;

    private Map<String, String>                      stringToStringMap;

    private Map<String, Class<?>>                    stringToClassMap;

    private Map<String, List<Map<String, Class<?>>>> complexDataStructure;

    public String getSimpleString() {
        return simpleString;
    }

    public SimpleTestObject setSimpleString(String simpleString) {
        this.simpleString = simpleString;
        return this;
    }

    public Boolean getSimpleBoolean() {
        return simpleBoolean;
    }

    public SimpleTestObject setSimpleBoolean(Boolean simpleBoolean) {
        this.simpleBoolean = simpleBoolean;
        return this;
    }

    public Long getSimpleLong() {
        return simpleLong;
    }

    public SimpleTestObject setSimpleLong(Long simpleLong) {
        this.simpleLong = simpleLong;
        return this;
    }

    public Map<String, String> getStringToStringMap() {
        return stringToStringMap;
    }

    public SimpleTestObject setStringToStringMap(Map<String, String> stringToStringMap) {
        this.stringToStringMap = stringToStringMap;
        return this;
    }

    public Map<String, Class<?>> getStringToClassMap() {
        return stringToClassMap;
    }

    public SimpleTestObject setStringToClassMap(Map<String, Class<?>> stringToClassMap) {
        this.stringToClassMap = stringToClassMap;
        return this;
    }

    public String getKeyField() {
        return keyField;
    }

    public SimpleTestObject setKeyField(String keyField) {
        this.keyField = keyField;
        return this;
    }

    public String getNonQueryColumn() {
        return nonQueryColumn;
    }

    public SimpleTestObject setNonQueryColumn(String nonQueryColumn) {
        this.nonQueryColumn = nonQueryColumn;
        return this;
    }

    public String getTransientData() {
        return transientData;
    }

    public SimpleTestObject setTransientData(String transientData) {
        this.transientData = transientData;
        return this;
    }

    public Map<String, List<Map<String, Class<?>>>> getComplexDataStructure() {
        return complexDataStructure;
    }

    public SimpleTestObject setComplexDataStructure(Map<String, List<Map<String, Class<?>>>> complexDataStructure) {
        this.complexDataStructure = complexDataStructure;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public SimpleTestObject setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String toString() {
        return "SimpleTestObject{" + "keyField='" + keyField + '\'' + ", simpleString='" + simpleString + '\'' + ", simpleBoolean=" + simpleBoolean + ", simpleLong=" + simpleLong
                + ", nonQueryColumn='" + nonQueryColumn + '\'' + ", createdAt=" + createdAt + ", transientData='" + transientData + '\'' + ", stringToStringMap="
                + stringToStringMap + ", stringToClassMap=" + stringToClassMap + ", complexDataStructure=" + complexDataStructure + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleTestObject that = (SimpleTestObject) o;

        if (keyField != null ? !keyField.equals(that.keyField) : that.keyField != null) return false;
        if (simpleString != null ? !simpleString.equals(that.simpleString) : that.simpleString != null) return false;
        if (simpleBoolean != null ? !simpleBoolean.equals(that.simpleBoolean) : that.simpleBoolean != null)
            return false;
        if (simpleLong != null ? !simpleLong.equals(that.simpleLong) : that.simpleLong != null) return false;
        if (nonQueryColumn != null ? !nonQueryColumn.equals(that.nonQueryColumn) : that.nonQueryColumn != null)
            return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (transientData != null ? !transientData.equals(that.transientData) : that.transientData != null)
            return false;
        if (stringToStringMap != null ? !stringToStringMap.equals(that.stringToStringMap) : that.stringToStringMap !=
                null)
            return false;
        if (stringToClassMap != null ? !stringToClassMap.equals(that.stringToClassMap) : that.stringToClassMap != null)
            return false;
        return !(complexDataStructure != null ? !complexDataStructure.equals(that.complexDataStructure) : that
                .complexDataStructure != null);

    }

    @Override
    public int hashCode() {
        int result = keyField != null ? keyField.hashCode() : 0;
        result = 31 * result + (simpleString != null ? simpleString.hashCode() : 0);
        result = 31 * result + (simpleBoolean != null ? simpleBoolean.hashCode() : 0);
        result = 31 * result + (simpleLong != null ? simpleLong.hashCode() : 0);
        result = 31 * result + (nonQueryColumn != null ? nonQueryColumn.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (transientData != null ? transientData.hashCode() : 0);
        result = 31 * result + (stringToStringMap != null ? stringToStringMap.hashCode() : 0);
        result = 31 * result + (stringToClassMap != null ? stringToClassMap.hashCode() : 0);
        result = 31 * result + (complexDataStructure != null ? complexDataStructure.hashCode() : 0);
        return result;
    }
}
