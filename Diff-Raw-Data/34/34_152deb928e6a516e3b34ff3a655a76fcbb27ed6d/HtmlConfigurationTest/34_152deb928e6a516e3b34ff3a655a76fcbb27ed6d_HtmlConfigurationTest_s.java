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
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class HtmlConfigurationTest {
 	
 	@Before
 	public void setUp() throws Exception {
 		
 	}
 	
 	@Test
 	public void defaultConfiguration() {		
 		URL url = HtmlConfigurationTest.class.getResource("/net/sf/okapi/filters/html/tests/testConfiguration1.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);	
 		assertEquals(rules.getMainRuleType("title"), TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTE_TRANS);
 		assertEquals(rules.getMainRuleType("abbr"), TaggedFilterConfiguration.RULE_TYPE.INLINE_ELEMENT);
 		assertEquals(rules.getMainRuleType("area"), TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTES_ONLY);
		assertEquals(rules.getMainRuleType("script"), TaggedFilterConfiguration.RULE_TYPE.SCRIPT_ELEMENT);
 		assertEquals(rules.getMainRuleType("pre"), TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE);
 		assertEquals(rules.getMainRuleType("meta"), TaggedFilterConfiguration.RULE_TYPE.ATTRIBUTES_ONLY);
 		
 		Map<String, String> attributes = new HashMap<String, String>();
 		attributes.put("http-equiv", "keywords");		
 		assertTrue(rules.isTranslatableAttribute("meta", "content", attributes));
 		assertFalse(rules.isTranslatableAttribute("dummy", "content", attributes));
 		
 		attributes.clear();
 		attributes.put("http-equiv", "content-language");
 		assertTrue(rules.isWritableLocalizableAttribute("meta", "content", attributes));
 
 		attributes.clear();
 		attributes.put("http-equiv", "content-type");
 		assertFalse(rules.isReadOnlyLocalizableAttribute("meta", "content", attributes));
 		
 		attributes.clear();
 		attributes.put("name", "generator");
 		assertTrue(rules.isReadOnlyLocalizableAttribute("meta", "content", attributes));
 	}
 	
 	@Test
 	public void preserveWhileSpace() {
 		URL url = HtmlConfigurationTest.class.getResource("/net/sf/okapi/filters/html/tests/testConfiguration1.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		
 		assertEquals(rules.getMainRuleType("pre"), TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE);
 		assertEquals(rules.getMainRuleType("style"), TaggedFilterConfiguration.RULE_TYPE.EXCLUDED_ELEMENT);
 		assertTrue(rules.isRuleType("style", TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));
 		assertTrue(rules.isRuleType("pre", TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));
 		assertFalse(rules.isRuleType("stylesheet", TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));
 		assertFalse(rules.isRuleType("p", TaggedFilterConfiguration.RULE_TYPE.PRESERVE_WHITESPACE));		
 	}
 	
 	@Test
 	public void langAndXmlLang() {
 		URL url = HtmlConfigurationTest.class.getResource("/net/sf/okapi/filters/html/tests/testConfiguration1.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		
 		Map<String, String> attributes = new HashMap<String, String>();
 		attributes.clear();
 		attributes.put("lang", "en");
 		assertTrue(rules.isWritableLocalizableAttribute("x", "lang", attributes));
 		assertFalse(rules.isReadOnlyLocalizableAttribute("x", "lang", attributes));
 		assertFalse(rules.isTranslatableAttribute("x", "lang", attributes));
 		
 		assertTrue(rules.isWritableLocalizableAttribute("p", "lang", attributes));
 		assertFalse(rules.isReadOnlyLocalizableAttribute("p", "lang", attributes));
 		assertFalse(rules.isTranslatableAttribute("p", "lang", attributes));
 		
 		attributes.clear();
 		attributes.put("xml:lang", "en");
 		assertTrue(rules.isWritableLocalizableAttribute("x", "xml:lang", attributes));
 	}
 	
 	@Test
 	public void genericCodeTypes() {
 		URL url = HtmlConfigurationTest.class.getResource("/net/sf/okapi/filters/html/tests/testConfiguration1.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		
 		assertEquals(rules.getElementType("b"), "bold");
 		assertEquals(rules.getElementType("i"), "italic");
 		assertEquals(rules.getElementType("u"), "underlined");
 		assertEquals(rules.getElementType("img"), "image");
 		assertEquals(rules.getElementType("a"), "link");
 		assertEquals(rules.getElementType("p"), "p");
 		assertEquals(rules.getElementType("x"), "x");
 	}
 	
 	@Test
 	public void collapseWhitespace() {
 		URL url = HtmlConfigurationTest.class.getResource("/net/sf/okapi/filters/html/tests/testConfiguration1.yml");
 		TaggedFilterConfiguration rules = new TaggedFilterConfiguration(url);
 		assertTrue(rules.collapseWhitespace());
 		
 		url = HtmlConfigurationTest.class.getResource("/net/sf/okapi/filters/html/tests/maximalistConfiguration.yml");
 		rules = new TaggedFilterConfiguration(url);
 		assertTrue(rules.collapseWhitespace());
 	}
 }
