package com.otoomo.ioc.factory;

public interface SingletonBeanRegistry {
    void registerSingleton(String beanName, Object bean);

    Object getSingleton(String beanName);

    /**
     * 获取bean
     *
     * @param beanName
     * @param early    是否允许获取早期未完全准备好的bean
     * @return
     */
    Object getSingleton(String beanName, boolean early);
}
