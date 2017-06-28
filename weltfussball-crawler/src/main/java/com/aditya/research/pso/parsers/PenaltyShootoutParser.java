package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pso.Constants;
import pso.DBCache;
import pso.FileSystemCache;
import pso.Shot;

public class PenaltyShootoutParser implements Parser{
	private static boolean homeShotFirst;
	private static String competition;
	private static int gameId=0;
	private static String gameName;
	private static int kickNumber=1;
	private static int year;
	private static String stage;
	private static int homeScore=0;
	private static int awayScore=0;
	
//	private static Set<String> invalidGames = DataValiditor.findInvalidGames();
	
	private static Set<String> invalidGames = new HashSet<String>();
	
	static FileSystemCache webPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/games/", Constants.worldfootballURL + Constants.report + "/");
	
	public List<Map<String,String>> parse(Document doc){
		List<Shot> shots = parseForShots(doc);
		List<Map<String, String>> records  = new ArrayList<Map<String,String>>();
		for (Shot shot : shots) {
			records.add(shot.asMap());
		}
		return records;
	}
	public List<Shot> parseForShots(Document doc) {
		kickNumber = 1;
		homeScore = awayScore =0;
		gameName = doc.title().trim();
		if(invalidGames.contains(gameName)){
			throw new RuntimeException("Invalid game");
		}
		String competitionName = doc.getElementsByAttributeValue("name", "page-topic").get(0).attr("content").trim();
		year = getCompetitionYear(competitionName);
		competition = getCompetitionName(competitionName);
		gameId++;
		stage = getStageName(gameName);
		String date = doc.getElementsByClass("box").get(0).getAllElements().get(7).childNode(0).outerHtml().trim();
		
		List<Shot> shots = new ArrayList<Shot>();
		
		Elements hellElements = doc.getElementsByClass("standard_tabelle").get(2).getElementsByClass("hell");
		Elements dunkelElements = doc.getElementsByClass("standard_tabelle").get(2).getElementsByClass("dunkel");
		
		homeShotFirst = false;
		if(hellElements.get(1).attributes().get("style").equals("")){
			homeShotFirst = true;
		}
		Iterator<Element> hellIterator = hellElements.iterator();
		Iterator<Element> dunkelIterator = dunkelElements.iterator();
		while(hellIterator.hasNext()){
			
			shots.add(extractShot(hellIterator));
			
			
			if(dunkelIterator.hasNext()){
				shots.add(extractShot(dunkelIterator));
				
			}
			
		}
		return shots;
	}
	private static String getCompetitionName(String text){
		int i=0;
		for(i=0;i<text.length();i++){
			if(Character.isDigit(text.charAt(i))){
				break;
			}
		}
		return text.substring(0,i);
	}
	
	private static String getStageName(String text){
		return text.split(",")[1].replace(')', ' ').trim();
	}
	
	private static int getCompetitionYear(String str){
		Pattern pattern = Pattern.compile("\\w+([0-9]+)\\w+");
		Matcher matcher = pattern.matcher(str);
		matcher.find();
		return Integer.parseInt(matcher.group());
	}
	
//	public List<Shot> parseURI(String uri) throws IOException{
//		return parse(webPageCache.get(uri));
//	}
	
//	public static List<Shot> parseFile(String filename) throws IOException{
//		File input = new File(filename);
//		return parse(Jsoup.parse(input, "UTF-8", "http://example.com/"));
//	}
	public static Shot extractShot(Iterator<Element> hellIterator) {
		Element hellElement = hellIterator.next();
		Shot s = new Shot();
		String[] scoreStrings = hellElement.text().split(":");
		try{
		s.homeScore = Integer.parseInt(scoreStrings[0].trim());
		s.awayScore = Integer.parseInt(scoreStrings[1].trim());
		homeScore = s.homeScore;
		awayScore = s.awayScore;
		}
		catch(NumberFormatException n){
			s.homeScore = homeScore;
			s.awayScore = awayScore;
		}
		hellElement = hellIterator.next();
		s.isConverted =  hellElement.getAllElements().get(0).childNode(2).toString().trim().equals("scores")?true:false;
		try{
			s.striker = hellElement.getAllElements().get(0).childNode(1).attr("href").split("/")[2].replace('/', ' ').trim();
		}
		catch(Exception e){
			s.striker = "NA";
		}
		s.homeShotFirst = homeShotFirst;
		s.competition = competition;
		s.gameId=gameId + "";
		s.kickNumber = kickNumber++;
		s.gameName = gameName;
		s.year = year;
		s.stage = stage;
		return s;
	}
	
	public static void main(String[] args) throws IOException {
		DBCache cache = DBCache.weltpsoCache();
		PenaltyShootoutParser psp = new PenaltyShootoutParser();
		Utils.printRecords(psp.parse( cache.get("afc-champions-league-qual-2013-finale-buriram-united-brisbane-roar")));
	}
}
