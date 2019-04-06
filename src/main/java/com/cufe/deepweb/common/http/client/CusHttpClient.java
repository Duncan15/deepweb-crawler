package com.cufe.deepweb.common.http.client;

import com.cufe.deepweb.common.http.client.resp.JsonContent;
import com.cufe.deepweb.common.http.client.resp.RespContent;

import java.io.Closeable;

/*
使用HttpClient的客户端
 */
public interface CusHttpClient extends Closeable {
    /**
     * return the content from the url,
     * return a string or a inputStream
     * if the return value contains a stream, after consuming it, must close it
     * @param URL
     * @return
     */
    RespContent getContent(String URL);


    JsonContent getJSON(String URL);
}
