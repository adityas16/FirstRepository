package com.aditya.research.pso.markovchain.transitions;

import com.aditya.research.pso.markovchain.states.State;

public class ConstandDifferenceTransitionMatrix implements TransitionMatrix {
	float diff;
	float base;
	
	public ConstandDifferenceTransitionMatrix(float diff, float base) {
		super();
		this.diff = diff;
		this.base = base;
	}



	public float getTransitionProbability(State state, boolean isAShot) {
		return isAShot?base+diff/2:base-diff/2;
	}

}
