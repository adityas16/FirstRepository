package com.aditya.research.pso.markovchain.states;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.aditya.research.pso.common.TrinomialOutcome;

public class StateSpace implements Iterable<Entry<State,Float>>{
	public Map<State, Float> stateDistribution = new HashMap<State, Float>();
	
	public static StateSpace initialSpace(){
		StateSpace space = new StateSpace();
		space.stateDistribution.put(State.startState(), 1f);
		return space;
	}
	
	public static StateSpace initialize(State state){
		StateSpace space = new StateSpace();
		space.addTransition(state, 1f);
		return space;
	}
	
	public void addTransition(State state,float probability){
		
		float currentProbabilityOfState = 0f; 
		if(stateDistribution.containsKey(state)){
			currentProbabilityOfState = stateDistribution.get(state);
		}
		stateDistribution.put(state, currentProbabilityOfState + probability);
	}
	
	public Map<Integer, Float> gdDistribution(){
		Map<Integer, Float> gdDistribution = new HashMap<Integer, Float>();
		for (Entry<State, Float> stateEntry : this) {
			float currentProbabilityOfGD = 0f; 
			int gd = stateEntry.getKey().gd();
			if(gdDistribution.containsKey(gd)){
				currentProbabilityOfGD = gdDistribution.get(gd);
			}
			gdDistribution.put(gd, currentProbabilityOfGD + stateEntry.getValue());
		}
		return gdDistribution;
	}
	
	public TrinomialOutcome resultDistribution(){
		float pA=0,pT=0;
		for (Entry<State, Float> stateEntry : this) {
			float currentProbabilityOfResult = 0f;
			int gd = stateEntry.getKey().gd();
			int result = gd == 0 ? 0 : gd / Math.abs(gd);
			if(gd > 0){
				pA += stateEntry.getValue();
			}
			else if(gd == 0){
				pT += stateEntry.getValue();
			}
		}
		return new TrinomialOutcome(pA,pT);
	}
	
	public Iterator<Entry<State, Float>> iterator() {
		return stateDistribution.entrySet().iterator();
	}

	private float getpARapidFire(){
		return 0.5403535742f;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateSpace other = (StateSpace) obj;
		if (stateDistribution == null) {
			if (other.stateDistribution != null)
				return false;
		} else if (!stateDistribution.equals(other.stateDistribution))
			return false;
		return true;
	}
	
	
}
