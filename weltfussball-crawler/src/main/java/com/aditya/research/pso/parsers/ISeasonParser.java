package com.aditya.research.pso.parsers;

import java.io.IOException;
import java.util.List;

import org.jsoup.nodes.Document;

public interface ISeasonParser {
	public List<String> parse(Document doc) throws IOException;
}
