package com.cufe.deepweb.crawler.branch;

import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.ApiBaseQueryLinkService;
import com.cufe.deepweb.crawler.service.infos.InfoLinkService;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ApiBaseScheduler extends Scheduler {
    private ApiBaseQueryLinkService queryLinkService;
    private InfoLinkService infoLinkService;
    private List<Info> infos;
    public ApiBaseScheduler(AlgorithmBase algo, ApiBaseQueryLinkService apiBaseQueryLinkService, InfoLinkService infoLinkService) {
        super(algo, apiBaseQueryLinkService, infoLinkService, null);
        this.queryLinkService = apiBaseQueryLinkService;
        this.infoLinkService = infoLinkService;
    }
    @Override
    protected void status3(String query) {
        infos = queryLinkService.getInfoLinks(query);
    }

    @Override
    protected ThreadPoolExecutor status4() {
        //use CallerRunsPolicy to provide a simple feedback control mechanism
        ThreadPoolExecutor pool = new ThreadPoolExecutor(Constant.extraConf.getThreadNum(),
                Constant.extraConf.getThreadNum(),
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(100),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        infos.stream().forEach(info -> {
            pool.execute(() -> {
                infoLinkService.downloadAndIndex(info);
            });
        });

        return pool;
    }
}
