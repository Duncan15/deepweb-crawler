package com.cufe.deepweb.crawler.branch;

import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.orm.model.Current;
import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.common.orm.Orm;
import com.cufe.deepweb.crawler.service.infos.InfoLinkService;
import com.cufe.deepweb.crawler.service.querys.QueryLinkService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.*;
import java.util.concurrent.*;

/**
 * main scheduler thread, be responsible for produce queryLinks and manage crawler status
 */
public abstract class Scheduler extends Thread{
    private static Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private AlgorithmBase algo;
    private InfoLinkService infoLinkService;
    private Sql2o sql2o;
    protected BlockingDeque msgQueue;
    protected QueryLinkService queryLinkService;
    protected ThreadFactory threadFactory;

    /**
     * the status keeper, which is specified to maintain the status
     */
    private ReactiveStatusKeeper keeper;
    public Scheduler(AlgorithmBase algo, QueryLinkService queryLinkService, InfoLinkService infoLinkService, BlockingDeque msgQueue) {
        super("scheduler_thread");
        this.algo = algo;
        this.queryLinkService = queryLinkService;
        this.infoLinkService = infoLinkService;
        this.msgQueue = msgQueue;
        this.sql2o = Orm.getSql2o();
        this.keeper = new ReactiveStatusKeeper();

        //configure the download thread pool's threadFactory
        threadFactory = new ThreadFactoryBuilder()
            .setUncaughtExceptionHandler((thread, exception) -> {
                logger.error("error happen in consume thread", exception);
            })
            .setNameFormat("consume_thread_%s")
            .build();

    }
    @Override
    public void run() {
        logger.info("start the produce thread");
        keeper.start();//start the status keeper
        if(Constant.round == 0) {//when round = 0, it need to infer initial query
            if (this.init() == 0) {
                System.exit(1);
            }
        }

        while (this.isContinue()) {
            this.round();
        }
        System.exit(0);
    }

    /**
     * the initial method used to detect and use the initial query by accessing the search link,
     * if can't access the search link without parameters, use the index.html of the target site inside.
     * @return
     */
    private int init() {
        logger.info("start initial process");
        WebBrowser browser = this.queryLinkService.getWebBrowser();

        String prefix = "";
        if (this instanceof UrlBaseScheduler) {
            prefix = Constant.urlBaseConf.getPrefix();
        } else if (this instanceof ApiBaseScheduler) {
            prefix = Constant.apiBaseConf.getPrefix();
        } else if (this instanceof JsonBaseScheduler) {
            prefix = Constant.jsonBaseConf.getPrefix();
        }

        //firstly select the search link without parameters
        Optional<String> contentOp = browser.getPageContent(prefix);
        logger.info("select the search page:{} to generate initial query", prefix);
        if(!contentOp.isPresent() || contentOp.get().trim().equals("")) {
            logger.info("can't get the search page, select the index page:{} to generate initial query", Constant.webSite.getIndexUrl());
            //if can't get content from search link without parameters, use the index.html of the target site inside
            contentOp = browser.getPageContent(Constant.webSite.getIndexUrl());
            if((!contentOp.isPresent() || contentOp.get().trim().equals("")) && !StringUtils.isBlank(Constant.extraConf.getLoginUrl())) {
                logger.info("can't get the index page, select the login page:{} to generate initial query", Constant.extraConf.getLoginUrl());
                contentOp = browser.getPageContent(Constant.extraConf.getLoginUrl());
            }
        }

        if (!contentOp.isPresent() || contentOp.get().trim().equals("")) {
            logger.error("fail to generate initial query, return 0");
            return 0;
        }

        logger.trace("content is " + contentOp.get());
        String[] terms = NlpAnalysis.parse(contentOp.get()).toString().split(",");
        Set<String> deduSet = new HashSet<>();
        Random r = new Random(System.currentTimeMillis());
        for(int i = 0; i < terms.length; i++) {
            String t = terms[r.nextInt(terms.length)].trim();
            //specified for ansj_seg split word
            if(t.contains("/")) {
                t = t.substring(0, t.indexOf("/"));
            }

            //if this term has been used or this term is an empty string, jump
            if(t.length() == 0 || !deduSet.add(t)) {
                i--;
                continue;
            }
            logger.info("initial query is " + t);
            algo.setInitQuery(t);
            int num = this.round();
            if(num != 0) {
                logger.info("initiate success");
                return num;
            }
            Constant.round = 0;
        }
        logger.info("initiate fail");
        return 0;
    }
    /**
     * a crawling round in the loop
     * @return the downloaded document number of current round
     */
    private int round() {
        int sLinkNum = 0;//record the return value


        //status1: deal with the database initialization
        //tag: round initiation
        keeper.roundInit();


        //status2
        //tag: term inference
        keeper.fixStatus(1,2);
        logger.info("start the M2status");
        //status2: generate the query term
        String curQuery = algo.getNextQuery();
        if (curQuery == null) {
            logger.info("can't generate more queries, exit");
            System.exit(0);
        }
        logger.info("this turn's query is {}",curQuery);

        //status3
        //tag: queryLink generation
        keeper.fixStatus(2,3);
        logger.info("start the M3status");
        //status3: get all the queryLinks
        status3(curQuery);

        //status4
        //tag: infoLink download
        keeper.fixStatus(3,4);
        logger.info("start the M4status");

        ThreadPoolExecutor threadPool = status4();
        threadPool.shutdown();

        //loop here until all the thread in thread pool exit
        int stopCount = 20;//a flag to indicate whether to force stop the thread pool
        while (true) {
            try {
                //most of the situation, the thread pool would close after the following block, and jump out the while loop
                if (threadPool.awaitTermination(Constant.extraConf.getThreadNum(), TimeUnit.SECONDS)) {
                    break;
                }

                //but sometimes some thread would block in the net IO and wouldn't wake up
                //I also don't know why, but it did exist
                //so need to force close the thread pool in the following clauses
                if (threadPool.getActiveCount() < threadPool.getCorePoolSize() / 2) {
                    logger.info("activeCount:{}, poolSize:{}", threadPool.getActiveCount(), threadPool.getCorePoolSize());
                    stopCount--;
                    if(stopCount <= 0) {
                        threadPool.shutdownNow();
                        break;
                    }
                }
            } catch (InterruptedException ex) {
                logger.error("interrupted when wait for thread pool");
            }
        }
        sLinkNum = keeper.dynamicUpdate();
        keeper.fixStatus(4,0);
        return sLinkNum;
    }
    /**
     * detect whether to continue to crawl
     * detect rule：the successful download number of  latest 10 round is lower than 10,
     * if true , return false and stop to crawl
     * @return
     */
    private boolean isContinue() {
        List<Integer> sLinkNumList = null;
        try (Connection conn = sql2o.open()) {
            //get the latest 10 round's successful download number
            String sql = "select sLinkNum from status where webId=:webID and type=:type order by statusId desc limit 10";
            sLinkNumList = conn.createQuery(sql).addParameter("webID", Constant.webSite.getWebId())
                    .addParameter("type",Constant.STATUS_TYPE_INFO)
                    .executeScalarList(Integer.class);
        }
        int totalNum = 0;//sum all the num
        if (!sLinkNumList.isEmpty()) {
            for (Integer each : sLinkNumList) {
                totalNum += each;
            }
        }

        //only when the all-in algorithm is not used, the above calculation is valid
        if (sLinkNumList.size() == 10 && totalNum <= 1 && Constant.ALL_IN_NUM == 0) {
            return false;
        }
        return true;
    }



    protected abstract void status3(String query);
    protected abstract ThreadPoolExecutor status4();


    /**
     * ReactiveStatusKeeper is used to record some status which should be record instantaneously
     */
    private class ReactiveStatusKeeper extends Thread {
        public ReactiveStatusKeeper() {
            super("reactiveStatusKeeper");
        }

        /**
         * status operations for initiating a new round
         */
        public synchronized void roundInit() {
            try (Connection conn = sql2o.open()) {

                String sql;
                //if at the initial time, round = 0, increase it
                //if restart from a stop task, give up the last round and increase the round number
                Constant.round++;
                infoLinkService.reset();
                queryLinkService.reset();
                lastFInfoLink = 0;
                lastSInfoLink = 0;
                //update the row corresponding to the webID in database's current table
                sql = "update current set M1status =:M1status, M2status =:M2status, M3status =:M3status, M4status =:M4status, round =:round where webId =:webID";
                conn.createQuery(sql)
                        .addParameter("M1status", Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M2status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M3status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M4status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("round", Constant.round + "")
                        .addParameter("webID",Constant.webSite.getWebId())
                        .executeUpdate();

                logger.info("start new round: {}", Constant.round);
                this.fixStatus(0,1);
                logger.info("start the M1status");

                sql = "insert into status(webId,round,type,fLinkNum,sLinkNum)" +
                        "values(:webID,:round,:type,:fLinkNum,:sLinkNum)";
                conn.createQuery(sql)
                        .addParameter("webID", Constant.webSite.getWebId())
                        .addParameter("round", Constant.round + "")
                        .addParameter("type", Constant.STATUS_TYPE_INFO)
                        .addParameter("fLinkNum", 0)
                        .addParameter("sLinkNum", 0)
                        .executeUpdate();
                conn.createQuery(sql)
                        .addParameter("webID", Constant.webSite.getWebId())
                        .addParameter("round", Constant.round + "")
                        .addParameter("type", Constant.STATUS_TYPE_QUERY)
                        .addParameter("fLinkNum", 0)
                        .addParameter("sLinkNum", 0)
                        .executeUpdate();

            }
        }
        /**
         * change the current status of current table
         * here the performance is not the key point, so don't care the number of sql to be executed
         * @param pre 1-4
         * @param cur 1-4
         */
        public void fixStatus(int pre, int cur) {
            try (Connection conn = sql2o.open()) {
                if (1 <= pre && pre <= 4) {
                    String preStr = "M"+pre+"status";
                    //only change the status value here
                    String sql = "update current set " + preStr +" =:MpreStatus, round=:round where webId =:webID";
                    conn.createQuery(sql)
                            .addParameter("MpreStatus",Constant.CURRENT_STATUS_DONE)
                            .addParameter("round", Constant.round+"")
                            .addParameter("webID", Constant.webSite.getWebId())
                            .executeUpdate();
                }
                if (1 <= cur && cur <= 4) {
                    String curStr = "M"+cur+"status";
                    String sql = "update current set " + curStr +" =:McurStatus, round=:round where webId =:webID";
                    conn.createQuery(sql)
                            .addParameter("McurStatus",Constant.CURRENT_STATUS_ACTIVE)
                            .addParameter("round", Constant.round+"")
                            .addParameter("webID", Constant.webSite.getWebId())
                            .executeUpdate();
                }

                //get the newest value from current table
                Constant.current = conn.createQuery("select * from current where webId=:webID")
                        .addParameter("webID",Constant.webSite.getWebId())
                        .executeAndFetchFirst(Current.class);
            }
        }

        /**
         * the number of different status's info link at the last update
         */
        private int lastSInfoLink = 0;
        private int lastFInfoLink = 0;
        /**
         * status update operations for change the status in db dynamically, this method would be invoked by scheduler and current class
         * @return
         */
        public synchronized int dynamicUpdate() {
            int sLinkNum = 0;
            try (Connection conn = sql2o.open()) {
                conn.setRollbackOnException(true);
                String sql = null;

                //update the last round's fLinkNum and sLinkNum in database's status table
                sql = "update status set fLinkNum =:fLinkNum, sLinkNum =:sLinkNum where webId =:webID and type =:type and round=:round";

                //update the last round's fLinkNum and sLinkNum in database's status table
                int fLinkNum = queryLinkService.getFailedLinkNum();
                sLinkNum = queryLinkService.getTotalLinkNum() - queryLinkService.getFailedLinkNum();
                logger.trace("fLinkNum:{},sLinkNum:{},totalLinkNum:{}", fLinkNum, sLinkNum, queryLinkService.getTotalLinkNum());
                conn.createQuery(sql)
                        .addParameter("fLinkNum", fLinkNum)
                        .addParameter("sLinkNum", sLinkNum)
                        .addParameter("webID", Constant.webSite.getWebId())
                        .addParameter("type", Constant.STATUS_TYPE_QUERY)
                        .addParameter("round", Constant.round + "")
                        .executeUpdate();

                fLinkNum = infoLinkService.getFailedLinkNum();
                sLinkNum = infoLinkService.getTotalLinkNum() - infoLinkService.getFailedLinkNum();
                sLinkNum = sLinkNum > 0 ? sLinkNum : 0;
                conn.createQuery(sql)
                        .addParameter("fLinkNum", fLinkNum)
                        .addParameter("sLinkNum", sLinkNum)
                        .addParameter("webID", Constant.webSite.getWebId())
                        .addParameter("type", Constant.STATUS_TYPE_INFO)
                        .addParameter("round", Constant.round + "")
                        .executeUpdate();

                //update current table's SampleData_sum by adding sLinkNum to it
                sql = "update current set SampleData_sum = SampleData_sum +:sLinkNum where webId =:webID";

                conn.createQuery(sql)
                        .addParameter("sLinkNum", sLinkNum - lastSInfoLink)
                        .addParameter("webID", Constant.webSite.getWebId())
                        .executeUpdate();
                lastSInfoLink = sLinkNum;
                lastFInfoLink = fLinkNum;
            } catch (Sql2oException ex) {
                logger.error("sql2o error when update db", ex);
            }
            return sLinkNum;
        }


        @Override
        public void run() {
            while (true) {
                /**
                 * record SampleData_sum in current table, fLinkNum and sLinkNum in status table instantaneously
                 */
                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException ex) {
                    //ignored
                }
                this.dynamicUpdate();
                logger.trace("update");

            }
        }
    }

}
