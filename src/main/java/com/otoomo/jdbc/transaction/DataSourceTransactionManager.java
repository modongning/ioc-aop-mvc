package com.otoomo.jdbc.transaction;

import com.otoomo.jdbc.ConnectionUtils;
import com.otoomo.jdbc.annotation.Transactional;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceTransactionManager implements PlatformTransactionManager, MethodInterceptor {

    private Object targetObject;

    public DataSourceTransactionManager(Object targetObject) {
        this.targetObject = targetObject;
    }

    /**
     * 开启事务
     *
     * @throws SQLException
     */
    @Override
    public void beginTransaction() throws SQLException {
        ConnectionUtils.getCurrentThreadConn().setAutoCommit(false);
    }

    @Override
    public void commit() throws SQLException {
        Connection connection = ConnectionUtils.getCurrentThreadConn();
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        Connection connection = ConnectionUtils.getCurrentThreadConn();
        connection.rollback();
    }

    public void close() throws SQLException {
        Connection connection = ConnectionUtils.getCurrentThreadConn();
        connection.close();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object result = null;

        //判断方法是否存在事务注解
        Transactional annotation = method.getAnnotation(Transactional.class);
        if (null == annotation) {
            //不存在事务则直接执行
            result = method.invoke(this, objects);
        } else {
            //执行事务流程
            try {
                beginTransaction();

                result = method.invoke(this.targetObject, objects);

                commit();
            } catch (Exception e) {
                rollback();
                throw e.getCause();
            } finally {
                close();
            }
        }
        return result;
    }

}
