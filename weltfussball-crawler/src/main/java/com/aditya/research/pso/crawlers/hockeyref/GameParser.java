package com.aditya.research.pso.crawlers.hockeyref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class GameParser implements Parser{
	DBCache cache = DBCache.hockeyrefGameCache();
	public List<Map<String, String>> parse(Document doc) {
		Map<String, String> record = new LinkedHashMap<String, String>();
		String awayTeamString = doc.select("#content > div.scorebox > div:nth-child(1) > div:nth-child(1) > strong > a").get(0).attr("href");
		Matcher m = Pattern.compile("/teams/([\\p{Alnum}]*)/").matcher(awayTeamString);
		m.find();
		record.put("away_team",m.group(1));
		int date = StringUtils.extractInt( doc.getElementsByAttributeValue("property","og:url").attr("content"));
		date = date/10;
		record.put("day", date % 100 + "");
		date = date / 100;
		record.put("month", date % 100 + "");
		date = date / 100;
		record.put("year", date + "");
		
		String homeTeamString = doc.select("#content > div.scorebox > div:nth-child(2) > div:nth-child(1) > strong > a").get(0).attr("href");
		m = Pattern.compile("/teams/([\\p{Alnum}]*)/").matcher(homeTeamString);
		m.find();
		record.put("home_team",m.group(1));
		
		m = Pattern.compile("/teams/[\\p{Alnum}]*/(\\d+)").matcher(awayTeamString);
		m.find();
		record.put("season", m.group(1));

		record.put("isPSO", "0");

		if(doc.outerHtml().contains("Scoring Summary")){
			if(doc.select("#scoring > tbody").size() > 0){
				Element goalTable =  doc.select("#scoring > tbody").get(0);
				int period = 1;
				for(int j=1;j<goalTable.childNodeSize();j=j+2){
					Node rowElement = goalTable.childNode(j);
					//if this is a period
					if(goalTable.childNode(j).hasAttr("class") && goalTable.childNode(j).attr("class").equals("thead onecell")){
						String periodName = goalTable.childNode(j).childNode(1).childNode(0).toString().trim();
						if(periodName.equals("Shootout")){
							record.put("isPSO", "1");
						}
					}
				}
			}
		}

		return Arrays.asList(record);
	}

	public List<Map<String, String>> parse(String url) throws IOException {
		return parse(cache.get(url));
	}

	public static void main(String[] args) throws IOException {
		GameParser g = new GameParser();
		System.out.println(g.parse("200711070BUF"));
	}
}