package com.aditya.research.pso.dataentry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aditya.research.pso.etl.StringUtils;

import pso.DataValiditor;
import pso.Shot;

public class TemplateFileReader {
	private DataValiditor dv = DataValiditor.football();
	
	public static void main(String[] args) throws IOException {
//		List<String> lines = Files.readAllLines(Paths.get("/home/aditya/Research Data/DataEntry/PSO-data-input-template.csv"));
		TemplateFileReader tfr = new TemplateFileReader();
		tfr.checkShootouts();
	}

	private  void checkShootouts() throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("/home/aditya/CDL_DataEntry.csv"));
		Matcher m1 = null;
		Iterator<String> iter = lines.iterator();
		int validShootouts = 0,invalidShootouts =0;
		while(iter.hasNext()){
			Map<String, String> gameMap = new LinkedHashMap<String, String>();
			extractDate(iter, gameMap);
			try{
			extractTeams(iter, gameMap);
			List<Shot> shootout = extractShootout(iter);
			validShootouts++;
			}catch(Exception e){
				System.out.println("Invalid game : " + getGameName(gameMap));
				System.out.println(e.getMessage());
				invalidShootouts++;
			}
			
		}
		System.out.println("valid shootouts" + validShootouts);
		System.out.println("invalid shootouts" + invalidShootouts);
	}

	private String getGameName(Map<String, String> gameMap) {
		return gameMap.get("day") +"/"  + gameMap.get("month")+"/" + gameMap.get("year")+":" + gameMap.get("home") + gameMap.get("away");
	}

	private  void extractDate(Iterator<String> iter, Map<String, String> gameMap) {
		while(iter.hasNext()){
			String line = iter.next();
			Matcher m = Pattern.compile("date,(\\d+),(\\d+),(\\d+)").matcher(line);
			if(m.find()){
				gameMap.put("year", m.group(3));
				gameMap.put("month", m.group(2));
				gameMap.put("day", m.group(1));
				break;
			}
			else if(line.replace(",", "").length() > 0){
				//Searching for next date instead of throwing error
//				throw new RuntimeException("Couldn't find date");
			}
		}
	}
	
	private  void extractTeams(Iterator<String> iter, Map<String, String> gameMap) {
		while(iter.hasNext()){
			String line = iter.next();
			Matcher m = Pattern.compile("Team 1,(.*),(.*)").matcher(StringUtils.toSimpleCharset(line));
			if(m.find()){
				gameMap.put("home", m.group(1));
				break;
			}
			else if(line.replace(",", "").length() > 0){
				//Searching for next date instead of throwing error
				throw new RuntimeException("Couldn't find home team");
			}
		}
		
		while(iter.hasNext()){
			String line = iter.next();
			Matcher m = Pattern.compile("Team 2,(.*),(.*)").matcher(StringUtils.toSimpleCharset(line));
			if(m.find()){
				gameMap.put("away", m.group(1));
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

		while(iter.hasNext()){
			String line = iter.next();
			if(!line.contains("shootout")){
				continue;
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
		try{
		dv.checkGame("1", shootout);
		}
		catch(Exception e){
			throw new RuntimeException("Invalid kick sequence");
		}
		return shootout;
	}

}
