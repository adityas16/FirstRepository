package com.aditya.research.pso.crawlers.hockeyref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.aditya.research.pso.etl.StringUtils;
import com.aditya.research.pso.parsers.Parser;
import com.aditya.research.pso.parsers.Utils;

import pso.DBCache;

public class GoalParser implements Parser{
	static final int TIME_PER_PERIOD = 20;
	DBCache cache = DBCache.hockeyrefGameCache();
	public List<Map<String, String>> parse(Document doc) {
		List<Map<String, String>> goals = new ArrayList<Map<String,String>>();
		String awayTeam = doc.select("#content > div.scorebox > div:nth-child(1) > div:nth-child(1) > strong > a").get(0).attr("href");
		Matcher m = Pattern.compile("/teams/([\\p{Alnum}]*)/").matcher(awayTeam);
		m.find();
		awayTeam = m.group(1);
		if(!doc.outerHtml().contains("Scoring Summary")){
			throw new RuntimeException("No goal Time info");
		}
		//0-0 match
		if(doc.select("#scoring > tbody").size() == 0){
			return goals;
		}
		Element goalTable =  doc.select("#scoring > tbody").get(0);
		int period = 1;
		for(int j=1;j<goalTable.childNodeSize();j=j+2){
			Node rowElement = goalTable.childNode(j);
			//if this is a period
			if(goalTable.childNode(j).hasAttr("class") && goalTable.childNode(j).attr("class").equals("thead onecell")){
				String periodName = goalTable.childNode(j).childNode(1).childNode(0).toString().trim();
				if(periodName.equals("Shootout")){
					break;
				}
				period = StringUtils.extractInt(periodName);
				continue;
			}
			Map<String, String> goal = new LinkedHashMap<String, String>();
			goal.put("time", Integer.parseInt(rowElement.childNode(1).childNode(0).toString().split(":")[0]) + (period -1) * TIME_PER_PERIOD + 1 + "");
			String scoringSide = rowElement.childNode(3).childNode(0).childNode(0).toString().trim();
			goal.put("isHomeGoal", Utils.asInt(!scoringSide.equals(awayTeam))+ "");
			
			goals.add(goal);
		}
		return goals;
	}
	
	public List<Map<String, String>> parse(String url) throws IOException {
		return parse(cache.get(url));
	}
	
	public static void main(String[] args) throws IOException {
		GoalParser g = new GoalParser();
		System.out.println(g.parse("200604040MIN"));
	}
}
