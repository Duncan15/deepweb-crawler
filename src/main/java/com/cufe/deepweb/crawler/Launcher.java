package com.cufe.deepweb.crawler;

import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.algorithm.LinearIncrementalAlgorithm;
import com.cufe.deepweb.crawler.branch.Producer;
import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.http.client.ApacheClient;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.common.index.IndexClient;
import com.cufe.deepweb.common.orm.model.Current;
import com.cufe.deepweb.common.orm.model.Pattern;
import com.cufe.deepweb.common.orm.model.WebSite;
import com.cufe.deepweb.crawler.service.InfoLinkService;
import com.cufe.deepweb.crawler.branch.Consumer;
import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.dedu.RAMMD5Dedutor;
import com.cufe.deepweb.common.orm.Orm;
import com.cufe.deepweb.crawler.service.QueryLinkService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public final class Launcher {
    private static Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static IndexClient indexClient;//索引客户端
    private static Deduplicator dedu;//去重器
    private static WebBrowser webBrowser;//web浏览器
    private static CusHttpClient httpClient;//http客户端
    private static BlockingDeque msgQueue;
    private static final int queueSize = 10_000;
    /**
     * msg备份文件文件名
     */
    private static final String MSG_DATA_NAME = "msg.dat";
    private static ExecutorService threadPool;//消费线程池
    private Launcher() { }
    public static void main(final String[] args) {
        init(args);
        Runtime.getRuntime().addShutdownHook(new Exitor());
        //初始化策略算法，该算法只会使用在producer线程中
        AlgorithmBase alg = new LinearIncrementalAlgorithm.Builder(indexClient,dedu).build();
        //初始化查询链接处理服务
        QueryLinkService queryLinkService = new QueryLinkService(webBrowser);
        //初始化信息链接处理服务
        InfoLinkService infoLinkService = new InfoLinkService(httpClient, indexClient, dedu);
        //初始化线程
        Producer producer = new Producer(alg, queryLinkService, infoLinkService, msgQueue);
        for (int i = 0; i < Constant.webSite.getThreadNum() ; i++) {
            Consumer consumer = new Consumer(queryLinkService, infoLinkService, msgQueue);
            threadPool.execute(consumer);
        }
        producer.start();
    }

    /**
     * 注:爬虫不做任何数据库的初始化插入操作，启动爬虫时应该保证数据库各项配置的完整性
     *     0   webID
     *     1   jdbcURL
     *     2   userName
     *     3   password
     * @param args
     */
    private static void init(final String[] args) {
        //config mysql
        HikariConfig hikariConfig = new HikariConfig("/orm/hikari.properties");
        String webIDStr = Utils.getValue(args,0);
        int webID = Integer.parseInt(webIDStr);//webID
        String jdbcURL = Utils.getValue(args, 1);
        String userName = Utils.getValue(args, 2);
        String password = Utils.getValue(args, 3);
        jdbcURL = jdbcURL != null ? jdbcURL : "jdbc:xxx";
        userName = userName != null ? userName : "root";
        password = password != null ? password : "1215287416";
        logger.info("crawler start at {}", new Date().toString());
        logger.info("configure cmd param with jdbcURL:{},userName:{},password:{}", jdbcURL, userName, password);
        hikariConfig.setJdbcUrl(jdbcURL);
        hikariConfig.setUsername(userName);
        hikariConfig.setPassword(password);
        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Orm.setSql2o(new Sql2o(ds));

        //config website info
        Sql2o sql2o=Orm.getSql2o();
        try(Connection conn=sql2o.open()){
            String sql = "select * from website where webId=:webID";
            Constant.webSite = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(WebSite.class);
            sql = "select * from current where webId=:webID";
            Constant.current = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(Current.class);
            sql = "select * from pattern where webId=:webID";
            List<Pattern> patterns = conn.createQuery(sql).addParameter("webID", webID).executeAndFetch(Pattern.class);
            patterns.forEach(pattern -> {
                if (Constant.FT_INDEX_FIELD.equals(pattern.getPatternName())) return;//跳过全文索引的pattern，因为全文索引在爬虫中默认执行
                Constant.patternMap.put(pattern.getPatternName(),pattern.getXpath());
            });
            sql = "select round from status where webId=:webID";
            List<String> rounds = conn.createQuery(sql).addParameter("webID", Constant.webSite.getWebId()).executeScalarList(String.class);
            for (String round : rounds) {//将round设置为数据库中的最大值
                int ro = Integer.parseInt(round);
                if (Constant.round < ro) {
                    Constant.round = ro;
                }
            }
            Constant.round++;//最大值+1
        }
        if(Constant.webSite == null || Constant.current == null){
            logger.error("webID所对应的webSite信息无法找到，程序退出");
            System.exit(1);
        }
        //检测配置项
        String workFilePath = Constant.webSite.getWorkFile();
        if (StringUtils.isBlank(workFilePath)) {
            logger.error("工作路径不能为空");
            System.exit(1);
        } else {
            File f = new File(workFilePath);
            if (!f.exists() || !f.isDirectory() || !f.canWrite()) {//如果文件不存在 or 如果不是文件夹 or 如果不可写
                logger.error("工作路径必须是已存在的文件夹，且执行用户拥有写权限");
                System.exit(1);
            }
        }

        //配置消息队列
        msgQueue = new LinkedBlockingDeque(queueSize);
        File f = Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR, MSG_DATA_NAME).toFile();
        List<String> msgList = null;
        //如果文件存在，将列表读入内存
        if (f.exists()) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
                msgList = (List<String>)inputStream.readObject();
            } catch (Exception ex) {
                logger.error("Exception happen when read msg object file");
            } finally {
                f.delete();
            }
        }
        //将列表中的数据插入消息队列
        if (msgList != null) {
            msgList.forEach(link -> {
                msgQueue.offer(link);
            });
        }

        //配置索引客户端
        indexClient = new IndexClient.Builder(Paths.get(Constant.webSite.getWorkFile(),Constant.FT_INDEX_ADDR)).build();
        //配置去重器
        dedu = new RAMMD5Dedutor(Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR));
        //配置模拟器
        webBrowser = new HtmlUnitBrowser.Builder().build();
        if (!StringUtils.isBlank(Constant.webSite.getLoginUrl())) {//如果有配置登录链接，则表示必须登录才能爬取
            if (!webBrowser.login(Constant.webSite.getLoginUrl(),Constant.webSite.getUserName(),Constant.webSite.getPassword(),Constant.webSite.getUserParam(),Constant.webSite.getPwdParam(),Constant.webSite.getSubmitXpath())) {
                logger.error("无法登录，请检查登录参数设置是否正确");
                System.exit(1);
            }
        }
        //配置http客户端
        httpClient = new ApacheClient.Builder()
                .setCookieManager(((HtmlUnitBrowser)webBrowser).getCookieManager())
                .build();


        //配置下载线程池
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((thread, exception) -> {
            logger.error("消费线程出错", exception);
        })
                .setNameFormat("consume_thread_%s")
                .build();
        threadPool = Executors.newFixedThreadPool(Constant.webSite.getThreadNum(), threadFactory);

    }
    private static class Exitor extends Thread {
        private Logger logger = LoggerFactory.getLogger(Exitor.class);
        @Override
        public void run() {
            logger.info("start the exit thread");
            threadPool.shutdown();
            while (!threadPool.isTerminated()) {}//等待线程池完全退出
            //将msgQueue的内容写入文件
            List<String> msgList = new ArrayList<>();
            msgQueue.forEach( link -> {
                msgList.add((String) link);
            });
            File f = Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR, MSG_DATA_NAME).toFile();
            if (f.exists()) {
                f.delete();
            }
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(f))) {
                outputStream.writeObject(msgList);
            } catch (IOException ex) {
                logger.error("IOException happen when write msg object to file", ex);
            }

            try {
                httpClient.close();
                dedu.close();//数据保存
            } catch (IOException ex) {
                //nothing to do
            }

        }
    }
}
