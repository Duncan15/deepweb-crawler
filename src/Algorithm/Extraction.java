package Algorithm;
import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.sun.org.apache.bcel.internal.generic.FADD;

public class Extraction {
	
	public void random_Extract_by_number(int n,String source_path,String target_path) throws IOException
	{
		Directory db_Directory=FSDirectory.open(new File(source_path));
		Directory target_Directory=FSDirectory.open(new File(target_path));
		IndexReader db_IndexReader=IndexReader.open(db_Directory);
		IndexSearcher db_IndexSearcher=new IndexSearcher(db_IndexReader);
		IndexWriter target_IndexWriter=new IndexWriter(target_Directory, new Algorithm().indexWriterConfig);
		drawtable temp=new drawtable();
		temp.createsample(n, db_IndexSearcher, target_IndexWriter);
		
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new Extraction().random_Extract_by_number(1000000, "D:/experiment/enwiki-20161220-pages-articles-multistream-index","D:/experiment/enwiki-20161220-pages-articles-multistream-index-sample" );
	}

}
