package com.aditya.research.pso.parsers.io;

import org.jsoup.nodes.Document;

public class DocumentWIthIdentifier {
	Document doc;
	String identifier;
	public DocumentWIthIdentifier(Document doc, String identifier) {
		super();
		this.doc = doc;
		this.identifier = identifier;
	}
	public Document getDoc() {
		return doc;
	}
	public String getIdentifier() {
		return identifier;
	}
	
}
