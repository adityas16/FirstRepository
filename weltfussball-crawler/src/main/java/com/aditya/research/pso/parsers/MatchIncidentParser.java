
package com.aditya.research.pso.parsers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

import pso.DBCache;

public class MatchIncidentParser implements Parser{
	// FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/games/", Constants.worldfootballURL + Constants.report + "/");
	DBCache webPageCache = DBCache.weltgameCache();
	GameParser gp = new GameParser();
	MatchGoalParser mgp = new MatchGoalParser();

	public List<Map<String,String>> parse(Document doc){
		//		doc.title().split(regex)
		List<Map<String,String>> incidents = new ArrayList<Map<String,String>>();
		//TODO: Rectify this error. The home table is #3 only when there are incidents. Else, it is number 2.
		Element awayTeamTable = getAwayTeamTable(doc);
		Element homeTeamTable = getHomeTeamTable(doc);

		//Add scored penalties
		List<Map<String,String>> goals = mgp.parse(doc);
		for (Map<String, String> goal : goals) {
			if(!goal.get("isPenalty").equals("1")){
				continue;
			}
			Map<String,String> event = new LinkedHashMap<String, String>();
			event.put("shooter", goal.get("scorer"));
			event.put("is_home", goal.get("is_home"));
			event.put("is_miss", "0");
			event.put("time", goal.get("time"));
			event.put("current_home_score",goal.get("current_home_score") + "");
			event.put("current_away_score",goal.get("current_away_score") + "");
			event.putAll(gp.parse(doc).get(0));
			incidents.add(event);
		}

		Element incidentsTable = doc.getElementsByClass("standard_tabelle").get(2);

		if(!incidentsTable.outerHtml().contains("Incidents")){
			return incidents;
		}

		Node incidentRows = incidentsTable.childNode(1);
		//Missed penalties
		for(int i=3;i<incidentRows.childNodeSize();i=i+2){
			Map<String,String> event = new LinkedHashMap<String, String>();
			Node incidentRow = incidentRows.childNode(i);
			
			//Check if this is a penalty miss event
			Matcher m = Pattern.compile("misses a penalty kick \\((\\d+)'\\)").matcher(incidentRow.outerHtml());
			int time;
			if(!m.find()){
				continue;
			}
			else{
				time = Integer.parseInt(m.group(1));
			}
			
			String misser = incidentRow.childNode(1).childNode(0).attr("href").split("/")[2].replace('/', ' ').trim();
			event.put("shooter", misser);
			if(awayTeamTable.outerHtml().contains(misser)){
				event.put("is_home", Utils.asInt(false) + "");
			}
			else if(homeTeamTable.outerHtml().contains(misser)){
				event.put("is_home", Utils.asInt(true) + "");
			}
			else{
				event.put("is_home", "NA");
			}
			event.put("is_miss", "1");
			event.put("time", time + "");
			event.putAll(getScoreAtTime(goals,time));
			event.putAll(gp.parse(doc).get(0));
			incidents.add(event);
		}

		return incidents;
	}
	public  List<Map<String,String>> parseURI(String uri) throws IOException{
		return parse(webPageCache.get(uri));
	}
	
	private Map<String,String> getScoreAtTime(List<Map<String,String>> goals,int time){
		Map<String, String> currentScore = new HashMap<>();
		currentScore.put("current_home_score", "0");
		currentScore.put("current_away_score", "0");
		for (Map<String, String> goal : goals) {
			if(Integer.parseInt(goal.get("time")) < time){
				int currentHomeScore = Integer.parseInt(goal.get("current_home_score"));
				int currentAwayScore = Integer.parseInt(goal.get("current_away_score"));
				boolean isHomeGoal = Integer.parseInt(goal.get("is_home"))==1;
				currentScore.put("current_home_score", currentHomeScore + Utils.asInt(isHomeGoal) + "");
				currentScore.put("current_away_score", currentAwayScore + Utils.asInt(!isHomeGoal) + "");
			}
		}
		return currentScore;
	}

	public List<Map<String,String>> parseFile(String filename) throws IOException{
		File input = new File(filename);
		return parse(Jsoup.parse(input, "UTF-8", "http://example.com/"));
	}
	public static Element getAwayTeamTable(Document doc){
		 Elements allTables = doc.getElementsByClass("box_zelle");
		 return (Element) allTables.get(0).childNode(1).childNode(1);
	}
	
	public static Element getHomeTeamTable(Document doc){
		 Elements allTables = doc.select("td[width=50%][valign=top]");
		 return (Element) allTables.get(0).childNode(1).childNode(1);
	}

	public static void main(String[] args) throws IOException {
		MatchIncidentParser mep = new MatchIncidentParser();

//		Utils.printRecords(mep.parseURI("premier-league-2007-2008-wigan-athletic-fulham-fc"));

		//2 penalties
		Utils.printRecords(mep.parseURI("premier-league-2015-2016-west-bromwich-albion-watford-fc"));

		Utils.printRecords(mep.parseURI("premier-league-2016-2017-arsenal-fc-liverpool-fc"));
		Utils.printRecords(mep.parseURI("europa-league-1961-1962-1-runde-hibernian-fc-os-belenenses"));
		//Non penalty miss incident
		Utils.printRecords(mep.parseURI("a-grupa-2015-2016-pfc-litex-lovech-pfc-ludogorets-razgrad"));
		//Away penalty miss
		Utils.printRecords(mep.parseURI("premier-league-2015-2016-west-ham-united-watford-fc"));
		
		//No Penalties
		Utils.printRecords(mep.parseURI("a-junioren-bundesliga-nord-nordost-2005-2006-hamburger-sv-hannover-96"));

		//error
//		System.out.println(mep.parseURI("a-junioren-bundesliga-west-2004-2005-rot-weiss-essen-fortuna-duesseldorf"));

	}
}
