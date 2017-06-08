package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.util.Arrays;
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

import com.aditya.research.pso.etl.RoundNameParser;
import com.aditya.research.pso.etl.StringUtils;

import pso.DBCache;
import pso.Shot;

public class GameParser implements Parser {

	public List<Map<String, String>> parse(Document doc) {
		Map<String, String> asMap = new LinkedHashMap<String, String>();
		extractTitleInfo(doc.title(), asMap);

		String date = doc.select("#site > div.white > div.content > div > div.box > div > table:nth-child(1) > tbody > tr:nth-child(1) > th:nth-child(2)").get(0).childNode(0).toString();
		extractDate(asMap, date);

		extractHomeKeeper(doc, asMap,5);
		extractAwayKeeper(doc, asMap,5);

		asMap.put("season_uri", doc.select("#site > div.white > div.content > div > div.box2.borderless > div > table > tbody > tr > td:nth-child(1) > form > select > option:nth-child(1)").get(0).attr("value").split("/")[2]);

		return Arrays.asList(asMap);
	}

	public static void initAllFields(Map<String, String> asMap){
		asMap.put("homeTeam", "NA");
		asMap.put("awayTeam", "NA");
		asMap.put("homeScore", "NA");
		asMap.put("awayScore", "NA");
		asMap.put("competition", "NA");
		asMap.put("season", "NA");
		asMap.put("round", "NA");
		asMap.put("dist_from_final","NA");
		asMap.put("day", "NA");
		asMap.put("month", "NA");
		asMap.put("year", "NA");
		asMap.put("home_keeper","NA");
		asMap.put("away_keeper","NA");
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
			asMap.put("homeTeam", m.group(1));
			asMap.put("awayTeam", m.group(2));
			asMap.put("homeScore", m.group(3));
			asMap.put("awayScore", m.group(4));
			asMap.put("competition", m.group(5));
			asMap.put("season", m.group(6));
			asMap.put("round", m.group(8));
			asMap.put("dist_from_final",RoundNameParser.findDistanceFromFinal(m.group(8)) + "");
		}
	}

	public static void main(String[] args) throws IOException {
		GameParser mp = new GameParser();
		//Download from site
		System.out.println(mp.parse(Jsoup.connect("http://www.worldfootball.net/report/" + "afrika-cup-1992-im-senegal-finale-elfenbeinkueste-ghana")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
	               .referrer("http://www.google.com") 
	               .timeout(10000)               
	               .get()));
		
		//Read from cache
		mp.parse(DBCache.weltgameCache().get("afrika-cup-1992-im-senegal-finale-elfenbeinkueste-ghana"));
	}
}
