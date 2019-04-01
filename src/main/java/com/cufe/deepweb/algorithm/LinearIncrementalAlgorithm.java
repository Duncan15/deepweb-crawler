package com.cufe.deepweb.algorithm;

import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.index.IndexClient;
import com.cufe.deepweb.common.dedu.Deduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

    /**
     * a path to remark whether run in production mode
     * in production mode, when exit should save some information
     */
    private Path productPath;

    private Queue<Double> stepQueue;
    private LinearIncrementalAlgorithm(Builder builder) {
        super(builder.mainField, builder.upBound, builder.lowBound, builder.threshold, builder.sendingCost);
        stepLen = builder.stepLen;
        stepQueue = new ArrayDeque<>(stepLen);
        si = builder.indexClient;
        dedu = builder.deduplicator;
        productPath = builder.productPath;
        this.setInitQuery(builder.initQuery);
        this.productInit();
    }

    /**
     * do some initial operations for production mode
     */
    private void productInit() {
        if (this.productPath != null) {
            File f = this.productPath.resolve(DATA_FILE).toFile();
            if(f.exists()) {
                logger.info("start to read qList information from file {}", f.getAbsolutePath());
                try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
                    List<String> qList = (List<String>) inputStream.readObject();
                    this.setqList(qList);
                    logger.info("the size of qList read from file is " + qList.size());
                } catch (Exception ex) {
                    logger.error("Exception happen when read qList object file");
                } finally {
                    logger.info("read qList information finish");
                    f.delete();
                }
            }
        }
    }

    /**
     * do some information collecting operations
     */
    @Override
    public void close() {
        super.close();
        if(this.productPath != null) {
            System.out.println("start to store qList information");
            File f = this.productPath.resolve(DATA_FILE).toFile();
            if (f.exists()) {
                System.out.println("the qList data saving file has existed, exit directly");
                return;
            }
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(f))) {
                outputStream.writeObject(this.getqList());
            } catch (IOException ex) {
                //ignored
            }
            System.out.println("store qList information finish");
        }
    }

    @Override
    protected boolean isUpdate() {
        int scSize = getSetCoverSize();

        //only run one time at the beginning, update the index and confirm to update
        if (scSize == 0) {
            logger.trace("first run isUpdate() method, just return true");
            si.updateIndex();
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
                si.updateIndex();
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
    public static class Builder{
        private IndexClient indexClient;
        private Deduplicator deduplicator;
        private int stepLen = 3;

        private String mainField = Constant.FT_INDEX_FIELD; //the default lucene index's main field
        private double upBound = 0.15; //set covering algorithm's up bound
        private double lowBound = 0.02;
        private double threshold = 0.99;
        private int sendingCost = 100;

        private String initQuery = "produce";
        private Path productPath;


        public Builder(IndexClient client,Deduplicator dedu){
            indexClient = client;
            deduplicator = dedu;
        }

        public Builder setLowBound(double lowBound) {
            this.lowBound = lowBound;
            return this;
        }

        public Builder setUpBound(double upBound) {
            this.upBound = upBound;
            return this;
        }

        public Builder setMainField(String mainField) {
            this.mainField = mainField;
            return this;
        }

        public Builder setThreshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder setSendingCost(int sendingCost) {
            this.sendingCost = sendingCost;
            return this;
        }

        public Builder setStepLen(int stepLen) {
            this.stepLen = stepLen;
            return this;
        }

        public Builder setInitQuery(String initQuery) {
            this.initQuery = initQuery;
            return this;
        }
        public Builder setProductPath(Path productPath) {
            this.productPath = productPath;
            return this;
        }



        public LinearIncrementalAlgorithm build(){
            LinearIncrementalAlgorithm algo = new LinearIncrementalAlgorithm(this);
            return algo;
        }
    }
}
