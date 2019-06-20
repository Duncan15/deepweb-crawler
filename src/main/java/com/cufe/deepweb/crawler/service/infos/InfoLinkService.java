package com.cufe.deepweb.crawler.service.infos;

import com.cufe.deepweb.common.http.client.resp.JsonContent;
import com.cufe.deepweb.common.http.client.resp.RespContent;
import com.cufe.deepweb.common.http.client.resp.StreamContent;
import com.cufe.deepweb.common.http.client.resp.HtmlContent;
import com.cufe.deepweb.crawler.Constant;
import com.cufe.deepweb.common.Utils;
import com.cufe.deepweb.common.http.client.CusHttpClient;
import com.cufe.deepweb.common.index.IndexClient;
import com.cufe.deepweb.crawler.service.LinkService;
import com.cufe.deepweb.crawler.service.infos.info.Info;
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
 * the service used to deal with info links
 * the implementation must be thread-safe
 */
public class InfoLinkService extends LinkService {
    private final static Logger logger = LoggerFactory.getLogger(InfoLinkService.class);
    private CusHttpClient httpClient;
    private IndexClient indexClient;
    /**
     * html tag parser, thread-safe
     */
    private HtmlCleaner htmlCleaner;
    /**
     * used to compute the downloaded document number in current round
     */
    private AtomicInteger count;
    public InfoLinkService(CusHttpClient httpClient, IndexClient indexClient) {
        this.httpClient = httpClient;
        this.indexClient = indexClient;
        this.htmlCleaner = new HtmlCleaner();
        CleanerProperties properties = this.htmlCleaner.getProperties();
        properties.setOmitComments(true);
        count = new AtomicInteger(0);
    }

    private Map<String, String> getFieldContentMap(String content) {
        Map<String, String> fieldContentMap = new HashMap<>();
        TagNode root = null;
        try {
            root = htmlCleaner.clean(content);//first to clean the html content
        } catch (Exception ex) {
            logger.error("exception happen when parse html content", ex);
            return Collections.emptyMap();
        }
        if (root == null) {
            return Collections.emptyMap();
        }
        for (Map.Entry<String, String> pattern : Constant.patternMap.entrySet()) {
            try {
                Object[] objects = root.evaluateXPath(pattern.getValue());
                List<String> strings = new ArrayList<>();
                if (objects != null && objects.length != 0) {//if can find data by the xpath
                    for (Object o : objects) {
                        TagNode node = (TagNode) o;
                        strings.add(node.getText().toString());
                    }
                    fieldContentMap.put(pattern.getKey(), StringUtils.join(strings,"\t"));
                }
            } catch (XPatherException ex) {
                logger.error("XpatherException happen when evaluate XPath for field " + pattern.getKey(), ex);
            }
        }


        //if //body can evaluate the content from HTML, just build the HTML content into fulltext field
        if (StringUtils.isBlank(fieldContentMap.get(Constant.FT_INDEX_FIELD))) {
            fieldContentMap.put(Constant.FT_INDEX_FIELD, content);
        }
        return fieldContentMap;
    }
    private String getFileAddr(String link, boolean generateFileName) {
        Path p = Paths.get(Constant.webSite.getWorkFile(),Constant.HTML_ADDR, Constant.current.getRound());
        File f = p.toFile();
        String newFilePath = null;
        //if the path of f no exist, create it
        if (!f.exists()) {
            f.mkdirs();
        }
        if (generateFileName) {
            String ext = ".html";//the default extension is .html
            if (link.contains(".")) {
                ext = link.substring(link.lastIndexOf("."));

                //if the document's extension is not in the range of this crawler's define, just change it to html
                if (!Constant.docTypes.contains(ext)) {
                    ext = ".html";
                }
            }
            newFilePath = p.resolve(count.getAndIncrement() + ext).toString();
        } else {
            newFilePath = p.resolve(link).toString();
        }
        return newFilePath;
    }


    /**
     * download the target document and build into index
     * @param info
     */
    public void  downloadAndIndex(Info info) {
        totalLinkNum++;
        RespContent content = httpClient.getContent(info.getUrl());
        Map<String ,String> map = info.getPayload() == null ? new HashMap<>() : info.getPayload();

        String fileAddr = "";
        if (content instanceof HtmlContent) {
            //save the document into directory if the return value contains a string
            HtmlContent htmlContent = (HtmlContent) content;
            fileAddr = getFileAddr(info.getUrl(), true);
            try {
                Utils.save2File(htmlContent.getContent(), fileAddr);
            } catch (IOException ex) {
                failedLinkNum++;
                logger.error("IOException in save content to file", ex);
                Utils.deleteFile(fileAddr);
            }
            map.putAll(getFieldContentMap((htmlContent.getContent())));
            indexClient.addDocument(map);
        } else if (content instanceof JsonContent) {
            //at current implementation of crawler, here must not a json
            //here leave a TODO for future development.
            return;

        } else if (content instanceof StreamContent) {
            //or save the attachment into a file if the return value contains an inputStream
            StreamContent streamContent = (StreamContent) content;
            if (map.get("filename") != null) {
                fileAddr = getFileAddr(map.get("filename"), false);
            } else {
                fileAddr = getFileAddr(streamContent.getFileName(), false);
            }

            try {
                Utils.save2File(streamContent.getStream(), fileAddr);
            } catch (IOException ex) {
                failedLinkNum++;
                logger.error("IOException in save content to file:" + fileAddr, ex);
                File f = new File(fileAddr);
                Utils.deleteFile(fileAddr);
            } finally {
                try {
                    streamContent.getStream().close();
                } catch (IOException ex) {
                    //ignored
                }

            }
            indexClient.addDocument(map);
        } else {
            failedLinkNum++;
        }
    }

    @Override
    public void clearThreadResource() {

    }
}
