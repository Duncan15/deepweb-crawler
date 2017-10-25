package RandomTermGetter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
public class RandomAlgorithm {
	final static String direcoryPath="src/RandomTermGetter/websterNew.txt";
	private BufferedReader buf;
	private ArrayList<String> dictionary;
	private int fileLength;
	private Random random;
	private int location;
	private HashSet<Integer> pool;
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
}
