 /*
  * Copyright (C) 2013 Bojun Shim <sragent@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.sharpshim.yarc;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import nl.matshofman.saxrssreader.RssFeed;
 import nl.matshofman.saxrssreader.RssItem;
 import nl.matshofman.saxrssreader.RssReader;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import com.sharpshim.yarc.util.Config;
 import com.sharpshim.yarc.util.HtmlUtil;
 import com.sharpshim.yarc.util.UrlQueue;
 
 /**
  * This Thread class is the worker thread which crawl and write the RSS feeds
  * 
  * @author sharpshim
  */
 public class RssDocumentCrawlerThread extends AbstractCrawlerThread {
 	/** Log4j Logger */
 	private static final Logger logger = LogManager.getLogger(RssDocumentCrawlerThread.class.getName());
 	/** Configurations */
 	private static final Config config = Config.getInstance();
 	/** The tsv file writer */ 
 	private static BufferedWriter documentTsvBw;
 	/** Set to avoid handle duplicated urls */
 	private static Set<String> dupCheckLinkSet;
 	
 	static {
 		final int LINK_FIELD_IDX = 2;
 		try {
 			//Open the document files which maded before to avoid duplicated urls
 			dupCheckLinkSet = new HashSet<String>();
 			logger.info("Document file to save: " + config.get("DOC_FILE"));
 			BufferedReader br = new BufferedReader(new FileReader(config.get("DOC_FILE")));
 			String line = null;
 			int docCount = 0;
 			while((line = br.readLine()) != null) {
 				int idx1 = 0;
 				int idx2 = -1;
 				for (int i = 0;; i++) {
 					idx2 = line.indexOf('\t', idx1+1);
 					if (i == LINK_FIELD_IDX)
 						break;
 					idx1 = idx2 + 1;
 				}
 				if (idx2 == -1 || idx1 == 0) {
 					continue;
 				}
 				String link = line.substring(idx1, idx2);
 				docCount++;
 				dupCheckLinkSet.add(link);
 			}
 			br.close();
 			logger.info("There are already " + docCount + " documents in "+ config.get("DOC_FILE"));
 		}
 		// It might be occurred when the document files doesn't exist.
 		catch (FileNotFoundException e) {
			logger.info(config.get("DOC_FILE") + " is not exist. It will be created automatically.");
 			
 		}
 		catch (IOException e) {
 			logger.error(e.getMessage(), e);
 		}
 		
 		try {
 			documentTsvBw = new BufferedWriter(new FileWriter(config.get("DOC_FILE"), true));
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			System.exit(-1);
 		}
 	}
 	
 	public RssDocumentCrawlerThread(UrlQueue urlQ) throws IOException {
 		super(urlQ);
 	}
 
 	@Override
 	protected void crawlAndWrite(URL url) throws IOException {
 		tsvWrite(url);
 	}
 
 	private void tsvWrite(URL url) throws IOException {
 		logger.info("Process feed URL: " + url.toString());
 		RssFeed feed = null;
 		try {
 			feed = RssReader.read(url);
 		}
 		catch (SAXException e) {
 			throw new IOException("Fail in Sax Parsing " + url.toString());
 		}
 		List<RssItem> items = feed.getRssItems();
 		StringBuffer sb = new StringBuffer();
 		for (RssItem item : items) {
 			String regDate = (new Date()).toString();
 			String link = item.getLink();
 			if (link == null || link.trim().length() == 0) {
 				logger.info("Link is null or length=0\tURL: " + url.toString());
 				continue;
 			}
 			if (dupCheckLinkSet.contains(link)) {
 				continue;
 			}
 			String descriptionHtml = item.getDescription();
 			if (descriptionHtml == null) {
 				logger.info("Description is null\turl: " + url.toString());
 				descriptionHtml = "";
 			}
 			descriptionHtml = descriptionHtml.replaceAll("[\\t\\n\\r]", " ");
 			String descriptionText = HtmlUtil.stripHtml(descriptionHtml);
 			descriptionText = descriptionText.replaceAll("[\\t\\n\\r]", " ");
 			String title = item.getTitle();
 			if (title == null || title.trim().length() == 0) {
 				logger.info("Title is null or length=0\turl: " + url.toString());
 				continue;
 			}
 			Date pubDateTemp = item.getPubDate();
 			if (pubDateTemp == null) {
 				logger.info("PubDate is null\turl: " + url.toString());
 				continue;
 			}
 			String pubDate = pubDateTemp.toString();
 			if (pubDate.trim().length() == 0) {
 				logger.info("PubDate length=0\turl: " + url.toString());
 				continue;
 			}
 			String line = String.format("%s\t%s\t%s\t%s\t%s\t%s\n", regDate, pubDate, link, title, descriptionHtml, descriptionText);
 			sb.append(line);
 		}
 		synchronized(documentTsvBw) {
 			documentTsvBw.write(sb.toString());
 			documentTsvBw.flush();
 		}
 	}
 }
