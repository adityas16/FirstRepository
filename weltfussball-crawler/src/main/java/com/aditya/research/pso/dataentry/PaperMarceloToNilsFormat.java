package com.aditya.research.pso.dataentry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pso.Constants;
import pso.FileUtils;
import pso.Shot;

public class PaperMarceloToNilsFormat {
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Map<String, List<Shot>> shotsByGame;
		Map<String, List<Shot>> validGames = new HashMap<String, List<Shot>>();
		shotsByGame = FileUtils.readShootoutsByMatch(Constants.BASE_FOLDER + "short_paper_data/" + "extractedCSV/pso_raw.csv");
		for (Entry<String,List<Shot>> game : shotsByGame.entrySet()) {
			int homeScore=0;
			int awayScrore=0;
			for (Shot shot : game.getValue()) {
				if(shot.isConverted) {
					if(shot.homeShotFirst) {
						//Team 1 is home team 
						if(shot.kickNumber %2 == 1) {
							//Team 1 is the one that scored
							homeScore++;
						}
						else {
							awayScrore++;
						}
					}
					else {
						//Team 2 is home team
						if(shot.kickNumber %2 == 0) {
							//Team 2 is the one that scored
							homeScore++;
						}
						else {
							awayScrore++;
						}
					}	
				}
				System.out.println("\""+ shot.uri+"\"" +","+ homeScore + "," + awayScrore);
			}
		}
	}
}
