package pso.player;

import java.util.HashMap;
import java.util.Map;

import pso.player.Player.PlayerPosition;

public class PlayerPositionSanitizer {

	static Map<String, PlayerPosition> lookup = new HashMap<String, PlayerPosition>();
	
	static{
		lookup.put("goalkeeper", PlayerPosition.DEFENDER);
		lookup.put("sweeper", PlayerPosition.DEFENDER);
		lookup.put("back", PlayerPosition.DEFENDER);
		lookup.put("defender", PlayerPosition.DEFENDER);
		lookup.put("winger", PlayerPosition.FORWARD);
		lookup.put("forward", PlayerPosition.FORWARD);
		lookup.put("midfielder", PlayerPosition.MIDFIELDER);
	}
	
	public static String sanitize(String playerPosition){
		playerPosition = playerPosition.toLowerCase();
		if(lookup.containsKey(playerPosition)){
			return lookup.get(playerPosition).toString();
		}
		return "NA";
	}
}
