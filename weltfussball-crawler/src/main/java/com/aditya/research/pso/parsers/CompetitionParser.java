package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import pso.DBCache;
import pso.SeasonParser;

public class CompetitionParser implements Parser{
	DBCache webPageCache = DBCache.weltCompetitionCache();
	SeasonParser sp = new SeasonParser();
	
	@Override
	public List<Map<String, String>> parse(Document doc) {
		List<Map<String, String>> records  = new ArrayList<Map<String,String>>();
		Map<String,String> asMap =new LinkedHashMap<String,String>();
		asMap.put("naem", "NA");
		
		return null;
	}
	
	public List<String> getSeasons(String uri) throws IOException {
		Document doc = webPageCache.get(uri);
		Matcher m = Pattern.compile("/all_matches/([^/]*)/").matcher(doc.select("#navi > div.sitenavi > div.navibox2").outerHtml());
		String latestSeason="";
		if(m.find()) {
			latestSeason = m.group(1);
			return sp.parse(latestSeason);
		}
		else {
			throw new RuntimeException("Could not find latest season on competiton page");
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		CompetitionParser competitionParser = new CompetitionParser();
		System.out.println(
				competitionParser.getSeasons("ita-serie-c-girone-b")
				);
	}
}
