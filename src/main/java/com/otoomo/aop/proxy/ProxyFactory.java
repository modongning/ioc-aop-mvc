package com.otoomo.aop.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 代理工厂类.
 *
 * @author modongning
 * @date 20/10/2020 10:49 AM
 */
public class ProxyFactory {
    /**
     * 创建cglib代理对象
     *
     * @author modongning
     * @date 20/10/2020 11:02 AM
     */
    public static <T> T createCglibProxy(Object object) {
        return (T) createCglibProxy(object, new MethodInterceptor() {
            @Override
            public Object intercept(Object targetObject, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                return methodProxy.invokeSuper(object, objects);
            }
        });
    }

    public static <T> T createCglibProxy(Object object, MethodInterceptor interceptor) {
        return (T) Enhancer.create(object.getClass(), interceptor);
    }

    public static <T> T createJDKProxy(Object obj) {
        return (T) createJDKProxy(obj, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(obj, args);
            }
        });
    }

    public static <T> T createJDKProxy(Object obj, InvocationHandler invocationHandler) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(obj, args);
            }
        });
    }
}
