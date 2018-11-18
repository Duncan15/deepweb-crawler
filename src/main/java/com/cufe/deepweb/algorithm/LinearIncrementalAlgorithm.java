package com.cufe.deepweb.algorithm;

import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.index.IndexClient;
import com.cufe.deepweb.common.dedu.Deduplicator;

import java.util.*;

/*
    线性搜索算法具体实现类：该类负责具体数据提供以及是否进行下一轮set-covering的策略判断
    该算法的扩展算法可直接实现isUpdate方法来完成
*/
public class LinearIncrementalAlgorithm extends SetCoverAlgorithm {
    private long newV; //store the new from the beginning of this round of set covering
    private long costV; //store the costV from the beginning of this round of set covering
    private IndexClient si;//用于操作索引
    private Deduplicator dedu;//用于获取真实下载时的参数
    private int stepLen;//线性搜索的步长
    private Queue<Double> stepQueue;
    private LinearIncrementalAlgorithm(Builder builder) {
        super(builder.mainField,builder.upBound,builder.lowBound,builder.threshold,builder.sendingCost);
        stepLen = builder.stepLen;
        stepQueue = new ArrayDeque<>(stepLen);
        si = builder.indexClient;
        dedu = builder.deduplicator;
    }

    /*
    以下实现供父类使用
     */
    @Override
    protected boolean isUpdate() {
        int scSize = getSetCoverSize();
        if (scSize == 0)return true;//当该函数第一次运行时，应该返回true，以便先进行一次set covering
        if (scSize == 1) {//size为1代表进行新一轮的setcovering，则应该清空stepqueue
            stepQueue.clear();
        }
        newV = dedu.getNew();
        costV = getBuildTableCost() + dedu.getCost();
        if(stepQueue.size() < stepLen){//先记录前n个词的quality
            stepQueue.offer(newV/(double)costV);
            return false;
        } else {
            double curQuality = newV/(double)costV;
            double preQuality = stepQueue.poll();
            if (curQuality >= preQuality) {
                stepQueue.offer(curQuality);
                return false;
            } else {
                si.updateIndex();//当返回true时，set-covering进行新一轮更新，要先updateIndex
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

        private String mainField = Constant.FT_INDEX_FIELD; //the lucene index's main field
        private double upBound = 0.15; //set covering algorithm's up bound
        private double lowBound = 0.02;
        private double threshold = 0.99;
        private int sendingCost = 100;

        private String initQuery = "produce";


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

        public LinearIncrementalAlgorithm build(){
            LinearIncrementalAlgorithm algo = new LinearIncrementalAlgorithm(this);
            algo.setInitQuery(initQuery);
            return algo;
        }
    }
}
