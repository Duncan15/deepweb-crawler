package com.cufe.deepweb.algorithm.demo;

import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.algorithm.LinearIncrementalAlgorithm;
import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.dedu.RAMDocIDDedutor;
import com.cufe.deepweb.common.index.IndexClient;
import com.google.common.base.Stopwatch;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 算法验证demo
 */
public class Starter {
    private static final Logger logger = LoggerFactory.getLogger(Starter.class);
    private Starter() {}
    /**
     * 爬虫停止的爬取比例
     */
    private static double threshold;
    /**
     * 爬虫处理的field
     */
    private static String field;
    /**
     * 源索引操作客户端
     */
    private static IndexClient sourceClient;
    /**
     * 目标索引操作客户端
     */
    private static IndexClient targetClient;
    /**
     * 去重器
     */
    private static Deduplicator dedu;
    /**
     *
     * @param args
     * 0 source dir
     * 1 target dir
     */
    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        init(args);
        //默认使用的field为fulltext
        AlgorithmBase algo;
        LinearIncrementalAlgorithm.Builder builder = new LinearIncrementalAlgorithm.Builder(targetClient, dedu);
        builder.setMainField(field);
        algo = builder.build();
        //当爬取比例小于指定比例时，继续
        while (threshold > dedu.getTotal() / (double)sourceClient.getDocSize()) {
            logger.info("HR:{}", dedu.getTotal() / (double)sourceClient.getDocSize());
            //通过算法获取query
            String query = algo.getNextQuery();
            //在源索引中搜索
            Set<Integer> docIDSet = sourceClient.search(field, query);

            //去重
            logger.info("start to dedu");
            Utils.logMemorySize();

            docIDSet = docIDSet.stream().filter(id -> {
                return dedu.add(id);
            }).collect(Collectors.toSet());

            Utils.logMemorySize();
            logger.info("dedu finish");

            //写入索引
            sourceClient.write2TargetIndex(targetClient, docIDSet, Runtime.getRuntime().availableProcessors());
            sourceClient.forceUpdateIndex();
            Utils.logMemorySize();
        }
        exit();
        logger.info("总耗时:{}分钟", stopwatch.elapsed(TimeUnit.MINUTES));
        logger.info("总用词量:{}", algo.getqList().size());
    }

    /**
     * 用于初始化
     * @param args
     */
    private static void init(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("s")
            .longOpt("source-addr")
                .hasArg()
                .required()
                .desc("源索引地址")
                .build()
        );
        options.addOption(Option.builder("t")
            .longOpt("target-addr")
                .hasArg()
                .required()
                .desc("目标索引地址")
                .build()
        );
        options.addOption(Option.builder("f")
            .longOpt("field-name")
                .hasArg()
                .desc("源索引中使用的field")
                .build()
        );
        options.addOption(Option.builder("c")
            .longOpt("chinese")
            .hasArg(false)
            .desc("指定进行中文分词")
            .build()
        );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.error("error when parse commandline args", ex);
            System.exit(1);
        }

        String sourceAddr = cmd.getOptionValue("source-addr");
        IndexClient.Builder sBuilder = new IndexClient.Builder(Paths.get(sourceAddr));
        String targetAddr = cmd.getOptionValue("target-addr");
        IndexClient.Builder tBuilder = new IndexClient.Builder(Paths.get(targetAddr));
        if (cmd.hasOption("chinese")) {
            sBuilder.setAnalyzer(IndexClient.AnalyzerTpye.cn);
            tBuilder.setAnalyzer(IndexClient.AnalyzerTpye.cn);
        }
        if (cmd.hasOption("field-name")) {
            field = cmd.getOptionValue("field-name");
        }
        if (field == null) {
            field = "text";//默认值为text
        }
        threshold = 0.95;//默认值
        sourceClient = sBuilder.setReadOnly().build();
        targetClient = tBuilder.build();
        dedu = new RAMDocIDDedutor();
    }
    private static void exit() {
        try {
            targetClient.close();
            sourceClient.close();
            dedu.close();
        } catch (IOException ex) {
            logger.error("IOException happen when close index", ex);
        }
    }
}
