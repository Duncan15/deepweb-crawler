package com.cufe.deepweb.common.retry;

public abstract class RetryOperation<T> {
    public abstract T execute();
}
