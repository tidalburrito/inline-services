package com.tb.fw;

import java.util.HashMap;
import java.util.Map;

public class InlineClientCache {
    Map<String, Object> cache=new HashMap<>();
    public void put(String key, Object client){
        cache.put(key, client);
    }
    public Object getClient(String key){
        return cache.get(key);
    }

    @Override
    public String toString() {
        return "InlineClientCache{" +
                "cache=" + cache +
                '}';
    }
}
