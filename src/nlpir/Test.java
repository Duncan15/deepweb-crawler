package nlpir;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
//testing the nlpir analyzer
public class Test {
	// TODO Auto-generated method stub
	public static void main(String[] args) throws Exception{
//			String text = "(东方网)12月4日消息：2009年10月21日,辽宁省阜新市委收到举报信,举报以付玉红为首吸毒、强奸、聚众淫乱,阜新市委政法委副书记于洋等参与"+
//					"吸毒、强奸、聚众淫乱等。对此,阜新市委高度重视,责成阜新市公安局立即成立调查组,抽"+
//					"调精干力量展开调查。调查期间,署名举报人上官宏祥又通过尹东方(女)向阜新市公安"+
//					"局刑警支队提供书面举报,举报于洋等参与吸毒、强奸、聚众淫乱。11月19日,正义网发表"+
//					"上官宏祥接受记者专访,再次实名举报于洋等参与吸毒、强奸、聚众淫乱,引起网民广泛关"+
//					"注。对此辽宁省政法委、省公安厅高度重视。当日,责成有关领导专程赴阜新听取案件调查"+
//					"情况。为加强对案件的督办和指导,省有关部门迅速成立工作组,赴阜新督办、指导案件调"+
//					"查工作,并将情况上报有关部门。经前一段调查证明,举报事实不存在,上官宏祥行为触"+
//					"犯《刑法》第243条,涉嫌诬告陷害罪。根据《刑事诉讼法》有关规定,阜新市公安局已于"+
//					"11月27日依法立案侦查。上官宏祥已于2009年12月1日到案,12月2日阜新市海州区人大常"+
//					"委会已依法停止其代表资格,阜新市公安局对其进行刑事拘留,并对同案人尹东方进行监视";
			String text2="Pension Regina Map: Explore Trevelez on TripAdvisor Pension Regina Map,"
					+ " Hotels and Restaurants near Pension Regina Home Trevelez Trevelez Tourism Trevelez Hotels"
					+ " Trevelez Restaurants Trevelez Travel Forum Trevelez Visitors Guide Trevelez Photos Hotels "
					+ "Restaurants Best of 2012 More Travel Guides Travel Forum City Guides Mobile Write a Review "
					+ "Sign in with Facebook Sign in Register Now! FREE Mobile App Pension Regina Trevelez Tourism "
					+ "Trevelez Hotels Trevelez Special Offers Restaurants Travel Forum Travel Guide Photos Map Refine "
					+ "search 4 of 5 Clear filter(s) Price per night INR0 – INR8,000+ U.S. Dollars Euros British Pounds "
					+ "Canadian Dollars Australian Dollars Swiss Francs Japanese Yen Chinese Yuan Indian Rupees Swedish "
					+ "Krona Brasilian Real Turkish Lira Danish Krone Mexican Peso Argentine Peso Norwegian Krone Polish "
					+ "Zloty Singapore Dollars Thai Baht South Korean Won Russian Rubles Indonesian Rupiah New Taiwan Dollars"
					+ " Malaysian Ringgit EGP Traveller rating 1.0 – 5.0 Amenity type All (4) Free Parking (2) Restaurant (2)"
					+ " Property name Reset Free Newsletter Interested in Trevelez? We'll send you updates with the latest deals,"
					+ " reviews and articles for Trevelez each week. Recently Reviewed Hotels around Trevelez Best Western Hotel"
					+ " Dauro II 339 Reviews Last reviewed 30 Mar 2012 #25 of 138 hotels in Granada Melia Granada 288 Reviews"
					+ " Last reviewed 30 Mar 2012 #33 of 138 hotels in Granada Alcazaba de Busquistar 21 Reviews Last reviewed"
					+ " 28 Mar 2012 #1 of 1 speciality lodging in Busquistar El CastaNar Nazari 41 Reviews Last reviewed 28 Mar"
					+ " 2012 #1 of 1 hotels in Busquistar Las Terrazas de la Alpujarra 20 Reviews Last reviewed 28 Mar 2012 #1"
					+ " of 2 hotels in Bubion, Las Alpujarras Hotel Villa Sur 17 Reviews Last reviewed 26 Mar 2012 #1 of 1 "
					+ "hotels in Huetor Vega Hesperia Sabinal 247 Reviews Last reviewed 25 Mar 2012 #15 of 46 hotels in Almeria"
					+ " Casa Rural El Paraje 27 Reviews Last reviewed 18 Mar 2012 #1 of 1 B&Bs / inns in Berchules Hostal Jayma"
					+ " 29 Reviews Last reviewed 19 Mar 2012 #1 of 6 B&Bs / inns in Salobrena Hesperia Granada 260 Reviews Last"
					+ " reviewed 18 Mar 2012 #54 of 138 hotels in Granada Hotel NH Victoria 135 Reviews Last reviewed 14 Mar 2012"
					+ " #20 of 138 hotels in Granada San Gabriel Hotel -- Granada 37 Reviews Last reviewed 6 Mar 2012 #103 of 138"
					+ " hotels in Granada Villa Blanca Hotel 39 Reviews Last reviewed 1 Mar 2012 #4 of 4 hotels in Albolote Casa"
					+ " rural Vina y Rosales 13 Reviews Last reviewed 23 Feb 2012 #2 of 2 B&Bs / inns in Mairena La Fragua 6 "
					+ "Reviews Last reviewed 2 Feb 2012 #1 of 2 hotels in Trevelez, Las Alpujarras Explore the world! TripAdvisor"
					+ " has reviews and information on over 400,000 locations, including: Sightseeing Lodi Gardens in New Delhi"
					+ " 4.5 out of 5, 132 reviews Restaurants Hotel Saravana in Kanyakumari 1.5 out of 5, 3 reviews Hotels Savoy"
					+ " Suites in Noida 4.0 out of 5, 22 reviews Last reviewed 17 Mar 2012 Marugarh Resort in Jodhpur 3.5 out of"
					+ " 5, 19 reviews Last reviewed 24 Mar 2012 Jamal Resort in Srinagar 3.5 out of 5, 21 reviews Last reviewed"
					+ "22 Mar 2012 Shilon Resort in Shimla 4.0 out of 5, 28 reviews Last reviewed 22 Feb 2012 TSG Emerald View "
					+ "in Port Blair 3.5 out of 5, 34 reviews Last reviewed 30 Mar 2012 Shiva Residency in Dehradun 3.0 out of "
					+ "5, 6 reviews Last reviewed 21 Mar 2012 Piccolo Hotel in Kuala Lumpur 4.0 out of 5, 350 reviews Last reviewed"
					+ " 30 Mar 2012 Hotel Chanda in Jhansi Last reviewed 27 Mar 2012 The Elephant Court in Thekkady 4.0 out of 5,"
					+ " 174 reviews Last reviewed 29 Mar 2012 Club Mahindra Nature Trails Corbett in Nainital 4.5 out of 5, 39 "
					+ "reviews Last reviewed 19 Mar 2012 Hotel Kelson DX in New Delhi River Queen Houseboats in Alappuzha 4.0 "
					+ "out of 5, 13 reviews Last reviewed 22 Mar 2012 The Claridges, Surajkund, Delhi, NCR in Faridabad 4.0 out"
					+ " of 5, 89 reviews Last reviewed 27 Mar 2012 Home Europe Spain Andalucia Province of Granada Las Alpujarras"
					+ " Trevelez Hotels Map of Pension Regina Hotels (2) B&Bs / Inns (2) Speciality Lodging (1) You are zoomed "
					+ "out too far to see location pins. Please zoom back in. Search hotels City Check-in Check-out Adults 1 2 "
					+ "3 4 Search Your recent searches No recent searches Map showing X-Y of Z Clear filter(s) Reset map Top Va"
					+ "lues first (0) Show points of interest Restaurants Places to visit Airports Search by address or point of"
					+ " interest Travel deals: Trevelez Trevelez:省钱，即刻预订！ Booking.com最佳选择，价格低廉 La Fragua II:省钱，即刻预订！ Boo"
					+ "king.com最佳选择，价格低廉 La Fragua:省钱，即刻预订！ Booking.com最佳选择，价格低廉 View all 11 Trevelez travel deals Spons"
					+ "ored links * Free Trevelez Guide Get your quick guide to the top hotels, restaurants and things to do. Gra"
					+ " it and Go! Trevelez weather essentials Month High Low Mar 64°F 17°C 38°F 3°C Apr 68°F 20°C 43°F 6°C May "
					+ "76°F 24°C 49°F 9°C Jun 87°F 30°C 56°F 13°C Jul 92°F 33°C 59°F 15°C Aug 91°F 32°C 58°F 14°C SEE NEXT 6 MON"
					+ "THS » More weather for Trevelez | Powered by Weather Underground Other Destinations Hotels in Popular Pro"
					+ "vince of Granada Destinations Granada 434 hotels, 14,777 Reviews Guadix 25 hotels, 122 Reviews Sierra Nev"
					+ "ada National Park 63 hotels, 549 Reviews Hotels Travellers Trust 1-4 of 4 1 Sort by [ Popularity ] [ Pric"
					+ "e ] Top Values first (0) La Fragua, Trevelez, Spain San Antonio 4, 18417 Trevelez, Spain ABS:HotelCheckRa"
					+ "tes-d310031?&src=LocalMapsRedesign&fromServlet=LocalMapsRedesign&Action=QC_Button INR2,353 - 2,711 (€35 - "
					+ "€40) Avg. price/night* 6 reviews #1 of 2 hotels in Trevelez “ Traditional food ” A great place to taste tr"
					+ "aditional local cuisine and try the La Alujarra... more 23 January 2012 Hotel photos Hotel Restaurante Pe"
					+ "pe Alvarez, Trevelez, Spain Plaza Francisco Abellan 16, Trevelez, Spain 5 reviews #1 of 2 B&Bs in Trevele"
					+ "z “ Wonderful ” I just spent 5 nights at 'Pepe Alavarez'. I will definately return... more 6 August 2011 "
					+ "Hotel photos La Fragua II, Trevelez, Spain C/ Posadas, s/n, 18417 Trevelez, Spain ABS:HotelCheckRates-d678"
					+ "186?&src=LocalMapsRedesign&fromServlet=LocalMapsRedesign&Action=QC_Button INR3,939 - 4,552 ($77 - $89) Avg."
					+ " price/night* 8 reviews #2 of 2 hotels in Trevelez “ A hidden gem! ” What a wonderfully situated hotel. Qu"
					+ "ietly located on the edge of the... more 14 October 2011 Hotel photos Pension Regina, Trevelez, Spain Pl. "
					+ "Francisco Abellan, s/n, 18417 Trevelez, Spain 1-4 of 4 1 About TripAdvisor™ TripAdvisor features reviews a"
					+ "nd advice on hotels, resorts, flights, restaurants, holiday packages, travel guides, and lots more. Review"
					+ "s and advice on hotels, resorts, flights, restaurants, holiday packages, and lots more! Visit TripAdvisor'"
					+ "s international sites: About Us | Write a review | Owners & DMO/CVB | Help Centre | Business Listings © 20"
					+ "12 TripAdvisor LLC All rights reserved. TripAdvisor Terms of Use and Privacy Policy. * TripAdvisor LLC is "
					+ "not a booking agent and does not charge any service fees to users of our site... (more) TripAdvisor LLC is "
					+ "not responsible for content on external web sites. Taxes, fees not included for deals content. Other TripA"
					+ "dvisor sites: See all sites Cruise Critic The ultimate cruise resource: cruise reviews, deals, news and ad"
					+ "vice. SeatGuru The world's most comprehensive source for airline seats, airline info, & in-flight amenitie"
					+ "s. Family Vacation Critic The best family holidays start here. Get the #1 travel app FREE>> Take TripAdviso"
					+ "r with you Find 50 million reviews & opinions of hotels, things to do, restaurants and more GET IT Named '"
					+ "#1 Travel App' by Frommer's";

			NlpirAnalyzer analy = new NlpirAnalyzer();
			analyze(analy,text2);
			
			
			Directory directorySample = FSDirectory.open(new File("F:/sIndex"));
			Analyzer analyzer = new NlpirAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			IndexWriter writer =new IndexWriter(directorySample, iwc);
			Document doc = new Document();
			doc.add(new Field("text",text2,Field.Store.YES,Field.Index.ANALYZED,Field.TermVector.WITH_POSITIONS_OFFSETS));
			writer.addDocument(doc);
			writer.optimize();
			writer.close();
			
			IndexReader reader = IndexReader.open(directorySample);
			
			TermEnum terms = reader.terms();
			while(terms.next()){
				System.out.println(terms.term().text()+"\t"+reader.docFreq(terms.term()));
				
			}
			
			

		}
		
		 public static void analyze(Analyzer analyzer, String text) throws Exception {
			 try {
		            //将一个字符串创建成Token流
		            TokenStream stream  = analyzer.tokenStream("", new StringReader(text));
		            //保存相应词汇
		            CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
		            while(stream.incrementToken()){
		                System.out.print("[" + cta + "]");
		            }
		            System.out.println();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }


}
