package com.cufe.deepweb.common.dedu;

import java.io.Closeable;

//用于去重
public abstract class Deduplicator<T> implements Closeable {
    protected final String DATA_FILE_NAME = "_dedu.dat";
    /**
     * 该方法的具体实现应该保证线程安全
     * @param o
     * @return true if o can be added into the deduplicator, or false if o has been in the deduplicator
     */
    public abstract boolean add(T o);

    /**
     * 从上一次调用该方法至今，新元素的数量
     * @return
     */
    public abstract int getNew();

    /**
     * 从上一次调用该方法至今，进行去重检测的次数
     * @return
     */
    public abstract int getCost();

    /**
     * 当前元素数量
     * @return
     */
    public abstract int getTotal();
}
