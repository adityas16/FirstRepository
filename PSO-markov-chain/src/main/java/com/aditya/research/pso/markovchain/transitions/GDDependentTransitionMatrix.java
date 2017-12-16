package com.aditya.research.pso.markovchain.transitions;

import com.aditya.research.pso.markovchain.states.State;

public class GDDependentTransitionMatrix implements TransitionMatrix{
	
	public float getTransitionProbability(State state, boolean isAShot) {
		return state.gd()>0f?0.7f:0.4f;
	}

}
