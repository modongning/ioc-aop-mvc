package com.otoomo.ioc.beans;

/**
 * bean依赖的属性容器
 *
 * @author modongning
 * @date 13/10/2020 11:41 PM
 */
public class PropertyValue {
    /**
     * bean的名称
     */
    private String name;
    /**
     * bean依赖的属性值
     * 有可能是赋予普通值（value），有可能是引用对象(ref)
     */
    private Object value;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
