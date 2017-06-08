package pso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aditya.research.pso.parsers.ISeasonParser;


public class WeltSeasonParser implements ISeasonParser{
//	FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/competitions/", Constants.baseURL + Constants.all_matches + "/");
	DBCache webPageCache = DBCache.weltseasonCache();
	
	static int errors = 0;
	public List<String> parse(String uri) throws IOException{
		Document doc = webPageCache.get(uri);
		return parse(doc);
	}
	@Override
	public List<String> parse(Document doc) throws IOException{
		Elements elements = doc.getElementsMatchingText(Constants.all_matches);
		//		doc.getElementsByAttributeValue("name", "saison").get(0).chi.attr("value")
		List<String> urls = new ArrayList<String>();
		for (Element element : doc.getElementsByAttributeValue("name", "saison").get(0).children()) {
			urls.add(element.attr("value").replace("/" + Constants.all_matches +"/", "").replace('/',' ').trim());
		}
		return urls;
	}

}
