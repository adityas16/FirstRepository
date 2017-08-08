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
		int currentHomeScore = 0;
		int currentAwayScore = 0;
		extractSquad(doc);
		List<Map<String,String>> goals = new ArrayList<Map<String,String>>();

		Element goalsTable = doc.getElementsByClass("standard_tabelle").get(1);

		Node goalRows = goalsTable.childNode(1);

		if(goalRows.childNodeSize() <3){
			return goals;
		}

		for(int i=3;i<goalRows.childNodeSize();i=i+2){
			Map<String,String> event = new HashMap<String, String>();
			Node goalRow = goalRows.childNode(i);
			if(goalRow.outerHtml().contains("<b>none</b>")){
				continue;
			}
			String scoreStrings[] = goalRow.childNode(1).childNode(1).childNode(0).toString().split(":");


			event.put("scorer",goalRow.childNode(3).childNode(1).attr("href").split("/")[2].replace('/', ' ').trim());
			event.put("time", Utils.extractIntFromString(goalRow.childNode(3).childNode(2).toString().trim()) + "");
			event.put("isPenalty", Utils.asInt(goalRow.childNode(3).childNode(2).toString().contains("enalty")) + "");
			String scorer = event.get("scorer");
			boolean isHomeGoal = false;
			if(MatchIncidentParser.getAwayTeamTable(doc).outerHtml().contains(scorer)){
				isHomeGoal = false;
			}
			else if(MatchIncidentParser.getHomeTeamTable(doc).outerHtml().contains(scorer)){
				isHomeGoal = true;
			}
			else{
				throw new RuntimeException("Could not find which side scored");
			}
			
			event.put("is_home", Utils.asInt(isHomeGoal)+"");
			event.put("current_home_score",currentHomeScore + "");
			event.put("current_away_score",currentAwayScore + "");
			if(goalRow.outerHtml().contains("own goal")){
				isHomeGoal = !isHomeGoal;
			}
			//update score
			if(isHomeGoal){
				currentHomeScore++;
			}
			else{
				currentAwayScore++;
			}
			if(currentHomeScore != Integer.parseInt(scoreStrings[0].trim())){
//				System.out.println("Error in score computation");
			}
			try{
				event.put("current_home_score",Integer.parseInt(scoreStrings[0].trim()) - Utils.asInt(isHomeGoal)+ "");
				event.put("current_away_score",Integer.parseInt(scoreStrings[1].trim())- Utils.asInt(!isHomeGoal) + "");
				}
				catch(NumberFormatException n){
					event.put("current_home_score",-1 + "");
					event.put("current_away_score",-1 + "");
				}
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
			//			System.out.println(squadTable.childNode(i).childNode(3).childNode(1).attr("href").split("/")[2].replace('/', ' ').trim());
		}
	}

	public  List<Map<String,String>> parseURI(String uri) throws IOException{
		return parse(webPageCache.get(uri));
	}

	public List<Map<String,String>> parseFile(String filename) throws IOException{
		File input = new File(filename);
		return parse(Jsoup.parse(input, "UTF-8", "http://example.com/"));
	}

	public static void main(String[] args) throws IOException {
		MatchGoalParser mep = new MatchGoalParser();
		//No goals
		mep.parseURI("a-grupa-2009-2010-botev-plovdiv-cherno-more-varna");
		mep.parseURI("a-junioren-bundesliga-nord-nordost-2005-2006-hamburger-sv-sc-borea-dresden");
		
		mep.parseURI("europa-league-1961-1962-1-runde-hibernian-fc-os-belenenses");
		mep.parseURI("a-grupa-2008-2009-botev-plovdiv-ofc-sliven");
	}
}
