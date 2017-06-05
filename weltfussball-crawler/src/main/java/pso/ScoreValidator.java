package pso;

public class ScoreValidator {
	static int kicksPerTeam = 5;
	
	public static void assertTransition(int a1,int b1,int k,int a2,int b2){
		assertOneGoalPerKick(a1, b1, k, a2, b2);
		assertWithinStateSpace(a2, b2, k);
	}
	private static void assertOneGoalPerKick(int a1, int b1, int k, int a2, int b2) {
		if(isHomeKick(k)){
			if(b2!=b1){
				throw new RuntimeException("away scored on home kick");
			}
			else if(a2!=a1 && a2!=a1+1){
				throw new RuntimeException("home scored more than one goal");
			}
		}
		else if(!isHomeKick(k)){
			if(a2!=a1){
				throw new RuntimeException("home scored on away kick");
			}
			else if(b2!=b1 && b2!=b1+1){
				throw new RuntimeException("away scored more than one goal");
			}
		}
	}
	public static boolean isEndState(int Ascore,int Bscore,int kickNumber){
		return getMinGDofStrongerTeam(Ascore, Bscore, kickNumber)==1;
	}

	public static void assertValidity(int Ascore,int Bscore,int kickNumber){
		assertReachability(Ascore, Bscore, kickNumber);
		assertWithinStateSpace(Ascore, Bscore, kickNumber); 
	}

	private static void assertReachability(int Ascore,int Bscore,int kickNumber){
		if(Ascore > kicksByA(kickNumber) || Bscore > kicksByB(kickNumber)){
			throw new RuntimeException("Unreachable state.Not enough kicks");
		}
	}

	private static boolean isHomeKick(int kickNumber){
		return kickNumber % 2 == 1;
	}
	
	public static void assertWithinStateSpace(int Ascore,int Bscore,int kickNumber){
		int gd = getMinGDofStrongerTeam(Ascore, Bscore, kickNumber);
		if(gd>1){
			throw new RuntimeException("Beyond end state. Min GD" + gd);
		}
	}	
	public static int getMinGDofStrongerTeam(int Ascore,int Bscore,int kickNumber){
		if(Ascore > Bscore){
			return worstCaseHomeGD(Ascore, Bscore, kickNumber);
		}
		else if(Bscore > Ascore){
			return worstCaseAwayGD(Ascore, Bscore, kickNumber);
		}
		else{
			return 0;
		}
	}


	public static int worstCaseHomeGD(int Ascore,int Bscore,int kickNumber){
		return Ascore - Bscore - kicksLeftByB(kickNumber);
	}

	public static int worstCaseAwayGD(int Ascore,int Bscore,int kickNumber){
		return Bscore - Ascore - kicksLeftByA(kickNumber);
	}

	private static int kicksByA(int kickNumber){
		return (kickNumber-1)/2 + 1;
	}

	private static int kicksByB(int kickNumber){
		return kickNumber/2;
	}

	private static int kicksLeftByB(int kickNumber){
		return kicksPerTeam - kicksByB(kickNumber);
	}

	private static int kicksLeftByA(int kickNumber){
		return kicksPerTeam - kicksByA(kickNumber);
	}

	public static void main(String[] args) {
		System.out.println(getMinGDofStrongerTeam(1, 2, 4));
	}

}
