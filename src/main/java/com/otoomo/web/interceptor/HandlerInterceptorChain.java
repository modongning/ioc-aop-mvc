package com.otoomo.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 拦截器链
 * 作用是执行uri对应的所有拦截器
 * <p>
 * 前置拦截，正序执行拦截器列表的拦截器preHandle()方法
 * 后置拦截，倒叙执行拦截器列表的拦截器preHandle()方法
 */
public class HandlerInterceptorChain {

    private List<HandlerInterceptor> interceptorList;

    public HandlerInterceptorChain(List<HandlerInterceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    /**
     * 正序执行拦截逻辑
     *
     * @param request
     * @param response
     * @param handler
     * @param handler
     * @return method
     */
    public boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Method method) {
        for (HandlerInterceptor handlerInterceptor : interceptorList) {
            if (!handlerInterceptor.preHandle(request, response, handler, method)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 倒叙执行拦截逻辑
     *
     * @param request
     * @param response
     * @param handler
     * @param method
     */
    public void doPostHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Method method) {
        for (int i = interceptorList.size() - 1; i >= 0; i--) {
            interceptorList.get(i).postHandle(request, response, handler, method);
        }
    }
}
