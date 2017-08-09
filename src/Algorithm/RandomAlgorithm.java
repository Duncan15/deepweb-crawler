package Algorithm;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class RandomAlgorithm {
	final static String direcoryPath="src/Algorithm/websterNew.txt";
	private BufferedReader buf;
	private ArrayList<String> dictionary;
	private int fileLength;
	private Random random;
	private int location;
	private HashSet<Integer> pool;
	private ArrayList<String> container;
	public String randomAccess()
	{
		do
		{
			location=random.nextInt(fileLength);
		}
		while(pool.contains(location));
		pool.add(location);
		return dictionary.get(location);
		
	}
	public RandomAlgorithm() throws IOException
	{


		// TODO Auto-generated constructor stub
		dictionary=new ArrayList<>();
		pool=new HashSet<>();
		
		random=new Random(new Date().getTime());
		buf=new BufferedReader(new FileReader(new File(direcoryPath)));
		String temp;
		while((temp=buf.readLine())!=null)
		{
			dictionary.add(temp.trim());
		}
		buf.close();
		fileLength=dictionary.size();
	}
	public void algorithm() throws IOException
	{
		BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(new File("D:/experiment/result_random.csv")));

		Algorithm used=new Algorithm();
		container=new ArrayList<>();
		Directory db_Directory=FSDirectory.open(new File(Algorithm.DB_path_Wiki));
		IndexReader db_IndexReader=IndexReader.open(db_Directory);
		IndexSearcher db_IndexSearcher=new IndexSearcher(db_IndexReader);
		
		Directory d_Directory=FSDirectory.open(new File(Algorithm.sample_D_path));
		
		IndexWriter d_IndexWriter=new IndexWriter(d_Directory,used.indexWriterConfig);
		IndexReader d_IndexReader;
		
		
		float db_size=db_IndexReader.numDocs();
		float d_size=0,pre_d_size=0,total_hit=0;
//		bufferedWriter.write("target database have"+db_size+" documents\n");
		while(d_size<0.90*db_size)
		{
			container.clear();
			String query=this.randomAccess();
//			bufferedWriter.write("the query for this turn is "+query+"\n");
			container.add(query);
//			bufferedWriter.write("the "+pool.size()+"turn \n");
			total_hit+=used.add_from_original_to_sample(used.all_hits, d_IndexWriter, db_IndexReader, db_IndexSearcher,container);
			
			d_IndexReader=IndexReader.open(d_Directory);
			//d_IndexReader=IndexReader.openIfChanged(d_IndexReader);
			d_size=d_IndexReader.numDocs();
//			float New=d_size-pre_d_size;
//			pre_d_size=d_size;
			d_IndexReader.close();
			
			float HR=0,OR=0;
			HR=d_size/db_size;
			OR=total_hit/d_size;
			bufferedWriter.write(HR+","+OR+"\n");
			System.out.println(HR+"\t"+OR);
			
			
		}

		bufferedWriter.close();
		d_IndexWriter.close();
		db_IndexSearcher.close();
		//d_IndexReader.close();
		db_IndexReader.close();
		d_Directory.close();
		db_Directory.close();

	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		RandomAlgorithm tmp=new RandomAlgorithm();
		tmp.algorithm();
	}

}
