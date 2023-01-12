# 自定义实现IOC AOP
模仿spring的ioc，aop进行的自定义实现

##使用方式

### 添加依赖
```xml
<dependency>
    <groupId>com.otoomo</groupId>
    <artifactId>ioc-aop</artifactId>
    <packaging>jar</packaging>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### XML方式启动

#### application.xml配置

```xml
<beans>
    <!--
        第一种配置方式：指定注入到容器的bean
    -->
    <!--id标识对象，class是类的全限定类名-->
    <bean id="testDao" class="com.otoomo.dao.TestDao"></bean>
    
    <bean id="testService" class="com.otoomo.service.impl.TestServiceImpl">
        <!--set+${name} 之后锁定到传值的set方法(setTestDao)，通过反射技术可以调用该方法传入对应的值-->
        <property name="testDao" ref="testDao"></property>
    </bean>
    

    <!--
        第二种配置方式：指定扫描包名下的所有目录。使用这个，需要添加@Service注解
    -->
    <!--<component-scan base-package="com.otoomo.xxx"/>-->

</beans>
```
两种配置方式都可以使用，或者使用其中一种。

这里先介绍存xml扫码注入的方式。注解方式请往下看


#### 代码中使用

```java
public class TestService {
    //xml中配置了注入testDao，会通过反射注入实例
    TestDao testDao;
    //...业务实现
}
```

```java
public class TestDao {
    //...业务实现
}
```

```java
@WebServlet(name = "testServlet", urlPatterns = "/test")
public class TransferServlet extends HttpServlet {

    ApplicationContext applicationContext;

    TestSerivce testService;

    {
        try {
            //使用xml方式初始化
            applicationContext = new ClasspathXmlApplicationContext("application.xml");
            //从容器中获取Bean
            testService = applicationContext.getBean("testService");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //调用service层方法
        testService.test();

        // 响应
        resp.setContentType("application/json;charset=utf-8");
        resp.getWriter().print("OK");
    }
}
```



### 全注解的方式启动

- @Service
  
    添加了@Service注解的类，都会被添加到容器中

- @Autowired
  
    容器在初始化时，添加了@Autowired注解的，都会被注入具体的实例

```java
import com.otoomo.ioc.annotation.Autowired;
import com.otoomo.ioc.annotation.Service;

@Service
public class TestService {
    @Autowired
    TestDao testDao;
    //...业务实现
}
```

```java
import com.otoomo.ioc.annotation.Service;

@Service
public class TestDao {
    //...业务实现
}
```


#### 代码中使用IOC

```java
@WebServlet(name = "testServlet", urlPatterns = "/test")
public class TransferServlet extends HttpServlet {

    ApplicationContext applicationContext;

    TestService testService;
    
    {
        try {
            //使用注解方式方式初始化
            applicationContext = new AnnotationConfigApplicationContext("com.otoomo");
            //从容器中获取Bean (由于目前没有实现在Servlet层就注入实例，所以这里手工从容器获取)
            testService = applicationContext.getBean("testService");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //调用service层方法
        testService.test();
        
        // 响应
        resp.setContentType("application/json;charset=utf-8");
        resp.getWriter().print("OK");
    }
}
```
