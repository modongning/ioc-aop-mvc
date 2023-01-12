package com.otoomo.ioc.resource;

import java.io.InputStream;

/**
 * 获取配置资源顶级接口
 *
 * @author modongning
 * @date 13/10/2020 11:52 PM
 */
public interface Resource {
    /**
     * 根据路径获取配置文件流
     *
     * @return
     */
    InputStream getInputStream();
}
