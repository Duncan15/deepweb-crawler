import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SmallText {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Algorithm al=new Algorithm();
		Directory db_Directory=FSDirectory.open(new File(al.DB_path));
		IndexReader db_IndexReader=IndexReader.open(db_Directory);
		IndexSearcher db_IndexSearcher=new IndexSearcher(db_IndexReader);
		Directory d_Directory=FSDirectory.open(new File(Algorithm.sample_D_path));
		IndexWriter d_IndexWriter=new IndexWriter(d_Directory,Algorithm.indexWriterConfig);
		
		Set<String> s=new HashSet<>();
		s.add("query");
		
		for(int i=0;i<5;i++)
		{
			d_IndexWriter.deleteAll();
			d_IndexWriter.commit();
			al.add_from_original_to_sample(Algorithm.all_hits, d_IndexWriter, db_IndexReader, db_IndexSearcher, s);
			System.out.println("OK");
			al.all_hits.clear();

		}
		
	}

}
