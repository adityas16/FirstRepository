package com.aditya.research.pso.crawlers.hockeyref;

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

public class PenaltyShootoutParser  implements Parser{
	DBCache cache = DBCache.hockeyrefGameCache();
	public List<Map<String, String>> parse(Document doc) {
		List<Shot> shots = new ArrayList<Shot>();
		String awayTeam = doc.select("#content > div.scorebox > div:nth-child(1) > div:nth-child(1) > strong > a").get(0).attr("href");
		Matcher m = Pattern.compile("/teams/([\\p{Alnum}]*)/").matcher(awayTeam);
		m.find();
		awayTeam = m.group(1);
		if(!doc.outerHtml().contains("Scoring Summary")){
			throw new RuntimeException("No goal Time info");
		}
		//0-0 match
		Element goalTable =  doc.select("#scoring > tbody").get(0);
		if(doc.select("#scoring > tbody").size() != 0){
			for(int j=1;j<goalTable.childNodeSize();j=j+2){
				Node rowElement = goalTable.childNode(j);
				//if this is a period
				if(goalTable.childNode(j).hasAttr("class") && goalTable.childNode(j).attr("class").equals("thead onecell")){
					String periodName = goalTable.childNode(j).childNode(1).childNode(0).toString().trim();
					if(periodName.equals("Shootout")){
						int kickNumber =1;
						boolean homeShotFirst = !goalTable.childNode(j+2).childNode(3).childNode(0).childNode(0).toString().trim().equals(awayTeam);
						int homeScore =0,awayScore =0;
						for(int k=j+2;k<goalTable.childNodeSize();k=k+2){
							Node rowElement1 = goalTable.childNode(k);
							Shot s = new Shot();
							s.kickNumber = kickNumber ++;
							s.isConverted = rowElement1.childNode(5).childNode(3).childNode(0).toString().equals("successful");
							s.homeShotFirst = homeShotFirst;
							homeScore = homeScore + Utils.asInt(s.isHomeShot()) * Utils.asInt(s.isConverted);
							awayScore = awayScore + Utils.asInt(!s.isHomeShot()) * Utils.asInt(s.isConverted);
							s.homeScore = homeScore;
							s.awayScore = awayScore;
							shots.add(s);
						}
						break;
					}
				}
			}
		}
		List<Map<String, String>> records  = new ArrayList<Map<String,String>>();
		for (Shot shot : shots) {
			records.add(shot.asMap());
		}
		return records;
	}


	public List<Map<String, String>> parse(String url) throws IOException {
		return parse(cache.get(url));
	}

	public static void main(String[] args) throws IOException {
		PenaltyShootoutParser g = new PenaltyShootoutParser();
		System.out.println(g.parse("201512080PHI"));
	}
}
