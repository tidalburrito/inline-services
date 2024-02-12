package com.tb;

import java.util.List;

public interface Packager {
    void pack(String origPackage, List<String> serviceClasses);
}
