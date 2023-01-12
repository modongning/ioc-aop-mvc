package com.otoomo.ioc.factory;

/**
 * 可配置的Bean工厂
 *
 * @author modongning
 * @date 13/10/2020 11:30 PM
 */
public interface ConfigurableListableBeanFactory extends BeanFactory, SingletonBeanRegistry {
    /**
     * 用于后续实现类处理未实例化bean
     */
    void preInstantiateSingletons();
}
