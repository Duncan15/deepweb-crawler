package com.cufe.deepweb.common;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;

public final class Utils {
    private Utils() { }
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    public static void logMemorySize() {
        logger.trace("current memory size is {} G", (double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 /1024);
    }
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
    public static void save2File(InputStream stream, String filePath) throws IOException {

        try (BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(filePath))) {
            //use a buffer of 8192 bytes, no need to use the default BufferedInputStream
            byte[] buffer = new byte[8192];
            int limit = 0;
            while (true) {
                int len = 0;
                try {
                    len = stream.read(buffer);
                    limit = 0;
                } catch (IOException ex) {
                    limit++;
                    if (limit >= 3) {
                        throw  ex;
                    }
                }
                if (len == -1) break;
                bo.write(buffer, 0, len);
            }
        } finally {
            stream.close();
        }

    }

    public static void deleteFile(String fileName) {
        File f = new File(fileName);
        if (f.exists()) {
            f.delete();
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
