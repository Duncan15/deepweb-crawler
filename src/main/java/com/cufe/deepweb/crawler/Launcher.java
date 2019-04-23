package com.cufe.deepweb.crawler;

import com.cufe.deepweb.algorithm.AlgorithmBase;
import com.cufe.deepweb.algorithm.AllInAlgorithm;
import com.cufe.deepweb.algorithm.LinearIncrementalAlgorithm;
import com.cufe.deepweb.common.dedu.RAMStrDedutor;
import com.cufe.deepweb.common.orm.model.*;
import com.cufe.deepweb.crawler.branch.ApiBaseScheduler;
import com.cufe.deepweb.crawler.branch.JsonBaseScheduler;
import com.cufe.deepweb.crawler.branch.Scheduler;
import com.cufe.deepweb.common.http.client.ApacheClient;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import com.cufe.deepweb.common.http.simulate.HtmlUnitBrowser;
import com.cufe.deepweb.common.http.simulate.WebBrowser;
import com.cufe.deepweb.common.index.IndexClient;
import com.cufe.deepweb.crawler.branch.UrlBaseScheduler;
import com.cufe.deepweb.crawler.service.infos.InfoLinkService;
import com.cufe.deepweb.common.dedu.Deduplicator;
import com.cufe.deepweb.common.orm.Orm;
import com.cufe.deepweb.crawler.service.infos.info.Info;
import com.cufe.deepweb.crawler.service.querys.ApiBaseQueryLinkService;
import com.cufe.deepweb.crawler.service.querys.JsonBaseQueryLinkService;
import com.cufe.deepweb.crawler.service.querys.UrlBaseQueryLinkService;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.util.Cookie;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    private static AlgorithmBase alg;
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

        //initialize the service to deal with infoLinks
        InfoLinkService infoLinkService = new InfoLinkService(httpClient, indexClient);

        Scheduler scheduler;
        //initialize the service to deal with queryLinks
        //initialize the scheduler thread
        if (Constant.urlBaseConf != null) {
            logger.info("prepare urlBaseScheduler");
            UrlBaseQueryLinkService urlBaseQueryLinkService = new UrlBaseQueryLinkService(webBrowser, dedu);
            scheduler = new UrlBaseScheduler(alg, urlBaseQueryLinkService, infoLinkService, msgQueue);
        } else if (Constant.apiBaseConf != null) {
            logger.info("prepare apiBaseScheduler");
            ApiBaseQueryLinkService apiBaseQueryLinkService = new ApiBaseQueryLinkService(webBrowser, dedu);
            scheduler = new ApiBaseScheduler(alg, apiBaseQueryLinkService, infoLinkService);
        } else if (Constant.jsonBaseConf != null) {
            logger.info("prepare jsonBaseScheduler");
            JsonBaseQueryLinkService jsonBaseQueryLinkService = new JsonBaseQueryLinkService(webBrowser, dedu, httpClient);
            scheduler = new JsonBaseScheduler(alg, jsonBaseQueryLinkService, infoLinkService, msgQueue);
        } else {
            logger.error("can't judge to use which scheduler, exit");
            System.exit(1);
            return;
        }

        //when scheduler thread start to run, everything startup
        scheduler.start();
    }

    /**
     * @note: the crawler don't do some things about inserting initial data into database,
     * when this program start to run, every configuration in database should be confirmed to be finished
     * in other word, it should have a initialize step when the user finish to configure in the web's front end
     * @param args
     *        [0] web-id
     *        [1] jdbc-url
     *        [2] username
     *        [3] password
     *        [4] all-in-num: specified for the all-in mode
     */
    private static void init(final String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("wi")
            .longOpt("web-id")
            .hasArg()
            .required()
            .desc("the specified website ID")
            .build()
        );
        options.addOption(Option.builder("jl")
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
        options.addOption(Option.builder("ain")
                .longOpt("all-in-num")
                .hasArg()
                .desc("the number of turns in the all-in algorithm")
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

        Constant.ALL_IN_NUM = Integer.parseInt(cmd.getOptionValue("all-in-num", "0"));

        //config mysql
        String webIDStr = cmd.getOptionValue("web-id");
        int webID = Integer.parseInt(webIDStr);//webID
        String jdbcURL = cmd.getOptionValue("jdbc-url");
        String userName = cmd.getOptionValue("username");
        String password = cmd.getOptionValue("password");
        logger.info("crawler start");
        logger.info("configure cmd param with jdbcURL:{},userName:{},password:{}", jdbcURL, userName, password);

        Orm.setSql2o(new Sql2o(jdbcURL, userName, password));

        //config website info
        Sql2o sql2o = Orm.getSql2o();
        try (Connection conn=sql2o.open()) {
            conn.setRollbackOnException(true);
            //there should have one row corresponding to the webID in database
            String sql = "select * from website where webId=:webID";
            Constant.webSite = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(WebSite.class);

            if (Constant.webSite.getBase() == Constant.URL_BASED) {
                sql = "select * from urlBaseConf where webId = :webID";
                Constant.urlBaseConf = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(UrlBaseConf.class);
            } else if (Constant.webSite.getBase() == Constant.API_BASED) {
                sql = "select * from apiBaseConf where webId = :webID";
                Constant.apiBaseConf = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(ApiBaseConf.class);
            } else if (Constant.webSite.getBase() == Constant.JSON_BASED) {
                sql = "select * from jsonBaseConf where webId =:webID";
                Constant.jsonBaseConf = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(JsonBaseConf.class);
            } else {
                logger.error("the base value in website table is undefined, exit");
                System.exit(1);
            }

            //if extraConf hasn't exist, create it
            sql = "select * from extraConf where webId=:webID";
            Constant.extraConf = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(ExtraConf.class);
            if(Constant.extraConf == null) {
                sql = "insert into extraConf(webId) values(:webID)";
                conn.createQuery(sql).addParameter("webID", webID)
                        .executeUpdate();
                sql = "select * from extraConf where webId=:webID";
                Constant.extraConf = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(ExtraConf.class);
            }

            //if current hasn't exist, create it
            sql = "select * from current where webId=:webID";
            Constant.current = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(Current.class);
            if (Constant.current == null) {
                sql = "insert into current (webId)" +
                        "values(:webID)";
                conn.createQuery(sql).addParameter("webID", webID)
                        .executeUpdate();
                sql = "select * from current where webId=:webID";
                Constant.current = conn.createQuery(sql).addParameter("webID", webID).executeAndFetchFirst(Current.class);

            }

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

        //check the db initial result
        if (Constant.webSite == null) {
            logger.error("the website information corresponding to the webID can't be find，program exit");
            System.exit(1);
        }
        if (Constant.apiBaseConf == null && Constant.urlBaseConf == null && Constant.jsonBaseConf == null) {
            logger.error("both urlBaseConf and apiBaseConf are not exist, exit");
            System.exit(1);
        }


        //detect the validation of workFile
        String workFilePath = Constant.webSite.getWorkFile();
        if (StringUtils.isBlank(workFilePath)) {
            logger.error("the work directory can't be blank");
            System.exit(1);
        } else {
            //for compatible, just reset the workFile to workFile/webId, this change wouldn't change the configuration in db
            Constant.webSite.setWorkFile(Paths.get(Constant.webSite.getWorkFile(), Constant.webSite.getWebId() + "").toString());
            workFilePath = Constant.webSite.getWorkFile();
            File f = new File(workFilePath);

            if (!f.exists()) {
                f.mkdirs();
            }
            //if directory no exist or if this is not a directory or if this directory can be written by the user of crawler
            if (!f.isDirectory() || !f.canWrite()) {
                logger.error("the work file:{} should be a writable directory，and the owner of this program should have right to write", workFilePath);
                System.exit(1);
            }
        }


        //configure the message queue
        msgQueue = new LinkedBlockingDeque(Constant.QUEUE_SIZE);
        File f = Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR, MSG_DATA_NAME).toFile();
        List<Info> msgList = null;
        //if message queue stored file exist, load the data into memory
        if (f.exists()) {
            logger.info("read msg from file {}", f.getAbsolutePath());
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
                msgList = (List)inputStream.readObject();
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
                msgQueue.offer(link);
            });
        }

        //configure the index client to use ansj_seg analyzer
        indexClient = new IndexClient.Builder(Paths.get(Constant.webSite.getWorkFile(),Constant.FT_INDEX_ADDR)).setAnalyzer(IndexClient.AnalyzerTpye.cn).build();
        //configure the RAM md5 deduplicater
        //dedu = new RAMMD5Dedutor(Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR));

        dedu = new RAMStrDedutor(Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR));

        //the global cookie manager
        CookieManager cookieManager = new CookieManager();
        webBrowser = new HtmlUnitBrowser(cookieManager, Constant.extraConf.getTimeout());

        //if the login URL is not blank, first to confirm the login information is valid
        if (!StringUtils.isBlank(Constant.extraConf.getLoginUrl())) {
            if (!webBrowser.login(Constant.extraConf.getLoginUrl(),Constant.extraConf.getUserName(),Constant.extraConf.getPassword(),Constant.extraConf.getUserNameXpath(),Constant.extraConf.getPasswordXpath(),Constant.extraConf.getSubmitXpath())) {
                logger.error("detect the login information, but can't login, please check the configuration");
                System.exit(1);
            }
            logger.info("print cookie after login");
            for (Cookie cookie : cookieManager.getCookies()) {
                logger.info("cookie:{}", cookie.toString());
            }
        }

        //configure the HTTP client
        httpClient = new ApacheClient.Builder()
                .setCookieManager(cookieManager)
                .setTimeout(Constant.extraConf.getTimeout())
                .build();


        //initialize the strategy algorithm, this algorithm would only be used in scheduler thread
        AlgorithmBase.Builder builder;
        if (Constant.ALL_IN_NUM == 0) {
            builder = new LinearIncrementalAlgorithm.Builder(indexClient, dedu);
        } else {
            builder = new AllInAlgorithm.Builder().setIndexClient(indexClient).setAllInNum(Constant.ALL_IN_NUM).setLowBound(0.002).setUpBound(0.05);
        }
        builder.setProductPath(Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR));
        alg = builder.build();

        //after initialize all the utility, set the current pid to db
        try (Connection conn = sql2o.open()) {
            long pid = ProcessHandle.current().pid();
            String sql = "update current set run = :PID where webId = :webID";
            conn.createQuery(sql)
                    .addParameter("PID", pid)
                    .addParameter("webID", Constant.webSite.getWebId())
                    .executeUpdate();
            logger.info("finish to set pid: {} into db", pid);
        }


    }
    private static class Exitor extends Thread {
        @Override
        public void run() {
            System.out.println("start the exit thread");
            //when process come to exit, set the current pid in db to 0
            try (Connection conn = Orm.getSql2o().open()) {
                String sql = "update current set run = 0 where webId = :webID";
                conn.createQuery(sql)
                        .addParameter("webID", Constant.webSite.getWebId())
                        .executeUpdate();
                System.out.println("finish to set pid in db to 0");
            }

            //write the content from message queue into file
            System.out.println("start to write the content from message queue into file");
            List msgList = new ArrayList<>();
            msgQueue.forEach( link -> {
                msgList.add(link);
            });
            System.out.println("the left link num in current round is " + msgList.size());
            //if data directory no exists, invoke mkdirs
            Path dataDirPath = Paths.get(Constant.webSite.getWorkFile(), Constant.DATA_ADDR);
            File dir = dataDirPath.toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //if data file exists, remove it
            File dataFile = dataDirPath.resolve(MSG_DATA_NAME).toFile();
            if (dataFile.exists()) {
                dataFile.delete();
            }

            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                outputStream.writeObject(msgList);
            } catch (IOException ex) {
                System.err.println("IOException happen when write msg object to file");
            }
            System.out.println("finish to write the message queue content");

            System.out.println("start to close resource");
            try {
                httpClient.close();
                dedu.close();//dedu data save
                alg.close();//qList data save
                indexClient.close();
            } catch (IOException ex) {
                //nothing to do
            }
            System.out.println("finish to close resource");

        }
    }
}
