package com.otoomo.ioc.factory;

import com.otoomo.ioc.beans.BeanDefinition;

/**
 * beanDefinition注册接口
 *
 * @author modongning
 * @date 14/10/2020 8:52 AM
 */
public interface BeanDefinitionRegistry {
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
}
