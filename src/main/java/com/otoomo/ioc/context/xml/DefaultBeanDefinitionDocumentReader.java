package com.otoomo.ioc.context.xml;

import com.otoomo.ioc.beans.BeanDefinition;
import com.otoomo.ioc.beans.BeanReference;
import com.otoomo.ioc.beans.PropertyValue;
import com.otoomo.ioc.context.annotation.AnnotationConfigApplicationContext;
import com.otoomo.ioc.context.annotation.AnnotationConfigRegistry;
import com.otoomo.ioc.context.annotation.ComponentScanBeanDefinitionParser;
import com.otoomo.ioc.exception.BeanDefinitionAttrException;
import com.otoomo.ioc.factory.BeanDefinitionRegistry;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 实现xml的Document解析
 * <p>
 * 解析完成会注册到registry中
 *
 * @author modongning
 * @date 14/10/2020 4:22 PM
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

    private BeanDefinitionRegistry registry;

    public DefaultBeanDefinitionDocumentReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void registerBeanDefinitions(Document document) {
        //<beans>
        Element rootElement = document.getRootElement();
        /*
        开始注册BeanDefinition
         */
        doRegisterBeanDefinitions(rootElement);
    }

    /**
     * 开始注册BeanDefinition
     *
     * @param rootElement
     */
    private void doRegisterBeanDefinitions(Element rootElement) {
        /*
        获取所有bean配置
         */
        List<Element> beanElements = rootElement.selectNodes("//bean");
        for (Element beanElement : beanElements) {
            parseBeanDefinitionElement(beanElement);
        }

        //获取扫描包路径
        List<Element> componentScanElements = rootElement.selectNodes("//component-scan");
        parseComponentScanElements(componentScanElements);
    }

    /**
     * 解析扫描包配置的元素
     *
     * @param componentScanElements
     */
    private void parseComponentScanElements(List<Element> componentScanElements) {
        List<String> scanPackages = new ArrayList<>();
        for (Element componentScanElement : componentScanElements) {
            String packagePath = componentScanElement.attributeValue("base-package");
            if (null == packagePath || packagePath.length() == 0) {
                continue;
            }
            scanPackages.add(packagePath);
        }
        //开始根据包路径配置解析BeanDefinition配置
        loadComponentScanBeanDefinition(scanPackages);
    }

    /**
     * 根据包路径配置解析BeanDefinition配置
     *
     * @param scanPackages
     */
    private void loadComponentScanBeanDefinition(List<String> scanPackages) {
        AnnotationConfigRegistry scanner = new ComponentScanBeanDefinitionParser(registry);
        String[] packageNames = new String[scanPackages.size()];
        for (int i = 0; i < scanPackages.size(); i++) {
            packageNames[i] = scanPackages.get(i);
        }
        scanner.scan(packageNames);
    }

    /**
     * 解析bean配置
     *
     * @param beanElement
     */
    private void parseBeanDefinitionElement(Element beanElement) {
        String beanName = beanElement.attributeValue("id");
        if (null == beanName || beanName.length() == 0) {
            throw new BeanDefinitionAttrException("element <bean> missed 'id' attribute");
        }

        String beanClassName = beanElement.attributeValue("class");
        if (null == beanClassName || beanClassName.length() == 0) {
            throw new BeanDefinitionAttrException("element <bean> missed 'class' attribute");
        }

        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);

        //解析bean的属性配置
        parsePropertyElements(beanElement, beanDefinition);

        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 解析bean的所有属性配置
     *
     * @param beanElement
     * @param beanDefinition
     */
    private void parsePropertyElements(Element beanElement, BeanDefinition beanDefinition) {
        List<Element> propertyElements = beanElement.selectNodes("property");
        for (Element propertyElement : propertyElements) {
            parsePropertyElement(propertyElement, beanDefinition);
        }
    }

    /**
     * 解析bean属性配置
     *
     * @param propertyElement
     * @param beanDefinition
     */
    private void parsePropertyElement(Element propertyElement, BeanDefinition beanDefinition) {
        String name = propertyElement.attributeValue("name");
        String value = propertyElement.attributeValue("value");
        String ref = propertyElement.attributeValue("ref");

        Object valueObj;
        if (null != value) {
            valueObj = value;
        } else if (null != ref) {
            //如果是引用类型，则使用BeanReference存储引用的属性值
            valueObj = new BeanReference(ref);
        } else {
            throw new IllegalArgumentException("element <property> mush set a value or ref");
        }

        //添加到beanDefinition的属性列表中
        beanDefinition.getPropertyValueList().add(new PropertyValue(name, valueObj));
    }
}
