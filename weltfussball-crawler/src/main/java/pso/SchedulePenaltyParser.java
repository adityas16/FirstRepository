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
	//	static FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/seasons/", Constants.baseURL + Constants.all_matches + "/");
	DBCache webPageCache = DBCache.weltseasonCache();

	public  List<Map<String, String>> parse(Document doc){
		return parseAllShootouts(doc);
		//		return parseShootoutsWithLink(doc);
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
		String competitionName="",latestDay="",latestMonth="",latestYear="";
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
		String competitionID="";
		m = Pattern.compile("/competition/([^/]*)/").matcher(doc.select("#navi > div.sitenavi > div > div > ul:nth-child(1) > li > a").get(0).outerHtml());
		if(m.find()) {
			competitionID = m.group(1);
		}
		
		
		m = Pattern.compile("class=\"sitenavi_active\" href=\"/all_matches/(.*)/\">Schedule</a></li>").matcher(doc.outerHtml());
		String season_uri="";
		if(m.find()) {
			season_uri = m.group(1);
		}
		else {
			throw new RuntimeException("Could not find season URI");
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

			//Only needed for aet
//			if(!row.childNodes().get(11).outerHtml().contains("aet")) {
//				continue;
//			}

			Map<String, String> asMap = create_game_map();
			asMap.put("competition", competitionName);
			asMap.put("competition_ID", competitionID);

			asMap.put("day", latestDay);
			asMap.put("month", latestMonth);
			asMap.put("year", latestYear);
			
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
			
			m = Pattern.compile("align=\"right\"><a href=\"/teams/([^/]*)").matcher(row.outerHtml());
			if(m.find()) {
				asMap.put("homeTeamID", m.group(1));
			}
			
			m = Pattern.compile("<a href=\"/teams/([^/]*)/\" title=.*<a href=\\\"/teams/([^/]*)").matcher(row.outerHtml().replace("\n", "").replace("\r", ""));
			if(m.find()) {
				asMap.put("awayTeamID", m.group(2));
			}
			
			m = Pattern.compile("<a href=\"/report/(.*)/\"").matcher(row.outerHtml());
			if(m.find()) {
				asMap.put("uri" , m.group(1));
			}
			else {
				/*If the match does not have it own page/uri, create a URI as:
				<season_uri>_<yearmonthday>_<home_team_id><away_teamid>
				If there are no team IDs , use names instead:<season_uri>_<yearmonthday>_<home_team_name><away_team name>*/
				if(!asMap.get("homeTeamID").equals("") && !asMap.get("awayTeamID").equals("")) {
					asMap.put("uri" , "myid_"+season_uri+"_"+latestYear+""+latestMonth +"" +latestDay +"_" + asMap.get("homeTeamID")+"_" + asMap.get("awayTeamID"));
				}
				else {
					throw new RuntimeException("No Team IDs found for shootout game");
//					asMap.put("uri" , "welt_no_seq_" + (gameWithoutTeamIDCounter ++));
				}
				
			}
			
			records.add(asMap);
		}
		return records;
	}

	//Try and maintain the same order as GameParser.java
	private Map<String, String> create_game_map() {
		Map<String, String> asMap = new LinkedHashMap<String, String>();
		asMap.put("uri", "");
		asMap.put("homeTeam", "");
		asMap.put("awayTeam", "");
		asMap.put("homeScore", "");
		asMap.put("awayScore", "");
		asMap.put("competition", "");
		asMap.put("season", "");
		asMap.put("round", "");
		asMap.put("dist_from_final", "");
		asMap.put("competition_ID","");
		asMap.put("day", "");
		asMap.put("month", "");
		asMap.put("year", "");
		asMap.put("homeTeamID", "");
		asMap.put("awayTeamID", "");
		asMap.put("has_shootout", "");
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
				schedulePenaltyParser.parseURI("frauen-caf-womens-cup-1998-in-nigeria")
				);
	}
}

