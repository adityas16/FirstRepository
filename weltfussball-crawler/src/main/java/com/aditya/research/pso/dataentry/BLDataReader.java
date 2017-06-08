package com.aditya.research.pso.dataentry;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.aditya.research.pso.etl.StringUtils;
import com.aditya.research.pso.parsers.Utils;

import pso.Constants;
import pso.DataValiditor;
import pso.FileUtils;
import pso.Shot;

public class BLDataReader {
	private DataValiditor dv = DataValiditor.football();
	
	private  Map<String, List<Shot>> parseShootouts(String fileName) throws IOException {
		CSVParser csvParser = CSVFormat.EXCEL.withHeader().parse(new FileReader(new File(fileName)));
		List<CSVRecord> records = csvParser.getRecords();
		Map<String, List<Shot>> shotsByGame = new HashMap<String, List<Shot>>();
		String currentGame = "";
		List<Shot> currentGameShots = new ArrayList<Shot>();
		boolean isHomeShotFirst = false;
		int homeScore = 0,awayScore=0,kickNumber=1;
		for (CSVRecord record : records) {
			if(!record.get("uri").equals(currentGame)){
				if(!currentGameShots.isEmpty() && dv.checkGame(currentGame, currentGameShots)){
				shotsByGame.put(currentGame, currentGameShots);
				}
				else{
					System.out.println("Invalid Game : " + currentGame);
				}
				currentGame = record.get("uri");
				currentGameShots = new ArrayList<Shot>();
				isHomeShotFirst = Integer.parseInt(record.get("is_home_shot")) == 1;
				homeScore = 0;awayScore=0;kickNumber=1;
			}
			Shot s = new Shot();
			s.homeShotFirst = isHomeShotFirst;
			s.isConverted = Integer.parseInt(record.get("event_type_id"))==13;
			s.kickNumber = kickNumber++;
			s.uri = currentGame;
			s.gameId = record.get("uri");
			s.competition = "NA";
			homeScore = homeScore + Utils.asInt(s.isHomeShot()) * Utils.asInt(s.isConverted);
			awayScore = awayScore + Utils.asInt(!s.isHomeShot()) * Utils.asInt(s.isConverted);
			s.homeScore = homeScore;
			s.awayScore = awayScore;
			currentGameShots.add(s);
		}
		shotsByGame.put(currentGame, currentGameShots);
		return shotsByGame;
	}
	
	private static void getValidShootouts(String inputFileName) throws IOException {
		BLDataReader tfr = new BLDataReader();
		Map<String, List<Shot>> shotsByGame = tfr.parseShootouts(inputFileName + ".csv");
		List<Shot> allShots = new ArrayList<>();
		for (List<Shot> shootout : shotsByGame.values()) {
			allShots.addAll(shootout);
		}
		FileUtils.writeToCSV(inputFileName + "_valid.csv", allShots);
	}
	
	public static void main(String[] args) throws IOException {
		String inputFileName = Constants.BLFolder + "shootout_events";
		getValidShootouts(inputFileName);
	}





}
