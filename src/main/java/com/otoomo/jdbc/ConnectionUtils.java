package com.otoomo.jdbc;

import com.otoomo.util.DruidUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionUtils {

    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    /**
     * 从当前线程获取连接
     */
    public static Connection getCurrentThreadConn() throws SQLException {
        /**
         * 判断当前线程中是否已经绑定连接，如果没有绑定，需要从连接池获取一个连接绑定到当前线程
         */
        Connection connection = currentConnection.get();
        if (connection == null) {
            // 从连接池拿连接并绑定到线程
            connection = DruidUtils.getInstance().getConnection();
            // 绑定到当前线程
            currentConnection.set(connection);
        }
        return connection;

    }
}
