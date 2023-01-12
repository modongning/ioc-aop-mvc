package com.otoomo.web.annotation;

import java.lang.annotation.*;

/**
 * 拦截器注解
 * 注解了这个的class，则表示为拦截器，会加入到IOC容器中
 *
 * @author modongning
 * @date 02/11/2020 10:56 AM
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptor {
    String[] interceptUri() default {};

    String[] excludeUri() default {};
}
