package com.otoomo.web.context;

import com.otoomo.ioc.factory.ConfigurableListableBeanFactory;

import javax.servlet.ServletContext;

/**
 * 上下文加载器
 *
 * @author modongning
 * @date 30/10/2020 11:00 AM
 */
public class ContextLoader {
    WebApplicationContext context;
    //默认加载文件的属性名
    public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";


    public ContextLoader() {
    }

    public ContextLoader(WebApplicationContext context) {
        this.context = context;
    }

    /**
     * 初始化WebApplicationContext
     *
     * @param servletContext
     */
    public void initWebApplicationContext(ServletContext servletContext) {
        //创建XmlWebApplicationContext
        try {
            Object context = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            //获取springmvc的配置路径
            String contextInitParameter = servletContext.getInitParameter(ContextLoader.CONFIG_LOCATION_PARAM);
            if (null == contextInitParameter || contextInitParameter.length() == 0) {
                contextInitParameter = "applicationContext.xml";
            }
            if (null == context) {
                this.context = new XmlWebApplicationContext(servletContext, contextInitParameter);
            } else {
                this.context = (XmlWebApplicationContext) context;
            }
            //开始执行刷新，刷新会把所有的bean配置信息都初始化
            this.context.refresh();

            //添加到servletContext中，把WebApplicationContext存储起来
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
