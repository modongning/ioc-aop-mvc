package com.otoomo.ioc.factory;

import com.otoomo.ioc.beans.BeanDefinition;
import com.otoomo.ioc.beans.BeanReference;
import com.otoomo.ioc.beans.PropertyValue;
import com.otoomo.ioc.exception.BeanCreateException;
import com.otoomo.ioc.exception.InjectPropertyNotFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * bean工厂，真正存储bean实例的工厂类
 * bean的创建都在这个工厂类中完成
 *
 * @author modongning
 * @date 14/10/2020 9:24 AM
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableListableBeanFactory {

    /**
     * 根据bean名称获取beanDefinition
     * <p>
     * 钩子方法，该方法有具体的DefaultListableBeanFactory继承类实现。
     * 在getBean方法中会调用这个方法，实际是调用到了子类中的getBeanDefinitionByName
     *
     * @param beanName
     * @return
     */
    protected abstract BeanDefinition getBeanDefinitionByName(String beanName);

    /**
     * 获取bean
     *
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) {
        /*
        从单例池获取bean
         */
        Object bean = this.getSingleton(beanName, false);
        if (null != bean) {
            return bean;
        }
        /*
        调用钩子方法getBeanDefinitionByName(具体由子类DefaultListableBeanFactory实现)，获取beanDefinition
         */
        BeanDefinition beanDefinition = getBeanDefinitionByName(beanName);
        if (null == beanDefinition) {
            return null;
        }
        /*
        创建bean
         */
        bean = doGetBean(beanDefinition);

        return bean;
    }

    private Object doGetBean(BeanDefinition beanDefinition) {
        Object instance = getSingleton(beanDefinition.getBeanName());
        if (null == instance) {
            /**
             * getSingleton函数中获取ObjectFactory的getObject()流程：
             * 1. createBean (钩子方法，子类实现)
             * 2. doCreateBean
             */
            instance = createBean(beanDefinition);
        }

        return instance;
    }

    protected abstract Object createBean(BeanDefinition beanDefinition);
}
