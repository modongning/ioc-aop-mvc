package com.otoomo.web.context;

import com.otoomo.ioc.context.ConfigurableApplicationContext;

import javax.servlet.ServletContext;

public interface WebApplicationContext extends ConfigurableApplicationContext {
    String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

    void setConfigLocation(String configLocation);

    ServletContext getServletContext();
}
