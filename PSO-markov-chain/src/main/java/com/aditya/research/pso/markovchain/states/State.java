package com.aditya.research.pso.markovchain.states;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.aditya.research.pso.common.Constants;
import com.aditya.research.pso.markovchain.MarkovChain;
import com.sun.corba.se.impl.orbutil.closure.Constant;

public class State {
	public static int SHOTS_IN_REGULAR_PSO_PHASE = 5;
	private static Set<String> allFinalStates = new HashSet<>();
	public int na,nb,a,b;

	public State() {
	}

	public State(int na, int nb, int a, int b) {
		super();
		this.na = na;
		this.nb = nb;
		this.a = a;
		this.b = b;
	}

	public static State byGDAndKickNumber(int gd,int kickNumber,boolean[] kickSequence){
		State s= new State();
		for(int i=1;i<=kickNumber;i++){
			if(kickSequence[i]){
				s.na++;
			}
			else{
				s.nb++;
			}
		}
		if(gd>0){
			s.a = gd;
		}
		else if(gd<0){
			s.b = gd * -1;
		}
		return s;
	}
	
	public static State byKickNumberAndScore(int kicksCompleted,int a, int b,boolean[] kickSequence){
		State s= new State();
		for(int i=1;i<=kicksCompleted;i++){
			if(kickSequence[i]){
				s.na++;
			}
			else{
				s.nb++;
			}
		}
		s.a = a;
		s.b = b;
		return s;
	}
	
	public State getNextState(boolean isNextKickA,boolean isScored){
		if(isScored){
			return ifScored(isNextKickA);
		}else{
			return ifNotScored(isNextKickA);
		}
	}

	public State ifScored(boolean isNextKickA){
		if(isEndState()){
//			throw new UnsupportedOperationException();
		}
		if(isNextKickA){
			return new State(na+1,nb,a+1,b);
		}
		else{
			return new State(na,nb+1,a,b+1);
		}
	}

	public State ifNotScored(boolean isNextKickA){
		if(isEndState()){ 
//			throw new UnsupportedOperationException();
		}
		if(isNextKickA){
			return new State(na+1,nb,a,b);
		}
		else{
			return new State(na,nb+1,a,b);
		}
	}
	public State reducedState(){
		int g = Math.min(a, b);
		return new State(na,nb,a - g,b-g);
	}
	public int kicksTaken(){
		return na + nb;
	}
	
	public int getMinGDofStrongerTeam(){
		if(a > b){
			return worstCaseHomeGD();
		}
		else if(b > a){
			return worstCaseAwayGD();
		}
		else{
			return 0;
		}
	}

	public int gd(){
		return a - b;
	}

	private int worstCaseHomeGD(){
		return a - b - kicksLeftByB();
	}

	private int worstCaseAwayGD(){
		return b - a - kicksLeftByA();
	}

	private int kicksLeftByA(){
		return SHOTS_IN_REGULAR_PSO_PHASE - na;
	}

	private int kicksLeftByB(){
		return SHOTS_IN_REGULAR_PSO_PHASE - nb;
	}

	public boolean isEndState(){
		return getMinGDofStrongerTeam()==1;
	}

	static AllPaths allPaths = new AllPaths();
	public static AllPaths computeAllPaths(boolean[] kickSequence){
		allPaths = new AllPaths();
		Stack<State> path = new Stack<State>();
		path.push(new State());
		dfs(kickSequence,1,path);
		return allPaths;
	}

	private static void dfs(boolean[] kickSequence,int depth, Stack<State> path){
		if(depth == kickSequence.length){
			allPaths.addPath(new ArrayList<State>(path));
			allFinalStates.add(path.get(depth - 1).a + "," + path.get(depth - 1).b);
			return;
		}
		if(path.get(depth - 1).isEndState()){
			allPaths.addPath(new ArrayList<State>(path));
			allFinalStates.add(path.get(depth - 1).a + "," + path.get(depth - 1).b);
			return;
		}
		State next = path.get(depth - 1).ifScored(kickSequence[depth]);
		addToPath(kickSequence, depth, path, next);

		next = path.get(depth - 1).ifNotScored(kickSequence[depth]);
		addToPath(kickSequence, depth, path, next);
	}

	private static void addToPath(boolean[] kickSequence, int depth, Stack<State> path, State next) {
		path.add(next);
		dfs(kickSequence,depth + 1, path);
		path.pop();
	}

	@Override
	public String toString() {
		return "State [na=" + na + ", nb=" + nb + ", a=" + a + ", b=" + b + "]";
	}

	public static State startState(){
		return new State();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a;
		result = prime * result + b;
		result = prime * result + na;
		result = prime * result + nb;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (a != other.a)
			return false;
		if (b != other.b)
			return false;
		if (na != other.na)
			return false;
		if (nb != other.nb)
			return false;
		return true;
	}

	public static void main(String[] args) {
		boolean[] kickSequence = {true,true,false,true};
		//		System.out.println(State.computeAllPaths(kickSequence));
		//		System.out.println(State.computeAllPaths(MarkovChain.defaultKickSequence()));
		AllPaths allPaths = State.computeAllPaths(Constants.defaultKickSequence());
		for (List<State> path : allPaths.allPaths) {
			for (int i=1;i<path.size();i++) {
				System.out.print(path.get(i).a+ "" + path.get(i).b);
			}
			System.out.println(";");
		}
		System.out.println(allFinalStates);
	}

}
