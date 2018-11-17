package com.cufe.deepweb.common.dedu;

import com.cufe.deepweb.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public class RAMDeduplicator extends Deduplicator<String> {
    private final Logger logger = LoggerFactory.getLogger(RAMDeduplicator.class);
    private Set<byte[]> deduSet;
    private MessageDigest md5;
    private int newV;
    private int costV;
    private Path dataPath; //数据存储文件夹
    public RAMDeduplicator(Path dataPath){
        this.dataPath = dataPath;
        deduSet = null;
        File f  = dataPath.resolve( Constant.round+DATA_FILE_NAME).toFile();
        if (f.exists()) {//如果对象文件存在
            try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
                deduSet = (Set<byte[]>) inputStream.readObject();
            } catch (Exception ex) {
                logger.error("Exception happen when read dedu object file");
            } finally {
                f.delete();
            }
        }
        /**
         * 如果读取失败，则忽略此文件的存在
         */
        if (deduSet == null) {
            deduSet = new HashSet<>();
        }
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex){
            logger.error("can't find the md5 algorithm");//此错误不可能发生
        }
    }

    /**
     * 通过简单加锁保证线程安全
     * @param o
     * @return
     */
    @Override
    public synchronized boolean add(String o) {
        costV++;
        byte[] osmd5 = md5.digest(o.getBytes());
        if (deduSet.add(osmd5)) {
            newV++;
            return true;
        }
        return false;
    }

    @Override
    public int getNew() {
        int tmp = newV;
        newV = 0;
        return tmp;
    }

    @Override
    public int getCost() {
        int tmp = costV;
        costV = 0;
        return tmp;
    }

    @Override
    public int getTotal() {
        return deduSet.size();
    }

    /**
     * 用于去重对象的收尾操作
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        String fileName = Constant.round+DATA_FILE_NAME;
        File f = this.dataPath.resolve(fileName).toFile();
        if (f.exists()) {//如果该轮次的备份文件已经存在，直接退出
            return;
        }
        //如果该轮次的备份文件不存在，直接写入
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(f))) {
            outputStream.writeObject(deduSet);
        }
    }
}
