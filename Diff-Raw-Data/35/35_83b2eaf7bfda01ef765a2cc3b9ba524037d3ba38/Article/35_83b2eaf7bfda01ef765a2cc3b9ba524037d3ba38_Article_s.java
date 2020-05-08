 package com.infoc.domain;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Ordering;
 import com.infoc.enumeration.ArticleSection;
 
 /**
  * @author anbv3
  */
 public class Article {
 	private static final Logger LOG = LoggerFactory.getLogger(Article.class);
 
 	private String hashId; // for later
 
 	private String title;
 
 	private String link;
 
 	private String img;
 
 	private String contents;
 
 	private DateTime pubDate;
 
 	private String author;
 	
 	private ArticleSection section;
 
 	// /////////////////////////////////////////////////////////////////////////////
 	private Set<String> keyWordList = new HashSet<>(); // use for summarization
 														// and duplication check
 
 	private String mainContents;
 	// /////////////////////////////////////////////////////////////////////////////
 
 	public static final Ordering<Article> dateOrder = new Ordering<Article>() {
 		@Override
 		public int compare(Article left, Article right) {
 			return left.getPubDate().compareTo(right.getPubDate());
 		}
 	};
 
 	public static final Ordering<SentenceInfo> matchedOrder = new Ordering<SentenceInfo>() {
 		@Override
 		public int compare(SentenceInfo left, SentenceInfo right) {
 			// 1. determine by match word
 			int match = left.getMatchedWord().compareTo(right.getMatchedWord());
 			if (match != 0) {
 				return match;
 			}
 			
 			// 2. pick the longer sentence among the same matched sentences
 			return left.getLength().compareTo(right.getLength());
 		}
 	};
 
 	public static final Ordering<SentenceInfo> indexOrder = new Ordering<SentenceInfo>() {
 		@Override
 		public int compare(SentenceInfo left, SentenceInfo right) {
 			return left.getIndex().compareTo(right.getIndex());
 		}
 	};
 
 	public void createContentsFromLink() {
 		try {
 			String contentId = null;
 
 			// for naver news, remove the last link section from the contents
 			if (this.link.contains("naver")) {
 				contentId = "#articleBody";
 
 				Document doc = Jsoup.connect(this.link).timeout(6000).get();
 				Elements contentsArea = doc.select(contentId);
 				Elements linkArea = doc.select(".link_news");
 
 				int linkIdx = contentsArea.text().indexOf(linkArea.text());
 				if (linkIdx != -1) {
 					this.contents = contentsArea.text().substring(0, linkIdx);
 				} else {
 					this.contents = contentsArea.text();
 				}
 				return;
 			}
 
 			if (this.link.contains("daum")) {
 
 				contentId = "#newsBodyShadow";
 
 			} else if (this.link.contains("interview365")) {
 
 				contentId = "#IDContents";
 
 			} else if (this.link.contains("khan")) {
 
 				contentId = "#_article";
 
 			} else if (this.link.contains("segye")) {
 
 				contentId = "#article_txt";
 
 			} else if (this.link.contains("asiae")) {
 
 				contentId = ".article";
 
 			} else if (this.link.contains("chosun")) {
 
 				contentId = ".par";
 
 			} else if (this.link.contains("dailian")) {
 
 				contentId = "#view_con";
 
 			} else if (this.link.contains("newsen")) {
 
 				contentId = "#CLtag";
 
 			} else if (this.link.contains("hankooki")) {
 
 				contentId = "#GS_Content";
 
 			} else if (this.link.contains("ittoday")
 				|| this.link.contains("unionpress")
 				|| this.link.contains("yonhapnews")
 				|| this.link.contains("newsis")) {
 
 				contentId = "#articleBody";
 
 			} else if (this.link.contains("hankyung") || this.link.contains("ahatv")) {
 
 				contentId = "#sstvarticle";
 
 			} else if (this.link.contains("sportsworldi")) {
 
 				contentId = "#article_content";
 
 			} else if (this.link.contains("tvdaily")
 				|| this.link.contains("etoday")
 				|| this.link.contains("ytn")
 				|| this.link.contains("vop")) {
 
 				contentId = "#CmAdContent";
 
 			} else {
 				return;
 			}

 			Document doc = Jsoup.connect(this.link).timeout(6000).get();
 			Elements contentsArea = doc.select(contentId);
 			this.contents = contentsArea.text();
 		} catch (IOException e) {
 			LOG.error(this.link + "\n", e);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return Objects.toStringHelper(this).add("title", this.title)
 			.add("link", this.link).add("pubDate", this.pubDate)
 			.add("mainContents", this.mainContents)
 			.add("keyWordList", this.keyWordList).toString();
 	}
 
 	public String getHashId() {
 		return hashId;
 	}
 
 	public void setHashId(String hashId) {
 		this.hashId = hashId;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getLink() {
 		return link;
 	}
 
 	public void setLink(String link) {
 		this.link = link;
 	}
 
 	public String getImg() {
 		return img;
 	}
 
 	public void setImg(String img) {
 		this.img = img;
 	}
 
 	public String getContents() {
 		return contents;
 	}
 
 	public void setContents(String contents) {
 		this.contents = contents;
 	}
 
 	public DateTime getPubDate() {
 		return pubDate;
 	}
 
 	public void setPubDate(DateTime pubDate) {
 		this.pubDate = pubDate;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 	
 	public ArticleSection getSection() {
 		return section;
 	}
 
 	public void setSection(ArticleSection section) {
 		this.section = section;
 	}
 
 	public Set<String> getKeyWordList() {
 		return keyWordList;
 	}
 
 	public void setKeyWordList(Set<String> keyWordList) {
 		this.keyWordList = keyWordList;
 	}
 
 	public String getMainContents() {
 		return mainContents;
 	}
 
 	public void setMainContents(String mainContents) {
 		this.mainContents = mainContents;
 	}
 }
