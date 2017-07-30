
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.sun.javafx.geom.Point2D;
public class AlgorithmByTable {
	private Directory db_Directory;
	private Directory sample_Directory;
	private IndexReader db_IndexReader;
	private IndexReader sample_IndexReader;
	private IndexSearcher db_IndexSearcher;
	private IndexSearcher sample_IndexSearcher;
	private IndexWriter sample_IndexWriter;
	private String initail_Query;
	private BufferedWriter bufferedWriter;
	private ArrayList<Point2D> trend=new ArrayList<>();
	public AlgorithmByTable(String db_Path,String sample_Path,String query,String txt_Path)
	{
		try
		{
			db_Directory=FSDirectory.open(new File(db_Path));
			sample_Directory=FSDirectory.open(new File(sample_Path));
			db_IndexReader=IndexReader.open(db_Directory);
//			sample_IndexReader=IndexReader.open(sample_Directory);
			db_IndexSearcher=new IndexSearcher(db_IndexReader);
			sample_IndexWriter=new IndexWriter(sample_Directory, Algorithm.indexWriterConfig);
			bufferedWriter=new BufferedWriter(new FileWriter(new File(txt_Path)));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		trend.add(new Point2D(100,(float) 25.5));
		trend.add(new Point2D(1000, (float) 57.6));
		trend.add(new Point2D(10000,(float) 64));
		trend.add(new Point2D(100000,(float) 64));
		trend.add(new Point2D(500000,128));
		trend.add(new Point2D(700000,(float) 128));
		
		initail_Query=query;
	}
	public int calculate_number_of_query(int x)

	{
		int i=0,y=0;
		
		for(;i<trend.size();i++)
		{
			if(x<trend.get(i).x)
			{
				if(i==0)
				{
					y=(int) (x*trend.get(i).y/trend.get(i).x);
					break;
				}
				y=(int) ((x-trend.get(i-1).x)*(trend.get(i).y-trend.get(i-1).y)/(trend.get(i).x-trend.get(i-1).x)+trend.get(i-1).y);
				break;
			}
		}
		if(i==trend.size())y=(int) trend.get(i-1).y;
		return y;
	}
	public static void initial_create_sample(int num,HashSet<Integer> target_pool,IndexWriter target_IndexWriter,IndexSearcher source_IndexSearcher) throws CorruptIndexException, IOException
	{
		int counter=0;
		int source_num=source_IndexSearcher.maxDoc();
		Random rand=new Random(new Date().getTime());
		while(counter<num)
		{
			int tmp=rand.nextInt(source_num);
			if(!target_pool.contains(tmp))
			{
				target_pool.add(tmp);
				counter++;
			}
			
		}
		for(int i :target_pool)
		{
			Document a=source_IndexSearcher.doc(i);
			target_IndexWriter.addDocument(a);
		}
		target_IndexWriter.commit();
		
	}
	public void one_turn_all_in_by_SET() throws IOException
	{
		Algorithm used=new Algorithm();
		used.main_Field_Setter("text");
		used.bound_Setter(0.001f, 0.20f);
		
		float db_size=db_IndexReader.numDocs();
		float sample_size=0,total_hit=0;
		float HR=0,OR=0;
		
		//
		initial_create_sample(3000, (HashSet<Integer>) used.all_hits, sample_IndexWriter, db_IndexSearcher);
		sample_IndexReader=IndexReader.open(sample_Directory);
		sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		System.out.println("阶段1");
		System.out.println("样本大小"+sample_IndexReader.numDocs());
		
		ArrayList<String> array=used.Algorithm_2(sample_IndexReader, sample_IndexSearcher, Algorithm.q);
		System.out.println("阶段2");
		System.out.println("一轮拿到词的个数为"+array.size());
		
		Set<String> container=new HashSet<>();
		container.addAll(array);
		total_hit+=used.add_from_original_to_sample(used.all_hits, sample_IndexWriter, db_IndexReader, db_IndexSearcher,container);
		System.out.println("阶段3");
		
		sample_IndexReader=IndexReader.openIfChanged(sample_IndexReader);
		sample_IndexSearcher.close();
		sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		sample_size=sample_IndexReader.numDocs();
		HR=sample_size/db_size;
		OR=total_hit/sample_size;
		System.out.println("HR="+HR);
		System.out.println("OR="+OR);
		
	}
	public void experiment_all_in()throws IOException
	{
		Algorithm used=new Algorithm();
		Set<String> container=new HashSet<>();
		float db_size=db_IndexReader.numDocs();
		float sample_size=0,total_hit=0;
		float HR=0,OR=0;
		
		container.add(initail_Query);
		used.q.add(initail_Query);
		total_hit+=used.add_from_original_to_sample(used.all_hits, sample_IndexWriter, db_IndexReader, db_IndexSearcher,container);
		sample_IndexReader=IndexReader.open(sample_Directory);
		sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		sample_size=sample_IndexReader.numDocs();
		HR=sample_size/db_size;
		OR=total_hit/sample_size;
		bufferedWriter.write(HR+","+OR+"\n");
		System.out.println(HR+"\t"+OR);
		
		
		while(sample_size<0.90*db_size)
		{
			container.clear();
				
			ArrayList<String> array=used.Algorithm_2(sample_IndexReader, sample_IndexSearcher, Algorithm.q);
			System.out.println("»ñµÃquery¸öÊý£º"+array.size());
			
			for(int i=0;i<array.size();i++)
			{
				container.add(array.get(i));
			}
			total_hit+=used.add_from_original_to_sample(used.all_hits, sample_IndexWriter, db_IndexReader, db_IndexSearcher,container);
			sample_IndexReader=IndexReader.openIfChanged(sample_IndexReader);
			sample_IndexSearcher.close();
			sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
			sample_size=sample_IndexReader.numDocs();
			HR=sample_size/db_size;
			OR=total_hit/sample_size;
			bufferedWriter.write(HR+","+OR+"\n");
			System.out.println(HR+"\t"+OR);
		}
		
	}
	public void experiment() throws IOException
	{
		Algorithm used=new Algorithm();
		Set<String> container=new HashSet<>();
		float db_size=db_IndexReader.numDocs();
		float sample_size=0,total_hit=0;
		float HR=0,OR=0;
		
		container.add(initail_Query);
		used.q.add(initail_Query);
		total_hit+=used.add_from_original_to_sample(used.all_hits, sample_IndexWriter, db_IndexReader, db_IndexSearcher,container);
		sample_IndexReader=IndexReader.open(sample_Directory);
		sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
		sample_size=sample_IndexReader.numDocs();
		HR=sample_size/db_size;
		OR=total_hit/sample_size;
		bufferedWriter.write(HR+","+OR+"\n");
		System.out.println(HR+"\t"+OR);
		
		
		while(sample_size<0.90*db_size)
		{
			container.clear();
				
			ArrayList<String> array=used.Algorithm_2(sample_IndexReader, sample_IndexSearcher, Algorithm.q);
			System.out.println("»ñµÃquery¸öÊý£º"+array.size());
			int num=calculate_number_of_query((int)sample_size);
			System.out.println("Ó¦È¡"+num+"¸öquery¡£");
			if(array.size()>num)
			{
				for(int i=0;i<num;i++)
				{
					container.add(array.get(i));
				}
			}
			else
			{
				for(int i=0;i<array.size();i++)
				{
					container.add(array.get(i));
				}
			}

			total_hit+=used.add_from_original_to_sample(used.all_hits, sample_IndexWriter, sample_IndexReader, db_IndexSearcher,container);
			sample_IndexReader=IndexReader.openIfChanged(sample_IndexReader);
			sample_IndexSearcher.close();
			sample_IndexSearcher=new IndexSearcher(sample_IndexReader);
			sample_size=sample_IndexReader.numDocs();
			HR=sample_size/db_size;
			OR=total_hit/sample_size;
			bufferedWriter.write(HR+","+OR+"\n");
			System.out.println(HR+"\t"+OR);
		}
		
	}
	public void destroy() throws IOException
	{
		bufferedWriter.close();
		db_IndexSearcher.close();
		sample_IndexSearcher.close();
		sample_IndexWriter.close();
		db_IndexReader.close();
		db_Directory.close();
		sample_Directory.close();
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String txt_Path="D:/experiment/result_all_in.csv";
		AlgorithmByTable tmp=new AlgorithmByTable(Algorithm.DB_path_Wiki2, Algorithm.sample_D_path, Algorithm.initial_queries, txt_Path);
		//tmp.experiment_all_in();
		tmp.one_turn_all_in_by_SET();
		tmp.destroy();
	}

}

