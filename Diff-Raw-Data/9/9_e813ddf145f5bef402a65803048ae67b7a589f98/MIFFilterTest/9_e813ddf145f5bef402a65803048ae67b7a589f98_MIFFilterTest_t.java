 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.mif.tests;
 
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.FilterTestDriver;
 import net.sf.okapi.common.filters.InputDocument;
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.filters.mif.MIFFilter;
 import net.sf.okapi.filters.mif.Parameters;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MIFFilterTest {
 
 	private final static String STARTMIF = "<MIFFile 9.00 <TextFlow <Para ";
 	private final static String ENDMIF = ">>>";
 	
 	private LocaleId locEN = LocaleId.fromString("en");
 
 	private String root;
 	private MIFFilter filter;
 	private GenericContent fmt = new GenericContent();
 	
 	@Before
 	public void setUp() {
 		filter = new MIFFilter();
 		URL url = MIFFilterTest.class.getResource("/Test01.mif");
 		root = Util.getDirectoryName(url.getPath()) + File.separator;
 	}
 
 	@Test
 	public void testDefaultInfo () {
 		//Not using parameters yet: assertNotNull(filter.getParameters());
 		assertNotNull(filter.getName());
 		List<FilterConfiguration> list = filter.getConfigurations();
 		assertNotNull(list);
 		assertTrue(list.size()>0);
 	}
 
 	@Test
 	public void testStartDocument () {
 		assertTrue("Problem in StartDocument", FilterTestDriver.testStartDocument(filter,
 			new InputDocument(root+"Test01.mif", null),
 			null, locEN, locEN));
 	}
 
 	@Test
 	public void testSimpleText () {
 		List<Event> list = getEventsFromFile("Test01.mif", null);
 		ITextUnit tu = FilterTestDriver.getTextUnit(list, 194);
 		assertNotNull(tu);
 		assertEquals("Line 1\nLine 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		
 		tu = FilterTestDriver.getTextUnit(list, 195);
 		assertNotNull(tu);
 		assertEquals("\u00e0=agrave", fmt.setContent(tu.getSource().getFirstContent()).toString());
 	}
 
 	@Test
 	public void testExtractIndexMarkers () {
 		Parameters params = new Parameters();
 		params.setExtractHiddenPages(false);
 		params.setExtractMasterPages(false);
 		params.setExtractReferencePages(false);
 		params.setExtractVariables(false);
 		
 		// Extract index markers
 		List<Event> list = getEventsFromFile("TestMarkers.mif", params);
 		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("Text of marker", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		assertEquals("x-index", tu.getType());
 		
 		// Do not extract index markers
 		params.setExtractIndexMarkers(false);
 		list = getEventsFromFile("TestMarkers.mif", params);
 		tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("Text with index about some subject.", fmt.setContent(tu.getSource().getFirstContent()).toString());
 	}
 
 	@Test
 	public void testExtractLinks () {
 		Parameters params = new Parameters();
 		params.setExtractHiddenPages(false);
 		params.setExtractMasterPages(false);
 		params.setExtractReferencePages(false);
 		params.setExtractVariables(false);
 		
 		// Do not extract links
 		List<Event> list = getEventsFromFile("TestMarkers.mif", params);
 		ITextUnit tu = FilterTestDriver.getTextUnit(list, 5);
 		assertNotNull(tu);
 		assertEquals("text with a link to <1/>http://okapi.opentag.org/", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		
 		// Do extract links
 		params.setExtractLinks(true);
 		list = getEventsFromFile("TestMarkers.mif", params);
 		tu = FilterTestDriver.getTextUnit(list, 5);
 		assertNotNull(tu);
 		assertEquals("http://okapi.opentag.com/", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		assertEquals("link", tu.getType());
 	}
 
 	@Test
 	public void testBodyOnlyNoVariables () {
 		Parameters params = new Parameters();
 		params.setExtractHiddenPages(false);
 		params.setExtractMasterPages(false);
 		params.setExtractReferencePages(false);
 		params.setExtractVariables(false);
 
 		List<Event> list = getEventsFromFile("Test01.mif", params);
 		ITextUnit tu = FilterTestDriver.getTextUnit(list, 1);
 		assertNotNull(tu);
 		assertEquals("Line 1\nLine 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		
 		tu = FilterTestDriver.getTextUnit(list, 2);
 		assertNotNull(tu);
 		assertEquals("\u00e0=agrave", fmt.setContent(tu.getSource().getFirstContent()).toString());
 	}
 
 	@Test
 	public void testSimpleEntry () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <String `text'>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("text", fmt.setContent(tu.getSource().getFirstContent()).toString());
 	}
 	
 	@Test
 	public void testNoTextEntry () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <TextRectID 9> >"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertTrue(tu==null);
 	}
 
 	@Test
 	public void testTwoPartsEntry () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <String `Part 1'><ParaLine <String ` and part 2'>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Part 1 and part 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
 	}
 	
 	@Test
 	public void testEmptyString () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <String `Text 1'><Dummy 1><Char ThinSpace><String `'><Dummy 2><String ` end'>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Text 1<1/>\u2009<2/> end", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		Code code = tu.getSource().getFirstContent().getCode(0);
 		assertEquals("'><Dummy 1><String `", code.getData());
 		code = tu.getSource().getFirstContent().getCode(1);
 		assertEquals("'><Dummy 2><String `", code.getData());
 	}
 	
 	@Test
 	public void testTabs () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <String ` '><Var 1><Char Tab><Char Tab>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertTrue(tu==null);
 	}
 
 	@Test
 	public void testTabsAndCodes () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <Char Tab><Font 1><Var 1><Font 2><Char Tab><ParaLine <Font 3>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertTrue(tu==null);
 		DocumentPart dp = FilterTestDriver.getDocumentPart(getEvents(snippet), 2);
 		assertEquals("<TextFlow <Para <Unique 12345><ParaLine <String `'><Char Tab><String `'><Font 1><Var 1><Font 2><String `'><Char Tab><String `'><ParaLine <Font 3>>>>", dp.getSkeleton().toString());
 	}
 
 	@Test
 	public void testDummyBeforeChar () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <String `Text 1'><Dummy <InDummy 2>><Char ThinSpace><String `Text 2'>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Text 1<1/>\u2009Text 2", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		Code code = tu.getSource().getFirstContent().getCode(0);
 		assertEquals("'><Dummy <InDummy 2>><String `", code.getData());
 	}
 
 	@Test
 	public void testCharOnly () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <Dummy 1><Char Tab><Dummy 2>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertTrue(tu==null);
 	}
 
 	@Test
 	public void testEndsInCharAndCode () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <String `aaa'><Dummy 1><Char Tab><Dummy 2>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("aaa<1/>\t", fmt.setContent(tu.getSource().getFirstContent()).toString());
 	}
 
 	@Test
 	public void testDummyCharString () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <Var 1><Char Tab><String `aaa'>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("\taaa", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		assertEquals("<TextFlow <Para <Unique 12345><ParaLine <Var 1><String `[#$$self$]'>>>", tu.getSkeleton().toString());
 	}
 
 	@Test
 	public void testEmptyFTag () {
 		String snippet = STARTMIF
 			+ "<Unique 12345><ParaLine <Dummy 1><String `Text 1'><Char ThinSpace><Dummy 2><String `5'><Dummy 3>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("Text 1\u2009<1/>5", fmt.setContent(tu.getSource().getFirstContent()).toString());
 		Code code = tu.getSource().getFirstContent().getCode(0);
 		assertEquals("'><Dummy 2><String `", code.getData());
 	}
 	
 	@Test
 	public void testSoftHyphen () {
 		String snippet = STARTMIF
 			+ "<Unique 123><ParaLine <TextRectID 20><String `How'><Char SoftHyphen>>"
 			+ "<ParaLine <String `ever.'>>"
 			+ ENDMIF;
 		ITextUnit tu = FilterTestDriver.getTextUnit(getEvents(snippet), 1);
 		assertNotNull(tu);
 		assertEquals("However.", fmt.setContent(tu.getSource().getFirstContent()).toString());
 	}
 	
 	@Test
 	public void testOutput () {
 		rewriteFile("TestMarkers.mif");
 		rewriteFile("Test03.mif");
 		rewriteFile("Test01.mif");
 		rewriteFile("Test02-v9.mif");
 
 		rewriteFile("Test03_mif7.mif");
 		rewriteFile("Test01-v7.mif");
 	}
 
 	@Test
 	public void testOutputThenCompare () {
 		rewriteThenCompareFile("TestMarkers.mif");
 		rewriteThenCompareFile("Test03.mif");
 		rewriteThenCompareFile("Test01.mif");
 		rewriteThenCompareFile("Test02-v9.mif");
 		rewriteThenCompareFile("Test01.mif");
 		rewriteThenCompareFile("JATest.mif");
 		rewriteThenCompareFile("TestFootnote.mif");
 
 		rewriteThenCompareFile("Test01-v7.mif");
 //		rewriteThenCompareFile("Test03_mif7.mif");
 	}
 	
 //	@Test
 //	public void testDoubleExtraction () {
 //		// Read all files in the data directory
 //		ArrayList<InputDocument> list = new ArrayList<InputDocument>();
 //		list.add(new InputDocument(root+"Test01.mif", null));
 //		list.add(new InputDocument(root+"Test02-v9.mif", null));
 //		list.add(new InputDocument(root+"Test03.mif", null));
 //		list.add(new InputDocument(root+"TestMarkers.mif", null));
 //		list.add(new InputDocument(root+"JATest.mif", null));
 //		list.add(new InputDocument(root+"Test01-v7.mif", null));
 //		
 //		RoundTripComparison rtc = new RoundTripComparison();
 //		assertTrue(rtc.executeCompare(filter, list, "UTF-8", locEN, locEN));
 //	}
 
 	private void rewriteFile (String fileName) {
 		filter.open(new RawDocument(Util.toURI(root+fileName), null, locEN));
 		IFilterWriter writer = filter.createFilterWriter();
 		writer.setOptions(locEN, null);
 		writer.setOutput(root+fileName+".rewrite.mif");
 		while ( filter.hasNext() ) {
 			writer.handleEvent(filter.next());
 		}
 		writer.close();
 		filter.close();
 	}
 
 	private void rewriteThenCompareFile (String fileName) {
 		// Rewrite the file
 		filter.open(new RawDocument(Util.toURI(root+fileName), null, locEN));
 		IFilterWriter writer = filter.createFilterWriter();
 		writer.setOptions(locEN, null);
 		File outFile = new File(root+fileName+".rewrite.mif");
 		outFile.delete();
 		writer.setOutput(outFile.getAbsolutePath());
 		// Store while rewriting
 		ArrayList<Event> list = new ArrayList<Event>();
 		while ( filter.hasNext() ) {
 			Event event = filter.next();
 			list.add(event);
 			writer.handleEvent(event);
 		}
 		writer.close();
 		filter.close();
 		
 		// Read from the rewritten file
 		int i = 0;
 		filter.open(new RawDocument(outFile.toURI(), null, locEN));
 		while ( filter.hasNext() ) {
 			Event event1 = list.get(i++);
 			Event event2 = filter.next();
 			assertTrue(event1.getEventType() == event2.getEventType());
 			if ( event1.getEventType() == EventType.TEXT_UNIT ) {
 				ITextUnit tu1 = event1.getTextUnit();
 				ITextUnit tu2 = event2.getTextUnit();
 				assertEquals(tu1.getSource().getFirstContent().getText(),
 					tu2.getSource().getFirstContent().getText());
 			}
 		}
 		filter.close();
 		
 	}
 
 	private ArrayList<Event> getEventsFromFile (String filename,
 		Parameters params)
 	{
 		ArrayList<Event> list = new ArrayList<Event>();
 		
 		// Switch parameters if needed
 		Parameters oldParams = (Parameters)filter.getParameters();
 		if ( params != null ) {
 			filter.setParameters(params);
 		}
 		filter.open(new RawDocument(Util.toURI(root+filename), null, locEN));
 		while (filter.hasNext()) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		filter.setParameters(oldParams);
 		return list;
 	}
 
 	private ArrayList<Event> getEvents(String snippet) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, locEN));
 		while (filter.hasNext()) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 }
