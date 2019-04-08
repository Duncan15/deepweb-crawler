package com.cufe.deepweb.common.dedu;

import java.util.HashSet;
import java.util.Set;

/**
 * 使用唯一的docID去重的去重器
 */
public class RAMDocIDDedutor extends Deduplicator<Integer> {
    /**
     * 去重集合
     */
    private Set<Integer> deduSet;
    public RAMDocIDDedutor() {
        deduSet = new HashSet<>();

    }
    @Override
    public synchronized boolean add(Integer o) {
        this.costV++;
       if (deduSet.add(o)) {
           this.newV++;
           return true;
       }
       return false;
    }


    @Override
    public int getTotal() {
        return deduSet.size();
    }

    @Deprecated
    @Override
    public void close() {

    }
}
