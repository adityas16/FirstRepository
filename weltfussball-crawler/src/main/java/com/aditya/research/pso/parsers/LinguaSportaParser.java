package com.aditya.research.pso.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aditya.research.pso.etl.StringUtils;

import pso.Constants;

public class LinguaSportaParser {
	static Set<String> allDates = new HashSet<String>();
	static final String MUNDO_PORTIVO_URL = "http://hemeroteca-paginas.mundodeportivo.com/EMD01/HEM/";

	public static void main(String[] args) throws IOException, ParseException {
		//Should have 397 shootouts
		parseDetailsOfMatchesWithPSO();
		System.out.println("got " + allDates.size() + "days");

//		for (String date : Utils.shuffle(allDates)) {
//			date = increment(date);
//
//			String[] dateElements = date.split("-");
//			String day = String.format("%02d", Integer.parseInt(dateElements[0]));
//			String month = String.format("%02d", Integer.parseInt(dateElements[1]));
//			String year = dateElements[2];
//			for(int i=1;i<=200;i++){
//				String page = String.format("%03d", i);
//				String url = MUNDO_PORTIVO_URL + year+ "/" + month+ "/" + day + "/MD" + year+ month + day + "-" + page + ".pdf";
//				File outFile = new File(Constants.linguaSportFolder + "/archives/" + year+ month + day + "/" + i + ".pdf");
//				outFile.getParentFile().mkdirs();
//				if(outFile.exists()){
//					continue;
//				}
//				try{
//					FileUtils.copyURLToFile(new URL(url), outFile);
//				}
//				catch (FileNotFoundException f) {
//					break;
//				}
//			}
//		}
	}
	//		parseRoundInfoOfMatchesWithPSO();

	private static String increment(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy");
		Calendar c = Calendar.getInstance();
		c.setTime(sdf.parse(date));
		c.add(Calendar.DATE, 1);  // number of days to add
		date = sdf.format(c.getTime());  // dt is now the new date
		return date;
	}

	private static void parseDetailsOfMatchesWithPSO() throws IOException {
		Pattern p1 = Pattern.compile(">([\\p{Alnum}\\p{Blank}]*)</span>");
		Pattern p2 = Pattern.compile("\\(((\\d+)-.*)\\)");
		Pattern p3 = Pattern.compile("((\\d+)-(\\d+)-(\\d+))");
		Pattern p4 = Pattern.compile("<font size=\"(\\d+)\">(.*) - ([\\p{Alnum}\\p{Blank}]*)</font>");

		Matcher m;
		for(int i =71;i<=117;i++){
			//			System.out.println("Reading file : " + i);
			File f = new File("/home/aditya/Research Data/linguasport/"+i+".htm");
			Document doc = Jsoup.parse(f, "UTF-8", "http://example.com/");

			Elements roundTables = doc.getElementsByClass("MsoTableWeb3");

			if(roundTables.size()==0){
				roundTables = doc.getElementsByClass("MsoNormalTable");
			}
			for (int j=0;j<roundTables.size();j++) {
				Element roundTable = roundTables.get(j);
				boolean hasSecondLeg = true;
				String secondLegDate = "";
				try{
					Matcher m1 = p2.matcher(roundTable.toString());
					if(!m1.find()){
						m1 = Pattern.compile("(\\d+-\\d+-\\d+)").matcher(roundTable.toString());
						m1.find();
					}
					secondLegDate = m1.group(1);
					try{
						secondLegDate = secondLegDate.split("/")[1].replace(")","").trim();	
					}
					catch(Exception e){
						hasSecondLeg = false;
						secondLegDate = secondLegDate.replace(")","").replace("(","").trim();
					}
				}
				catch(Exception e){
					System.out.println("Skipped round" + i + ", round" + j);
					continue;
				}

				Elements matches = roundTable.select("tr");
				
				for (Element match : matches) {
					try{
						String teamStrings = match.select("td:nth-child(1)").toString();
						teamStrings = extractTeams(teamStrings);
						String homeTeam = teamStrings.split("-")[0].trim();
						String awayTeam = teamStrings.split("-")[1].trim();
						
						int tdPenalty = hasSecondLeg?4:3; 
						boolean isPSO = match.select("td:nth-child(" + tdPenalty +")").toString().contains("pp:");
						if(isPSO){
							//							System.out.println(teamStrings);
							//							System.out.println(match.select("td:nth-child(1)").get(0).childNode(1).childNode(0).childNode(0).toString());
							allDates.add(secondLegDate);
							System.out.println(dateToCSV(secondLegDate) + "," + homeTeam + ","  + awayTeam + "," + (1900 + i));
						}
					}
					catch(Exception e){
						continue;
					}
				}

				//New Roman&quot;">Paiporta CF <span style="
			}

			Element finals = doc.getElementsByAttributeValue("bgcolor","#6633CC").parents().get(0);
			m = p3.matcher(finals.toString());
			m.find();
			String date = m.group(1);
			boolean isPSO =  finals.select("b:contains(pp:)").size()>0;
			if(isPSO){
				allDates.add(new String(date));
				String finalsString = finals.select("b:contains(pp:)").parents().get(5).toString();
				m = p4.matcher(finalsString);
				finalsString = extractTeams(finalsString);
				String homeTeam = finalsString.split("-")[0].trim();
				String awayTeam = finalsString.split("-")[1].trim().split("\n")[0].trim();
				System.out.println(dateToCSV(date) + "," + homeTeam + ","  + awayTeam+ "," + (1900 + i));
			}
		}
	}

	private static String extractTeams(String teamStrings) {
		while(true){
			int close = teamStrings.lastIndexOf('>');
			int open = teamStrings.lastIndexOf('<');
			if(close == -1){
				break;
			}
			teamStrings = teamStrings.substring(0, open) + teamStrings.substring(close+1,teamStrings.length());
		}
		return teamStrings;
	}

	private static String dateToCSV(String date) {
		String[] dateElements = date.split("-");
		date = dateElements[0] + "," + dateElements[1] + "," + dateElements[2];
		return date;
	}


	private static void parseRoundInfoOfMatchesWithPSO() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("1/16", "Round of 32");
		map.put("1/8", "Pre Quarter Final");
		map.put("1/4", "Quarter Final");
		map.put("1/2", "Semi Final");
		map.put("FINAL", "Final");

		for(int i =71;i<=117;i++){
			List<String> all = new ArrayList<String>();

			//			System.out.println("wget www.linguasport.com/futbol/nacional/copa/copa" + i + ".htm -O " + i + ".htm");
			List<String> lines = Files.readAllLines(Paths.get("/home/aditya/Research Data/linguasport/"+i+".htm"));

			Pattern p1 = Pattern.compile("normal'>(\\S+) RONDA");
			Pattern p2 = Pattern.compile("pp:\\d+-\\d+");
			Pattern p3 = Pattern.compile("normal'>(\\S+) FINAL<o:p></o:p></span></font></b></p>");
			Pattern p4 = Pattern.compile("<font size=\"4\">(\\S+)</font></b>");

			String current_round = "";
			for (String line : lines) {
				Matcher m = p1.matcher(line);
				if (m.find()) {
					current_round = m.group(1);
				}
				m = p3.matcher(line);
				if (m.find()) {
					current_round = m.group(1);
				}
				m = p4.matcher(line);
				if (m.find()) {
					current_round = m.group(1);
				}
				m = p2.matcher(line);
				if (m.find()) {
					if(current_round.equals("")){
						System.out.println("issue");
					}
					if(map.containsKey(current_round)){
						//						System.out.println(map.get(current_round));
						all.add(map.get(current_round));
					}
					else{
						all.add(current_round);
					}

				}
			}
			Collections.reverse(all);
			System.out.println(i-1);
			for (String round : all) {
				System.out.println(round);
			}
			all = new ArrayList<String>();
		}
	}
}
