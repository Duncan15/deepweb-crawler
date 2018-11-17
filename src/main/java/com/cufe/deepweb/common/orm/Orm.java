package com.cufe.deepweb.common.orm;

import org.sql2o.Sql2o;

public final class Orm {
    private Orm() { }
    private static Sql2o sql2o;

    /**
     * 初始化设置sql2o
     * @param newSql2o 初始化的sql2o对象
     */
    public static void setSql2o(final Sql2o newSql2o) {
        Orm.sql2o = newSql2o;
    }

    /**
     * 获取sql2o实例，该实例线程安全，可全局共用
     * @return
     */
    public static Sql2o getSql2o() {
        return Orm.sql2o;
    }
}
