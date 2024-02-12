package com.tb.local;

import com.tb.Packager;
import com.tb.util.ZipUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SimpleLocalPackager implements Packager {
    @Override
    public void pack(String origPackage, List<String> serviceClasses){
        Map<String, Map<String,String>> configurations=new HashMap<>();
        Properties serviceProperties=new Properties();

        int port=8080;
        Map<String, String> config=new HashMap<>();
        config.put("server.port", ""+port);
        config.put("app.serviceclass", "Main");
        configurations.put("Main", config);
        port++;

        for (String classFile : serviceClasses) {
            config=new HashMap<>();
            config.put("server.port", ""+port);
            config.put("app.serviceclass", classFile);
            configurations.put(classFile, config);
            serviceProperties.put(classFile.toLowerCase(), "http://localhost:"+port);
            port++;
        }

        try {
            File tempFile = File.createTempFile("service", ".properties");
            serviceProperties.store(new FileOutputStream(tempFile), "Created for Inline Services");
            ZipUtil.appendToZip(origPackage, tempFile.getAbsolutePath(), "BOOT-INF/classes/services.properties");
            tempFile.delete();

            String artifactPrefix=origPackage.substring(0, origPackage.lastIndexOf('.'));

            for (String className :configurations.keySet()){
                FileUtils.copyFile(new File(origPackage), new File(artifactPrefix + ".inline."+className+".jar"));

                tempFile = File.createTempFile("server", ".properties");
                Properties serverProps=new Properties();
                serverProps.putAll(configurations.get(className));
                serverProps.store(new FileOutputStream(tempFile), "Created for Inline Services");
                ZipUtil.appendToZip(artifactPrefix + ".inline."+className+".jar", tempFile.getAbsolutePath(), "BOOT-INF/classes/server.properties");
                tempFile.delete();

                //ZipUpdate.updateFileInZip(artifactPrefix + "."+className+".jar", "BOOT-INF/classes/application.properties", "C:\\Work\\research\\micro\\microuser\\src\\main\\java\\com\\tb\\MicroApplication.java");
                System.out.println("Created package:"+artifactPrefix + ".inline."+className+".jar");
            }
        }
        catch (Exception ex){
            System.err.println("Error creating packages :"+ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}
