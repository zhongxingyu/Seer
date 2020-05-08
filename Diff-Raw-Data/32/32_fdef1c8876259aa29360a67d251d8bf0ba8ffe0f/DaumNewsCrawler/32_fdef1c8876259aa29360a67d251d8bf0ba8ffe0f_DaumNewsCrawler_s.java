 /*
  * @(#)DaumNewsCrawler.java $version 2013. 10. 25.
  */
 
 package com.infoc.crawler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Strings;
 import com.infoc.domain.Article;
 import com.infoc.enumeration.ArticleSection;
 import com.infoc.service.ContentsAnalysisService;
 import com.infoc.util.RSSCrawler;
 import com.sun.syndication.feed.synd.SyndEnclosure;
 import com.sun.syndication.feed.synd.SyndEntry;
 
 public class DaumNewsCrawler implements NewsCrawler {
 	private static final Logger LOG = LoggerFactory.getLogger(DaumNewsCrawler.class);
 
 	private static String TODAY = "http://media.daum.net/syndication/today_sisa.rss";
 	private static String POLITICS = "http://media.daum.net/syndication/politics.rss";
 	private static String ECON = "http://media.daum.net/syndication/economic.rss";
 	private static String SOCIETY = "http://media.daum.net/syndication/society.rss";
 	private static String CULTURE = "http://media.daum.net/syndication/culture.rss";
 	private static String ENT = "http://media.daum.net/syndication/today_entertain.rss";
 	private static String SPORT = "http://media.daum.net/syndication/today_sports.rss";
 	private static String IT = "http://media.daum.net/syndication/digital.rss";
 
 	private List<Article> articleList = new ArrayList<>();
 
 	@Override
 	public List<Article> createArticlList() {
 		LOG.debug("get RSS from Daum.");
 
 		createListBySection(TODAY, ArticleSection.TODAY);
 		createListBySection(POLITICS, ArticleSection.POLITICS);
 		createListBySection(ECON, ArticleSection.ECON);
 		createListBySection(SOCIETY, ArticleSection.SOCIETY);
		createListBySection(CULTURE, ArticleSection.CULTURE);
		createListBySection(ENT, ArticleSection.ENT);
		createListBySection(SPORT, ArticleSection.SPORT);
		createListBySection(IT, ArticleSection.IT);
 
 		return this.articleList;
 	}
 
 	private void createListBySection(String rssUrl, ArticleSection section) {
 		for (SyndEntry item : RSSCrawler.getArticleList(rssUrl)) {
 			Article article = parseRSSItem(item, section);
 			if (article == null) {
 				continue;
 			}
 			this.articleList.add(article);
 		}
 	}
 
 	private Article parseRSSItem(SyndEntry rssItem, ArticleSection section) {
 		Article article = new Article();
 		article.setSection(section);
 		article.setAuthor(rssItem.getAuthor());
 		article.setLink(rssItem.getLink());
 		article.setPubDate(new DateTime(rssItem.getPublishedDate(), DateTimeZone.forID("Asia/Seoul")));
 		article.setTitle(ContentsAnalysisService.clearInvalidWords(rssItem.getTitle()));
 		if (Strings.isNullOrEmpty(article.getTitle()) || article.getTitle().length() < 5) {
 			return null;
 		}
 
 		if (!rssItem.getEnclosures().isEmpty()) {
 			SyndEnclosure enclosure = (SyndEnclosure)(rssItem.getEnclosures().get(0));
 			article.setImg(enclosure.getUrl());
 		}
 		
 		article.createContentsFromLink();
 		if (Strings.isNullOrEmpty(article.getContents())) {
 			article.setContents(rssItem.getDescription().getValue());
 		}
 
 		article.setContents(ContentsAnalysisService.clearInvalidWords(article.getContents()));
 
 		return article;
 	}
 
 }
