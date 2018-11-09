package pso;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.aditya.research.pso.parsers.Parser;


public class SchedulePenaltyParser implements Parser {
	static int gameWithoutLinkCounter = 1;
//	static FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/seasons/", Constants.baseURL + Constants.all_matches + "/");
	DBCache webPageCache = DBCache.weltseasonCache();
	
	public  List<Map<String, String>> parse(Document doc){
		//return parseAllShootouts(doc);
		return parseShootoutsWithLink(doc);
	}

	private List<Map<String, String>> parseShootoutsWithLink(Document doc) {
		List<Map<String, String>> records  = new ArrayList<Map<String,String>>();
		List<String> urls = new ArrayList<String>();
		Elements elements = doc.getElementsMatchingText("pso");
		for (Element element : elements) {
			if(!element.attr("href").equals("")){
				String uri = element.attr("href").replace("/"+ Constants.report +"/", "").replace('/',' ').trim();
				Map<String, String> asMap = new LinkedHashMap<String, String>();
				asMap.put("uri" , uri);
				records.add(asMap);
			}
		}
		return records;
	}

	private List<Map<String, String>> parseAllShootouts(Document doc) {
		List<Map<String, String>> records  = new ArrayList<Map<String,String>>();
		String competitionName="NA",latestDay="NA",latestMonth="NA",latestYear="NA";
		Matcher m = Pattern.compile(" » (.*) \\d{4}").matcher(doc.select("#navi > div.breadcrumb > h1").get(0).outerHtml());
		if(m.find()) {
			competitionName = m.group(1);
		}
		else {
			m = Pattern.compile("<h1>(.*) \\d{4}").matcher(doc.select("#navi > div.breadcrumb > h1").get(0).outerHtml());
			if(m.find()) {
				competitionName = m.group(1);
			}
		}
		
		Node table = doc.getElementsByAttributeValue("class", "portfolio").get(0).getElementsByAttributeValue("class", "box").get(0).getElementsByClass("standard_tabelle").get(0).childNode(1);
		
		Iterator<Node> rowIterator = table.childNodes().iterator();
		while (rowIterator.hasNext()) {
						rowIterator.next();
			if(!rowIterator.hasNext()) {
				break;
			}
			Node row = rowIterator.next();
			
			m = Pattern.compile("title=\"further matches on (\\d+)/(\\d+)/(\\d+)\"").matcher(row.outerHtml());
			if(m.find()) {
				latestDay = m.group(1);
				latestMonth = m.group(2);
				latestYear = m.group(3);
			}
			
			if(!row.outerHtml().contains("pso")) {
				continue;
			}
			
			Map<String, String> asMap = create_game_map();
			
			asMap.put("day", latestDay);
			asMap.put("month", latestMonth);
			asMap.put("year", latestYear);
			
			m = Pattern.compile("<a href=\"/report/(.*)/\"").matcher(row.outerHtml());
			if(m.find()) {
				asMap.put("uri" , m.group(1));
			}
			else {
				asMap.put("uri" , "welt_no_seq_" + (gameWithoutLinkCounter ++));
			}
			
			asMap.put("competition", competitionName);
			
			
			m = Pattern.compile("/schedule/.*\">(.*)</a").matcher(row.outerHtml());
			if(m.find()) {
				asMap.put("round", m.group(1));
			}
			 
			 m = Pattern.compile("<a href=\"/teams/.* title=\"(.*)\\\"").matcher(row.outerHtml());
			 if(m.find()) {
				 asMap.put("homeTeam", m.group(1));
			 }
			 if(m.find()) {
				 asMap.put("awayTeam", m.group(1));
			 }
			 records.add(asMap);
			 System.out.println(asMap);
		}
		return records;
	}

	//Try and maintain the same order as GameParser.java
	private Map<String, String> create_game_map() {
		Map<String, String> asMap = new LinkedHashMap<String, String>();
		asMap.put("uri", "NA");
		asMap.put("homeTeam", "NA");
		asMap.put("awayTeam", "NA");
		asMap.put("homeScore", "NA");
		asMap.put("awayScore", "NA");
		asMap.put("competition", "NA");
		asMap.put("season", "NA");
		asMap.put("round", "NA");
		asMap.put("dist_from_final", "NA");
		//asMap.put("competition_ID","NA");
		asMap.put("day", "NA");
		asMap.put("month", "NA");
		asMap.put("year", "NA");
		return asMap;
	}
	
	public List<String> parseURI(String uri) throws IOException{
		List<Map<String, String>> records = parse(webPageCache.get(uri));
		List<String> uris = new ArrayList<String>();
		for (Map<String, String> record : records) {
			uris.add(record.get("uri"));
		}
		return uris;
	}
	
	public static void main(String[] args) throws IOException {
		SchedulePenaltyParser schedulePenaltyParser = new SchedulePenaltyParser();
//		System.out.println(
//				schedulePenaltyParser.parseURI("aze-kubok-2013-2014")
//				);
//		System.out.println(
//				schedulePenaltyParser.parseURI("wm-2014-in-brasilien")
//				);
		
		System.out.println(
				schedulePenaltyParser.parseURI("ec-der-pokalsieger-1995-1996")
				);

		
	}
}

