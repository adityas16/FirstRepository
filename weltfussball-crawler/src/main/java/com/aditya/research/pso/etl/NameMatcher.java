package com.aditya.research.pso.etl;

import java.util.Arrays;
import java.util.List;

public class NameMatcher {

	public static Long computeDistance(String fullName, String partialName){
		String[] fullNameWords = fullName.split(" ");
		String[] partialNameWords = partialName.split(" ");
		Long totalDistance = 0L; 
		for (int i=0; i<  partialNameWords.length;i++) {
			String partialNameWord = partialNameWords[i];
			Long minDistance = new Long(partialNameWord.length());
			for (int j=0; j<  fullNameWords.length;j++) {
				String fullNameWord = fullNameWords[j];
				minDistance = Math.min(minDistance, LevenshteinDistance.levenshteinDistance(partialNameWord, fullNameWord));
			}
			totalDistance += minDistance;
		}
		return totalDistance;
	}
	
	public static void main(String[] args) {
		System.out.println(computeDistance(StringUtils.toSimpleCharset("Ângelo Mariano de Almeida"), "Angelo"));
	}
}
