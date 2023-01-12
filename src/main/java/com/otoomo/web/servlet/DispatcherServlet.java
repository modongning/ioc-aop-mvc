package com.otoomo.web.servlet;

import com.otoomo.ioc.factory.ConfigurableListableBeanFactory;
import com.otoomo.ioc.factory.DefaultListableBeanFactory;
import com.otoomo.web.annotation.Controller;
import com.otoomo.web.annotation.Interceptor;
import com.otoomo.web.annotation.RequestMapping;
import com.otoomo.web.context.ContextLoader;
import com.otoomo.web.context.WebApplicationContext;
import com.otoomo.web.context.XmlWebApplicationContext;
import com.otoomo.web.interceptor.HandlerInterceptor;
import com.otoomo.web.interceptor.HandlerInterceptorChain;
import com.otoomo.web.pojo.HandlerInterceptorMapping;
import com.otoomo.web.pojo.HandlerMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义控制器
 *
 * @author modongning
 * @date 30/10/2020 10:01 AM
 */
public class DispatcherServlet extends HttpServlet {

    WebApplicationContext context;

    /**
     * 处理器映射器
     */
    List<HandlerMapping> handlerMappings = new ArrayList<>();
    /**
     * 拦截器映射器
     */
    List<HandlerInterceptorMapping> handlerInterceptorMappings = new ArrayList<>();
    /**
     * uri拦截器缓存
     * <p>
     * key是请求的uri
     * value是拦截器链，包含多个拦截器的执行顺序
     */
    Map<String, HandlerInterceptorChain> handlerInterceptorCache = new HashMap<>();

    Class[] servletInnerParamClasses = new Class[]{
            HttpSession.class,
            HttpServletRequest.class,
            HttpServletResponse.class
    };

    /**
     * 初始化servlet
     * <p>
     * 1.先获取context，判断是否已初始化过
     * 2.context未初始化，新建一个。已经初始化，则沿用这个context
     * 3.调用context的refresh方法初始化mvn的相关配置（也就是执行了ioc的初始化流程）
     * 4.调用onRefresh方法初始化mvc（包括所有处理器，拦截器）
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        try {
            Object context = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            //获取springmvc的配置路径
            String configLocation = config.getInitParameter(ContextLoader.CONFIG_LOCATION_PARAM);
            if (null == configLocation || configLocation.length() == 0) {
                configLocation = "applicationContext.xml";
            }
            if (null == context) {
                this.context = new XmlWebApplicationContext(servletContext, configLocation);
            } else {
                this.context = (XmlWebApplicationContext) context;
                this.context.setConfigLocation(configLocation);
            }
            //开始执行刷新，刷新会把所有的bean配置信息都初始化
            this.context.refresh();

            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);

            this.onRefresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        executePost(req, resp);
    }

    /**
     * 执行请求
     * 1.获取处理器映射器
     * 2.获取所有请求参数数组
     * 3.执行处理器
     *
     * @param request
     * @param response
     */
    private void executePost(HttpServletRequest request, HttpServletResponse response) {
        //根据请求的url，查询对应的handler
        HandlerMapping handlerMapping = getHandlerMapping(request);

        if (null == handlerMapping) {
            response.setStatus(404);
            return;
        }

        try {
            //获取请求参数
            Object[] args = getRequestArgs(request, response, handlerMapping);

            //执行
            this.doHandler(request, response, handlerMapping, args);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            response.setStatus(400);
        } catch (InvocationTargetException e) {
            response.setStatus(400);
            e.printStackTrace();
        }
    }

    /**
     * 获取请求的所有参数
     *
     * @param request
     * @param response
     * @param handlerMapping
     * @return
     */
    private Object[] getRequestArgs(HttpServletRequest request, HttpServletResponse response, HandlerMapping handlerMapping) {
        Map<String, Integer> paramsIndexMapping = handlerMapping.getParamsIndexMapping();

        /*
        遍历请求的参数，把类型一致且名称一致的参数值记录到数组对应的下标存储
         */
        Object[] args = new Object[paramsIndexMapping.size()];
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        String paramStr = null;
        for (Map.Entry<String, String[]> param : requestParameterMap.entrySet()) {
            String key = param.getKey();
            if (!paramsIndexMapping.containsKey(key)) {
                continue;
            }
            String[] value = param.getValue();
            if (value.length > 1) {
                //多个相同类型的值，使用,拼接。 name=1&name=2 ----> 1,2
                StringJoiner stringJoiner = null;
                for (String s : value) {
                    if (null == stringJoiner) {
                        stringJoiner = new StringJoiner(s);
                    } else {
                        stringJoiner.add(s);
                    }
                }
                paramStr = stringJoiner.toString();
                if (!paramsIndexMapping.containsKey(key)) {
                    continue;
                }
            } else {
                paramStr = value[0];
            }

            Integer index = paramsIndexMapping.get(key);
            args[index] = paramStr;
        }

        /*
        处理3个特殊的参数：HttpServletRequest，HttpServletResponse，HttpSession
         */
        for (Class servletInnerParamClass : servletInnerParamClasses) {
            if (!paramsIndexMapping.containsKey(servletInnerParamClass.getSimpleName())) {
                continue;
            }
            if (servletInnerParamClass == HttpServletRequest.class) {
                args[paramsIndexMapping.get(servletInnerParamClass.getSimpleName())] = request;
            } else if (servletInnerParamClass == HttpServletResponse.class) {
                args[paramsIndexMapping.get(servletInnerParamClass.getSimpleName())] = response;
            } else if (servletInnerParamClass == HttpSession.class) {
                HttpSession session = request.getSession();
                args[paramsIndexMapping.get(servletInnerParamClass.getSimpleName())] = session;
            }
        }
        return args;
    }

    /**
     * 正式执行请求
     * 1. 先获取拦截器
     * 2. 执行前置拦截逻辑
     * 3. 执行handler
     * 4. 执行后置拦截逻辑
     *
     * @param request
     * @param response
     * @param handlerMapping
     * @param args
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void doHandler(HttpServletRequest request, HttpServletResponse response, HandlerMapping handlerMapping, Object[] args) throws InvocationTargetException, IllegalAccessException {
        /*
            获取拦截器链
         */
        HandlerInterceptorChain interceptorChain = getInterceptorChain(request, response, handlerMapping);

        Method method = handlerMapping.getMethod();
        Object handler = handlerMapping.getHandler();

        /*
        执行前置拦截
         */
        if (null != interceptorChain && !interceptorChain.doPreHandle(request, response, handlerMapping.getHandler(), method)) {
            return;
        }

        /*
        执行controller逻辑
         */
        method.invoke(handler, args);

        /*
        执行后置拦截
         */
        if (null != interceptorChain) {
            interceptorChain.doPostHandle(request, response, handlerMapping.getHandler(), method);
        }
    }

    /**
     * 获取拦截器链
     * 1. 先从缓存获取
     * 2. 缓存没有，则计算当前uri是否需要拦截
     * 3. 添加到uri拦截器缓存中
     * 4. 返回拦截器链
     *
     * @param request
     * @param response
     * @param handlerMapping
     * @return
     */
    private HandlerInterceptorChain getInterceptorChain(HttpServletRequest request, HttpServletResponse response, HandlerMapping handlerMapping) {
        String requestURI = request.getRequestURI();

        //从缓存获取uri拦截器链
        if (handlerInterceptorCache.containsKey(requestURI)) {
            return handlerInterceptorCache.get(requestURI);
        }

        List<HandlerInterceptor> matchInterceptList = new ArrayList<>();
        //缓存没有拦截器链，则计算符合的拦截器
        for (HandlerInterceptorMapping interceptorMapping : handlerInterceptorMappings) {
            String[] interceptUris = interceptorMapping.getInterceptUri();
            String[] excludeUris = interceptorMapping.getExcludeUri();

            //判断是否需要拦截
            for (String uri : interceptUris) {
                boolean intercept = true;
                //判断是否在拦截uri配置中
                Pattern pattern = Pattern.compile(uri);
                Matcher matcher = pattern.matcher(requestURI);
                if (!matcher.matches()) {
                    continue;
                }
                //判断这个uri是否已排除
                for (String excludeUri : excludeUris) {
                    Pattern excludePattern = Pattern.compile(excludeUri);
                    Matcher excludeMatcher = excludePattern.matcher(requestURI);
                    if (!excludeMatcher.matches()) {
                        continue;
                    }
                    intercept = false;
                }
                if (intercept) {
                    matchInterceptList.add(interceptorMapping.getInterceptor());
                }
            }
        }
        HandlerInterceptorChain chain = null;
        if (!matchInterceptList.isEmpty()) {
            //加入到拦截执行链中，缓存起来key = uri，value = HandlerInterceptorChain
            chain = new HandlerInterceptorChain(matchInterceptList);
        }
        //添加缓存
        handlerInterceptorCache.put(requestURI, chain);

        return chain;
    }

    /**
     * 获取处理器映射器
     *
     * @param request
     * @return
     */
    private HandlerMapping getHandlerMapping(HttpServletRequest request) {
        if (handlerMappings.isEmpty()) {
            return null;
        }
        String requestURI = request.getRequestURI();
        //遍历所有handlerMapping
        //匹配url一致的handlerMapping
        for (HandlerMapping handlerMapping : handlerMappings) {
            Pattern urlPattern = handlerMapping.getPattern();
            //正则匹配
            Matcher matcher = urlPattern.matcher(requestURI);
            if (!matcher.matches()) {
                continue;
            }
            return handlerMapping;
        }
        return null;
    }

    private void onRefresh() {
        initHandlerMapping();
        initHandlerInterceptions();
    }

    /**
     * 初始化所有拦截器
     */
    private void initHandlerInterceptions() {
        ConfigurableListableBeanFactory beanFactory = this.context.getBeanFactory();
        if (beanFactory instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
            List<String> beanDefinitionNames = listableBeanFactory.getBeanDefinitionNames();
            for (String beanDefinitionName : beanDefinitionNames) {
                Object bean = this.context.getBean(beanDefinitionName);

                Class<?> beanClass = bean.getClass();
                //没有添加Interceptor注解或者没有实现HandlerInterceptor接口都不是拦截器
                if (!beanClass.isAnnotationPresent(Interceptor.class)
                        || !(bean instanceof HandlerInterceptor)) {
                    continue;
                }

                Interceptor interceptor = beanClass.getAnnotation(Interceptor.class);
                String[] interceptUri = interceptor.interceptUri();
                String[] excludeUri = interceptor.excludeUri();

                handlerInterceptorMappings.add(new HandlerInterceptorMapping(interceptUri, excludeUri, (HandlerInterceptor) bean));
            }
        }
    }

    /**
     * 构建HandlerMapping处理器映射器，将配置好的url和method建立映射关系
     */
    private void initHandlerMapping() {
        ConfigurableListableBeanFactory beanFactory = this.context.getBeanFactory();
        if (beanFactory instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
            List<String> beanDefinitionNames = listableBeanFactory.getBeanDefinitionNames();
            processHandlerMappings(beanDefinitionNames);
        }
    }

    private void processHandlerMappings(List<String> beanDefinitionNames) {
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = this.context.getBean(beanDefinitionName);
            processHandlerMapping(bean);
        }
    }

    /**
     * 处理bean的url和method的映射关系
     *
     * @param bean
     */
    private void processHandlerMapping(Object bean) {
        //判断是否有@Controller注解
        Class<?> beanClass = bean.getClass();
        if (!beanClass.isAnnotationPresent(Controller.class)) {
            return;
        }
        //获取bean上的RequestMapping注解信息
        String baseUrl = "";
        if (beanClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = beanClass.getAnnotation(RequestMapping.class);
            baseUrl = standardizedUrl(requestMapping.value());
        }

        //遍历bean的所有方法，判断是否有RequestMapping注解。
        Method[] methods = beanClass.getMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(RequestMapping.class)) {
                //没有注解
                continue;
            }
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            String methodUrl = standardizedUrl(requestMapping.value());
            String url = baseUrl + methodUrl;

            //创建映射关系容器
            HandlerMapping handlerMapping = new HandlerMapping(bean, method, Pattern.compile(url));
            //计算方法的参数位置
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> parameterType = parameter.getType();

                //判断是否servlet内部的参数类型
                boolean isInnerParamType = false;
                for (Class servletInnerParamClass : servletInnerParamClasses) {
                    if (parameterType == servletInnerParamClass) {
                        isInnerParamType = true;
                        handlerMapping.getParamsIndexMapping().put(servletInnerParamClass.getSimpleName(), i);
                        break;
                    }
                }
                if (!isInnerParamType) {
                    //不是servletInnerParamClasses类型的参数，获取参数名存入
                    handlerMapping.getParamsIndexMapping().put(parameter.getName(), i);
                }
            }
            //保存映射关系
            handlerMappings.add(handlerMapping);
        }
    }

    /**
     * 标准化url
     *
     * @param url
     * @return
     */
    private String standardizedUrl(String url) {
        if (!"".equals(url) && !url.startsWith("/")) {
            url += "/";
        }
        return url;
    }

}
