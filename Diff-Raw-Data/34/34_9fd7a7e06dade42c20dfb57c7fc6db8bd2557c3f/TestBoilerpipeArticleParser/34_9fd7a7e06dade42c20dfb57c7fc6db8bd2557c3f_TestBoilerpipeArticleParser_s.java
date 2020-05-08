 package com.cee.news.parser.impl;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.ccil.cowan.tagsoup.Parser;
 import org.junit.Test;
 
 import com.cee.news.model.Article;
 import com.cee.news.parser.ArticleParser;
 import com.cee.news.parser.ParserException;
 import com.cee.news.parser.net.ClassResourceWebClient;
 import com.cee.news.parser.net.impl.DefaultWebClient;
 import com.cee.news.parser.net.impl.XmlStreamReaderFactory;
 
 public class TestBoilerpipeArticleParser {
 
     private static final String ARTICLE_START_TEXT = "Russischer ";
 	private static final String ARTICLE_LOCATION = "http://www.test.com/com/cee/news/parser/impl/spiegelArticle.html";
 
 	@Test
     public void testParse() throws ParserException, IOException {
         Article article = new Article();
         article.setLocation(ARTICLE_LOCATION);
         ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new ClassResourceWebClient());
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().startsWith(ARTICLE_START_TEXT));
     }
 
 	@Test
 	public void testParseRegressionIssue120() throws ParserException, IOException {
 		Article article = new Article();
         article.setLocation("http://www.swr.de/nachrichten/-/id=396/nid=396/did=8825882/1u2s8qj/index.html");
        ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new DefaultWebClient(new DefaultHttpClient(), new XmlStreamReaderFactory()));
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().contains("Kein Referendum, kein Rücktritt, keine Lösung"));
 	}
 }
