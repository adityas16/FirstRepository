package com.aditya.research.pso.parsers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.aditya.research.pso.etl.StringUtils;
import com.aditya.research.pso.vo.Match;

import pso.Constants;
import pso.DBCache;
import pso.FileSystemCache;
import pso.Shot;

public class MatchGoalParser implements Parser{
// FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/games/", Constants.worldfootballURL + Constants.report + "/");
 DBCache webPageCache = DBCache.weltgameCache();
 
	public List<Map<String,String>> parse(Document doc){
//		doc.title().split(regex)
		extractSquad(doc);
		List<Map<String,String>> goals = new ArrayList<Map<String,String>>();
		
		Element goalsTable = doc.getElementsByClass("standard_tabelle").get(1);

		Node goalRows = goalsTable.childNode(1);
		
		if(goalRows.childNodeSize() == 5){
			return goals;
		}
		
		for(int i=3;i<goalRows.childNodeSize();i=i+2){
			Map<String,String> event = new HashMap<String, String>();
			Node goalRow = goalRows.childNode(i);
			String scoreStrings[] = goalRow.childNode(1).childNode(1).childNode(0).toString().split(":");
			try{
				event.put("homeScore",Integer.parseInt(scoreStrings[0].trim()) + "");
				event.put("awayScore",Integer.parseInt(scoreStrings[1].trim()) + "");
				}
				catch(NumberFormatException n){
					event.put("homeScore",-1 + "");
					event.put("awayScore",-1 + "");
				}
			event.put("scorer",goalRow.childNode(3).childNode(1).attr("href").split("/")[2].replace('/', ' ').trim());
			event.put("time", Utils.extractIntFromString(goalRow.childNode(3).childNode(2).toString().trim()) + "");
			event.put("isPenalty", Utils.asInt(goalRow.childNode(3).childNode(2).toString().contains("enalty")) + "");
			goals.add(event);
		}
		return goals;
	}
	private void extractSquad(Document doc) {
		Node squadTable = doc.getElementsByClass("standard_tabelle").get(3).childNode(1);
		for(int i=1;i<squadTable.childNodeSize();i=i+2){
			if(squadTable.childNode(i).outerHtml().contains("Substitutes")){
				continue;
			}
			System.out.println(squadTable.childNode(i).childNode(3).childNode(1).attr("href").split("/")[2].replace('/', ' ').trim());
		}
	}
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
	
	public  List<Map<String,String>> parseURI(String uri) throws IOException{
		return parse(webPageCache.get(uri));
	}
	
	public List<Map<String,String>> parseFile(String filename) throws IOException{
		File input = new File(filename);
		return parse(Jsoup.parse(input, "UTF-8", "http://example.com/"));
	}
	
	public static void main(String[] args) throws IOException {
		MatchGoalParser mep = new MatchGoalParser();
		mep.parseURI("wm-2010-in-suedafrika-gruppe-a-uruguay-frankreich");
		mep.parseURI("europa-league-1961-1962-1-runde-hibernian-fc-os-belenenses");
	}
}
