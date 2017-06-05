package com.aditya.research.pso.markovchain.transitions;

import com.aditya.research.pso.markovchain.states.State;

public class ConstantTransitionMatrix implements TransitionMatrix {

	public float getTransitionProbability(State state, boolean isAShot) {
		return 0.74f;
	}

}
