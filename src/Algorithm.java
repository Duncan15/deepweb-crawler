import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.SysexMessage;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
public class Algorithm {
	public class Quality
	{
		int New;
		int Coat;
		float quality;
		public Quality(int New,int Coat,float quality)
		{
			this.New=New;
			this.Coat=Coat;
			this.quality=quality;
		}
	}
	final static String DB_path="D:/experiment/Algorithm_1_DB";
	final static String sample_D_path="D:/experiment/sample";
	final static String DB_path_Wiki="D:/experiment/enwiki-20161220-pages-articles-multistream-index-sample";
	final static String path_for_algorithm3="D:/experiment/path_in_algorithm3";
	final static String path_for_algorithm32="D:/experiment/2path_in_algorithm3";
	final static String path_for_algorithm33="D:/experiment/3path_in_algorithm3";
	final static String path_for_algorithm34="D:/experiment/4path_in_algorithm3";
	final static String path_for_algorithm35="D:/experiment/5path_in_algorithm3";
	
	static String initial_queries="caucus";

	static Float lambda=null;
	//output contains a list of <Size,queries>pairs
	static Map<Integer,Integer> Output_1=new HashMap<Integer,Integer>();
	//q is a set of the queries
	static Set<String> q=new HashSet<>();
	//new_q is a set of the new queries
	static Set<String> new_q=new HashSet<>();
	//hits is a set of ScoreDoc which has been hit


	//used for algorithm2
	static Set<Integer> all_hits=new HashSet<>();
	Map<Integer, ScoreDoc> s_in_Algorithm_2=new HashMap<>();
	Map<String, Integer> df_D=new HashMap<>();//all the items from D
	Map<String, HashSet<Integer>> update_df_D=new HashMap<>();//the dynamic update df_D


	//used for algorithm3
	static Set<String> virtual_q=new HashSet<>();
	static Set<Integer> virtual_all_hits=new HashSet<>();



	static Analyzer analyzer=new StandardAnalyzer(Version.LUCENE_31);
	static IndexWriterConfig  indexWriterConfig=new IndexWriterConfig(Version.LUCENE_31, analyzer);
	static IndexWriterConfig indexWriterConfig2=new IndexWriterConfig(Version.LUCENE_31, analyzer);
	
	public Quality getQual1(Set<Integer> all_hits_set,int k,ArrayList<String> Terms,Map<String, HashSet<Integer>> search_in_DB,Map<String, Integer> df_in_search_in_DB)
	{
		int New=0;
		int Cost=0;
		Cost=k*100;
		Set<Integer> tmp_All=new HashSet<>();
		for(int i=0;i<k;i++)
		{
			
			tmp_All.addAll(search_in_DB.get(Terms.get(i)));
			Cost+=df_in_search_in_DB.get(Terms.get(i));
			
			
		}
		New=tmp_All.size();
		System.out.println("quality1="+(float)New/Cost); 
		return new Quality(New, Cost, (float)New/Cost);
	}
	public Quality getQual2(Set<Integer> all_hits_set,int k,ArrayList<String> Terms,Set<Integer> virtual_all_hits,Map<String, HashSet<Integer>> search_in_DB,Map<String, Integer> df_in_search_in_DB,Directory a3_Directory,IndexWriter a3_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher,int pre_cost) throws IOException
	{
			k=k/2;
			Quality preQual=getQual1(all_hits_set,k, Terms, search_in_DB, df_in_search_in_DB);
			float qual1=preQual.quality;
			int qual1_cost=preQual.Coat;
			virtual_q.clear();
//			virtual_q.add(initial_queries);
//			virtual_q.addAll(q);
			
			for(int i=0;i<k;i++)
			{
				virtual_q.add(Terms.get(i));
			}
			
			
			
			add_from_original_to_sample(virtual_all_hits,a3_IndexWriter, db_IndexReader, db_IndexSearcher, virtual_q);
			//the first half

			IndexReader a3_IndexReader=IndexReader.open(a3_Directory);
			IndexSearcher a3_IndexSearcher=new IndexSearcher(a3_IndexReader);
			ArrayList<String> virtual_Term=Algorithm_2(a3_IndexReader, a3_IndexSearcher,virtual_q);

			float improve_cost=(float) (0.0001*virtual_all_hits.size()*update_df_D.size());

			int left_cost=pre_cost-qual1_cost-(int)improve_cost;
			int New=0;
			HashSet<Integer> inner=new HashSet<>();
			
			
			
			for(int local_cost=0,i=0;;i++)
			{
				TermQuery termQuery=new TermQuery(new Term("text",virtual_Term.get(i)));
				ScoreDoc[] hits=db_IndexSearcher.search(termQuery, 1000000).scoreDocs;
				local_cost=local_cost+100+hits.length;
				if(local_cost<left_cost)
				{
					
					
					for(ScoreDoc every_hit:hits)
					{
						inner.add(every_hit.doc);
					}
					
					//System.out.println("quality2:query:\t"+virtual_Term.get(i));//text
					//System.out.println("quality2:new\t"+inner.size());//text
					//System.out.println("quality2:cost\t"+hits.length);//text
					
				}
				else
					break;
			}
			inner.removeAll(virtual_all_hits);
			
			
			New=inner.size();
			
			
			New=New+preQual.New;
			a3_IndexSearcher.close();
			a3_IndexReader.close();
			System.out.println("quality2="+(float)New/pre_cost); 
			
			
			return new Quality(New, pre_cost, (float)New/pre_cost);
	}
	//method to add documents from original database to sample database
	
	
	public void initial_add_from_original_to_sample_by_SET(Set<Integer> allhit,IndexWriter d_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher) throws IOException, IOException
	{
		for(int i :allhit)
		{
			Document added=db_IndexSearcher.doc(i);
			d_IndexWriter.addDocument(added);
		}
		d_IndexWriter.commit();
	}
	public float add_from_original_to_sample(Set<Integer> allhit,IndexWriter d_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher,Set<String> new_q) throws IOException
	{
		int i=0;
		float result=0;
		for (String query:new_q)
		{
			TermQuery termQuery=new TermQuery(new Term("text", query));
			ScoreDoc[] hits=db_IndexSearcher.search(termQuery, 1000000).scoreDocs;
			result+=hits.length;
			for(ScoreDoc hit:hits)
			{
				if(!allhit.contains(hit.doc))
				{
					Document document=db_IndexSearcher.doc(hit.doc);
					d_IndexWriter.addDocument(document);
					allhit.add(hit.doc);
				}
			}
			d_IndexWriter.commit();
		}

		return result;
	}
	public ArrayList<String> Algorithm_2(IndexReader d_IndexReader,IndexSearcher d_IndexSearcher,Set<String> q_be_checked) throws IOException
	{
		int i=1;
		s_in_Algorithm_2.clear();
		ArrayList<String> Terms=new ArrayList<>();
		df_D.clear();
		update_df_D.clear();
		int d_Size=d_IndexReader.numDocs();
		TermEnum d_Enum=d_IndexReader.terms();
		while (d_Enum.next())
		{
			if(d_Enum.term().field().equals("text"))
			{
				if((0.02*d_Size)<d_Enum.docFreq()&&d_Enum.docFreq()<=(0.15*d_Size))
				{
					df_D.put(d_Enum.term().text(), d_Enum.docFreq());
					TermQuery termQuery=new TermQuery(new Term("text", d_Enum.term().text()));
					ScoreDoc[] hits=d_IndexSearcher.search(termQuery, 1000000).scoreDocs;
					HashSet<Integer> inner_Set=new HashSet<>();
					for(ScoreDoc hit:hits)
					{
						inner_Set.add(hit.doc);
					}
					update_df_D.put(d_Enum.term().text(),inner_Set);
				}
			}
		}
		if(q_be_checked!=null)
		{
			for(String per_Q:q_be_checked)
			{
				if(df_D.containsKey(per_Q))
				{
					df_D.remove(per_Q);
					update_df_D.remove(per_Q);
				}
			}

		}
		//the above is initial


		while(s_in_Algorithm_2.size()<0.999*d_Size)
		{
			Set<String> T=new HashSet<>();
			String qi_final=null;
			float max_Fre=0.0f;//the bigst new/cost value which has been known.
			int maximize=0;//the bigest df value which has been known.
			float maximum=0.0f;//tmp


			//select the queries which have the bigest new/cost value
			for(String each_In_Map:df_D.keySet())
			{
				if(update_df_D.get(each_In_Map).size()!=0)
				{
					maximum=(float)update_df_D.get(each_In_Map).size()/(float)df_D.get(each_In_Map);//select the queries which have the bigest new/cost value
					if(maximum>max_Fre)
					{
						max_Fre=maximum;
						T.clear();
						T.add(each_In_Map);
					}
					else if(maximum==max_Fre)
					{
						T.add(each_In_Map);
					}
				}
				
			}


			//select the query which has the bigest df value
			if(T.size()>1)
			{
				maximize=0;
				for(String qi:T)
				{
					if(maximize<df_D.get(qi))
					{
						maximize=df_D.get(qi);
						qi_final=qi;
					}
				}


				
				df_D.remove(qi_final);
				
			}
			else if(T.size()==1)
			{
				for(String qi:T)
				{
					qi_final=qi;
					maximize=df_D.get(qi);
					df_D.remove(qi);
				}
			}
			else if(T.size()==0)break;//stop if can't achieve the 0.999 and can't get another query


			Terms.add(qi_final);
			TermQuery qi_query=new TermQuery(new Term("text", qi_final));
			ScoreDoc[] add_to_s=d_IndexSearcher.search(qi_query, 1000000).scoreDocs;


			Set<Integer> to_be_sub=new HashSet<>();
			for(ScoreDoc every_add_to_s:add_to_s)
			{
				to_be_sub.add(every_add_to_s.doc);
				if (!s_in_Algorithm_2.containsKey(every_add_to_s.doc))
				{
					s_in_Algorithm_2.put(every_add_to_s.doc, every_add_to_s);
				}
			}
			
			for(String iterator:update_df_D.keySet())
			{
				update_df_D.get(iterator).removeAll(to_be_sub);
			}
			i++;
		}
		
		return Terms;
	}
	public Set<String> Algorithm_3(Set<Integer> asset,Directory a3_Directory,IndexWriter a3_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher,ArrayList<String> Terms) throws IOException
	{
		new_q.clear();

		//calculate the new and cost for every query
		Map<String, HashSet<Integer>> search_in_DB=new HashMap<>();
		Map<String, Integer> df_in_search_in_DB=new HashMap<>();
		for(String eachTerm:Terms)
		{
			TermQuery termQuery=new TermQuery(new Term("text",eachTerm));
			ScoreDoc[] hits=db_IndexSearcher.search(termQuery, 100000).scoreDocs;
			HashSet<Integer> inner=new HashSet<>();
			for(ScoreDoc every_hit:hits)
			{
				inner.add(every_hit.doc);
			}
			inner.removeAll(asset);
			search_in_DB.put(eachTerm, inner);//as new
			df_in_search_in_DB.put(eachTerm, hits.length);//as cost
		}


		virtual_all_hits.clear();
		a3_IndexWriter.deleteAll();
		a3_IndexWriter.commit();
		virtual_all_hits.addAll(asset);
		initial_add_from_original_to_sample_by_SET(asset, a3_IndexWriter, db_IndexReader, db_IndexSearcher);


		int k=1;
		float qual1=0,qual2=-1,u=0.05f;
		while(qual1>qual2)
		{
			k=2*k;
			if(k<=Terms.size())
			{
				Quality qual1_unity=getQual1(asset,k, Terms, search_in_DB, df_in_search_in_DB);
				qual1=qual1_unity.quality;
				int pre_cost=qual1_unity.Coat;
 				qual2=getQual2(asset,k, Terms, virtual_all_hits,search_in_DB, df_in_search_in_DB, a3_Directory, a3_IndexWriter, db_IndexReader, db_IndexSearcher, pre_cost).quality;
 				System.out.println("k="+k);
 				System.out.println("\n\n\n");
			}
			else
				break;
		}
		if(Math.abs((double)(qual2-qual1)/(qual1+qual2))<u&&k<=Terms.size())
		{
			for(int i=0;i<k;i++)
			{
				new_q.add(Terms.get(i));
			}
			
			System.out.println("ture ture ture       new/cost="+qual1);
			System.out.println("退出的k="+k);
			System.out.println("\n\n\n");
			return new_q;
		}

		int step=k/4,p=k/2+step,r=k/2;
		
		virtual_all_hits.clear();//clear the virtual_all_hits
		a3_IndexWriter.deleteAll();
		a3_IndexWriter.commit();
		virtual_all_hits.addAll(asset);
		initial_add_from_original_to_sample_by_SET(asset, a3_IndexWriter, db_IndexReader, db_IndexSearcher);
		
		while(true)
		{
			
			while(p>Terms.size())
			{
				System.out.println("the query number got in algorithm3 is more than the number in algorithm2");
				step=step/2;
				p=p-step;
				if(step<1)
				{
					k=r;
					for(int i=0;i<k;i++)
					{
						new_q.add(Terms.get(i));
					}
					System.out.println("new/cost="+qual1);
					System.out.println("退出的k="+k);
					System.out.println("\n\n\n");
					return new_q;
				}
			}
			Quality qual1_unity=getQual1(asset,p, Terms, search_in_DB, df_in_search_in_DB);
			
			
			qual1=qual1_unity.quality;
			qual2=getQual2(asset,p, Terms, virtual_all_hits,search_in_DB, df_in_search_in_DB, a3_Directory, a3_IndexWriter, db_IndexReader, db_IndexSearcher,qual1_unity.Coat ).quality;
			System.out.println("k="+p);
			System.out.println("\n\n\n");
			if(Math.abs((double)(qual2-qual1)/(qual1+qual2))<u)
			{
				k=p;
				for(int i=0;i<k;i++)
				{
					new_q.add(Terms.get(i));
				}
				System.out.println("ture ture ture      new/cost="+qual1);
				System.out.println("退出的k="+k);
				System.out.println("\n\n\n");
				return new_q;
			}
			if(qual1<qual2)
			{
				step=step/2;
				p=p-step;
				virtual_all_hits.clear();//clear the virtual_all_hits
				a3_IndexWriter.deleteAll();
				a3_IndexWriter.commit();
				virtual_all_hits.addAll(asset);
				initial_add_from_original_to_sample_by_SET(asset, a3_IndexWriter, db_IndexReader, db_IndexSearcher);
			}
			if(qual1>qual2)
			{
				r=p;
				p=p+step;
			}
			if(step<1)
			{
				k=r;
				for(int i=0;i<k;i++)
				{
					new_q.add(Terms.get(i));
					
				}
				System.out.println("new/cost="+qual1);
				System.out.println("退出的k="+k);
				System.out.println("\n\n\n");
				return new_q;
			}
		}
	}
	public Map<Integer, Integer> Algorithm_1(String path_in_algorithm3,String db_Path,String d_Path,Float lambda) throws IOException
	{
		int i=0;
		BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D://experiment/result.txt"))));
		bufferedWriter.newLine();
		bufferedWriter.write(initial_queries);
		bufferedWriter.newLine();
		Directory db_Directory=FSDirectory.open(new File(db_Path));
		IndexReader db_IndexReader=IndexReader.open(db_Directory);
		IndexSearcher db_IndexSearcher=new IndexSearcher(db_IndexReader);
		int db_Size=db_IndexReader.numDocs();
		Directory d_Directory=FSDirectory.open(new File(d_Path));
		IndexWriter d_IndexWriter=new IndexWriter(d_Directory,indexWriterConfig);
		Set<String> tmp=new HashSet<>();
		tmp.add(initial_queries);
		add_from_original_to_sample(all_hits,d_IndexWriter,db_IndexReader,db_IndexSearcher,tmp);
		IndexReader d_IndexReader=IndexReader.open(d_Directory);
		IndexSearcher d_IndexSearcher=new IndexSearcher(d_IndexReader);
		int d_Size=d_IndexReader.numDocs();
		Directory a3_Directory=FSDirectory.open(new File(path_in_algorithm3));
		IndexWriter a3_IndexWriter=new IndexWriter(a3_Directory,indexWriterConfig2);
		while(d_Size<(lambda*db_Size))
		{
			i++;
			bufferedWriter.newLine();
			ArrayList<String> Terms=Algorithm_2(d_IndexReader,d_IndexSearcher,q);	
			new_q=Algorithm_3(all_hits, a3_Directory,a3_IndexWriter,db_IndexReader,db_IndexSearcher,Terms);	
			Output_1.put(d_Size, new_q.size());
			for (String every_new_q :new_q)
			{
				q.add(every_new_q);
				bufferedWriter.write(every_new_q+"\t");
				System.out.println(every_new_q);
			}
			bufferedWriter.newLine();
			bufferedWriter.flush();
			add_from_original_to_sample(all_hits,d_IndexWriter,db_IndexReader, db_IndexSearcher, new_q);
			d_IndexReader=IndexReader.openIfChanged(d_IndexReader);
			d_IndexSearcher.close();
			d_IndexSearcher=new IndexSearcher(d_IndexReader);
			d_Size=d_IndexReader.numDocs();
		}
		bufferedWriter.close();
		d_IndexWriter.close();
		a3_IndexWriter.close();
		db_IndexSearcher.close();
		d_IndexSearcher.close();
		db_IndexReader.close();
		d_IndexReader.close();
		db_Directory.close();
		d_Directory.close();
		a3_Directory.close();
		return Output_1;
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Algorithm algorithm=new Algorithm();
		algorithm.Algorithm_1(path_for_algorithm3,DB_path,sample_D_path, 0.95f);
	}
}
