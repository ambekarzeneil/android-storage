package com.zva.android.commonLib.utils;

import android.content.Context;
import dalvik.system.DexFile;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright CoreStorage 2015 Created by zeneilambekar on 31/07/15.
 */
public class ClassLoaderUtils {

    /**
     * Finds all the set of classes located in given package names
     *
     * @param scannedPackages the packages to be scanned
     * @param applicationContext the application context
     * @return the set of classes located in the scanned packages
     * @throws IOException if searching the dex file throws any IOException
     */
    public <T> Set<Class<? extends T>> find(String[] scannedPackages, Context applicationContext) throws IOException {
        return find(scannedPackages, applicationContext, null);
    }

    /**
     * Finds the set of classes located in given package names having a particular annotation
     *
     * @param scannedPackages the packages to be scanned
     * @param applicationContext the application context
     * @param annotationClassToSearchFor the annotation class to search for
     * @return the set
     * @throws IOException if searching the dex file throws any IOException
     */
    public <T> Set<Class<? extends T>> find(String[] scannedPackages, Context applicationContext, Class<? extends Annotation> annotationClassToSearchFor) throws IOException {
        Enumeration<String> entries = new DexFile(applicationContext.getPackageCodePath()).entries();

        Set<Class<? extends T>> returnClassSet = new HashSet<>();

        while (entries.hasMoreElements()) {
            String potentialPackageClassName = entries.nextElement();

            for (String scannedPackage : scannedPackages) {
                if (potentialPackageClassName.startsWith(scannedPackage)) {
                    try {
                        @SuppressWarnings("unchecked")
                        //If there is a class cast exception, treat the class as malformed and move on
                        Class<? extends T> potentialPackageClass = (Class<? extends T>) Class.forName(potentialPackageClassName);
                        if (annotationClassToSearchFor == null || potentialPackageClass.getAnnotation(annotationClassToSearchFor) != null)
                            returnClassSet.add(potentialPackageClass);
                    } catch (ClassNotFoundException | ClassCastException ignored) {
                    }
                    break;
                }
            }
        }
        return returnClassSet;

    }

}
