package com.cufe.deepweb.crawler.service;

public abstract class LinkService {
    protected int totalLinkNum;
    protected int failedLinkNum;

    /**
     * 返回并重置
     * @return
     */
    public int getFailedLinkNum() {
        int num = this.failedLinkNum;
        this.failedLinkNum = 0;
        return num;
    }
    public int getTotalLinkNum() {
        int num = this.totalLinkNum;
        this.totalLinkNum = 0;
        return num;
    }
}
