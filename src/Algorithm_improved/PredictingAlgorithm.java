package Algorithm_improved;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

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
public class PredictingAlgorithm {
	private String stored_file="";
	private String sample_Path="";
	private String main_Field;
	private float upper_bound;
	private float lower_bound;
	private boolean firstTurnFlag=true;
	private ArrayList<String> Q=new ArrayList<>();//all the term this algorithm has selected
	private Directory sample_Directory;
	private IndexReader sample_IndexReader;
	private IndexSearcher sample_IndexSearcher;
	private BufferedWriter bufferedWriter;
	
	Random global_Random=new Random(new Date().getTime());//全局随机数
	public PredictingAlgorithm(String sampleAddr,String outputTxtAddr)
	{
		bound_Setter(0.15f, 0.02f);
		main_Field_Setter("body");
		sample_Path_Setter(sampleAddr);
		stored_File_Setter(outputTxtAddr);
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
	public void sample_Path_Setter(String tmp)
	{
		this.sample_Path=tmp;
	}
	public void main_Field_Setter(String tmp)
	{
		this.main_Field=tmp;
	}

	//point_num用来设置拟合的点数
	public double[] curve_fitting(ArrayList<WeightedObservedPoint> points) throws IOException
	{
		
		MyFuncFitter fitter = new MyFuncFitter();
		final double coeffs[] = fitter.fit(points);
		return coeffs;
	}
		
	public ArrayList<Pair> dealing_for_selecting_algorithm(IndexReader target_IndexReader,IndexSearcher target_IndexSearcher) throws IOException
	{
		int d_Size=target_IndexReader.numDocs();
		Fields fields=MultiFields.getFields(target_IndexReader);
		Terms terms=fields.terms(main_Field);
		TermsEnum d_Enum=terms.iterator();
		ArrayList<Pair> term_df=new ArrayList<>();
		while(d_Enum.next()!=null)
		{
			String termString=d_Enum.term().utf8ToString();
			if(lower_bound*d_Size<=d_Enum.docFreq()&&d_Enum.docFreq()<=upper_bound*d_Size)
			{
				term_df.add(new Pair(termString, d_Enum.docFreq()));
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
		//分段
		int stepLength=0;
		int point_num=100;
		if(100>=rank_TermSet_Map.size())//异常情况
		{
			stepLength=1;
			point_num=rank_TermSet_Map.size();
		}
		else
		{
			stepLength=rank_TermSet_Map.size()/(point_num+1);//正常情况
		}
		double step=1;
		ArrayList<String> queryList=new ArrayList<>();
		for(int i=0;i<point_num;i++)
		{
			int step_size=rank_TermSet_Map.get((int)step).size();
			String step_Term=(String) rank_TermSet_Map.get((int)step).toArray()[global_Random.nextInt(step_size)];
			queryList.add(step_Term);
			step+=stepLength;
		}
		//分段
		
		//调用接口(queryList) return Hashmap
		
		HashMap<String, Integer> queryResultMap=null;//临时
		step=1;
		ArrayList<WeightedObservedPoint> points=new ArrayList<>();
		for(int i=0;i<point_num;i++)
		{
			points.add(new WeightedObservedPoint(1.0, step,Math.log(queryResultMap.get(queryList.get(i)))));
			step+=stepLength;
		}
		double coeffs[]=curve_fitting(points);
		//分段
		
		//根据拟合出来的曲线计算所有term的total_df
		MyFunc myFunc=new MyFunc();
		for(Pair each:term_df)
		{
			double tmps= myFunc.value(each.rank,coeffs);
			each.total_df=(int) Math.pow(Math.E, tmps);
		}
		return term_df;
	}
	
	public ArrayList<String> selecting_algorithm(IndexReader target_IndexReader,IndexSearcher target_IndexSearcher,ArrayList<Pair> info,ArrayList<String> set_be_checked) throws IOException//select_Flag为true时选一个词，select_Flag为false时选多个词
	{
		ArrayList<String> res=new ArrayList<>();
		double biggest_New_Cost_Rate=0;
		//select_Flag==true时，一轮只选一个term
		ArrayList<Pair> T=new ArrayList<>();
		for(Pair each:info)
		{
			if(set_be_checked.contains(each.term))continue;//已选中的词跳过
			float tmp=(float)(each.total_df-each.sample_df)/each.total_df;
			if(tmp>biggest_New_Cost_Rate)
			{
				T.clear();
				biggest_New_Cost_Rate=tmp;
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
		for(String each:res)
		{
			set_be_checked.add(each);
		}
		return res;
	}
	
	public void simple_crawling_algorithm() throws IOException
	{
		sample_Directory=FSDirectory.open(Paths.get(sample_Path));
		bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(stored_file)) ,"utf-8"));
		if(firstTurnFlag)
		{
			String initial_Term="模拟";
			bufferedWriter.write(initial_Term);
			firstTurnFlag=false;
		}
		else{
			sample_IndexReader=DirectoryReader.open(sample_Directory);
			sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
			ArrayList<Pair> term_info=dealing_for_selecting_algorithm(sample_IndexReader, sample_IndexSearcher);
			ArrayList<String> res=null;
			res=selecting_algorithm(sample_IndexReader,sample_IndexSearcher,term_info, Q);
			for(String each:res)
			{
				bufferedWriter.write(each);
				bufferedWriter.newLine();
			}
			sample_IndexReader.close();
		}
		bufferedWriter.close();
		sample_Directory.close();
	}
}
