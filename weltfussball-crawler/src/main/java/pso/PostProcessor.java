package pso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aditya.research.pso.markovchain.PSOModel;
import com.aditya.research.pso.markovchain.states.State;

public class PostProcessor {
	static PSOModel psoModel = new PSOModel();
	private static Map<State, Float> getExpectedKicksLeftByState(Map<String, List<Shot>> shotsByGame){
		Map<State, Integer> totalKicks = new HashMap<State, Integer>();
		Map<State, Integer> numberOfOccurences = new HashMap<State, Integer>();
		Map<State, Float> expKicks = new HashMap<State, Float>();
		
		for (List<Shot> shots : shotsByGame.values()) {
			for(int i = 0; i<10 && i<shots.size();i++){
				State s = shots.get(i).previousState().reducedState();
				if(!totalKicks.containsKey(s)){
					totalKicks.put(s, 0);
					numberOfOccurences.put(s, 0);
				}
				totalKicks.put(s, totalKicks.get(s) + shots.size() - i);
				numberOfOccurences.put(s, numberOfOccurences.get(s) + 1);
				expKicks.put(s, ((float)totalKicks.get(s))/numberOfOccurences.get(s));
			}
		}
		return expKicks;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String myBaseFolder = Constants.combined;
		String fileName = myBaseFolder +  "processedCSV/pso_extended.csv";

		//m.transitionMatrix = new DataBasedTransitionMatrix(Constants.weltFolder + "TransitionMatrixData.csv");
		
//		Map<State, Float> expectedKicks = getExpectedKicksLeftByState(FileUtils.readShootoutsByMatch(fileName));
		
		List<Map<String,String>> results = new ArrayList<Map<String,String>>();
		
		for (Shot shot : FileUtils.readAllShots(fileName)) {
			Map<String,String> output = new LinkedHashMap<String, String>();
			output.put("uri", shot.uri);
			output.put("kickNumber", shot.kickNumber + "");

			
			output.put("round","NA");
			output.put("team_A_score_pre_shot","NA");
			output.put("team_B_score_pre_shot","NA");
			output.put("pr_A_pre_shot","NA");
			output.put("pr_A_if_scored","NA");
			output.put("pr_A_if_missed","NA");
			output.put("endIfScore","NA");
			output.put("endIfMiss","NA");
			
			State previous = shot.previousState();
			float pAPreShot = getProbAWin(previous);
			float pAIfscored = getProbAWin(previous.ifScored(shot.isAShot()));
			float pAIfMissed = getProbAWin(previous.ifNotScored(shot.isAShot()));
			int endIfScore = 0,endIfMiss=0;
			
			if(isCertain(pAIfscored)){
				endIfScore = 1;
			}
			if(isCertain(pAIfMissed)){
				endIfMiss = 1;
			}
			output.put("round",shot.round() + "");
			output.put("team_A_score_pre_shot",previous.a + "");
			output.put("team_B_score_pre_shot",previous.b + "");
			output.put("pr_A_pre_shot",pAPreShot+ "");
			output.put("pr_A_if_scored",pAIfscored + "");
			output.put("pr_A_if_missed",pAIfMissed + "");
			output.put("endIfScore",endIfScore + "");
			output.put("endIfMiss",endIfMiss + "");
			
			results.add(output);
		}
		FileUtils.write(results, myBaseFolder + "processedCSV/pso_model_output.csv");
	}
	private static boolean isCertain(float p){
		return p>0.9999f || p<0.00001f;
	}
	private static float getProbAWin(State s) {
		return psoModel.getProbAWin(s);
	}
}
