package com.aditya.research.pso.dataentry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aditya.research.pso.etl.StringUtils;
import com.aditya.research.pso.parsers.GameParser;
import com.aditya.research.pso.parsers.Utils;

import pso.DataValiditor;
import pso.FileUtils;
import pso.Shot;

public class TemplateFileReader {
	private String competition;

	public TemplateFileReader(String competition) {
		this.competition = competition;
	}

	//11,5,2002 Mallorca, starting team = 2
	//30,10,1991 Leida, starting team = 1
	//31,3,1982 Real Madrid, starting team = 2
	//27,3,1974 Real Oviedo, all 1, then all 2
	//11,1,1978 Getafe, all 2, then all 1
	//6	1	1988  Castilla repeated
	//Repeated matches. Matches repeates as home-away, away-home
	//5	7	1975,Real Madrid starting team is incorrect 

	private DataValiditor dv = DataValiditor.football();

	public static void main(String[] args) throws IOException {
		//		List<String> lines = Files.readAllLines(Paths.get("/home/aditya/Research Data/DataEntry/PSO-data-input-template.csv"));
		TemplateFileReader tfr = new TemplateFileReader("Copa del Rey");
		tfr.checkShootouts("/home/aditya/Research Data/DataEntry/installment4/" + "repaired");
		//		tfr.checkShootouts("/home/aditya/Research Data/DataEntry/" + "test");
	}

	private  void checkShootouts(String fileName) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(fileName + ".csv"));
		List<Shot> allShootouts = new ArrayList<>();
		List<Map<String,String>> allGames = new ArrayList<>();
		Set<String> uniqueGames = new HashSet<>();
		Map<String,String> status = new TreeMap<>();

		Matcher m1 = null;
		Iterator<String> iter = lines.iterator();
		int invalidShootouts =0;
		int validShootouts =0;
		while(iter.hasNext()){
			Map<String, String> gameMap = new LinkedHashMap<String, String>();
			gameMap.put("uri", "NA");
			GameParser.initAllFields(gameMap);
			gameMap.put("competition", competition);
			if(!extractDate(iter, gameMap)){
				break;
			}
			status.put(gameMap.get("ID"), "invalid");
			List<Shot> shootout = new ArrayList<>();
			try{
				//				System.out.println("Extract shootout for" + getGameName(gameMap));
				extractTeams(iter, gameMap);
				shootout = extractShootout(iter);
				setShootoutGameName(getGameName(gameMap), shootout);
				validShootouts ++ ;
			}catch(Exception e){
				System.out.println(e.getMessage());
				System.out.println("Invalid game : " + getGameName(gameMap));
				invalidShootouts++;
				continue;
			}
			if(!uniqueGames.contains(getGameName(gameMap))){
				uniqueGames.add(getGameName(gameMap));
				allShootouts.addAll(shootout);
				gameMap.put("uri", getGameName(gameMap));
				allGames.add(gameMap);
				status.put(gameMap.get("ID"), "valid");
			}
			else{
				System.out.println("Duplicate game :" + getGameName(gameMap));
			}

		}
		System.out.println("valid unique shootouts" + uniqueGames.size());
		System.out.println("valid  shootouts" + validShootouts);
		System.out.println("invalid shootouts" + invalidShootouts);
//		for (String string : uniqueGames) {
//			System.out.println(string);
//		}
//		for (Entry<String, String> string : status.entrySet()) {
//			System.out.println(string.getKey() + "," + string.getValue());
//		}
				FileUtils.writeToCSV(fileName + "_validated.csv", allShootouts);
				FileUtils.write(allGames,fileName + "_games.csv");
	}
	
	
	private void setShootoutGameName(String gameName,List<Shot> shootout){
		for (Shot shot : shootout) {
			shot.gameName = gameName;
			shot.gameId = gameName;
			shot.competition = competition;
		}
	}

	private String getGameName(Map<String, String> gameMap) {
		return gameMap.get("ID") +"," + gameMap.get("day") +"/"  + gameMap.get("month")+"/" + gameMap.get("year")+":" + gameMap.get("homeTeam") + "-" + gameMap.get("awayTeam");
	}

	private  boolean extractDate(Iterator<String> iter, Map<String, String> gameMap) {
		while(iter.hasNext()){
			String line = iter.next();
			Matcher m = Pattern.compile("ID,(\\d+)").matcher(line);
			if(m.find()){
				gameMap.put("ID",m.group(1));
				line = iter.next();
				m = Pattern.compile("date *,(\\d+),(\\d+),(\\d+)").matcher(line);
				if(m.find()){
					gameMap.put("year", m.group(3));
					gameMap.put("month", m.group(2));
					gameMap.put("day", m.group(1));
					return true;
				}
			}
			else if(line.replace(",", "").length() > 0){
				//Searching for next date instead of throwing error
				//				throw new RuntimeException("Couldn't find date");

				if(line.contains("date")){
					//					System.out.println("ERROR, no date found");
				}
			}
			if(line.contains("date")){
				System.out.println(line);
			}
		}
		return false;
	}

	private  void extractTeams(Iterator<String> iter, Map<String, String> gameMap) {
		while(iter.hasNext()){
			String line = iter.next();
			Matcher m = Pattern.compile("Team 1,([^,]*)").matcher(StringUtils.toSimpleCharset(line));
			if(m.find()){
				gameMap.put("homeTeam", m.group(1));
				break;
			}
			else if(line.replace(",", "").length() > 0){
				//Searching for next date instead of throwing error
				throw new RuntimeException("Couldn't find home team");
			}
		}

		while(iter.hasNext()){
			String line = iter.next();
			Matcher m = Pattern.compile("Team 2\\s*,([^,]*)").matcher(StringUtils.toSimpleCharset(line));
			if(m.find()){
				gameMap.put("awayTeam", m.group(1));
				break;
			}
			else if(line.replace(",", "").length() > 0){
				//Searching for next date instead of throwing error
				throw new RuntimeException("Couldn't find away team");
			}
		}
	}

	private  List<Shot> extractShootout(Iterator<String> iter) {
		List<Shot> shootout = new ArrayList<>();
		int finalHomeScore=-1,finalAwayScore=-1;
		while(iter.hasNext()){
			String line = iter.next();
			if(!line.contains("shootout")){
				continue;
			}
			Matcher m = Pattern.compile("shootout,(\\d+),(\\d+)").matcher(StringUtils.toSimpleCharset(line));
			if(m.find()){
				finalHomeScore = Integer.parseInt(m.group(1));
				finalAwayScore = Integer.parseInt(m.group(2));
			}
			line = iter.next();
			
			boolean isHomeShotFirst = false;
			if(line.contains("Starting team,1")){
				isHomeShotFirst = true;
			}
			else if(line.contains("Starting team,2")){
				isHomeShotFirst = false;
			}
			else{
				throw new RuntimeException("Invalid Starting team");
			}
			if(finalHomeScore>0){
			System.out.println(finalHomeScore + "," + finalAwayScore + "," + Utils.asInt(isHomeShotFirst));
			}
			iter.next();

			int homeScore=0,awayScore = 0,kickNumber=1;
			while(iter.hasNext()){
				line = iter.next();
				if(!(line.charAt(0) == '0') && !(line.charAt(0) == '1')){
					break;
				}
				Shot shot = new Shot();
				shot.homeShotFirst = isHomeShotFirst;
				shot.isConverted = line.charAt(0) == '1';
				shot.kickNumber = kickNumber;
				if(shot.isConverted){
					if(shot.isHomeShot()){
						homeScore ++;
					}
					if(!shot.isHomeShot()){
						awayScore ++;
					}
				}
				shot.homeScore = homeScore;
				shot.awayScore = awayScore;

				shootout.add(shot);
				//				System.out.println(shot);
				kickNumber ++;
			}
			break;
		}
		if(!dv.checkGame("1", shootout)){
			if(isOldSequence(shootout)){
				System.out.println("Old sequence");
			}
			throw new RuntimeException("Invalid Shootout");
		}
		return shootout;
	}

	/*
	 * Checks if the shootout is of the form AAAAABBBB AB AB ..
	 */
	private boolean isOldSequence(List<Shot> shootout){
		if(shootout.size()<=5){
			return false;
		}

		//First 5 shots by team going first
		for(int i =1;i<=5;i++){
			if(shootout.get(i-1).homeShotFirst){
				if(shootout.get(i-1).awayScore!=0){
					return false;
				}
			}
			else{
				if(shootout.get(i-1).homeScore!=0){
					return false;
				}
			}
		}
		return true;
	}

}
