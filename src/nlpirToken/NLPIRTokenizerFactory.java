package nlpirToken;

import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/**
 * 
 * @author panhongyan
 *
 */
public class NLPIRTokenizerFactory extends TokenizerFactory {

	public NLPIRTokenizerFactory(Map<String, String> args) {
		super(args);
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
	}

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
	public Tokenizer create(AttributeFactory factory, String data, int encoding, String sLicenceCode, String userDict,
			boolean bOverwrite) {
		return new NLPIRTokenizer(factory, data, encoding, sLicenceCode, userDict, bOverwrite);
	}

	@Override
	public Tokenizer create(AttributeFactory factory) {
		return new NLPIRTokenizer(factory);
	}
}
