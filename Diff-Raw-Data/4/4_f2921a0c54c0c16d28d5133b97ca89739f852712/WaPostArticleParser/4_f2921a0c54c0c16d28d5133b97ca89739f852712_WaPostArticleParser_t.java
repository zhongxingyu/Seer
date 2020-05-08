 /*
  * WaPostArticleParser.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.server.parse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.filter.Filter;
 import org.jdom.input.DOMBuilder;
 import org.w3c.tidy.Tidy;
 
 import com.google.inject.Inject;
 import com.prealpha.extempdb.server.http.HttpClient;
 import com.prealpha.extempdb.server.http.RobotsExclusionException;
 import com.prealpha.extempdb.server.util.XmlUtils;
 
 class WaPostArticleParser implements ArticleParser {
 	private static enum ArticleType {
 		STORY {
 			@Override
 			Filter getBodyFilter() {
 				return XmlUtils.getElementFilter("div", "id", "article_body");
 			}
 		},
 
 		BLOG {
 			@Override
 			Filter getBodyFilter() {
 				return XmlUtils.getElementFilter("div", "id", "entrytext");
 			}
 		};
 
 		abstract Filter getBodyFilter();
 	}
 
 	/*
 	 * Package visibility for unit testing.
 	 */
 	static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
 
 	private final HttpClient httpClient;
 
 	private final Tidy tidy;
 
 	private final DOMBuilder builder;
 
 	@Inject
 	public WaPostArticleParser(HttpClient httpClient, Tidy tidy,
 			DOMBuilder builder) {
 		this.httpClient = httpClient;
 		this.tidy = tidy;
 		this.builder = builder;
 	}
 
 	@Override
 	public String getCanonicalUrl(String url) {
 		// strip off any GET parameters before dealing with other stuff
 		if (url.contains("?")) {
 			url = url.substring(0, url.indexOf("?"));
 		}
 
 		if (url.matches(".*_story_\\d+.html")) {
 			// page numbers
 			int index = url.lastIndexOf("_story");
 			return url.substring(0, index) + "_story.html";
 		} else if (url.endsWith("_singlePage.html")) {
 			// single page version
 			int index = url.lastIndexOf("_singlePage");
 			return url.substring(0, index) + "_story.html";
 		} else if (url.endsWith("_print.html")) {
 			// printable version
 			int index = url.lastIndexOf("_print");
 			return url.substring(0, index) + "_story.html";
 		} else {
 			return url;
 		}
 	}
 
 	@Override
 	public ProtoArticle parse(String url) throws ArticleParseException {
 		ArticleType type;
 		if (url.endsWith("_story.html")) {
 			type = ArticleType.STORY;
 
 			// we have to replace this with the single page version for fetching
 			int index = url.lastIndexOf("_story.html");
 			url = url.substring(0, index) + "_singlePage.html";
 		} else if (url.endsWith("_blog.html")) {
 			type = ArticleType.BLOG;
 		} else {
 			throw new IllegalArgumentException(
 					"unrecognized canonical URL type");
 		}
 
 		try {
 			Map<String, String> params = Collections.emptyMap();
 			InputStream stream = httpClient.doGet(url, params);
 			return getFromHtml(stream, type);
 		} catch (IOException iox) {
 			throw new ArticleParseException(iox);
 		} catch (RobotsExclusionException rex) {
 			throw new ArticleParseException(rex);
 		}
 	}
 
 	private ProtoArticle getFromHtml(InputStream html, ArticleType type)
 			throws ArticleParseException {
 		org.w3c.dom.Document doc = tidy.parseDOM(html, null);
 		doc.removeChild(doc.getDoctype());
 		Document document = builder.build(doc);
 
 		Element headElement = document.getRootElement().getChild("head");
 		Map<String, String> metaMap = XmlUtils.getMetaMap(headElement);
 
 		String title = metaMap.get("DC.title");
 
 		String byline;
 		if (metaMap.containsKey("DC.creator")) {
 			byline = "By " + metaMap.get("DC.creator");
 		} else {
 			byline = null;
 		}
 
 		Date date;
 		try {
 			date = DATE_FORMAT.parse(metaMap.get("DC.date.issued"));
 		} catch (ParseException px) {
 			throw new ArticleParseException(px);
 		}
 
 		Filter bodyFilter = type.getBodyFilter();
 		Element articleBody = (Element) document.getDescendants(bodyFilter)
 				.next();
 		List<String> paragraphs = new ArrayList<String>();
 		for (Object obj : articleBody.getChildren("p")) {
 			Element paragraph = (Element) obj;
 
 			// remove image captions
 			Filter imageLeftFilter = XmlUtils.getElementFilter("span", "class",
 					"imgleft");
 			Filter imageRightFilter = XmlUtils.getElementFilter("span",
 					"class", "imgright");
 			Filter imageFilter = XmlUtils.getOrFilter(imageLeftFilter,
 					imageRightFilter);
 			Iterator<?> imageIterator = paragraph.getDescendants(imageFilter);
 			while (imageIterator.hasNext()) {
				imageIterator.next();
				imageIterator.remove();
 			}
 
 			paragraphs.add(paragraph.getValue().trim());
 		}
 
 		return new ProtoArticle(title, byline, date, paragraphs);
 	}
 }
