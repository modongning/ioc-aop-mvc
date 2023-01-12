package com.otoomo.ioc.context.annotation;

import java.util.Set;

public interface BeanDefinitionClassReader {
    void registerBeanDefinitions(Set<Class<?>> classes);
}
