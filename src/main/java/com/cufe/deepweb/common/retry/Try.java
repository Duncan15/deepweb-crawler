package com.cufe.deepweb.common.retry;

public class Try<T> {
    private int num;
    public Try(int num) {
        this.num = num;
    }


    public T run(RetryOperation<T> op) {
        T ans = null;
        for (int i = 0; i < num; i++) {
            ans = op.execute();
            if (ans != null) return ans;
        }
        return ans;
    }
}
