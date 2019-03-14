package com.cufe.deepweb.common.http.client.resp;

import java.io.InputStream;

public class RespContent {
    public static RespStringContent asString(String str) {
        return new RespStringContent(str);
    }
    public static RespStreamContent asStream(String name, InputStream stream) {
        return new RespStreamContent(name, stream);
    }
}
