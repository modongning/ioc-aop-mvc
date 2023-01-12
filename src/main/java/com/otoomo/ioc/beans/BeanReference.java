package com.otoomo.ioc.beans;

public class BeanReference {
    private String value;

    public BeanReference(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
