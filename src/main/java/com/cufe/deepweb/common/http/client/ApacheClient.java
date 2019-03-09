package com.cufe.deepweb.common.http.client;

import com.cufe.deepweb.crawler.Constant;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.httpclient.HtmlUnitCookieStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
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
import java.net.UnknownHostException;
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
            .setConnectionRequestTimeout(builder.timeout)
            .setConnectTimeout(builder.timeout)
            .setSocketTimeout(builder.timeout)
            .build();
    }

    /**
     * download the content corresponding to the URL and change it to a string
     * @param URL
     * @return
     */
    public Optional<String> getContent(String URL) {
        URI uri = null;
        try {
            uri = URI.create(URL);
        } catch (IllegalArgumentException ex) {
            logger.error("url error, url content is " + URL, ex);
            return Optional.empty();
        }
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(config);
        httpGet.setHeader("user-agent", getUserAgent());
        try (CloseableHttpResponse response = httpClient.execute(httpGet, httpContext.get())) {
            if (response.getStatusLine().getStatusCode() >= 300) {
                logger.error("HTTP response status code {}, reason phase: {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            } else {
                HttpEntity entity = response.getEntity();
                ContentType contentType = ContentType.getOrDefault(entity);

                //if can't auto detect the charset from the response, set the charset to the value configured.
                Charset charset = contentType.getCharset();
                if(charset == null || StringUtils.isBlank(charset.name())) {
                    charset = Charset.forName(Constant.extraConf.getCharset());
                }
                return Optional.ofNullable(EntityUtils.toString(entity, charset));
            }
        }catch (Exception ex) {
            //if ex is UnknownHostException, don't record it
            if (ex instanceof IOException) {
                return Optional.empty();
            }
            if (ex instanceof ConnectException) {
                return Optional.empty();
            }
            logger.error("Exception in HTTP invoke " + URL, ex);
        }
        return Optional.empty();
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
