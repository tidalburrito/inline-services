package com.tb;

import com.tb.util.PackageIntrospecUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@Mojo(name = "touch", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class InlineServicesMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}", readonly = true)
    private File artifact;

    @Parameter(property = "packager", defaultValue = "local")
    private String packager;


    public void execute()
            throws MojoExecutionException {
        getLog().info("Inline Services Packaging...");
        File outputDir=new File(project.getBuild().getOutputDirectory());
        PackageIntrospecUtil packageIntrospecUtil=new PackageIntrospecUtil(outputDir);
        List<String> inlineClasses=packageIntrospecUtil.getInlineServiceClasses();
        Packager packagerObj= PackagerFactory.getInstance().getPackager(packager);
        packagerObj.pack(artifact.getAbsolutePath(), inlineClasses);
        getLog().info("Inline Services Packaging Done.");

        try {
            getLog().info("Moving original file "+artifact.getAbsolutePath()+".original");
            Files.move(
                    Paths.get(artifact.getAbsolutePath()+".original"),
                    Paths.get(artifact.getAbsolutePath()),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
        catch (Exception ex){
            getLog().error("Error Restoring orignial package :"+ex.getMessage(), ex);
        }
    }

}
