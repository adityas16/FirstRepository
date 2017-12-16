//package pso.player;
//
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.nodes.Node;
//import org.jsoup.select.Elements;
//
//import au.com.bytecode.opencsv.CSVWriter;
//import pso.Constants;
//import pso.FileUtils;
//import pso.Shot;
//import pso.FileSystemCache;
//import pso.player.Player.PlayerPosition;
//
//public class PlayerCrawler {
//	static FileSystemCache webPageCache = new FileSystemCache(Constants.weltFolder + "/players/", Constants.worldfootballURL + Constants.player_summary + "/");
//
//	public static Player parse(Document doc){
//		Player p = new Player();
//
//		//		p.position = doc.getElementsByClass("sidebar").get(0).getElementsByClass("standard_tabelle").get(0).getElementsBy
//		Node clubCareerTable = doc.select(":containsOwn(club career)").parents().get(0).siblingElements().get(0).childNode(1);
//		Node clubStatsRow = clubCareerTable.childNode(1).childNode(1);
//		p.setPosition(extractPositionFromClubStats(clubStatsRow));
//		if(p.getPosition().equals("NA")){
//			p.setPosition(extractPositionFromClubStats(clubCareerTable.childNode(1).childNode(3)));
//		}
//
//		if(p.getPosition().equals("NA")){
//			System.out.println("Attempting yellow box");
//			for (Element table : doc.select("[class=standard_tabelle yellow]")){
//				for (Element row : table.select("tr")) {
//					Elements tds = row.select("td");
//					if(tds.get(0).children().size()==0){
//						continue;
//					}
//					if(tds.get(0).child(0).text().contains("Position")){
//						String exactPosition = tds.get(1).text();
//						p.setPosition(exactPosition.split(" ")[exactPosition.split(" ").length-1]);
//					}
//				}
//			}
//		}
//		return p;
//	}
//
//	private static String extractPositionFromClubStats(Node clubStatsRow) {
//		return clubStatsRow.childNode(clubStatsRow.childNodeSize() - 2).childNode(0).toString();
//	}
//
//	public static Player parseURI(String uri) throws IOException{
//		return parse(webPageCache.get(uri));
//	}
//
//
//	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
//		PlayerCrawler.parseURI("souza_2");
//
//		CSVWriter writer = new CSVWriter(new FileWriter(Constants.weltFolder + "players.csv"));
//		writer.writeNext(Player.headers());
//
//		int count = 1;
//		Set<String> uniquePlayers = new HashSet<String>();
//		for (Shot shot : FileUtils.readAllShots("PSO_analysis.csv")) {
//			if(shot.striker.equals("NA")){
//				continue;
//			}
//			try{
//				Player p = PlayerCrawler.parseURI(shot.striker);
//				p.id = shot.striker;
//				if(!uniquePlayers.contains(p.id)){
//					writer.writeNext(p.fieldsAsString());
//					uniquePlayers.add(shot.striker);
//				}
//			}
//			catch(Exception e){
//				System.out.println(shot.striker);
//				continue;
//			}
//
//		}
//		writer.close();
//		System.out.println(uniquePlayers.size());
//		System.out.println("DONE");
//	}
//}
