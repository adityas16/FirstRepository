package com.aditya.research.pso.crawlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pso.Constants;
import pso.DBCache;

public class PlayerCrawler {

	public void crawlPlayersInFile(String filename) throws IOException{
		DBCache playerPageCache = DBCache.weltPlayerCache();

		List<String> playerURIsList = new ArrayList(new HashSet<String>(Files.readAllLines(Paths.get(filename))));
		Collections.shuffle(playerURIsList);
		
		for (String player_uri : playerURIsList) {
			playerPageCache.get(player_uri);
			System.out.println(player_uri);
		}
	}

	public static void main(String[] args) throws IOException {
		PlayerCrawler pc = new PlayerCrawler();
		pc.crawlPlayersInFile(Constants.weltFolder + "keepperPages_all");
	}
}
