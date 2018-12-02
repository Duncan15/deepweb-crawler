package com.cufe.deepweb.crawler.branch;

import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.orm.model.Current;
import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.common.orm.Orm;
import com.cufe.deepweb.crawler.service.InfoLinkService;
import com.cufe.deepweb.crawler.service.QueryLinkService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 主调度线程，主要工作是生产分页链接和把握爬虫状态
 */
public final class Scheduler extends Thread{
    private static Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private AlgorithmBase algo;
    private QueryLinkService queryLinkService;
    private InfoLinkService infoLinkService;
    private BlockingDeque msgQueue;
    private Sql2o sql2o;
    private ThreadPoolExecutor threadPool;
    public Scheduler(AlgorithmBase algo, QueryLinkService queryLinkService, InfoLinkService infoLinkService, BlockingDeque msgQueue){
        super("producer_thread");
        this.algo = algo;
        this.queryLinkService = queryLinkService;
        this.infoLinkService = infoLinkService;
        this.msgQueue = msgQueue;
        this.sql2o = Orm.getSql2o();

        //配置下载线程池
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setUncaughtExceptionHandler((thread, exception) -> {
                logger.error("消费线程出错", exception);
            })
            .setNameFormat("consume_thread_%s")
            .build();
        threadPool = new ThreadPoolExecutor(
            Constant.webSite.getThreadNum(),
            Constant.webSite.getThreadNum(),
            0,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(),
            threadFactory
        );

    }
    @Override
    public void run() {
        logger.info("start the produce thread");

        while (this.isContinue()) {
            //part1:处理数据库初始化
            try (Connection conn = sql2o.open()) {//初始化current表中的对应行
                String sql = "update current set M1status =:M1status, M2status =:M2status, M3status =:M3status, M4status =:M4status where webId =:webID";
                conn.createQuery(sql)
                        .addParameter("M1status", Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M2status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M3status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M4status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("webID",Constant.webSite.getWebId())
                        .executeUpdate();

                //插入status表
                sql = "update status set fLinkNum =:fLinkNum, sLinkNum =:sLinkNum where webId =:webID and type =:type";
                int fLinkNum = infoLinkService.getFailedLinkNum();
                conn.createQuery(sql)
                    .addParameter("fLinkNum", fLinkNum)
                    .addParameter("sLinkNum", infoLinkService.getTotalLinkNum() - fLinkNum)
                    .addParameter("webID", Constant.webSite.getWebId())
                    .addParameter("type", Constant.STATUS_TYPE_INFO)
                    .executeUpdate();
                fLinkNum = queryLinkService.getFailedLinkNum();
                conn.createQuery(sql)
                    .addParameter("fLinkNum", fLinkNum)
                    .addParameter("sLinkNum", queryLinkService.getTotalLinkNum() - fLinkNum)
                    .addParameter("webID", Constant.webSite.getWebId())
                    .addParameter("type", Constant.STATUS_TYPE_QUERY)
                    .executeUpdate();
                Constant.round++;//增加当前轮次标示
                sql = "insert into status(webId,round,type,fLinkNum,sLinkNum)" +
                    "values(:webID,:round,:type,:fLinkNum,:sLinkNum)";
                conn.createQuery(sql)
                    .addParameter("webID", Constant.webSite.getWebId())
                    .addParameter("round", Constant.round+"")
                    .addParameter("type", Constant.STATUS_TYPE_INFO)
                    .addParameter("fLinkNum", 0)
                    .addParameter("sLinkNum", 0)
                    .executeUpdate();
                conn.createQuery(sql)
                    .addParameter("webID", Constant.webSite.getWebId())
                    .addParameter("round", Constant.round+"")
                    .addParameter("type", Constant.STATUS_TYPE_QUERY)
                    .addParameter("fLinkNum", 0)
                    .addParameter("sLinkNum", 0)
                    .executeUpdate();
            }
            this.fixStatus(0,1);
            logger.info("start the M1status");


            this.fixStatus(1,2);
            logger.info("start the M2status");
            //part2:生成关键词阶段
            String curQuery = algo.getNextQuery();
            logger.info("this turn's query is {}",curQuery);


            this.fixStatus(2,3);
            logger.info("start the M3status");
            //part3:确定分页链接
            List<String> queryLinks = queryLinkService.getQueryLinks(curQuery);
            //正在运行的任务集合
            Set<Future> runSet = new HashSet<>();
            runSet.add(threadPool.submit(() -> {
                queryLinks.forEach(link -> {
                    try {
                        msgQueue.put(link);
                    } catch (InterruptedException ex) {
                        logger.error("InterruptedException happen when put queryLink into msgQueue",ex);
                    }

                });
            }));

            this.fixStatus(3,4);
            logger.info("start the M4status");
            //part4:下载链接
            List runList = new ArrayList(Constant.webSite.getThreadNum());
            do {
                int runNum = threadPool.getActiveCount();
                if (runNum < Constant.webSite.getThreadNum()) {
                    runList.clear();
                    msgQueue.drainTo(runList, Constant.webSite.getThreadNum() - runNum);
                    runList.forEach(o -> {
                        runSet.add(threadPool.submit(() -> {
                            if (o instanceof String) {
                                String link = (String)o;
                                if (queryLinkService.isQueryLink(link)) {//如果是分页链接
                                    this.consumeQueryLink(link);
                                }else {//如果是数据链接
                                    this.consumeInfoLink(link);
                                }
                            }
                        }));
                    });

                }
            } while (Utils.isRun(runSet) || threadPool.getQueue().size() > 0 || msgQueue.size() > 0);
            this.fixStatus(4,0);
        }
        System.exit(0);
    }

    /**
     * 检测是否继续爬取
     * 检测规则为：最近10次爬取所获取的数据链接数量小于10
     * @return
     */
    private boolean isContinue() {
        List<Integer> sLinkNumList = null;
        try (Connection conn = sql2o.open()) {//获取对应webID的最近10个成功爬取链接数
            String sql = "select fLinkNum from status where webId=:webID and type=:type order by statusId desc limit 10";
            sLinkNumList = conn.createQuery(sql).addParameter("webID", Constant.webSite.getWebId())
                    .addParameter("type",Constant.STATUS_TYPE_INFO)
                    .executeScalarList(Integer.class);
        }
        int totalNum = 0;//累计成功链接数
        if (!sLinkNumList.isEmpty()) {
            for (Integer each : sLinkNumList) {
                totalNum += each;
            }
        }
        if (sLinkNumList.size() == 10 && totalNum < 10) {
            return false;
        }
        return true;
    }

    /**
     * 修改current表阶段状态(这里并不是很追求速度，多次sql请求并无大碍)
     * @param pre 1-4
     * @param cur 1-4
     */
    private void fixStatus(int pre, int cur) {
        try (Connection conn = sql2o.open()) {
            if (1 <= pre && pre <= 4) {
                String preStr = "M"+pre+"status";
                //只在此处修改status的值即可
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
            //获取当前最新的current表内容
            Constant.current = conn.createQuery("select * from current where webId=:webID")
                    .addParameter("webID",Constant.webSite.getWebId())
                    .executeAndFetchFirst(Current.class);
        }
    }

    /**
     * 消费QueryLink（获取分页页面上的信息链接，重新存入消息队列）
     * @param queryLink
     */
    private void consumeQueryLink(String queryLink) {
        List<String> infoLinks = queryLinkService.getInfoLinks(queryLink);
        logger.info("consume query link {},get info link num {}", queryLink, infoLinks.size());
        infoLinks.forEach(infoLink -> {
            if (!msgQueue.offer(infoLink)) {//如果不能存入消息队列，则直接在本线程进行消费
                consumeInfoLink(infoLink);
            }
        });
    }

    /**
     * 消费InfoLink（下载信息链接对应的页面，打成索引）
     * @param infoLink
     */
    private void consumeInfoLink(String infoLink) {
        logger.info("consume info link {}", infoLink);
        infoLinkService.downloadAndIndex(infoLink, infoLinkService.getFileAddr(infoLink));
    }

}
