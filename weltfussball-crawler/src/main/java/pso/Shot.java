package pso;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aditya.research.pso.markovchain.states.State;

public class Shot{
	public String competition,gameId,gameName;
	public int homeScore,awayScore;
	public int kickNumber;
	public boolean isConverted,homeShotFirst;
	public int year;
	public String stage;
	public String uri;
	public boolean isHomeShot;
	public String striker="NA";

	public int getAScore(){
		return homeShotFirst?homeScore:awayScore;
	}

	public int getBScore(){
		return homeShotFirst?awayScore:homeScore;
	}
	
	public int gd(){
		return getAScore() - getBScore();
	}

	@Override
	public String toString() {
		return "Shot [gameId=" + gameId + ", gameName=" + gameName + ", homeScore=" + homeScore + ", awayScore="
				+ awayScore + ", kickNumber=" + kickNumber + ", homeShotFirst=" + homeShotFirst + "]";
	}

	public String[] fieldsAsString(){
		String [] strings = {gameId,competition,homeScore + "",awayScore + "" , asInt(isConverted) + "", asInt(homeShotFirst)+ "",kickNumber + "", gameName + "", year + "",stage, striker,uri};
		return strings;
	}

	public int round() {
		return (kickNumber-1)/2 + 1;
	}
	public static String[] headers(){
		String [] strings = {"game_id","competition","homeScore","awayScore" , "isConverted" , "homeShotFirst", "kickNumber", "gameName", "year","stage" ,"striker","uri"};
		return strings;
	}
	public Map<String,String> asMap(){
		Map<String, String> asMap = new LinkedHashMap<String, String>();
		asMap.put("game_id",gameId);
		asMap.put("competition",competition);
		asMap.put("homeScore",homeScore + "" );
		asMap.put("awayScore" ,awayScore + "" );
		asMap.put( "isConverted" ,asInt(isConverted) + "" );
		asMap.put( "homeShotFirst",asInt(homeShotFirst) + "" );
		asMap.put( "kickNumber",kickNumber + "" );
		asMap.put( "gameName",gameName);
		asMap.put( "year",year + "" );
		asMap.put("stage" ,stage);
		asMap.put("shooter",striker);
		asMap.put("uri",uri);
		asMap.put("isHomeShot",asInt(isHomeShot) + "");
		return asMap;
	}

	public static int asInt(boolean b){
		return b?1:0;
	}

	public boolean isAShot(){
		return kickNumber % 2 == 1;
	}
	
	public boolean isHomeShot(){
		return isAShot() && homeShotFirst || !isAShot() && !homeShotFirst;
	}

	public State previousState(){
		int a = getAScore(),b=getBScore();
		int na = kicksByA(),nb = kicksByB();

		if(isAShot()){
			na--;
			if(isConverted){
				a--;
			}
		}
		else{
			nb--;
			if(isConverted){
				b--;
			}
		}
		return new State(na,nb,a,b);
	}

	private int kicksByA(){
		return (kickNumber-1)/2 + 1;
	}

	private int kicksByB(){
		return (kickNumber)/2;
	}
	public State asState(){
		return new State(kicksByA(),kicksByB(), getAScore(),getBScore());
	}
}