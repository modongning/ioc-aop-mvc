package com.otoomo.ioc.context.annotation;

import com.otoomo.ioc.factory.BeanDefinitionRegistry;
import com.otoomo.util.ClassScanner;

import java.util.HashSet;
import java.util.Set;

/**
 * 扫描指定包路径下的所有bean配置
 *
 * @author modongning
 * @date 15/10/2020 6:31 PM
 */
public class ComponentScanBeanDefinitionParser implements AnnotationConfigRegistry {

    private BeanDefinitionRegistry registry;

    public ComponentScanBeanDefinitionParser(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void scan(String... packageNames) {
        doScan(packageNames);
    }

    /**
     * 开始扫描所有包
     *
     * @param packageNames
     */
    private void doScan(String... packageNames) {
        ClassScanner scanner = new ClassScanner();
        //获取所有包下的所有class
        Set<Class<?>> allPackageClass = new HashSet<>();
        for (String packageName : packageNames) {
            try {
                Set<Class<?>> classSet = scanner.scan(packageName);
                allPackageClass.addAll(classSet);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        //开始注册bean的信息
        DefaultAnnotationBeanDefinitionClassReader classReader = new DefaultAnnotationBeanDefinitionClassReader(registry);
        classReader.registerBeanDefinitions(allPackageClass);
    }

}
