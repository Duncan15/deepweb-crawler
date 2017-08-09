package Algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;

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
	final static String wiki2_noredirect="D:/experiment/enwiki-20161220-pages-articles-multistream-sample_2noredirect";
	
	final static String wiki_new="D:/experiment/enwiki-20161220-pages-articles-multistream_noredirect-sample";
	
	final static String DB_path_Wiki2="D:/experiment/enwiki-20161220-pages-articles-multistream-sample3";//none
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
	static ArrayList<String> q=new ArrayList<>();
	//new_q is a set of the new queries
	static ArrayList<String> new_q=new ArrayList<>();

	private String main_Field;
	private float lower_bound;
	private float upper_bound;
	private int algo2_Cost;
	
	private String stored_file;

	//used for algorithm2
	static Set<Integer> all_hits=new HashSet<>();
	Set<Integer> s_in_Algorithm_2=new HashSet<>();
	Map<String, Integer> df_D=new HashMap<>();//all the items from D
	Map<String, HashSet<Integer>> update_df_D=new HashMap<>();//the dynamic update df_D

	//used for algorithm3
	static ArrayList<String> virtual_q=new ArrayList<>();
	static Set<Integer> virtual_all_hits=new HashSet<>();

	static Analyzer analyzer=new StandardAnalyzer(Version.LUCENE_31);
	static IndexWriterConfig  indexWriterConfig=new IndexWriterConfig(Version.LUCENE_31, analyzer);
	static IndexWriterConfig indexWriterConfig2=new IndexWriterConfig(Version.LUCENE_31, analyzer);
	
	public void main_Field_Setter(String text)
	{
		this.main_Field=text;
	}
	public void bound_Setter(float lower,float upper)
	{
		this.upper_bound=upper;
		this.lower_bound=lower;
	}
	public void stored_file_Setter(String tmp)
	{
		this.stored_file=tmp;
	}
	
	public Quality getQual1(int k,ArrayList<String> Terms,Map<String, HashSet<Integer>> search_in_DB,Map<String, Integer> df_in_search_in_DB)
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
			Quality preQual=getQual1(k, Terms, search_in_DB, df_in_search_in_DB);
			float qual1=preQual.quality;
			int qual1_cost=preQual.Coat;
			virtual_q.clear();

			
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
				TermQuery termQuery=new TermQuery(new Term(main_Field,virtual_Term.get(i)));
				ScoreDoc[] hits=db_IndexSearcher.search(termQuery, 1000000).scoreDocs;
				local_cost=local_cost+100+hits.length;
				if(local_cost<left_cost)
				{
					
					
					for(ScoreDoc every_hit:hits)
					{
						inner.add(every_hit.doc);
					}
				
					
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
	
	public Quality getQual2_for_improved1(Set<Integer> all_hits_set,int k,ArrayList<String> Terms,Set<Integer> virtual_all_hits,Map<String, HashSet<Integer>> search_in_DB,Map<String, Integer> df_in_search_in_DB,Directory a3_Directory,IndexWriter a3_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher,int pre_cost,int initial_pool,Map<String, HashSet<Integer>> update_df_D) throws IOException
	{
			k=k/2;
			Quality preQual=getQual1(k, Terms, search_in_DB, df_in_search_in_DB);
			int qual1_cost=preQual.Coat;
			virtual_q.clear();

			for(int i=0;i<k;i++)
			{
				virtual_q.add(Terms.get(i));
			}
			
			//制作update_df_D的本地副本,同时处理该副本，删去本地已更新的前半部分
			Map<String, HashSet<Integer>> tmp_df_D=new HashMap<>();
			tmp_df_D.putAll(update_df_D);
			
			Set<Integer> tmp_set=new HashSet<>();
			for(String each:virtual_q)
			{
				
				tmp_set.addAll(tmp_df_D.get(each));
				tmp_df_D.remove(each);
			}
			
			//处理临时initial_pool,去除前半部分
			initial_pool-=tmp_set.size();
			
			for(String each:tmp_df_D.keySet())
			{
				tmp_df_D.get(each).removeAll(tmp_set);
			}
			
			
			add_from_original_to_sample(virtual_all_hits,a3_IndexWriter, db_IndexReader, db_IndexSearcher, virtual_q);
			//the first half
			
			IndexReader a3_IndexReader=IndexReader.open(a3_Directory);
			IndexSearcher a3_IndexSearcher=new IndexSearcher(a3_IndexReader);
			ArrayList<String> virtual_Term=Algorithm_2_for_improved1(a3_IndexReader, a3_IndexSearcher,initial_pool,tmp_df_D);
			if(virtual_Term.size()==0)
			{
				return null;
			}
			float improve_cost=(float) (0.0001*virtual_all_hits.size()*df_D.size());

			int left_cost=pre_cost-qual1_cost-(int)improve_cost;
			int New=0;
			HashSet<Integer> inner=new HashSet<>();
			
			for(int local_cost=0,i=0;;i++)
			{
				TermQuery termQuery=new TermQuery(new Term(main_Field,virtual_Term.get(i)));
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

	public HashMap<String, HashSet<Integer>> initial_setting(int inital_sample_num,IndexReader initial_IndexReader,IndexSearcher initial_IndexSearcher) throws IOException
	{
		
		HashMap<String, HashSet<Integer>> tmp=new HashMap<>();
		TermEnum d_Enum=initial_IndexReader.terms();
		while (d_Enum.next())
		{
			if(d_Enum.term().field().equals(main_Field))
			{
				
				TermQuery termQuery=new TermQuery(new Term(main_Field, d_Enum.term().text()));
				ScoreDoc[] hits=initial_IndexSearcher.search(termQuery,inital_sample_num ).scoreDocs;
				HashSet<Integer> inner_Set=new HashSet<>();
				for(ScoreDoc hit:hits)
				{
					inner_Set.add(hit.doc);
				
				}
				tmp.put(d_Enum.term().text(),inner_Set);
			
			}
			
		}
		System.out.println("dynamic_DF中键值对数量为"+tmp.size());
		
		return tmp;
	}
	
	public void initial_add_from_original_to_sample_by_SET(Set<Integer> allhit,IndexWriter d_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher) throws IOException, IOException
	{
		for(int i :allhit)
		{
			Document added=db_IndexSearcher.doc(i);
			d_IndexWriter.addDocument(added);
		}
		d_IndexWriter.commit();
	}
	public float add_from_original_to_sample(Set<Integer> allhit,IndexWriter d_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher,ArrayList<String> new_q) throws IOException
	{
		float result=0;
		for (String query:new_q)
		{
			TermQuery termQuery=new TermQuery(new Term(main_Field, query));
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

	public ArrayList<String> Algorithm_2(IndexReader d_IndexReader,IndexSearcher d_IndexSearcher,ArrayList<String> q_be_checked) throws IOException
	{
		
		s_in_Algorithm_2.clear();
		ArrayList<String> Terms=new ArrayList<>();
		df_D.clear();
		update_df_D.clear();
		int d_Size=d_IndexReader.numDocs();
		TermEnum d_Enum=d_IndexReader.terms();
		while (d_Enum.next())
		{
			if(d_Enum.term().field().equals(main_Field))
			{
				if((lower_bound*d_Size)<d_Enum.docFreq()&&d_Enum.docFreq()<=(upper_bound*d_Size))
				{
					df_D.put(d_Enum.term().text(), d_Enum.docFreq());
					TermQuery termQuery=new TermQuery(new Term(main_Field, d_Enum.term().text()));
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
			TermQuery qi_query=new TermQuery(new Term(main_Field, qi_final));
			ScoreDoc[] add_to_s=d_IndexSearcher.search(qi_query, 1000000).scoreDocs;


			Set<Integer> to_be_sub=new HashSet<>();
			for(ScoreDoc every_add_to_s:add_to_s)
			{
				to_be_sub.add(every_add_to_s.doc);
				if (!s_in_Algorithm_2.contains(every_add_to_s.doc))
				{
					s_in_Algorithm_2.add(every_add_to_s.doc);
				}
			}
			System.out.println(update_df_D.get(qi_final).size());
			
			for(String iterator:update_df_D.keySet())
			{
				update_df_D.get(iterator).removeAll(to_be_sub);
			}
			
			System.out.println(max_Fre);
			System.out.println(s_in_Algorithm_2.size());
		}
		
		return Terms;
	}
	public ArrayList<String> Algorithm_2_for_improved1(IndexReader d_IndexReader,IndexSearcher d_IndexSearcher,int initial_pool,Map<String , HashSet<Integer>> update_df_D) throws IOException
	{
		s_in_Algorithm_2.clear();
		df_D.clear();
		algo2_Cost=0;//for compute
		
		//Terms存放返回值
		ArrayList<String> Terms=new ArrayList<>();
		
		//创建algorithm2内部使用的df
		Map<String, HashSet<Integer>> inner_update_df_D=new HashMap<>();
		inner_update_df_D.putAll(update_df_D);
		
		
		int d_Size=d_IndexReader.numDocs();
		TermEnum d_Enum=d_IndexReader.terms();
		while (d_Enum.next())
		{
			algo2_Cost++;//for compute
			if(d_Enum.term().field().equals(main_Field)&&inner_update_df_D.containsKey(d_Enum.term().text()))
			{
				if((lower_bound*d_Size)<d_Enum.docFreq()&&d_Enum.docFreq()<=(upper_bound*d_Size))
				{
					df_D.put(d_Enum.term().text(), d_Enum.docFreq());
				}
			}
		}
		while(s_in_Algorithm_2.size()<initial_pool)
		{
			Set<String> T=new HashSet<>();
			String qi_final=null;
			float max_Fre=0.0f;//the bigst new/cost value which has been known.
			int maximize=0;//the bigest df value which has been known.
			float maximum=0.0f;//tmp


			//select the queries which have the bigest new/cost value
			for(String each_In_Map:df_D.keySet())
			{
				if(inner_update_df_D.get(each_In_Map).size()!=0)
				{
					maximum=(float)inner_update_df_D.get(each_In_Map).size()/(float)df_D.get(each_In_Map);//select the queries which have the bigest new/cost value
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
			algo2_Cost+=df_D.size();//for compute
			
			
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
			}
			else if(T.size()==1)
			{
				for(String qi:T)
				{
					qi_final=qi;
					maximize=df_D.get(qi);
				}
			}
			else if(T.size()==0)
				{
					System.out.println("无词可选，异常退出");;
					break;//stop if can't achieve the 0.999 and can't get another query
				}
			Set<Integer> to_be_sub=new HashSet<>();
			to_be_sub.addAll(inner_update_df_D.get(qi_final));
			//System.out.println(to_be_sub.size());
			
			s_in_Algorithm_2.addAll(to_be_sub);
			inner_update_df_D.remove(qi_final);
			df_D.remove(qi_final);
			
			for(String iterator:inner_update_df_D.keySet())
			{
				inner_update_df_D.get(iterator).removeAll(to_be_sub);
			}
			
			algo2_Cost+=inner_update_df_D.size();//for compute
			
			//System.out.println(s_in_Algorithm_2.size());
			Terms.add(qi_final);
		}
		
		return Terms;
	}

	public ArrayList<String> Algorithm_3_for_improved1(Set<Integer> asset,Directory a3_Directory,IndexWriter a3_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher,ArrayList<String> Terms,int initial_pool,Map<String, HashSet<Integer>> update_df_D) throws IOException
	{
		
		
		new_q.clear();
		//calculate the new and cost for every query
		Map<String, HashSet<Integer>> search_in_DB=new HashMap<>();
		Map<String, Integer> df_in_search_in_DB=new HashMap<>();
		
		for(String eachTerm:Terms)
		{
			TermQuery termQuery=new TermQuery(new Term(main_Field,eachTerm));
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
				Quality qual1_unity=getQual1(k, Terms, search_in_DB, df_in_search_in_DB);
				qual1=qual1_unity.quality;
				int pre_cost=qual1_unity.Coat;
 				Quality qual2_unity=getQual2_for_improved1(asset,k, Terms, virtual_all_hits,search_in_DB, df_in_search_in_DB, a3_Directory, a3_IndexWriter, db_IndexReader, db_IndexSearcher, pre_cost,initial_pool,update_df_D);
 				if(qual2_unity==null)return null;
 				qual2=qual2_unity.quality;
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
			Quality qual1_unity=getQual1(p, Terms, search_in_DB, df_in_search_in_DB);
			qual1=qual1_unity.quality;
			Quality qual2_unity=getQual2_for_improved1(asset,k, Terms, virtual_all_hits,search_in_DB, df_in_search_in_DB, a3_Directory, a3_IndexWriter, db_IndexReader, db_IndexSearcher, qual1_unity.Coat,initial_pool,update_df_D);
			if(qual2_unity==null)return null;
			qual2=qual2_unity.quality;
			
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
	public ArrayList<String> Algorithm_3(Set<Integer> asset,Directory a3_Directory,IndexWriter a3_IndexWriter,IndexReader db_IndexReader,IndexSearcher db_IndexSearcher,ArrayList<String> Terms) throws IOException
	{
		new_q.clear();

		//calculate the new and cost for every query
		Map<String, HashSet<Integer>> search_in_DB=new HashMap<>();
		Map<String, Integer> df_in_search_in_DB=new HashMap<>();
		for(String eachTerm:Terms)
		{
			TermQuery termQuery=new TermQuery(new Term(main_Field,eachTerm));
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
				Quality qual1_unity=getQual1(k, Terms, search_in_DB, df_in_search_in_DB);
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
			Quality qual1_unity=getQual1(p, Terms, search_in_DB, df_in_search_in_DB);
			
			
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
		ArrayList<String> tmp=new ArrayList<>();
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
	
	
	public void Algorithm_1_improved1(String path_in_algorithm3,String db_Path,String d_Path,int initial_num) throws IOException
	{
		BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(new File(stored_file)));
		
		Directory db_Directory=FSDirectory.open(new File(db_Path));
		IndexReader db_IndexReader=IndexReader.open(db_Directory);
		IndexSearcher db_IndexSearcher=new IndexSearcher(db_IndexReader);
		
		int db_size=db_IndexReader.numDocs();
		
		Directory d_Directory=FSDirectory.open(new File(d_Path));
		IndexWriter d_IndexWriter=new IndexWriter(d_Directory,indexWriterConfig);
		
		
		AlgorithmByTable.initial_create_sample(initial_num, (HashSet<Integer>) all_hits, d_IndexWriter, db_IndexSearcher);
		
		//注：initial_pool构建成int的形式更易维护
		int initial_pool=0;
		initial_pool=all_hits.size();
		System.out.println("初始样本个数"+initial_pool);
		
		IndexReader d_IndexReader=IndexReader.open(d_Directory);
		IndexSearcher d_IndexSearcher=new IndexSearcher(d_IndexReader);
		int d_Size=d_IndexReader.numDocs();
		
		Directory a3_Directory=FSDirectory.open(new File(path_in_algorithm3));
		IndexWriter a3_IndexWriter=new IndexWriter(a3_Directory,indexWriterConfig2);
		
		update_df_D=initial_setting(initial_num, d_IndexReader, d_IndexSearcher);
		
		
		double all_num=initial_num;
		double total_Cost=initial_num;//for compute
		while(true)
		{
			
			ArrayList<String> Terms=Algorithm_2_for_improved1(d_IndexReader,d_IndexSearcher,initial_pool,update_df_D);
			total_Cost+=0.001*algo2_Cost;//for compute
			bufferedWriter.write((double)all_hits.size()/db_size+","+total_Cost);//for store message
			//bufferedWriter.write((double)all_hits.size()/db_size+","+1);//for draw HR_OR graph
			bufferedWriter.newLine();
			
			
			if(Terms.size()==0)
			{
				System.out.println("无词可选，异常退出");
				break;
			}
			
			new_q=Algorithm_3_for_improved1(all_hits, a3_Directory, a3_IndexWriter, db_IndexReader, db_IndexSearcher, Terms,initial_pool,update_df_D);
			if(new_q==null)
			{
				break;
			}
			
			Set<Integer> global_to_be_sub=new HashSet<>();
			for (String every_new_q :new_q)
			{
				global_to_be_sub.addAll(update_df_D.get(every_new_q));
				update_df_D.remove(every_new_q);
				
				q.add(every_new_q);
				
				System.out.println(every_new_q);
			}
			
			System.out.println("初始样本将减少"+global_to_be_sub.size());
			for(String each:update_df_D.keySet())
			{
				update_df_D.get(each).remove(global_to_be_sub);
				
			}
			initial_pool-=global_to_be_sub.size();
			System.out.println("全局initial_pool还剩"+initial_pool);
			
			
			for(String each_term:new_q)
			{
				ArrayList<String> tmp_new_q=new ArrayList<>();
				tmp_new_q.add(each_term);
				double tmps=add_from_original_to_sample(all_hits,d_IndexWriter,db_IndexReader, db_IndexSearcher, tmp_new_q);
				all_num+=tmps;
				total_Cost=total_Cost+100+tmps;
				double HR=(double)all_hits.size()/db_size;
				double OR=all_num/all_hits.size();
				System.out.println("HR="+HR);
				System.out.println("OR="+OR);
				bufferedWriter.write(HR+","+total_Cost);//for store message
				//bufferedWriter.write(HR+","+OR);//for draw HR_OR graph
				bufferedWriter.newLine();
				
			}
			
			d_IndexReader=IndexReader.openIfChanged(d_IndexReader);
			d_IndexSearcher.close();
			d_IndexSearcher=new IndexSearcher(d_IndexReader);
			d_Size=d_IndexReader.numDocs();
			
			if(initial_pool==0)
			{
				System.out.println("初始样本实现全覆盖，正常退出");
				break;
			}
			
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
	}
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String stored_file="D:/experiment/reuter_mutipleturn_mutipleterm_HR_OR.csv";
		Algorithm algorithm=new Algorithm();
		//algorithm.Algorithm_1(path_for_algorithm3,DB_path,sample_D_path, 0.95f);
		algorithm.stored_file_Setter(stored_file);
		algorithm.main_Field_Setter("text");
		algorithm.bound_Setter(0.001f, 0.20f);
		//algorithm.bound_Setter(0.02f, 0.15f);
		algorithm.Algorithm_1_improved1(path_for_algorithm3, wiki_new, sample_D_path, 3000);
	}
}
