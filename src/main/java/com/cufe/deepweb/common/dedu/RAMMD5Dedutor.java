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
public class RAMMD5Dedutor extends RAMStrDedutor {
    private MessageDigest md5;

    /**
     * don't use data saving file
     */
    public RAMMD5Dedutor() {
        super();
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
        super(dataPath);
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex){
            //ignored
        }
    }

    @Override
    protected boolean dedu(String o) {
        byte[] osmd5 = md5.digest(o.getBytes());
        String hexMd5 = Hex.encodeHexString(osmd5);
        return deduSet.add(hexMd5);
    }
}
