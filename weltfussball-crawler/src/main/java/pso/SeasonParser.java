package pso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class SeasonParser {
//	FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/competitions/", Constants.baseURL + Constants.all_matches + "/");
	DBCache webPageCache = DBCache.weltseasonCache();
	
	static int errors = 0;
	public List<String> parse(String uri) throws IOException{
		Document doc = webPageCache.get(uri);
		Elements elements = doc.getElementsMatchingText(Constants.all_matches);
		//		doc.getElementsByAttributeValue("name", "saison").get(0).chi.attr("value")
		List<String> urls = new ArrayList<String>();
		for (Element element : doc.getElementsByAttributeValue("name", "saison").get(0).children()) {
			urls.add(element.attr("value").replace("/" + Constants.all_matches +"/", "").replace('/',' ').trim());
		}
		return urls;
	}

//	private List<Shot> processCompetition(String uri) throws IOException{
//		List<String> seasonURIs = parse(uri);
//
//		System.out.println(seasonURIs);
//		
//		List<Shot> shots = new ArrayList<Shot>();
//		for (String seasonURI : seasonURIs) {
//			processSeason(shots, seasonURI);
//		}
//		return shots;
//	}
//
//	private static void processSeason(List<Shot> shots, String seasonURI) throws IOException {
//		List<String> gameURIs = SchedulePenaltyParser.parseURI(seasonURI);
//		for (String gameURI : gameURIs) {
//			try{
//				shots.addAll(EventParser.parseURI(gameURI));
//			}
//			catch(Exception e){
////					e.printStackTrace();
//				System.out.println(gameURI);
//				errors ++;
//			}
//		}
//	}

//	public static void main(String[] args) throws IOException {
////		crawlCompetitions();
//		crawlSeasons();
//	}

//	private static void crawlSeasons() throws IOException {
//		List<String> competitionURIsList = Files.readAllLines(Paths.get(Constants.weltFolder + "season_pages"));
//		//		List<String> seasonURIs = SeasonParser.parse("wm-2014-in-brasilien");
//		Set<String> seasonURIs = new HashSet<String>(competitionURIsList);
//		List<Shot> shots = new ArrayList<Shot>();
//		
//		int count = 0;
//		for (String seasonURI : seasonURIs) {
//			try{
//				processSeason(shots,seasonURI);
//				System.out.println(seasonURI);
//			}
//			catch(Exception e){
//				System.out.println("failed" + seasonURI);
//			}
//			if(count ++ > 1000){
//				break;
//			}
//		}
//		
//		FileUtils.writeToCSV(Constants.weltFolder + "PSO.csv", shots);
//		
//		System.err.println(errors);
//	}

//	private void crawlCompetitions() throws IOException {
//		List<String> competitionURIsList = Files.readAllLines(Paths.get(Constants.weltFolder + "competition_pages"));
//		//		List<String> seasonURIs = SeasonParser.parse("wm-2014-in-brasilien");
//		Set<String> competitionURIs = new HashSet<String>(competitionURIsList);
//		List<Shot> shots = new ArrayList<Shot>();
//		
//		for (String competitionURI : competitionURIs) {
//			shots.addAll(processCompetition(competitionURI));
//		}
//		
//		FileUtils.writeToCSV(Constants.weltFolder + "PSO.csv", shots);
//		
//		System.err.println(errors);
//	}


}
