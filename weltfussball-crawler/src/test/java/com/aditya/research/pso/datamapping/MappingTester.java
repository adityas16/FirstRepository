package com.aditya.research.pso.datamapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.aditya.research.pso.etl.StringUtils;

public class MappingTester {
	Map<String, Entry<String, Integer>> map = new HashMap<String, Map.Entry<String,Integer>>();
	
	private int check(String a,String b,int rowNumber){
		a= a.toLowerCase();
		b = b.toLowerCase();
		
		a= a.replace("fc", "");
		b =b.replace("fc", "");
		
		a = StringUtils.toSimpleCharset(a.trim());
		b = StringUtils.toSimpleCharset(b.trim());
		a = a.trim();
		b= b.trim();
		if(!map.containsKey(a)){
			map.put(a, new AbstractMap.SimpleEntry<String,Integer>(b,rowNumber) );
		}
		if(!(map.get(a).getKey().equals(b))){
			System.out.println("got "+  b + " for " + a + ", had "+ map.get(a).getKey() + ", from line "  + map.get(a).getValue() + "," + rowNumber);
			return map.get(a).getValue();
		}
		return -1;
	}
	
	private void checkFile(String fileName) throws FileNotFoundException, IOException{

		CSVParser csvParser = CSVFormat.EXCEL.withHeader().parse(new FileReader(new File(fileName)));
		List<CSVRecord> records = csvParser.getRecords();
		int i = 2;
		for (CSVRecord record : records) {
			String a1,b1,a2,b2;
			a1 = record.get("homeTeam");
			b1 = record.get("ms_id_home_team");
			
			a2 = record.get("awayTeam");
			b2 = record.get("ms_id_away_team");
			
			int n;
			n = check(a1, b1, i);
			if(n != -1){
				if(check(a1, b2, i) != -1){
					System.out.println("Error");
				}
			}
			i++;
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		MappingTester mt = new MappingTester();
		mt.checkFile("/home/aditya/Research Data/other_papers/Reconciliation/ms_our_games.csv");
	}
}
