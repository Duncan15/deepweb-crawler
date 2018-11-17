package com.cufe.deepweb.common.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class IndexClient {
    private final Logger logger = LoggerFactory.getLogger(IndexClient.class);
    private Analyzer analyzer;
    private Directory indexDirectory;//directory for storing index
    private IndexReader indexReader;//indexReader for getting info
    private IndexWriter indexWriter;//indexWriter for writing index
    private IndexClient(Builder builder){
        analyzer = builder.analyzer;
        try{
            indexDirectory = FSDirectory.open(builder.sampleAddr);
            initIndexWriter();
        }catch (IOException ex){
            logger.error("IOException in open lucene index",ex);
        }
    }

    /**
     * 本方法用于初始化indexReader
     * indexReader只有当索引中有数据时才会初始化成功
     */
    private void initIndexReader() {
        try {
            indexReader = DirectoryReader.open(indexDirectory);
        } catch (IOException ex) {
            logger.error("IOException happen when create new indexReader", ex);
        }
    }

    /**
     * 本方法用于初始化indexWriter
     */
    private void initIndexWriter() {
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(indexDirectory,config);
        } catch (IOException ex) {
            logger.error("IOException happen when create new indexWriter", ex);
        }
    }
    /**
     * 用于更新索引，此操作比较消耗资源
     * 注意本方法不用于新建indexReader，但可用于新建indexWriter
     * 此方法多线程不安全，不支持并发
     */
    public void updateIndex() {
        try{
            //先对indexWriter的内容进行commit
            if (indexWriter != null) {
                indexWriter.close();
            }

            //然后再更新indexReader
            if(indexReader != null){//当更新indexReader时,注意本方法不用于新建indexReader
                if(indexReader instanceof DirectoryReader){
                    IndexReader ir = DirectoryReader.openIfChanged((DirectoryReader) indexReader);
                    if(ir == null){
                        indexReader.close();
                        indexReader = ir;
                    }
                }
            }

            //然后新建indexWriter
            initIndexWriter();
        }catch (IOException ex){
            logger.error("error happen when update index",ex);
        }
    }


    /**
     * 将map中的k-v对打成索引
     * indexWriter可多线程共享，无需考虑并发问题
     * @param fieldContentPairs
     */
    public void addDocument(Map<String, String> fieldContentPairs) {
        if (fieldContentPairs.isEmpty()) return;
        Document doc = new Document();
        for (Map.Entry<String, String> entry : fieldContentPairs.entrySet()) {
            doc.add(new TextField(entry.getKey(), entry.getValue(), Field.Store.YES));
        }
        try{
            indexWriter.addDocument(doc);
        }catch (IOException ex){
            logger.error("error happen when add document",ex);
        }
    }

    /*
    以下方法供算法使用,仅会单线程调用
     */
    public int getDocSize(){
        if (indexReader == null) {
            initIndexReader();
        }
        return indexReader.numDocs();
    }
    public Map<String, Set<Integer>> getDocSetMap(String field,double low,double up){
        Map<String, Set<Integer>> docSetMap = new HashMap<>();
        if (indexReader == null ) initIndexReader();
        int size = indexReader.numDocs();
        try{
            Terms terms = MultiFields.getTerms(indexReader,field);
            TermsEnum termsEnum = terms.iterator();
            while (termsEnum.next() != null){
                String term = termsEnum.term().utf8ToString();
                if((low*size) < termsEnum.docFreq() && termsEnum.docFreq() <= (up*size)){
                    //当前算法设计中不会对已存在对lucene索引进行删除，因此不需要考虑删除的情况
                    PostingsEnum postingsEnum = termsEnum.postings(null,PostingsEnum.NONE);
                    int id = 0;
                    Set<Integer> docSet = new HashSet<>();
                    while ((id = postingsEnum.nextDoc())!= DocIdSetIterator.NO_MORE_DOCS){
                        docSet.add(id);
                    }
                    docSetMap.put(term,docSet);
                }
            }
        }catch (IOException ex){
            logger.error("IOException in read lucene index",ex);
        }
        return docSetMap;
    }
    /*
    构造器
     */
    public static class Builder {
        private Path sampleAddr;
        private Analyzer analyzer;
        public Builder(Path addr){
            sampleAddr = addr;
        }

        public void setAnalyzer(Analyzer analyzr){
            analyzer = analyzr;
        }

        public IndexClient build(){
            if (analyzer == null) {
                analyzer = new SmartChineseAnalyzer();
            }
            return new IndexClient(this);
        }
    }
}
