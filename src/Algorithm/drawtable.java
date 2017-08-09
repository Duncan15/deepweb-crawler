package Algorithm;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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




public class drawtable {
	class point
	{
		int query_number;
		float qual1;
		float qual2;
		int cost;
		point(int query_number,float qual1,float qual2,int cost)
		{
			this.query_number=query_number;
			this.qual1=qual1;
			this.qual2=qual2;
			this.cost=cost;
			
		}
	}
	final static String sample_path="D:/experiment/sample";
	final static String sample_path2="D:/experiment/sample2";
	final static String sample_path3="D:/experiment/sample3";
	final static String sample_path4="D:/experiment/sample4";
	final static String sample_path5="D:/experiment/sample5";
	static Set<Integer> add_to_sample=new HashSet<>();
	Set<Integer> virtual_all_hits=new HashSet<>();
	public void createsample(int sample_size,IndexSearcher db_IndexSearcher,IndexWriter sample_IndexWriter) throws IOException
	{
		
		int max=db_IndexSearcher.maxDoc();
		Random rand=new Random();
		while(add_to_sample.size()<sample_size)
		{
			add_to_sample.add(rand.nextInt(max));
		}
		for(int i :add_to_sample)
		{
			Document added=db_IndexSearcher.doc(i);
			sample_IndexWriter.addDocument(added);
		}
		sample_IndexWriter.commit();
	}
	public ArrayList<point> drawcrossgraph(ArrayList<String> items,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher,Directory a3_Directory,IndexWriter a3_IndexWriter,Algorithm algo) throws IOException
	{
		ArrayList<point> result=new ArrayList<>();
		int n=items.size();
		Map<String, HashSet<Integer>> search_in_DB=new HashMap<>();
		Map<String, Integer> df_in_search_in_DB=new HashMap<>();
		
		for(String eachterm:items)
		{
			TermQuery termQuery=new TermQuery(new Term("Text",eachterm));
			ScoreDoc[] hits=db_IndexSearcher.search(termQuery, 1000000).scoreDocs;
			HashSet<Integer> inner=new HashSet<>();
			for(ScoreDoc every_hit:hits)
			{
				inner.add(every_hit.doc);
			}
			inner.removeAll(add_to_sample);
			search_in_DB.put(eachterm, inner);
			df_in_search_in_DB.put(eachterm, hits.length);
		}
		virtual_all_hits.clear();
		a3_IndexWriter.deleteAll();
		a3_IndexWriter.commit();
		int k=1;
		float qual1=0,qual2=-1,u=0.05f;
		while(k<n)
		{
			k=2*k;
			if(k>n)k=n;
			Algorithm.Quality qual1_unity=algo.getQual1(k, items, search_in_DB, df_in_search_in_DB);
			qual1=qual1_unity.quality;
			int pre_cost=qual1_unity.Coat;
			System.out.println("cost is "+pre_cost);
			qual2=algo.getQual2(add_to_sample,k, items,virtual_all_hits, search_in_DB, df_in_search_in_DB, a3_Directory, a3_IndexWriter, db_IndexReader, db_IndexSearcher, pre_cost).quality;
			result.add(new point(k,qual1,qual2,pre_cost));
		}
		
		return result;
	}
	public static void main(String[] args) throws IOException
	{
		//BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:/experiment/result5.txt"))));
		
		Directory db_Directory=FSDirectory.open(new File(Algorithm.DB_path_Wiki));
		IndexReader db_IndexReader=IndexReader.open(db_Directory);
		IndexSearcher db_IndexSearcher=new IndexSearcher(db_IndexReader);
		
		Directory sample_Directory=FSDirectory.open(new File(sample_path2));
		IndexWriter sample_IndexWriter=new IndexWriter(sample_Directory, new IndexWriterConfig(Version.LUCENE_31, new StandardAnalyzer(Version.LUCENE_31)));
		
		Directory a3_Directory=FSDirectory.open(new File(Algorithm.path_for_algorithm32));
		IndexWriter a3_IndexWriter=new IndexWriter(a3_Directory,new IndexWriterConfig(Version.LUCENE_31, new StandardAnalyzer(Version.LUCENE_31)));
		
		drawtable draw=new drawtable();
		Algorithm algo=new Algorithm();
		int turns=20;
		while(turns-->0)
		{
			System.out.println("µÚ"+turns+"ÂÖ");
			draw.createsample(100,db_IndexSearcher,sample_IndexWriter);
		
			IndexReader sample_IndexReader=IndexReader.open(sample_Directory);
			IndexSearcher sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		
			ArrayList<String> items=algo.Algorithm_2(sample_IndexReader, sample_IndexSearcher, null);
			System.out.println(items.size()+"¸öquery from algorithm2");
			ArrayList<String> results=algo.Algorithm_3(add_to_sample,a3_Directory, a3_IndexWriter, db_IndexReader, db_IndexSearcher, items);
	
			

 			System.out.println("queryµÄ¸öÊýÎª "+results.size());
			
			add_to_sample.clear();
			sample_IndexWriter.deleteAll();
			a3_IndexWriter.deleteAll();
			sample_IndexWriter.commit();
			a3_IndexWriter.commit();
			
			sample_IndexSearcher.close();
			sample_IndexReader.close();
		}
		a3_IndexWriter.close();
		a3_Directory.close();
		
		sample_IndexWriter.close();
		sample_Directory.close();
		
		db_IndexSearcher.close();
		db_IndexReader.close();
		db_Directory.close();

	}

}