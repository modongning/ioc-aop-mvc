package com.otoomo.ioc.context.annotation;

import com.otoomo.ioc.context.AbstractApplicationContext;
import com.otoomo.ioc.factory.BeanDefinitionRegistry;

public class AnnotationConfigApplicationContext extends AbstractApplicationContext {

    private String[] packageNames;

    public AnnotationConfigApplicationContext(String... packageNames) throws Exception {
        this.packageNames = packageNames;

        this.refresh();
    }

    /**
     * refresh执行过程中，父类调用
     *
     * @param registry
     * @throws Exception
     */
    @Override
    protected void loadDefinitions(BeanDefinitionRegistry registry) throws Exception {
        AnnotationConfigRegistry scanner = new ComponentScanBeanDefinitionParser(registry);
        scanner.scan(packageNames);
    }
}
