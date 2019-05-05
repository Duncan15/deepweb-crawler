package com.cufe.deepweb.common.http.client;

import com.cufe.deepweb.common.http.client.resp.HtmlContent;
import com.cufe.deepweb.common.http.client.resp.JsonContent;
import com.cufe.deepweb.common.http.client.resp.RespContent;
import com.cufe.deepweb.crawler.Constant;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.httpclient.HtmlUnitCookieStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * the apache httpclient implementation
 */
public class ApacheClient implements CusHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(ApacheClient.class);
    private static List<String> userAgent = new ArrayList<String>(){
        {
            add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
            add("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
            add("Mozilla/5.0(WindowsNT6.1;rv:31.0)Gecko/20100101Firefox/31.0");
            add("Mozilla");
            add("HTTP Banner Detection(security.ipip.net)");
        }
    };
    /**
     * httpClient is thread-safe
     */
    private CloseableHttpClient httpClient;

    /**
     * thread local value of http context
     */
    private ThreadLocal<HttpContext> httpContext = new ThreadLocal<HttpContext>() {
        /**
         * get the initial HttpContext in different thread
         * override ThreadLocal
         * @return
         */
        @Override
        protected HttpContext initialValue() {
            HttpContext context = new BasicHttpContext();
            if (builder.cookieManager != null) {
                context.setAttribute(HttpClientContext.COOKIE_STORE, new HtmlUnitCookieStore(builder.cookieManager));
            }
            return context;
        }
    };

    private PoolingHttpClientConnectionManager manager;
    private RequestConfig config;
    private Builder builder;
    private static String getUserAgent() {
        return userAgent.get((int)(System.currentTimeMillis() % userAgent.size()));
    }
    private ApacheClient(Builder builder) {
        this.builder =builder;
        manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(this.builder.maxTotal);
        manager.setDefaultMaxPerRoute(this.builder.defaultMaxPerRoute);
        httpClient = HttpClients.custom()
                .setConnectionManager(manager)
                .setRetryHandler(new StandardHttpRequestRetryHandler())
                .build();
        config = RequestConfig.custom()
            .setConnectionRequestTimeout(10 * builder.timeout)
            .setConnectTimeout(10 * builder.timeout)
            .setSocketTimeout(10 * builder.timeout)
            .build();
    }


    /**
     * check whether the response has attachments
     * if have, return the attached file name
     * or return Optional.empty()
     * @param response
     * @return
     */
    private static Optional<String> checkAttachment(HttpResponse response, String charset) {
        Header header = response.getFirstHeader("Content-Disposition");
        if (header != null) {
            HeaderElement[] elements = header.getElements();
            for (HeaderElement element : elements) {
                NameValuePair pair = null;
                if ((pair = element.getParameterByName("filename")) != null) {

                    String fileName = pair.getValue();
                    try {
                        fileName = new String(fileName.getBytes(charset), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        //ignored
                    }
                    logger.trace("filename:{}", fileName);
                    return Optional.ofNullable(fileName);

                }
            }
        }
        return Optional.empty();
    }

    private HttpGet buildBaseHttpGet(String URL) {
        URI uri = null;
        try {
            uri = URI.create(URL);
        } catch (IllegalArgumentException ex) {
            logger.error("url error, url content is " + URL, ex);
            return null;
        }
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(config);
        httpGet.setHeader("user-agent", getUserAgent());
        return httpGet;
    }
    @Override
    public JsonContent getJSON(String URL) {
        HttpGet httpGet = buildBaseHttpGet(URL);
        if (httpGet == null) return null;
        httpGet.addHeader("Accept", "application/json");
        try(CloseableHttpResponse response = httpClient.execute(httpGet, httpContext.get())) {
            if (response.getStatusLine().getStatusCode() >= 300) {
                logger.error("URL:{} HTTP response {}", URL, response.getStatusLine());
            } else {
                HttpEntity entity = response.getEntity();
                ContentType contentType = ContentType.getOrDefault(entity);

                //if can't auto detect the charset from the response, set the charset to the value configured.
                Charset charset = contentType.getCharset();
                if(charset == null || StringUtils.isBlank(charset.name())) {
                    charset = Charset.forName(Constant.extraConf.getCharset());
                }

                return RespContent.asJson(EntityUtils.toString(entity, charset));
            }
        } catch (IOException ex) {
            logger.error("error happen when get JSON content", ex);
            //ignored
        }
        return null;
    }

    /**
     * download the content corresponding to the URL
     * @param URL
     * @return
     */
    @Override
    public RespContent getContent(String URL) {
        HttpGet httpGet = buildBaseHttpGet(URL);
        if (httpGet == null) {
            return null;
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet, httpContext.get());

            if (response.getStatusLine().getStatusCode() >= 300) {
                logger.error("URL:{} , HTTP response: {} {}", URL, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                //here must remember to close the response
                response.close();
                return null;
            } else {
                Optional<String> fileNameOp;

                HttpEntity entity = response.getEntity();
                ContentType contentType = ContentType.getOrDefault(entity);

                //if can't auto detect the charset from the response, set the charset to the value configured.
                Charset charset = contentType.getCharset();
                if(charset == null || StringUtils.isBlank(charset.name())) {
                    charset = Charset.forName(Constant.extraConf.getCharset());
                }

                //rule 1:
                //if it has attachment, deal with it as stream content
                if ((fileNameOp = checkAttachment(response, charset.toString())).isPresent()) {
                    return RespContent.asStream(fileNameOp.get(), response.getEntity().getContent());
                } else {
                    //rule 2:
                    //if this response hasn't contain a attachment, deal with it as a string content whatever it is.
                    HtmlContent respContent = RespContent.asString(EntityUtils.toString(entity, charset));
                    response.close();
                    return respContent;
                }
            }
        }catch (Exception ex) {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    //ignored
                }
            }

            //if ex is UnknownHostException, don't record it
            if (ex instanceof IOException || ex instanceof ConnectException) {
                return null;
            }

            logger.error("Exception in HTTP invoke " + URL, ex);
            return null;
        }

    }

    @Override
    public void close() {
        manager.close();
        try {
            httpClient.close();
        } catch (IOException ex) {
            logger.error("error happen when close apache http client");
        }

    }

    public static class Builder {
        private int maxTotal;
        private int defaultMaxPerRoute;
        /**
         * timeout parameter
         */
        private int timeout;
        /**
         * the cookieManager from browser
         */
        private CookieManager cookieManager;
        /**
         * the max connection number
         * @param maxTotal
         * @return
         */
        public Builder setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        /**
         * the max connection number of a remote host
         * @param defaultMaxPerRoute
         * @return
         */
        public Builder setDefaultMaxPerRoute(int defaultMaxPerRoute) {
            this.defaultMaxPerRoute = defaultMaxPerRoute;
            return this;
        }

        public Builder setCookieManager(CookieManager cookieManager) {
            this.cookieManager = cookieManager;
            return this;
        }
        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }
        public Builder() {
            maxTotal = 200;
            defaultMaxPerRoute = 100;
        }

        public ApacheClient build() {
            return new ApacheClient(this);
        }
    }
}
