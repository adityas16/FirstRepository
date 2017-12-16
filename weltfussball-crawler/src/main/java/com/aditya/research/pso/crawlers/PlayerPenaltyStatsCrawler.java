package com.aditya.research.pso.crawlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import org.jsoup.Jsoup;

import pso.Constants;
import pso.DBCacheDao;

public class PlayerPenaltyStatsCrawler {
	public void crawlPlayersInFile(String filename) throws IOException{
		DBCacheDao playerPageCache = new DBCacheDao("player_penalty_stats", "transfermrkt");

		List<String> playerURIsList = new ArrayList(new HashSet<String>(Files.readAllLines(Paths.get(filename))));
		Collections.shuffle(playerURIsList);
		
		for (String player_uri : playerURIsList) {
			String name = player_uri.split(",")[0];
			String id = player_uri.split(",")[1];
			String dbURI = name + "_" + id;
			if(id.equals("") || name.equals("")){
				continue;
			}
			String webURI = name + "/elfmetertore/spieler/" + id;
			try{
				playerPageCache.get(dbURI);
			}
			catch(NoSuchElementException ne){
				try{
				playerPageCache.put(dbURI, Jsoup.connect(Constants.transfermrktURL + webURI)
						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
			               .referrer("http://www.google.com") 
			               .timeout(0) //it's in milliseconds, so this means 5 	seconds.              
			               .get().outerHtml());
				}catch(Exception e){
					System.err.println(dbURI);
					continue;
				}
			}
//			System.out.println(player_uri);
		}
	}

	public static void main(String[] args) throws IOException {
		PlayerPenaltyStatsCrawler pc = new PlayerPenaltyStatsCrawler();
		pc.crawlPlayersInFile(Constants.transermrktFolder + "playerPenaltyStatsPages_all");
	}
}
