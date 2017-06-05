package com.aditya.research.nhl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


public class SecondWiseGenerator {
	NHLTimeBuckets buckets = NHLTimeBuckets.getDefaultBuckets();	
	Queue<Integer> minorPenHome,otherPenHome;
	Queue<Integer> minorPenAway,otherPenAway;
	String currentGame = "";
	int homeScore,awayScore,timeElapsed,currentTime,bucketIndex;
	int noOfHomeSkaters,noOfAwaySkaters;
	int FULLTIME = 3600;
	CSVPrinter writer;
	Map<String, String> interval;
	
	Map<Integer, Integer> timeSpent = new LinkedHashMap<Integer, Integer>();
	Map<Integer, Integer> goalsScored = new LinkedHashMap<Integer, Integer>();
	
	public SecondWiseGenerator() throws IOException {
		this.writer = new CSVPrinter(new FileWriter("/home/aditya/nhl_aggregate.csv"),CSVFormat.EXCEL);
	}
	
	public void score() throws IOException {
		String fileName = "/home/aditya/nhl_goals_penls.csv";
		CSVParser csvParser = CSVFormat.EXCEL.withHeader().parse(new FileReader(new File(fileName)));
		initState();
		List<CSVRecord> records = csvParser.getRecords();
		currentGame = "";
		for (CSVRecord record : records) {
			if(!record.get("game_id").equals(currentGame)){
				if(timeElapsed>0){
					moveToGameEnd();
				}
				currentGame = record.get("game_id");
				initState();
			}
			int eventTime = Integer.parseInt(record.get("seconds"));
			moveForwardToTime(eventTime-1);
			updateBucketIndex();
			String eventType= record.get("etype");
			
			if(eventType.equals("GOAL")){
				processGoal(record);
			}
			if(record.get("etype").equals("PENL")){
				processPenalty(record);
			}
			
		}
		moveToGameEnd();
		writer.close();
	}

	private void moveForwardToTime(int toTime) throws IOException {
		if(toTime <= currentTime){
			return;
		}
		while(currentTime<=toTime){
			if(currentTime == FULLTIME){
				processGameEnd();
				return;
			}
			processTimeBuckets();
			currentTime++;
			timeSpent.put(getGD(), timeSpent.get(getGD()) + 1);
		}
	}

	private void processGameEnd() throws IOException {
		writeRecord();
	}
	private void moveToGameEnd() throws IOException{
		moveForwardToTime(FULLTIME);
	}
	private void initState() {
		homeScore = 0;
		awayScore = 0;
		timeElapsed = 0;
		bucketIndex = 0;
		currentTime = 0;
		noOfHomeSkaters = 6;
		noOfAwaySkaters = 6;
		minorPenHome = new PriorityQueue<Integer>();
		otherPenHome = new PriorityQueue<Integer>();

		minorPenAway = new PriorityQueue<Integer>();
		otherPenAway = new PriorityQueue<Integer>();
	}

	private void processGoal(CSVRecord record) throws IOException {
		boolean isHomeGoal = Integer.parseInt(record.get("side")) == 2;
		goalsScored.put(getGD(), goalsScored.get(getGD()) + 1);
		writeRecord(isHomeGoal,!isHomeGoal);
		if(isHomeGoal){
			homeScore++;
		}
		else{
			awayScore++;
		}
	}
	
	private void processPenalty(CSVRecord record) throws IOException {
		int pDuration = extractPenaltyDuration(record);
		if(record.get("side").equals("2")){
			updateHomePenaltyQueue(Integer.parseInt(record.get("seconds")), pDuration);
		}
		else{
			updateAwayPenaltyQueue(Integer.parseInt(record.get("seconds")), pDuration);
		}
		
	}

	private int extractPenaltyDuration(CSVRecord record) {
		String pString = record.get("type");
		Matcher m = Pattern.compile("(\\d+) min").matcher(pString);
		int pInt = 2;
		if(m.find()){
			pInt = Integer.parseInt(m.group(1));
		}
		return pInt;
	}
	
	
	private void processTimeBuckets() throws IOException {
		if(buckets.isBoundary(currentTime)){
			processGameEnd();
		}
		updateBucketIndex();
	}
	private int getGD(){
		int gd = homeScore -awayScore;
		gd = Math.max(-3, gd);
		gd = Math.min(3, gd);
		return gd;
	}
	private void updateBucketIndex() {
		bucketIndex = buckets.getBucketId(currentTime);
	}
	private void writeRecord() throws IOException{
		writeRecord(false, false);
	}
	private void writeRecord(boolean isHomeGoal,boolean isAwayGoal) throws IOException{
		writer.printRecord(intervalAsMap(isHomeGoal, isAwayGoal).values());
		timeElapsed = currentTime;
	}
	private Map<String, String> intervalAsMap(boolean isHomeGoal,boolean isAwayGoal){
		int duration = currentTime -timeElapsed;
		interval = new LinkedHashMap<String, String>();
		interval.put("game_id", currentGame + "");
		interval.put("time", duration + "");
		interval.put("home_score", homeScore + "");
		interval.put("away_score", awayScore + "");
		interval.put("bucket_index", bucketIndex + "");
		interval.put("is_home_goal", (isHomeGoal?1:0) + "");
		interval.put("is_away_goal", (isAwayGoal?1:0) + "");
		return interval;
	}

	private void updateHomePenaltyQueue(int time,int type){
		switch (type) {
		case 2:
			minorPenHome.add(time + 2*60);
			break;
		case 4:
		case 5:
		case 10:
			otherPenHome.add(time + type*60);
			break;
		default:
			throw new RuntimeException("Invalid penalty type");
		}
	}

	private void updateAwayPenaltyQueue(int time,int type){
		switch (type) {
		case 2:
			minorPenAway.add(time + 2*60);
			break;
		case 4:
		case 5:
		case 10:
			otherPenAway.add(time + type*60);
			break;
		default:
			throw new RuntimeException("Invalid penalty type");
		}
	}

	public static void main(String[] args) throws IOException {
		SecondWiseGenerator generator = new SecondWiseGenerator();
		for(int i=-3;i<=3;i++){
			generator.timeSpent.put(i, 0);
			generator.goalsScored.put(i, 0);
		}
		generator.score();
		System.out.println(generator.interval.keySet());
		System.out.println(generator.goalsScored);
		System.out.println(generator.timeSpent);
		
		float gd0 = (float)generator.goalsScored.get(0) / (float)generator.timeSpent.get(0) * 3600;
		System.out.println(gd0);
		for(int i=-3;i<=3;i++){
			System.out.println(((float)generator.goalsScored.get(i) / (float)generator.timeSpent.get(i) * 3600 )/ gd0);
		}
	}
}
