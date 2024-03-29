package com.bulingbuu;

import java.io.File;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 检测classPath下的类冲突，通常用于web应用的lib目录下jar包中class冲突检测，
 * 避免程序运行过程中出现如下的类似异常:
 * {@link NoSuchMethodError}、{@link NoSuchMethodException}
 * {@link ClassNotFoundException}、{@link NoClassDefFoundError}
 */
public class ClassConflictCheck {
    private static Map<String, HashSet<String>> classMap = new HashMap<String, HashSet<String>>();

    public static void check(String classPath) throws Exception {
        File dir = new File(classPath);
        File[] jarFiles = dir.listFiles();
        for (File jarFile : jarFiles) {
            if (jarFile.getName().endsWith(".jar")) {
                JarFile jar = new JarFile(jarFile);
                Enumeration<JarEntry> enumeration = jar.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = enumeration.nextElement();
                    if (jarEntry.getName().endsWith(".class")) {
                        storeClassAndJarRel(jarEntry.getName(), jar.getName());
                    }
                }
            }
        }
    }

    private static void storeClassAndJarRel(String className, String jarName) {
        HashSet<String> jarSet = classMap.get(className);
        if (jarSet == null) {
            jarSet = new HashSet<String>();
        }
        jarSet.add(jarName.substring(jarName.lastIndexOf("/") + 1));
        classMap.put(className, jarSet);
    }

    public static void main(String[] args) throws Exception {

        for (String arg : args) {
            check(arg);
        }

        boolean isConflict = false;
        for (Iterator<Map.Entry<String, HashSet<String>>> iterator = classMap.entrySet().iterator(); iterator
                .hasNext(); ) {
            Map.Entry<String, HashSet<String>> entry = iterator.next();
            HashSet<String> jarSet = entry.getValue();
            if (jarSet.size() > 1) {
                if (!isConflict) {
                    isConflict = true;
                }
                List<String> jarList = Arrays.asList(jarSet.toArray(new String[]{}));
                System.out.println("class :" + entry.getKey() + " ->jars : " + jarList);
            }
        }

        if (!isConflict) {
            System.out.println("there are no class conflict in the jars");
        }
    }

}