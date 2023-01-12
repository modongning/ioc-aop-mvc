package com.otoomo.ioc.context;

import com.otoomo.ioc.factory.BeanDefinitionRegistry;
import com.otoomo.ioc.factory.ConfigurableListableBeanFactory;
import com.otoomo.ioc.factory.DefaultListableBeanFactory;

/**
 * 抽象应用上下文
 *
 * <p>getBean():
 * 从工厂中获取bean，这个方法是由BeanFactory接口定义的，由ApplicationContext继承而来
 * <p>refresh():
 * 实例化默认bean工厂，并加载beanDefinition到容器
 * <p>onRefresh():
 * refresh之后，使用工厂的preInstantiateSingletons方法，实例化所有已经准备好的bean
 * <p>loadBeanDefinitions():
 * 抽象方法，作为钩子使用，具体实现类实现这个方法
 *
 * @author modongning
 * @date 14/10/2020 8:33 AM
 */
public abstract class AbstractApplicationContext implements ConfigurableApplicationContext {
    private ConfigurableListableBeanFactory beanFactory;

    private DefaultListableBeanFactory defaultListableBeanFactory;

    /**
     * 作为钩子使用，由子类实现
     * 这里的registry参数会在 refresh 函数中调用，传入到子类中
     *
     * @param registry
     * @throws Exception
     */
    protected abstract void loadDefinitions(BeanDefinitionRegistry registry) throws Exception;

    @Override
    public <T> T getBean(String beanName) {
        return (T) beanFactory.getSingleton(beanName, false);
    }

    @Override
    public void refresh() throws Exception {
        //初始化默认工厂
        if (null == this.defaultListableBeanFactory) {
            defaultListableBeanFactory = new DefaultListableBeanFactory();
        }
        /*
        调用钩子方法，loadDefinitions加载所有bean配置信息,由子类实现
         */
        loadDefinitions(defaultListableBeanFactory);

        this.beanFactory = defaultListableBeanFactory;
        //调用doRefresh实例化所有定义好的bean
        doRefresh();
    }

    public void doRefresh() {
        beanFactory.preInstantiateSingletons();
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }
}
