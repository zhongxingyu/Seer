 package net.sf.okapi.filters.ts.tests;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.resource.INameable;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.tests.FilterTestDriver;
import net.sf.okapi.filters.tests.InputDocument;
import net.sf.okapi.filters.ts.TsFilter;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class TsFilterTest {
 
 	private TsFilter filter;
 	private FilterTestDriver testDriver;
 	
 	String simpleSnippet = "<TS><context><name>AlarmAddLogDlg</name><message><source>Add Entry To System Log</source><translation type=\"unfinished\">Add Entry To System Log</translation></message></context></TS>";
 
 	@Before
 	public void setUp() throws ParserConfigurationException, SAXException, IOException {
 		filter = new TsFilter();
 		testDriver = new FilterTestDriver();
 		testDriver.setDisplayLevel(0);
 		testDriver.setShowSkeleton(true);
 		
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	      DocumentBuilder builder = factory.newDocumentBuilder();
 	      InputSource is = new InputSource(new StringReader("<hello><name>john</name></hello>"));
 	      Document d = builder.parse( is );
 //	      System.out.println("NodeName: "+d.getNodeName());
 //	      System.out.println("NodeName: "+d.getElementsByTagName("name"));
 		
 	}
 	
 	//--methods--
 	@Test
 	public void testGetName() {
 		assertEquals("okf_ts", filter.getName());
 	}
 
 	@Test
 	public void testGetMimeType() {
 		assertEquals("text/x-ts", filter.getMimeType());
 	}	
 	
 	//--exceptions--
 	@Test (expected=NullPointerException.class)
 	public void testSourceLangNotSpecified() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, null));
 	}
 
 	@Test (expected=NullPointerException.class)
 	public void testTargetLangNotSpecified() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us"));
 	}
 
 	@Test (expected=NullPointerException.class)
 	public void testTargetLangNotSpecified2() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us", null));
 	}
 	
 /*	@Test (expected=NullPointerException.class)
 	public void testSourceLangEmpty() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "","fr-fr"));
 	}*/	
 	
 /*	@Test (expected=NullPointerException.class)
 	public void testTargetLangEmpty() {
 		FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us",""));
 	}*/	
 	
 	@Test
 	public void testInputStream() {
 		InputStream tsStream = TsFilterTest.class.getResourceAsStream("/alarm_ro.ts");
 		filter.open(new RawDocument(tsStream, "UTF-8", "en-us","fr-fr"));
 		if ( !testDriver.process(filter) ) Assert.fail();
 		filter.close();
 	}	
 
 	@Test
 	public void testConsolidatedStream() {
 		filter.open(new RawDocument(simpleSnippet, "en-us","fr-fr"));
 		if ( !testDriver.process(filter) ) Assert.fail();
 		filter.close();
 		//System.out.println(FilterTestDriver.generateOutput(getEvents(simpleSnippet, "en-us","fr-fr"), simpleSnippet, "fr-fr"));
 	}	
 
 	
 	/*
 	@Test
 	public void testOutputBasic_Comment () {
 		assertEquals(simpleBilingualSnippet, FilterTestDriver.generateOutput(getEvents(simpleBilingualSnippet,"en-us","fr-fr"), simpleSnippet, "fr-fr"));
 		System.out.println(FilterTestDriver.generateOutput(getEvents(simpleBilingualSnippet,"en-us","fr-fr"), simpleSnippet, "en"));
 	}*/	
 	
 	/*@Test
 	public void testStartDocument () {
 		StartDocument sd = FilterTestDriver.getStartDocument(getEvents(simpleSnippet, "en-us","fr-fr"));
 		assertNotNull(sd);
 		assertNotNull(sd.getEncoding());
 		assertNotNull(sd.getType());
 		assertNotNull(sd.getMimeType());
 		assertNotNull(sd.getLanguage());
 		assertEquals("\r", sd.getLineBreak());
 	}*/
 	
 	/*@Test
 	public void testSimpleTransUnit () {
 		TextUnit tu = FilterTestDriver.getTextUnit(getEvents(simpleSnippet, "en-us","fr-fr"), 1);
 		assertNotNull(tu);
 		assertEquals("Hello World!", tu.getSource().toString());
 		assertEquals("tuid_1", tu.getName());
 	}*/
 	
 	@Test
 	public void testStartDocument () {
 		URL url = TsFilterTest.class.getResource("/TSTest01.ts");
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(url.getPath(), null),
 			"UTF-8", "en", "en"));
 	}
 	
 	@Test
 	public void runTest () {
 		FilterTestDriver testDriver = new FilterTestDriver();
 		TsFilter filter = null;		
 		try {
 			filter = new TsFilter();
 			URL url = TsFilterTest.class.getResource("/TSTest01.ts");
 			filter.open(new RawDocument(new URI(url.toString()), "UTF-8", "EN-US", "FR-CA"));			
 			if ( !testDriver.process(filter) ) Assert.fail();
 			//process(filter);
 			filter.close();
 			
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 			Assert.fail("Exception occured");
 		}
 		finally {
 			if ( filter != null ) filter.close();
 		}
 	}	
 	
 	private void process (IFilter filter) {
 		
 		System.out.println("==================================================");
 		Event event;
 		while ( filter.hasNext() ) {
 			event = filter.next();
 			switch ( event.getEventType() ) {		
 			case START_DOCUMENT:
 				System.out.println("---Start Document");
 				printSkeleton(event.getResource());
 				break;
 			case END_DOCUMENT:
 				System.out.println("---End Document");
 				printSkeleton(event.getResource());
 				break;
 			case START_GROUP:
 				System.out.println("---Start Group");
 				printSkeleton(event.getResource());
 				break;
 			case END_GROUP:
 				System.out.println("---End Group");
 				printSkeleton(event.getResource());
 				break;
 			case TEXT_UNIT:
 				System.out.println("---Text Unit");
 				TextUnit tu = (TextUnit)event.getResource();
 				printResource(tu);
 				System.out.println("S=["+tu.toString()+"]");
 				int i = 1;
 				for ( String lang : tu.getTargetLanguages() ) {
 					System.out.println("T"+(i++)+" "+lang+"=["+tu.getTarget(lang).toString()+"]");
 				}
 				printSkeleton(tu);
 				break;
 			case DOCUMENT_PART:
 				System.out.println("---Document Part");
 				printResource((INameable)event.getResource());
 				printSkeleton(event.getResource());
 				break;				
 			}
 		}
 	}
 	
 	private void printResource (INameable res) {
 		System.out.println("  id="+res.getId());
 		System.out.println("  name="+res.getName());
 		System.out.println("  type="+res.getType());
 		System.out.println("  mimeType="+res.getMimeType());
 	}
 
 	private void printSkeleton (IResource res) {
 		ISkeleton skel = res.getSkeleton();
 		if ( skel != null ) {
 			System.out.println("---");
 			System.out.println(skel.toString());
 			System.out.println("---");
 		}
 	}
 	
 	
 	
 	private ArrayList<Event> getEvents(String snippet, String srcLang, String trgLang){
 	ArrayList<Event> list = new ArrayList<Event>();
 	filter.open(new RawDocument(snippet, srcLang, trgLang));
 	while ( filter.hasNext() ) {
 		Event event = filter.next();
 		list.add(event);
 	}
 	filter.close();
 	return list;
 	}
 
 	
 	//--without specifying target language--
 	private ArrayList<Event> getEvents(String snippet, String srcLang){
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, srcLang));
 		while ( filter.hasNext() ) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 		}	
 }
