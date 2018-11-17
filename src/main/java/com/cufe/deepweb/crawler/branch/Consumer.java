package com.cufe.deepweb.crawler.branch;
import com.cufe.deepweb.crawler.service.InfoLinkService;
import com.cufe.deepweb.crawler.service.QueryLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public final class Consumer extends Thread {
    private static Logger logger = LoggerFactory.getLogger(Consumer.class);
    private QueryLinkService queryLinkService;
    private InfoLinkService infoLinkService;
    private BlockingDeque msgQueue;

    /**
     * 初始化消费线程
     * @param queryLinkService
     * @param infoLinkService
     * @param msgQueue
     */
    public Consumer(QueryLinkService queryLinkService, InfoLinkService infoLinkService, BlockingDeque msgQueue) {
        this.queryLinkService = queryLinkService;
        this.infoLinkService = infoLinkService;
        this.msgQueue = msgQueue;

    }
    @Override
    public void run(){
        logger.info("start the consume thread");
        while (!Thread.currentThread().isInterrupted()) {//如果interrupt信号置为true，退出循环
            try {
                Object o = msgQueue.take();//如果阻塞过程中收到interrupt，退出循环
                if (o instanceof String) {
                    String link = (String)o;
                    if (queryLinkService.isQueryLink(link)) {//如果是分页链接
                        this.consumeQueryLink(link);
                    }else {//如果是数据链接
                        this.consumeInfoLink(link);
                    }
                }
            }catch (InterruptedException ex) {
                logger.error("InterruptedException happen, jump out the loop", ex);
                break;
            }
        }
        logger.info("exit consume thread");

    }


    /**
     * 消费QueryLink（获取分页页面上的信息链接，重新存入消息队列）
     * @param queryLink
     */
    private void consumeQueryLink(String queryLink) {
        List<String> infoLinks = queryLinkService.getInfoLinks(queryLink);
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
        if (infoLinkService.exists(infoLink)) {//如果已下载过，直接返回
            return;
        }
        infoLinkService.downloadAndIndex(infoLink, infoLinkService.getFileAddr(infoLink));
    }
}
