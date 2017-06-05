package com.aditya.research.pso.etl;

public class RoundNameParser {


	public static int findDistanceFromFinal(String roundName) throws RuntimeException{
		roundName = roundName.toLowerCase();
		if(roundName.contains("semi") | roundName.equals("third place")){
			return 1;
		}
		else if(roundName.contains("quarter")){
			return 2;
		}
		else if(roundName.equals("final") || roundName.equals("finals")){
			return 0;
		}
		else{
			return 3;
		}
		
//		int noOfTeamsInRound = Integer.parseInt(roundName.replaceAll("[^0-9]", ""));
//		return (int) Math.round(Math.log(noOfTeamsInRound));
	}
}
