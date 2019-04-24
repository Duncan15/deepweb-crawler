package com.cufe.deepweb.algorithm;

import com.cufe.deepweb.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * abstract class for crawling algorithm
 * query-generate algorithm，this class only focus on query generation logic
 */
public abstract class AlgorithmBase {
    private final Logger logger = LoggerFactory.getLogger(AlgorithmBase.class);
    private List<String> qList; //the list which store all seleted queries
    private int qCount; //the new generated query index
    private Path productPath;

    /**
     * the name of data saving file for production mode
     */
    private static final String DATA_FILE = "qList.dat";

    public AlgorithmBase(Builder builder) {
        this.productPath = builder.productPath;
        qList = new ArrayList<>();
        this.productInit();
        qCount = qList.size();
        if (this.qList.size() == 0) {
            setInitQuery("consume");//set the default initial query, the implementation class can cover this value
        }
    }
    /*
    for set initial query when algorithm initiates
     */
    public final void setInitQuery(final String init) {
        qCount = 0;
        qList.clear();
        qList.add(init);
    }

    /**
     * get the term list generate by current algorithm
     * @return
     */
    public final List<String> getqList(){
        return Collections.unmodifiableList(qList);
    }

    /**
     * rebuild the term list, this method should only be invoked on production mode
     */
    public final void setqList(List<String> list) {
        this.qList.clear();
        this.qList.addAll(list);
        this.qCount = qList.size();
    }

    /**
     * if return null that represent something error happen
     * @return
     */
    public final String getNextQuery() {
        logger.trace("start to infer query");
        Utils.logMemorySize();

        //if the query to get is not the first one
        // invoke the implementation class's override method generateQuery() by dynamic binding
        if(qCount != 0) {
            String nextQuery = generateQuery();
            if (nextQuery == null) return null;//something error happen
            logger.trace("get query {}", nextQuery);
            qList.add(nextQuery);
        }

        String query = qList.get(qCount);
        qCount++;
        Utils.logMemorySize();
        logger.trace("infer finish");
        return query;
    }

    /*
    the implementing class must implement this method to generate next query
    */
    protected abstract String generateQuery();


    /**
     * do some initial operations for production mode
     */
    private void productInit() {
        if (this.productPath != null) {
            File f = this.productPath.resolve(DATA_FILE).toFile();
            if(f.exists()) {
                logger.info("start to read qList information from file {}", f.getAbsolutePath());
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(f))) {
                    List<String> qList = new ArrayList<>();
                    bufferedReader.lines().forEach(line -> qList.add(line));
                    logger.info("the size of qList read from file is " + qList.size());
                    this.setqList(qList);
                } catch (IOException ex) {
                    logger.error("Exception happen when read qList object file");
                } finally {
                    logger.info("read qList information finish");
                    f.delete();
                }
            }
        }
    }

    /**
     * the implementation class can override this method for doing some close operations
     */
    public void close(){
        if(this.productPath != null) {
            System.out.println("start to store qList information");
            File f = this.productPath.resolve(DATA_FILE).toFile();
            if (f.exists()) {
                System.out.println("the qList data saving file has existed, exit directly");
                return;
            }
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f))) {
                for (String e : this.getqList()) {
                    bufferedWriter.write(e);
                    bufferedWriter.newLine();
                }
            } catch (IOException ex) {
                //ignored
            }
            System.out.println("store qList information finish");
        }
    }
    public static class Builder {
        private Path productPath;
        public Builder setProductPath(Path productPath) {
            this.productPath = productPath;
            return this;
        }
        public AlgorithmBase build() {return null;}
    }
}
