package com.aditya.research.pso.parsers;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
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
//		extractSquad(doc);
		List<Map<String,String>> goals = new ArrayList<Map<String,String>>();
		
		int j;
		for(j=0;j<=10;j++) {
			try {
				Utils.extractIntFromString(doc.getElementsByClass("standard_tabelle").get(j).childNode(1).childNode(3).childNode(3).childNode(2).toString().trim());
				break;
			}
			catch(Exception e) {
				continue;
			}
		}
		if(j>10){
			return goals;
		}
		
		Element goalsTable = doc.getElementsByClass("standard_tabelle").get(j);
		
		Node goalRows = goalsTable.childNode(1);
		
		
		int homeScore=0,awayScore=0;
		for(int i=3;i<goalRows.childNodeSize();i=i+2){
			Map<String,String> event = new HashMap<String, String>();
			Node goalRow = goalRows.childNode(i);
			Pattern p = Pattern.compile("(\\d+) : (\\d+)");
			Matcher m = p.matcher(goalRow.childNode(1).childNode(1).childNode(0).toString());
			if (m.find()) {
				event.put("homeScore",m.group(1));
				event.put("awayScore",m.group(2));
			}
			else {
				event.put("homeScore","NA");
				event.put("awayScore", "NA");
			}
			event.put("scorer",goalRow.childNode(3).childNode(1).attr("href").split("/")[2].replace('/', ' ').trim());
			if(!goalRow.childNode(3).hasAttr("style")) {
				homeScore++;
				event.put("isHomeGoal", 1 + "");
			}
			else {
				awayScore++;
				event.put("isHomeGoal", 0 + "");
			}
			
			event.put("time", Utils.extractIntFromString(goalRow.childNode(3).childNode(2).toString().trim()) + "");
			event.put("isPenalty", Utils.asInt(goalRow.childNode(3).childNode(2).toString().contains("enalty")) + "");
			goals.add(event);
			if(!event.get("homeScore").equals("NA") && (homeScore!=Integer.parseInt(event.get("homeScore")) || awayScore!=Integer.parseInt(event.get("awayScore")))) {
				throw new InvalidParameterException("Computed and parsed score doesnt match");
			}
		}
		
		return goals;
	}
	private void extractSquad(Document doc) {
		Node squadTable = doc.getElementsByClass("standard_tabelle").get(3).childNode(1);
		for(int i=1;i<squadTable.childNodeSize();i=i+2){
			if(squadTable.childNode(i).outerHtml().contains("Substitutes")){
				continue;
			}
//			System.out.println(squadTable.childNode(i).childNode(3).childNode(1).attr("href").split("/")[2].replace('/', ' ').trim());
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
		System.out.println(mep.parseURI("primera-division-2012-2013-rayo-vallecano-celta-vigo"));
		//own goal
		System.out.println(mep.parseURI("premier-league-2015-2016-manchester-united-tottenham-hotspur"));
		System.out.println(mep.parseURI("wm-2010-in-suedafrika-gruppe-a-uruguay-frankreich"));
		System.out.println(mep.parseURI("europa-league-1961-1962-1-runde-hibernian-fc-os-belenenses"));
		//goal film with score
		System.out.println(mep.parseURI("bundesliga-2008-2009-hannover-96-karlsruher-sc"));
		System.out.println(mep.parseURI("bundesliga-2009-2010-rapid-wien-sturm-graz_2"));
		
	}
}
