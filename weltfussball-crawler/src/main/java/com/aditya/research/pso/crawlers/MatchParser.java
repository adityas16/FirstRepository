//package com.aditya.research.pso.crawlers;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.nodes.Node;
//import org.jsoup.select.Elements;
//
//import com.aditya.research.pso.etl.StringUtils;
//import com.aditya.research.pso.vo.Match;
//
//import pso.Constants;
//import pso.DataValiditor;
//import pso.EventParser;
//import pso.Shot;
//import pso.FileSystemCache;
//
//public class MatchParser {
//	static FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/games/", Constants.worldfootballURL + Constants.report + "/");
//	
//	public static Match parse(Document doc){
//		Match m = new Match();
//		m.gameName = doc.title().trim();
//		m.competition = doc.getElementsByAttributeValue("name", "page-topic").get(0).attr("content").trim();
//		m.year = getCompetitionYear(m.competition);
//		m.competition = getCompetitionName(m.competition);
//		m.stage = getStageName(m.gameName);
////		String date = doc.getElementsByClass("box").get(0).getAllElements().get(7).childNode(0).outerHtml().trim();
//		
//		try{
//		Element goalsTable = doc.getElementsByClass("standard_tabelle").get(1);
//		Node goalRows = goalsTable.childNode(1);
//		Node lastGoal = goalRows.childNode(goalRows.childNodeSize() - 2).childNode(3);
//		m.goalScoredInRegularPlay = true;
//		m.homeScoredLastFieldGoal =  lastGoal.attr("class").equals("hell");
//		m.timeOfLastGoal = StringUtils.extractInt(lastGoal.childNode(2).toString());
//		}
//		catch(Exception e){
//			m.goalScoredInRegularPlay = false;
//		}
//		
//		List<Shot> shots = new ArrayList<Shot>();
//		
//		Elements hellElements = doc.getElementsByClass("standard_tabelle").get(2).getElementsByClass("hell");
//		Elements dunkelElements = doc.getElementsByClass("standard_tabelle").get(2).getElementsByClass("dunkel");
//		
//		m.homeShotFirst = false;
//		if(hellElements.get(1).attributes().get("style").equals("")){
//			m.homeShotFirst = true;
//		}
//		return m;
//	}
//	private static String getCompetitionName(String text){
//		int i=0;
//		for(i=0;i<text.length();i++){
//			if(Character.isDigit(text.charAt(i))){
//				break;
//			}
//		}
//		return text.substring(0,i);
//	}
//	
//	private static String getStageName(String text){
//		return text.split(",")[1].replace(')', ' ').trim();
//	}
//	
//	private static int getCompetitionYear(String str){
//		Pattern pattern = Pattern.compile("\\w+([0-9]+)\\w+");
//		Matcher matcher = pattern.matcher(str);
//		matcher.find();
//		return Integer.parseInt(matcher.group());
//	}
//	
//	public static Match parseURI(String uri) throws IOException{
//		return parse(webPageCache.get(uri));
//	}
//	
//	public static Match parseFile(String filename) throws IOException{
//		File input = new File(filename);
//		return parse(Jsoup.parse(input, "UTF-8", "http://example.com/"));
//	}
//	
//	public static void main(String[] args) throws IOException {
//		
//		Match m = MatchParser.parseFile(Constants.weltFolder + "games/afc-champions-league-2009-achtelfinale-kashima-antlers-fc-seoul.html");
//	}
//}
