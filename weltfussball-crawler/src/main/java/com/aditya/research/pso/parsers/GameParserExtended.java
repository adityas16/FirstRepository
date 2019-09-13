package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

import pso.DBCache;

public class GameParserExtended extends GameParser {
	
	@Override
	public List<Map<String, String>> parse(Document doc) {
		Map<String, String> asMap ;
		asMap = super.parse(doc).get(0);
		
		asMap.put("h_h", "0");
		asMap.put("h_a", "0");
		asMap.put("r_h", "0");
		asMap.put("r_a", "0");
		asMap.put("o_h", "0");
		asMap.put("o_a", "0");
		
		List<Map<String, String>> goals = mgp.parse(doc);
		
	
		Map<String,Integer> score = computeScore(0 ,45, goals);
		asMap.put("h_h", score.get("home")+"");
		asMap.put("h_a", score.get("away")+ "");
		
		score = computeScore(46 ,90, goals);
		asMap.put("r_h", score.get("home")+"");
		asMap.put("r_a", score.get("away")+ "");
		
		score = computeScore(91 ,120, goals);
		asMap.put("o_h", score.get("home")+"");
		asMap.put("o_a", score.get("away")+ "");
		
		return Arrays.asList(asMap);
	}
	
	private Map<String,Integer> computeScore(int fromTime ,int toTime,List<Map<String, String>> goals) {
		int homeScoreComputed = 0;
		int awayScoreComputed = 0;
		for (Map<String, String> goal : goals) {
			if(Integer.parseInt(goal.get("time")) <fromTime || Integer.parseInt(goal.get("time")) > toTime ) {
				continue;
			}
			if(Integer.parseInt(goal.get("isHomeGoal")) == 1) {
				homeScoreComputed++;
			}
			else {
				awayScoreComputed++;
			}
		}
		Map<String,Integer> score = new HashMap<String,Integer>() ;
		score.put("home", homeScoreComputed);
		score.put("away", awayScoreComputed);
		return score;
	}
	
	public static void main(String[] args) throws IOException {
		GameParser mp = new GameParserExtended();
		System.out.println(mp.parse(DBCache.weltgameCache().get("primera-division-2012-2013-rayo-vallecano-atletico-madrid")));
		System.out.println(mp.parse(DBCache.weltgameCache().get("afrika-cup-1992-im-senegal-finale-elfenbeinkueste-ghana")));
	
	}
}
