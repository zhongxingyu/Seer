 /*===========================================================================
   Copyright (C) 2010-2012 by the Okapi Framework contributors
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
 
 package org.w3c.its;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URI;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.resource.TextFragment;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class TraversalTest {
 
 	private String root = TestUtil.getParentDir(this.getClass(), "/input.xml");
 	//private LocaleId locEN = LocaleId.fromString("en");
 	private DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
 
 	@Before
 	public void setUp() {
 		fact.setNamespaceAware(true);
 		fact.setValidating(false);
 	}
 
 	@Test
 	public void testSimple () throws SAXException, IOException, ParserConfigurationException {
 		Document doc = fact.newDocumentBuilder().parse(root + "/input.xml");
 		ITraversal trav = applyITSRules(doc, new File(root + "/input.xml").toURI(), false, null);
 		Element elem = getElement(trav, "p", 1);
 		assertNotNull(elem);
 		assertTrue(trav.getTranslate(null));
 		elem = getElement(trav, "term", 1);
 		assertNotNull(elem);
 		assertFalse(trav.getTranslate(null));
 	}
 
 	@Test
 	public void testTerm () throws SAXException, IOException, ParserConfigurationException {
 		Document doc = fact.newDocumentBuilder().parse(root + "/input.xml");
 		ITraversal trav = applyITSRules(doc, new File(root + "/input.xml").toURI(), false, null);
 		Element elem = getElement(trav, "p", 1);
 		assertNotNull(elem);
 		assertFalse(trav.getTerm(null));
 		elem = getElement(trav, "term", 1);
 		assertNotNull(elem);
 		assertTrue(trav.getTerm(null));
 		// This is empty because ref in id(@ref) is not defined as IDType
 		// So no text is detected
 		assertEquals("", trav.getTermInfo(null));
 	}
 
 	@Test
 	public void testXmlId () throws SAXException, IOException, ParserConfigurationException {
 		Document doc = fact.newDocumentBuilder().parse(root + "/input.xml");
 		ITraversal trav = applyITSRules(doc, new File(root + "/input.xml").toURI(), false, null);
 		Element elem = getElement(trav, "gloss", 1);
 		assertNotNull(elem);
 		assertEquals("TDPV", trav.getIdValue(null));
 	}
 
 	@Test
 	public void testLocNote () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:locNoteRule selector='//p' locNoteType='description' locNotePointer='@note'/>"
 			+ "</i:rules>"
 			+ "<p note='some note'>text</p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "p", 1);
 		assertNotNull(elem);
 		assertEquals("some note", trav.getLocNote(null));
 		assertEquals("description", trav.getLocNoteType(null));
 	}
 
 	@Test
 	public void testWithinText () throws SAXException, IOException, ParserConfigurationException {
 		Document doc = fact.newDocumentBuilder().parse(root + "/Translate1.xml");
 		ITraversal trav = applyITSRules(doc, new File(root + "/Translate1.xml").toURI(), false, null);
 		Element elem = getElement(trav, "verbatim", 1);
 		assertNotNull(elem);
 		assertFalse(trav.getTranslate(null));
 		assertTrue(trav.getWithinText()==ITraversal.WITHINTEXT_YES);
 	}
 
 	@Test
 	public void testDomainGlobal () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:domainRule selector='//doc' domainPointer='head/subject' "
 			+ " domainMapping=\"dom1 finalDom1, 'dom2 VAL' 'final dom2'\"/>"
 			+ "</i:rules>"
 			+ "<head><subject>dom1, dom3</subject><subject>'dom2 val', \"Dom4\"</subject></head><p>text</p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "doc", 1);
 		assertNotNull(elem);
 		assertEquals("finalDom1, dom3, final dom2, Dom4", trav.getDomains(null));
 		elem = getElement(trav, "head", 1);
 		assertNotNull(elem);
 		assertEquals("finalDom1, dom3, final dom2, Dom4", trav.getDomains(null));
 		elem = getElement(trav, "p", 1);
 		assertNotNull(elem);
 		assertEquals("finalDom1, dom3, final dom2, Dom4", trav.getDomains(null));
 	}
 
 	@Test
 	public void testTargetPointerGlobal () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:targetPointerRule selector='//entry/src' targetPointer='../trg'/>"
 			+ "</i:rules>"
 			+ "<entry><src>source</src><trg></trg></entry></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "entry", 1);
 		assertNotNull(elem);
 		assertTrue(trav.getTranslate(null));
 		assertEquals(null, trav.getTargetPointer(null));
 		elem = getElement(trav, "src", 1);
 		assertNotNull(elem);
 		assertTrue(trav.getTranslate(null));
 		assertEquals("../trg", trav.getTargetPointer(null));
 	}
 
 	@Test
 	public void testTargetPointerAttributes () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:targetPointerRule selector='//entry/@src' targetPointer='../@trg'/>"
 			+ "</i:rules>"
 			+ "<entry src='source' trg='' /></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "entry", 1);
 		assertNotNull(elem);
 		Attr attr = elem.getAttributeNode("src");
 		assertEquals("../@trg", trav.getTargetPointer(attr));
 	}
 
 	@Test
 	public void testLocaleFilter () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<book xmlns:its=\"http://www.w3.org/2005/11/its\">"
 			+ "<info>"
 			+ "<its:rules version=\"2.0\">"
 			+ "<its:localeFilterRule selector=\"//legalnotice[@role='Canada']\" localeFilterList=\"en-CA, fr-CA\"/>"
 			+ "</its:rules>"
 			+ "<legalnotice role=\"Canada\">"
 			+ "<para>This legal notice is only for Canadian locales.</para>"
 			+ "<para its:localeFilterList='*'>This text is for all locales.</para>"
 			+ "</legalnotice>"
 			+ "</info></book>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "para", 1);
 		assertNotNull(elem);
 		assertEquals("en-CA, fr-CA", trav.getLocaleFilter());
 		elem = getElement(trav, "para", 2);
 		assertNotNull(elem);
 		assertEquals("*", trav.getLocaleFilter());
 	}
 
 	@Test
 	public void testPreserveSpaces () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<book xmlns:its=\"http://www.w3.org/2005/11/its\">"
 			+ "<info>"
 			+ "<its:rules version=\"2.0\">"
 			+ "<its:preserveSpaceRule selector=\"//pre\" space=\"preserve\"/>"
 			+ "</its:rules>"
 			+ "<p> a  b  c  </p>"
 			+ "<pre> a  b  c  </pre>"
 			+ "</info></book>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "p", 1);
 		assertNotNull(elem);
 		assertFalse(trav.preserveWS());
 		elem = getElement(trav, "pre", 1);
 		assertTrue(trav.preserveWS());
 	}
 
 	@Test
 	public void testAllowedCharsGlobal () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:allowedCharactersRule selector='//p' allowedCharacters='[a-zA-Z]'/>"
 			+ "<i:allowedCharactersRule selector='//p/@abc' allowedCharacters='[0-9]'/>"
 			+ "</i:rules>"
 			+ "<p abc='123'>text</p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "p", 1);
 		assertEquals("[a-zA-Z]", trav.getAllowedCharacters(null));
 		Attr attr = elem.getAttributeNode("abc");
 		assertEquals("[0-9]", trav.getAllowedCharacters(attr));
 	}
 
 	@Test
 	public void testAllowedCharsLocal () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i=\"http://www.w3.org/2005/11/its\" version='2.0'>"
 			+ "<p i:allowedCharacters='[abc]'>ab</p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "p", 1);
 		assertEquals("[abc]", trav.getAllowedCharacters(null));
 	}
 
 	@Test
 	public void testStorageSizeGlobal1 () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:storageSizeRule selector='//p' storageSize='255' lineBreakType='crlf'/>"
 			+ "</i:rules>"
 			+ "<p>Text</p><q>text</q></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "p", 1);
 		assertEquals("255", trav.getStorageSize(null));
 		assertEquals("UTF-8", trav.getStorageEncoding(null));
 		assertEquals("crlf", trav.getLineBreakType(null));
 		getElement(trav, "q", 1); // Not inherited
 		assertEquals(null, trav.getStorageSize(null));
 		assertEquals("UTF-8", trav.getStorageEncoding(null));
 		assertEquals("lf", trav.getLineBreakType(null));
 	}
 
 	@Test
 	public void testStorageSizeGlobal2 () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:storageSizeRule selector='//p' lineBreakType='cr' storageSizePointer='@max' storageEncodingPointer='@enc'/>"
 			+ "</i:rules>"
 			+ "<p max='222' enc='UTF-16'>Text<b>text</b></p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "p", 1);
 		assertEquals("222", trav.getStorageSize(null));
 		assertEquals("UTF-16", trav.getStorageEncoding(null));
 		assertEquals("cr", trav.getLineBreakType(null));
 		getElement(trav, "b", 1);
 		assertEquals(null, trav.getStorageSize(null));
 		assertEquals("UTF-8", trav.getStorageEncoding(null));
 		assertEquals("lf", trav.getLineBreakType(null));
 	}
 	
 	@Test
 	public void testStorageSizeLocal1 () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i=\"http://www.w3.org/2005/11/its\" version='2.0'>"
 			+ "<p i:storageEncoding='Shift-JIS' i:storageSize='111'>text</p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "p", 1);
 		assertEquals("111", trav.getStorageSize(null));
 		assertEquals("Shift-JIS", trav.getStorageEncoding(null));
 		assertEquals("lf", trav.getLineBreakType(null));
 	}
 
 	@Test
 	public void testLQIssueGlobal1 () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:locQualityIssueRule selector='//z' locQualityIssueTypePointer='@type' locQualityIssueCommentPointer='@comment'"
 			+ " locQualityIssueSeverityPointer='@score' locQualityIssueProfileRefPointer='@pref'/>"
 			+ "</i:rules>"
 			+ "<p>Text with <z type='other' comment='comment' pref='uri' score='1'>error</z></p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "z", 1);
 		assertEquals("other", trav.getLocQualityIssueType(null, 0));
 		assertEquals("comment", trav.getLocQualityIssueComment(null, 0));
 		assertEquals(1.0, trav.getLocQualityIssueSeverity(null, 0), 0);
 		assertEquals("uri", trav.getLocQualityIssueProfileRef(null, 0));
 		assertEquals(true, trav.getLocQualityIssueEnabled(null, 0));
 	}
 
 	@Test
 	public void testLQIssueGlobal2 () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<i:rules version='2.0'>"
 			+ "<i:locQualityIssueRule selector='//z' locQualityIssuesRefPointer='@ref' />"
 			+ "</i:rules>"
 			+ "<p>Text with <z ref='#id1'>error</z></p>"
 			+ "<i:locQualityIssues xml:id='id1'>"
 			+ "<i:locQualityIssue locQualityIssueEnabled='no' locQualityIssueComment='comment1'/>"
 			+ "<i:locQualityIssue locQualityIssueProfileRef='pref2' locQualityIssueSeverity='50' locQualityIssueComment='comment2'/>"
 			+ "</i:locQualityIssues>"
 			+ "</doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "z", 1);
 		assertEquals("#id1", trav.getLocQualityIssuesRef(null));
 		assertEquals(2, trav.getLocQualityIssueCount(null));
 		// Values
 		assertEquals(null, trav.getLocQualityIssueType(null, 0));
 		assertEquals(null, trav.getLocQualityIssueType(null, 1));
 		assertEquals("comment1", trav.getLocQualityIssueComment(null, 0));
 		assertEquals("comment2", trav.getLocQualityIssueComment(null, 1));
 		assertEquals(null, trav.getLocQualityIssueSeverity(null, 0));
 		assertEquals(50.0, trav.getLocQualityIssueSeverity(null, 1), 0);
 		assertEquals(null, trav.getLocQualityIssueProfileRef(null, 0));
 		assertEquals("pref2", trav.getLocQualityIssueProfileRef(null, 1));
 		assertEquals(false, trav.getLocQualityIssueEnabled(null, 0));
 		assertEquals(true, trav.getLocQualityIssueEnabled(null, 1));
 	}
 
 	@Test
 	public void testLQIssueGlobalAndLocal1 () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:locQualityIssueRule selector='//z' locQualityIssueTypePointer='@type' locQualityIssueCommentPointer='@comment'"
 			+ " locQualityIssueSeverityPointer='@score' locQualityIssueProfileRefPointer='@pref'/>"
 			+ "</i:rules>"
 			+ "<p>Text with <z type='other' comment='comment' pref='uri' score='1'"
 			+ " i:locQualityIssueType='terminology' i:locQualityIssueProfileRef='thisUri'>error</z></p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "z", 1);
 		// Local markup overrides everything, even undefined data (complete overriding rule in ITS)
 		assertEquals(null, trav.getLocQualityIssuesRef(null));
 		assertEquals(null, trav.getLocQualityIssueComment(null, 0));
 		assertEquals(null, trav.getLocQualityIssueSeverity(null, 0));
 		assertEquals("terminology", trav.getLocQualityIssueType(null, 0));
 		assertEquals("thisUri", trav.getLocQualityIssueProfileRef(null, 0));
 		assertEquals(true, trav.getLocQualityIssueEnabled(null, 0));
 	}
 
 	@Test
 	public void testLQIssueOnAttributes () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:locQualityIssueRule selector='//p/@abc' locQualityIssuesRef='#id1'/>"
 			+ "</i:rules>"
 			+ "<p abc='some text'>Text</p>"
 			+ "<i:locQualityIssues xml:id='id1'>"
 			+ "<i:locQualityIssue locQualityIssueEnabled='no' locQualityIssueComment='comment1'/>"
 			+ "<i:locQualityIssue locQualityIssueProfileRef='pref2' locQualityIssueSeverity='50' locQualityIssueComment='comment2'/>"
 			+ "</i:locQualityIssues>"
 			+ "</doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "p", 1);
 		Attr attr = elem.getAttributeNode("abc");
 		assertEquals("#id1", trav.getLocQualityIssuesRef(attr));
 		assertEquals(2, trav.getLocQualityIssueCount(attr));
 		assertEquals(null, trav.getLocQualityIssueType(attr, 0));
 		assertEquals(null, trav.getLocQualityIssueType(attr, 1));
 		assertEquals("comment1", trav.getLocQualityIssueComment(attr, 0));
 		assertEquals("comment2", trav.getLocQualityIssueComment(attr, 1));
 		assertEquals(null, trav.getLocQualityIssueSeverity(attr, 0));
 		assertEquals(50.0, trav.getLocQualityIssueSeverity(attr, 1), 0);
 		assertEquals(null, trav.getLocQualityIssueProfileRef(attr, 0));
 		assertEquals("pref2", trav.getLocQualityIssueProfileRef(attr, 1));
 		assertEquals(false, trav.getLocQualityIssueEnabled(attr, 0));
 		assertEquals(true, trav.getLocQualityIssueEnabled(attr, 1));
 	}
 
 	@Test
 	public void testToolsRef () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<group i:toolsRef='terminology|uri2 mt-confidence|uri1'>"
 			+ "<p i:toolsRef='disambiguation|uriDisamb'>Text with <z i:toolsRef='terminology|uri3'"
 			+ " i:term='yes'>a term</z></p></group></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "group", 1);
 		assertEquals("mt-confidence|uri1 terminology|uri2", trav.getToolsRef());
 		getElement(trav, "p", 1);
		assertEquals("disambiguation|uriDisamb mt-confidence|uri1 terminology|uri2", trav.getToolsRef());
 		getElement(trav, "z", 1);
		assertEquals("disambiguation|uriDisamb mt-confidence|uri1 terminology|uri3", trav.getToolsRef());
 	}
 	
 	@Test
 	public void testToolsRefBadValue () throws SAXException, IOException, ParserConfigurationException {
 		// Should pass without exception, but generate an error in the log.
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<group i:toolsRef='Invalid-value-for-test|uri1'>"
 			+ "<p>Text with</p></group></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "group", 1);
 		assertEquals("Invalid-value-for-test|uri1", trav.getToolsRef());
 	}
 	
 	@Test
 	public void testQueryLanguage () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' queryLanguage='xpath2' version='2.0'>"
 			+ "<i:translateRule selector='//par/@title' translate='yes' />"
 			+ "<i:translateRule selector='//par/@alt' translate='yes' />"
 			+ "</i:rules>"
 			+ "<par title='title text' test='test' alt='alt text'>Text</par></doc>"));
 		// Passes but should generate a warning in the log
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "par", 1);
 		assertTrue(trav.getTranslate(elem.getAttributeNode("title")));
 	}
 
 	@Test (expected=ITSException.class)
 	public void testBadQueryLanguage () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' queryLanguage='invalid-value' version='2.0'>"
 			+ "<i:translateRule selector='//par/@title' translate='yes' />"
 			+ "<i:translateRule selector='//par/@alt' translate='yes' />"
 			+ "</i:rules>"
 			+ "<par title='title text' test='test' alt='alt text'>Text</par></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 	}
 	
 	@Test
 	public void testIdValueOnAttribute () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:translateRule selector='//elem/@myText' translate='yes' />"
 			+ "<i:idValueRule selector='//elem/@myText' idValue='../@myId' />"
 			+ "</i:rules>"
 			+ "<elem myText='text' myId='id1'/>"
 			+ "</doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "elem", 1);
 		assertTrue(trav.getTranslate(elem.getAttributeNode("myText")));
 		assertEquals("id1", trav.getIdValue(elem.getAttributeNode("myText")));
 	}
 
 	@Test
 	public void testTermOnAttribute () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:translateRule selector='//elem/@myText' translate='yes' />"
 			+ "<i:termRule selector='//elem/@myText' term='yes' termInfoPointer='../@myInfo' />"
 			+ "</i:rules>"
 			+ "<elem myText='text' myInfo='some info'/>"
 			+ "</doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "elem", 1);
 		assertTrue(trav.getTerm(elem.getAttributeNode("myText")));
 		assertEquals("some info", trav.getTermInfo(elem.getAttributeNode("myText")));
 	}
 	
 	@Test
 	public void testLQIssueLocal1 () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<p>Text with <z i:locQualityIssueType='other' i:locQualityIssueComment='comment'"
 			+ " i:locQualityIssueProfileRef='uri' i:locQualityIssueSeverity='1.0'>error</z></p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "z", 1);
 		assertEquals("other", trav.getLocQualityIssueType(null, 0));
 		assertEquals("comment", trav.getLocQualityIssueComment(null, 0));
 		assertEquals(1.0, trav.getLocQualityIssueSeverity(null, 0), 0);
 		assertEquals("uri", trav.getLocQualityIssueProfileRef(null, 0));
 	}
 	
 	@Test
 	public void testLQIssueLocalWithSpan () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc xmlns:i='"+ITSEngine.ITS_NS_URI+"' i:version='2.0'>"
 			+ "<p>Text with <i:span locQualityIssueType='other' locQualityIssueComment='comment'"
 			+ " locQualityIssueProfileRef='uri' locQualityIssueSeverity='1'>error</i:span></p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		getElement(trav, "i:span", 1);
 		assertEquals("other", trav.getLocQualityIssueType(null, 0));
 		assertEquals("comment", trav.getLocQualityIssueComment(null, 0));
 		assertEquals(1.0, trav.getLocQualityIssueSeverity(null, 0), 0);
 		assertEquals("uri", trav.getLocQualityIssueProfileRef(null, 0));
 	}
 	
 	@Test
 	public void testExternalResourceRefGlobal () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:externalResourceRefRule selector='//video/@src' externalResourceRefPointer='.' />"
 			+ "<i:externalResourceRefRule selector='//video/@poster' externalResourceRefPointer='.' />"
 			+ "</i:rules>"
 			+ "<p>Text with <video src=\"http://www.example.com/v2.mp\" poster=\"video-image.png\" /></p></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "video", 1);
 		assertEquals("http://www.example.com/v2.mp", trav.getExternalResourceRef(elem.getAttributeNode("src")));
 		assertEquals("video-image.png", trav.getExternalResourceRef(elem.getAttributeNode("poster")));
 	}
 
 	@Test
 	public void testTranslateGlobal () throws SAXException, IOException, ParserConfigurationException {
 		InputSource is = new InputSource(new StringReader("<doc>"
 			+ "<i:rules xmlns:i='"+ITSEngine.ITS_NS_URI+"' version='2.0'>"
 			+ "<i:translateRule selector='//par/@title' translate='yes' />"
 			+ "<i:translateRule selector='//par/@alt' translate='yes' />"
 			+ "</i:rules>"
 			+ "<par title='title text' test='test' alt='alt text'>Text</par></doc>"));
 		Document doc = fact.newDocumentBuilder().parse(is);
 		ITraversal trav = applyITSRules(doc, null, false, null);
 		Element elem = getElement(trav, "par", 1);
 		assertTrue(trav.getTranslate(elem.getAttributeNode("title")));
 		assertTrue(trav.getTranslate(elem.getAttributeNode("alt")));
 		assertFalse(trav.getTranslate(elem.getAttributeNode("test")));
 	}
 
 	private static Element getElement (ITraversal trav,
 		String name,
 		int number)
 	{
 		trav.startTraversal();
 		Node node;
 		int count = 0;
 		while ( (node = trav.nextNode()) != null ) {
 			switch ( node.getNodeType() ) {
 			case Node.ELEMENT_NODE:
 				if ( !trav.backTracking() ) {
 					if ( node.getNodeName().equals(name) ) {
 						if ( ++count == number ) return (Element)node;
 					}
 				}
 				break;
 			}
 		}
 		return null;
 	}
 	
 	private static ITraversal applyITSRules (Document doc,
 		URI docURI,
 		boolean isHTML5,
 		File rulesFile)
 	{
 		// Create the ITS engine
 		ITSEngine itsEng = new ITSEngine(doc, docURI, isHTML5, null);
 		// Add any external rules file(s)
 		if ( rulesFile != null ) {
 			itsEng.addExternalRules(rulesFile.toURI());
 		}
 		// Apply the all rules (external and internal) to the document
 		itsEng.applyRules(ITSEngine.DC_ALL);
 		return itsEng;
 	}
 
 }
