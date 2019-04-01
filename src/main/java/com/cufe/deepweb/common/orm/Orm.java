package com.cufe.deepweb.common.orm;

import org.sql2o.Sql2o;

public final class Orm {
    private Orm() { }
    private static Sql2o sql2o;

    /**
     * initialize a new sql2o
     * @param newSql2o the specified sql2o object
     */
    public static void setSql2o(final Sql2o newSql2o) {
        Orm.sql2o = newSql2o;
    }

    /**
     * get a instance of sql2o，the instance is thread-safe, support share globally
     * @return
     */
    public static Sql2o getSql2o() {
        return Orm.sql2o;
    }
}
