package pso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import au.com.bytecode.opencsv.CSVWriter;

public class FileUtils {

	public static Map<String, List<Shot>> readShootoutsByMatch(String fileName) throws IOException, FileNotFoundException {
		CSVParser csvParser = CSVFormat.EXCEL.withHeader().parse(new FileReader(new File(fileName)));
		List<CSVRecord> records = csvParser.getRecords();
		Map<String, List<Shot>> shotsByGame = new LinkedHashMap<String, List<Shot>>();
		String currentGame = "";
		List<Shot> currentGameShots = new ArrayList<Shot>();

		for (CSVRecord record : records) {
			if(!record.get("gameName").equals(currentGame)){
				shotsByGame.put(currentGame, currentGameShots);
				currentGame = record.get("gameName");
				currentGameShots = new ArrayList<Shot>();
			}
			Shot s = new Shot();
			s.homeShotFirst = Integer.parseInt(record.get("homeShotFirst"))==1;
			s.isConverted = Integer.parseInt(record.get("isConverted"))==1;
			s.homeScore = Integer.parseInt(record.get("homeScore"));
			s.awayScore = Integer.parseInt(record.get("awayScore"));
			s.kickNumber = Integer.parseInt(record.get("kickNumber"));
			try {
				s.gameId = record.get("game_id");
			}
			catch(IllegalArgumentException i) {
				s.gameId = record.get("uri");
			}
			s.gameName = currentGame;
			try {
				s.striker = record.get("shooter");
			}
			catch(IllegalArgumentException i) {
				s.striker = record.get("striker");
			}
			s.stage = record.get("stage");
			s.year = Integer.parseInt(record.get("year"));
			s.competition = record.get("competition");
			s.uri = record.get("uri");
			currentGameShots.add(s);
		}
		shotsByGame.remove("");
		shotsByGame.put(currentGame, currentGameShots);
		return shotsByGame;
	}

	public static List<Shot> readAllShots(String fileName) throws FileNotFoundException, IOException{
		Map<String, List<Shot>> shotsByMatch = readShootoutsByMatch(fileName);
		List<Shot> allShots = new ArrayList<Shot>();
		for (List<Shot> gameShots : shotsByMatch.values()) {
			for (Shot shot : gameShots) {
				allShots.add(shot);
			}
		}
		return allShots;
	}

	public static void writeToCSV(String fileName,List<Shot> shots) throws IOException{
		CSVWriter writer = new CSVWriter(new FileWriter(fileName));
		writer.writeNext(shots.get(0).headers());

		for (Shot shot : shots) {
			writer.writeNext(shot.fieldsAsString());
		}

		writer.close();
	}
	public static void write(List<Map<String,String>> data,String fileName) throws IOException{
		CSVWriter writer = new CSVWriter(new OutputStreamWriter (new FileOutputStream(fileName), "UTF-8"));
		String[] headers = new String[1];
		headers = data.get(0).keySet().toArray(headers);
		writer.writeNext(headers);

		writeRecords(data, writer);
	}

	private static void writeRecords(List<Map<String, String>> data, CSVWriter writer) throws IOException {
		String[] headers = new String[1];
		headers = data.get(0).keySet().toArray(headers);
		
		String[] fields = new String[headers.length];
		for (Map<String, String> record : data) {
			int i=0;
			for (String header : headers) {
				fields[i++] = record.get(header);
			}
			writer.writeNext(fields);
		}
		writer.close();
	}
	public static void append(List<Map<String,String>> data,String fileName) throws IOException{
		CSVWriter writer = new CSVWriter(new OutputStreamWriter (new FileOutputStream(fileName,true), "UTF-8"));
		writeRecords(data, writer);
	}
}
