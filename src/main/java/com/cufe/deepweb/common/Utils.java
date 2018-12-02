package com.cufe.deepweb.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;

public final class Utils {
    private Utils() { }

    /**
     * 获取数组中对应未知的值，当越界时返回空值或0值
     * @param arr
     * @param index
     * @param <T>
     * @return
     */
    public static <T> T getValue(final T[] arr, final int index) {
        if (index >= arr.length || index < 0) {
            return null;
        }
        return arr[index];
    }

    /**
     * 将content的内容写入filePath的文件中
     * @param filePath 文件路径
     */
    public static void save2File(String content, String filePath) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), "UTF8")) {
            writer.write(content);
        }
    }
    /**
     * 判断集合内任务是否运行完毕，同时对运行完毕的任务进行清理
     * @param runSet
     * @return true if have task running
     */
    public static boolean isRun(Set<Future> runSet) {
        if (runSet == null) return false;
        Iterator<Future> iter = runSet.iterator();
        boolean isRun = false;
        while (iter.hasNext()) {
            Future f = iter.next();
            if (f.isDone()) {
                iter.remove();
            } else {
                isRun = true;
            }
        }
        return isRun;
    }


}
