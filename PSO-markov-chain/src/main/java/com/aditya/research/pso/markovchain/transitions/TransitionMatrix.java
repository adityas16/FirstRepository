package com.aditya.research.pso.markovchain.transitions;

import com.aditya.research.pso.markovchain.states.State;

public interface TransitionMatrix {
	float getTransitionProbability(State state,boolean isAShot);
}
