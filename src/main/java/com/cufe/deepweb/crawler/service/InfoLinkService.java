package com.cufe.deepweb.crawler.service;

import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import com.cufe.deepweb.common.index.IndexClient;
import com.cufe.deepweb.common.dedu.Deduplicator;
import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于处理信息链接相关的服务
 * 必须是线程安全的实现
 */
public class InfoLinkService extends LinkService {
    private final static Logger logger = LoggerFactory.getLogger(InfoLinkService.class);
    private CusHttpClient httpClient;
    private IndexClient indexClient;
    /**
     * html标签解析器，多线程安全
     */
    private HtmlCleaner htmlCleaner;
    /**
     * 用于计算当前round所下载的文件数量
     */
    private AtomicInteger count;
    public InfoLinkService(CusHttpClient httpClient, IndexClient indexClient) {
        this.httpClient = httpClient;
        this.indexClient = indexClient;
        this.htmlCleaner = new HtmlCleaner();
        CleanerProperties properties = this.htmlCleaner.getProperties();
        properties.setOmitComments(true);//忽略注释
        count = new AtomicInteger(0);
    }

    public Map<String, String> getFieldContentMap(String content) {
        Map<String, String> fieldContentMap = new HashMap<>();
        Constant.patternMap.forEach((k, v) -> {
            TagNode root = htmlCleaner.clean(content);//先对content内容进行清洗
            try {
                Object[] objects = root.evaluateXPath(v);
                List<String> strings = new ArrayList<>();
                if (objects != null && objects.length != 0) {//如果安装xpath找得到数据
                    for (Object o : objects) {
                        TagNode node = (TagNode)o;
                        strings.add(node.getText().toString());
                    }
                    fieldContentMap.put(k,StringUtils.join(strings,"\t"));
                }
            } catch (XPatherException ex) {
                logger.error("XpatherException happen when evaluate XPath for field "+k,ex);
            }
        });
        return fieldContentMap;
    }
    public String getFileAddr(String infoLink) {
        Path p = Paths.get(Constant.webSite.getWorkFile(),Constant.HTML_ADDR,Constant.current.getRound());
        File f = p.toFile();
        if (!f.exists()) {//如果当前路径不存在，则创建，该情况有可能存在，因为round路径由爬虫来新建，html_addr路径理论上不应该由爬虫新建
            f.mkdirs();
        }
        String ext = ".html";//ext默认为.html
        if (infoLink.contains(".")) {
            ext = infoLink.substring(infoLink.lastIndexOf("."));
            if (!Constant.docTypes.contains(ext)) {//如果下载的文件的扩展格式不在爬虫约定的范围内，则修改为html
                ext = ".html";
            }
        }
        String newFilePath = p.resolve(count.getAndIncrement() + ext).toString();
        totalLinkNum++;
        return newFilePath;
    }
    /**
     * 下载并打索引
     * @param URL
     * @param filePath
     */
    public void  downloadAndIndex(String URL, String filePath) {
        Optional<String> contentOp = httpClient.getContent(URL);
        if (contentOp.isPresent()) {//如果存在返回结果
            //将下载内容存入文件
            try {
                Utils.save2File(contentOp.get(), filePath);
            } catch (IOException ex) {
                logger.error("IOException in save content to file", ex);
            }
            //将下载内容打成索引
            indexClient.addDocument(getFieldContentMap(contentOp.get()));
        } else {
            failedLinkNum++;
        }
    }

}
