package com.otoomo.ioc.factory;

import com.otoomo.aop.proxy.ProxyFactory;
import com.otoomo.ioc.beans.BeanDefinition;
import com.otoomo.ioc.beans.BeanReference;
import com.otoomo.ioc.beans.PropertyValue;
import com.otoomo.ioc.exception.BeanCreateException;
import com.otoomo.ioc.exception.InjectPropertyNotFoundException;
import com.otoomo.jdbc.annotation.Transactional;
import com.otoomo.jdbc.transaction.DataSourceTransactionManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {
    @Override
    protected Object createBean(BeanDefinition beanDefinition) {
        //这里获取到的是完整的bean
        Object bean = doCreateBean(beanDefinition);
        //如果最后对象还存在三级缓存，说明没有循环依赖情况，没有调用到getEarlyBeanReference()处理AOP的逻辑
        bean = initAOP(bean);
        this.registerSingleton(beanDefinition.getBeanName(), bean);
        return bean;
    }

    /**
     * 根据beanDefinition创建bean
     *
     * @param beanDefinition
     * @return
     */
    protected Object doCreateBean(BeanDefinition beanDefinition) throws BeanCreateException {
        /*
        根据beanDefinition创建实例
         */
        Object bean = createBeanInstance(beanDefinition);

        //循环依赖关键处理
        this.addSingletonFactory(beanDefinition.getBeanName(), new ObjectFactory() {
            @Override
            public Object getObject() throws BeanCreateException {
                /*
                 * 获取原始对象的早期引用，在 getEarlyBeanReference 方法中，会执行 AOP
                 * 相关逻辑。若 bean 未被 AOP 拦截，getEarlyBeanReference 原样返回
                 * bean，所以大家可以把
                 *      return getEarlyBeanReference(beanName, mbd, bean)
                 * 等价于：
                 *      return bean;
                 */
                //TODO AOP操作
                return bean;
            }
        });

        populateBean(bean, beanDefinition);

        return bean;
    }

    protected Object createBeanInstance(BeanDefinition beanDefinition) throws BeanCreateException {
        /*
        通过反射实例化
         */
        Class beanClass = beanDefinition.getBeanClass();
        String beanClassName = beanDefinition.getBeanClassName();
        try {
            if (null == beanClass && null != beanClassName && beanClassName.length() > 0) {
                beanClass = Class.forName(beanClassName);
                beanDefinition.setBeanClass(beanClass);
            }
            if (null == beanClass) {
                throw new BeanCreateException("bean class < " + beanClassName + " > not found");
            }
            return beanClass.newInstance();
        } catch (Exception e) {
            throw new BeanCreateException("create bean < " + beanClassName + " > failed", e);
        }
    }

    protected Object initAOP(Object bean) {
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(Transactional.class)) {
                continue;
            }
            Transactional annotation = method.getAnnotation(Transactional.class);
            if (null != annotation) {
                DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(bean);
                bean = ProxyFactory.createCglibProxy(bean, transactionManager);
                break;
            }
        }
        return bean;
    }

    void populateBean(Object bean, BeanDefinition beanDefinition) {
        populatePropertyValues(bean, beanDefinition);
    }

    /**
     * 依赖属性注入
     *
     * @param bean
     * @param beanDefinition
     */
    protected void populatePropertyValues(Object bean, BeanDefinition beanDefinition) {
        /*
        遍历所有属性
         */
        List<PropertyValue> propertyValues = beanDefinition.getPropertyValueList();
        for (PropertyValue propertyValue : propertyValues) {
            populatePropertyValue(bean, propertyValue);
        }

    }

    /**
     * 依赖注入
     *
     * @param bean
     * @param propertyValue
     */
    private void populatePropertyValue(Object bean, PropertyValue propertyValue) {
        String name = propertyValue.getName();
        Object value = propertyValue.getValue();
        //如果值是应用其他的bean，则从bean工厂中获取
        if (value instanceof BeanReference) {
            BeanReference beanReference = (BeanReference) value;

            //依赖的bean名称
            String refBeanName = beanReference.getValue();

            //循环依赖关键处理点，从缓存中获取bean,可能是完整的bean,也可能是早期的bean
            value = getSingleton(refBeanName);
            if (null == value) {
                //如果没有，则创建bean
                value = getBean(refBeanName);
            }
            if (null == value) {
                throw new InjectPropertyNotFoundException("inject bean < " + name + " > not instant");
            }
        }
        /*
        通过反射设置属性值
        */
        Class<?> beanClass = bean.getClass();
        try {
            /*
            先使用set方法设置。如果没有set，则使用反射的方式设置字段属性值
             */
            String setMethod = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Method method = beanClass.getDeclaredMethod(setMethod, beanClass);

            method.setAccessible(true);
            method.invoke(bean, value);
        } catch (Exception ex) {
            try {
                Field field = beanClass.getDeclaredField(name);
                field.setAccessible(true);
                field.set(bean, value);
            } catch (Exception e) {
                throw new InjectPropertyNotFoundException("inject property < " + name + " > failed", e);
            }
        }
    }

}
