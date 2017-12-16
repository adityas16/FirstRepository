package com.aditya.research.pso.crawlers.hockeyref;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.aditya.research.pso.parsers.GameParser;
import com.aditya.research.pso.parsers.Utils;

public class SeasonParser {
	private GoalParser goalParser = new GoalParser();
	public static void main(String[] args) throws IOException {
		SeasonParser s =new SeasonParser();
		s.extractGames();
	}

	private  void extractGames() throws IOException {
		for(int i =2013;i<=2016;i++){
			System.out.println("Reading file : " + i);
			File f = new File("/home/aditya/Research Data/hockeyref/seasons/"+i+".htm");
			Document doc = Jsoup.parse(f, "UTF-8", "http://example.com/");
			List<String> gamesInSeason = new ArrayList<String>();
			try{
				 gamesInSeason = parseSeason(doc);
			}
			catch(Exception e){
				System.err.println("failed season : " + i);
			}
			for (String gameURL : Utils.shuffle(gamesInSeason)) {
				try{
					goalParser.parse(gameURL);
				}
				catch(Exception e){
					System.out.println("failed Game " + gameURL);
				}
			}
		}
	}

	private  List<String> parseSeason(Document doc) {
		Element regularGames = doc.select("#games > tbody").get(0);
		
		List<String> gameUrls = parseRound(regularGames);
		
		Element playOffGames = doc.select("#games_playoffs > tbody").get(0);
		
		gameUrls.addAll(parseRound(playOffGames));
		return gameUrls;
	}

	private List<String> parseRound(Element regularGames) {
		List<String> gameUrls = new ArrayList<String>();
		for(int j=0;j<regularGames.childNodeSize();j=j+2){
			String gameURL = regularGames.childNode(j).childNode(0).childNode(0).attr("href").split("/")[2].replace('/', ' ').replace(".html","").trim();
			gameUrls.add(gameURL);
		}
		return gameUrls;
	}
}