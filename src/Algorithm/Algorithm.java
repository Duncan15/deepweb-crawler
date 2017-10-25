package Algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import com.cwc.lucene.analyzer.chinese.JieBaChineseAnalyzer;

import RandomTermGetter.RandomAlgorithm;
public class Algorithm {
	public final static String sample_D_path1="F:/experiment/tmp/sample1";
	public final static String sample_D_path2="F:/experiment/tmp/sample2";
	public final static String sample_D_path3="F:/experiment/tmp/sample3";
	public final static String sample_D_path4="F:/experiment/tmp/sample4";
	public final static String reuters="F:/experiment/Index6.6.1/reuters_new";
	public final static String chinese_DB="F:/experiment/Index6.6.1/Index034jieba";
	public final static String citeSeer_v2="F:/experiment/Index6.6.1/citeSeer_new";
	public final static String wiki_new="F:/experiment/Index6.6.1/wiki_new";
	
	private Analyzer analyzer=new StandardAnalyzer();
	private float lowerBound;
	private float upperBound;
	private String mainField;
	private String storedFile;
	private String termStoredFile;
	private int recordK=2;
	private BufferedWriter hrorWriter;
	private BufferedWriter termWriter;
	
	static String initialQuery;
	static int IFINITY=100000000;
	//q is a set of the queries
	public ArrayList<String> q=new ArrayList<>();
	//new_q is a set of the new queries
	public ArrayList<String> new_q=new ArrayList<>();
	public IndexWriterConfig  indexWriterConfig=new IndexWriterConfig(analyzer);
	public IndexWriterConfig indexWriterConfig2=new IndexWriterConfig(analyzer);
	public Directory db_Directory;
	public IndexReader db_IndexReader;
	public IndexSearcher db_IndexSearcher;
	public Directory d_Directory;
	public IndexWriter d_IndexWriter;
	public IndexReader d_IndexReader;
	public IndexSearcher d_IndexSearcher;
	public int dbSize;
	public int allDownloadNumber;

	//used for algorithm2
	protected Set<Integer> allHits=new HashSet<>();
	protected Set<Integer> backup=new HashSet<>();
	protected Map<String, Integer> df_D=new HashMap<>();//all the items from D
	protected Map<String, HashSet<Integer>> update_df_D=new HashMap<>();//the dynamic update df_D
	
	public void analyzerSetter(boolean chineseFlag) throws IOException
	{
		if(chineseFlag)
		{
			//analyzer= new NLPIRTokenizerAnalyzer("F:/Java-external-library/NPLIR20140928", 1, "", "", false);
			analyzer=new JieBaChineseAnalyzer(false);
			indexWriterConfig=new IndexWriterConfig(analyzer);
			indexWriterConfig2=new IndexWriterConfig(analyzer);
			
		}
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		indexWriterConfig2.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	}
	public void mainFieldSetter(String text)
	{
		this.mainField=text;
	}
	public void boundSetter(float lower,float upper)
	{
		this.upperBound=upper;
		this.lowerBound=lower;
	}
	public void storedFileSetter(String tmp)
	{
		this.storedFile=tmp;
	}
	public void termStoredFileSetter(String tmp)
	{
		this.termStoredFile=tmp;
	}
	public boolean qualityProcessing(int k,int preK,ArrayList<String> terms,Map<String, HashSet<Integer>> searchInDB,Map<String, Integer> dfInSearchInDB) throws IOException
	{
		Set<Integer> midState=new HashSet<>();
		midState.addAll(allHits);
		Quality pre_unity=getQual1(preK, terms, searchInDB, dfInSearchInDB,backup);
		ArrayList<String> virtual_Term=Algorithm2(d_IndexSearcher,q);
		for(int i=preK;i<k;i++)
		{
			queryInDB(terms.get(i), searchInDB, dfInSearchInDB,true);//预处理
		}
		Quality qual1_unity=getQual1(k, terms, searchInDB, dfInSearchInDB,backup);
		float improve_cost=(float) (0.0001*allHits.size()*update_df_D.size());
		int left_cost=qual1_unity.Cost-pre_unity.Cost-(int)improve_cost;
		
		int New=0;
		HashSet<Integer> inner=new HashSet<>();
		for(int local_cost=0,i=0;;i++)
		{
			String term=virtual_Term.get(i);
			queryInDB(term, searchInDB, dfInSearchInDB,false);
			local_cost=local_cost+100+dfInSearchInDB.get(term);
			if(local_cost<left_cost)
			{
				inner.addAll(searchInDB.get(term));
			}
			else
				break;
		}
		inner.removeAll(midState);
		New=inner.size();
		New=New+pre_unity.New;
		
		float qual1=qual1_unity.quality;
		float qual2=(float)New/qual1_unity.Cost;
		if(qual2>=qual1)return true;
		return false;
	}
	public Quality getQual1(int k,ArrayList<String> Terms,Map<String, HashSet<Integer>> search_in_DB,Map<String, Integer> df_in_search_in_DB,Set<Integer> all_hits)
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
		tmp_All.removeAll(all_hits);
		New=tmp_All.size();
		return new Quality(New, Cost);
	}
	public HashMap<String, HashSet<Integer>> initialSetting(int inital_sample_num,IndexReader initial_IndexReader,IndexSearcher initial_IndexSearcher) throws IOException
	{
		
		HashMap<String, HashSet<Integer>> tmp=new HashMap<>();
		Fields fields=MultiFields.getFields(initial_IndexReader);
		Terms terms=fields.terms(mainField);
		TermsEnum d_Enum=terms.iterator();
		while (d_Enum.next() != null)
		{
			String termString=d_Enum.term().utf8ToString();
			TermQuery termQuery=new TermQuery(new Term(mainField, termString));
			ScoreDoc[] hits=initial_IndexSearcher.search(termQuery,inital_sample_num ).scoreDocs;
			HashSet<Integer> inner_Set=new HashSet<>();
			for(ScoreDoc hit:hits)
			{
				inner_Set.add(hit.doc);
			
			}
			tmp.put(termString,inner_Set);
		}
		System.out.println("dynamic_DF中键值对数量为"+tmp.size());
		return tmp;
	}
	public void initialAddFromOriginalToSampleBySet(Set<Integer> allhit,IndexWriter d_IndexWriter) throws IOException, IOException
	{
		for(int i :allhit)
		{
			Document added=db_IndexSearcher.doc(i);
			d_IndexWriter.addDocument(added);
		}
		d_IndexWriter.commit();
	}
	public float addFromOriginalToSample(ArrayList<String> new_q) throws IOException
	{
		float result=0;
		for (String query:new_q)
		{
			TermQuery termQuery=new TermQuery(new Term(mainField, query));
			ScoreDoc[] hits=db_IndexSearcher.search(termQuery, 1000000).scoreDocs;
			result+=hits.length;
			for(ScoreDoc hit:hits)
			{
				if(!allHits.contains(hit.doc))
				{
					Document document=db_IndexSearcher.doc(hit.doc);
					d_IndexWriter.addDocument(document);
					allHits.add(hit.doc);
				}
			}
			d_IndexWriter.commit();
		}
		return result;
	}
	public ArrayList<String> Algorithm2(IndexSearcher indexSearcher,ArrayList<String> q_be_checked) throws IOException
	{
		IndexReader indexReader=indexSearcher.getIndexReader();
		ArrayList<String> Terms=new ArrayList<>();
		Set<Integer> sInAlgorithm2=new HashSet<>();
		df_D.clear();
		update_df_D.clear();
		int d_Size=indexReader.numDocs();
		Fields fields=MultiFields.getFields(indexReader);
		Terms terms=fields.terms(mainField);
		TermsEnum d_Enum=terms.iterator();
		while (d_Enum.next()!=null)
		{
			String termString=d_Enum.term().utf8ToString();
			if((lowerBound*d_Size)<d_Enum.docFreq()&&d_Enum.docFreq()<=(upperBound*d_Size))
			{
				df_D.put(termString, d_Enum.docFreq());
				TermQuery termQuery=new TermQuery(new Term(mainField, termString));
				ScoreDoc[] hits=indexSearcher.search(termQuery, 1000000).scoreDocs;
				HashSet<Integer> inner_Set=new HashSet<>();
				for(ScoreDoc hit:hits)
				{
					inner_Set.add(hit.doc);
				}
				update_df_D.put(termString,inner_Set);
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
		while(sInAlgorithm2.size()<0.999*d_Size)
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
			else if(T.size()==0)
			{
				System.out.println("异常退出");
				break;//stop if can't achieve the 0.999 and can't get another query
			}
			Terms.add(qi_final);
			Set<Integer> to_be_sub=new HashSet<>();
			to_be_sub.addAll(update_df_D.get(qi_final));
			sInAlgorithm2.addAll(to_be_sub);
			for(String iterator:update_df_D.keySet())
			{
				update_df_D.get(iterator).removeAll(to_be_sub);
			}
		}
		return Terms;
	}
	public void queryInDB(String term,Map<String, HashSet<Integer>> searchInDB,Map<String, Integer> dfInSearchInDB,boolean downloadFlag) throws IOException
	{
		if(!searchInDB.containsKey(term))
		{
			TermQuery termQuery=new TermQuery(new Term(mainField,term));
			ScoreDoc[] hits=db_IndexSearcher.search(termQuery, IFINITY).scoreDocs;
			HashSet<Integer> inner=new HashSet<>();
			for(ScoreDoc every_hit:hits)
			{
				inner.add(every_hit.doc);
				if(!downloadFlag)continue;
				if(!allHits.contains(every_hit.doc))
				{
					Document document=db_IndexSearcher.doc(every_hit.doc);
					d_IndexWriter.addDocument(document);
					allHits.add(every_hit.doc);
				}
			}
			if(downloadFlag){
				q.add(term);
				allDownloadNumber+=hits.length;
				double HR=(double)allHits.size()/dbSize;
				double OR=(double)allDownloadNumber/allHits.size();
				System.out.println("HR="+HR+"   OR="+OR+"  "+term);
				hrorWriter.write(HR+","+OR);
				hrorWriter.newLine();
				termWriter.write(term);
				termWriter.newLine();
			}
			searchInDB.put(term, inner);//no new
			dfInSearchInDB.put(term, hits.length);
			d_IndexWriter.commit();
		}
	}
	public void Algorithm3(ArrayList<String> Terms) throws IOException
	{
		new_q.clear();
		//calculate the new and cost for every query
		Map<String, HashSet<Integer>> search_in_DB=new HashMap<>();
		Map<String, Integer> df_in_search_in_DB=new HashMap<>();
		backup.clear();
		backup.addAll(allHits);
		int k=recordK/2;
		int preK=0;
		for(int i=0;i<k;i++)
		{
			queryInDB(Terms.get(i), search_in_DB, df_in_search_in_DB,true);
		}
		while(true)
		{
			preK=k;
			k=2*k;
			if(k<=Terms.size())
			{
				if(qualityProcessing(k, preK,Terms, search_in_DB, df_in_search_in_DB))break;
			}
			else
			{
				k=Terms.size();
				qualityProcessing(k,preK, Terms, search_in_DB, df_in_search_in_DB);
				break;
			}
			System.out.println("k="+k);
		}
		//改进1
		recordK=k;
		for(int i=0;i<k;i++)
		{
			new_q.add(Terms.get(i));
		}
		System.out.println("\n\n\n");

	}
	public String gettingInitialTerm(int lower_limit,int upper_limit,IndexSearcher target_IndexSearcher) throws IOException
	{
		RandomAlgorithm randomAlgorithm=new RandomAlgorithm();
		String initial_Term=null;
		while(true)
		{
			initial_Term=randomAlgorithm.randomAccess();
			TermQuery qi_query=new TermQuery(new Term(mainField, initial_Term));
			ScoreDoc[] add_to_sample=target_IndexSearcher.search(qi_query,IFINITY).scoreDocs;
			if(add_to_sample.length>lower_limit&&add_to_sample.length<upper_limit)break;//确保取词足够
		}
		return initial_Term;
	}
	public void Algorithm1(String db_Path,String d_Path,Float lambda) throws IOException
	{
		hrorWriter=new BufferedWriter(new FileWriter(new File(storedFile)));//存HR OR
		termWriter=new BufferedWriter(new FileWriter(new File(termStoredFile)));//存词
		db_Directory=FSDirectory.open(Paths.get(db_Path));
		db_IndexReader=DirectoryReader.open(db_Directory);
		db_IndexSearcher=new IndexSearcher(db_IndexReader);
		initialQuery=gettingInitialTerm(10000, 30000, db_IndexSearcher);
		System.out.println(initialQuery);
		//initial_queries="our";//真实环境无法random
		q.add(initialQuery);
		termWriter.write(initialQuery);
		termWriter.newLine();
		d_Directory=FSDirectory.open(Paths.get(d_Path));
		d_IndexWriter=new IndexWriter(d_Directory,indexWriterConfig);
		
		ArrayList<String> tmp=new ArrayList<>();
		tmp.add(initialQuery);
		addFromOriginalToSample(tmp);
		System.out.println("inital sample size is"+allHits.size());
		
		d_IndexReader=DirectoryReader.open(d_Directory);
		d_IndexSearcher=new IndexSearcher(d_IndexReader);
		
		dbSize=db_IndexReader.numDocs();
		int d_Size=d_IndexReader.numDocs();
		allDownloadNumber=d_Size;
		hrorWriter.write((double)allHits.size()/dbSize+","+allDownloadNumber/allHits.size());
		System.out.println("HR="+(double)allHits.size()/dbSize+"  OR="+allDownloadNumber/allHits.size());
		hrorWriter.newLine();
		while(d_Size<(lambda*dbSize))
		{
			ArrayList<String> terms=Algorithm2(d_IndexSearcher,q);	
			for(String each:terms)
			{
				System.out.println(each);
			}
			Algorithm3(terms);	
			
			System.out.println("此轮共"+new_q.size()+"个term");
			termWriter.write("此轮共"+new_q.size()+"个term");
			termWriter.newLine();
			termWriter.newLine();
			
			d_IndexReader=DirectoryReader.openIfChanged((DirectoryReader)d_IndexReader);
			d_IndexSearcher=new IndexSearcher(d_IndexReader);
			d_Size=d_IndexReader.numDocs();
		}
		termWriter.close();
		hrorWriter.close();
		d_IndexWriter.close();
		db_IndexReader.close();
		d_IndexReader.close();
		db_Directory.close();
		d_Directory.close();
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		long start=System.currentTimeMillis();
		String storedFile="F:/experiment/HR_OR/wiki_HR_OR.csv";
		String termStoredFile="F:/experiment/Term/result_wiki.txt";
		Algorithm algorithm=new Algorithm();
		algorithm.storedFileSetter(storedFile);
		algorithm.termStoredFileSetter(termStoredFile);
		algorithm.mainFieldSetter("text");
		algorithm.boundSetter(0.001f, 0.20f);
		//algorithm.boundSetter(0.02f, 0.15f);
		//algorithm.analyzerSetter(true);
		algorithm.Algorithm1(wiki_new,sample_D_path4, 0.95f);
		long end=System.currentTimeMillis();
		System.out.println("总耗时："+(double)(end-start)/(1000*3600));
	}
}
