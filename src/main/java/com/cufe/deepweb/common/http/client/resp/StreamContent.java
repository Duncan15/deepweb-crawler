package com.cufe.deepweb.common.http.client.resp;

import java.io.InputStream;

public class StreamContent extends RespContent{
    StreamContent(String fileName, InputStream stream) {
        this.fileName = fileName;
        this.stream = stream;
    }
    private String fileName;
    private InputStream stream;

    public InputStream getStream() {
        return stream;
    }

    public String getFileName() {
        return fileName;
    }
}
