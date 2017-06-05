package com.aditya.research.pso.markovchain.transitions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.aditya.research.pso.common.Constants;
import com.aditya.research.pso.markovchain.MarkovChain;
import com.aditya.research.pso.markovchain.states.State;

public class DataBasedTransitionMatrix implements TransitionMatrix {
	Map<State, Float> probsByState = new HashMap<State, Float>();
	
	public DataBasedTransitionMatrix(String inputFile) throws FileNotFoundException, IOException {
		CSVParser csvParser = CSVFormat.EXCEL.withHeader().parse(new FileReader(new File(inputFile)));
		int k = 1;
		for (CSVRecord record : csvParser.getRecords()) {
			if(!record.get("kickNumber").equals("")){
				k = asInt(record.get("kickNumber"));
			}
			State s = State.byGDAndKickNumber(asInt(record. get("gd")), k - 1, Constants.defaultKickSequence());
			probsByState.put(s, Float.parseFloat(record.get("probability")));
		}
	}
	private int asInt(String s){
		return Integer.parseInt(s);
	}
	public float getTransitionProbability(State state, boolean isAShot) {
		if(!probsByState.containsKey(state.reducedState())){
			System.out.println(state.reducedState());
		}
		return probsByState.get(state.reducedState());
	}
	
}
