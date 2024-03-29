package com.aditya.research.pso.parsers.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.aditya.research.pso.crawlers.hockeyref.GoalParser;
import com.aditya.research.pso.etl.StringUtils;
import com.aditya.research.pso.parsers.GameParser;
import com.aditya.research.pso.parsers.GameParserExtended;
import com.aditya.research.pso.parsers.MatchGoalParser;
import com.aditya.research.pso.parsers.MatchIncidentParser;
import com.aditya.research.pso.parsers.MatchRedCardParser;
import com.aditya.research.pso.parsers.NewSeasonParser;
import com.aditya.research.pso.parsers.Parser;
import com.aditya.research.pso.parsers.PenaltyShootoutParser;
import com.aditya.research.pso.parsers.PlayerParser;
import com.aditya.research.pso.parsers.PlayerPenaltyStatsParser;

import au.com.bytecode.opencsv.CSVWriter;
import pso.Constants;
import pso.DBCache;
import pso.FileUtils;
import pso.SchedulePenaltyParser;

public class ParserRuner {
	
	DBCache pageCache = null;
	Parser parser = null;
	String outputFile =null;
	int count = 0;
	private ParserRuner() {
	}
	
	public static ParserRuner hockeyRefParserRuner(String extractFor) {
		ParserRuner pr = new ParserRuner();
		pr.outputFile = Constants.hockeyrefFolder + extractFor + ".csv";
		if(extractFor.equals("pso")){
			pr.pageCache = DBCache.hockeyrefGameCache();
			pr.parser = new com.aditya.research.pso.crawlers.hockeyref.PenaltyShootoutParser();
		}
		else if(extractFor.equals("goals")){
			pr.pageCache = DBCache.hockeyrefGameCache();
			pr.parser = new GoalParser();
		} 
		else if(extractFor.equals("games")){
			pr.pageCache = DBCache.hockeyrefGameCache();
			pr.parser = new com.aditya.research.pso.crawlers.hockeyref.GameParser();
		} 
		return pr;
	}
	
	public ParserRuner(String extractFor) {
		String outputCSV = null;
		if(extractFor.equals("pso")){
			pageCache = DBCache.weltpsoCache();
			parser = new PenaltyShootoutParser();
			outputCSV = "pso_raw.csv";
		}
		else if(extractFor.equals("player")){
			pageCache = DBCache.weltPlayerCache();
			parser = new PlayerParser();
			outputCSV = "players.csv";
		}
		else if(extractFor.equals("goal")){
			pageCache = DBCache.weltgameCache();
			parser = new MatchGoalParser();
			outputCSV = "goals.csv";
		}
		else if(extractFor.equals("incident")){
			pageCache = DBCache.weltgameCache();
			parser = new MatchIncidentParser();
			outputCSV = "incidents.csv";
		}
		else if(extractFor.equals("shooterPenaltyStats")){
			pageCache = DBCache.transfermrktPlayerPenaltyStatsCache();
			parser = PlayerPenaltyStatsParser.shooterParser();
			outputCSV = "shooter_penalty_stats.csv";
		}
		else if(extractFor.equals("game")){
			pageCache = DBCache.weltpsoCache();
			parser = new GameParser();
			outputCSV = "games.csv";
		}
		else if(extractFor.equals("keeperPenaltyStats")){
			pageCache = DBCache.transfermrktKeeperPenaltyStatsCache();
			parser = PlayerPenaltyStatsParser.keeperParser();
			outputCSV = "keeper_penalty_stats.csv";
		}
		else if(extractFor.equals("redCards")){
			pageCache = DBCache.weltgameCache();
			parser = new MatchRedCardParser();
			outputCSV = "red_cards.csv";
		}
		else if(extractFor.equals("allPsoGames")){
			pageCache = DBCache.weltseasonCache();
			parser = new SchedulePenaltyParser();
			outputCSV = "all_pso_games.csv";
		}
		else if(extractFor.equals("gameExtended")){
			pageCache = DBCache.weltpsoCache();
			parser = new GameParserExtended();
			outputCSV = "games_extended_details.csv";
		}
		else if(extractFor.equals("newSeasons")){
			pageCache = DBCache.weltseasonCache();
			parser = new NewSeasonParser();
			outputCSV = "new_seasons.csv";
		}
		outputFile =  Constants.extractedCSV + outputCSV;
	}
	
	public void parseAll() throws IOException {
		Iterator<DocumentWIthIdentifier> iter = pageCache.allDocuments();
		parseStream(iter);
	}

	private void parseStream(Iterator<DocumentWIthIdentifier> iter) throws IOException {
		CSVWriter writer = new CSVWriter(new OutputStreamWriter (new FileOutputStream(outputFile), "UTF-8"));
		String[] headers = new String[1];
		
		int errorCount=0;
		
		boolean headersWritten = false;
		while(iter.hasNext()){
			List<Map<String, String>> records = new ArrayList<Map<String,String>>();
			DocumentWIthIdentifier documentWIthIdentifier = iter.next();
			try{
			  records = parser.parse(documentWIthIdentifier.getDoc());
			}
			catch(Exception e){
				errorCount++;
				System.out.println("Processing error: "+ documentWIthIdentifier.getIdentifier() + e.getMessage());
			}
			if(records.isEmpty()){
				continue;
			}
			
			if(!headersWritten){
				headers = records.get(0).keySet().toArray(headers);
				writer.writeNext(StringUtils.prependToStringArray(headers,"uri"));
				headersWritten = true;
			}
			String[] fields = new String[headers.length + 1];
			fields[0] = documentWIthIdentifier.getIdentifier();
			for (Map<String, String> record : records) {
				int i=1;
				for (String header : headers) {
					fields[i++] = record.get(header);
				}
				writer.writeNext(fields);
			}
			writer.flush();
		}
		
		writer.close();
		System.out.println(errorCount + " errors");
	}
	
	public void parseFromFile(String filename) throws IOException{
		List<String> URIList = Files.readAllLines(Paths.get(filename));
		List<Map<String, String>> records = new ArrayList<Map<String,String>>();
		
		int entriesOnPage=0;
		for (String URI : new LinkedHashSet<String>(URIList)) {
			count ++;
			entriesOnPage++;
			Map<String, String> map = new LinkedHashMap<String, String>();
			map.put("uri", URI);
			try{
				map.putAll(parser.parse(pageCache.get(URI)).get(0));
			}
			catch(Exception e){
				continue;
			}
			records.add(map);
			if(entriesOnPage==1000) {
				//First page
				if(count==entriesOnPage) {
					FileUtils.write(records, outputFile);
				}
				//Subsequent pages
				else {
					FileUtils.append(records, outputFile);
				}
				entriesOnPage = 0;
				records = new ArrayList<Map<String,String>>();
			}
			
//			System.out.println(URI + "," + count);
		}
		//Still wont write if there are less than 1000 entries total
		if(!records.isEmpty()) {
			FileUtils.append(records, outputFile);
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		String extractFor = "game";
		
		ParserRuner pr = new ParserRuner(extractFor);
//		ParserRuner pr = ParserRuner.hockeyRefParserRuner(extractFor);
//		pr.parseAll();
//		pr.parseFromFile(Constants.weltFolder + "shooter_pages");
		pr.parseFromFile("C:\\Users\\adity\\Desktop\\temp.csv");
	}
}
