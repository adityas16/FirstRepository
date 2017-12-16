package com.aditya.research.pso.parsers;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

public interface Parser {
	public List<Map<String,String>> parse(Document doc);
}
