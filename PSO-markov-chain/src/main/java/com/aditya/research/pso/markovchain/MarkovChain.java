package com.aditya.research.pso.markovchain;

import java.util.Map.Entry;

import com.aditya.research.pso.common.Constants;
import com.aditya.research.pso.markovchain.states.State;
import com.aditya.research.pso.markovchain.states.StateSpace;
import com.aditya.research.pso.markovchain.states.StateSpaceList;
import com.aditya.research.pso.markovchain.transitions.ConstantTransitionMatrix;
import com.aditya.research.pso.markovchain.transitions.TransitionMatrix;

public class MarkovChain {
	public TransitionMatrix transitionMatrix = new ConstantTransitionMatrix();
	public boolean[] kickSequence = Constants.defaultKickSequence();
	
	public StateSpaceList run(State currentState){
		StateSpaceList markovChain = new StateSpaceList();

		for(int i=0;i < currentState.kicksTaken(); i++){
			markovChain.add(StateSpace.initialize(new State()));
		}
		
		markovChain.add(StateSpace.initialize(currentState));
		
		for(int kick = currentState.kicksTaken() + 1;kick<kickSequence.length;kick++){
			markovChain.add(new StateSpace());
			for (Entry<State,Float> stateEntry: markovChain.get(kick -1 )) {	
				if(stateEntry.getKey().isEndState()){
					markovChain.get(kick).addTransition(stateEntry.getKey(), stateEntry.getValue());
					continue;
				}
				boolean isAKick = kickSequence[kick];
				float scoringProbability = transitionMatrix.getTransitionProbability(stateEntry.getKey(), isAKick);
				markovChain.get(kick).addTransition(stateEntry.getKey().getNextState(isAKick, true), scoringProbability * stateEntry.getValue());
				markovChain.get(kick).addTransition(stateEntry.getKey().getNextState(isAKick, false), (1 - scoringProbability) * stateEntry.getValue());
			}
		}
		return markovChain;
	}
	public StateSpaceList run(){
		return run(new State());
	}

	
	public static void main(String[] args) {
		MarkovChain m = new MarkovChain();
		StateSpaceList fullSpace = m.run();
		System.out.println(fullSpace.get(10).stateDistribution);
		System.out.println(fullSpace.get(1).stateDistribution);
		System.out.println(fullSpace.get(2).stateDistribution);
		System.out.println(fullSpace.get(10).resultDistribution());
	}
}
