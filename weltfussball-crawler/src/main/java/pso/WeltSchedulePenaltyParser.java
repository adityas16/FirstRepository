package pso;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.aditya.research.pso.parsers.IScheduleParser;


public class WeltSchedulePenaltyParser implements IScheduleParser{
	//	static FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/seasons/", Constants.baseURL + Constants.all_matches + "/");
	DBCache webPageCache = DBCache.weltseasonCache();
	private static Set<String> matchesWithNoPage = new HashSet<>();

	
	@Override
	public  List<String> parse(Document doc) throws IOException{
		extractMatchesWithNoPage(doc);
		
		List<String> urls = new ArrayList<String>();
		Elements elements = doc.getElementsMatchingText("pso");
		for (Element element : elements) {
			if(!element.attr("href").equals("")){
				urls.add(element.attr("href").replace("/"+ Constants.report +"/", "").replace('/',' ').trim());
			}
		}
		return urls;
	}

	private void extractMatchesWithNoPage(Document doc) {
		String date="NA";
		String season = "NA";
		String competition ="NA",homeTeam="NA",awayTeam="NA";
		Matcher m = Pattern.compile("content=\"//www.worldfootball.net/all_matches/([a-z|-]*)-(\\d+)").matcher(doc.outerHtml());
		if(m.find()){
			competition = m.group(1);
			season = m.group(2);
		}
		
		Element matchTable = doc.select("#site > div.white > div.content > div > div.box > div > table > tbody").get(0);
		String day = "NA";
		String month = "NA";
		String year = "NA";
		for (Node match : matchTable.childNodes()) {
			m = Pattern.compile("/matches_today/(.*)/\"").matcher(match.outerHtml());
			
			
			if(m.find()){
				date = m.group(1);
				try{
				day = date.split("/")[2];
				month = date.split("/")[1];
				year = date.split("/")[0];
				}
				catch(Exception e){
					//Invalid date
				}
			}
			if(match.outerHtml().contains("pso") && !match.outerHtml().contains("a href=\"/report/") ){
				homeTeam = match.childNode(5).childNode(0).attr("title");
				awayTeam = match.childNode(9).childNode(0).attr("title");
				
//				System.out.println(season + "," + date);
				matchesWithNoPage.add(competition + "," + season + "," + day + "," + month + "," + year + "," + homeTeam + "," + awayTeam );
			}

		}
	}

	public List<String> parseURL(String url) throws IOException{
		return parse(Jsoup.connect(url).get());
	}

	public List<String> parseFile(String fileName) throws IOException{
		File input = new File(fileName);
		return parse(Jsoup.parse(input, "UTF-8", "http://example.com/"));
	}

	public List<String> parseURI(String uri) throws IOException{
		return parse(webPageCache.get(uri));
	}
	public static void listMatchesWithNoPage(){
		for (String string : matchesWithNoPage) {
			System.out.println(string);
		}
		System.out.println("Total : " + matchesWithNoPage.size());
	}

	public static void main(String[] args) throws IOException {
		WeltSchedulePenaltyParser schedulePenaltyParser = new WeltSchedulePenaltyParser();
		System.out.println(
				//		ScheduleParser.parseFile("/home/aditya/Research Data/weltfussball/index.html")
				schedulePenaltyParser.parseURI("eng-fa-cup-1994-1995")
				);
		
		System.out.println(
				//		ScheduleParser.parseFile("/home/aditya/Research Data/weltfussball/index.html")
				schedulePenaltyParser.parseURI("eng-league-cup-1998-1999")
				);
		
		WeltSchedulePenaltyParser.listMatchesWithNoPage();
	}


}

