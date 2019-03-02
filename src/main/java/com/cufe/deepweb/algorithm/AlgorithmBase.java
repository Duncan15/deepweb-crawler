package com.cufe.deepweb.algorithm;

import com.cufe.deepweb.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * abstract class for crawling algorithm
 * query-generate algorithm，this class only focus on query generation logic
 */
public abstract class AlgorithmBase {
    private final Logger logger = LoggerFactory.getLogger(AlgorithmBase.class);
    private List<String> qList; //the list which store all seleted queries
    private int qCount; //the new generated query index

    public AlgorithmBase() {
        qCount = 0;
        qList = new ArrayList<>();
        setInitQuery("consume");//set the default initial query, the implementation class can cover this value
    }
    /*
    for set initial query when algorithm initiates
     */
    public final void setInitQuery(final String init) {
        qCount = 0;
        qList.clear();
        qList.add(init);
    }

    /**
     * get the term list generate by current algorithm
     * @return
     */
    public final List<String> getqList(){
        return Collections.unmodifiableList(qList);
    }

    /**
     * rebuild the term list, this method should only be invoked on production mode
     */
    public final void setqList(List<String> list) {
        this.qList.clear();
        this.qList.addAll(list);
        this.qCount = qList.size();
    }
    /*
    使用该算法时直接调用该方法，不必关心具体算法实现
     */
    public final String getNextQuery() {
        logger.trace("start to infer query");
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
        logger.trace("infer finish");
        return query;
    }

    /*
    the implementing class must implement this method to generate next query
    */
    protected abstract String generateQuery();

    /**
     * the implementation class can override this method for doing some close operations
     */
    public void close(){};
}
