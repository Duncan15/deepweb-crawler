package com.cufe.deepweb.crawler.branch;

import com.cufe.deepweb.Constant;
import com.cufe.deepweb.common.orm.model.Current;
import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.common.orm.Orm;
import com.cufe.deepweb.crawler.service.InfoLinkService;
import com.cufe.deepweb.crawler.service.QueryLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.concurrent.BlockingDeque;

/**
 * 生产者线程，主要工作是生产分页链接和把握爬虫状态
 */
public final class Producer extends Thread{
    private static Logger logger = LoggerFactory.getLogger(Producer.class);
    private AlgorithmBase algo;
    private QueryLinkService queryLinkService;
    private InfoLinkService infoLinkService;
    private BlockingDeque msgQueue;
    private Sql2o sql2o;
    public Producer(AlgorithmBase algo, QueryLinkService queryLinkService, InfoLinkService infoLinkService, BlockingDeque msgQueue){
        super("producer_thread");
        this.algo = algo;
        this.queryLinkService = queryLinkService;
        this.infoLinkService = infoLinkService;
        this.msgQueue = msgQueue;
        this.sql2o = Orm.getSql2o();
    }
    @Override
    public void run() {
        logger.info("start the produce thread");

        while (this.isContinue()) {
            try (Connection conn = sql2o.open()) {//初始化current表中的对应行
                String sql = "update current set M1status =:M1status, M2status =:M2status, M3status =:M3status, M4status =:M4status where webId =:webID";
                conn.createQuery(sql)
                        .addParameter("M1status", Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M2status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M3status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("M4status",Constant.CURRENT_STATUS_INACTIVE)
                        .addParameter("webID",Constant.webSite.getWebId())
                        .executeUpdate();
            }


            this.fixStatus(0,1);
            logger.info("start the M1status");
            //part1:生成关键词阶段
            String curQuery = algo.getNextQuery();
            logger.info("this turn's query is {}",curQuery);


            this.fixStatus(1,2);
            logger.info("start the M2status");
            //part2:确定分页链接
            List<String> queryLinks = queryLinkService.getQueryLinks(curQuery);
            for (String queryLink : queryLinks) {
                while (true) {//如果阻塞过程中出行InterruptedException，则重试，保证不丢数据
                    try {
                        msgQueue.put(queryLink);
                        break;
                    } catch (InterruptedException ex) {
                        logger.error("InterruptedException happen when put queryLink into msgQueue",ex);
                    }
                }
            }


            this.fixStatus(2,3);
            logger.info("start the M3status");
            //part3:下载链接
            while (msgQueue.size() > 0) {
                try {
                    Thread.sleep(1_1000);
                } catch (InterruptedException ex) {
                    logger.error("InterruptedException happen when sleep", ex);
                }
            }


            this.fixStatus(3,4);
            logger.info("start the M4status");
            //part4:本轮收尾工作

            try (Connection conn = sql2o.open()) {
                //插入status表
                Constant.round++;//增加当前轮次标示
                String sql = "insert into status(webId,round,type,fLinkNum,sLinkNum)" +
                        "values(:webID,:round,:type,:fLinkNum,sLinkNum)";
                conn.createQuery(sql)
                        .addParameter("webID", Constant.webSite.getWebId())
                        .addParameter("round", Constant.round+"")
                        .addParameter("type", Constant.STATUS_TYPE_INFO)
                        .addParameter("fLinkNum", infoLinkService.getFailedLinkNum())
                        .addParameter("sLinkNum", infoLinkService.getTotalLinkNum())
                        .executeUpdate();
                conn.createQuery(sql)
                        .addParameter("webID", Constant.webSite.getWebId())
                        .addParameter("round", Constant.round+"")
                        .addParameter("type", Constant.STATUS_TYPE_QUERY)
                        .addParameter("fLinkNum", queryLinkService.getFailedLinkNum())
                        .addParameter("sLinkNum", queryLinkService.getTotalLinkNum())
                        .executeUpdate();
            }


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
                String sql = "update current set" + preStr +" =:MpreStatus, status=:status where webId =:webID";
                conn.createQuery(sql)
                        .addParameter("MpreStatus",Constant.CURRENT_STATUS_DONE)
                        .addParameter("status", Constant.round+"")
                        .executeUpdate();
            }
            if (1 <= cur && cur <= 4) {
                String curStr = "M"+cur+"status";
                String sql = "update current set" + curStr +" =:McurStatus where webId =:webID";
                conn.createQuery(sql)
                        .addParameter("McurStatus",Constant.CURRENT_STATUS_ACTIVE)
                        .executeUpdate();
            }
            //获取当前最新的current表内容
            Constant.current = conn.createQuery("select * from current where webId=:webID")
                    .addParameter("webID",Constant.webSite.getWebId())
                    .executeAndFetchFirst(Current.class);
        }
    }
}
