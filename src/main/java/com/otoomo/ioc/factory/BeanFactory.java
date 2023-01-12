package com.otoomo.ioc.factory;

/**
 * Bean工厂顶级接口
 *
 * @author modongning
 * @date 13/10/2020 11:26 PM
 */
public interface BeanFactory {
    /**
     * 根据名称获取bean
     *
     * @param beanName
     * @return
     */
    <T> T getBean(String beanName);
}
