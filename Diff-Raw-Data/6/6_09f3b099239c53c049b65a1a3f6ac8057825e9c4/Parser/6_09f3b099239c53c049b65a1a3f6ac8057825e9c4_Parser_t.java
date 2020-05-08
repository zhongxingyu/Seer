 package com.github.geakstr.parser;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 public abstract class Parser<T> implements IParser<T> {
 
 	protected Document doc;
 	protected List<T> results;
 
 	public Parser(String link) throws IOException {
 		doc = Jsoup.connect(link).get();
		// File in = new File("dump.txt");
		// doc = Jsoup.parse(in, "UTF-8");
 		addAllResultsToList();
 	}
 
 	public Document getDoc() {
 		return doc;
 	}
 
 	public abstract void addAllResultsToList();
 
 	public List<T> getResults() {
 		return this.results.size() > 0 ? this.results : null;
 	}
 }
