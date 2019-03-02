package com.cufe.deepweb.common.dedu;

import com.cufe.deepweb.crawler.Constant;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * memory deduplicator based on MD5
 */
public class RAMMD5Dedutor extends Deduplicator<String> {
    private final Logger logger = LoggerFactory.getLogger(RAMMD5Dedutor.class);
    private Set<String> deduSet;
    private MessageDigest md5;
    private Path dataPath; //the directory path for data saving

    /**
     * don't use data saving file
     */
    public RAMMD5Dedutor() {
        deduSet = new HashSet<>();
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex){
            //ignored
        }
    }

    /**
     * use data saving file
     * @param dataPath
     */
    public RAMMD5Dedutor(Path dataPath){
        this.dataPath = dataPath;
        deduSet = null;
        File f  = dataPath.resolve( Constant.round + DATA_FILE_NAME).toFile();
        if (f.exists()) {//if data saving file exists
            logger.info("start to read dedu information from file {}", f.getAbsolutePath());
            try(ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
                deduSet = (Set<String>) inputStream.readObject();
            } catch (Exception ex) {
                logger.error("Exception happen when read dedu object file");
            } finally {
                logger.info("read dedu information finish");
                f.delete();
            }
        }

        /**
         * if fail to read information from data saving file, just ignore it
         */
        if (deduSet == null) {
            deduSet = new HashSet<>();
        }
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex){
            //ignored
        }
    }

    /**
     * use synchronized keyword to support thread-safe
     * @param o
     * @return
     */
    @Override
    public synchronized boolean add(String o) {
        costV++;
        byte[] osmd5 = md5.digest(o.getBytes());

        String hexMd5 = Hex.encodeHexString(osmd5);
        if (deduSet.add(hexMd5)) {
            newV++;
            return true;
        }
        return false;
    }

    @Override
    public int getTotal() {
        return deduSet.size();
    }

    /**
     * use to restore information in dedu, and release resource
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if (dataPath == null) return;
        System.out.println("start to store information in dedu");
        String fileName = Constant.round + DATA_FILE_NAME;
        File f = this.dataPath.resolve(fileName).toFile();
        if (f.exists()) {//if the data saving file belongs to current round has existed, exit directly
            System.out.println("the data saving file belongs to current round has existed, exit directly");
            return;
        }

        //if the data saving file belongs to current round hasn't been created, create it and write into it
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(f))) {
            outputStream.writeObject(deduSet);
        }
        System.out.println("store information in dedu finish");

    }
}
