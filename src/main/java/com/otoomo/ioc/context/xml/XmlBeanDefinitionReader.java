package com.otoomo.ioc.context.xml;

import com.otoomo.ioc.factory.BeanDefinitionRegistry;
import com.otoomo.ioc.resource.Resource;
import com.otoomo.ioc.resource.impl.ClasspathResource;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * 读取xml的bean配置
 *
 * @author modongning
 * @date 14/10/2020 8:30 AM
 */
public class XmlBeanDefinitionReader implements BeanDefinitionReader {

    private BeanDefinitionRegistry registry;

    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void loadBeanDefinitions(String path) throws Exception {
        Resource resource = new ClasspathResource(path);
        InputStream inputStream = resource.getInputStream();

        doLoadBeanDefinitions(inputStream);
    }

    private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
        Document document = new SAXReader().read(inputStream);
        BeanDefinitionDocumentReader documentReader = new DefaultBeanDefinitionDocumentReader(registry);
        documentReader.registerBeanDefinitions(document);
    }
}
