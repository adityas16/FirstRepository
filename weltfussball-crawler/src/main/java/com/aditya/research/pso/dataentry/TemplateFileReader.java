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

import pso.Shot;

public class TemplateFileReader {
	public static void main(String[] args) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("/home/aditya/Research Data/DataEntry/PSO-data-input-template.csv"));
		Matcher m1 = null;
		Iterator<String> iter = lines.iterator();
		while(iter.hasNext()){
			Map<String, String> gameMap = new LinkedHashMap<String, String>();
			extractDate(iter, gameMap);
			List<Shot> shootout = extractShootout(iter);
			
		}
		System.out.println("done");
	}

	private static void extractDate(Iterator<String> iter, Map<String, String> gameMap) {
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
				throw new RuntimeException("Couldn't find date");
			}
		}
	}

	private static List<Shot> extractShootout(Iterator<String> iter) {
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
				System.out.println(shot);
				kickNumber ++;
			}
			break;
		}
		return shootout;
	}

}
