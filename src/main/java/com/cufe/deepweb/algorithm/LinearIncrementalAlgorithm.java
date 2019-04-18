package com.cufe.deepweb.algorithm;

import com.cufe.deepweb.common.index.IndexClient;
import com.cufe.deepweb.common.dedu.Deduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;


/**
 * the implementation class of linear search algorithm
 * this class is responsible for providing concrete data and strategy for whether start a new turn set covering
 * if want to extend this class, can just override the isUpdate method
 */
public class LinearIncrementalAlgorithm extends SetCoverAlgorithm {
    private final Logger logger = LoggerFactory.getLogger(LinearIncrementalAlgorithm.class);
    /**
     * store the new from the beginning of this round of set covering
     */
    private long newV;
    /**
     * store the costV from the beginning of this round of set covering
     */
    private long costV;
    /**
     * the client to operate underling index
     */
    private IndexClient si;
    /**
     * the deduplicator for getting coefficient in downloading
     */
    private Deduplicator dedu;
    /**
     * the step length in linear search
     */
    private int stepLen;

    /**
     * the name of data saving file for production mode
     */
    private static final String DATA_FILE = "qList.dat";

    private Queue<Double> stepQueue;
    private LinearIncrementalAlgorithm(Builder builder) {
        super(builder);
        stepLen = builder.stepLen;
        stepQueue = new ArrayDeque<>(stepLen);
        si = builder.indexClient;
        dedu = builder.deduplicator;
    }

    protected void update() {
        si.updateIndex();
    }

    @Override
    protected boolean isUpdate() {
        int scSize = getSetCoverSize();

        //only run one time at the beginning, update the index and confirm to update
        if (scSize == 0) {
            logger.trace("first run isUpdate() method, just return true");
            dedu.getNew();
            dedu.getCost();
            return true;
        }

        //after generating first term in current turn's set covering, clear the stepQueue and initialize newV 、 costV
        if (scSize == 1) {
            stepQueue.clear();
            newV = 0;
            costV = getBuildTableCost();
        }

        //accumulate newV and costV for computing quality
        newV += dedu.getNew();
        costV += getModifyTableCost() + dedu.getCost();

        if(stepQueue.size() < stepLen){//record the first n quality
            stepQueue.offer(newV / (double)costV);
            return false;
        } else {//after recording first n quality, start to judge whether update after add current term
            double curQuality = newV / (double)costV;
            double preQuality = stepQueue.poll();
            if (curQuality >= preQuality) {
                stepQueue.offer(curQuality);
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    protected final Map<String, Set<Integer>> getDocSetMap(String field, double low, double up) {
        return si.getDocSetMap(field,low,up);
    }

    @Override
    protected final int getDocSize() {
        return si.getDocSize();
    }
    /*
    构造器
     */
    public static class Builder extends SetCoverAlgorithm.Builder {
        private IndexClient indexClient;
        private Deduplicator deduplicator;
        private int stepLen = 3;



        public Builder(IndexClient client,Deduplicator dedu){
            indexClient = client;
            deduplicator = dedu;
        }



        public Builder setStepLen(int stepLen) {
            this.stepLen = stepLen;
            return this;
        }

        public LinearIncrementalAlgorithm build(){
            LinearIncrementalAlgorithm algo = new LinearIncrementalAlgorithm(this);
            return algo;
        }
    }
}
