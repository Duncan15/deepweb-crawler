package nlpirToken;

import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import nlpirToken.NLPIRTokenizerAnalyzer;

public class NLPIRTokenizerTest {

	public static void main(String[] args) throws Exception {
		NLPIRTokenizerAnalyzer nta = new NLPIRTokenizerAnalyzer("F:/Java-external-library/NPLIR20140928", 1, "", "", false);
		IndexWriterConfig inconf=new IndexWriterConfig(nta);
		inconf.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter index=new IndexWriter(FSDirectory.open(Paths.get("D:/index/")),inconf);
		Document doc = new Document();
		doc.add(new TextField("contents", "涓滄柟缃�12鏈�4鏃ユ秷鎭細2009骞�10鏈�21鏃�,杈藉畞鐪侀槣鏂板競濮旀敹鍒颁妇鎶ヤ俊,涓炬姤浠ヤ粯鐜夌孩涓洪鍚告瘨銆佸己濂搞�佽仛浼楁帆涔�,闃滄柊甯傚鏀挎硶濮斿壇涔﹁浜庢磱绛夊弬涓庡惛姣掋�佸己濂搞�佽仛浼楁帆涔辩瓑銆傚姝�,闃滄柊甯傚楂樺害閲嶈,璐ｆ垚闃滄柊甯傚叕瀹夊眬绔嬪嵆鎴愮珛璋冩煡缁�,鎶借皟绮惧共鍔涢噺灞曞紑璋冩煡銆傘��銆�璋冩煡鏈熼棿,缃插悕涓炬姤浜轰笂瀹樺畯绁ュ張閫氳繃灏逛笢鏂�(濂�)鍚戦槣鏂板競鍏畨灞�鍒戣鏀槦鎻愪緵涔﹂潰涓炬姤,涓炬姤浜庢磱绛夊弬涓庡惛姣掋�佸己濂搞�佽仛浼楁帆涔便��11鏈�19鏃�,姝ｄ箟缃戝彂琛ㄤ笂瀹樺畯绁ユ帴鍙楄鑰呬笓璁�,鍐嶆瀹炲悕涓炬姤浜庢磱绛夊弬涓庡惛姣掋�佸己濂搞�佽仛浼楁帆涔�,寮曡捣缃戞皯骞挎硾鍏虫敞銆傚姝よ窘瀹佺渷鏀挎硶濮斻�佺渷鍏畨鍘呴珮搴﹂噸瑙嗐�傚綋鏃�,璐ｆ垚鏈夊叧棰嗗涓撶▼璧撮槣鏂板惉鍙栨浠惰皟鏌ユ儏鍐点�備负鍔犲己瀵规浠剁殑鐫ｅ姙鍜屾寚瀵�,鐪佹湁鍏抽儴闂ㄨ繀閫熸垚绔嬪伐浣滅粍,璧撮槣鏂扮潱鍔炪�佹寚瀵兼浠惰皟鏌ュ伐浣�,骞跺皢鎯呭喌涓婃姤鏈夊叧閮ㄩ棬銆傘��銆�缁忓墠涓�娈佃皟鏌ヨ瘉鏄�,涓炬姤浜嬪疄涓嶅瓨鍦�,涓婂畼瀹忕ゥ琛屼负瑙︾姱銆婂垜娉曘�嬬243鏉�,娑夊珜璇憡闄峰缃�傛牴鎹�婂垜浜嬭瘔璁兼硶銆嬫湁鍏宠瀹�,闃滄柊甯傚叕瀹夊眬宸蹭簬11鏈�27鏃ヤ緷娉曠珛妗堜睛鏌ャ�備笂瀹樺畯绁ュ凡浜�2009骞�12鏈�1鏃ュ埌妗�,12鏈�2鏃ラ槣鏂板競娴峰窞鍖轰汉澶у父濮斾細宸蹭緷娉曞仠姝㈠叾浠ｈ〃璧勬牸,闃滄柊甯傚叕瀹夊眬瀵瑰叾杩涜鍒戜簨鎷樼暀,骞跺鍚屾浜哄肮涓滄柟杩涜鐩戣灞呬綇銆傜幇渚︽煡宸ヤ綔姝ｅ湪杩涜涓��",Field.Store.YES));
		index.addDocument(doc);
		index.flush();
		index.close();
		
		String field = "contents";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("D:/index/")));
		IndexSearcher searcher = new IndexSearcher(reader);
		QueryParser parser = new QueryParser(field, nta);
		Query query = parser.parse("閻楄婀曢弲顔荤瘎鏉╂垵閽�");
		TopDocs top=searcher.search(query, 100);
		ScoreDoc[] hits = top.scoreDocs;
		for(int i=0;i<hits.length;i++) {
			System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
			Document d = searcher.doc(hits[i].doc);
			System.out.println(d.get("contents"));
		}

	}
}
