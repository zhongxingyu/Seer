 package com.ml.bus.service;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import com.ml.bus.model.Category;
 import com.ml.bus.model.CrawlPattern;
 import com.ml.db.MongoDB;
 import com.ml.util.Constants;
 
 public class InitDatasetsService {
 	static MongoDB m;
 	
 	private void saveCrawlPattern() {
 		Map<String, String> urlPatterns = new HashMap<String, String>();
 		//urlPatterns.put("http://news.sohu.com", "http://news.sohu.com/[\\d]+/n[\\d]+.shtml");
 		urlPatterns.put("http://it.sohu.com", "http://it.sohu.com/[\\d]+/n[\\d]+.shtml");
 		urlPatterns.put("http://learning.sohu.com", "http://learning.sohu.com/[\\d]+/n[\\d]+.shtml");
 		urlPatterns.put("http://travel.sohu.com", "http://travel.sohu.com/[\\d]+/n[\\d]+.shtml");
 		urlPatterns.put("http://health.sohu.com", "http://health.sohu.com/[\\d]+/n[\\d]+.shtml");
 		for(String url: urlPatterns.keySet()) {
 			CrawlPattern cp = new CrawlPattern(null, url, urlPatterns.get(url), "sohu");
 			m.save(cp, Constants.crawlPatternCollectionName);
 		}
 	}
 	private void saveCategory() throws Exception {
		String filePath = "C:\\soft\\dm\\text_classification\\ClassList.txt";
 		String text = getText(filePath, " ");
 		String[] lines = text.split(" ");
 		for(String line: lines) {
 			String[] l = line.split("\t");
 			Category cate = new Category(l[0], l[1]);
 			m.save(cate, Constants.categoryCollectionName);
 		}
 		
 	}
 	
 	private String getText(String filePath, String token) throws Exception {
 
 		InputStreamReader isReader = new InputStreamReader(new FileInputStream(
				filePath), "GBK");
 		BufferedReader reader = new BufferedReader(isReader);
 		String aline;
 		StringBuilder sb = new StringBuilder();
 
 		while ((aline = reader.readLine()) != null) {
 			sb.append(aline.trim() + token);
 		}
 		isReader.close();
 		reader.close();
 		return sb.toString();
 	}
 	
 	public static void main(String[] args) throws Exception {
 		String confFile = Constants.defaultConfigFile.substring(1);
 		System.out.println(confFile);
 		Properties props = new Properties();
 		try {
 			props.load(new FileInputStream(confFile));
 		} catch (FileNotFoundException e) {
 			System.out.println(e.toString());
 			return;
 		} catch (IOException e) {
 			System.out.println(e.toString());
 			return;
 		}
 		m = new MongoDB(props);
 		InitDatasetsService mk = new InitDatasetsService();
 		mk.saveCrawlPattern();
 		mk.saveCategory();
 	}
 }
