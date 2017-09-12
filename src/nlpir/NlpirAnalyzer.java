package nlpir;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import com.sun.jna.Native;

public class NlpirAnalyzer extends Analyzer{
	
	private static class ICTCLASTokenzier extends Tokenizer {
		private TermAttribute termAttr;
		private OffsetAttribute offAttr;
		private Matcher matcher;

		public ICTCLASTokenzier(String segmented) {
			init(segmented);
		}

		private void init(String segmented) {
			termAttr = addAttribute(TermAttribute.class);
			offAttr = addAttribute(OffsetAttribute.class);
			matcher = Pattern.compile("(([^ ]+)| )/(\\w+)").matcher(segmented);
		}

		/*
		 * filter that removes only space and punctuations
		private boolean filter(String t, String type) {
			
			if (t.matches("\\s*")){
				return false;
			}
			else if (type.startsWith("w")) {
				return false;
			}
			else{
				return true;
			}
			
		}
		*/
		
		private boolean filter(String t, String type) {
			if (t.matches("\\s*")){
				return false;
			}
			//remove punctuation
			else if (type.startsWith("w")) {
				return false;
			}
			//remove 把
			else if(type.equals("pba")){
				return false;
			}
			//remove 被
			else if(type.equals("pbei")){
				return false;
			}
			else if(type.startsWith("u")&&!type.equals("uyy")&&!type.equals("uls")){
				return false;
			}
			else if(type.equals("e")){
				return false;
			}
			else if(type.startsWith("x")&&t.length()<=1){
				return false;
			}
			else{
				return true;
			}
		}

		@Override
		public boolean incrementToken() throws IOException {
			int s = offAttr.endOffset();
			clearAttributes();
			while (matcher.find()) {
				String t = matcher.group(1);
				//System.out.println("t:"+t);
				String type = matcher.group(3);
				//System.out.println("type:"+type);
				if (filter(t, type)) {
					termAttr.setTermBuffer(t, 0, t.length());
					offAttr.setOffset(s, s + t.length());
					return true;
				} else {
					s += t.length();
				}
			}
			return false;
		}
	}

	//private CLibrary library;
	private String result;
	private String libLocation ="F:/Java-external-library/NPLIR20140928/lib/win64/NLPIR";
	private String sDataPath = "F:/Java-external-library/NPLIR20140928";
	private String system_charset = "UTF-8";
	private int charset_type = 1;
	
	
	
	public NlpirAnalyzer() {
//		library = (CLibrary) Native.loadLibrary(libLocation, CLibrary.class);
//		library.Instance.NLPIR_Init(sDataPath, charset_type, "0");
	}
	//0 for gbk, 1 for utf-8
	public void setCharset(int type){
		this.charset_type = type;
	}
	
	public String getSegmentedString() {
		return result;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		CLibrary library = (CLibrary) Native.loadLibrary(libLocation, CLibrary.class);
		library.Instance.NLPIR_Init(sDataPath, charset_type, "0");
		try {
			byte[] buff = new byte[1024];
			int cpos = 0;
			int len;
			ReaderInputStream ris = new ReaderInputStream(reader);
			//CharBuffer cb = CharBuffer.allocate(buff.length);
			while ((len = ris.read(buff, cpos, 1024)) == 1024) {
				byte[] t = buff;
				buff = new byte[buff.length + 1024];
				System.arraycopy(t, 0, buff, 0, t.length);
				cpos += len;
			}
			cpos += len;
			result = library.NLPIR_ParagraphProcess(new String(buff, 0, cpos), 1);
			library.Instance.NLPIR_Exit();
			return new ICTCLASTokenzier(result);
		} catch (IOException e) {
			return null;
		}
	}



}
