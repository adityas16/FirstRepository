package pso;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


public class SchedulePenaltyParser {
//	static FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/seasons/", Constants.baseURL + Constants.all_matches + "/");
	DBCache webPageCache = DBCache.weltseasonCache();
	
	public  List<String> parse(Document doc) throws IOException{
		Node table = doc.getElementsByAttributeValue("class", "portfolio").get(0).getElementsByAttributeValue("class", "box").get(0).getElementsByClass("standard_tabelle").get(0).childNode(1);
		
		Iterator<Node> rowIterator = table.childNodes().iterator();
		String currentRound="NA";
		String latestYear="NA",latestMonth="NA",latestDay="NA";
		while (rowIterator.hasNext()) {
			rowIterator.next();
			Node row = rowIterator.next();
			Matcher m = Pattern.compile("/schedule/.*\">(.*)</a").matcher(row.outerHtml());
			if(m.find()) {
				currentRound = m.group(1);
			}
			 m = Pattern.compile("title=\"further matches on (\\d+)/(\\d+)/(\\d+)\"").matcher(row.outerHtml());
			if(m.find()) {
					latestDay = m.group(1);
					latestMonth = m.group(1);
					latestYear = m.group(1);
			}
			if(row.outerHtml().contains("pso")) {
				System.out.println(row);
			}
		}
		//Parse shootouts with link
		List<String> urls = new ArrayList<String>();
		Elements elements = doc.getElementsMatchingText("pso");
		for (Element element : elements) {
			if(!element.attr("href").equals("")){
				urls.add(element.attr("href").replace("/"+ Constants.report +"/", "").replace('/',' ').trim());
			}
		}
		return urls;
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
	
	public static void main(String[] args) throws IOException {
		SchedulePenaltyParser schedulePenaltyParser = new SchedulePenaltyParser();
		System.out.println(
//				ScheduleParser.parseFile("/home/aditya/Research Data/weltfussball/index.html")
				schedulePenaltyParser.parseURI("afrika-cup-1980-in-nigeria")
				);
		System.out.println(
//				ScheduleParser.parseFile("/home/aditya/Research Data/weltfussball/index.html")
				schedulePenaltyParser.parseURI("wm-2014-in-brasilien")
				);
	}
}

