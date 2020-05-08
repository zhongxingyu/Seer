package com.cee.news.language;
 
import static junit.framework.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.cee.news.SiteExtraction;
 import com.cee.news.model.Feed;
 import com.cee.news.model.Site;
 
 public class TestSiteLanguageDetector {
 
 	@Test
 	public void testOne() {
 		List<LanguageDetector> detectors = new ArrayList<LanguageDetector>();
 		detectors.add( new  LanguageDetector() {
 			
 			@Override
 			public String detect(String text) {
 				return "de";
 			}
 		});
 		SiteLanguageDetector detector = new SiteLanguageDetector(detectors);
 		assertEquals("de", detector.detect(new SiteExtraction()));
 	}
 
 	@Test
 	public void testFirstMatch() {
 		List<LanguageDetector> detectors = new ArrayList<LanguageDetector>();
 		detectors.add( new  LanguageDetector() {
 			
 			@Override
 			public String detect(String text) {
 				return "de";
 			}
 		});
 		detectors.add( new  LanguageDetector() {
 			
 			@Override
 			public String detect(String text) {
 				return "en";
 			}
 		});
 		SiteLanguageDetector detector = new SiteLanguageDetector(detectors);
 		assertEquals("de", detector.detect(new SiteExtraction()));
 	}
 	
 	@Test
 	public void testSecondMatch() {
 		List<LanguageDetector> detectors = new ArrayList<LanguageDetector>();
 		detectors.add( new  LanguageDetector() {
 			
 			@Override
 			public String detect(String text) {
 				return null;
 			}
 		});
 		detectors.add( new  LanguageDetector() {
 			
 			@Override
 			public String detect(String text) {
 				return "en";
 			}
 		});
 		SiteLanguageDetector detector = new SiteLanguageDetector(detectors);
 		assertEquals("en", detector.detect(new SiteExtraction()));
 	}
 	
 	@Test
 	public void testFeedLanguageMatch() {
 		List<LanguageDetector> detectors = new ArrayList<LanguageDetector>();
 		detectors.add( new  LanguageDetector() {
 			
 			@Override
 			public String detect(String text) {
 				return null;
 			}
 		});
 		detectors.add( new  LanguageDetector() {
 			
 			@Override
 			public String detect(String text) {
 				return "en";
 			}
 		});
 		SiteLanguageDetector detector = new SiteLanguageDetector(detectors);
 		SiteExtraction siteExtraction = new SiteExtraction();
 		Site site = siteExtraction.getSite();
 		site.getFeeds().add(new Feed());
 		Feed feed = new Feed();
 		feed.setLanguage("ko");
 		site.getFeeds().add(feed);
 		assertEquals("ko", detector.detect(siteExtraction));
 	}
	
	@Test
	public void testNoMatch() {
		assertNull(new SiteLanguageDetector().detect(new SiteExtraction()));
	}
 }
