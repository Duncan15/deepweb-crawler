package com.cufe.deepweb.algorithm;

import com.cufe.deepweb.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * abstract class for crawling algorithm
 * query-generate algorithm，该类仅关注query的生成逻辑
 */
public abstract class AlgorithmBase {
    private final Logger logger = LoggerFactory.getLogger(AlgorithmBase.class);
    private List<String> qList; //the list which store all seleted queries
    private int qCount; //the new generated query index

    public AlgorithmBase() {
        qCount = 0;
        qList = new ArrayList<>();
        setInitQuery("consume");//set the default initial query
    }
    /*
    for set initial query when algorithm initiates
     */
    public final void setInitQuery(final String init) {
        qList.clear();
        qList.add(init);
    }

    /**
     * 获取算法所生成的所有词的列表，该列表不可修改
     * @return
     */
    public final List<String> getqList(){
        return Collections.unmodifiableList(qList);
    }
    /*
    使用该算法时直接调用该方法，不必关心具体算法实现
     */
    public final String getNextQuery() {
        logger.info("start to infer query");
        Utils.logMemorySize();

        //if the query to get is not the first one
        // invoke the implementation class's override method generateQuery() by dynamic binding
        if(qCount != 0) {
            String nextQuery = generateQuery();
            logger.trace("get query {}", nextQuery);
            qList.add(nextQuery);
        }

        String query = qList.get(qCount);
        qCount++;
        Utils.logMemorySize();
        logger.info("infer finish");
        return query;
    }

    /*
    the implementing class must implement this method to generate next query
    */
    protected abstract String generateQuery();
}
