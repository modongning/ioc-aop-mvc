package com.otoomo.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 拦截器接口
 */
public interface HandlerInterceptor {

    /**
     * 请求前置拦截
     *
     * @param request
     * @param response
     * @param handler  被拦截的controller
     * @param method   被拦截的方法
     * @return
     */
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Method method) {
        return true;
    }

    /**
     * 请求后置拦截
     *
     * @param request
     * @param response
     * @param handler  被拦截的controller
     * @param method   被拦截的方法
     */
    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Method method) {

    }
}
