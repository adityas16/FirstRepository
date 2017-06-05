package com.aditya.research.pso.markovchain;

import com.aditya.research.pso.common.Constants;
import com.aditya.research.pso.markovchain.states.State;
import com.aditya.research.pso.markovchain.transitions.ConstantTransitionMatrix;
import com.aditya.research.pso.markovchain.transitions.TransitionMatrix;

public class PSOModel {
	MarkovChain m  = new MarkovChain();
	float prRapidFire = 0.5f;
	float pbRapidFire = new ConstantTransitionMatrix().getTransitionProbability(new State(), true);
	TransitionMatrix rapidFireTransitionMatrix = new ConstantTransitionMatrix(); 
	
	public PSOModel() {
		m.kickSequence = Constants.defaultKickSequence();
	}
	
	public static PSOModel customModel(TransitionMatrix regular, TransitionMatrix rapidFire){
		PSOModel psoModel = new PSOModel();
		psoModel.m.transitionMatrix = regular;
		psoModel.rapidFireTransitionMatrix = rapidFire;
		return psoModel;
	}
	
	public float getProbAWin(int kicksCompleted, int a, int b){
		return getProbAWin(State.byKickNumberAndScore(kicksCompleted, a, b, Constants.defaultKickSequence()));
	}
	
	public float getProbAWin(State s){
		//In rapid fire
		if(s.kicksTaken()>10){
			return rapidFire(s);
		}
		return m.run(s).getFinalOutcomeDistribution().asBinomial(getRecursiveRapidFire()).getAWin();
	}

	private float rapidFire(State s) {
		//This needs to be updated based on the new rapid fire trnasition matrix
		throw new UnsupportedOperationException();
		
//		if(s.na > s.nb){
//			//A has taken the last shot
//			if(s.gd()>0){
//				//b scores, next round + b misses
//				return pbRapidFire * prRapidFire + (1-pbRapidFire);
//			}
//			else{
//				//B misses, go to next round
//				return (1-pbRapidFire) * prRapidFire;
//			}
//		}
//		else{
//			//B has taken the last shot
//			if(s.gd()>0){
//				return 1f;
//			}
//			else if(s.gd()<0){
//				return 0;
//			}
//			else{
//				return prRapidFire;
//			}
//		}
	}
	
	public float getRecursiveRapidFire(){
		float p0 = rapidFireTransitionMatrix.getTransitionProbability(new State(), true);
		float q0 = rapidFireTransitionMatrix.getTransitionProbability(new State(), false);
		float q_minus = rapidFireTransitionMatrix.getTransitionProbability(State.byGDAndKickNumber(-1,1, Constants.defaultKickSequence()), false);
		return p0*(1-q_minus)/(p0+q0-p0*q_minus-p0*q0);
	}
}
