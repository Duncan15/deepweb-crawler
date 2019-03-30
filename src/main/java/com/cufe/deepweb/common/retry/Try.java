package com.cufe.deepweb.common.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Try<T> {
    private Logger logger = LoggerFactory.getLogger(Try.class);
    private int num;
    public Try(int num) {
        this.num = num;
    }


    public T run(RetryOperation<T> op) {
        T ans = null;
        Exception exception = null;
        for (int i = 0; i < num; i++) {
            exception = null;
            try {
                ans = op.execute();
            } catch (Exception ex) {
                //ignored
                exception = ex;
            }

            if (ans != null) return ans;
        }
        if (exception != null) {
            logger.error("error happen when try", exception);
        }
        return ans;
    }
}
