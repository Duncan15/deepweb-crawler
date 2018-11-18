package com.cufe.deepweb.common.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.Doc;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class IndexClient implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(IndexClient.class);
    /**
     * 用于判断打开的客户端的状态
     */
    private boolean readOnly;
    private Analyzer analyzer;
    /**
     * directory for storing index，该实例可在文件夹中打开
     */
    private Directory indexDirectory;
    /**
     * indexReader for getting info，只有当索引存在时reader才可以正确打开
     */
    private IndexReader indexReader;
    /**
     * indexWriter for writing index，该类有文件锁，一个文件夹只能由一个实例并发访问
     */
    private IndexWriter indexWriter;
    /**
     * indexSearcher
     */
    private IndexSearcher indexSearcher;
    /**
     * 用于indexSearcher
     */
    private ExecutorService searchThreadPool;
    /**
     * 用于indexSearcher的线程池线程数量
     */
    private int searchThreadNum;
    /**
     * 单条搜索最大击中数量
     */
    private int maxHitNum;
    /**
     * 初始化directory 和 writer
     * @param builder
     */
    private IndexClient(Builder builder){
        this.analyzer = builder.analyzer;
        this.readOnly = builder.readOnly;
        this.searchThreadNum = builder.searchThreadNum;
        this.maxHitNum = builder.maxHitNum;
        try{
            indexDirectory = FSDirectory.open(builder.sampleAddr);
            updateIndex();
        }catch (IOException ex){
            logger.error("IOException in open lucene index",ex);
        }
    }

    /**
     * 本方法用于更新indexReader
     * indexReader只有当索引中有数据时才会更新成功
     */
    private synchronized void updateIndexReader() {
        try {
            if (!DirectoryReader.indexExists(indexDirectory)) return;

            if (indexReader == null) {//第一次新建indexReader
                indexReader = DirectoryReader.open(indexDirectory);
            } else {//更新indexReader
                if (indexReader instanceof DirectoryReader) {
                    IndexReader ir = DirectoryReader.openIfChanged((DirectoryReader) indexReader);
                    if(ir == null){
                        indexReader.close();
                        indexReader = ir;
                    }
                }
            }

        } catch (IOException ex) {
            logger.error("IOException happen when update indexReader", ex);
        }
    }

    /**
     * 本方法用于更新indexSearcher
     */
    private synchronized void updateIndexSearcher() {
        if (indexReader != null) {
            if (indexSearcher == null && searchThreadNum != 0) {//设置了searchThreadNum，且还未初始化indexSearcher
                searchThreadPool = Executors.newFixedThreadPool(searchThreadNum);
            }
            //每次更新都会执行
            if (searchThreadNum == 0) {
                indexSearcher = new IndexSearcher(indexReader);
            } else {
                indexSearcher = new IndexSearcher(indexReader, searchThreadPool);
            }
        }
    }


    /**
     * 本方法用于更新indexWriter
     * 对indexWriter的更新会导致底层索引的改变
     */
    private synchronized void updateIndexWriter() {
        try {
            //如果indexWriter已经存在
            if (indexWriter != null) {
                indexWriter.commit();
                return;
            }

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(indexDirectory,config);
        } catch (IOException ex) {
            logger.error("IOException happen when create new indexWriter", ex);
        }
    }
    /**
     * 用于更新索引，此操作比较消耗资源
     * 注意本方法不用于只用于更新，不用于新建
     */
    public synchronized void updateIndex() {
        if (!readOnly) {//如果为读写客户端，则新建indexWriter
            updateIndexWriter();
        }
        updateIndexReader();
        updateIndexSearcher();
    }


    /**
     * 将map中的k-v对打成索引
     * indexWriter可多线程共享，无需考虑并发问题
     * @param fieldContentPairs
     */
    public void addDocument(Map<String, String> fieldContentPairs) {
        if (readOnly) return;//只读客户端，不可写
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

    /**
     * 从对应的field中根据query搜索数据,获取docID的集合
     * @param field
     * @param query
     * @return
     */
    public Set<Integer> search(String field, String query) {
        Set<Integer> docIDSet = new HashSet<>();
        if (indexSearcher == null) return docIDSet;//还未初始化，直接返回
        ScoreDoc[] scoreDocs = null;
        try {
            scoreDocs = indexSearcher.search(new TermQuery(new Term(field, query)), maxHitNum).scoreDocs;
            for (ScoreDoc scoreDoc : scoreDocs) {
                int k = scoreDoc.doc;
                docIDSet.add(k);
            }
        }catch (IOException ex) {
            logger.error("IOException happen when search", ex);
        }
        return docIDSet;
    }

    /**
     * 根据dicIDSet获取索引中field区域的内容
     * @param field
     * @param docIDSet
     * @return
     */
    public Map<Integer, String> loadDocuments(String field, Set<Integer> docIDSet) {
        Map<Integer, String> docIDValueMap = new HashMap<>();
        if (indexReader == null) return docIDValueMap;
        docIDSet.forEach(id -> {
            String v = "";
            try {
                Document doc = indexReader.document(id, Collections.singleton(field));
                v = doc.get(field);
            } catch (IOException ex) {
                logger.error("IOException happen when read document, docID is {}",id);
            }
            docIDValueMap.put(id, v);
        });
        return docIDValueMap;
    }
    

    /**
     * 获取当前索引最近一次更新时存在的文档数量
     * @return
     */
    public int getDocSize(){
        if (indexReader == null) {
            return 0;
        }
        return indexReader.numDocs();
    }

    /**
     * 获取当前索引最近一次更新时对应field对应范围内的term-set(docId) map
     * @param field
     * @param low
     * @param up
     * @return
     */
    public Map<String, Set<Integer>> getDocSetMap(String field,double low,double up){
        Map<String, Set<Integer>> docSetMap = new HashMap<>();
        if (indexReader == null ) return docSetMap;
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

    @Override
    public void close() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (indexReader != null) {
            indexReader.close();
        }
        if (indexDirectory != null) {
            indexDirectory.close();
        }
        if (searchThreadPool != null) {
            searchThreadPool.shutdown();
        }
    }


    /**
     * 构造器
     */
    public static class Builder {
        private Path sampleAddr;
        private Analyzer analyzer;
        private boolean readOnly;
        private int searchThreadNum;
        private int maxHitNum;
        public Builder(Path addr){
            sampleAddr = addr;
        }

        /**
         * 设置解析器
         * @param analyzr
         */
        public Builder setAnalyzer(Analyzer analyzr){
            analyzer = analyzr;
            return this;
        }

        /**
         * 设置客户端为只读客户端
         */
        public Builder setReadOnly() {
            this.readOnly = true;
            return this;
        }

        /**
         * 设置单条搜索最大击中数量
         * @param maxHitNum
         */
        public Builder setMaxHitNum(int maxHitNum) {
            this.maxHitNum = maxHitNum;
            return this;
        }

        /**
         * 设置搜索线程数量
         * @param searchThreadNum
         */
        public Builder setSearchThreadNum(int searchThreadNum) {
            this.searchThreadNum = searchThreadNum;
            return this;
        }

        public IndexClient build(){
            if (analyzer == null) {
                analyzer = new SmartChineseAnalyzer();
            }
            if (maxHitNum == 0) {
                maxHitNum = Integer.MAX_VALUE;
            }

            return new IndexClient(this);
        }
    }
}
