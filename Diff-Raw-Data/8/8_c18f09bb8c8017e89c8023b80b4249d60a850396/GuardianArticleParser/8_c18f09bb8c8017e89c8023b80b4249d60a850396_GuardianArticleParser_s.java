 /*
  * GuardianArticleParser.java
  * Copyright (C) 2011 Meyer Kizner, Ty Overby
  * All rights reserved.
  */
 
 package com.subitarius.instance.server.parse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.subitarius.domain.Article;
 import com.subitarius.domain.ArticleUrl;
 import com.subitarius.domain.Team;
 import com.subitarius.util.http.RobotsExclusionException;
 import com.subitarius.util.http.SimpleHttpClient;
 
 final class GuardianArticleParser implements ArticleParser {
 	private static enum Feed {
 		NONE(null, DATE_FORMAT_UK) {
 		},
 
 		AP("AP foreign", DATE_FORMAT_US) {
 		},
 
 		REUTERS("Reuters", DATE_FORMAT_US, "div#article-body-blocks > div") {
 		},
 
 		PRESS_ASSOCIATION("Press Association", DATE_FORMAT_US) {
 		};
 
 		private final String name;
 
 		private final DateFormat dateFormat;
 
 		private final String bodySelector;
 
 		private Feed(String name, DateFormat dateFormat) {
 			this(name, dateFormat, "div#article-body-blocks > p");
 		}
 
 		private Feed(String name, DateFormat dateFormat, String bodySelector) {
 			this.name = name;
 			this.dateFormat = dateFormat;
 			this.bodySelector = bodySelector;
 		}
 
 		private static Feed fromName(String name) {
 			for (Feed feed : values()) {
 				if (name.equals(feed.name)) {
 					return feed;
 				}
 			}
 			return NONE;
 		}
 	}
 
 	private static final DateFormat DATE_FORMAT_UK = new SimpleDateFormat(
 			"EEEEE d MMMMM yyyy");
 
 	private static final DateFormat DATE_FORMAT_US = new SimpleDateFormat(
 			"EEEEE MMMMM d yyyy");
 
 	private final Provider<Team> teamProvider;
 
 	private final SimpleHttpClient httpClient;
 
 	@Inject
 	private GuardianArticleParser(Provider<Team> teamProvider,
 			SimpleHttpClient httpClient) {
 		this.teamProvider = teamProvider;
 		this.httpClient = httpClient;
 	}
 
 	@Override
 	public Article parse(ArticleUrl articleUrl) throws ArticleParseException {
 		try {
 			String url = articleUrl.getUrl();
 			InputStream stream = httpClient.doGet(url);
 			Document document = Jsoup.parse(stream, null, url);
 
			if (document.body().className().contains("has-badge")) {
				// blog posts, contests, etc. all seem to contain this
 				return null;
 			}
 
 			String title = document.select("h1").first().text();
 
 			Element bylineElem = document.select("a.contributor").first();
 			String byline = ((bylineElem == null) ? null : bylineElem.text());
 
 			// publication element is absent if expired
 			Element publicationElem = document.select("li.publication").first();
 			if (publicationElem == null) {
 				// expired Reuters articles do this
 				return null;
 			}
 
 			// get both the date and the feed from the publication element
 			Date date;
 			String[] publication = publicationElem.text().split(",");
 			Feed feed = Feed.fromName(publication[0].trim());
 			try {
 				String dateStr = publication[1].trim();
 				date = feed.dateFormat.parse(dateStr);
 			} catch (ParseException px) {
 				throw new ArticleParseException(articleUrl, px);
 			}
 
 			List<String> paragraphs = Lists.newArrayList();
 			List<Element> elements = document.select(feed.bodySelector);
 			for (Element elem : elements) {
 				String text = elem.text().trim();
 				if (!text.isEmpty()) {
 					paragraphs.add(text);
 				}
 			}
 
 			return new Article(teamProvider.get(), articleUrl, title, byline,
 					date, paragraphs);
 		} catch (IOException iox) {
 			throw new ArticleParseException(articleUrl, iox);
 		} catch (RobotsExclusionException rex) {
 			throw new ArticleParseException(articleUrl, rex);
 		}
 	}
 }
