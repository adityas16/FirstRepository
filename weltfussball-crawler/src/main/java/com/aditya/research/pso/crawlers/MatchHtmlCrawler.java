package com.aditya.research.pso.crawlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aditya.research.pso.parsers.Utils;

import pso.Constants;
import pso.DBCache;
import pso.FileSystemCache;
import pso.SchedulePenaltyParser;
import pso.SeasonParser;

public class MatchHtmlCrawler implements Runnable{
	SeasonParser seasonParser = new SeasonParser();
	
//	DBCache matchPageCache = DBCache.weltgameCache();
//	ScheduleCrawler scheduleCrawler = new ScheduleCrawler();
	
	DBCache matchPageCache = DBCache.weltpsoCache();
	SchedulePenaltyParser scheduleCrawler = new SchedulePenaltyParser();
//	FileSystemCache matchPageCache = new FileSystemCach																		e("/home/aditya/Research Data/weltfussball/friendlies/", Constants.worldfootballURL + Constants.report + "/");
	
	public void crawlSeasons(String path) throws IOException{
		
		//		String path = args.length==0?Constants.baseFolder + "season_pages":args[0];

		List<String> competitionURIsList = Files.readAllLines(Paths.get(path));
		
		for (String seasonURI : Utils.shuffle(competitionURIsList)) {
			crawlSeason(seasonURI);
		}
	}
	private void crawlSeason(String seasonURI) {
		try{
			List<String> matchURIs = scheduleCrawler.parseURI(seasonURI);
			for (String matchURI : Utils.shuffle(matchURIs)) {
				matchPageCache.get(matchURI);  
				System.out.println(matchURI);
			}
//			System.out.println(seasonURI);
		}
		catch(Exception e){
			System.out.println("failed" + seasonURI);
		}
	}
	private void crawlCompetitions() throws IOException {
		List<String> competitionURIsList = Files.readAllLines(Paths.get(Constants.weltFolder + "competition_pages"));
			for (String competition_uri : Utils.shuffle(competitionURIsList)) {
			crawlCompetition(competition_uri);
		}
	}
	private void crawlCompetition(String competition_uri) throws IOException {
		try{
			List<String> seasons = Utils.shuffle(seasonParser.parse(competition_uri));
			for (String seasonURI : seasons) {
				try{
				crawlSeason(seasonURI);
				}
				catch(Exception e){
					System.out.println("failed season" + seasonURI);
				}
			}
		}
		catch(Exception e){
			System.out.println("failed commpetition" + competition_uri);
		}
	}
	public void run() {
		try {
			crawlCompetitions();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void launchCrawler(int noOfThreads){
		for(int i=1;i<=noOfThreads;i++){
			Thread t = new Thread(new MatchHtmlCrawler());
			t.start();
		}
	}
	
	public static void main(String[] args) throws IOException{
		MatchHtmlCrawler mhc = new MatchHtmlCrawler();
//		mhc.crawlSeason("eng-fa-community-shield-2017");
//		mhc.crawlCompetition("eng-fa-community-shield-2017");
//		mhc.crawlCompetition(args[0]);
//		mhc.crawlCompetitions();
		MatchHtmlCrawler.launchCrawler(8);
	}

}
