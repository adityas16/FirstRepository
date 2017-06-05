package pso;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class SchedulePenaltyParser {
//	static FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/seasons/", Constants.baseURL + Constants.all_matches + "/");
	DBCache webPageCache = DBCache.weltseasonCache();
	
	public  List<String> parse(Document doc) throws IOException{
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
//		ScheduleParser.parseFile("/home/aditya/Research Data/weltfussball/index.html")
		schedulePenaltyParser.parseURI("wm-2014-in-brasilien")
		);
	}
}

