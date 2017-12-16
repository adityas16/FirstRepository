package com.aditya.research.pso.markovchain.states;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aditya.research.pso.common.TrinomialOutcome;

public class StateSpaceList implements Iterable<StateSpace>{
	List<StateSpace> stateSpaces = new ArrayList<StateSpace>();
	
	public StateSpace get(int index){
		return stateSpaces.get(index);
	}

	public void add(StateSpace space) {
		stateSpaces.add(space);
	}

	public Iterator<StateSpace> iterator() {
		return stateSpaces.iterator();
	}
	
	public TrinomialOutcome getFinalOutcomeDistribution(){
		return stateSpaces.get(stateSpaces.size() - 1).resultDistribution();
	}
}
