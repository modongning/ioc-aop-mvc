package com.otoomo.web.context;

import com.otoomo.ioc.context.AbstractApplicationContext;
import com.otoomo.ioc.context.xml.XmlBeanDefinitionReader;
import com.otoomo.ioc.factory.BeanDefinitionRegistry;

import javax.servlet.ServletContext;

/**
 * @author modongning
 * @date 30/10/2020 8:59 AM
 */
public class XmlWebApplicationContext extends AbstractApplicationContext implements WebApplicationContext {
    private ServletContext servletContext;
    private String configLocation;

    public XmlWebApplicationContext(ServletContext servletContext, String configLocation) throws Exception {
        this.servletContext = servletContext;
        this.configLocation = configLocation;
    }

    @Override
    protected void loadDefinitions(BeanDefinitionRegistry registry) throws Exception {
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(registry);
        xmlBeanDefinitionReader.loadBeanDefinitions(configLocation);
    }

    @Override
    public void setConfigLocation(String configPath) {
        this.configLocation = configPath;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }
}
