package Algorithm_improved;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
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

import Algorithm.Algorithm;
import Algorithm.RandomAlgorithm;
import nlpir.NlpirAnalyzer;



class Pair implements Comparable<Pair>
{
	protected String term;
	protected Set<Integer> termSet=new HashSet<>();
	protected int sample_df;
	protected int rank;
	protected int total_df;
	public Pair(String a,Set<Integer> b)
	{
		this.term=a;
		this.termSet.addAll(b);
		this.sample_df=b.size();
		this.rank=0;
	}
	public Pair(String a,int b)
	{
		// TODO Auto-generated constructor stub
		this.term=a;
		this.termSet=null;
		this.sample_df=b;
		this.rank=0;
	}
	//根据df进行排序
	@Override
	public int compareTo(Pair o) //降序排列
	{
		// TODO Auto-generated method stub
		if(this.sample_df>o.sample_df)return -1;
		else if(this.sample_df<o.sample_df)return 1;
		return 0;
	}
}
public class Algorithm_improved {
	private String stored_file="";
	private String db_Path="";
	private String sample_Path="";
	private String main_Field;
	private int inital_sample_num;
	private float upper_bound;
	private float lower_bound;
	private int INFINITY=1000000;
	private boolean chineseFlag=false;
	private Set<Integer> true_Hits;
	
	private int algo2_cost;//just for compute
	private double estimate,estRatio,realRatio,sampleDF;
	
	private Directory db_Directory;
	private IndexReader db_IndexReader;
	//private IndexWriter db_IndexWriter;
	private IndexSearcher db_IndexSearcher;
	
	private Directory sample_Directory;
	private IndexReader sample_IndexReader;
	private IndexWriter sample_IndexWriter;
	private IndexSearcher sample_IndexSearcher;
	
	private Algorithm algorithm;
	private Directory a3_Directory;
	private IndexWriter a3_IndexWriter;
	
	private Analyzer standardAnalyzer=new StandardAnalyzer(Version.LUCENE_31);
	private IndexWriterConfig  indexWriterConfig=new IndexWriterConfig(Version.LUCENE_31, standardAnalyzer);
	Random global_Random=new Random(new Date().getTime());//全局随机数
	
	public void INFINITY_Setter(int tmp)
	{
		this.INFINITY=tmp;
	}
	public void stored_File_Setter(String tmp)
	{
		this.stored_file=tmp;
	}
	public void bound_Setter(float upper,float lower)
	{
		this.upper_bound=upper;
		this.lower_bound=lower;
	}
	public void db_Path_Setter(String tmp)
	{
		this.db_Path=tmp;
	}
	public void sample_Path_Setter(String tmp)
	{
		this.sample_Path=tmp;
	}
	public void main_Field_Setter(String tmp)
	{
		this.main_Field=tmp;
	}
	public void initial_Sample_Number_Setter(int tmp)
	{
		this.inital_sample_num=tmp;
	}
	public void analyzer_Setter(boolean chineseFlag)
	//当chineseFlag为true时，采用中文分析器，否则，采用标准分析器
	{
		if(chineseFlag)
		{
			this.chineseFlag=chineseFlag;
			standardAnalyzer=new NlpirAnalyzer();
			indexWriterConfig=new IndexWriterConfig(Version.LUCENE_31, standardAnalyzer);
		}
	}
	
	public HashMap<String, Set<Integer>> initial_setting(IndexReader initial_IndexReader,IndexSearcher initial_IndexSearcher) throws IOException
	{
		
		HashMap<String, Set<Integer>> tmp=new HashMap<>();
		TermEnum d_Enum=initial_IndexReader.terms();
		while (d_Enum.next())
		{
			if(d_Enum.term().field().equals(main_Field))
			{
				
				TermQuery termQuery=new TermQuery(new Term(main_Field, d_Enum.term().text()));
				ScoreDoc[] hits=initial_IndexSearcher.search(termQuery,inital_sample_num ).scoreDocs;
				Set<Integer> inner_Set=new HashSet<>();
				for(ScoreDoc hit:hits)
				{
					inner_Set.add(hit.doc);
				
				}
				tmp.put(d_Enum.term().text(),inner_Set);
			
			}
			
		}
		System.out.println("dynamic_DF初始键值对总数为"+tmp.size());
		
		return tmp;
	}
	public String algorithm2(Set<Integer> initial_pool,HashMap<String,Set<Integer>>dynamic_DF,IndexReader target_IndexReader,IndexSearcher target_IndexSearcher,boolean random_flag) throws IOException
	{
		Random random=new Random(new Date().getTime());
		HashMap<String, Integer> stable_DF=new HashMap<>();
		int d_Size=target_IndexReader.numDocs();
		
		TermEnum d_Enum=target_IndexReader.terms();
		Set<String> dynamic_keyset=dynamic_DF.keySet();
		
		algo2_cost=0;//compute
		
		while(d_Enum.next())
		{
			algo2_cost++;//for compute
			
			if(d_Enum.term().field().equals(main_Field)&&dynamic_keyset.contains(d_Enum.term().text()))
			{
				if(dynamic_DF.get(d_Enum.term().text()).size()!=0)
				{
					if(lower_bound*d_Size<=d_Enum.docFreq()&&d_Enum.docFreq()<=upper_bound*d_Size)
					{
						stable_DF.put(d_Enum.term().text(),d_Enum.docFreq());
					}	
				}
			}
		}
		algo2_cost+=stable_DF.size();//for compute
		
		System.out.println("stable_DF_Sum="+stable_DF.size());
		
		String qi_final=null;
		float max_n_c=0.0f;//the bigst new/cost value which has been known.
		int max_df=0;//the bigest df value which has been known.
		float tmp=0.0f;//tmp

		ArrayList<String> T=new ArrayList<>();
		//select the queries which have the bigest new/cost value
		for(String each_In_Map:stable_DF.keySet())
		{
			if(dynamic_DF.get(each_In_Map).size()!=0)
			{
				tmp=(float)dynamic_DF.get(each_In_Map).size()/stable_DF.get(each_In_Map);//select the queries which have the bigest new/cost value
				if(tmp>max_n_c)
				{
					max_n_c=tmp;
					T.clear();
					T.add(each_In_Map);
				}
				else if(tmp==max_n_c)
				{
					T.add(each_In_Map);
				}
			}		
		}
		//System.out.println("new/cost="+max_n_c);
		
		
		//ע�����²��־���������ǰ�������	2017.7.12
		//select the query which has the bigest df value
		if(T.size()>1)
		{
			if(random_flag)
			{
				qi_final=T.get(random.nextInt(T.size()));
				max_df=stable_DF.get(qi_final);
			}
			else
			{
				for(String qi:T)
				{
				
					if(max_df<stable_DF.get(qi))
					{
						max_df=stable_DF.get(qi);
						qi_final=qi;
					}
				}
			}
			
		}
		else if(T.size()==1)
		{
			for(String qi:T)
			{
				qi_final=qi;
				max_df=stable_DF.get(qi_final);
				
			}
		}
		else if(T.size()==0)
		{
			System.out.println(stable_DF.size());
			return null;//stop if can't achieve the 0.999 and can't get another query
		}
		Set<Integer> inner_Set=new HashSet<>();
		for(int i:dynamic_DF.get(qi_final))
		{
			inner_Set.add(i);
		}
		dynamic_DF.remove(qi_final);
	
		System.out.println(qi_final);
		System.out.println("当前term可增加初始样本覆盖数为"+inner_Set.size());
		initial_pool.addAll(inner_Set);
		System.out.println("当前初始样本覆盖数为"+initial_pool.size());
		//System.out.println("new/cost="+max_n_c);
		//System.out.println("df="+max_df);
		//System.out.println("sample size="+d_Size);
		//System.out.println("\n\n");
		for(String iterator:dynamic_DF.keySet())
		{
			dynamic_DF.get(iterator).removeAll(inner_Set);
		}
		
		algo2_cost+=dynamic_DF.size();
		
		return qi_final;
	}
	public ArrayList<String> algorithm2_for_one_turn(Set<Integer> initial_pool,HashMap<String,Set<Integer>>dynamic_DF,IndexReader target_IndexReader,IndexSearcher target_IndexSearcher,boolean random_flag) throws IOException
	{
		Random random=new Random(new Date().getTime());
		
		ArrayList<String> all_terms=new ArrayList<>();
		HashMap<String, Integer> stable_DF=new HashMap<>();
		int d_Size=target_IndexReader.numDocs();
		
		algo2_cost=0;//for compute
		
		for(String each:dynamic_DF.keySet())
		{
			if(lower_bound*d_Size<=dynamic_DF.get(each).size()&&dynamic_DF.get(each).size()<=upper_bound*d_Size)
			{
				stable_DF.put(each, dynamic_DF.get(each).size());
			}
		}
		
		algo2_cost+=dynamic_DF.size();//for compute
		
		//此处为测试代码
//		Set<Integer> test=new HashSet<>();
//		for(String each:stable_DF.keySet())
//		{
//			test.addAll(dynamic_DF.get(each));
//		}
//		System.out.println("当前范围的所有term可覆盖初始样本的个数为"+test.size());

		
		while(initial_pool.size()!=inital_sample_num)
		{
			String qi_final=null;
			float max_n_c=0.0f;//the bigst new/cost value which has been known.
			int max_df=0;//the bigest df value which has been known.
			float tmp=0.0f;//tmp
			ArrayList<String> T=new ArrayList<>();
			
			algo2_cost+=stable_DF.size();//for compute
			
			for(String each_In_Map:stable_DF.keySet())
			{
				if(dynamic_DF.get(each_In_Map).size()!=0)
				{
					tmp=(float)dynamic_DF.get(each_In_Map).size()/stable_DF.get(each_In_Map);//select the queries which have the bigest new/cost value
					if(tmp>max_n_c)
					{
						max_n_c=tmp;
						T.clear();
						T.add(each_In_Map);
					}
					else if(tmp==max_n_c)
					{
						T.add(each_In_Map);
					}
				}		
			}
			//System.out.println("new/cost="+max_n_c);
			
			
			//select the query which has the bigest df value
			if(T.size()>1)
			{
				if(random_flag)
				{
					qi_final=T.get(random.nextInt(T.size()));
					max_df=stable_DF.get(qi_final);
				}
				else
				{
					for(String qi:T)
					{
					
						if(max_df<stable_DF.get(qi))
						{
							max_df=stable_DF.get(qi);
							qi_final=qi;
						}
					}
				}
				
			}
			else if(T.size()==1)
			{
				for(String qi:T)
				{
					qi_final=qi;
					max_df=stable_DF.get(qi_final);
					
				}
			}
			else if(T.size()==0)
			{
				System.out.println("剩余键值对数量为"+stable_DF.size());
				
				System.out.println("异常退出");
				break;//stop if can't achieve the 0.999 and can't get another query
			}
			all_terms.add(qi_final);
			System.out.println(qi_final);
			System.out.println("当前term可增加初始样本覆盖数为"+dynamic_DF.get(qi_final).size());
			Set<Integer> inner_Set=new HashSet<>();
			inner_Set.addAll(dynamic_DF.get(qi_final));
			
			stable_DF.remove(qi_final);
			dynamic_DF.remove(qi_final);
			
//			TermQuery termQuery=new TermQuery(new Term(main_Field, qi_final));
//			ScoreDoc[] hits=target_IndexSearcher.search(termQuery,inital_sample_num ).scoreDocs;
//			
//			for(ScoreDoc hit:hits)
//			{
//				inner_Set.add(hit.doc);
//			}
//			
			initial_pool.addAll(inner_Set);
			System.out.println("初始样本当前总覆盖数为"+initial_pool.size());
			System.out.println("new/cost="+max_n_c);
			System.out.println("df="+max_df);
			System.out.println("\n\n");
			for(String iterator:dynamic_DF.keySet())
			{
				dynamic_DF.get(iterator).removeAll(inner_Set);
			}
			
			algo2_cost+=dynamic_DF.size();
		}
		return all_terms;
	}
	
	public int add_sample_by_term(String term,Set<Integer> true_Hits,IndexWriter sample_IndexWriter,IndexSearcher source_IndexSearcher) throws IOException
	{
		TermQuery qi_query=new TermQuery(new Term(main_Field, term));
		ScoreDoc[] add_to_sample=source_IndexSearcher.search(qi_query, INFINITY).scoreDocs;
		for(ScoreDoc hit:add_to_sample)
		{
			if(!true_Hits.contains(hit.doc))
			{
				Document document=source_IndexSearcher.doc(hit.doc);
				try
				{
					sample_IndexWriter.addDocument(document);
				}
				catch(Error e)
				{
					e.printStackTrace();
					System.out.println(document.get("body"));
				}
				true_Hits.add(hit.doc);
			}
		}
		sample_IndexWriter.commit();
		return add_to_sample.length;
	}
	public void initial_create_sample(int num,Set<Integer> true_Hits,IndexWriter target_IndexWriter,IndexSearcher source_IndexSearcher) throws CorruptIndexException, IOException
	{
		int counter=0;
		int source_num=source_IndexSearcher.maxDoc();
		Random rand=new Random(new Date().getTime());
		while(counter<num)
		{
			int tmp=rand.nextInt(source_num);
			if(!true_Hits.contains(tmp))
			{
				true_Hits.add(tmp);
				counter++;
			}
			
		}
		for(int i :true_Hits)
		{
			Document a=db_IndexSearcher.doc(i);
			sample_IndexWriter.addDocument(a);
		}
		sample_IndexWriter.commit();
		
	}
	public ArrayList<String> Round_Turn_Algorithm(boolean multi_turn,boolean random_flag) throws IOException
	{
		//临时代码,用于存储数据
		BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(new File(stored_file)));

		db_Directory=FSDirectory.open(new File(db_Path));
		db_IndexReader=IndexReader.open(db_Directory);
		db_IndexSearcher=new IndexSearcher(db_IndexReader);
		double db_size=db_IndexReader.numDocs();
				
		sample_Directory=FSDirectory.open(new File(sample_Path));
		sample_IndexWriter=new IndexWriter(sample_Directory, indexWriterConfig);
			
		Set<Integer> true_Hits=new HashSet<>();//the all hit set
		initial_create_sample(inital_sample_num, true_Hits, sample_IndexWriter, db_IndexSearcher);
		sample_IndexReader=IndexReader.open(sample_Directory);
		sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		
		
		
		ArrayList<String> Term;
		HashMap<String, Set<Integer>> dynamic_DF=initial_setting(sample_IndexReader, sample_IndexSearcher);
		System.out.println("initial sample size is "+inital_sample_num);
		Set<Integer> s_in_a2=new HashSet<>();
		
		int total_hits=inital_sample_num;
		double total_Cost=inital_sample_num;
		
		if(multi_turn)
		{
			Term=new ArrayList<>();
			
			while(true)
			{
				String term=algorithm2(s_in_a2,dynamic_DF, sample_IndexReader, sample_IndexSearcher,random_flag);
				if(term==null)
				{
					System.out.println("异常退出");
					break;
				}
				Term.add(term);
			
				int tmps=add_sample_by_term(term, true_Hits, sample_IndexWriter, db_IndexSearcher);
				System.out.println("df="+tmps);
				total_hits+=tmps;
				
				try
				{
					sample_IndexReader=IndexReader.openIfChanged(sample_IndexReader);
					sample_IndexSearcher.close();
					sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				double HR=(double)true_Hits.size()/db_size;
				double OR=(double)total_hits/true_Hits.size();
				
				total_Cost=(int) (total_Cost+100+tmps+0.001*algo2_cost);//for compute
				
				System.out.println("HR="+HR);
				System.out.println("OR="+OR);
				System.out.println("\n\n");
				
				//bufferedWriter.write(HR+","+total_Cost);
				bufferedWriter.write(HR+","+OR);//for draw HR_OR graph
				bufferedWriter.newLine();
				if(s_in_a2.size()==inital_sample_num)
				{
					System.out.println("正常退出");
					break;
				}
			}
			
		}
		else
		{
			Term=algorithm2_for_one_turn(s_in_a2, dynamic_DF, sample_IndexReader, sample_IndexSearcher, random_flag);
		}
		
		
		
		System.out.println(inital_sample_num+"进行全覆盖需要query个数为"+Term.size());
		
//		for(String each:Term)
//		{
//			System.out.println(each);
//		}
		if(!multi_turn)
		{
			//count for the initial compute
			total_Cost+=0.001*algo2_cost;
			
			//bufferedWriter.write((float)true_Hits.size()/db_IndexReader.numDocs()+","+total_Cost);
			bufferedWriter.write((double)true_Hits.size()/db_size+","+1);//for draw HR_OR graph
			bufferedWriter.newLine();
			
			for(String each:Term)
			{
				int tmps=0;
				tmps=add_sample_by_term(each, true_Hits, sample_IndexWriter, db_IndexSearcher);
				total_hits+=tmps;
				
				total_Cost=total_Cost+tmps+100;
				
				double HR=(double)true_Hits.size()/db_size;
				double OR=(float)total_hits/true_Hits.size();
				System.out.println("HR="+HR);
				System.out.println("OR="+OR+"\n\n");
				
				//临时代码
				//bufferedWriter.write(HR+","+total_Cost);
				bufferedWriter.write(HR+","+OR);//for draw HR_OR graph
				bufferedWriter.newLine();
			}
			
			//System.out.println(true_Hits.size());
			sample_IndexReader=IndexReader.openIfChanged(sample_IndexReader);
			sample_IndexSearcher.close();
			sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
			
		}
		
		
		bufferedWriter.close();//临时代码
		
		sample_IndexWriter.close();
		sample_IndexSearcher.close();
		sample_IndexReader.close();
		sample_Directory.close();
		
		db_IndexSearcher.close();
		db_IndexReader.close();
		db_Directory.close();
		
		return Term;
		
	}

	public void verified_algorithm() throws IOException
	{
		db_Directory=FSDirectory.open(new File(db_Path));
		db_IndexReader=IndexReader.open(db_Directory);
		db_IndexSearcher=new IndexSearcher(db_IndexReader);
				
		sample_Directory=FSDirectory.open(new File(sample_Path));
		sample_IndexWriter=new IndexWriter(sample_Directory, indexWriterConfig);
			
		HashSet<Integer> true_Hits=new HashSet<>();
		initial_create_sample(inital_sample_num, true_Hits, sample_IndexWriter, db_IndexSearcher);
		sample_IndexReader=IndexReader.open(sample_Directory);
		sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		
		int d_Size=sample_IndexReader.numDocs();
		System.out.println("sample��СΪ"+d_Size);
		
		TermEnum d_Enum=sample_IndexReader.terms();
		HashSet<Integer> tmps=new HashSet<>();
		while(d_Enum.next())
		{
			if(d_Enum.term().field().equals(main_Field))
			{
				if(lower_bound*d_Size<=d_Enum.docFreq()&&d_Enum.docFreq()<=upper_bound*d_Size)
				{
					TermQuery qi_query=new TermQuery(new Term(main_Field, d_Enum.term().text()));
					ScoreDoc[] add_to_sample=db_IndexSearcher.search(qi_query, 1000000).scoreDocs;
					for(ScoreDoc scores:add_to_sample)
					{
						tmps.add(scores.doc);
					}
				}	
			}
		}
		System.out.println("�ڸ�����Χ�ڵ����д�ȫ���������ս������ĵ�����Ϊ"+tmps.size());
		tmps.removeAll(true_Hits);
		for(int each:tmps)
		{
			Document document=db_IndexSearcher.doc(each);
			sample_IndexWriter.addDocument(document);
		}
		sample_IndexWriter.commit();
		sample_IndexReader=IndexReader.openIfChanged(sample_IndexReader);
		sample_IndexSearcher.close();
		sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		System.out.println(sample_IndexReader.numDocs());
		System.out.println(db_IndexReader.numDocs());
		System.out.println("�ܱ���Ϊ"+(float)sample_IndexReader.numDocs()/db_IndexReader.numDocs());
		
	}
	
	
	public String getting_initial_Term(int lower_limit,int upper_limit,IndexSearcher target_IndexSearcher) throws IOException
	{
		RandomAlgorithm randomAlgorithm=new RandomAlgorithm();
		String initial_Term=null;
		while(true)
		{
			initial_Term=randomAlgorithm.randomAccess();
			TermQuery qi_query=new TermQuery(new Term(main_Field, initial_Term));
			ScoreDoc[] add_to_sample=target_IndexSearcher.search(qi_query, INFINITY).scoreDocs;
			if(add_to_sample.length>lower_limit&&add_to_sample.length<upper_limit)break;//确保取词足够
		}
		return initial_Term;
	}
	
	//point_num用来设置拟合的点数
	public double[] curve_fitting(int point_num,HashMap<Integer, TreeSet<String>> sample_Rank_TermSet_Map,IndexSearcher db_IndexSearcher) throws IOException
	{
		ArrayList<WeightedObservedPoint> points=new ArrayList<>();
		int stepLength=0;
		if(point_num>=sample_Rank_TermSet_Map.size())//异常情况
		{
			stepLength=1;
			point_num=sample_Rank_TermSet_Map.size();
		}
		else
		{
			stepLength=sample_Rank_TermSet_Map.size()/(point_num+1);//正常情况
		}
		
		double step=1;
		for(int i=0;i<point_num;i++)
		{
			int step_size=sample_Rank_TermSet_Map.get((int)step).size();
			String step_Term=(String) sample_Rank_TermSet_Map.get((int)step).toArray()[global_Random.nextInt(step_size)];
			
			TermQuery query=new TermQuery(new Term(main_Field, step_Term));
			ScoreDoc[] termHits=db_IndexSearcher.search(query, INFINITY).scoreDocs;
			//1.0为权重
			points.add(new WeightedObservedPoint(1.0, step,Math.log(termHits.length)));
			step+=stepLength;
		}
		
		MyFuncFitter fitter = new MyFuncFitter();
		System.out.println("进入拟合");
		final double coeffs[] = fitter.fit(points);
		//System.out.println("退出拟合");
		return coeffs;
	}
	
	public ArrayList<Pair> dealing_for_selecting_algorithm(boolean multipleFlag,IndexReader target_IndexReader,IndexSearcher target_IndexSearcher,IndexSearcher db_IndexSearcher) throws IOException
	{
		int d_Size=target_IndexReader.numDocs();
		TermEnum target_TermEnum=target_IndexReader.terms();
		ArrayList<Pair> term_df=new ArrayList<>();
		while(target_TermEnum.next())
		{
			if(target_TermEnum.term().field().equals(main_Field))
			{
				if(lower_bound*d_Size<=target_TermEnum.docFreq()&&target_TermEnum.docFreq()<=upper_bound*d_Size)
				{
					
					//multipleFlag为true时只存储df，为false时存储在sample中击中的document的docID
					if(!multipleFlag)
					{
						term_df.add(new Pair(target_TermEnum.term().text(), target_TermEnum.docFreq()));
					}
					else
					{
						TermQuery query=new TermQuery(new Term(main_Field, target_TermEnum.term().text()));
						ScoreDoc[] termHits=target_IndexSearcher.search(query, INFINITY).scoreDocs;
						Set<Integer> tmp_Set=new HashSet<>();
						for(ScoreDoc each:termHits)
						{
							tmp_Set.add(each.doc);
						}
						term_df.add(new Pair(target_TermEnum.term().text(),tmp_Set));
					}
				}
			}
		}
		//排序并存储对应序号对应的term
		Collections.sort(term_df);
		HashMap<Integer, TreeSet<String>> rank_TermSet_Map=new HashMap<>(); 
		int ranks=0,pre=-1;
		TreeSet<String> tmp_Set=null;
		for(Pair each:term_df)
		{
			if(each.sample_df!=pre)
			{
				if(tmp_Set!=null)
				{
					rank_TermSet_Map.put(ranks, tmp_Set);
				}
				
				tmp_Set=new TreeSet<>();
				ranks++;
				pre=each.sample_df;
			}
			each.rank=ranks;
			tmp_Set.add(each.term);
		}
		rank_TermSet_Map.put(ranks, tmp_Set);
		
		double coeffs[]=curve_fitting(1000,rank_TermSet_Map, db_IndexSearcher);
		
		//根据拟合出来的曲线计算所有term的total_df
		MyFunc myFunc=new MyFunc();
		for(Pair each:term_df)
		{
			double tmps= myFunc.value(each.rank,coeffs);
			each.total_df=(int) Math.pow(Math.E, tmps);
		}
		return term_df;
	}
	
	public ArrayList<String> selecting_algorithm(IndexReader target_IndexReader,IndexSearcher target_IndexSearcher,ArrayList<Pair> info,ArrayList<String> set_be_checked,boolean select_Flag,Algorithm algorithm) throws IOException//select_Flag为true时选一个词，select_Flag为false时选多个词
	{
		ArrayList<String> res=new ArrayList<>();
		double biggest_New_Cost_Rate=0,New=0,Cost=0;
		int d_Size=target_IndexReader.numDocs();
		
		//select_Flag==true时，一轮只选一个term
		ArrayList<Pair> T=new ArrayList<>();
		for(Pair each:info)
		{
			//bufferedWriter.write(each.rank+","+each.total_df);
			//bufferedWriter.newLine();
				
			if(set_be_checked.contains(each.term))continue;//已选中的词跳过
			float tmp=(float)(each.total_df-each.sample_df)/each.total_df;
			
			if(tmp>biggest_New_Cost_Rate)
			{
				T.clear();
				biggest_New_Cost_Rate=tmp;
				New=each.total_df-each.sample_df;
				Cost=each.total_df;
				T.add(each);
			}
			else if(tmp==biggest_New_Cost_Rate)
			{
				T.add(each);
			}
		}
		Pair qi_info=T.get(global_Random.nextInt(T.size()));
		String qi_final=qi_info.term;
		res.add(qi_final);
		System.out.println(qi_final);
		System.out.println("new="+New+"\tcost="+Cost);
		System.out.println("New/Cost="+biggest_New_Cost_Rate);
		
		estimate=qi_info.total_df;
		sampleDF=qi_info.sample_df;
		estRatio=(double)(qi_info.total_df-qi_info.sample_df)/qi_info.total_df;
		
		if(select_Flag)//多选模式
		{
			int s_in_selecting_algo=qi_info.sample_df;
			Set<Integer> to_be_sub=new HashSet<>();
			to_be_sub.addAll(qi_info.termSet);
			for(Pair each:info)
			{
				each.termSet.removeAll(to_be_sub);
			}//从new中去除已选词的df
			to_be_sub.clear();
			while(s_in_selecting_algo<0.99*d_Size)
			{
				biggest_New_Cost_Rate=0;
				New=0;
				Cost=0;
				
				for(Pair each:info)
				{
					if(set_be_checked.contains(each.term))continue;//已选中的词跳过
					float tmp=(float)each.termSet.size()/each.sample_df;
					if(tmp>biggest_New_Cost_Rate)
					{
						T.clear();
						biggest_New_Cost_Rate=tmp;
						New=each.termSet.size();
						Cost=each.sample_df;
						T.add(each);
					}
					else if(tmp==biggest_New_Cost_Rate)
					{
						T.add(each);
					}
				}
				
				if(T.isEmpty())
				{
					System.out.println("异常退出");
					break;
				}
				qi_info=T.get(global_Random.nextInt(T.size()));
				s_in_selecting_algo+=qi_info.termSet.size();//important part
				qi_final=qi_info.term;
				res.add(qi_final);
				System.out.println(qi_final);
				System.out.println("new="+New+"\tcost="+Cost);
				System.out.println("New/Cost="+biggest_New_Cost_Rate);
				//以上为选词部分
				
				
				to_be_sub.addAll(qi_info.termSet);
				for(Pair each:info)
				{
					each.termSet.removeAll(to_be_sub);
				}//从new中去除已选词的df
				to_be_sub.clear();
				//以上为处理部分
			}
			
			res=algorithm.Algorithm_3(true_Hits, a3_Directory, a3_IndexWriter, db_IndexReader, db_IndexSearcher, res,set_be_checked);
		}
		
		for(String each:res)
		{
			set_be_checked.add(each);
		}
		return res;
	}
	public ArrayList<String> selecting_algorithm_only_new(ArrayList<Pair> info,ArrayList<String> set_be_checked,boolean select_Flag) throws IOException
	{
		ArrayList<String> res=new ArrayList<>();
		int New=0,Cost=0;
		ArrayList<String> T=new ArrayList<>();
		//select_Flag==true时，一轮只选一个term
		if(select_Flag)
		{
			for(Pair each:info)
			{
				if(set_be_checked.contains(each.term))continue;//已选中的词跳过
				int tmp=each.total_df-each.sample_df;
				//if(tmp>biggest_New_Cost_Rate)
				if(tmp>New)
				{
					T.clear();
					New=tmp;
					Cost=each.total_df;
					T.add(each.term);
				}
				else if(tmp==New)
				{
					T.add(each.term);
				}
			}
			String qi_final=T.get(global_Random.nextInt(T.size()));//New相同时，随机获取一个
			res.add(qi_final);
			set_be_checked.add(qi_final);
			System.out.println(qi_final);
			System.out.println("new="+New+"\tcost="+Cost);
			//System.out.println("New/Cost="+biggest_New_Cost_Rate);
			System.out.println((double)New/Cost);
		}
		else
		{
			
		}
		return res;
	}
	public void simple_crawling_algorithm(float lower_Limit,boolean multipleFlag,boolean newFlag) throws IOException
	{
		true_Hits=new HashSet<>();//the all hit set
		ArrayList<String> Q=new ArrayList<>();//all the term this algorithm has selected
		db_Directory=FSDirectory.open(new File(db_Path));
		db_IndexReader=IndexReader.open(db_Directory);
		db_IndexSearcher=new IndexSearcher(db_IndexReader);
		int db_Size=db_IndexReader.numDocs();
		
		sample_Directory=FSDirectory.open(new File(sample_Path));
		sample_IndexWriter=new IndexWriter(sample_Directory, indexWriterConfig);
		
		if(multipleFlag)
		{
			algorithm=new Algorithm();//Algorithm initiate and set
			algorithm.bound_Setter(lower_bound, upper_bound);
			algorithm.main_Field_Setter(main_Field);
			algorithm.analyzer_Setter(chineseFlag);//当采用中文数据源时应进行此设置
			a3_Directory=FSDirectory.open(new File(Algorithm.path_for_algorithm33));
			a3_IndexWriter=new IndexWriter(a3_Directory, Algorithm.indexWriterConfig2);
		}
		
		
		double total_hits=0;
		BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(new File(stored_file)));//存储HR_OR
		
		String initial_Term=getting_initial_Term(10000, 30000,db_IndexSearcher);
		
		
		System.out.println("initial term is "+initial_Term);
		total_hits+=add_sample_by_term(initial_Term, true_Hits, sample_IndexWriter, db_IndexSearcher);
		sample_IndexReader=IndexReader.open(sample_Directory);
		sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		//以上为算法初始化
		
		int d_Size=sample_IndexReader.numDocs();
		System.out.println("initial sample size is "+d_Size);
		
		double HR=(double)d_Size/db_Size,OR=total_hits/d_Size;//for store info
		bufferedWriter.write(HR+","+OR+","+initial_Term+","+total_hits+",0,1,1");//存储列
		bufferedWriter.newLine();
	
		int i=0;
		while(d_Size<lower_Limit*db_Size)
		{
			//select_flag可以控制deal_pattern
			ArrayList<Pair> term_info=dealing_for_selecting_algorithm(multipleFlag,sample_IndexReader, sample_IndexSearcher, db_IndexSearcher);
			ArrayList<String> res=null;
			if(!newFlag)
			{
				//mutipleFlag用于决定是简单单选还是多选
				res=selecting_algorithm(sample_IndexReader,sample_IndexSearcher,term_info, Q,multipleFlag,algorithm);
			
			}
			else
			{
				res=selecting_algorithm_only_new(term_info, Q, true);//暂时未处理
			}
			
			for(String each:res)
			{
				double df=add_sample_by_term(each, true_Hits, sample_IndexWriter, db_IndexSearcher);
				total_hits+=df;
				try
				{
					sample_IndexReader=IndexReader.openIfChanged(sample_IndexReader);
					sample_IndexSearcher.close();
					sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					sample_IndexReader=IndexReader.open(sample_Directory);
					sample_IndexSearcher.close();
					sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
				}
				d_Size=true_Hits.size();
				System.out.println("当前sample大小为"+d_Size);
				System.out.println(++i+"\n");
			
				HR=(double)d_Size/db_Size;
				OR=total_hits/d_Size;//for store info
				
				realRatio=(df-sampleDF)/df;
			
				bufferedWriter.write(HR+","+OR+","+each+","+df+","+estimate+","+estRatio+","+realRatio);
				bufferedWriter.newLine();
			}

		}
		
		bufferedWriter.close();
		db_IndexSearcher.close();
		db_IndexReader.close();
		db_Directory.close();
		
		sample_IndexSearcher.close();
		sample_IndexReader.close();
		sample_IndexWriter.close();
		sample_Directory.close();
		
		if(multipleFlag)
		{
			a3_IndexWriter.close();
			a3_Directory.close();
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//String db_wiki="F:/experiment/enwiki-20161220-pages-articles-multistream-index-sample";
		
		//String db_wiki2="F:/experiment/enwiki-20161220-pages-articles-multistream-sample2";
		//String wiki2_noredirect="F:/experiment/enwiki-20161220-pages-articles-multistream-sample_2noredirect";
		
		String wiki_new="F:/experiment/enwiki-20161220-pages-articles-multistream_noredirect-sample";
		
		//String db_reuters="F:/experiment/Algorithm_1_DB";
		
		//String citeSeer_v2="F:/experiment/index_network_dblp_citeseer_fulltext_url_v2";
		String sam="F:/experiment/sample5";
		String stored_file_path="F:/experiment/chineseinfo3.csv";
		Algorithm_improved al=new Algorithm_improved();
		al.stored_File_Setter(stored_file_path);
		al.sample_Path_Setter(sam);
		//al.bound_Setter(0.20f, 0.001f);
		al.bound_Setter(0.15f, 0.02f);
		//al.db_Path_Setter(wiki_new);
		//al.db_Path_Setter(db_reuters);
		//al.db_Path_Setter(citeSeer_v2);
		al.db_Path_Setter("F:/experiment/chinese");
		al.main_Field_Setter("body");
		al.analyzer_Setter(true);
		
		//al.initial_Sample_Number_Setter(3000);
		//al.Round_Turn_Algorithm(false,true);
		
		
		//boolean select_Flag,boolean dealing_pattern,boolean selecting_algorithm_flag
		al.simple_crawling_algorithm(0.9f,false,false);//new_cost selecting algorithm
		//al.simple_crawling_algorithm(0.9f, true,false);//new selecting algorithm
		//al.verified_algorithm();
		
	}

}
