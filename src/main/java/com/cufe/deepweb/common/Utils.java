package com.cufe.deepweb.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

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


}
