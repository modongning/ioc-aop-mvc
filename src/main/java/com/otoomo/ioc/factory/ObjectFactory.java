package com.otoomo.ioc.factory;

import com.otoomo.ioc.exception.BeanCreateException;

public interface ObjectFactory {
    Object getObject() throws BeanCreateException;
}
