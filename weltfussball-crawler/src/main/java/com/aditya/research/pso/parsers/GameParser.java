package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import com.aditya.research.pso.etl.RoundNameParser;
import com.aditya.research.pso.etl.StringUtils;

import pso.DBCache;

public class GameParser implements Parser {
	MatchGoalParser mgp = new MatchGoalParser();
	public List<Map<String, String>> parse(Document doc) {
		Map<String, String> asMap = new LinkedHashMap<String, String>();
		extractTitleInfo(doc.title(), asMap);
		
		extractCompetitionID(doc,asMap);
		
		String date = doc.select("#site > div.white > div.content > div > div.box > div > table:nth-child(1) > tbody > tr:nth-child(1) > th:nth-child(2)").get(0).childNode(0).toString();
		extractDate(asMap, date);

		extractHomeKeeper(doc, asMap,5);
		extractAwayKeeper(doc, asMap,5);
		
		asMap.put("season_uri", doc.select("#site > div.white > div.content > div > div.box2.borderless > div > table > tbody > tr > td:nth-child(1) > form > select > option:nth-child(1)").get(0).attr("value").split("/")[2]);
		
		validateScore(doc, asMap);
		
		return Arrays.asList(asMap);
	}

	private void validateScore(Document doc, Map<String, String> asMap) {
		//No need to validate if this is a shootout
		if(doc.outerHtml().contains("<td colspan=\"2\" class=\"ueberschrift\" align=\"center\">Penalty shootout")) {
			return;
		}
		
		int homeScoreComputed=0,awayScoreComputed=0;
		List<Map<String, String>> goals = mgp.parse(doc);
		if(goals.size()!=0) {
			homeScoreComputed = Integer.parseInt(goals.get(goals.size()-1).get("homeScore"));
			awayScoreComputed = Integer.parseInt(goals.get(goals.size()-1).get("awayScore"));
		}
		
		if(!(homeScoreComputed == Integer.parseInt(asMap.get("homeScore"))) || !(awayScoreComputed == Integer.parseInt(asMap.get("awayScore")))){
//			throw new InvalidParameterException("Computed and parsed scores donot match");
		}
	}
	
	private void extractCompetitionID(Document doc, Map<String, String> asMap) {
		asMap.put("competition_ID","NA");
		try{
			//Picks competition ID from the Overview link on the top panel
			asMap.put("competition_ID", StringUtils.extractResourceURI(doc.select("#navi > div.sitenavi > div > div > ul:nth-child(1) > li > a").get(0).attr("href")));
		}
		catch(Exception e){
			
		}
	}
	
	private void extractHomeKeeper(Document doc, Map<String, String> asMap,int i) {
		asMap.put("home_keeper","NA");
		if(i>13){
			System.out.println("No home keeper info: " + doc.title());
			return;
		}
		try{
		asMap.put("home_keeper", StringUtils.extractResourceURI(doc.select("#site > div.white > div.content > div > div.box > div > table:nth-child("+i+") > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(1) > td:nth-child(2) > a").get(0).attr("href").toString()));
		}
		catch(Exception e){
			extractHomeKeeper(doc, asMap, i +1);
		}
	}
	private void extractAwayKeeper(Document doc, Map<String, String> asMap,int i) {
		asMap.put("away_keeper","NA");
		if(i>13){
			System.out.println("No away keeper info: " + doc.title());
			return;
		}
		try{
		asMap.put("away_keeper", StringUtils.extractResourceURI(doc.select("#site > div.white > div.content > div > div.box > div > table:nth-child("+i+") > tbody > tr > td.box_zelle > table > tbody > tr:nth-child(1) > td:nth-child(2) > a").get(0).attr("href").toString()));
		}
		catch(Exception e){
			extractAwayKeeper(doc, asMap, i +1);
		}
	}

	private void extractDate(Map<String, String> asMap, String date) {
		Pattern p2 = Pattern.compile(".*, (\\d+). ([a-zA-Z]*) (\\d+)");
		Matcher m2 = p2.matcher(date);
		if (m2.find()) {
		    asMap.put("day", m2.group(1));
		    asMap.put("month", StringUtils.extractMonthNumberFromString(m2.group(2)) + "");
		    asMap.put("year", m2.group(3));
		}
	}

	private void extractTitleInfo(String title, Map<String, String> asMap) {
		Pattern p = Pattern.compile("(.*) - (.*) (\\d+):(\\d+) \\((.*) (\\d+)(.*), (.*)\\)");
		Matcher m = p.matcher(title);
		if (m.find()) {
			asMap.put("homeTeam", StringUtils.toSimpleCharset(m.group(1)));
			asMap.put("awayTeam", StringUtils.toSimpleCharset(m.group(2)));
			asMap.put("homeScore", m.group(3));
			asMap.put("awayScore", m.group(4));
			asMap.put("competition", StringUtils.toSimpleCharset(m.group(5)));
			asMap.put("season", m.group(6));
			asMap.put("round", m.group(8));
			asMap.put("dist_from_final",RoundNameParser.findDistanceFromFinal(m.group(8)) + "");
		}
	}
	
	public static void main(String[] args) throws IOException {

		GameParser mp = new GameParser();
		System.out.println(mp.parse(DBCache.weltgameCache().get("europa-league-qual-2016-2017-2-runde-zaglebie-lubin-partizan")));
		System.out.println(mp.parse(DBCache.weltgameCache().get("afrika-cup-1992-im-senegal-finale-elfenbeinkueste-ghana")));
		//score and goals mismatch
		mp.parse(DBCache.weltgameCache().get("a-grupa-2009-2010-minyor-pernik-botev-plovdiv"));
		
		
		
		Pattern p = Pattern.compile("(.*) - (.*) (\\d+):(\\d+) \\((.*) (\\d+)(.*), (.*)\\)");
		Matcher m = p.matcher("Ivory Coast - Ghana 11:10 (Africa Cup 1992 Senegal, Final)");
		if (m.find()) {
		    System.out.println(m.group(1));
		    System.out.println(m.group(2));
		    System.out.println(m.group(3));
		    System.out.println(m.group(4));
		    System.out.println(m.group(5));
		    System.out.println(m.group(6));
		    System.out.println(m.group(8));
		}
		
		Pattern p2 = Pattern.compile(".*, (\\d+). ([a-zA-Z]*) (\\d+)");
		Matcher m2 = p2.matcher("Sunday, 26. January 1992");
		if (m2.find()) {
		    System.out.println(m2.group(1));
		    System.out.println(m2.group(2));
		    System.out.println(m2.group(3));
		}
	}
	
}
