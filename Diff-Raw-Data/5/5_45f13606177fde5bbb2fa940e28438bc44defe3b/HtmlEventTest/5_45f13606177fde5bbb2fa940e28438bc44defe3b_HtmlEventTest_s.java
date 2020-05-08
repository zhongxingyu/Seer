 /* Copyright (C) 2008 Jim Hargrave
 /*---------------------------------------------------------------------------*/
 /* This library is free software; you can redistribute it and/or modify it   */
 /* under the terms of the GNU Lesser General Public License as published by  */
 /* the Free Software Foundation; either version 2.1 of the License, or (at   */
 /* your option) any later version.                                           */
 /*                                                                           */
 /* This library is distributed in the hope that it will be useful, but       */
 /* WITHOUT ANY WARRANTY; without even the implied warranty of                */
 /* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   */
 /* General Public License for more details.                                  */
 /*                                                                           */
 /* You should have received a copy of the GNU Lesser General Public License  */
 /* along with this library; if not, write to the Free Software Foundation,   */
 /* Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA              */
 /*                                                                           */
 /* See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html */
 /*===========================================================================*/
 
 package net.sf.okapi.filters.html.tests;
 
 import java.net.URL;
 import java.util.ArrayList;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.filters.html.HtmlFilter;
 import net.sf.okapi.filters.tests.FilterTestDriver;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class HtmlEventTest {
 	private HtmlFilter htmlFilter;
 	private URL parameters;
 	
 	@Before
 	public void setUp() throws Exception {
 		htmlFilter = new HtmlFilter();
 		parameters = HtmlEventTest.class.getResource("testConfiguration1.yml");
 	}
 	
 	@Test
 	public void testWithDefaultConfig() {
 		URL originalParameters = parameters;
 		parameters = HtmlFilter.class.getResource("defaultConfiguration.yml");
 		
 		testMetaTagContent();
 		testLang();
 		testXmlLang();
 		testMETATagWithLanguage();
 		testMETATagWithEncoding();
 		
 		parameters = originalParameters;
 	}
 	
 	@Test
 	public void testHtmlKeywordsNotExtracted() {
 		URL originalParameters = parameters;
 		parameters = HtmlFilter.class.getResource("defaultConfiguration.yml");
 		
 		String snippet = "<meta http-equiv=\"keywords\" content=\"keyword text\"/>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		// Build the input
 		GenericSkeleton skel = new GenericSkeleton();
 		TextUnit tu = new TextUnit("tu1", "keyword text");
 		skel.add("content=\"");
 		skel.addContentPlaceholder(tu);
 		skel.add("\"");
 		tu.setIsReferent(true);
 		tu.setName("content");
 		tu.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu));
 
 		skel = new GenericSkeleton();
 		DocumentPart dp = new DocumentPart("dp1", false);
 		skel.add("<meta http-equiv=\"keywords\" ");
 		skel.addReference(tu);
 		skel.add("/>");
 		dp.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 		
 		parameters = originalParameters;
 	}
 	
 	@Test
 	public void testMetaTagContent() {
 		String snippet = "<meta http-equiv=\"keywords\" content=\"one,two,three\"/>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		// Build the input
 		GenericSkeleton skel = new GenericSkeleton();
 		TextUnit tu = new TextUnit("tu1", "one,two,three");
 		skel.add("content=\"");
 		skel.addContentPlaceholder(tu);
 		skel.add("\"");
 		tu.setIsReferent(true);
 		tu.setName("content");
 		tu.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu));
 
 		skel = new GenericSkeleton();
 		DocumentPart dp = new DocumentPart("dp1", false);
 		skel.add("<meta http-equiv=\"keywords\" ");
 		skel.addReference(tu);
 		skel.add("/>");
 		dp.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 
 	@Test
 	public void testPWithAttributes() {
 		String snippet = "<p title='my title' dir='rtl'>Text of p</p>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		// Build the input
 		GenericSkeleton skel = new GenericSkeleton();
 		TextUnit tu2 = new TextUnit("tu2", "my title");
 		skel.add("title='");
 		skel.addContentPlaceholder(tu2);
 		skel.add("'");
 		tu2.setIsReferent(true);
 		tu2.setName("title");
 		tu2.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu2));
 
 		skel = new GenericSkeleton();
 		TextUnit tu1 = new TextUnit("tu1", "Text of p");
 		skel.add("<p ");
 		skel.addReference(tu2);
 		skel.add(" dir='");
 		skel.addValuePlaceholder(tu1, "dir", null);		
 		skel.add("'>");
 		skel.addContentPlaceholder(tu1);
 		skel.append("</p>");
 		tu1.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu1));
 
 		addEndEvents(events);
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 	
 	@Test
 	public void testLang() {
 		String snippet = "<dummy lang=\"en\"/>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		// Build the input
 		GenericSkeleton skel = new GenericSkeleton();
 		DocumentPart dp1 = new DocumentPart("dp1", false);
 		skel.add("<dummy lang=\"");
 		dp1.setSourceProperty(new Property("language", "en", false));
 		skel.addValuePlaceholder(dp1, "language", null);		
 		skel.add("\"/>");
 		dp1.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp1));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 	
 	@Test
 	public void testXmlLang() {
 		String snippet = "<yyy xml:lang=\"en\"/>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		// Build the input
 		GenericSkeleton skel = new GenericSkeleton();
 		DocumentPart dp1 = new DocumentPart("dp1", false);
 		skel.add("<yyy xml:lang=\"");
 		dp1.setSourceProperty(new Property("language", "en", false));
 		skel.addValuePlaceholder(dp1, "language", null);		
 		skel.add("\"/>");
 		dp1.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp1));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 
 	@Test
 	public void testComplexEmptyElement() {
 		String snippet = "<dummy write=\"w\" readonly=\"ro\" trans=\"tu1\"/>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		// Build the input
 		GenericSkeleton skel = new GenericSkeleton();
 		TextUnit tu = new TextUnit("tu1", "tu1");
 		skel.add("trans=\"");
 		skel.addContentPlaceholder(tu);
 		skel.add("\"");
 		tu.setIsReferent(true);
 		tu.setName("content");
 		tu.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu));
 
 		skel = new GenericSkeleton();
 		DocumentPart dp = new DocumentPart("dp1", false);
 		skel.add("<dummy write=\"");
 		dp.setSourceProperty(new Property("write", "w", false));
 		skel.addValuePlaceholder(dp, "write", null);
 		dp.setSourceProperty(new Property("readonly", "ro", true));
 		skel.add("\" readonly=\"ro\" ");
 		skel.addReference(tu);
 		skel.add("/>");
 		dp.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 
 	@Test
 	public void testPWithInlines() {
 		String snippet = "<p>Before <b>bold</b> <a href=\"there\"/> after.</p>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		GenericSkeleton skel = new GenericSkeleton();
 		DocumentPart dp1 = new DocumentPart("dp1", true);
 		skel.add("<a href=\"");
 		skel.addValuePlaceholder(dp1, "href", null);
 		dp1.setSourceProperty(new Property("href", "there", false));
 		skel.add("\"/>");
 		dp1.setName("a");
 		dp1.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp1));
 
 		skel = new GenericSkeleton();
 		TextUnit tu1 = new TextUnit("tu1", "Before ");
 		TextFragment tf = tu1.getSourceContent();
 		Code code = new Code(TagType.OPENING, "b", "<b>");
 		code.setType(Code.TYPE_BOLD);
 		tf.append(code);
 		tf.append("bold");
 		code = new Code(TagType.CLOSING, "b", "</b>");
 		code.setType(Code.TYPE_BOLD);
 		tf.append(code);
 		tf.append(" ");
 		code = new Code(TagType.PLACEHOLDER, "a");
 		code.setType(Code.TYPE_LINK);
 		code.appendReference("dp1");
 		tf.append(code);
 		tf.append(" after.");
 		skel.add("<p>");
 		skel.addContentPlaceholder(tu1);
 		skel.append("</p>");
 		tu1.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu1));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 
 	@Test
 	public void testPWithComment() {
 		URL originalParameters = parameters;
		parameters = HtmlSnippetsTest.class.getResource("minimalistConfiguration.yml");
 		
 		String snippet = "<p>Before <!--comment--> after.</p>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		events.add(new Event(EventType.DOCUMENT_PART, new DocumentPart("dp1",false, new GenericSkeleton("<p>"))));
 		TextUnit tu1 = new TextUnit("tu1", "Before ");
 		TextFragment tf = tu1.getSourceContent();
 		Code code = new Code(TagType.PLACEHOLDER, Code.TYPE_COMMENT, "<!--comment-->");
 		tf.append(code);
 		tf.append(" after.");
 		events.add(new Event(EventType.TEXT_UNIT, tu1));
 		events.add(new Event(EventType.DOCUMENT_PART, new DocumentPart("dp2",false, new GenericSkeleton("</p>"))));		
 		
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 		parameters = originalParameters;
 	}
 
 	@Test
 	public void testPWithProcessingInstruction() {
 		URL originalParameters = parameters;
		parameters = HtmlSnippetsTest.class.getResource("minimalistConfiguration.yml");
 
 		String snippet = "<p>Before <?PI?> after.</p>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		events.add(new Event(EventType.DOCUMENT_PART, new DocumentPart("dp1",false, new GenericSkeleton("<p>"))));
 		TextUnit tu1 = new TextUnit("tu1", "Before ");
 		TextFragment tf = tu1.getSourceContent();
 		Code code = new Code(TagType.PLACEHOLDER, Code.TYPE_XML_PROCESSING_INSTRUCTION, "<?PI?>");		
 		tf.append(code);
 		tf.append(" after.");
 		events.add(new Event(EventType.TEXT_UNIT, tu1));
 		events.add(new Event(EventType.DOCUMENT_PART, new DocumentPart("dp2",false, new GenericSkeleton("</p>"))));
 		
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 		parameters = originalParameters;
 	}
 
 	@Test
 	public void testMETATagWithLanguage() {
 		String snippet = "<meta http-equiv=\"Content-Language\" content=\"en\"/>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		GenericSkeleton skel = new GenericSkeleton();
 		DocumentPart dp = new DocumentPart("dp1", false);
 		skel.add("<meta http-equiv=\"Content-Language\" content=\"");
 		skel.addValuePlaceholder(dp, "language", null);
 		skel.add("\"/>");
 		dp.setSourceProperty(new Property("language", "en", false));
 		dp.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 	
 	@Test
 	public void testMETATagWithEncoding() {
 		String snippet = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-2022-JP\">";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		GenericSkeleton skel = new GenericSkeleton();
 		DocumentPart dp = new DocumentPart("dp1", false);
 		skel.add("<meta http-equiv=\"Content-Type\" content=\"");
 		skel.add("text/html; charset=");
 		skel.addValuePlaceholder(dp, "encoding", null);
 		skel.add("\">");
 		dp.setSourceProperty(new Property("encoding", "ISO-2022-JP", false));
 		dp.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 
 	@Test
 	public void testPWithInlines2() {
 		String snippet = "<p>Before <b>bold</b> <img href=\"there\" alt=\"text\"/> after.</p>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		GenericSkeleton skel = new GenericSkeleton();
 		TextUnit tu2 = new TextUnit("tu2", "text");
 		skel.add("alt=\"");
 		skel.addContentPlaceholder(tu2);
 		skel.add("\"");
 		tu2.setIsReferent(true);
 		tu2.setName("alt");
 		tu2.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu2));
 
 		skel = new GenericSkeleton();
 		DocumentPart dp1 = new DocumentPart("dp1", true);
 		skel.add("<img href=\"");
 		dp1.setSourceProperty(new Property("href", "there", false));
 		skel.addValuePlaceholder(dp1, "href", null);
 		skel.add("\" ");
 		skel.addReference(tu2);
 		skel.add("/>");
 		dp1.setIsReferent(true);
 		dp1.setName("img");
 		dp1.setSkeleton(skel);
 		events.add(new Event(EventType.DOCUMENT_PART, dp1));
 
 		skel = new GenericSkeleton();
 		TextUnit tu1 = new TextUnit("tu1", "Before ");
 		TextFragment tf = tu1.getSourceContent();
 		Code code = new Code(TagType.OPENING, "b", "<b>");
 		code.setType(Code.TYPE_BOLD);
 		tf.append(code);
 		tf.append("bold");
 		code = new Code(TagType.CLOSING, "b", "</b>");
 		code.setType(Code.TYPE_BOLD);
 		tf.append(code);
 		tf.append(" ");
 		code = new Code(TagType.PLACEHOLDER, "img");
 		code.setType(Code.TYPE_IMAGE);
 		code.appendReference("dp1");
 		tf.append(code);
 		tf.append(" after.");
 		skel.add("<p>");
 		skel.addContentPlaceholder(tu2);
 		skel.append("</p>");
 		tu1.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu1));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 
 	@Test
 	public void testTableGroups() {
 		String snippet = "<table id=\"100\"><tr><td>text</td></tr></table>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 
 		StartGroup g1 = new StartGroup("ssd0", "sg1");
 		g1.setSkeleton(new GenericSkeleton("<table id=\"100\">"));
 		events.add(new Event(EventType.START_GROUP, g1));
 
 		StartGroup g2 = new StartGroup("sg1", "sg2");
 		g2.setSkeleton(new GenericSkeleton("<tr>"));
 		events.add(new Event(EventType.START_GROUP, g2));
 
 		StartGroup g3 = new StartGroup("sg2", "sg3");
 		g3.setSkeleton(new GenericSkeleton("<td>"));
 		events.add(new Event(EventType.START_GROUP, g3));
 
 		events.add(new Event(EventType.TEXT_UNIT, new TextUnit("tu1", "text")));
 
 		Ending e1 = new Ending("eg1");
 		e1.setSkeleton(new GenericSkeleton("</td>"));
 		events.add(new Event(EventType.END_GROUP, e1));
 
 		Ending e2 = new Ending("eg2");
 		e2.setSkeleton(new GenericSkeleton("</tr>"));
 		events.add(new Event(EventType.END_GROUP, e2));
 
 		Ending e3 = new Ending("eg3");
 		e3.setSkeleton(new GenericSkeleton("</table>"));
 		events.add(new Event(EventType.END_GROUP, e3));
 
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 
 	@Test
 	public void testGroupInPara() {
 		String snippet = "<p>Text before list:" + 
 		"<ul>" + 
 		"<li>Text of item 1</li>" + 
 		"<li>Text of item 2</li>" + 
 		"</ul>" + "and text after the list.</p>";
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 		
 		GenericSkeleton skel = new GenericSkeleton();
 		TextUnit tu3 = new TextUnit("tu3", "Text before list:");
 		
 		// embedded groups
 		StartGroup g1 = new StartGroup("tu3", "sg1");
 		g1.setIsReferent(true);
 		g1.setSkeleton(new GenericSkeleton("<ul>"));
 		events.add(new Event(EventType.START_GROUP, g1));
 
 		StartGroup g2 = new StartGroup("sg1", "sg2");
 		g2.setSkeleton(new GenericSkeleton("<li>"));
 		events.add(new Event(EventType.START_GROUP, g2));
 
 		events.add(new Event(EventType.TEXT_UNIT, new TextUnit("tu1", "Text of item 1")));
 		
 		Ending e1 = new Ending("eg1");
 		e1.setSkeleton(new GenericSkeleton("</li>"));
 		events.add(new Event(EventType.END_GROUP, e1));
 
 		StartGroup g3 = new StartGroup("sg1", "sg3");
 		g3.setSkeleton(new GenericSkeleton("<li>"));
 		events.add(new Event(EventType.START_GROUP, g3));
 
 		events.add(new Event(EventType.TEXT_UNIT, new TextUnit("tu2", "Text of item 2")));
 
 		Ending e2 = new Ending("eg2");
 		e2.setSkeleton(new GenericSkeleton("</li>"));
 		events.add(new Event(EventType.END_GROUP, e2));
 
 		Ending e3 = new Ending("eg3");
 		e3.setSkeleton(new GenericSkeleton("</ul>"));
 		events.add(new Event(EventType.END_GROUP, e3));
 		
 		TextFragment tf = tu3.getSourceContent();
 		Code c = new Code(TagType.PLACEHOLDER, "ul", TextFragment.makeRefMarker("sg1"));
 		c.setReferenceFlag(true);
 		tf.append(c);
 		
 		tf.append("and text after the list.");
 		skel.add("<p>");
 		skel.addContentPlaceholder(tu3);
 		skel.append("</p>");
 		tu3.setSkeleton(skel);
 		events.add(new Event(EventType.TEXT_UNIT, tu3));
 		
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 	
 	@Test
 	public void testPreserveWhitespace() {
 		String snippet = "<pre>\twhitespace is preserved</pre>"; 
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		addStartEvents(events);
 		
 		GenericSkeleton skel = new GenericSkeleton();		
 		DocumentPart dp = new DocumentPart("dp1", false);
 		
 		events.add(new Event(EventType.DOCUMENT_PART, dp));
 		skel.add("<pre>");
 		dp.setSkeleton(skel);
 		
 		TextUnit tu = new TextUnit("tu1", "\twhitespace is preserved");
 		tu.setPreserveWhitespaces(true);
 		events.add(new Event(EventType.TEXT_UNIT, tu));
 		
 		skel = new GenericSkeleton();
 		dp = new DocumentPart("dp2", false);
 		events.add(new Event(EventType.DOCUMENT_PART, dp));
 		skel.add("</pre>");
 		dp.setSkeleton(skel);
 		
 		addEndEvents(events);
 
 		assertTrue(FilterTestDriver.compareEvents(events, getEvents(snippet)));
 	}
 
 	private ArrayList<Event> getEvents(String snippet) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		htmlFilter.setParametersFromURL(parameters);
 		htmlFilter.open(snippet);
 		while (htmlFilter.hasNext()) {
 			Event event = htmlFilter.next();
 			list.add(event);
 		}
 		htmlFilter.close();
 		return list;
 	}
 
 	private void addStartEvents(ArrayList<Event> events) {		
 		events.add(new Event(EventType.START_DOCUMENT));
 	}
 
 	private void addEndEvents(ArrayList<Event> events) {
 		events.add(new Event(EventType.END_DOCUMENT));
 	}
 }
