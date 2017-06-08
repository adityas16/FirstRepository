package com.aditya.research.pso.parsers.austriasoccer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.aditya.research.pso.parsers.IScheduleParser;
import com.aditya.research.pso.parsers.ISeasonParser;

import pso.DBCache;

public class ASATScheduleParser implements IScheduleParser {

	@Override
	public List<String> parse(Document doc) throws IOException {
		List<String> uris = new ArrayList<>();
		Element table = doc.select("#content > div > table > tbody").get(0);
		for (Node match : table.childNodes()) {
			if(match.outerHtml().contains("Pen.")){
				Matcher m = Pattern.compile("href=\".*spiele/(.*)\">").matcher(match.outerHtml());
				if(m.find()){
				uris.add(m.group(1));
				}
			}
		}
		return uris;
	}

	public static void main(String[] args) throws IOException {
		DBCache cache = DBCache.ASATseasonCache();
		ASATScheduleParser asatSeasonParser = new ASATScheduleParser();
		System.out.println(asatSeasonParser.parse(cache.get("1980_89/cup_1980_81.htm")));
		
	}
}
