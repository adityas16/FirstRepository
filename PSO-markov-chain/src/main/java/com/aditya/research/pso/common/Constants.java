package com.aditya.research.pso.common;

public class Constants {
	public static float scoringProportionFromData = 0.739f;
	
	
	
	public static boolean[] defaultKickSequence(){
		boolean[] kickSequence = {true,true,false,true,false,true,false,true,false,true,false};
		return kickSequence;
	}
	
	public static boolean[] iceHockeyKickSequence(){
		boolean[] kickSequence = {true,true,false,true,false,true,false};
		return kickSequence;
	}
}
