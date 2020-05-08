 package net.sf.okapi.filters.html.integration;
 
 import static org.junit.Assert.assertTrue;
 
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.filters.html.HtmlFilter;
 import net.sf.okapi.filters.html.TestUtils;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filters.RoundTripComparison;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ExtractionComparisionTest {
 	private HtmlFilter htmlFilter;
 	private String[] testFileList;
 	private String root;
 
 	@Before
 	public void setUp() throws Exception {
 		htmlFilter = new HtmlFilter();
 		testFileList = TestUtils.getHtmlTestFiles();
 
 		URL url = ExtractionComparisionTest.class.getResource("/324.html");
		root = Util.getDirectoryName(url.getPath());
		root = Util.getDirectoryName(root) + "/html/";
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
	// FIXME: @Test
 	public void testStartDocument() {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(htmlFilter,
 				new InputDocument(root + "324.html", null),
 				"UTF-8", "en", "en"));
 	}
 
 	// FIXME: Should move to integration project @Test
 	public void testDoubleExtraction() throws URISyntaxException {
 		RoundTripComparison rtc = new RoundTripComparison();
 		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 		for (String f : testFileList) {
 			list.add(new InputDocument(root + f, null));
 		}
 		assertTrue(rtc.executeCompare(htmlFilter, list, "UTF-8", "en", "en"));
 	}
 }
