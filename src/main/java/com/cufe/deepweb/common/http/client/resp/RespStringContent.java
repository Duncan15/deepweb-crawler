package com.cufe.deepweb.common.http.client.resp;

public class RespStringContent extends RespContent{
    RespStringContent(String content) {
        this.content = content;
    }
    private String content;

    public String getContent() {
        return content;
    }
}
