 package com.cee.news.parser.impl;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
import java.net.ProxySelector;
 
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoutePlanner;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
 import org.ccil.cowan.tagsoup.Parser;
import org.junit.Rule;
 import org.junit.Test;
 
import betamax.Betamax;
import betamax.Recorder;
import betamax.TapeMode;

 import com.cee.news.model.Article;
 import com.cee.news.parser.ArticleParser;
 import com.cee.news.parser.ParserException;
 import com.cee.news.parser.net.ClassResourceWebClient;
 import com.cee.news.parser.net.impl.DefaultWebClient;
 import com.cee.news.parser.net.impl.XmlStreamReaderFactory;
 
 public class TestBoilerpipeArticleParser {
	
	@Rule
	public Recorder recorder = new Recorder();
 
     private static final String ARTICLE_START_TEXT = "Russischer ";
 	private static final String ARTICLE_LOCATION = "http://www.test.com/com/cee/news/parser/impl/spiegelArticle.html";
	
	protected HttpClient createHttpClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
		    client.getConnectionManager().getSchemeRegistry(),
		    ProxySelector.getDefault()
		);
		client.setRoutePlanner(routePlanner);
		return client;
	}
 
 	@Test
     public void testParse() throws ParserException, IOException {
         Article article = new Article();
         article.setLocation(ARTICLE_LOCATION);
         ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new ClassResourceWebClient());
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().startsWith(ARTICLE_START_TEXT));
     }
 
	@Betamax(tape = "issue120", mode = TapeMode.READ_ONLY)
 	@Test
 	public void testParseRegressionIssue120() throws ParserException, IOException {
 		Article article = new Article();
         article.setLocation("http://www.swr.de/nachrichten/-/id=396/nid=396/did=8825882/1u2s8qj/index.html");
        
        ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new DefaultWebClient(createHttpClient(), new XmlStreamReaderFactory()));
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().contains("Kein Referendum, kein Rücktritt, keine Lösung"));
 	}
 }
