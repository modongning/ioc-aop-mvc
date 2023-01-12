package com.otoomo.web.pojo;

import com.otoomo.web.interceptor.HandlerInterceptor;

/**
 * 处理器拦截器映射器
 */
public class HandlerInterceptorMapping {
    private String[] interceptUri;
    private String[] excludeUri;
    private HandlerInterceptor interceptor;

    public HandlerInterceptorMapping(String[] interceptUri, String[] excludeUri, HandlerInterceptor interceptor) {
        this.interceptUri = interceptUri;
        this.excludeUri = excludeUri;
        this.interceptor = interceptor;
    }

    public String[] getInterceptUri() {
        return interceptUri;
    }

    public void setInterceptUri(String[] interceptUri) {
        this.interceptUri = interceptUri;
    }

    public String[] getExcludeUri() {
        return excludeUri;
    }

    public void setExcludeUri(String[] excludeUri) {
        this.excludeUri = excludeUri;
    }

    public HandlerInterceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(HandlerInterceptor interceptor) {
        this.interceptor = interceptor;
    }
}
