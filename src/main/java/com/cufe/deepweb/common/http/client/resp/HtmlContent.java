package com.cufe.deepweb.common.http.client.resp;

public class HtmlContent extends RespContent {
    HtmlContent(String content) {
        this.content = content;
    }
    private String content;

    public String getContent() {
        return content;
    }
}
