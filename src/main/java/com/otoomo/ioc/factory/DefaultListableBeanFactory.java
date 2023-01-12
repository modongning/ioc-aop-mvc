package com.otoomo.ioc.factory;

import com.otoomo.ioc.beans.BeanDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的beanDefinition工厂
 *
 * <p>该工厂实现了beanDefinition的注册和实例化（调用AbstractBeanFactory#getBean()）
 *
 * @author modongning
 * @date 14/10/2020 8:56 AM
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry {
    /**
     * 存储所有bean配置信息
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    /**
     * 存储所有bean的名称
     */
    private List<String> beanDefinitionNames = new ArrayList<>();

    public List<String> getBeanDefinitionNames() {
        return beanDefinitionNames;
    }

    /**
     * 调用getBean方法，该方法是实例化所有定义好的bean的重要方法
     */
    @Override
    public void preInstantiateSingletons() {
        for (String beanName : beanDefinitionNames) {
            //TODO 判断懒加载
            //具体实现在AbstractBeanFactory中
            getBean(beanName);
        }
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
        beanDefinitionNames.add(beanName);
    }

    /**
     * 该方法有AbstractBeanFactory中定义，是个抽象方法。作为钩子使用
     * 根据名称从beanDefinition容器中获取BeanDefinition
     *
     * @param beanName
     * @return
     */
    @Override
    protected BeanDefinition getBeanDefinitionByName(String beanName) {
        return beanDefinitionMap.get(beanName);
    }
}
