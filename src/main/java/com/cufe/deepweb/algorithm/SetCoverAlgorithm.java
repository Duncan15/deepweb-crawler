package com.cufe.deepweb.algorithm;

import com.cufe.deepweb.crawler.Constant;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/*
    set-covering algorithm:该类只关注set-covering算法的过程实现，具体数据以及是否启动新一轮set-covering由具体实现类提供
 */
public abstract class SetCoverAlgorithm extends AlgorithmBase {
    private static Logger logger = LoggerFactory.getLogger(SetCoverAlgorithm.class);
    private String mainField; //the lucene index's main field
    private double upBound; //set covering algorithm's up bound
    private double lowBound;
    private double threshold;
    private int sendingCost;
    /**
     * 建表消耗
     */
    private long buildTableCost;
    /**
     * 当前词的改表消耗
     */
    private long modifyTableCost;
    private int snapshootSize; //the snapshoot size of index when start set covering

    private List<String> termList; //term list collect by set cover
    private Map<String, Integer> df; //the initial df in set cover
    private Map<String, Set<Integer>> newMap; //the new doc set map in set cover,this map would be updated every round
    private Set<Integer> s; //set to store all the downloaded(logically) doc in set cover


    /*
    @param mField 算法使用的索引字段名
    @param ubound set covering的词频上限
    @param lbound set covering的词频下限
    @param thres set covering的终止条件
    @param sCost set covering计算中，每个term的send cost
    */
    public SetCoverAlgorithm(String mField,double ubound,double lbound,double thres,int sCost) {
        mainField = mField;
        upBound = ubound;
        lowBound = lbound;
        threshold = thres;
        sendingCost = sCost;
        buildTableCost = 0;
        snapshootSize = 0;
        termList = new ArrayList<>();
        df = new HashMap<>();
        s = new HashSet<>();
    }

    /*
    当update为true时，直接进行新一轮set covering
    @V@没错这个函数就是在炫技hhh
     */
    private String getNextTerm(boolean update) {
        if(!update){
            String newTerm = generateTerm();
            if(newTerm == null){
                return getNextTerm(true);
            }
            termList.add(newTerm);
            return newTerm;
//            if(newMap == null){ //第一次运行该方法，这个判断条件比较巧妙，需要注意buildMatrix的行为
//                return getNextTerm(true);
//            }else {
//                String newTerm = generateTerm();
//                if(newTerm == null){
//                    return getNextTerm(true);
//                }
//                termList.add(newTerm);
//                return newTerm;
//            }
        }else { //当指定进行新一轮set covering时
            buildMatrix();
            return getNextTerm(false);
        }
    }

    /*
    在set covering开启时建立矩阵
     */
    private void buildMatrix() {
        //以下建立矩阵
        logger.info("set-cover num:{}", termList.size());
        termList.clear();
        //用于记录建立矩阵的时间消耗
        Stopwatch stopwatch = Stopwatch.createStarted();
        s.clear(); //set to store all the downloaded(logically) doc in set cover
        df.clear(); //the initial df in set cover
        newMap = getDocSetMap(mainField,lowBound,upBound); //the new doc set map in set cover,this map would be updated every round
        //从候选词中去除已经选中的query
        if(getqList().size() != 0){
            for(String query:getqList()){
                if(newMap.containsKey(query)){
                    newMap.remove(query);
                }
            }
        }
        for(Map.Entry<String,Set<Integer>> entry : newMap.entrySet()){
            df.put(entry.getKey(),entry.getValue().size());
        }
        buildTableCost = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        snapshootSize = getDocSize();
    }
    /*
    当本轮set covering无法继续进行下去时，返回null
    出现以下情况，set covering无法继续进行：
    1.set covering达到预定当threshold
    2.set covering已经把所有候选词的new减少到0
    */
    private String generateTerm() {
        //以下进入set cover主流程
        if (s.size() < threshold*snapshootSize) {
            //以下进入每一轮set cover流程
            Stopwatch stopwatch = Stopwatch.createStarted();//计算消耗
            Set<String> candidate = new HashSet<>(); //用于存放new/cost相同的词
            String query = null; //the seleted query in this round
            double maxRate = 0.0; //the known biggest new/cost value
            int maxDF = 0; //the known biggest DF value
            //选择当前new/cost值最大的词
            for(Map.Entry<String, Set<Integer>> entry: newMap.entrySet()){
                if(entry.getValue().size()!=0) { //过滤在之前的round中new已经减少到0的词
                    double curRate = entry.getValue().size()/(double)(df.get(entry.getKey())+sendingCost); //new/cost=new/(df+100)
                    if(curRate > maxRate){ //当前词new/cost大于已知值时，更新最大值，同时清空candidate
                        maxRate = curRate;
                        candidate.clear();
                    }
                    if(curRate >= maxRate){ //当当前词new/cost等于最大值时，加入candidate
                        candidate.add(entry.getKey());
                    }
                }
            }
            //当candidate大于等于1时，选择DF最大的词。这种情况下一定会选出词
            if(candidate.size() >= 1){
                maxDF = 0;
                for(String each : candidate){
                    if(maxDF < df.get(each)){
                        maxDF = df.get(each);
                        query = each;
                    }
                }
            }else { //当candidate等于0时，无词可选，以后也会无词可选，直接退出set covering
                logger.warn("set covering 中出现new全部为0的情况，set covering");
                return null;
            }
            df.remove(query);
            Set<Integer> deleted = new HashSet<>(newMap.get(query));
            s.addAll(deleted);
            for(Map.Entry<String,Set<Integer>> entry : newMap.entrySet()){
                entry.getValue().removeAll(deleted);
            }
            newMap.remove(query);
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

    /*
    供子类使用
     */
    /*
    @return 获取当前setcovering获得的termList的size
    */
    protected final int getSetCoverSize() {
        return termList.size();
    }
    /*
    @return 本轮setcovering的建表消耗
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
    /*
    供子类实现
     */
    protected abstract boolean isUpdate();
    protected abstract Map<String, Set<Integer>> getDocSetMap(String field,double low,double up);
    protected abstract int getDocSize();
}
