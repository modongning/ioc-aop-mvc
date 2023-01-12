package com.otoomo.ioc.context.xml;

/**
 * bean定义读取器
 *
 * @author modongning
 * @date 14/10/2020 12:03 AM
 */
public interface BeanDefinitionReader {
    /**
     * 从配置路径中加载bean定义信息
     *
     * @param path
     */
    void loadBeanDefinitions(String path) throws Exception;
}
