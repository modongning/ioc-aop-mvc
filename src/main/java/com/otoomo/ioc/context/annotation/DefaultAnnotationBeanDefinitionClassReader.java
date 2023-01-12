package com.otoomo.ioc.context.annotation;

import com.otoomo.ioc.annotation.Autowired;
import com.otoomo.ioc.annotation.Service;
import com.otoomo.ioc.beans.BeanDefinition;
import com.otoomo.ioc.beans.BeanReference;
import com.otoomo.ioc.beans.PropertyValue;
import com.otoomo.ioc.factory.BeanDefinitionRegistry;
import com.otoomo.jdbc.annotation.Transactional;
import com.otoomo.web.annotation.Controller;
import com.otoomo.web.annotation.Interceptor;
import com.otoomo.web.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 根据类集合注册bean配置
 *
 * @author modongning
 * @date 15/10/2020 6:32 PM
 */
public class DefaultAnnotationBeanDefinitionClassReader implements BeanDefinitionClassReader {

    private BeanDefinitionRegistry registry;

    public DefaultAnnotationBeanDefinitionClassReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void registerBeanDefinitions(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            doRegisterBeanDefinition(clazz);
        }
    }

    /**
     * 开始注册beanDefinition
     * 读取bean注解@Service
     * 读取属性注入注解@
     *
     * @param clazz
     */
    private void doRegisterBeanDefinition(Class<?> clazz) {
        String beanName;
        if (clazz.isAnnotationPresent(Service.class)) {
            Service service = clazz.getAnnotation(Service.class);
            beanName = service.value();
            if (null == beanName || beanName.length() == 0) {
                String clazzSimpleName = clazz.getSimpleName();

                //这里使用父类的名称作为beanName
                Class<?>[] interfaces = clazz.getInterfaces();
                if (interfaces.length > 0) {
                    clazzSimpleName = interfaces[0].getSimpleName();
                }

                beanName = Character.toLowerCase(clazzSimpleName.charAt(0)) + clazzSimpleName.substring(1);
            }
        } else if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Interceptor.class)) {
            String clazzSimpleName = clazz.getSimpleName();
            beanName = Character.toLowerCase(clazzSimpleName.charAt(0)) + clazzSimpleName.substring(1);
        } else {
            return;
        }

        //实例化beanDefinition
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClassName(clazz.getName());
        beanDefinition.setBeanName(beanName);
        beanDefinition.setBeanClass(clazz);

        //设置beanDefinition的依赖属性
        parseBeanDefinitionFields(clazz, beanDefinition);
        //TODO 处理方法上的注解
        parseBeanDefinitionMethods(clazz, beanDefinition);

        //注册bean
        this.registry.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 解析类的所有属性，检查是否有依赖的注解
     * 如果有配置依赖注解，则添加到beanDefinition的属性列表中
     *
     * @param clazz
     * @param beanDefinition
     */
    private void parseBeanDefinitionFields(Class<?> clazz, BeanDefinition beanDefinition) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            if (null == autowired) {
                continue;
            }
            //属性名称
            String name = field.getName();

            //获取依赖的bean名称
            String value = autowired.value();
            if (null == value || value.length() == 0) {
                //如果没有设置名称，则使用属性名
                value = name;
            }

            //添加属性到列表
            beanDefinition.getPropertyValueList().add(new PropertyValue(name, new BeanReference(value)));
        }

    }

    private void parseBeanDefinitionMethods(Class<?> clazz, BeanDefinition beanDefinition) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        //TODO 处理方法上的注解
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchFieldException {
        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass("com.otoomo.ioc.context.AbstractApplicationContext");
        Field[] fields = aClass.getFields();
        for (Field field : fields) {
            System.out.printf(field.getName());
        }
    }
}
