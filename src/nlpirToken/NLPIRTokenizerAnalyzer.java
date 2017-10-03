package nlpirToken;



import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.Tokenizer;

/**
 * 
 * @author panhongyan
 *
 */
public class NLPIRTokenizerAnalyzer extends Analyzer{

	String data=null;
	int encoding=1;
	String sLicenceCode=null;
	String userDict=null;
	boolean bOverwrite=false;
	NLPIRTokenizer tokenizer = null;
	/**
	 * 鍒嗚瘝鍒濆鍖�
	 * 
	 * @param data
	 *            璇嶅�?璺�?
	 * @param encoding
	 *            缂栫�? 0锛欸BK锛�1锛歎TF-8
	 * @param sLicenceCode
	 *            鎺堟潈鐮侊紝榛樿涓�?""
	 * @param userDict
	 *            鐢ㄦ埛璇嶅吀鏂囦�?
	 * @param nOverwrite
	 *            鐢ㄦ埛璇嶅吀寮曞叆鏂瑰紡
	 */
	public NLPIRTokenizerAnalyzer(String data,int encoding,String sLicenceCode,String userDict,boolean bOverwrite) {
		this.data=data;
		this.encoding=encoding;
		this.sLicenceCode=sLicenceCode;
		this.userDict=userDict;
		this.bOverwrite=bOverwrite;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		tokenizer = new NLPIRTokenizer(this.data,this.encoding,this.sLicenceCode,this.userDict,this.bOverwrite);
		//tokenizer.exit();
		return new TokenStreamComponents(tokenizer);
	}
	
	public void exit() {
		tokenizer.exit();
	}

}
