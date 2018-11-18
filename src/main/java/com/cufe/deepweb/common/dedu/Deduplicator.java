package com.cufe.deepweb.common.dedu;

import java.io.Closeable;

//用于去重
public abstract class Deduplicator<T> implements Closeable {
    protected final String DATA_FILE_NAME = "_dedu.dat";
    /**
     * new值
     */
    protected int newV;
    /**
     * cost值
     */
    protected int costV;
    /**
     * 该方法的具体实现应该保证线程安全，同时注意处理newV和costV的值
     * @param o
     * @return true if o can be added into the deduplicator, or false if o has been in the deduplicator
     */
    public abstract boolean add(T o);
    /**
     * 当前元素数量
     * @return
     */
    public abstract int getTotal();



    /**
     * 从上一次调用该方法至今，新元素的数量
     * @return
     */
    public final int getNew() {
        int tmp = newV;
        newV = 0;
        return tmp;
    }


    /**
     * 从上一次调用该方法至今，进行去重检测的次数
     * @return
     */
    public final int getCost() {
        int tmp = costV;
        costV = 0;
        return tmp;
    }

}
