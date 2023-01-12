package com.otoomo.ioc.factory;

import com.otoomo.aop.proxy.ProxyFactory;
import com.otoomo.ioc.exception.BeanCreateException;
import com.otoomo.ioc.exception.BeanRepeatRegisteredException;
import com.otoomo.jdbc.annotation.Transactional;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认单例池工厂类
 *
 * @author modongning
 * @date 14/10/2020 10:32 AM
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    /**
     * 这里通过三级缓存，处理循环依赖的问题
     */
    //一级缓存，单例池。存储最终已经完成创建的单例bean
    protected Map<String, Object> singleObjects = new ConcurrentHashMap<>();
    //二级缓存, 用来存储没有成为bean的实例，是从三级缓存中获取到的。
    private Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    //三级缓存，用来存储刚实例化之后，没有成为bean的实例
    private Map<String, ObjectFactory> singletonObjectFactorys = new ConcurrentHashMap<>();

    /**
     * 注册单例bean到单例池
     */
    @Override
    public void registerSingleton(String beanName, Object bean) {
        Object singletonBean = this.singleObjects.get(beanName);

        //判断是否重复注册相同名称的bean
        if (null != singletonBean) {
            throw new BeanRepeatRegisteredException("bean <" + beanName + "> repeat register : " + bean.getClass().getName());
        }

        this.singleObjects.put(beanName, bean);
        this.earlySingletonObjects.remove(beanName);
        this.singletonObjectFactorys.remove(beanName);
    }

    /**
     * 获取bean
     * 处理了循环依赖问题
     *
     * @param beanName
     * @return
     */
    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    @Override
    public Object getSingleton(String beanName, boolean early) {
        //从一级缓存获取
        Object singletonBean = this.singleObjects.get(beanName);
        if (null == singletonBean && (isSingletonCurrentlyInCreation(beanName) || this.earlySingletonObjects.containsKey(beanName)) && early) {
            //从二级缓存获取
            singletonBean = this.earlySingletonObjects.get(beanName);
            if (null == singletonBean) {
                //从三级缓存获取
                ObjectFactory objectFactory = this.singletonObjectFactorys.get(beanName);

                singletonBean = objectFactory.getObject();

                //从三级缓存移除
                this.singletonObjectFactorys.remove(beanName);
                //添加到二级缓存中
                this.earlySingletonObjects.put(beanName, singletonBean);
            }
        }
        return singletonBean;
    }

    /**
     * 添加正在处理bean
     *
     * @param beanName
     * @param objectFactory
     */
    protected void addSingletonFactory(String beanName, ObjectFactory objectFactory) {
        if (!this.singleObjects.containsKey(beanName)) {
            this.singletonObjectFactorys.put(beanName, objectFactory);
            this.earlySingletonObjects.remove(beanName);
        }
    }

    protected boolean isSingletonCurrentlyInCreation(String beanName) {
        return singletonObjectFactorys.containsKey(beanName);
    }
}
