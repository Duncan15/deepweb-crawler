package Algorithm;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import nlpirToken.NLPIRTokenizerAnalyzer;

public class SmallText {
	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		//String wiki="F:/experiment/enwiki-20161220-pages-articles-multistream-sample";
		//String wiki2="D:/experiment/enwiki-20161220-pages-articles-multistream-sample2";
		//String wiki3="D:/experiment/enwiki-20161220-pages-articles-multistream-sample3";
		
		//String wiki_total_noredirect="D:/experiment/enwiki-20161220-pages-articles-multistream_noredirect";
		NLPIRTokenizerAnalyzer nta = new NLPIRTokenizerAnalyzer("F:/Java-external-library/NPLIR20140928", 1, "", "", false);
		IndexWriterConfig indexWriterConfig=new IndexWriterConfig(nta);
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter indexWriter=new IndexWriter(FSDirectory.open(Paths.get("F:/experiment/chinesetext4")),indexWriterConfig);
		
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("F:/experiment/Index269")));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		QueryParser parser = new QueryParser("body", nta);
		Query query = parser.parse("with");
		TopDocs top=indexSearcher.search(query, 100000000);
		ScoreDoc[] add_to_sample = top.scoreDocs;
		
		
		
		//Directory dir=FSDirectory.open(Paths.get(Algorithm.chinese_DB));
		//IndexReader indexReader=IndexReader.open(dir);
		//IndexSearcher indexSearcher=new IndexSearcher(indexReader);
		
		//Directory dir2=FSDirectory.open(new File("F:/experiment/chinesetext4"));
		//Analyzer standardAnalyzer=new NlpirAnalyzer();
		//NLPIRTokenizerAnalyzer nta = new NLPIRTokenizerAnalyzer("", 1, "", "", false);
		//IndexWriterConfig  indexWriterConfig=new IndexWriterConfig(Version.LUCENE_36, standardAnalyzer);
		//IndexWriter indexWriter=new IndexWriter(dir2, indexWriterConfig);
		//int total_num=indexReader.numDocs();
		int i=0,counter=0;
		
		//TermQuery qi_query=new TermQuery(new Term("body", "with"));
		//ScoreDoc[] add_to_sample=indexSearcher.search(qi_query, 100000000).scoreDocs;
		System.out.println("总共"+add_to_sample.length);
		for(ScoreDoc each:add_to_sample)
		{
			i++;
			Document docu=indexSearcher.doc(each.doc);
			//try
			{
				//String text = docu.get("body");
				//System.out.println(text);
				indexWriter.addDocument(docu);
				//indexWriter.commit();
				System.out.println(i);
			}
			//catch(Error e)
			{
				//e.printStackTrace();
				//String content=docu.get("body");
			}
			
		}
		
//		while(i<total_num)
//		{
//			
//			Document each=indexSearcher.doc(i);
//			try
//			{
//				
//				indexWriter.addDocument(each);
//				counter++;
//			}
//			catch(Error e)
//			{
//				e.printStackTrace();
//				System.out.println(each.get("body"));
//			}
//			
//			
//			
//			
//			i++;
//		}
//		System.out.println("原文档个数"+indexReader.numDocs());
//		System.out.println("可用文档个数为"+counter);
		//indexWriter.forceMerge(1);
		indexWriter.close();
		//indexSearcher.close();
		indexReader.close();
		//dir.close();
		//dir2.close();
	}

}
