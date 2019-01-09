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

    public Map<String, String> getFieldContentMap(String content) {
        Map<String, String> fieldContentMap = new HashMap<>();
        TagNode root = null;
        try {
            root = htmlCleaner.clean(content);//first to clean the html content
        } catch (Exception ex) {
            logger.error("exception happen when parse html content", ex);
            return Collections.emptyMap();
        }
        for (Map.Entry<String, String> pattern : Constant.patternMap.entrySet()) {
            try {
                Object[] objects = root.evaluateXPath(pattern.getValue());
                List<String> strings = new ArrayList<>();
                if (objects != null && objects.length != 0) {//if can find data by the xpath
                    for (Object o : objects) {
                        TagNode node = (TagNode)o;
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
    public String getFileAddr(String infoLink) {
        Path p = Paths.get(Constant.webSite.getWorkFile(),Constant.HTML_ADDR,Constant.current.getRound());
        File f = p.toFile();

        //if the path of f no exist, create it
        if (!f.exists()) {
            f.mkdirs();
        }
        String ext = ".html";//the default extension is .html
        if (infoLink.contains(".")) {
            ext = infoLink.substring(infoLink.lastIndexOf("."));

            //if the document's extension is not in the range of this crawler's define, just change it to html
            if (!Constant.docTypes.contains(ext)) {
                ext = ".html";
            }
        }
        String newFilePath = p.resolve(count.getAndIncrement() + ext).toString();
        totalLinkNum++;
        return newFilePath;
    }
    /**
     * download the target document and build into index
     * @param URL
     * @param filePath
     */
    public void  downloadAndIndex(String URL, String filePath) {
        Optional<String> contentOp = httpClient.getContent(URL);
        if (contentOp.isPresent()) {//if the target document get successfully

            //save the document into directory
            try {
                Utils.save2File(contentOp.get(), filePath);
            } catch (IOException ex) {
                logger.error("IOException in save content to file", ex);
            }

            //build the document content into index
            indexClient.addDocument(getFieldContentMap(contentOp.get()));
        } else {
            failedLinkNum++;
        }
    }

    @Override
    public void clearThreadResource() {

    }
}
