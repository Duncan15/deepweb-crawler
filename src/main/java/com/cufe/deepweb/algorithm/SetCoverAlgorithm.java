package com.cufe.deepweb.algorithm;

import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.crawler.Constant;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * set-covering algorithm: this class only focus on the implementation of set-covering algorithm，
 * the concrete data and whether to start a new turn of set-covering provided by the following implementation class
 */
public abstract class SetCoverAlgorithm extends AlgorithmBase {
    private final Logger logger = LoggerFactory.getLogger(SetCoverAlgorithm.class);
    /**
     * the lucene index's main field focused by this algorithm
     */
    private String mainField;
    /**
     * set covering algorithm's up bound
     */
    private double upBound;
    private double lowBound;
    /**
     * set covering threshold, such as 0.95
     */
    private double threshold;
    /**
     * the cost for sending a query
     */
    private int sendingCost;
    /**
     * the cost for building the set covering matrix
     */
    private long buildTableCost;
    /**
     * the cost for modifying the set covering matrix by current query
     */
    private long modifyTableCost;
    /**
     * the snapshot size of index when start current turn's set covering
     */
    private int snapshotSize;
    /**
     * term list collect by current turn's set covering
     */
    private List<String> termList;
    /**
     * the each term's initial document frequency at the beginning of current turn's set covering
     */
    private Map<String, Integer> df;
    /**
     * the each term's new docID set in set covering,this map would be updated every round
     */
    private Map<String, Set<Integer>> newMap;
    /**
     * a set to store all the downloaded(logically) docID in current turn's set covering
     */
    private Set<Integer> s;


    /**
     *
     * @param mField the lucene index's main field focused by this algorithm
     * @param ubound set covering algorithm's up bound
     * @param lbound
     * @param thres set covering threshold, such as 0.95
     * @param sCost the cost for sending a query
     */
    public SetCoverAlgorithm(String mField,double ubound,double lbound,double thres,int sCost) {
        mainField = mField;
        upBound = ubound;
        lowBound = lbound;
        threshold = thres;
        sendingCost = sCost;
        buildTableCost = 0;
        snapshotSize = 0;
        termList = new ArrayList<>();
        df = new HashMap<>();
        newMap = new HashMap<>();
        s = new HashSet<>();
    }

    /**
     * if update is true，start the new turn's set covering
     * @param update
     * @return
     */
    private String getNextTerm(boolean update) {
        if(!update){ //if update is false
            logger.trace("try to generate term in current turn's set covering");
            String newTerm = generateTerm();
            if(newTerm == null) {
                logger.trace("fail to generate term from current turn's set covering");
                return getNextTerm(true);
            }
            logger.trace("generate successfully");
            termList.add(newTerm);
            return newTerm;
        }else { //when update is true
            buildMatrix();
            return getNextTerm(false);
        }
    }

    /**
     * build the set covering matrix at the beginning of each turn's set covering
     */
    private void buildMatrix() {
        //start to build matrix
        logger.trace("start to build matrix");
        Utils.logMemorySize();

        termList.clear();
        Stopwatch stopwatch = Stopwatch.createStarted();//use to record the time cost in building matrix
        s.clear();
        df.clear();
        newMap.clear();
        newMap = getDocSetMap(mainField,lowBound,upBound);//store each term's new docID set in set covering

        //remove the selected query in the candidate terms
        if(getqList().size() != 0) {
            for(String query : getqList()) {
                if(newMap.containsKey(query)) {
                    newMap.remove(query);
                }
            }
        }

        newMap.entrySet().forEach(entry -> df.put(entry.getKey(), entry.getValue().size()));//store each term's initial document frequency

        buildTableCost = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        snapshotSize = getDocSize();

        Utils.logMemorySize();
        logger.info("build matrix finish");
    }

    /**
     * when can't generate new term in current turn's set covering, return null
     * when following situations happen, can't generate new term in current turn's set covering:
     * 1.the current turn's set covering hit the predefined threshold
     * 2.the current turn's set covering has decreased all the candidate terms' new to 0
     * @return
     */
    private String generateTerm() {
        //the following is set covering's main flow

        if (s.size() < threshold * snapshotSize) {//check whether satisfy the predefined threshold
            Stopwatch stopwatch = Stopwatch.createStarted();//use to compute the cost in modifying matrix for current term
            Set<String> candidate = new HashSet<>(); //use to store the terms whose new/cost is identical
            String query = null; //the term would be generate in current round
            double maxRate = 0.0; //the known biggest new/cost value
            int maxDF = 0; //the known biggest DF value

            //select the term from candidates, which has the biggest new/cost value
            for(Map.Entry<String, Set<Integer>> entry : newMap.entrySet()) {
                if(entry.getValue().size() != 0) { //filter out the term whose new has been decreased to 0
                    double curRate = entry.getValue().size() / (double)(df.get(entry.getKey()) + sendingCost); //new/cost=new/(df+100)

                    //if current term's new/cost bigger than the known biggest one，update the biggest value，and clear candidate
                    if(curRate > maxRate){
                        maxRate = curRate;
                        candidate.clear();
                    }

                    //if current term's new/cost is equal to biggest one, add it to candidate
                    if(curRate >= maxRate){
                        candidate.add(entry.getKey());
                    }
                }
            }

            //when the size of candidates bigger than 1, choose the term whose document frequency is biggest
            if(candidate.size() >= 1){
                maxDF = 0;
                for(String each : candidate){
                    if(maxDF < df.get(each)){
                        maxDF = df.get(each);
                        query = each;
                    }
                }
            }else { //when the size is 0, it means can't generate new term from current turn's set covering, return null
                logger.warn("set covering 中出现new全部为0的情况，set covering");
                return null;
            }

            //remove the reference of query in df and newMap
            Set<Integer> deleted = newMap.remove(query);
            df.remove(query);
            s.addAll(deleted);

            //update new
            for(Map.Entry<String,Set<Integer>> entry : newMap.entrySet()) {
                entry.getValue().removeAll(deleted);
            }

            modifyTableCost = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            return query;
        }
        return null;
    }

    /*
    在当前实现层次实现父类的抽象函数，同时进行更细致的抽象，留给子类实现
     */
    @Override
    protected final String generateQuery() {
        return getNextTerm(isUpdate());
    }

    /**
     * the following method is for sub class to use
     */
    /**
     * get the termList's size in current turn's set covering
     * @return
     */
    protected final int getSetCoverSize() {
        return termList.size();
    }

    /**
     * get the current turn's set covering's build matrix cost
     * @return
     */
    protected final long getBuildTableCost() {
        return buildTableCost;
    }

    /**
     *
     * @return
     */
    protected final long getModifyTableCost() {
        return modifyTableCost;
    }

    /**
     * the following method is for sub class to implement
     */
    /**
     * judge whether to start a new turn set covering
     * @return
     */
    protected abstract boolean isUpdate();

    /**
     * get the specified field's candidate terms with corresponding docID set
     * @param field
     * @param low
     * @param up
     * @return
     */
    protected abstract Map<String, Set<Integer>> getDocSetMap(String field,double low,double up);

    /**
     * get current index's document size
     * @return
     */
    protected abstract int getDocSize();
}
