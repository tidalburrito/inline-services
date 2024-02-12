package com.tb.fw;

import com.tb.fw.annotations.InlineApi;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class InlineInterceptor implements MethodInterceptor {

    String serviceClass;

    InlineClientCache inlineClientCache;

    public InlineInterceptor(String serviceClass, InlineClientCache inlineClientCache) {
        this.serviceClass=serviceClass;
        this.inlineClientCache=inlineClientCache;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (method.isAnnotationPresent(InlineApi.class) && (!method.getDeclaringClass().getName().equals(serviceClass))) {
            Object target=inlineClientCache.getClient(method.getDeclaringClass().getName());
            return proxy.invoke(target, args);
        } else {
            return proxy.invokeSuper(obj, args);
        }
    }
}

