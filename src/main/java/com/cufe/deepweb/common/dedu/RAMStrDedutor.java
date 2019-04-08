package com.cufe.deepweb.common.dedu;

import com.cufe.deepweb.crawler.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class RAMStrDedutor extends Deduplicator<String> {
    private final Logger logger = LoggerFactory.getLogger(RAMMD5Dedutor.class);
    protected Set<String> deduSet;
    private Path dataPath; //the directory path for data saving


    public RAMStrDedutor() {

    }
    /**
     * use data saving file
     * @param dataPath
     */
    public RAMStrDedutor(Path dataPath){
        this.dataPath = dataPath;
        deduSet = new HashSet<>();
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
    }

    /**
     * use synchronized keyword to support thread-safe
     * @param o
     * @return
     */
    @Override
    public synchronized boolean add(String o) {
        costV++;
        if (dedu(o)) {
            newV++;
            return true;
        }
        return false;
    }

    /**
     * the concrete dedu strategy, the subclass can override this method to provide other operations
     * @param o
     * @return
     */
    protected boolean dedu(String o) {
        return deduSet.add(o);
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
