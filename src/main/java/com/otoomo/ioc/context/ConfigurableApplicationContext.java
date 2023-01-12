package com.otoomo.ioc.context;

import com.otoomo.ioc.factory.BeanDefinitionRegistry;
import com.otoomo.ioc.factory.ConfigurableListableBeanFactory;
import com.otoomo.ioc.factory.DefaultListableBeanFactory;

/**
 * @author modongning
 * @date 29/10/2020 11:39 PM
 */
public interface ConfigurableApplicationContext extends ApplicationContext {
    void refresh() throws Exception;

    ConfigurableListableBeanFactory getBeanFactory();
}
