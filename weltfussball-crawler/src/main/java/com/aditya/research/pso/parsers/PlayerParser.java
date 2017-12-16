package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.aditya.research.pso.etl.StringUtils;

import pso.DBCache;
import pso.player.Player;

public class PlayerParser implements Parser{

	public List<Map<String,String>> parse(Document doc){
		Player p = new Player();

		p.name = StringUtils.toSimpleCharset(doc.title()).trim();

		setSummaryBoxInfo(doc, p);


		extractClubStats(doc, p);

		extractIntStats(doc, p);


		//		p.position = doc.getElementsByClass("sidebar").get(0).getElementsByClass("standard_tabelle").get(0).getElementsBy
		extractClubCareerTable(doc, p);

		extractYellowBox(doc, p);
		return Arrays.asList(p.asMap());
	}


	private void extractClubCareerTable(Document doc, Player p) {
		try{
			Node clubCareerTable = doc.select(":containsOwn(club career)").parents().get(0).siblingElements().get(0).childNode(1);
			Node clubStatsRow = clubCareerTable.childNode(1).childNode(1);
			p.setPosition(extractPositionFromClubStats(clubStatsRow));
			if(p.getPosition().equals("NA")){
				p.setPosition(extractPositionFromClubStats(clubCareerTable.childNode(1).childNode(3)));
			}
		}
		catch(Exception e){
			System.out.println("Club career table");
		}
	}


	private void extractYellowBox(Document doc, Player p) {
		p.fullName = doc.select("[class=standard_tabelle yellow]").get(0).select("tr").get(0).select("td").get(0).childNode(0).toString().trim();
		for (Element table : doc.select("[class=standard_tabelle yellow]")){
			for (Element row : table.select("tr")) {
				Elements tds = row.select("td");
				if(tds.get(0).children().size()==0){
					continue;
				}
				if(tds.get(0).child(0).text().contains("Nationality")){
					p.country = tds.get(1).childNode(3).childNode(0).toString().trim();
				}
				if(tds.get(0).child(0).text().contains("Position")){
					String exactPosition = tds.get(1).text();
					p.setPosition(exactPosition.split(" ")[exactPosition.split(" ").length-1]);
				}
				if(tds.get(0).child(0).text().contains("Born:")){
					if(tds.get(1).text().equals("")){
						continue;
					}
					String birthDate = tds.get(1).text().split(" ")[0];
					if(birthDate.split("\\.").length == 1){
						p.birthYear = Integer.parseInt(birthDate.trim()) + "";
						continue;
					}
					else{
						p.birthYear = Integer.parseInt(birthDate.split("\\.")[2].trim())+ "";
						p.birthMonth = Integer.parseInt(birthDate.split("\\.")[1].trim())+ "";
						p.birthDay = Integer.parseInt(birthDate.split("\\.")[0].trim())+ "";
					}
				}
			}
		}
	}


	private void extractIntStats(Document doc, Player p) {
		try{
			for(int i = 1;i<=10;i++){
				Elements box = doc.select("#site > div.white > div.content > div.portfolio.spec > div:nth-child(" + i + ")");
				if(box.size()==0){
					continue;
				}
				if(box.get(0).outerHtml().contains("Internationals")){
					Elements internationalStatsBox = doc.select("#site > div.white > div.content > div.portfolio.spec > div:nth-child(7) > div.data > table > tbody");
					p.internationalAggregate = extractAggregateStats(internationalStatsBox,"i");
				}
			}
		}
		catch(Exception e){
			System.out.println("No IntStats");
		}

	}


	private void extractClubStats(Document doc, Player p) {
		try{
			for(int i = 1;i<=10;i++){
				Elements box = doc.select("#site > div.white > div.content > div.portfolio.spec > div:nth-child(" + i + ")");
				if(box.size()==0){
					continue;
				}
				if(box.get(0).outerHtml().contains("Club matches")){
					Elements clubStatsBox = doc.select("#site > div.white > div.content > div.portfolio.spec > div:nth-child(" + i + ") > div.data > table > tbody");
					p.clubAggregate = extractAggregateStats(clubStatsBox,"c");
				}
			}
		}
		catch(Exception e){
			System.out.println("No club stats");
		}
	}


	private void setSummaryBoxInfo(Document doc, Player p) {
		try{
			Elements summaryBox = doc.select("#site > div.white > div.content > div.portfolio.spec > div:nth-child(4) > div.data > table > tbody > tr.dunkel > td:nth-child(2)");
			p.club = summaryBox.select(">a").attr("href").split("/")[2].replace('/', ' ').trim();
			p.country = summaryBox.select("div > img").attr("title");
			p.setPosition(summaryBox.select("div").get(0).childNodes().get(4).toString());
			p.firstYear = Integer.parseInt(summaryBox.select("div").get(0).childNodes().get(6).toString().split("-")[0].split("/")[1].trim()) + "";
		}
		catch(Exception e){
			System.out.println("No Summary");
		}
	}


	private Map<String, String> extractAggregateStats(Elements clubStatsBox,String type) {
		int n = clubStatsBox.get(0).childNodeSize();
		Node lastRowOfStats = clubStatsBox.get(0).childNode(n-2);
		Map<String, String> aggregateStats = new LinkedHashMap<String, String>();
		aggregateStats.put(type + "_matches", Integer.parseInt(lastRowOfStats.childNode(3).childNode(0).childNode(0).toString()) + "");
		aggregateStats.put(type + "_goals", Integer.parseInt(lastRowOfStats.childNode(5).childNode(0).childNode(0).toString()) + "");
		aggregateStats.put(type + "_start", Integer.parseInt(lastRowOfStats.childNode(7).childNode(0).childNode(0).toString()) + "");
		aggregateStats.put(type + "_sub-in",Integer.parseInt(lastRowOfStats.childNode(9).childNode(0).childNode(0).toString()) + "");
		aggregateStats.put(type + "_sub-out", Integer.parseInt(lastRowOfStats.childNode(11).childNode(0).childNode(0).toString()) + "");
		aggregateStats.put(type + "_yellow_card", Integer.parseInt(lastRowOfStats.childNode(13).childNode(0).childNode(0).toString()) + "");
		aggregateStats.put(type + "_2_yellow_card", Integer.parseInt(lastRowOfStats.childNode(15).childNode(0).childNode(0).toString()) + "");
		aggregateStats.put(type + "_red_card", Integer.parseInt(lastRowOfStats.childNode(17).childNode(0).childNode(0).toString()) + "");
		return aggregateStats;
	}


	private static String extractPositionFromClubStats(Node clubStatsRow) {
		return clubStatsRow.childNode(clubStatsRow.childNodeSize() - 2).childNode(0).toString();
	}

	public static void main(String[] args) throws IOException {
		DBCache pageCache = DBCache.weltPlayerCache();
		//
		PlayerParser pp = new PlayerParser();
		System.out.println(pp.parse(pageCache.get("theo-walcott")));
		pp.parse(pageCache.get("zlatan-ibrahimovic"));
		

		String s = "口水雞 hello Ä";

		String s1 = Normalizer.normalize(s, Normalizer.Form.NFKD);
		String regex = "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+";

		System.out.println(s1);
		String s2 = new String(s1.replaceAll(regex, "").getBytes("ascii"), "ascii");

		System.out.println(s2);
		System.out.println(s.length() == s2.length());
	}
}
