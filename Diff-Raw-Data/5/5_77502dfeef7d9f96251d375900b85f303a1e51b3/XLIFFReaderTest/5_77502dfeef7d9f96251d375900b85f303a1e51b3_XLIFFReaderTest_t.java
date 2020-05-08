 package net.sf.okapi.lib.xliff;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import net.sf.okapi.lib.xliff.XLIFFEvent.XLIFFEventType;
 
 import org.junit.Test;
 import org.oasisopen.xliff.v2.IAnnotation;
 import org.oasisopen.xliff.v2.ICandidate;
 import org.oasisopen.xliff.v2.ICode;
 import org.oasisopen.xliff.v2.IExtendedAttribute;
 import org.oasisopen.xliff.v2.IExtendedAttributes;
 import org.oasisopen.xliff.v2.IFragment;
 import org.oasisopen.xliff.v2.INote;
 
 public class XLIFFReaderTest {
 
 	@Test
 	public void testWithoutNamespace () {
 		// No namespace declaration
 		String text = "<?xml version='1.0'?>\n"
 			+ "<xliff version=\"2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\">\n<segment>\n<source>Source 1.</source><target>Target 1.</target>\n"
 			+ "</segment>\n<segment>\n<source>Source 2.</source><target>Target 2.</target>\n</segment>\n</unit>\n</file></xliff>";
 		verifyDocument(text);
 	}
 	
 	@Test
 	public void testWithDefaultNamespace () {
 		// XLIFF namespace is the default declaration
 		String text = "<?xml version='1.0'?>\n"
 			+ "<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\">\n<segment>\n<source>Source 1.</source><target>Target 1.</target>\n"
 			+ "</segment>\n<segment>\n<source>Source 2.</source><target>Target 2.</target>\n</segment>\n</unit>\n</file></xliff>";
 		verifyDocument(text);
 	}
 	
 	@Test
 	public void testWithSpecifiedNamespace () {
 		// XLIFF namespace is the default declaration
 		String text = "<?xml version='1.0'?>\n"
 			+ "<x:xliff version=\"2.0\" xmlns:x=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<x:file srclang=\"en\" tgtlang=\"fr\">\n<x:unit id=\"id\">\n<x:segment>\n<x:source>Source 1.</x:source><x:target>Target 1.</x:target>\n"
 			+ "</x:segment>\n<x:segment>\n<x:source>Source 2.</x:source><x:target>Target 2.</x:target>\n</x:segment>\n</x:unit>\n</x:file></x:xliff>";
 		verifyDocument(text);
 	}
 	
 	@Test
 	public void testCDATA () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source><![CDATA[Source 1]]>.</source>"
 			+ "<target>Target<![CDATA[ 1.]]></target>\n"
 			+ "</segment></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		assertEquals("Source 1.", unit.getPart(0).getSource().toString());
 		assertEquals("Target 1.", unit.getPart(0).getTarget(false).toString());
 	}
 	
 	@Test
 	public void testCPElements () {
 		char[] chars = Character.toChars(0x10001);
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source>"
 			+ "<ph id=\"1\">[<cp hex=\"019\"/><cp hex=\"45\"/><cp hex=\"FFFF\"/><cp hex=\"10001\"/>]</ph>"
 			+ "<cp hex=\"019\"/><cp hex=\"45\"/><cp hex=\"FFFF\"/><cp hex=\"10001\"/>"
 			+ "</source>"
 			+ "</segment></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		
 		assertEquals("\u0019\u0045\uFFFF"+chars[0]+chars[1], unit.getPart(0).getSource().getCodedText().substring(2));
 		ICode code = (ICode)unit.getDataStore().getSourceMarkers().get(0);
 		assertEquals("[\u0019\u0045\uFFFF"+chars[0]+chars[1]+"]", code.getOriginalData());
 
 		assertEquals("<ph id=\"1\">[<cp hex=\"0019\"/>\u0045<cp hex=\"FFFF\"/>"+chars[0]+chars[1]+"]</ph><cp hex=\"0019\"/>\u0045<cp hex=\"FFFF\"/>"+chars[0]+chars[1],
 			unit.getPart(0).getSource().toXLIFF(IFragment.STYLE_DATAINSIDE));
 	}
 	
 	@Test
 	public void testBadCPValues () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source>"
 			+ "<ph id=\"1\">[<cp hex=\"_bad1_\"/>]</ph>"
 			+ "a<cp hex=\"_bad2_\"/>z"
 			+ "</source>"
 			+ "</segment></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		
 		assertEquals("a\uFFFDz", unit.getPart(0).getSource().getCodedText().substring(2));
 		ICode code = (ICode)unit.getDataStore().getSourceMarkers().get(0);
 		assertEquals("[\uFFFD]", code.getOriginalData());
 
 		assertEquals("<ph id=\"1\">[\uFFFD]</ph>a\uFFFDz",
 			unit.getPart(0).getSource().toXLIFF(IFragment.STYLE_DATAINSIDE));
 	}
 	
 	@Test
 	public void testInlineCodes () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source>"
 			+ "<sc id=\"1\" equiv=\"eq1\" disp=\"di1\"/>t1<pc id=\"2\" equivStart=\"eq2\" dispStart=\"di2\">t2"
 				+ "<ph id=\"3\" equiv=\"eq3\" disp=\"di3\"/>t3"
 				+ "</pc><ec rid=\"1\" equiv=\"eq1e\" disp=\"di1e\"/>"
 			+ "</source>"
 			+ "</segment></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		
 		ICode code = (ICode)unit.getDataStore().getSourceMarkers().get(0);
 		assertEquals("eq1", code.getEquiv());
 		assertEquals("di1", code.getDisp());
 		code = (ICode)unit.getDataStore().getSourceMarkers().get(1);
 		assertEquals("eq2", code.getEquiv());
 		assertEquals("di2", code.getDisp());
 		code = (ICode)unit.getDataStore().getSourceMarkers().get(2);
 		assertEquals("eq3", code.getEquiv());
 		assertEquals("di3", code.getDisp());
 		
 		assertEquals("<pc id=\"1\" equivStart=\"eq1\" dispStart=\"di1\" equivEnd=\"eq1e\" dispEnd=\"di1e\">t1" +
 				"<pc id=\"2\" equivStart=\"eq2\" dispStart=\"di2\">t2" +
 				"<ph id=\"3\" equiv=\"eq3\" disp=\"di3\"/>t3" +
 				"</pc></pc>",
 			unit.getPart(0).getSource().toXLIFF(IFragment.STYLE_NODATA));
 	}
 	
 	@Test
 	public void testAnnotations () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source>"
 			+ "<sa id=\"1\" type=\"comment\" value=\"my note\"/>t1<ea rid=\"1\"/>"
 			+ "</source>"
 			+ "<target>"
 			+ "<mrk id=\"1\" type=\"comment\" value=\"my note trg\">t1</mrk>"
 			+ "</target>"
 			+ "</segment></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		
 		IAnnotation ann = (IAnnotation)unit.getDataStore().getSourceMarkers().get(0);
 		assertEquals("comment", ann.getType());
 		assertEquals("my note", ann.getValue());
 		assertEquals("<mrk id=\"1\" type=\"comment\" value=\"my note\">t1</mrk>",
 			unit.getPart(0).getSource().toXLIFF(IFragment.STYLE_NODATA));
 		
 		ann = (IAnnotation)unit.getDataStore().getTargetMarkers().get(0);
 		assertEquals("comment", ann.getType());
 		assertEquals("my note trg", ann.getValue());
 		assertEquals("<mrk id=\"1\" type=\"comment\" value=\"my note trg\">t1</mrk>",
 			unit.getPart(0).getTarget(false).toXLIFF(IFragment.STYLE_NODATA));
 	}
 	
 	@Test
 	public void testComments () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source><!--comment-->Source 1.</source>"
 			+ "<target>Target<!--comment--> 1.</target>\n"
 			+ "</segment></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		assertEquals("Source 1.", unit.getPart(0).getSource().toXLIFF());
 		assertEquals("Target 1.", unit.getPart(0).getTarget(false).toXLIFF());
 	}
 	
 	@Test
 	public void testPI () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source><?myPI?>Source 1.</source>"
 			+ "<target>Target<?myPI?> 1.</target>\n"
 			+ "</segment></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		assertEquals("Source 1.", unit.getPart(0).getSource().toString());
 		assertEquals("Target 1.", unit.getPart(0).getTarget(false).toString());
 	}
 	
 	@Test
 	public void testTranslatable () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\">"
 			+ "<segment>"
 			+ "<source>translatable</source>"
 			+ "</segment>"
			+ "<segment translate=\"no\">"
 			+ "<source>non-translatable</source>"
 			+ "</segment>"
			+ "<segment translate=\"yes\">"
 			+ "<source>translatable</source>"
 			+ "</segment>"
 			+ "</unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		assertTrue(((Segment)unit.getPart(0)).isTranslatable());
 		assertFalse(((Segment)unit.getPart(1)).isTranslatable());
 		assertTrue(((Segment)unit.getPart(2)).isTranslatable());
 	}
 	
 	@Test
 	public void testWhiteSpaces () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\">"
 			+ "<segment>"
 			+ "<source>a  b \t c <ph id=\"1\">a  b</ph></source>"
 			+ "</segment>"
 			+ "<segment>"
 			+ "<source xml:space=\"preserve\">a  b \t c <ph id=\"1\">a  b</ph></source>"
 			+ "</segment>"
 			+ "<segment>"
 			+ "<source xml:space=\"default\">a  b \t c <ph id=\"1\">a  b</ph></source>"
 			+ "</segment>"
 			+ "</unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		
 		Segment seg = (Segment)unit.getPart(0);
 		assertEquals("a  b \t c <ph id=\"1\">a  b</ph>", seg.getSource().toXLIFF(IFragment.STYLE_DATAINSIDE));
 		
 	}
 	
 	@Test
 	public void testIgnorables () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><ignorable>\n"
 			+ "<source>  \t</source>"
 			+ "<target>\t\t </target>\n"
 			+ "</ignorable></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		assertEquals("  \t", unit.getPart(0).getSource().toString());
 		assertEquals("\t\t ", unit.getPart(0).getTarget(false).toString());
 	}
 
 	@Test
 	public void testMatches () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source>source</source>"
 			+ "<matches>\n"
 			+ "<match>\n"
 			+ "<source>src-match</source>"
 			+ "<target>trg-match</target>"
 			+ "</match>\n"
 			+ "</matches>\n"
 			+ "</segment>\n"
 			+ "<matches>\n"
 			+ "<match>\n"
 			+ "<source>unit-src-match</source>"
 			+ "<target>unit-trg-match</target>"
 			+ "</match>\n"
 			+ "</matches>\n"
 			+ "</unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		// Test segment-level match
 		Segment seg = (Segment)unit.getPart(0);
 		assertEquals("source", seg.getSource().toString());
 		List<ICandidate> list = seg.getCandidates();
 		assertNotNull(list);
 		assertEquals(1, seg.getCandidateCount());
 		assertEquals("src-match", list.get(0).getSource().toString());
 		assertEquals("trg-match", list.get(0).getTarget().toString());
 		// test unit-level match
 		list = unit.getCandidates();
 		assertEquals(1, unit.getCandidateCount());
 		assertEquals("unit-src-match", list.get(0).getSource().toString());
 		assertEquals("unit-trg-match", list.get(0).getTarget().toString());
 	}
 	
 	@Test
 	public void testNotes () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">\n<unit id=\"id\"><segment>\n"
 			+ "<source>source</source>"
 			+ "<notes>\n"
 			+ "<simpleNote>seg-note</simpleNote>\n"
 			+ "</notes>\n"
 			+ "</segment>\n"
 			+ "<notes>\n"
 			+ "<simpleNote>unit-note</simpleNote>\n"
 			+ "</notes>\n"
 			+ "</unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		// Test segment-level match
 		Segment seg = (Segment)unit.getPart(0);
 		assertEquals("source", seg.getSource().toString());
 		List<INote> list = seg.getNotes();
 		assertNotNull(list);
 		assertEquals(1, seg.getNoteCount());
 		assertEquals("seg-note", list.get(0).getText());
 		// test unit-level match
 		list = unit.getNotes();
 		assertEquals(1, unit.getNoteCount());
 		assertEquals("unit-note", list.get(0).getText());
 	}
 	
 	@Test
 	public void testExtendedAttributes () {
 		String text = "<?xml version='1.0'?>\n<xliff version=\"2.0\" xmlns=\"urn:oasis:names:tc:xliff:document:2.0\" xmlns:x=\"abc\">"
 			+ "<file srclang=\"en\" tgtlang=\"fr\">"
 			+ "<unit id=\"id\" x:a1=\"v1\" x:a2=\"v2\">"
 			+ "<segment x:sa1='sv1'>"
 			+ "<source>src</source>"
 			+ "<target>trg</target>\n"
 			+ "</segment></unit>\n</file></xliff>";
 		Unit unit = getUnit(text, 1);
 		assertNotNull(unit);
 		IExtendedAttributes atts = unit.getExtendedAttributes();
 		assertNotNull(atts);
 		IExtendedAttribute att = atts.getAttribute("abc", "a1");
 		assertNotNull(att);
 		assertEquals("abc", att.getNamespaceURI());
 		assertEquals("x", att.getPrefix());
 		assertEquals("a1", att.getLocalPart());
 		assertEquals("v1", att.getValue());
 
 		Segment seg = (Segment)unit.getPart(0);
 		atts = seg.getExtendedAttributes();
 		assertNotNull(atts);
 		att = atts.getAttribute("abc", "sa1");
 		assertNotNull(att);
 		assertEquals("abc", att.getNamespaceURI());
 		assertEquals("x", att.getPrefix());
 		assertEquals("sa1", att.getLocalPart());
 		assertEquals("sv1", att.getValue());
 	}
 
 	private Unit getUnit (String data,
 		int index)
 	{
 		XLIFFReader reader = new XLIFFReader();
 		reader.open(data);
 		int i = 0;
 		while ( reader.hasNext() ) {
 			XLIFFEvent e = reader.next();
 			if ( e.isUnit() ) {
 				i++;
 				if ( i == index ) {
 					reader.close();
 					return e.getUnit();
 				}
 			}
 		}
 		reader.close();
 		return null;
 	}
 	
 	private void verifyDocument (String data) {
 		XLIFFReader reader = new XLIFFReader();
 		reader.open(data);
 		int i = 0;
 		while ( reader.hasNext() ) {
 			XLIFFEvent e = reader.next();
 			switch ( i ) {
 			case 0:
 				assertTrue(e.getType() == XLIFFEventType.START_DOCUMENT);
 				assertTrue(e.isStartDocument());
 				break;
 			case 1:
 				assertTrue(e.getType() == XLIFFEventType.START_SECTION);
 				assertTrue(e.isStartSection());
 				SectionData sd = e.getSectionData();
 				assertNotNull(sd);
 				assertEquals("en", sd.getSourceLanguage());
 				assertEquals("fr", sd.getTargetLanguage());
 				break;
 			case 2:
 				assertTrue(e.getType() == XLIFFEventType.TEXT_UNIT);
 				assertTrue(e.isUnit());
 				Unit unit = e.getUnit();
 				assertNotNull(unit);
 				assertEquals("Source 1.", unit.getPart(0).getSource().toString());
 				assertEquals("Target 1.", unit.getPart(0).getTarget(false).toString());
 				break;
 			case 3:
 				assertTrue(e.getType() == XLIFFEventType.END_SECTION);
 				assertTrue(e.isEndSection());
 				assertNotNull(e.getSectionData());
 				break;
 			case 4:
 				assertTrue(e.getType() == XLIFFEventType.END_DOCUMENT);
 				assertTrue(e.isEndDocument());
 				break;
 			}
 			i++;
 		}
 		reader.close();
 	}
 
 }
