package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.aditya.research.pso.etl.StringUtils;

import pso.DBCache;

public class PlayerPenaltyStatsParser  implements Parser{
	String success;
	String failure;
	
	
	private PlayerPenaltyStatsParser(String success, String failure) {
		super();
		this.success = success;
		this.failure = failure;
	}
	public static PlayerPenaltyStatsParser keeperParser(){
		return new PlayerPenaltyStatsParser("n_saved_penalties", "n_conceeded_penalties");
	}
	public static PlayerPenaltyStatsParser shooterParser(){
		return new PlayerPenaltyStatsParser("n_scored_penalties", "n_missed_penalties");
	}
	public List<Map<String, String>> parse(Document doc) {
		Map<String, String> asMap = new LinkedHashMap<String, String>();
		asMap.put(success, "NA");
		asMap.put(failure, "NA");
		
		extractPenaltyHeaders(doc, asMap);
		extractMarketValue(doc,asMap);
		extractTier(doc,asMap);
		return Arrays.asList(asMap);
	}
	private void extractPenaltyHeaders(Document doc, Map<String, String> asMap) {
		try{
		Elements scoredPenaltyHeader = doc.select("#main > div:nth-child(13) > div.large-8.columns > div:nth-child(1) > div.table-header > a");
		asMap.put(success, StringUtils.extractInt(scoredPenaltyHeader.get(0).childNode(0).toString()) + "");
		Elements missedPenaltyHeader = doc.select("#main > div:nth-child(13) > div.large-8.columns > div:nth-child(2) > div.table-header > a");
		asMap.put(failure, StringUtils.extractInt(missedPenaltyHeader.get(0).childNode(0).toString()) + "");
		}
		catch(Exception e){
			System.out.println("penalty header not found");
		}
	}
	private void extractTier(Document doc, Map<String, String> asMap) {
		asMap.put("league_level","NA");
		try{
			
		 String tier = doc.select("#main > div:nth-child(9) > div > div.dataHeader.dataExtended > div.dataZusatzbox > div.dataZusatzDaten > span:nth-child(6)").get(0).childNode(2).toString().trim();
		 asMap.put("league_level", StringUtils.extractInt(tier)+ "");
		}
		catch(Exception e){
			System.out.println("Market value not found");
		}
	}
	private void extractMarketValue(Document doc, Map<String, String> asMap) {
		asMap.put("market_value","NA");
		try{
		Elements marketValueBox = doc.select("#main > div:nth-child(9) > div > div.dataHeader.dataExtended > div.dataMarktwert");
		String val = marketValueBox.get(0).childNode(1).childNode(0).toString().trim();
		Long marketValue = Long.parseLong(val.replace(",", ""));
		String unit = marketValueBox.select(" > a > span").get(0).childNode(0).toString().trim();
		
		if(unit.contains("Th")){
			asMap.put("market_value", + (marketValue * 1000) + "");
		}
		else if(unit.contains("Mil")){
			asMap.put("market_value", + (marketValue * 1000000) + "");
		}
		else{
			throw new RuntimeException("Unknown market value unit" + unit);
		}
		}
		catch(Exception e){
			System.out.println("Market value not found");
		}
	}
	public static void main(String[] args) throws IOException {
			DBCache pageCache = DBCache.transfermrktPlayerPenaltyStatsCache();

			PlayerPenaltyStatsParser ppp = shooterParser();
			System.out.println(ppp.parse(pageCache.get("lionel-messi/elfmetertore/spieler/28003")));
			System.out.println(ppp.parse(pageCache.get("diederik-boer/elfmetertore/spieler/7288")));
			

	}
}
