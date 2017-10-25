package Algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.cwc.lucene.analyzer.chinese.JieBaChineseAnalyzer;


public class SmallText {
	public static final String path="F:/experiment/citeSeer_vv6";
	public static final String targetPath="F:/experiment/citeSeer_new";
	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		Directory dir=FSDirectory.open(Paths.get(targetPath));
//		DirectoryReader reader=DirectoryReader.open(dir);
//		System.out.println(reader.numDocs());
//		for(int i=0;i<reader.numDocs();i++)
//		{
//			System.out.println(reader.document(i).get("text"));
//		}
		StandardAnalyzer analyzer=new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig=new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		IndexWriter indexWriter=new IndexWriter(dir,indexWriterConfig);
		
		File file=new File(path);
		if(file.exists())
		{
			List<File> list=new ArrayList<>();
			File[] files=file.listFiles();
			for(File eachFile:files)
			{
				BufferedReader bufferedReader=new BufferedReader(new FileReader(new File(eachFile.getAbsolutePath())));
				String title=eachFile.getName();
				System.out.println(title);
				String content="";
				String tmp;
				while((tmp=bufferedReader.readLine())!=null)
				{
					content+=tmp;
				}
				content=new String(content.getBytes("utf8"),"utf-8");
				Document document=new Document();
				document.add(new StringField("title", title, Store.YES));
				document.add(new TextField("text", content, Store.YES));
				indexWriter.addDocument(document);
				bufferedReader.close();
			}
		}
		
		
		
		
//		Fields fields=MultiFields.getFields(indexReader);
//		Terms terms=fields.terms("body");
//		TermsEnum d_Enum=terms.iterator();
//		while(d_Enum.next()!=null)
//		{
//			System.out.println(d_Enum.term().utf8ToString()+"\t"+d_Enum.docFreq());
//		}
		//indexWriter.forceMerge(1);
		
		indexWriter.close();
	}

}
