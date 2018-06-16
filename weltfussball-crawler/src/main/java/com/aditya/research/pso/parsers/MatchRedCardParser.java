package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import pso.DBCache;

public class MatchRedCardParser implements Parser{
	
	DBCache webPageCache = DBCache.weltgameCache();
	GameParser gp = new GameParser();
	MatchGoalParser mgp = new MatchGoalParser();

	@Override
	public List<Map<String, String>> parse(Document doc) {
//		doc.title().split(regex)
		List<Map<String,String>> redCards = new ArrayList<Map<String,String>>();
		//TODO: Rectify this error. The home table is #3 only when there are incidents. Else, it is number 2.
		
		List<Element> redCardElements = doc.select("img[src$=https://s.hs-data.com/bilder/shared/karten/3.gif]");
		//Second yellow resulting in red
		redCardElements.addAll(doc.select("img[src$=https://s.hs-data.com/bilder/shared/karten/2.gif]"));
		
		for (Element element : redCardElements) {
			Map<String,String> event = new HashMap<String, String>();
			event.put("is_home", (element.parents().get(4).siblingIndex() == 1) + "");
			event.put("time", "NA");
			Matcher m = Pattern.compile("title=\"at (\\d+)\"").matcher(element.parent().toString());
			if(m.find()) {
				event.put("time", m.group(1));
			}
			event.putAll(gp.parse(doc).get(0));
			redCards.add(event);
		}
		return redCards;
	}

	public  List<Map<String,String>> parseURI(String uri) throws IOException{
		return parse(webPageCache.get(uri));
	}
	
	public static void main(String[] args) throws IOException {
		MatchRedCardParser mep = new MatchRedCardParser();
		
		System.out.println(mep.parseURI("premier-league-2017-2018-arsenal-fc-southampton-fc"));
		//one red, one second yellow
		System.out.println(mep.parseURI("premier-league-2017-2018-chelsea-fc-burnley-fc"));
		System.out.println(mep.parseURI("premier-league-2017-2018-newcastle-united-tottenham-hotspur"));
//		mep.parseURI("europa-league-1961-1962-1-runde-hibernian-fc-os-belenenses");
	}
	
}
