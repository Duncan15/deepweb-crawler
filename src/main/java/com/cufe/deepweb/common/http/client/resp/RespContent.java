package com.cufe.deepweb.common.http.client.resp;

import java.io.InputStream;

public class RespContent {
    public static HtmlContent asString(String str) {
        return new HtmlContent(str);
    }
    public static StreamContent asStream(String name, InputStream stream) {
        return new StreamContent(name, stream);
    }
    public static JsonContent asJson(String rawContent) {
        return new JsonContent(rawContent);

    }
}
