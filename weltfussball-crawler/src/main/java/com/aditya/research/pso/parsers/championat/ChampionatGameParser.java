package com.aditya.research.pso.parsers.championat;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import com.aditya.research.pso.parsers.Parser;

import pso.DBCache;

public class ChampionatGameParser implements Parser{
	

	@Override
	public List<Map<String, String>> parse(Document doc) {
		Map<String, String> record = new LinkedHashMap<String, String>();
		Matcher m = Pattern.compile(", (\\d+).(\\d+).(\\d+), (\\d+):(\\d+)").matcher(doc.outerHtml());
		m.find();
		record.put("day", m.group(1));
		record.put("month", m.group(2));
		record.put("year", m.group(3));
		
		
		return Arrays.asList(record);
	}
	
	public static void main(String[] args) throws IOException {
		DBCache cache = DBCache.ASATpsoCache();
		ChampionatGameParser asatSeasonParser = new ChampionatGameParser();
		System.out.println(asatSeasonParser.parse(cache.get("2016/20160921sv_lsv_m1.htm")));
		
	}
}
