//package com.aditya.research.pso.crawlers;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import pso.Constants;
//import pso.DBCache;
//import pso.SchedulePenaltyParser;
//import pso.SeasonParser;
//
//public class PenaltyShootoutCrawler {
//	DBCache matchPageCache = DBCache.weltpsoCache();
//
//	public void crawlSeasons(String path) throws IOException{
//
//		//		String path = args.length==0?Constants.baseFolder + "season_pages":args[0];
//
//		List<String> competitionURIsList = Files.readAllLines(Paths.get(path));
//		Set<String> seasonURIs = new HashSet<String>(competitionURIsList);
//		List<String> seasonURIsList = new ArrayList<String>(seasonURIs);
//		Collections.shuffle(seasonURIsList);
//		for (String seasonURI : seasonURIsList) {
//			crawlSeason(seasonURI);
//		}
//	}
//	private void crawlSeason(String seasonURI) {
//		try{
//			List<String> matchURIs = SchedulePenaltyParser.parseURI(seasonURI);
//			for (String matchURI : matchURIs) {
//				matchPageCache.get(matchURI);
//				System.out.println(matchURI);
//			}
//			System.out.println(seasonURI);
//		}
//		catch(Exception e){
//			System.out.println("failed season" + seasonURI);
//		}
//	}
//	private void crawlCompetition(String competition_uri) throws IOException {
//		try{
//		Set<String> seasonURIs = new HashSet<String>(SeasonParser.parse(competition_uri));
//		for (String seasonURI : seasonURIs) {
//			crawlSeason(seasonURI);
//		}
//		}
//		catch(Exception e){
//			System.out.println("failed competition" + competition_uri);
//		}
//		
//	}
//	private void crawlCompetitions(String path) throws IOException {
//		List<String> competitionURIsList = Files.readAllLines(Paths.get(path));
//
//		Set<String> competitionURIs = new HashSet<String>(competitionURIsList);
//		competitionURIsList = new ArrayList<String>(competitionURIs);
//		Collections.shuffle(competitionURIsList);
//		for (String competition_uri : competitionURIsList) {
//			crawlCompetition(competition_uri);
//		}
//	}
//
//	public static void main(String[] args) throws IOException{
//		PenaltyShootoutCrawler mhc = new PenaltyShootoutCrawler();
//		//893
//		mhc.crawlCompetitions(Constants.weltFolder + "scriptsToGetAllCompetitions/competition_pages");
////		mhc.crawlSeasons(Constants.baseFolder + "season_pages");
//			
//	}
//}
