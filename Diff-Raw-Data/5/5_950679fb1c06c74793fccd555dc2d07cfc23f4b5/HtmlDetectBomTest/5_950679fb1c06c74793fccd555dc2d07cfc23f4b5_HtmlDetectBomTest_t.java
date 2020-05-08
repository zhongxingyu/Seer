 package net.sf.okapi.filters.html;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.filters.html.HtmlFilter;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class HtmlDetectBomTest {
 
 	private HtmlFilter htmlFilter;
 	private LocaleId locEN = LocaleId.fromString("en");
 	
 	@Before
 	public void setUp() throws Exception {
 		htmlFilter = new HtmlFilter();		
 	}
 
 	@After
 	public void tearDown() {
 		htmlFilter.close();
 	}
 
 	@Test
 	public void testDetectBom() throws IOException {
 		InputStream htmlStream = HtmlDetectBomTest.class.getResourceAsStream("/ruby.html");		
 		BOMNewlineEncodingDetector bomDetector = new BOMNewlineEncodingDetector(htmlStream, "UTF-8");
 		bomDetector.detectBom();
 		
 		assertTrue(bomDetector.hasBom());
 		assertTrue(bomDetector.hasUtf8Bom());
 		assertFalse(bomDetector.hasUtf7Bom());
 		
 		
 		htmlFilter.open(new RawDocument(htmlStream, bomDetector.getEncoding(), locEN));
 		while (htmlFilter.hasNext()) {
 			Event event = htmlFilter.next();			
 			if (event.getEventType() == EventType.START_DOCUMENT) {
 				StartDocument sd = (StartDocument)event.getResource();
 				assertTrue(sd.hasUTF8BOM());
 				assertEquals("UTF-8", sd.getEncoding());
 				assertEquals(locEN, sd.getLocale());
				assertEquals("\n", sd.getLineBreak());
 			}
 		}
 	}
 	
 	@Test
 	public void testDetectUnicodeLittleBom() throws IOException {
 		InputStream htmlStream = HtmlDetectBomTest.class.getResourceAsStream("/FFFEBOM.html");		
 		BOMNewlineEncodingDetector bomDetector = new BOMNewlineEncodingDetector(htmlStream, "UTF-16LE");
 		bomDetector.detectBom();
 		
 		assertTrue(bomDetector.hasBom());
 		assertFalse(bomDetector.hasUtf8Bom());
 		assertFalse(bomDetector.hasUtf7Bom());
 		
 		
 		htmlFilter.open(new RawDocument(htmlStream, bomDetector.getEncoding(), locEN));
 		while (htmlFilter.hasNext()) {
 			Event event = htmlFilter.next();			
 			if (event.getEventType() == EventType.START_DOCUMENT) {
 				StartDocument sd = (StartDocument)event.getResource();
 				assertFalse(sd.hasUTF8BOM());
 				assertEquals("UTF-16LE", sd.getEncoding());
 				assertEquals(locEN, sd.getLocale());
 				assertEquals("\r\n", sd.getLineBreak());
 			}
 		}
 	}
 	
 	@Test
 	public void testDetectAndRemoveBom() throws IOException {
 		InputStream htmlStream = HtmlDetectBomTest.class.getResourceAsStream("/ruby.html");		
 		BOMNewlineEncodingDetector bomDetector = new BOMNewlineEncodingDetector(htmlStream, "UTF-8");
 		bomDetector.detectAndRemoveBom();
 		
 		assertTrue(bomDetector.hasBom());
 		assertTrue(bomDetector.hasUtf8Bom());
 		assertFalse(bomDetector.hasUtf7Bom());		
 		
 		htmlFilter.open(new RawDocument(htmlStream, bomDetector.getEncoding(), locEN));
 		while (htmlFilter.hasNext()) {
 			Event event = htmlFilter.next();		
 			if (event.getEventType() == EventType.START_DOCUMENT) {
 				StartDocument sd = (StartDocument)event.getResource();
 				assertFalse(sd.hasUTF8BOM());
 				assertEquals("UTF-8", sd.getEncoding());
 				assertEquals(locEN, sd.getLocale());
				assertEquals("\n", sd.getLineBreak());
 			}
 		}		
 	}	
 }
