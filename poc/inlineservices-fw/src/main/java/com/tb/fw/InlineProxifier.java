package com.tb.fw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.proxy.Enhancer;

import java.util.Map;

public class InlineProxifier {

    @Value("${app.serviceclass}")
    String serviceClass;
    @Autowired
    @Qualifier("InlineClientCache")
    InlineClientCache inlineClientCache;

    public <T> T getProxied(Class<T> tClass){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(tClass);
        System.out.println("inclientclientcach in proxifier="+inlineClientCache +"[hash="+inlineClientCache.hashCode()+"]");
        InlineInterceptor inlineInterceptor=new InlineInterceptor(serviceClass, inlineClientCache);
        enhancer.setCallback(inlineInterceptor);
        T proxy = (T) enhancer.create();
        return proxy;
    }
}
