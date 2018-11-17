package com.cufe.deepweb.common.http.client;

import java.io.Closeable;
import java.util.Optional;

/*
使用HttpClient的客户端
 */
public interface CusHttpClient extends Closeable {
    /**
     * 将URL的内容返回
     * @param URL
     * @return
     */
    Optional<String> getContent(String URL);


}
