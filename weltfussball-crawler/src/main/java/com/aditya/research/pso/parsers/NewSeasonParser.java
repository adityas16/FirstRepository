package com.aditya.research.pso.parsers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

import pso.DBCache;

public class NewSeasonParser implements Parser {
	DBCache webPageCache = DBCache.weltseasonCache();


	@Override
	public List<Map<String, String>> parse(Document doc) {
		List<Map<String, String>> records  = new ArrayList<Map<String,String>>();
		Map<String,String> asMap =new LinkedHashMap<String,String>();
		asMap.put("has_unplayed_games", "0");
		
		if(doc.outerHtml().contains("-:-")) {
			asMap.put("has_unplayed_games", "1");
		}
		records.add(asMap);
		return records;
	}
	
	public static void main(String[] args) {
		
	}

}
