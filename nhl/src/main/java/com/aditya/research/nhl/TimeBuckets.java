package com.aditya.research.nhl;

import java.util.HashSet;
import java.util.Set;

public class TimeBuckets {
	int bucketIds[];
	int noOfBuckets;
	int maxTime;
	public TimeBuckets(Set<Integer> bucketBoundaries) {
		maxTime = findMaxTime(bucketBoundaries);
		bucketIds = new int[maxTime + 1];
		noOfBuckets = bucketBoundaries.size();
		int currentId = 1;
		for(int i=1;i<=maxTime;i++){
			bucketIds[i] = currentId;
			if(bucketBoundaries.contains(i)){
				currentId++;
			}
		}
	}
	public static TimeBuckets createSingleBucket(int maxTime){
		Set<Integer> a = new HashSet<Integer>();
		a.add(maxTime);
		return new TimeBuckets(a);
	}
	
	public int getBucketId(int time) {
		return bucketIds[time];
	}

	public int getNoOfBuckets() {
		return noOfBuckets;
	}
	
	public int getMaxTime() {
		return maxTime;
	}

	private int findMaxTime(Set<Integer> bucketBoundaries){
		int maxTime = -1;
		for (Integer boundary : bucketBoundaries) {
			maxTime = Math.max(maxTime, boundary);
		}
		return maxTime;
	}
	
}
