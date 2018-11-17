package com.cufe.deepweb.common.http.client;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.httpclient.HtmlUnitCookieStore;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;

/**
 * 使用apache HttpClient实现
 */
public class ApacheClient extends ThreadLocal<HttpContext> implements CusHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(ApacheClient.class);
    /**
     * httpClient是线程安全的，因此可在多个线程中共用一个client
     */
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager manager;
    private Builder builder;
    private ApacheClient(Builder builder) {
        this.builder =builder;
        manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(this.builder.maxTotal);
        manager.setDefaultMaxPerRoute(this.builder.defaultMaxPerRoute);
        httpClient = HttpClients.custom()
                .setConnectionManager(manager)
                .build();
    }


    /**
     * 获取各个线程中的初始化HttpContext
     * override ThreadLocal
     * @return
     */
    @Override
    protected HttpContext initialValue() {
        HttpContext context = new BasicHttpContext();
        if (builder.cookieManager != null) {
            context.setAttribute(HttpClientContext.COOKIE_STORE,new HtmlUnitCookieStore(builder.cookieManager));
        }
        return context;
    }

    /**
     * 将URL的内容下载并转成string
     * @param URL
     * @return
     */
    public Optional<String> getContent(String URL) {
        HttpGet httpGet = new HttpGet(URL);
        try (CloseableHttpResponse response = httpClient.execute(httpGet,get())) {
            if (response.getStatusLine().getStatusCode() >= 300) {
                logger.error("HTTP response status code {}, reason phase: {}",response.getStatusLine().getStatusCode(),response.getStatusLine().getReasonPhrase());
            } else {
                HttpEntity entity = response.getEntity();
                ContentType contentType = ContentType.getOrDefault(entity);
                return Optional.ofNullable(EntityUtils.toString(entity,contentType.getCharset()));
            }
        }catch (IOException ex) {
            logger.error("IOException in HTTP invoke", ex);
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
         * 来自模拟器的cookieManager
         */
        private CookieManager cookieManager;
        /**
         * 设置总最大连接数量
         * @param maxTotal
         * @return
         */
        public Builder setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        /**
         * 设置单路由默认最大连接数
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
        public Builder() {
            maxTotal = 200;
            defaultMaxPerRoute = 100;
        }

        public ApacheClient build() {
            return new ApacheClient(this);
        }
    }
}
