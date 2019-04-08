package com.cufe.deepweb.crawler.branch;

import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.infos.InfoLinkService;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.JsonBaseQueryLinkService;
import com.cufe.deepweb.crawler.service.querys.QueryLinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonBaseScheduler extends Scheduler {
    private Logger logger = LoggerFactory.getLogger(JsonBaseScheduler.class);
    private JsonBaseQueryLinkService queryLinkService;
    private InfoLinkService infoLinkService;
    private QueryLinkService.QueryLinks queryLinks;

    public JsonBaseScheduler(AlgorithmBase alg, JsonBaseQueryLinkService queryLinkService, InfoLinkService infoLinkService, BlockingDeque msgQueue) {
        super(alg, queryLinkService, infoLinkService, msgQueue);
        this.queryLinkService = queryLinkService;
        this.infoLinkService = infoLinkService;
    }
    @Override
    protected void status3(String query) {
        queryLinks = queryLinkService.getQueryLinks(query);
    }

    @Override
    protected ThreadPoolExecutor status4() {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                Constant.extraConf.getThreadNum(),
                Constant.extraConf.getThreadNum(),
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(),//set the size of thread queue to infinity
                threadFactory
        );
        if (queryLinks.getPageNum() <= 0) return pool;
        AtomicInteger produceCounter = new AtomicInteger(0);
        Runnable producer = () -> {
          String link = null;
          while ((link = queryLinks.next()) != null) {
              queryLinkService.getInfoLinks(link).forEach(info -> msgQueue.offer(info));
          }
          produceCounter.incrementAndGet();
          logger.trace("producer:{} exit", Thread.currentThread().getName());

        };
        for (int i = 0; i < 5; i++) {
            new Thread(producer).start();
        }
        for (int i = 0; i < Constant.extraConf.getThreadNum(); i++) {
            pool.execute(() -> {
                while (true) {
                    Info info = (Info) msgQueue.poll();
                    if (info != null) {
                        infoLinkService.downloadAndIndex(info);
                        continue;
                    } else if (produceCounter.get() < 5) {
                        try {
                            Thread.sleep(3_000);
                        } catch (InterruptedException ex) {
                            //ignored
                        }

                        logger.trace("finished producer number {}", produceCounter.get());
                        continue;
                    } else {
                        break;
                    }
                }
            });
        }
        return pool;
    }
}
