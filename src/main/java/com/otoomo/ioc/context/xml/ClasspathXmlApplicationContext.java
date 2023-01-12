package com.otoomo.ioc.context.xml;

import com.otoomo.ioc.context.AbstractApplicationContext;
import com.otoomo.ioc.factory.BeanDefinitionRegistry;
import com.otoomo.ioc.context.xml.XmlBeanDefinitionReader;

/**
 * xml方式实现
 *
 * @author modongning
 * @date 13/10/2020 4:53 PM
 */
public class ClasspathXmlApplicationContext extends AbstractApplicationContext {

    private String configLocation;

    public ClasspathXmlApplicationContext(String configLocation) throws Exception {
        this.configLocation = configLocation;
        /*
        父类执行刷新操作

         */
        this.refresh();
    }

    /**
     * 钩子方法。
     * 实现父类的抽象方法，在父类中会调用loadDefinitions，会调用到子类的具体实现
     *
     * @author modongning
     * @date 14/10/2020 5:39 PM
     */
    @Override
    protected void loadDefinitions(BeanDefinitionRegistry registry) throws Exception {
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(registry);
        xmlBeanDefinitionReader.loadBeanDefinitions(configLocation);
    }
}
