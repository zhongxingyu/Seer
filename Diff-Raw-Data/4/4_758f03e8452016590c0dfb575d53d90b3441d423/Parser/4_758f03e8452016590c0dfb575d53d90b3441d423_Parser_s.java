 /*******************************************************************************
  * Copyright (c) 2013 sgelb.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sgelb - initial API and implementation
  ******************************************************************************/
 package com.github.sgelb.springerlinkdownloader.model;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Connection;
 import org.jsoup.HttpStatusException;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.github.sgelb.springerlinkdownloader.helper.NoAccessException;
 
 public class Parser {
 	private String url;
 	private String urlBase;
 	private Book book;
 	private Document doc = null;
 	private TreeMap<String, URL> chapters = new TreeMap<>();
 
 	public Parser(String url, Book book) {
 		System.setProperty("java.net.useSystemProxies", "true");
 		this.book = book;
 		if (!url.startsWith("http://")) {
 			url = "http://" + url;
 		}
 		this.url = url.replaceAll("/page/\\d+$", "");
 		this.urlBase = url + "/page/";
 	}
 
 	public void parseHtml() throws NoAccessException, HttpStatusException,
 			IOException {
 
 		Connection con = Jsoup.connect(url);
 		con.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");
 		doc = con.timeout(5000).get();
 
 		if (!doc.getElementsByClass("access-link").isEmpty()) {
 			throw new NoAccessException();
 		}
 
 		Integer totalPages;
 		try {
 			totalPages = Integer.parseInt(doc
 					.getElementsByClass("number-of-pages").first().text());
 		} catch (NullPointerException e) {
 			totalPages = 1;
 		}
 
 		for (int i = 2; i <= totalPages; i++) {
 			url = urlBase + i;
 			doc = Jsoup.connect(url).timeout(5000).get();
 			chapters.putAll(getChapters(chapters));
 		}
 	}
 
 	public void setBookData() {
 		Element summary = doc.getElementsByClass("summary").first();
 		HashMap<String, String> cssIds = new HashMap<>();
 		// <key name, css id name>
 		cssIds.put("title", "abstract-about-title");
 		cssIds.put("subtitle", "abstract-about-book-subtitle");
 		cssIds.put("year", "abstract-about-book-chapter-copyright-year");
 		cssIds.put("doi", "abstract-about-book-chapter-doi");
 		cssIds.put("printIsbn", "abstract-about-book-print-isbn");
 		cssIds.put("onlineIsbn", "abstract-about-book-online-isbn");
 
 		for (Entry<String, String> cssId : cssIds.entrySet()) {
 			String text = summary.getElementById(cssId.getValue()).text();
 			if (text != null) {
 				book.setInfo(cssId.getKey(), text);
 			}
 		}
 
 		Elements authorElements = summary.getElementsByClass("person");
 		if (!authorElements.isEmpty()) {
 			String author = null;
 			for (Element authorElement : authorElements) {
 				if (author == null) {
 					author = authorElement.text();
 					continue;
 				}
 				author += ", " + authorElement.text();
 			}
 			book.setInfo("author", author);
 		}
 
 		book.setInfo("url", url);
 		book.setChapters(chapters);
 	}
 
 	public TreeMap<String, URL> getChapters(TreeMap<String, URL> chapters)
 			throws IOException {
 
 		Elements items = doc.getElementsByClass("toc-item");
 
 		for (Element item : items) {
 			String pageString = item.getElementsByClass("page-range").first()
 					.text();
 			Matcher matcher = Pattern.compile("\\d+|[MDCLXVI]",
 					Pattern.CASE_INSENSITIVE).matcher(pageString);
 
 			String page = null;
 			int count = 1;
 			if (matcher.find()) {
 				try { // decimal page numbers
 					int factor = 100;
 					page = String.format("%06d",
 							Integer.parseInt(matcher.group()) * factor);
 				} catch (NumberFormatException e) { // roman page numbers
 					page = String.format("%06d", count++);
 				}
 			}
 
 			URL pdfUrl = null;
 			pdfUrl = new URL(item.select("a[href$=.pdf]").first()
 					.attr("abs:href"));
 			chapters.put(page, pdfUrl);
 		}
 		return chapters;
 	}
 }
