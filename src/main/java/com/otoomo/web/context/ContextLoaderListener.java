package com.otoomo.web.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ioc 容器加载监听器
 * 用于初始化ioc容器
 *
 * @author modongning
 * @date 30/10/2020 1:20 PM
 */
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

    public ContextLoaderListener() {
    }

    public ContextLoaderListener(WebApplicationContext context) {
        super(context);
    }

    /**
     * 初始化入口
     *
     * @param event
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("ContextLoaderListener contextInitialized......");

        ServletContext servletContext = event.getServletContext();
        this.initWebApplicationContext(servletContext);
    }

    /**
     * 销毁
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("ContextLoaderListener contextDestroyed......");
    }
}
