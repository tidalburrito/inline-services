package com.tb;

import com.tb.local.SimpleLocalPackager;

public class PackagerFactory {

    private static PackagerFactory packagerFactory=new PackagerFactory();
    private PackagerFactory(){}

    public static PackagerFactory getInstance(){
        return packagerFactory;
    }
    public Packager getPackager(String packager){
        System.out.println("Packager="+packager);

        //TODO: Hardcoded packager, add logic to create packager based on "packager" parameter
        return new SimpleLocalPackager();
    }
}
