import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SmallText {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String wiki="F:/experiment/enwiki-20161220-pages-articles-multistream-sample";
		String wiki2="D:/experiment/enwiki-20161220-pages-articles-multistream-sample2";
		String wiki3="D:/experiment/enwiki-20161220-pages-articles-multistream-sample3";
		
		String wiki_total_noredirect="D:/experiment/enwiki-20161220-pages-articles-multistream_noredirect";
		
		Directory dir=FSDirectory.open(new File(wiki_total_noredirect));
		//IndexReader indexReader=IndexReader.open(dir,false);
		IndexReader indexReader=IndexReader.open(dir);
		
		IndexSearcher indexSearcher=new IndexSearcher(indexReader);
		
//		Directory dir2=FSDirectory.open(new File(wiki3));
//		Analyzer standardAnalyzer=new StandardAnalyzer(Version.LUCENE_31);
//		IndexWriterConfig  indexWriterConfig=new IndexWriterConfig(Version.LUCENE_31, standardAnalyzer);
//		IndexWriter indexWriter=new IndexWriter(dir2, indexWriterConfig);

		
		
		int total_num=indexReader.numDocs();
		int i=0,counter=0;
		
		
		TermQuery qi_query=new TermQuery(new Term("text", "redirect"));
		ScoreDoc[] add_to_sample=indexSearcher.search(qi_query, 100000000).scoreDocs;
		System.out.println(add_to_sample.length);
		
//		while(i<total_num)
//		{
//			
//			Document each=indexSearcher.doc(i);
//			String tmp=each.get("text").trim().replace("redirect", "");
//			each.removeField("text");
//			each.add(new Field("text", tmp,Store.YES,Index.ANALYZED));
//			
//			indexWriter.addDocument(each);
//			counter++;
//			
//			
//			i++;
//		}
		System.out.println("原文档个数"+indexReader.numDocs());
		//System.out.println("剩余文档个数为"+counter);
		
//		indexWriter.close();
		indexSearcher.close();
		indexReader.close();
		dir.close();
//		dir2.close();
	}

}
