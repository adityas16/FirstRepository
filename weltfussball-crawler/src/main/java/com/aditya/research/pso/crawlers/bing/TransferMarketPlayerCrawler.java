//package com.aditya.research.pso.crawlers.bing;
//
//import java.io.File;
//import java.io.FileReader;
//import java.text.DateFormatSymbols;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.NoSuchElementException;
//
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//
//import com.google.gson.Gson;
//
//import it.unipi.di.acube.searchapi.CachedSearchApi;
//import it.unipi.di.acube.searchapi.bing.BingSearchApi;
//import it.unipi.di.acube.searchapi.model.WebsearchResponse;
//import it.unipi.di.acube.searchapi.model.WebsearchResponseEntry;
//import pso.Constants;
//import pso.DBCacheDao;
//import pso.FileUtils;
//
//public class TransferMarketPlayerCrawler {
//	public static void main(String[] args) throws Exception {
//		Gson gson = new Gson();
//		DBCacheDao cache = new DBCacheDao("player_search", "transfermrkt");
//		BingSearchApi bing = new BingSearchApi("d88c8a81ffd24a268734e1a140172e08");
//		CachedSearchApi cached = new CachedSearchApi(bing, Constants.transermrktFolder + "bingCache");
//		CSVParser csvParser = CSVFormat.EXCEL.withHeader().parse(new FileReader(new File(Constants.weltFolder + "/extractedCSV/keepers.csv")));
//		List<CSVRecord> records = csvParser.getRecords();
//		Collections.shuffle(records);
//		List<Map<String,String>> outRecords = new ArrayList<Map<String,String>>();
//		
//		int i = 0;
//		boolean success = false;
//		for (CSVRecord record : records) {
//			Map<String, String> outRecord = new LinkedHashMap<String, String>();
//			WebsearchResponse response = null;
//			try{
//				response = gson.fromJson(cache.get("patricio-abraham"),WebsearchResponse.class);
//			}
//			catch(NoSuchElementException ne){
//				response = cached.query(createQuery(record));
//				cache.put(record.get("uri"), gson.toJson(response));
//			}
//			
//			for (WebsearchResponseEntry entry : response.getWebEntries()){
//				if(entry.getDisplayUrl().contains("profil")){
//					try{
//					outRecord.put("welt_uri",record.get("uri"));
//					String uri_name = entry.getDisplayUrl().split("/")[1].trim().replaceAll("\\p{C}", "");
//					String uri_id = entry.getDisplayUrl().split("/")[4].trim().replaceAll("\\p{C}", "");
//					outRecord.put("transfermrkt_id",uri_name + "_" + uri_id);
//					success = true;
//					break;
//					}
//					catch(Exception e){
//						continue;
//					}
//				}
//			}
//			if(!success){
//				System.err.println(createQuery(record));
//				continue;
//			}
//			success = false;
//			System.out.println(record.get("uri") + i);
//			outRecords.add(outRecord);
////			if(i++%10==0){
////				FileUtils.write(outRecords, Constants.transermrktFolder + "welt_transfermkt_mapping.csv");
////			}
//		}
//		FileUtils.write(outRecords, Constants.transermrktFolder + "welt_transfermkt_mapping.csv");
//	}
//
//	private static String createQuery(CSVRecord record) {
//		String name = record.get("name").replace('?', '\0');
//		String birth_year = null;
//		String birth_day = null;
//		String birth_month = null;
//		
//		try{
//			birth_year = record.get("birth_year");
//			birth_day = Integer.parseInt(record.get("birth_day")) + "";
//			birth_month = new DateFormatSymbols().getMonths()[Integer.parseInt(record.get("birth_month"))-1];
//			
//		}
//		catch(Exception e){
//			
//		}
//		return "transfermarkt.com:" + name+ " " + birth_year+ " " + birth_day + " " + birth_month;
//	}
//}
