package com.cufe.deepweb.common.index;

import com.cufe.deepweb.common.Utils;
import org.ansj.lucene6.AnsjAnalyzer;
import org.apache.commons.codec.Charsets;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * after initializing, there must has a directory
 * if this is a writable indexClient, there also has a indexWriter
 * only when the index has data, there would has a indexReader
 */
public final class IndexClient implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(IndexClient.class);
    /**
     * judge whether this client is used to write index
     */
    private boolean readOnly;
    /**
     * the address of index in file system
     */
    private Path indexAddr;
    private Analyzer analyzer;
    /**
     * directory for storing index
     */
    private Directory indexDirectory;
    /**
     * indexReader for getting info，only when index exists in the index this reader will be opened
     */
    private IndexReader indexReader;
    /**
     * indexWriter for writing index，this class have a file lock, a directory can only be written by a instance
     */
    private IndexWriter indexWriter;
    /**
     * indexSearcher
     */
    private IndexSearcher indexSearcher;
    /**
     * thread pool used for indexSearcher to search
     */
    private ExecutorService searchThreadPool;
    /**
     * the pool size of indexSearcher's thread pool
     */
    private int searchThreadNum;
    /**
     * the maximum hit number for unique query
     */
    private int maxHitNum;
    /**
     * initialize directory
     * @param builder
     */
    private IndexClient(Builder builder){
        this.analyzer = builder.analyzer;
        this.readOnly = builder.readOnly;
        this.searchThreadNum = builder.searchThreadNum;
        this.maxHitNum = builder.maxHitNum;
        this.indexAddr = builder.sampleAddr;
        try{
            indexDirectory = FSDirectory.open(this.indexAddr);
            updateIndex();
        }catch (IOException ex){
            logger.error("IOException in open lucene index",ex);
        }
    }

    /**
     * force update index, can used to free memory out of JVM
     */
    public synchronized void forceUpdateIndex() {
        logger.info("start to force update index");
        Utils.logMemorySize();
        try {
            if (!readOnly && indexWriter != null) {
                indexWriter.close();
            }
            if (indexReader != null) {
                indexReader.close();
            }
            if (indexDirectory != null) {
                indexDirectory.close();
            }

            indexDirectory = FSDirectory.open(indexAddr);
            if (DirectoryReader.indexExists(indexDirectory)) {
                indexReader = DirectoryReader.open(indexDirectory);
                indexSearcher = new IndexSearcher(indexReader);
            }

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            if (!readOnly) {
                indexWriter = new IndexWriter(indexDirectory, config);
            }
        } catch (IOException ex) {
            logger.error("force update index error", ex);
        }
        Utils.logMemorySize();
        logger.info("force update index finish");
    }
    /**
     * used to update indexReader
     * the update would be successfully only when there exists data in the index
     */
    private synchronized void updateIndexReader() {
        try {
            if (!DirectoryReader.indexExists(indexDirectory)) {
                logger.info("this index have no data, refuse to initialize indexReader");
                return;
            }

            if (indexReader == null) {//the first time to initialize indexReader
                logger.trace("initialize indexReader");
                indexReader = DirectoryReader.open(indexDirectory);
            } else {//update indexReader
                if (indexReader instanceof DirectoryReader) {
                    IndexReader ir = DirectoryReader.openIfChanged((DirectoryReader) indexReader);
                    if(ir != null) {
                        logger.trace("update indexReader");
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
     * used to update indexSearcher
     */
    private synchronized void updateIndexSearcher() {
        if (indexReader != null) {
            if (indexSearcher == null && searchThreadNum != 0) {//if have set the searchThreadNum，and have never initialized the indexSearcher
                logger.trace("initialize executorService for indexSearcher");
                searchThreadPool = Executors.newFixedThreadPool(searchThreadNum);
            }
            //initialize a new index searcher every time
            logger.trace("update indexSearcher");
            if (searchThreadNum == 0) {
                indexSearcher = new IndexSearcher(indexReader);
            } else {
                indexSearcher = new IndexSearcher(indexReader, searchThreadPool);
            }
        }
    }


    /**
     * used to upate indexWriter
     * the update on indexWriter would cause the underline index to change
     */
    private synchronized void updateIndexWriter() {
        try {
            //if index writer has existed
            if (indexWriter != null) {
                logger.trace("commit indexWriter");
                try {
                    indexWriter.commit();
                } catch (IOException ex) {//often be a AlreadyClosedException here
                    logger.error("error happen when commit", ex);
                    indexWriter = null;
                    updateIndex();//reopen the indexWriter
                }

                return;
            }
            //every time create a new index writer
            logger.trace("initialize indexWriter");
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(indexDirectory, config);
            indexWriter.commit();
        } catch (IOException ex) {
            logger.error("IOException happen when create new indexWriter, exit", ex);
            //now indexWriter would be null or in an unexpected status
            //for example, the disk hasn't enough space
            //can't create an indexWriter, must be an serious exception, should exit
            System.exit(1);
        }
    }
    /**
     * used to update index, won't free memory out of JVM
     */
    public synchronized void updateIndex() {
        logger.trace("start to update index");
        Utils.logMemorySize();
        if (!readOnly) {//如果为读写客户端，则新建indexWriter
            updateIndexWriter();
        }
        updateIndexReader();
        updateIndexSearcher();
        Utils.logMemorySize();
        logger.trace("update index finish");
    }


    /**
     * build the key-value pair from the specified map into index
     * indexWriter is thread-safe under multi-thread environment
     * @param fieldContentPairs
     */
    public void addDocument(Map<String, String> fieldContentPairs) {
        if (readOnly) {
            logger.warn("this client is readOnly, no support to write");
            return;
        }
        if (fieldContentPairs.isEmpty()) return;
        Document doc = new Document();
        for (Map.Entry<String, String> entry : fieldContentPairs.entrySet()) {
            doc.add(new TextField(entry.getKey(), entry.getValue(), Field.Store.YES));
        }
        try{
            indexWriter.addDocument(doc);
        }catch (IOException ex) {//often be a AlreadyClosedException here
            logger.error("error happen when add document",ex);
            synchronized (this) {
                if (!indexWriter.isOpen()) {
                    indexWriter = null;
                    updateIndex();
                }
            }

        }
    }


    /**
     * search by query in the specified field and get the set of hit docID
     * @param field
     * @param query
     * @return
     */
    public Set<Integer> search(String field, String query) {
        logger.info("start to search");
        Utils.logMemorySize();

        if (indexSearcher == null) {
            logger.warn("this indexSearcher hasn't been initialized");
            return Collections.emptySet();
        }
        Set<Integer> docIDSet = new HashSet<>();
        try {
            ScoreDoc[] scoreDocs = indexSearcher.search(new TermQuery(new Term(field, query)), maxHitNum).scoreDocs;
            for (ScoreDoc scoreDoc : scoreDocs) {
                int k = scoreDoc.doc;
                docIDSet.add(k);
            }
        }catch (IOException ex) {
            logger.error("IOException happen when search", ex);
        }

        Utils.logMemorySize();
        logger.info("search finish");
        return docIDSet;
    }

    /**
     * load documents by the set of docID list and add them into a list
     * @param docIDList
     * @return
     */
    public List<Map<String, String>> loadDocuments(List<Integer> docIDList) {
        Utils.logMemorySize();
        if (indexReader == null) {
            logger.warn("this indexReader hasn't been initialized");
            return Collections.emptyList();
        }
        List<Map<String, String>> docList = new ArrayList<>(docIDList.size());
        docIDList.forEach(id -> {
            try {
                Map<String, String> doc = new HashMap<>();
                Document document = indexReader.document(id);
                document.forEach(indexableField -> doc.put(indexableField.name(), indexableField.stringValue()));
                docList.add(doc);
            } catch (IOException ex) {
                logger.error("IOException happen when read document, docID is {}",id);
            }
        });
        Utils.logMemorySize();
        return docList;
    }
    /**
     * load the specified field of documents by the list of docID into a list
     * @param field
     * @param docIDList
     * @return
     */
    public List<String> loadDocuments(String field, Set<Integer> docIDList) {
        if (indexReader == null) {
            logger.warn("this indexReader hasn't been initialized");
            return Collections.emptyList();
        }
        List<String> contentList = new ArrayList<>();
        docIDList.forEach(id -> {
            String v = "";
            try {
                Document doc = indexReader.document(id, Collections.singleton(field));
                v = doc.get(field);
            } catch (IOException ex) {
                logger.error("IOException happen when read document, docID is {}",id);
            }
            contentList.add(v);
        });
        return contentList;
    }

    /**
     * directly write document from source index to target index and don't change the document format(such as field name and so on)
     * @param targetClient
     * @param docIDSet the set of docID which would be processed
     */
    public void write2TargetIndex(IndexClient targetClient, Set<Integer> docIDSet, int parallelNum) {
        logger.info("download num is {}", docIDSet.size());
        logger.info("start to download");
        Utils.logMemorySize();
        if (targetClient.readOnly || indexReader == null || targetClient.indexWriter == null) {
            logger.warn("unable to write to target client");
            return;
        }
        ExecutorService service = Executors.newFixedThreadPool(parallelNum);
        BlockingDeque<Integer> queue = new LinkedBlockingDeque<>(parallelNum * parallelNum);
        AtomicInteger downloadNum = new AtomicInteger(0);
        Runnable produceTask = () -> {
            for (int id : docIDSet) {
                try {
                    queue.put(id);
                } catch (InterruptedException ex) {
                    logger.error("interrupted in put id {} into queue", id);
                }
            }
            logger.trace("exit produce thread");
        };
        Runnable consumeTask = () -> {
            while (true) {
                try {
                    Integer id = queue.poll(parallelNum, TimeUnit.SECONDS);
                    if (id == null) {
                        if (docIDSet.size() - downloadNum.get() < 100) break;
                        continue;
                    }
                    Document doc = indexReader.document(id);
                    targetClient.indexWriter.addDocument(doc);
                    downloadNum.incrementAndGet();
                } catch (InterruptedException ex) {
                    logger.error("interrupted in load document and write it to index");
                } catch (IOException ex) {
                    logger.error("IOException happen when read and write document to target index");
                }
            }
            logger.trace("exit consume thread");
        };
        service.execute(produceTask);
        for (int i = 0 ; i < parallelNum - 1 ; i ++) {
            service.execute(consumeTask);
        }
        service.shutdown();
        while (true) {
            try {
                if (service.awaitTermination(parallelNum, TimeUnit.SECONDS)) {
                    break;
                }
                logger.trace("have download {} documents", downloadNum.get());
            } catch (InterruptedException ex) {
                logger.error("interrupted when wait for thread pool");
            }
        }

        Utils.logMemorySize();
        logger.info("download finish");
    }


    /**
     * get the document size at the last time to update index
     * @return
     */
    public int getDocSize(){
        if (indexReader == null) {
            logger.warn("this indexReader hasn't been initialized");
            return 0;
        }
        return indexReader.numDocs();
    }

    /**
     * get current index's term-set(docId) map in the specified field between the specified DF range after the latest update
     * note: this method view the index as a entirety
     * @param field
     * @param low
     * @param up
     * @return
     */
    public Map<String, Set<Integer>> getDocSetMap2(String field,double low,double up) {
        logger.info("start to get doc set map");
        Utils.logMemorySize();

        Map<String, Set<Integer>> docSetMap = new HashMap<>();
        if (indexReader == null) {
            logger.warn("this indexReader hasn't been initialized");
            return docSetMap;
        }
        int size = indexReader.numDocs();
        try{
            Terms terms = MultiFields.getTerms(indexReader, field);
            TermsEnum termsEnum = terms.iterator();
            while (termsEnum.next() != null) {
                String term = termsEnum.term().utf8ToString();
                if((low * size) < termsEnum.docFreq() && termsEnum.docFreq() <= (up * size)) {
                    //the current algorithm design wouldn't think about delete references in index, therefore don't prepare for deleting
                    PostingsEnum postingsEnum = termsEnum.postings(null, PostingsEnum.NONE);
                    int id = 0;
                    Set<Integer> docSet = new HashSet<>();
                    while ((id = postingsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        docSet.add(id);
                    }
                    docSetMap.put(term,docSet);
                }
            }
        }catch (IOException ex){
            logger.error("IOException in read lucene index", ex);
        }

        logger.info("map size is {}", docSetMap.size());
        Utils.logMemorySize();
        logger.info("get doc set map finish");
        return docSetMap;
    }

    /**
     * get current index's term-set(docId) map in the specified field between the specified DF range after the latest update
     * note: this method view the index as a set of some sub index
     * @param field
     * @param low
     * @param up
     * @return
     */
    public Map<String, Set<Integer>> getDocSetMap(String field,double low,double up) {
        logger.trace("start to get doc set map");
        Utils.logMemorySize();

        if (indexReader == null) {
            logger.warn("this indexReader hasn't been initialized");
            return Collections.emptyMap();
        }
        Map<String, Set<Integer>> docSetMap = new HashMap<>();
        int size = indexReader.numDocs();

        logger.trace("start to get all the terms which fit the target bound range");
        try{
            Terms terms = MultiFields.getTerms(indexReader, field);
            if (terms == null) return Collections.emptyMap();
            TermsEnum termsEnum = terms.iterator();
            while (termsEnum.next() != null) {
                String term = termsEnum.term().utf8ToString();
                if((low * size) < termsEnum.docFreq() && termsEnum.docFreq() <= (up * size)) {
                    docSetMap.put(term, new HashSet<>());
                }
            }

        }catch (IOException ex){
            logger.error("IOException in read lucene index", ex);
        }
        logger.trace("get terms finish");
        if (docSetMap.size() == 0) {
            return Collections.emptyMap();
        }
        ExecutorService service = Executors.newFixedThreadPool(indexReader.leaves().size());//thread pool to operate the sub index
        List<LeafReaderContext> leafList = indexReader.leaves();
        logger.trace("sub index size:{}", leafList.size());
        List<Future> futureList = new ArrayList<>();
        for (int i = 0 ; i < leafList.size() ; i ++) {
            int curIndex = i;
            futureList.add(service.submit(() -> {
                LeafReaderContext ct = leafList.get(curIndex);
                LeafReader reader = ct.reader();
                try {
                    Terms terms = reader.fields().terms(field);
                    TermsEnum termsEnum = terms.iterator();
                    while (termsEnum.next() != null) {
                        String term = termsEnum.term().utf8ToString();

                        //if the DF of current term is not between the target bound range
                        if (!docSetMap.containsKey(term)) {
                            continue;
                        }

                        PostingsEnum postingsEnum = termsEnum.postings(null, PostingsEnum.NONE);
                        int id = 0;
                        Set<Integer> tmpSet = new HashSet<>();
                        while ((id = postingsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                            tmpSet.add(id);
                        }
                        Set<Integer> docSet = docSetMap.get(term);
                        synchronized (docSet) {
                            docSet.addAll(tmpSet);
                        }
                    }
                } catch (IOException ex) {
                    logger.error("IOException happen when read from sub index reader", ex);
                }

                return;
            }));
        }
        service.shutdown();
        logger.trace("DF bound is between {} and {}", low * size, up * size);
        while (futureList.size() > 0) {
            for (int i = 0 ; i < futureList.size() ;) {
                Future<Map<String, Set<Integer>>> f = futureList.get(i);
                if (f.isDone()) {
                    futureList.remove(i);
                    Utils.logMemorySize();
                    logger.trace("least size of the list of future is {}", futureList.size());
                    continue;
                }
                i ++;
            }
        }

        Utils.logMemorySize();
        logger.trace("get doc set map finish");
        return docSetMap;
    }
    /**
     * get the metadata information of current index
     * @return size/fields/leaves
     */
    public Map<String, Object> getIndexInfo() {
        Map<String, Object> infoMap = new HashMap<>();
        if (indexReader == null) {
            logger.warn("this indexReader hasn't been initialized");
            return infoMap;
        }
        //collect the document number in the index
        infoMap.put("size", indexReader.numDocs());
        //collect all the field name in the index
        Set<String> fieldSet = new HashSet<>();
        try {
            Fields fields = MultiFields.getFields(indexReader);
            fields.forEach( field -> fieldSet.add(field));
        } catch (IOException ex) {
            logger.info("IOException happen when read index", ex);
        }
        infoMap.put("fields", fieldSet);
        infoMap.put("leaves", indexReader.leaves().size());
        return infoMap;
    }

    /**
     * load the specified field of document in the index into the directory
     * set the file name in the directory as docID.extension
     * @param field
     * @param dir
     * @param extension the extension file name
     * @param charset
     */
    public void loadFieldIntoDirectory(String field, Path dir, String extension, Charset charset) {
        int num = this.getDocSize();
        if(num == 0) {
            return;
        }
        if (!extension.startsWith(".")) {
            extension = "." +extension;
        }
        for (int i = 1 ; i <= num ; i++) {
            try {
                Document doc = indexReader.document(i, Collections.singleton(field));
                String content = doc.get(field);
                Path addr = dir.resolve(i + extension);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(addr.toFile()), charset.name()));
                bw.write(content);
                bw.close();
            } catch (IOException ex) {

            }

        }
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

    public enum AnalyzerTpye {
        en,cn
    }
    /**
     * builder
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
         * set the analyzer
         * @param type analyzer type @AnalyzerTpye
         */
        public Builder setAnalyzer(AnalyzerTpye type){
            if (type == AnalyzerTpye.cn) {
                this.analyzer = new AnsjAnalyzer(AnsjAnalyzer.TYPE.nlp_ansj);
            } else if (type == AnalyzerTpye.en) {
                this.analyzer = new StandardAnalyzer();
            }
            return this;
        }

        /**
         * set this client to a read-only client
         */
        public Builder setReadOnly() {
            this.readOnly = true;
            return this;
        }

        /**
         * set the maximum number of hit documents in a unique query
         * @param maxHitNum
         */
        public Builder setMaxHitNum(int maxHitNum) {
            this.maxHitNum = maxHitNum;
            return this;
        }

        /**
         * set the thread number used to search the index
         * @param searchThreadNum
         */
        public Builder setSearchThreadNum(int searchThreadNum) {
            this.searchThreadNum = searchThreadNum;
            return this;
        }

        public IndexClient build(){
            if (analyzer == null) {
                analyzer = new StandardAnalyzer();
            }
            if (maxHitNum == 0) {
                maxHitNum = Integer.MAX_VALUE;
            }

            return new IndexClient(this);
        }
    }
    public static void main(String[] args) throws IOException {
        IndexClient client = new IndexClient.Builder(Paths.get("G:/Indexjieba11")).setReadOnly().build();
        client.loadFieldIntoDirectory("body", Paths.get("F:/html_file1/"), ".html", Charsets.UTF_8);
        client.close();
    }
}
