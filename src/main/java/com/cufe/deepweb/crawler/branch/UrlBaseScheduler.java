package com.cufe.deepweb.crawler.branch;

import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.infos.InfoLinkService;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.UrlBaseQueryLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UrlBaseScheduler extends Scheduler {
    private Logger logger = LoggerFactory.getLogger(UrlBaseScheduler.class);
    private InfoLinkService infoLinkService;
    private UrlBaseQueryLinkService queryLinkService;
    private UrlBaseQueryLinkService.QueryLinks queryLinks;

    public UrlBaseScheduler(AlgorithmBase algo, UrlBaseQueryLinkService urlBaseueryLinkService, InfoLinkService infoLinkService, BlockingDeque msgQueue) {
        super(algo, urlBaseueryLinkService, infoLinkService, msgQueue);
        this.queryLinkService = urlBaseueryLinkService;
        this.infoLinkService = infoLinkService;
    }

    @Override
    protected void status3(String query) {
        queryLinks = queryLinkService.getQueryLinks(query);
    }

    @Override
    protected void status4() {
        //only when query link number is bigger than zero, it's necessary to use the thread pool
        if (queryLinks.getPageNum() > 0) {
            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                    Constant.extraConf.getThreadNum(),
                    Constant.extraConf.getThreadNum(),
                    0,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingDeque<>(),//set the size of thread queue to infinity
                    threadFactory
            );

            AtomicInteger produceCounter = new AtomicInteger(0);
            Runnable producer =() -> {
                String link = null;
                int tick = 100;
                while ((link = queryLinks.next()) != null) {

                    //periodically close webclient to explicitly support gc
                    if (tick <= 0) {
                        queryLinkService.clearThreadResource();
                        tick = 100;
                    }
                    try {
                        if (queryLinkService.isQueryLink(link)) {
                            logger.trace(queryLinks.getCounter()+ "");
                            consumeQueryLink(link);
                            tick--;
                        }
                    } catch (Exception ex) {
                        //ignored
                        logger.error("runtime exception happen", ex);
                    }
                }
                produceCounter.incrementAndGet();
                queryLinkService.clearThreadResource();
            };
            for (int i = 0 ; i < 5 ; i++) {
                new Thread(producer).start();
            }

            for (int i = 0 ; i < Constant.extraConf.getThreadNum() ; i++) {
                threadPool.execute(() -> {
                    while (true) {

                        //if here is not null, this is a info link
                        Info info = (Info) msgQueue.poll();
                        if (info != null) {
                            this.consumeInfoLink(info);
                            continue;
                        }
                        if (produceCounter.get() < 5) {
                            continue;
                        }

                        //if can't get info link from message queue and all the produce thread exit,
                        //the thread exit
                        logger.trace("can't get query link, total page num is {}， current counter is {}", queryLinks.getPageNum(), queryLinks.getCounter());
                        break;
                    }

                });
            }
            threadPool.shutdown();

            //loop here until all the thread in thread pool exit
            int stopCount = 3;//a flag to indicate whether to force stop the thread pool
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
        }
    }

    /**
     * consume query link
     * get info links from the page corresponding to the query link, and restore them into message queue
     * @param queryLink
     */
    private void consumeQueryLink(String queryLink) {
        List<Info> infos = queryLinkService.getInfoLinks(queryLink);
        if (infos.size() == 0) return;
        infos.forEach(info -> {
            //if can't push info links into message queue
            //maybe because the message queue is full(this situation is hard to happen, just possible)
            //directly consume the info links in current thread
            if (!msgQueue.offer(info)) {
                consumeInfoLink(info);
            }
        });
    }

    /**
     * consume info link
     * download the page corresponding to the info link into directory, and build the page content into index
     * @param info
     */
    private void consumeInfoLink(Info info) {
        logger.trace("consume info link {}", info.getUrl());
        infoLinkService.downloadAndIndex(info);
    }
}
