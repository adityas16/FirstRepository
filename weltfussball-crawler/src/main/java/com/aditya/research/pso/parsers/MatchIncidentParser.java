
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

public class MatchIncidentParser implements Parser{
	// FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/games/", Constants.worldfootballURL + Constants.report + "/");
	DBCache webPageCache = DBCache.weltgameCache();
	GameParser gp = new GameParser();
	MatchGoalParser mgp = new MatchGoalParser();

	public List<Map<String,String>> parse(Document doc){
		//		doc.title().split(regex)
		List<Map<String,String>> incidents = new ArrayList<Map<String,String>>();
		//TODO: Rectify this error. The home table is #3 only when there are incidents. Else, it is number 2.
		Element homeTeamTable = doc.getElementsByClass("standard_tabelle").get(3);

		//Add scored penalties
		List<Map<String,String>> goals = mgp.parse(doc);
		for (Map<String, String> goal : goals) {
			if(!goal.get("isPenalty").equals("1")){
				continue;
			}
			Map<String,String> event = new HashMap<String, String>();
			event.put("shooter", goal.get("scorer"));
			event.put("is_home", Utils.asInt(homeTeamTable.outerHtml().contains(goal.get("scorer")))+"");
			event.put("is_miss", "0");
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
			Map<String,String> event = new HashMap<String, String>();
			Node incidentRow = incidentRows.childNode(i);
			String misser = incidentRow.childNode(1).childNode(0).attr("href").split("/")[2].replace('/', ' ').trim();
			event.put("shooter", misser);
			event.put("is_home", Utils.asInt(homeTeamTable.outerHtml().contains(misser))+"");
			event.put("is_miss", "1");
			event.putAll(gp.parse(doc).get(0));
			incidents.add(event);
		}

		return incidents;
	}
	public  List<Map<String,String>> parseURI(String uri) throws IOException{
		return parse(webPageCache.get(uri));
	}

	public List<Map<String,String>> parseFile(String filename) throws IOException{
		File input = new File(filename);
		return parse(Jsoup.parse(input, "UTF-8", "http://example.com/"));
	}

	public static void main(String[] args) throws IOException {
		MatchIncidentParser mep = new MatchIncidentParser();
		
		mep.parseURI("premier-league-2007-2008-wigan-athletic-fulham-fc");
		//error
		System.out.println(mep.parseURI("a-junioren-bundesliga-west-2004-2005-rot-weiss-essen-fortuna-duesseldorf"));

		//2 penalties
		System.out.println(mep.parseURI("premier-league-2015-2016-west-bromwich-albion-watford-fc"));

		System.out.println(mep.parseURI("premier-league-2016-2017-arsenal-fc-liverpool-fc"));
		mep.parseURI("europa-league-1961-1962-1-runde-hibernian-fc-os-belenenses");
	}
}
