# DispatcherServlet继承结构

HttpServlet .java << HttpServletBean.java << FrameworkServlet.java << DispatcherServlet.java



**请求来临时，被doGet/doPost处理**

FrameworkServlet.java

​	.doGet(request,response)/doPost(request,response)

​		.processRequest(request,response)

​			.abstract doService(request,response) 子类实现

DispatcherServlet.

​	.doService(request,response)

​		**.doDispatch(request,response)**核心方法，主要处理了请求的后续逻辑


# SpringMVC处理请求的大致流程

DispatcherServlet.doDispatch()方法核心步骤：

1. 调用getHandler获取到能够处理当前请求的执行链 HandlerExecutionChain（handler+拦截器）

  2. getHandlerAdapter(mappedHandler.getHandler()) 获取能够执行handler的适配器
   3. 执行前置拦截mappedHandler.applyPreHandle() ，正序执行拦截器的preHandle()方法
   4. 适配器调用Handler执行ha.handle(总会返回一个ModelAndView对象)
   5. 调用applyDefaultViewName()进行结果视图对象的处理
   6. 执行后置拦截mappedHandler.applyPostHandle(()，倒叙执行拦截器的postHandle()方法
   7. 调用processDispatchResult()方法完成视图跳转
   8. 最终会执行拦截器的afterCompletion()方法



```java
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    HttpServletRequest processedRequest = request;
    try {
        ModelAndView mv = null;
		Exception dispatchException = null;
        try {
            // 检查是否是⽂件上传的请求
            processedRequest = checkMultipart(request);

            //省略一些代码
            //...

            /*
                1.获取处理当前请求的Controller，即处理器
                这⾥并不是直接返回 Controller，⽽是返回 HandlerExecutionChain 请求处理链对象
                该对象封装了Handler和Inteceptor
            */
            mappedHandler = getHandler(processedRequest);
            if (mappedHandler == null) {
                // 如果 handler 为空，则返回404
                noHandlerFound(processedRequest, response);
                return; 
            }

            // 2.获取处理请求的处理器适配器 HandlerAdapter
            HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

            // Process last-modified header, if supported by the handler.
            //省略一些代码
            //...

            // 3.执行前置拦截器
            if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                return; 
            }

            // 4. 实际处理器处理请求，返回结果视图对象(ModelAndView)
            mv = ha.handle(processedRequest, response,mappedHandler.getHandler());

            if (asyncManager.isConcurrentHandlingStarted()) {
                return; 
            }
            // 5.结果视图对象的处理
            applyDefaultViewName(processedRequest, mv);
            
            // 6.执行后置拦截器
            mappedHandler.applyPostHandle(processedRequest, response, mv);
            
        }catch (Exception ex) {
            dispatchException = ex; 
        }catch (Throwable err) {
        // As of 4.3, we're processing Errors thrown from handler methods as well,
        // making them available for @ExceptionHandler methods and other scenarios.
        	dispatchException = new NestedServletException("Handler dispatch failed",err);
        }
        // 7.跳转⻚⾯，渲染视图
        processDispatchResult(processedRequest, response, mappedHandler, mv,dispatchException);
        
    }catch (Exception ex) {
        //最终会调⽤HandlerInterceptor的afterCompletion ⽅法
        triggerAfterCompletion(processedRequest, response, mappedHandler,ex);
    }catch (Throwable err) {
        //最终会调⽤HandlerInterceptor的afterCompletion ⽅法
        triggerAfterCompletion(processedRequest, response, mappedHandler,
        new NestedServletException("Handler processing failed", err));
    }finally {
        if (asyncManager.isConcurrentHandlingStarted()) {
            // Instead of postHandle and afterCompletion
            if (mappedHandler != null) {
                mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest,
                response);
            }
        }else {
            // Clean up any resources used by a multipart request.
            if (multipartRequestParsed) {
                cleanupMultipart(processedRequest);
            } 
		}
    }
}
```



## getHandler方法解析

1. 遍历两个hanlderMappings : BeanNameUrlHandlerMapping，RqeustMappingHandlerMapping
2. 根据请求链接获取对应的handler

## getHandlerAdapter

1. 遍历三个HandlerAdapter：HttpRequestHandlerAdapter,SimpleControllerHandlerAdapter,RequestMappingHandlerAdapter 判断实现类型是否实现对应 HttpRequest（继承） / Controller（继承） / @RequestMapping（注解）



# 九大组件

- MultipartResolver 多部件解析器

- LocaleResolver 国际化解析器

- ThemeResolver 主题解析器

- List<HanlderMapping> handlerMappings 处理器映射器组件

- List<HandlerAdapter> handlerAdapters 处理器适配器组件

- List<HandlerExceptionResolver> handlerExceptionResolvers 异常解析器组件

- RequestToViewNameTranslator viewNameTranslator 默认视图名转换器组件

- FlashMapManager flash属性管理组件（重定向属性）

- List<ViewResolver> viewResolvers 视图解析器

	

九大组件都是定义了接口，接口其实是定义了规范。由具体的子类去实现具体的业务逻辑



# 自定义实现MVC

自定义mvc 实现了前端请求的处理流程。

涉及到的注解有： @Controller,@RequestMapping,@Interceptor 

@Controller 注明是一个请求处理器，添加到ioc容器中

@RequestMapping 注明请求的映射路径信息

@Interceptor 指定一个类为拦截器，会加入到IOC容器



其中@Controller，@Interceptor注解的类，会添加到IOC容器中，具体实现在 IOC-AOP 项目中判断处理加入到容器。具体实现查看 自定义实现 ioc-aop项目。



@RequestMapping 的注解会在初始化Servlet的时候会使用，作用是识别相关的请求映射路径，用于定位前端的请求路径具体需要执行的是哪个类的哪个函数



## 实现步骤

1. 定义类：DispatcherServlet ，继承 HttpServlet
2. 重写init(ServletConfig config)
	1. 先获取context，判断是否已初始化过
	2. context未初始化，新建一个。已经初始化，则沿用这个context
	3. 调用context的refresh方法初始化mvn的相关配置（也就是执行了ioc的初始化流程）
	4. 调用onRefresh方法初始化mvc（包括所有处理器，拦截器）
3. 第一次接收到请求时，会执行初始化方法init()
4. 执行请求
	1. getHandlerMapping()，获取处理器映射器
	2. getRequestArgs()，获取所有请求参数数组
	3. doHandler()，执行处理器



###DispatcherServlet

WebApplicationContext 的上下文的相关逻辑在 自定义 IOC-AOP 的项目中实现。由于这是实现MVC的流程，所以这里不说明WebApplicationContext具体的实现细节。

```java
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
```



###HandlerInterceptor 拦截器接口

```java
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
```

###拦截器执行链 HandlerInterceptorChain

```java
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
```



###HandlerInterceptorMapping 处理器拦截器映射器

```java
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
}
```



###处理器映射器 HandlerMapping

```java
/**
 * 处理器映射器
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
}
```

​	

###拦截器注解 @Interceptor

```java
/**
 * 拦截器注解
 * 注解了这个的class，则表示为拦截器，会加入到IOC容器中。
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptor {
    //拦截的uri
    String[] interceptUri() default {};

    //不需要拦截的uri
    String[] excludeUri() default {};
}
```



### 处理器注解 @Controller

```java 
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
}
```



### 处理器映射器注解 @ RequestMapping

```java
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value() default "";
}
```



## 使用

###配置 web.xml

```xml
<web-app>
    <display-name>Archetype Created Web Application</display-name>

    <servlet>
        <servlet-name>mvc</servlet-name>
        <servlet-class>com.otoomo.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>mvc.xml</param-value>
        </init-param>
    </servlet>

    <servlet-mapping>
        <servlet-name>mvc</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

</web-app>
```

### 配置 mvc.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans>
    <component-scan base-package="com.xxx.controller"/>
</beans>
```

###定义处理器

```java
@Controller
@RequestMapping("/transfer")
public class TransferController {

    @RequestMapping
    public void transfer(HttpServletRequest request, HttpServletResponse response, String fromCardNo, String toCardNo, String money) throws IOException {
        //...业务逻辑
    }
}
```

### 定义拦截器

```java
@Interceptor(
        interceptUri = {"/transfer"}
)
public class SecurityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Method method) {
        System.out.println("SecurityInterceptor preHandle......");
		// .... 业务逻辑
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Method method) {
        System.out.println("SecurityInterceptor postHandle......");
    }
}
```

