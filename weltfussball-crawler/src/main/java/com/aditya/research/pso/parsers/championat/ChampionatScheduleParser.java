package com.aditya.research.pso.parsers.championat;

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

public class ChampionatScheduleParser implements IScheduleParser {

	@Override
	public List<String> parse(Document doc) throws IOException {
		List<String> uris = new ArrayList<>();
		Node table = doc.select("[class=\"table b-table-sortlist\"]").get(0).childNode(3);
		for (Node match : table.childNodes()) {
			Matcher m =Pattern.compile("&nbsp; \\d+:\\d+ ").matcher(match.outerHtml());
			if(m.find()){
				m = Pattern.compile("href=\"/football/_russiacup/(\\d+)/match/(\\d+).html\">").matcher(match.outerHtml());
				if(m.find()){
					uris.add(m.group(1) + "/match/" + m.group(2) + ".html");
				}
			}
		}
		return uris;
	}

	public static void main(String[] args) throws IOException {
		DBCache cache = DBCache.ChampionatSeasonCache();
		ChampionatScheduleParser asatSeasonParser = new ChampionatScheduleParser();
		System.out.println(asatSeasonParser.parse(cache.get("52/calendar/playoff.html")));
//		System.out.println(asatSeasonParser.parse(cache.get("1978/calendar/playoff.html")).size());
	}
}
