package com.aditya.research.pso.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {
	public static int extractIntFromString(String s){
		return Integer.parseInt(s.replaceAll("[^0-9]", ""));
	}
	
	public static List<String> shuffle(List<String> input){
		Set<String> asSet = new HashSet<String>(input);
		return shuffle(asSet);
	}
	
	public static List<String> shuffle(Set<String> input){
		List<String> asList = new ArrayList<String>(input);
		Collections.shuffle(asList);
		return asList;
	}
	
	public static void main(String[] args) {
		System.out.println(extractIntFromString("5. "));
	}
	public static int asInt(boolean b){
		return b?1:0;
	}
	
	public static void printRecords(List<Map<String, String>> records){
		for (Map<String, String> record : records) {
			System.out.println(record);
		}
	}

}
