package pso.player;

import java.util.LinkedHashMap;
import java.util.Map;

public class Player {
	public String id,name="NA",club="NA",country="NA",fullName="NA";
	public Map<String, String> clubAggregate;
	public Map<String, String> internationalAggregate;
	private String position = "NA";

	public String firstYear="NA";
	public String birthYear="NA";
	public String birthMonth="NA";
	public String birthDay="NA";
	

	public Player() {
		String type = "c";

		clubAggregate = initStats("c");
		internationalAggregate = initStats("i");

	}

	public static enum PlayerPosition{
		FORWARD,MIDFIELDER,DEFENDER;
	}

	public String[] fieldsAsString(){
		String [] strings = {id,position};
		return strings;
	}

	public void setPosition(String pos){
		if(position.equals("NA")){
			this.position = PlayerPositionSanitizer.sanitize(pos);
		}
	}

	public Map<String,String> asMap(){
		Map<String, String> asMap = new LinkedHashMap<String, String>();
		asMap.put("name", name);
		asMap.put("full_name", fullName);
		asMap.put("position", position.toLowerCase());
		asMap.put("club", club);
		asMap.put("country", country);
		asMap.put("birth_year", birthYear + "");
		asMap.put("birth_month", birthMonth + "");
		asMap.put("birth_day", birthDay + "");
		asMap.put("first_year", firstYear + "");

		asMap.putAll(clubAggregate);
		asMap.putAll(internationalAggregate);

		return asMap;
	}

	public String getPosition() {
		return position;
	}

	public static String[] headers(){
		String [] strings = {"id","position"};
		return strings;
	}

	private  Map<String, String> initStats(String type) {
		Map<String, String> aggregateStats = new LinkedHashMap<String, String>();
		aggregateStats.put(type + "_matches", "NA");
		aggregateStats.put(type + "_goals", "NA");
		aggregateStats.put(type + "_start", "NA");
		aggregateStats.put(type + "_sub-in", "NA");
		aggregateStats.put(type + "_sub-out", "NA");
		aggregateStats.put(type + "_yellow_card", "NA");
		aggregateStats.put(type + "_2_yellow_card", "NA");
		aggregateStats.put(type + "_red_card", "NA");
		return aggregateStats;
	}

}
