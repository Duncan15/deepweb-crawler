package com.cufe.deepweb.crawler.service;

public abstract class LinkService {
    protected int totalLinkNum;
    protected int failedLinkNum;

    /**
     * return the link number of different status in current round
     * @return
     */
    public int getFailedLinkNum() {
        return this.failedLinkNum;
    }
    public int getTotalLinkNum() {
        return this.totalLinkNum;
    }

    /**
     * reset the link number of different status to zero for recording new round's status
     * this method should be invoked by the user of this service
     */
    public void reset() {
        this.failedLinkNum = 0;
        this.totalLinkNum = 0;
    }

    /**
     * clear resource which is assigned for current thread
     * (clear thread local value)
     */
    public abstract void clearThreadResource();
}
