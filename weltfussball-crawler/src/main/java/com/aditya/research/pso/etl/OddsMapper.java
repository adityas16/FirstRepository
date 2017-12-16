package com.aditya.research.pso.etl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import pso.Constants;

public class OddsMapper {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Map<MyDate,Set<TeamPair>> primary = new HashMap<OddsMapper.MyDate, Set<TeamPair>>();
		Map<MyDate,Set<TeamPair>> secondary = new HashMap<OddsMapper.MyDate, Set<TeamPair>>();
		Map<String,String> teamNameMapping = new HashMap<String, String>();
		teamNameMapping.put("DAL", "Dallas Stars");
		Map<String,Integer> mappingHits = new HashMap<String, Integer>();
		Map<String,Integer> mappingMisses = new HashMap<String, Integer>();
		mappingHits.put("DAL", 10000);
		mappingMisses.put("DAL", 0);
		
		
		String baseFolder = Constants.NHLFolder + "/odds mapping/";
		primary = extractMatches(baseFolder + "primary.csv");
		secondary = extractMatches(baseFolder + "secondary.csv");
		
		List<Entry<MyDate, Set<TeamPair>>> random = new ArrayList<Entry<MyDate, Set<TeamPair>>>(primary.entrySet());
		Collections.shuffle(random);
		
		for(int i=1;i<=2;i++){
		for (Entry<MyDate, Set<TeamPair>> entry : random) {
			if(!secondary.containsKey(entry.getKey())){
				continue;
			}
			for (TeamPair primary_pair : entry.getValue()) {
				if(!mappingHits.containsKey(primary_pair.home)){
					mappingHits.put(primary_pair.home, 0);
					mappingMisses.put(primary_pair.home, 0);
				}
				if(!mappingHits.containsKey(primary_pair.away)){
					mappingHits.put(primary_pair.away, 0);
					mappingMisses.put(primary_pair.away, 0);
				}
				if(teamNameMapping.containsKey(primary_pair.home)){
					String mappedHomeTeam = teamNameMapping.get(primary_pair.home);
					for (TeamPair secondary_pair : secondary.get(entry.getKey())) {
						if(mappedHomeTeam.equals(secondary_pair.home)){
							if(teamNameMapping.containsKey(primary_pair.away)){
								if(!teamNameMapping.get(primary_pair.away).equals(secondary_pair.away)){
									if(mappingHits.get(primary_pair.home) < mappingMisses.get(primary_pair.home)){
										teamNameMapping.remove(primary_pair.home);
										mappingHits.put(primary_pair.home, 0);
										mappingMisses.put(primary_pair.home, 0);
									}
									if(mappingHits.get(primary_pair.away) < mappingMisses.get(primary_pair.away)){
										teamNameMapping.remove(primary_pair.away);
										mappingHits.put(primary_pair.away, 0);
										mappingMisses.put(primary_pair.away, 0);
									}
									mappingMisses.put(primary_pair.home, mappingMisses.get(primary_pair.home) + 1);
									mappingMisses.put(primary_pair.away, mappingMisses.get(primary_pair.away) + 1);
									continue;
								}
								else{
									teamNameMapping.put(primary_pair.away, secondary_pair.away);
									mappingHits.put(primary_pair.home, mappingHits.get(primary_pair.home) + 1);
									mappingHits.put(primary_pair.away, mappingHits.get(primary_pair.away) + 1);
								}
							}
							teamNameMapping.put(primary_pair.away, secondary_pair.away);
						}
					}
				}
			}
		}
		}
		System.out.println(teamNameMapping);
		for (Entry<String, String> mapping : teamNameMapping.entrySet()) {
			System.out.println(mapping.getKey() + "," + mapping.getValue() + "," + (float) (mappingHits.get(mapping.getKey()))/mappingMisses.get(mapping.getKey()));
		}
		System.out.println(mappingHits);
	}

	private static Map<MyDate,Set<TeamPair>> extractMatches(String inputFile) throws IOException, FileNotFoundException {
		CSVParser csvParser = CSVFormat.EXCEL.withHeader().parse(new FileReader(new File(inputFile)));
		List<CSVRecord> records = csvParser.getRecords();
		Map<MyDate,Set<TeamPair>> parsed = new HashMap<OddsMapper.MyDate, Set<TeamPair>>();
		for (CSVRecord record : records) {
			MyDate date = new MyDate(Integer.parseInt(record.get("day")),Integer.parseInt(record.get("month")),Integer.parseInt(record.get("year")));
			if(!parsed.containsKey(date)){
				parsed.put(date, new HashSet<TeamPair>());
			}
			parsed.get(date).add(new TeamPair(record.get("home_team"), record.get("away_team")));
		}
		return parsed;
	}
	
	private static class TeamPair{
		String home,away;

		public TeamPair(String home, String away) {
			super();
			this.home = home;
			this.away = away;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((away == null) ? 0 : away.hashCode());
			result = prime * result + ((home == null) ? 0 : home.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TeamPair other = (TeamPair) obj;
			if (away == null) {
				if (other.away != null)
					return false;
			} else if (!away.equals(other.away))
				return false;
			if (home == null) {
				if (other.home != null)
					return false;
			} else if (!home.equals(other.home))
				return false;
			return true;
		}
		
	}
	
	private static class MyDate{
		private int day,month,year;

		public MyDate(int day, int month, int year) {
			super();
			this.day = day;
			this.month = month;
			this.year = year;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + day;
			result = prime * result + month;
			result = prime * result + year;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MyDate other = (MyDate) obj;
			if (day != other.day)
				return false;
			if (month != other.month)
				return false;
			if (year != other.year)
				return false;
			return true;
		}
		
	}
}
