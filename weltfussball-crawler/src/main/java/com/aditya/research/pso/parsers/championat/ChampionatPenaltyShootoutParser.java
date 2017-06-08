package com.aditya.research.pso.parsers.championat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.aditya.research.pso.parsers.Parser;
import com.aditya.research.pso.parsers.Utils;

import pso.DBCache;
import pso.Shot;

public class ChampionatPenaltyShootoutParser implements Parser {

	@Override
	public List<Map<String, String>> parse(Document doc) {
		List<Shot> shots  = new ArrayList<>();
		int kickNumber =1;
		int homeScore =0,awayScore =0;
		
		Node goalTable = doc.select(":contains(Послематчевые пенальти)").last().parent().childNode(3);
		boolean homeShotFirst = goalTable.childNode(1).outerHtml().contains("class=\"match__stat__row _team1\"");
		for(int j=1;j<goalTable.childNodeSize();j=j+2){
			Shot s = new Shot();
			s.kickNumber = kickNumber ++;
			s.isConverted = !goalTable.childNode(j).outerHtml().contains("Сейв") && !goalTable.childNode(j).outerHtml().contains("Мимо");
			s.homeShotFirst = homeShotFirst;
			homeScore = homeScore + Utils.asInt(s.isHomeShot()) * Utils.asInt(s.isConverted);
			awayScore = awayScore + Utils.asInt(!s.isHomeShot()) * Utils.asInt(s.isConverted);
			s.homeScore = homeScore;
			s.awayScore = awayScore;
			shots.add(s);
			
		}
		
		for (Shot shot : shots) {
			System.out.println(shot);
		}
		
		List<Map<String, String>> records  = new ArrayList<Map<String,String>>();
		for (Shot shot : shots) {
			records.add(shot.asMap());
		}
		return records;
	}
	
	public static void main(String[] args) throws IOException {
		DBCache cache = DBCache.ChampionatpsoCache();
		ChampionatPenaltyShootoutParser asatSeasonParser = new ChampionatPenaltyShootoutParser();
		System.out.println(asatSeasonParser.parse(cache.get("1522/match/500362.html")));
		
	}
}
