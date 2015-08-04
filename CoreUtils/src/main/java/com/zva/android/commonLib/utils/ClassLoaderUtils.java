package com.zva.android.commonLib.utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import android.content.Context;
import dalvik.system.DexFile;

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
    public static <T> Set<Class<? extends T>> find(String[] scannedPackages, Context applicationContext) throws IOException {
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
    public static <T> Set<Class<? extends T>> find(String[] scannedPackages, Context applicationContext, Class<? extends Annotation> annotationClassToSearchFor) throws IOException {
        Enumeration<String> entries = new DexFile(applicationContext.getPackageCodePath()).entries();

        Set<Class<? extends T>> returnClassSet = new HashSet<>();

        while (entries.hasMoreElements()) {
            String potentialPackageClassName = entries.nextElement();

            if (scannedPackages.length == 0) {
                Class<? extends T> annotatedClass = checkForAnnotation(potentialPackageClassName, annotationClassToSearchFor);
                if (annotatedClass != null)
                    returnClassSet.add(annotatedClass);

                continue;

            }

            for (String scannedPackage : scannedPackages) {
                if (potentialPackageClassName.startsWith(scannedPackage)) {
                    Class<? extends T> annotatedClass = checkForAnnotation(potentialPackageClassName, annotationClassToSearchFor);
                    if (annotatedClass != null) {
                        returnClassSet.add(annotatedClass);
                        break;
                    }
                }
            }
        }

        return returnClassSet;

    }

    public static <T> Set<Class<? extends T>> find(Context applicationContext, Class<? extends Annotation> annotationClassToSearchFor) throws IOException {
        return find(new String[] {applicationContext.getPackageName()}, applicationContext, annotationClassToSearchFor);
    }

    @Nullable
    private static  <T> Class<? extends T> checkForAnnotation(String potentialPackageClassName, Class<? extends Annotation> annotationClassToSearchFor) {
        try {
            @SuppressWarnings("unchecked")
            //If there is a class cast exception, treat the class as malformed and move on
            Class<? extends T> potentialPackageClass = (Class<? extends T>) Class.forName(potentialPackageClassName);
            if (annotationClassToSearchFor == null || potentialPackageClass.getAnnotation(annotationClassToSearchFor) != null)
                return potentialPackageClass;
        } catch (ClassNotFoundException | ClassCastException | NoClassDefFoundError | ExceptionInInitializerError ignored) {
        }

        return null;

    }

}
