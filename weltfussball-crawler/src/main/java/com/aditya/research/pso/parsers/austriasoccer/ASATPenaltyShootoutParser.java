package com.aditya.research.pso.parsers.austriasoccer;

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

public class ASATPenaltyShootoutParser implements Parser {

	@Override
	public List<Map<String, String>> parse(Document doc) {
		List<Shot> shots  = new ArrayList<>();
		int kickNumber =1;
		int homeScore =0,awayScore =0;
		Element parent = doc.select(":contains(Elfmeterschiessen)").last().parent().parent().parent().parent();
		int index = doc.select(":contains(Elfmeterschiessen)").last().parent().parent().parent().siblingIndex() + 2;
		Node goalTable = parent.childNode(index).childNode(1).childNode(1).childNode(1).childNode(1);
		
		boolean homeShotFirst = !goalTable.childNode(1).childNode(1).outerHtml().contains("nbsp");
		for(int j=1;j<goalTable.childNodeSize()-2;j=j+2){
			Shot s = new Shot();
			s.kickNumber = kickNumber ++;
			s.isConverted = goalTable.childNode(j).childNode(3).childNode(1).childNode(0).toString().contains(":");
			s.homeShotFirst = homeShotFirst;
			homeScore = homeScore + Utils.asInt(s.isHomeShot()) * Utils.asInt(s.isConverted);
			awayScore = awayScore + Utils.asInt(!s.isHomeShot()) * Utils.asInt(s.isConverted);
			s.homeScore = homeScore;
			s.awayScore = awayScore;
			shots.add(s);
			
		}
		List<Map<String, String>> records  = new ArrayList<Map<String,String>>();
		for (Shot shot : shots) {
			records.add(shot.asMap());
		}
		return records;
	}
	
	public static void main(String[] args) throws IOException {
		DBCache cache = DBCache.ASATpsoCache();
		ASATPenaltyShootoutParser asatSeasonParser = new ASATPenaltyShootoutParser();
		System.out.println(asatSeasonParser.parse(cache.get("2016/20160921sv_lsv_m1.htm")));
		
	}
}
