package com.cufe.deepweb.algorithm;

import java.util.ArrayList;
import java.util.List;

/*
    abstract class for crawling algorithm
    query-generate algorithm，该类仅关注query的生成逻辑
 */
public abstract class AlgorithmBase {
    private List<String> qList; //the list which store all seleted queries
    private int qCount; //the new generated query index

    public AlgorithmBase() {
        qCount = 0;
        qList = new ArrayList<>();
    }
    /*
    for set initial query when algorithm initiates
     */
    public final void setInitQuery(final String init) {
        qList.clear();
        qList.add(init);
    }
    protected final List<String> getqList(){
        return qList;
    }
    /*
    使用该算法时直接调用该方法，不必关心具体算法实现
     */
    public final String getNextQuery() {
        if(qCount != 0){//当不是第一个词时，通过动态绑定调用实现类的generateQuery方法，获取下一个词
            String nextQuery = generateQuery();
            qList.add(nextQuery);
            qCount++;
        }
        return qList.get(qCount);
    }

    /*
    the implementing class must implement this method to generate next query
    */
    protected abstract String generateQuery();
}
