package pso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class DataFilter {


	public static void main(String[] args) throws IOException {
//		extractAERData();
	}

	
	private static void extractAERData() throws IOException, FileNotFoundException {
		String RAW_DATA_FILE="PSO.csv";
		Set<String> allowAll = new HashSet<String>();
		allowAll.add("Africa Cup"); 
		allowAll.add("EURO");
		allowAll.add("Gold Cup"); 
		allowAll.add("World Cup");
		allowAll.add("Copa América");
		allowAll.add("Supercup");

		Map<String, Integer> seasonWiseExtractor = new HashMap<String, Integer>();
		seasonWiseExtractor.put("Champions League",2000);
		seasonWiseExtractor.put("Europa League",2000);
		seasonWiseExtractor.put("DFB-Pokal",2001);
		seasonWiseExtractor.put("Copa del Rey",1999	);
		seasonWiseExtractor.put("FA Community Shield",9999);
		seasonWiseExtractor.put("FA Cup",9999);
		seasonWiseExtractor.put("League Cup",9999);

		
		
//		allowAll.add()

		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(Constants.weltFolder + "aer_data.csv")));
		CSVParser csvParser = CSVFormat.EXCEL.withHeader().parse(new FileReader(new File(Constants.weltFolder + RAW_DATA_FILE)));
		List<CSVRecord> records = csvParser.getRecords();
		for (CSVRecord record : records) {
			String competition = record.get("competition").trim();
			int year = Integer.parseInt(record.get("year"));
			String round = record.get("stage").trim();
					
			if(Integer.parseInt(record.get("year")) >2002 ){
				continue;
			}
			if(allowAll.contains(competition.trim())){
				writeRecord(record,csvWriter);
			}
			
			Integer firstFullSeason = seasonWiseExtractor.get(competition);
			if(firstFullSeason == null){
				continue;
			}
			if(year < firstFullSeason ){
				if(round.equals("Final")){
					writeRecord(record,csvWriter);
				}
			}
			else{
				writeRecord(record,csvWriter);
			}
		}
		csvWriter.flush();
	}

	public static void writeRecord(CSVRecord r,CSVWriter w){
		String[] recordArray = new String[r.toMap().size()];
		int i=0;
		for (String string : r) {
			recordArray[i++] = string;
		}
		w.writeNext(recordArray);
	}
}
