package com.aditya.research.pso.etl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import pso.FileUtils;

public class StringUtils {
	public static int extractInt(String text){
		return Integer.parseInt(text.replaceAll("[^0-9]", ""));
	}
	
	public static String[] prependToStringArray(final String[] strArr,final String str){
//		List<String> strList= new ArrayList<String>();
//		strList.add(str);
//		strList.addAll(Arrays.asList(strArr));
//		return strList.toArray(new String[0]);
		return new ArrayList<String>() {
			  {
				    add(str);
				    addAll(Arrays.asList(strArr));
				  }
				}.toArray(new String[0]);
	}
	
	public static String toSimpleCharset(String s){
		try {
			String s1 = Normalizer.normalize(s, Normalizer.Form.NFKD);
	    String regex = "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+";
	    
			return new String(s1.replaceAll(regex, "").getBytes("ascii"), "ascii");
		} catch (Exception e) {
			return s;
		}
	}
	
	public static String extractResourceURI(String baseURI){
		return baseURI.split("/")[2].replace('/', ' ').trim();
	}
	
	public static int extractMonthNumberFromString(String month){
		Date date = new Date("1 " + month + "2010");
		return date.getMonth()+1;
	}
	
	public static void main(String[] args) throws IOException {
		List<String> URIList = Files.readAllLines(Paths.get("C:\\Users\\adity\\Desktop\\temp.csv"));
		for (String URI : URIList) {
			System.out.println(StringUtils.toSimpleCharset(URI));
		}
	}
}
