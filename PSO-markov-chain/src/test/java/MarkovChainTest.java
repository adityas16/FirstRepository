import java.util.List;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.aditya.research.pso.common.Constants;
import com.aditya.research.pso.markovchain.MarkovChain;
import com.aditya.research.pso.markovchain.PSOModel;
import com.aditya.research.pso.markovchain.states.State;
import com.aditya.research.pso.markovchain.states.StateSpace;
import com.aditya.research.pso.markovchain.states.StateSpaceList;
import com.aditya.research.pso.markovchain.transitions.ConstandDifferenceTransitionMatrix;
import com.aditya.research.pso.markovchain.transitions.ConstantTransitionMatrix;
import com.aditya.research.pso.markovchain.transitions.TransitionMatrix;

public class MarkovChainTest {
	MarkovChain markovChain = new MarkovChain();
	final float TOLERANCE = 0.0001f;
	
	@Before
	public void init(){
		markovChain = new MarkovChain();
	}
	@Test
	public void testOneSTep(){
		boolean[] kickSequence = {true,true};
		markovChain.kickSequence = kickSequence;
		StateSpaceList stateSpaces = markovChain.run();
		Assert.assertEquals(new ConstantTransitionMatrix().getTransitionProbability(new State(), true), stateSpaces.get(1).stateDistribution.get(new State(1,0,1,0)), TOLERANCE);
		assertSumOfAllSpaces(stateSpaces);
	}
	
	
	@Test
	public void testFromGivenStartState(){
		Assert.assertEquals(markovChain.run().getFinalOutcomeDistribution().getAWin(), markovChain.run(new State()).getFinalOutcomeDistribution().getAWin(),0.00001f);
	}
	
		
	
	@Test
	public void testWithCustomTransitionProbs(){
		TransitionMatrix customTransitionMatric = new TransitionMatrix() {
			public float getTransitionProbability(State state, boolean isAShot) {
				return state.gd()>0f?0.7f:0.4f;
			}
		};
		
		markovChain.transitionMatrix = customTransitionMatric;
		float pAWinRapidFire = 0.5403535742f;
		Assert.assertEquals(0.4071622788f, markovChain.run().get(10).resultDistribution().asBinomial(pAWinRapidFire).getAWin(),TOLERANCE);
		
		Assert.assertEquals(0.1199904074f, markovChain.run(new State(3,2,0,1)).get(10).resultDistribution().asBinomial(pAWinRapidFire).getAWin(),TOLERANCE);
	}
	
	private float probAwin(StateSpaceList ssl){
		return ssl.get(10).resultDistribution().asBinomial(0.5f).getAWin();
	}
	private void assertSumOfAllSpaces(StateSpaceList allSpaces){
		for (StateSpace space : allSpaces) {
			Assert.assertEquals(1, getSum(space),TOLERANCE);
		}
	}
	
	private float getSum(StateSpace space){
		float sum = 0;
		for (Entry<State, Float> entry : space) {
			sum += entry.getValue();
		}
		return sum;
	}
	
	
	//Analysis Tests
	
	@Test
	public void analyzeExtraKick(){
		float probATakesExtraKick=0,probATakesExtraKickAndWins=0;
		
		for (Entry<State, Float> stateEntry : markovChain.run().get(Constants.defaultKickSequence().length-1).stateDistribution.entrySet()) {
			if(stateEntry.getKey().na > stateEntry.getKey().nb){
				probATakesExtraKick+= stateEntry.getValue();
				if(stateEntry.getKey().gd() > 0){
					probATakesExtraKickAndWins += stateEntry.getValue();
				}
			}
		}
		System.out.println(probATakesExtraKick);
		System.out.println(probATakesExtraKickAndWins / probATakesExtraKick);
	}
	
	@Test
	public void noOfTies(){
		float probTie=0;
		
		for (Entry<State, Float> stateEntry : markovChain.run().get(Constants.defaultKickSequence().length-1).stateDistribution.entrySet()) {
			if(stateEntry.getKey().a == stateEntry.getKey().b){
				probTie+= stateEntry.getValue();
			}
		}
		System.out.println(probTie);
	}
	
	@Test
	public void findScoringProbabilities(){
		float probAScore=0;
		float probBScore=0;
		float shotsA=0,shotsB=0,goalsA=0,goalsB=0;
		
		for (Entry<State, Float> stateEntry : markovChain.run().get(Constants.defaultKickSequence().length-1).stateDistribution.entrySet()) {
			probAScore += ((float)stateEntry.getKey().a / (float)stateEntry.getKey().na * stateEntry.getValue());
			probBScore += ((float)stateEntry.getKey().b / (float)stateEntry.getKey().nb * stateEntry.getValue());
//			if(stateEntry.getKey().a == stateEntry.getKey().b){
//				probAScore+= stateEntry.getValue();
//			}
			shotsA += (float)stateEntry.getKey().na * stateEntry.getValue();
			shotsB += (float)stateEntry.getKey().nb * stateEntry.getValue();
			goalsA += (float)stateEntry.getKey().a * stateEntry.getValue();
			goalsB += (float)stateEntry.getKey().b * stateEntry.getValue();
		}
		System.out.println(probAScore);
		System.out.println(probBScore);
		System.out.println(goalsA/shotsA);
		System.out.println(goalsB/shotsB);
	}

}
