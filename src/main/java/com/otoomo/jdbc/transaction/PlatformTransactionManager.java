package com.otoomo.jdbc.transaction;

import java.sql.SQLException;

/**
 * 顶级事务管理接口
 *
 * @author modongning
 * @date 21/10/2020 6:18 PM
 */
public interface PlatformTransactionManager {
    /**
     * 开始事务
     *
     * @throws SQLException
     */
    void beginTransaction() throws SQLException;

    /**
     * 提交事务
     *
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * 回滚事务
     *
     * @throws SQLException
     */
    void rollback() throws SQLException;
}
