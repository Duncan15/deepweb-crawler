package com.cufe.deepweb.common.retry;

public interface RetryOperation<T> {
    T execute() throws Exception;
}
