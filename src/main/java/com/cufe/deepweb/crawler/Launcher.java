package com.cufe.deepweb.crawler;

import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.algorithm.LinearIncrementalAlgorithm;
import com.cufe.deepweb.common.http.simulate.HtmlUnitFactory;
import com.cufe.deepweb.crawler.branch.Scheduler;
import com.cufe.deepweb.common.http.client.ApacheClient;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.common.index.IndexClient;
import com.cufe.deepweb.common.orm.model.Current;
import com.cufe.deepweb.common.orm.model.Pattern;
import com.cufe.deepweb.common.orm.model.WebSite;
import com.cufe.deepweb.crawler.service.InfoLinkService;
import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.dedu.RAMMD5Dedutor;
import com.cufe.deepweb.common.orm.Orm;
import com.cufe.deepweb.crawler.service.QueryLinkService;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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

/**
 * the launcher class of crawler
 * the crawler begins from here
 */
public final class Launcher {
    private static Logger logger = LoggerFactory.getLogger(Launcher.class);
    /**
     * the index client for building local index
     */
    private static IndexClient indexClient;
    /**
     * the deduplicator for deduplicating the repeat infoLink
     */
    private static Deduplicator dedu;
    /**
     * the web browser for dealing with queryLink
     */
    private static WebBrowser webBrowser;
    /**
     * the HTTP client for downloading the page corresponding to the infoLink
     */
    private static CusHttpClient httpClient;
    /**
     * the message queue for mantain all the link when running
     */
    private static BlockingDeque msgQueue;
    /**
     * the file name of data file for storing message queue's left data when restarting or stopping
     */
    private static final String MSG_DATA_NAME = "msg.dat";
    private Launcher() { }

    /**
     * everything begin here
     * @param args
     */
    public static void main(final String[] args) {
        //initialize the underline resource
        init(args);

        //register the hook to clear resource when crawler restart or stop
        Runtime.getRuntime().addShutdownHook(new Exitor());

        //initialize the strategy algorithm, this algorithm would only be used in scheduler thread
        AlgorithmBase alg = new LinearIncrementalAlgorithm.Builder(indexClient, dedu).build();

        //initialize the service to deal with queryLinks
        QueryLinkService queryLinkService = new QueryLinkService(webBrowser, dedu);

        //initialize the service to deal with infoLinks
        InfoLinkService infoLinkService = new InfoLinkService(httpClient, indexClient);

        //initialize the scheduler thread
        Scheduler scheduler = new Scheduler(alg, queryLinkService, infoLinkService, msgQueue);

        //when scheduler thread start to run, everything startup
        scheduler.start();
    }

    /**
     * @note: the crawler don't do some things about inserting initial data into database,
     * when this program start to run, every configuration in database should be confirmed to be finished
     * in other word, it should have a initialize step when the user finish to configure in the web's front end
     * @param args
     *        [0] webID
     *        [1] jdbcURL
     *        [2] userName
     *        [3] password
     */
    private static void init(final String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("i")
            .longOpt("web-id")
            .hasArg()
            .required()
            .desc("the specified website ID")
            .build()
        );
        options.addOption(Option.builder("l")
            .longOpt("jdbc-url")
            .hasArg()
            .required()
            .desc("the specified JDBC URL")
            .build()
        );
        options.addOption(Option.builder("u")
            .longOpt("username")
            .hasArg()
            .required()
            .desc("the specified database username")
            .build()
        );
        options.addOption(Option.builder("p")
            .longOpt("password")
            .hasArg()
            .required()
            .desc("the specified database password")
            .build()
        );
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            logger.error("error happen when parse commandline args", ex);
            System.exit(1);
        }

        //config mysql
        HikariConfig hikariConfig = new HikariConfig("/orm/hikari.properties");
        String webIDStr = cmd.getOptionValue("web-id");
        int webID = Integer.parseInt(webIDStr);//webID
        String jdbcURL = cmd.getOptionValue("jdbc-url");
        String userName = cmd.getOptionValue("username");
        String password = cmd.getOptionValue("password");
        logger.info("crawler start");
        logger.info("configure cmd param with jdbcURL:{},userName:{},password:{}", jdbcURL, userName, password);
        hikariConfig.setJdbcUrl(jdbcURL);
        hikariConfig.setUsername(userName);
        hikariConfig.setPassword(password);
        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Orm.setSql2o(new Sql2o(ds));

        //config website info
        Sql2o sql2o = Orm.getSql2o();
        try (Connection conn=sql2o.open()) {
            //there should have one row corresponding to the webID in database
            String sql = "select * from website where webId=:webID";
            Constant.webSite = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(WebSite.class);

            //there should have one row corresponding to the webId in database
            sql = "select * from current where webId=:webID";
            Constant.current = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(Current.class);

            //have no promise about this table
            sql = "select id, webId, patternName, xpath from pattern where webId=:webID";
            List<Pattern> patterns = conn.createQuery(sql).addParameter("webID", webID).executeAndFetch(Pattern.class);
            patterns.forEach(pattern -> {
                if (Constant.FT_INDEX_FIELD.equals(pattern.getPatternName())) return;//jump over fulltext's pattern，because it's the default pattern
                Constant.patternMap.put(pattern.getPatternName(),pattern.getXpath());
            });

            //set the round equal to the current table
            Constant.round = Integer.parseInt(Constant.current.getRound());
        }

        if(Constant.webSite == null || Constant.current == null){
            logger.error("the website infomation corresponding to the webID can't be find，program exit");
            System.exit(1);
        }

        //detect the configuration
        String workFilePath = Constant.webSite.getWorkFile();
        if (StringUtils.isBlank(workFilePath)) {
            logger.error("the work directory can't be blank");
            System.exit(1);
        } else {
            File f = new File(workFilePath);

            //if directory no exist or if this is not a directory or if this directory can be written by the user of crawler
            if (!f.exists() || !f.isDirectory() || !f.canWrite()) {
                logger.error("the work file should be a existed directory，and the owner of this program should have right to write");
                System.exit(1);
            }
        }

        //configure the message queue
        msgQueue = new LinkedBlockingDeque(Constant.QUEUE_SIZE);
        File f = Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR, MSG_DATA_NAME).toFile();
        List<String> msgList = null;
        //if message queue stored file exist, load the data into memory
        if (f.exists()) {
            logger.info("read msg from file {}", f.getAbsolutePath());
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
                msgList = (List<String>)inputStream.readObject();
                logger.info("the left link num from last stop is {}", msgList.size());
            } catch (Exception ex) {
                logger.error("Exception happen when read msg object file");
            } finally {
                f.delete();
            }
        }
        //insert the data into message queue
        if (msgList != null) {
            msgList.forEach(link -> {
                if (StringUtils.isNotBlank(link)) {//if this link is valid
                    msgQueue.offer(link);
                }
            });
        }

        //configure the index client
        indexClient = new IndexClient.Builder(Paths.get(Constant.webSite.getWorkFile(),Constant.FT_INDEX_ADDR)).build();
        //configure the RAM md5 deduplicater
        dedu = new RAMMD5Dedutor(Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR));

        //the global cookie manager
        CookieManager cookieManager = new CookieManager();

        //configure the web brawser
        GenericObjectPoolConfig<WebClient> config = new GenericObjectPoolConfig<>();
        //config.setBlockWhenExhausted(false);
        //limit the maximum browser number to 5
        config.setMaxTotal(5);
        webBrowser = new HtmlUnitBrowser(new GenericObjectPool<WebClient>(new HtmlUnitFactory(cookieManager, 90_000), config));

        //if the login URL is not blank, first to confirm the login information is valid
        if (!StringUtils.isBlank(Constant.webSite.getLoginUrl())) {
            if (!webBrowser.login(Constant.webSite.getLoginUrl(),Constant.webSite.getUserName(),Constant.webSite.getPassword(),Constant.webSite.getUserParam(),Constant.webSite.getPwdParam(),Constant.webSite.getSubmitXpath())) {
                logger.error("detect the login information, but can't login, please check the configuration");
                System.exit(1);
            }
        }

        //configure the HTTP client
        httpClient = new ApacheClient.Builder()
                .setCookieManager(cookieManager)
                .build();

    }
    private static class Exitor extends Thread {
        private Logger logger = LoggerFactory.getLogger(Exitor.class);
        @Override
        public void run() {
            logger.info("start the exit thread");

            //write the content from message queue into file
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
                dedu.close();//dedu data save
                indexClient.close();
            } catch (IOException ex) {
                //nothing to do
            }

        }
    }
}
