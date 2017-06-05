package com.aditya.research.nhl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NHLTimeBuckets extends TimeBuckets{
	Set<Integer> bucketBoundaries = new HashSet<Integer>();
	List<Integer> bucketList = new ArrayList<Integer>();
	
	public NHLTimeBuckets(List<Integer> bucketBoundaries) {
		super(new HashSet<Integer>(bucketBoundaries));
		for (Integer boundaryMinute : bucketBoundaries) {
			this.bucketBoundaries.add(boundaryMinute * 60);
			this.bucketList.add(boundaryMinute * 60);
		}
	}
	
	public boolean isBoundary(int seconds){
		return bucketBoundaries.contains(seconds);
	}
	
	@Override
	public int getBucketId(int seconds){
		return super.getBucketId((int) Math.ceil((float)seconds/60));
	}
	
	public static NHLTimeBuckets getDefaultBuckets(){
		return new NHLTimeBuckets(Arrays.asList(5,10,15,20,25,30,35,40,45,50,55,58,60));
	}
	
	public static void main(String[] args) {
		System.out.println(getDefaultBuckets().getBucketId(60*60));
		System.out.println(getDefaultBuckets().getBucketId(1));
		System.out.println(getDefaultBuckets().isBoundary(60*60-1));
	}
}
