package com.aditya.research.pso.crawlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aditya.research.pso.parsers.IScheduleParser;
import com.aditya.research.pso.parsers.ISeasonParser;
import com.aditya.research.pso.parsers.Utils;
import com.aditya.research.pso.parsers.austriasoccer.ASATScheduleParser;
import com.aditya.research.pso.parsers.championat.ChampionatScheduleParser;

import pso.Constants;
import pso.DBCache;
import pso.FileSystemCache;
import pso.WeltSchedulePenaltyParser;
import pso.WeltSeasonParser;

public class MatchHtmlCrawler implements Runnable{
	DBCache matchPageCache,seasonPageCache,competitionPageCache;
	
	ISeasonParser seasonParser;
	IScheduleParser scheduleCrawler;
	
	public static MatchHtmlCrawler weltCrawler(){
		MatchHtmlCrawler mhc = new MatchHtmlCrawler();
		mhc.matchPageCache = DBCache.weltpsoCache();
		mhc.seasonPageCache = DBCache.weltseasonCache();
		mhc.competitionPageCache = DBCache.weltseasonCache();
		mhc.seasonParser = new WeltSeasonParser();
		mhc.scheduleCrawler = new WeltSchedulePenaltyParser();
		return mhc;
	}
	
	public static MatchHtmlCrawler ASATCrawler(){
		MatchHtmlCrawler mhc = new MatchHtmlCrawler();
		mhc.matchPageCache = DBCache.ASATpsoCache();
		mhc.seasonPageCache = DBCache.ASATseasonCache();
		mhc.scheduleCrawler = new ASATScheduleParser();
		return mhc;
	}
	
	public static MatchHtmlCrawler ChampionatCrawler(){
		MatchHtmlCrawler mhc = new MatchHtmlCrawler();
		mhc.matchPageCache = DBCache.ChampionatpsoCache();
		mhc.seasonPageCache = DBCache.ChampionatSeasonCache();
		mhc.scheduleCrawler = new ChampionatScheduleParser();
		return mhc;
	}
	
	//Welt Init
	
//	ScheduleCrawler scheduleCrawler = new ScheduleCrawler();
//	FileSystemCache matchPageCache = new FileSystemCache("/home/aditya/Research Data/weltfussball/friendlies/", Constants.worldfootballURL + Constants.report + "/");
	
	public void crawlSeasons(String path) throws IOException{
		
		//		String path = args.length==0?Constants.baseFolder + "season_pages":args[0];

		List<String> competitionURIsList = Files.readAllLines(Paths.get(path));
		
		for (String seasonURI : Utils.shuffle(competitionURIsList)) {
			crawlSeason(seasonURI);
		}
	}
	private void crawlSeason(String seasonURI) {
		try{
			List<String> matchURIs = scheduleCrawler.parse(seasonPageCache.get(seasonURI));
			for (String matchURI : Utils.shuffle(matchURIs)) {
				matchPageCache.get(matchURI);  
//				System.out.println(matchURI);
			}
		}
		catch(Exception e){
			System.out.println("failed" + seasonURI);
		}
	}
	private void crawlCompetitions(String path) throws IOException {
		List<String> competitionURIsList = Files.readAllLines(Paths.get(path));
			for (String competition_uri : Utils.shuffle(competitionURIsList)) {
			crawlCompetition(competition_uri);
		}
	}
	private void crawlCompetition(String competition_uri) throws IOException {
		try{
			List<String> seasons = Utils.shuffle(seasonParser.parse(competitionPageCache.get(competition_uri)));
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
			crawlCompetitions(Constants.weltFolder + "competition_pages");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void launchCrawler(int noOfThreads){
		for(int i=1;i<=noOfThreads;i++){
			Thread t = new Thread(MatchHtmlCrawler.weltCrawler());
			t.start();
		}
	}
	
	public static void main(String[] args) throws IOException{
		MatchHtmlCrawler mhc = MatchHtmlCrawler.weltCrawler();
//		mhc.crawlSeasons(Constants.championatFolder + "seasons_russian_cup");
//		mhc.crawlSeason("52/calendar/playoff.html");
//		mhc.crawlCompetition("eng-league-cup-1998-1999");
//		mhc.crawlCompetition(args[0]);
//		mhc.crawlCompetitions(Constants.weltFolder + "competition_pages_set1");
//		MatchHtmlCrawler.launchCrawler(1);
		mhc.run();
		WeltSchedulePenaltyParser.listMatchesWithNoPage();
		System.out.println("done");
	}

}
