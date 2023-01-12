package com.otoomo.web.pojo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * url和method的映射关系信息
 */
public class HandlerMapping {
    /**
     * 要映射的controller类
     */
    private Object handler;
    /**
     * 映射的方法
     */
    private Method method;
    /**
     * 正则表达式的url
     */
    private Pattern pattern;
    /**
     * 方法的参数所在坐标位置
     */
    private Map<String, Integer> paramsIndexMapping;

    public HandlerMapping(Object controller, Method method, Pattern pattern) {
        this.pattern = pattern;
        this.handler = controller;
        this.method = method;
        this.paramsIndexMapping = new HashMap<>();
    }

    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Map<String, Integer> getParamsIndexMapping() {
        return paramsIndexMapping;
    }

    public void setParamsIndexMapping(Map<String, Integer> paramsIndexMapping) {
        this.paramsIndexMapping = paramsIndexMapping;
    }
}
