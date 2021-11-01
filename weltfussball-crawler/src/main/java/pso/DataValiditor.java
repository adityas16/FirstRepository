package pso;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.aditya.research.pso.markovchain.states.AllPaths;
import com.aditya.research.pso.markovchain.states.State;

import au.com.bytecode.opencsv.CSVWriter;

public class DataValiditor {
	private static AllPaths allPaths;
	private static int numberOfRegularKicks;
	static String baseFolder;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		//Hockey params
//		numberOfRegularKicks = 6;
//		ScoreValidator.kicksPerTeam = numberOfRegularKicks / 2;
//		State.SHOTS_IN_REGULAR_PSO_PHASE = numberOfRegularKicks / 2;
//		allPaths = State.computeAllPaths(com.aditya.research.pso.common.Constants.iceHockeyKickSequence());
//		baseFolder = Constants.hockeyrefFolder;
		
		//soccer params
		numberOfRegularKicks = 10;
		ScoreValidator.kicksPerTeam = numberOfRegularKicks / 2;
		State.SHOTS_IN_REGULAR_PSO_PHASE = numberOfRegularKicks / 2;
		allPaths = State.computeAllPaths(com.aditya.research.pso.common.Constants.defaultKickSequence());
		baseFolder = Constants.weltFolder;
		
		Map<String, List<Shot>> validGames = getValidGames(baseFolder + "extractedCSV/pso_raw.csv");
		writeToFile(validGames, baseFolder + "processedCSV/pso_validated.csv");
	}

	public static Map<String, List<Shot>> getValidGames(String fileName) throws FileNotFoundException, IOException  {
		Map<String, List<Shot>> shotsByGame;
		Map<String, List<Shot>> validGames = new HashMap<String, List<Shot>>();
		shotsByGame = FileUtils.readShootoutsByMatch(fileName);
		
		Set<String> invalidGames = new HashSet<String>();

		for (Entry<String,List<Shot>> game : shotsByGame.entrySet()) {
			if(!checkGame(game.getKey(),game.getValue())){
				invalidGames.add(game.getKey().trim());
			}
			else{
				validGames.put(game.getKey(), game.getValue());
			}
		}
		System.out.println("Invalid Games : " + invalidGames.size());
		return validGames;
	}
	
	private static void writeToFile(Map<String, List<Shot>> validGames,String filename) throws IOException{
		CSVWriter writer = new CSVWriter(new FileWriter(filename));
		String[] headers = new String[1];
		
		
		boolean headersWritten = false;
		for (Entry<String,List<Shot>> game : validGames.entrySet()) {
			List<Map<String, String>> records = new ArrayList<Map<String,String>>();
			for (Shot shot: game.getValue()) {
				records.add(shot.asMap());
			}
			if(!headersWritten){
				headers = records.get(0).keySet().toArray(headers);
				writer.writeNext(headers);
				headersWritten = true;
			}
			String[] fields = new String[headers.length];
			for (Map<String, String> record : records) {
				int i=0;
				for (String header : headers) {
					fields[i++] = record.get(header);
				}
				writer.writeNext(fields);
			}
			writer.flush();
		}
		writer.close();
	}
	
	static int count = 0;
	public static boolean checkGame(String gameId, List<Shot> shots) {
		int a1=0,b1=0;
		boolean flag=true;
		List<State> gamePath = new ArrayList<State>();
		gamePath.add(new State());
		for (Shot shot : shots) {
			if(shot.kickNumber > numberOfRegularKicks){
				continue;
			}
			State current = shot.asState();
			gamePath.add(current);
		}
		if(!allPaths.exists(gamePath)){
			count ++;
			System.out.print("Game path incorrect : ");
			for (int i=1;i<gamePath.size();i++) {
				System.out.print(gamePath.get(i).a+ "" + gamePath.get(i).b);
			}
			System.out.print("  :" + gameId);
			System.out.println("");
			flag = false;
		}
		int noOfShots = shots.size();
		if(noOfShots > numberOfRegularKicks){
			if(noOfShots %2 ==1){
				System.out.println("Odd number of shots including rapid fire : " + gameId);
				flag = false;
			}
			if(shots.get(numberOfRegularKicks - 1).getAScore()!=shots.get(numberOfRegularKicks - 1).getBScore()){
				System.out.println("Rapid fire even though score not tied after numberOfRegularKicks shots : " + gameId);
				flag = false;
			}
			if(Math.abs(shots.get(noOfShots-1).getAScore() - shots.get(noOfShots-1).getBScore()) != 1){
				System.out.println("Rapid fire end goal difference != 1  : " + gameId);
				flag = false;
			}
			for(int i=10; i<shots.size()-4;) {
				if(Math.abs(shots.get(i+1).getAScore() - shots.get(i+1).getBScore()) == 1){
					flag = false;
				}
				i=i+2;
			}
		}
		return flag;
	}


}
