package com.tb.util;

import org.objectweb.asm.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PackageIntrospecUtil {
    File outputDirectory;

    public PackageIntrospecUtil(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public List<String> getClassFiles() {
        List<String> classFiles = new ArrayList<>();
        findClassFiles(outputDirectory, "", classFiles);
        return classFiles;
    }

    public List<String> getInlineServiceClasses(){
       List<String> serviceClasses=new ArrayList<>();
       for (String classFile : getClassFiles()) {
           try {
               if (isInlineServiceClass(outputDirectory.getAbsolutePath(), classFile)) {
                    serviceClasses.add(classFile);
               }
           }
           catch (IOException e) {
               System.err.println("Ignoring Error reading class file: " + classFile+", "+e.getMessage());
               e.printStackTrace();
           }
       }
       return serviceClasses;
    }

    private void findClassFiles(File directory, String packagePath, List<String> classFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String subPackagePath = packagePath.isEmpty() ? file.getName() : packagePath + "." + file.getName();
                    findClassFiles(file, subPackagePath, classFiles); // Recursively process subdirectories
                } else if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = packagePath + "." + file.getName().replace(".class", "");
                    classFiles.add(className);
                }
            }
        }
    }

    private boolean isInlineServiceClass(String outputDirectory, String className) throws IOException {
        String classFilePath = outputDirectory + "/" + className.replace('.', '/') + ".class";
        ClassLoader classLoader = getClass().getClassLoader();
        ClassReader classReader = new ClassReader(new BufferedInputStream( new FileInputStream(classFilePath)));
        List<String> inlineServices=new ArrayList<>();
        classReader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                Type annotationType = Type.getType(descriptor);
                String annotationName = annotationType.getClassName();
                if("com.tb.fw.annotations.InlineService".equals(annotationName)){
                    inlineServices.add(annotationName);
                }
                return super.visitAnnotation(descriptor, visible);
            }
        }, 0);
        return (inlineServices.size()>0);
    }
}
