package com.otoomo.ioc.resource.impl;

import com.otoomo.ioc.resource.Resource;

import java.io.InputStream;

/**
 * 类路径资源获取
 *
 * @author modongning
 * @date 13/10/2020 11:54 PM
 */
public class ClasspathResource implements Resource {

    private String path;
    private ClassLoader classLoader;

    public ClasspathResource(String path) {
        this(path, null);
    }

    public ClasspathResource(String path, ClassLoader classLoader) {
        this.path = path;
        //如果没有传入类加载器，则使用当前线程正在执行的类加载器
        this.classLoader = null == classLoader ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    @Override
    public InputStream getInputStream() {
        return classLoader.getResourceAsStream(path);
    }
}
