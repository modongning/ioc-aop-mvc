package com.otoomo.ioc.context.xml;

import org.dom4j.Document;

public interface BeanDefinitionDocumentReader {
    void registerBeanDefinitions(Document document);
}
