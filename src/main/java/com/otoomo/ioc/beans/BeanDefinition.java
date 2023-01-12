package com.otoomo.ioc.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * bean配置的属性容器
 *
 * @author modongning
 * @date 13/10/2020 11:36 PM
 */
public class BeanDefinition {
    /**
     * bean的名称（唯一标识ID）
     */
    private String beanName;
    /**
     * bean的类型
     */
    private Class beanClass;
    /**
     * bean的全限定类名
     */
    private String beanClassName;
    /**
     * bean依赖的属性列表
     */
    private List<PropertyValue> propertyValueList = new ArrayList<>();

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public List<PropertyValue> getPropertyValueList() {
        return propertyValueList;
    }

    public void setPropertyValueList(List<PropertyValue> propertyValueList) {
        this.propertyValueList = propertyValueList;
    }
}
