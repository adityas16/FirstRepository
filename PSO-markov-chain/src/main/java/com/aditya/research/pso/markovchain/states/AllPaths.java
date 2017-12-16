package com.aditya.research.pso.markovchain.states;

import java.util.ArrayList;
import java.util.List;

public class AllPaths {
	List<List<State>> allPaths = new ArrayList<List<State>>();

	public void addPath(List<State> path) {
		allPaths.add(path);
	}
	
	public boolean exists(List<State> path){
		for (List<State> storedPath : allPaths) {
			if(storedPath.size() != path.size()){
				continue;
			}
			int i;
			for(i=0;i<path.size();i++){
				if(!storedPath.get(i).equals(path.get(i))){
					break;
				}
			}
			if(i==path.size()){
				return true;
			}
		}
		return false;
//		return allPaths.contains(path);
	}

	@Override
	public String toString() {
		return "AllPaths [allPaths=" + allPaths + "]";
	}
	
	
}
