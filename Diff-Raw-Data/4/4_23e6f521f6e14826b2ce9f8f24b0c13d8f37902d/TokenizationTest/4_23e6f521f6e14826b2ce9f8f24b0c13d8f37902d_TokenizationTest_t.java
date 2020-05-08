 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 // This test case uses parts of the code presented by Sujit Pal at http://sujitpal.blogspot.com/2008/05/tokenizing-text-with-icu4js.html
 
 package net.sf.okapi.steps.tokenization;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.ListUtil;
 import net.sf.okapi.common.Range;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.steps.tokenization.common.ILexer;
 import net.sf.okapi.steps.tokenization.common.LanguageAndTokenParameters;
 import net.sf.okapi.steps.tokenization.common.Token;
 import net.sf.okapi.steps.tokenization.common.TokensAnnotation;
 import net.sf.okapi.steps.tokenization.engine.RbbiLexer;
 import net.sf.okapi.steps.tokenization.engine.javacc.ParseException;
 import net.sf.okapi.steps.tokenization.engine.javacc.SimpleCharStream;
 import net.sf.okapi.steps.tokenization.engine.javacc.WordTokenizer;
 import net.sf.okapi.steps.tokenization.engine.javacc.WordTokenizerTokenManager;
 import net.sf.okapi.steps.tokenization.locale.LocaleUtil;
 import net.sf.okapi.steps.tokenization.tokens.Tokens;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.ibm.icu.text.BreakIterator;
 import com.ibm.icu.text.RuleBasedBreakIterator;
 
 public class TokenizationTest {
 
 	private String text = "Jaguar will sell its new XJ-6 model in the U.S. for " +
     "a small fortune :-). Expect to pay around USD 120ks ($120,000.00 on 05/30/2007 at 12.30PM). Custom options " +
     "can set you back another few 10,000 dollars. For details, go to " +
     "<a href=\"http://www.jaguar.com/sales\" alt=\"Click here\">" +
     "Jaguar Sales</a> or contact xj-6@jaguar.com."+
     " See http://www.jaguar.com/sales, www.jaguar.com, AT&T, P&G, Johnson&Johnson, 192.168.0.5 for info 3.5pct.";
 	
 	private LocaleId locENUS = LocaleId.fromString("en-us");
 
 	//private String text = "The quick (\"brown\") fox can't jump 32.3 feet, right?";
 //	private String text = "$120,000.00 on 05/30/2007 at 12.30PM is much better than $10.00 on 05/30/2007 at 22:30:15";
 		
 	//private String text = "http://www.jaguar.com/sales";
 	//private String text = "<a href=\"http://www.jaguar.com/sales\" alt=\"Click here\">";
 	
 	private TokenizationStep ts;
 	private Tokens tokens;
 
 	private String streamAsString(InputStream input) throws IOException {
 		BufferedReader reader = null;
 		reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
 
 		StringBuilder tmp = new StringBuilder();
 		char[] buf = new char[2048];
 		int count = 0;
 		while (( count = reader.read(buf)) != -1 ) {
 			tmp.append(buf, 0, count);
 		}
 		
         return tmp.toString();
     }
 	
 	private Tokens tokenizeText() {
 		
 		Tokens res = new Tokens();
 		ts.handleEvent(new Event(EventType.START_BATCH)); // Calls component_init();
 		
 		StartDocument startDoc = new StartDocument("tokenization");
 		startDoc.setLanguage(locENUS);
 		startDoc.setMultilingual(false);		
 		Event event = new Event(EventType.START_DOCUMENT, startDoc);		
 		ts.handleEvent(event);
 				
 		TextUnit tu = TextUnitUtil.buildTU(text);
 		event = new Event(EventType.TEXT_UNIT, tu);		
 		ts.handleEvent(event);
 		
 		// Move tokens from the event's annotation to result
 		TokensAnnotation ta = TextUnitUtil.getSourceAnnotation(tu, TokensAnnotation.class);
 		if (ta != null)
 			res.addAll(ta.getTokens());
 		
 		ts.handleEvent(new Event(EventType.END_BATCH)); // Calls component_done();
 		return res;
 	}
 	
 	@Before
 	public void setUp() {
 
 		ts = new TokenizationStep();		
 	}
 
 	@Test
 	public void testDefRules() {
 		
 		@SuppressWarnings("unused")
 		RuleBasedBreakIterator iterator = (RuleBasedBreakIterator) BreakIterator.getWordInstance();
 		// System.out.println(iterator.toString().replace(";", ";\n"));
 	}
 	
 	@Test
 	public void testLocaleUtil() {
 	
 		assertEquals("en-us", LocaleUtil.normalizeLanguageCode_Okapi("en_US"));
 		assertEquals("en_US", LocaleUtil.normalizeLanguageCode_ICU("EN-US"));
 	}
 	
 	@Test
 	public void testTS() {
 		
 		ts = new TokenizationStep();
 		
 		TextUnit tu = TextUnitUtil.buildTU(text);
 		Event event = new Event(EventType.TEXT_UNIT, tu);
 		
 		ts.handleEvent(new Event(EventType.START_BATCH));
 		ts.handleEvent(event);
 		ts.handleEvent(new Event(EventType.END_BATCH));
 	}
 	
 	private void listTokens(Tokens tokens) {
 		
 		if (tokens == null) return;
 		for (Token token : tokens) {	
 			
 			System.out.println(token.toString());
 		}
 	}
 	
 	@Test
 	public void listTokenizerOutput() {
 		/* 
 	    String text2 = "Test word count is correct.";
 	    String text3 = "The quick (\"brown\") fox can't jump 32.3 feet, right?";
 	    String text4 = "The quick (brown) fox cant jump 32.3 feet, right?";
 		*/
 		
 		Tokens tokens = Tokenizer.tokenize(text, locENUS); // All tokens
 		//assertEquals(127, tokens.size());
 						
 		//listTokens(tokens);
 		System.out.println(tokens.size());
 	}
 	
 	@Test
 	public void testFilters() {
 
 		// Defaults, filters not empty		
 		Parameters params = new Parameters();
 		ts.setParameters(params);
 		
 		List<String> languageFilter = ts.getLanguageFilter();
 		assertNotNull(languageFilter);
 		assertTrue(languageFilter.size() > 0);
 		
 		List<Integer> tokenFilter = ts.getTokenFilter();
 		assertNotNull(tokenFilter);
 		assertTrue(tokenFilter.size() > 0);
 		
 		// LANGUAGES_ALL, language filter not empty
 		params = new Parameters();
 		params.setLanguageMode(LanguageAndTokenParameters.LANGUAGES_ALL);
 		ts.setParameters(params);
 		
 		languageFilter = ts.getLanguageFilter();
 		assertNotNull(languageFilter);
 		assertTrue(languageFilter.size() > 0);
 		
 		// LANGUAGES_ONLY_WHITE_LIST, test size & elements
 		params = new Parameters();
 		params.setLanguageMode(LanguageAndTokenParameters.LANGUAGES_ONLY_WHITE_LIST);
 		params.setLanguageWhiteList(ListUtil.arrayAsList((new String[] {"en-us", "EN-CA", "en-IE", "EN-sg", "eN-Jm", 
 				"en_pk", "EN_NA", "en_GB", "EN_nz", "eN-mH"})));
 		ts.setParameters(params);
 		
 		languageFilter = ts.getLanguageFilter();
 		assertNotNull(languageFilter);
 //		assertTrue(languageFilter.size() == 10);
 //		
 //		assertEquals("EN-CA", languageFilter.get(0));
 //		assertEquals("EN-IE", languageFilter.get(2));
 //		assertEquals("EN-US", languageFilter.get(9));
 //		
 //		assertNotSame("en-us", languageFilter.get(0));
 //		assertNotSame("en-sg", languageFilter.get(3));
 		
 		assertTrue(languageFilter.size() == 1);
 		assertEquals("en-us", languageFilter.get(0));
 		
 		// LANGUAGES_ALL_EXCEPT_BLACK_LIST, white list still not empty, test size & elements
 		params.setLanguageMode(LanguageAndTokenParameters.LANGUAGES_ALL_EXCEPT_BLACK_LIST);
 		params.setLanguageBlackList(ListUtil.arrayAsList((new String[] {"en-us", "EN-CA", "en-IE"})));
 		ts.setParameters(params);
 		
 		languageFilter = ts.getLanguageFilter();
 		assertNotNull(languageFilter);
 		assertTrue(languageFilter.size() > 0);
 		
 		assertFalse(languageFilter.contains("EN-CA"));
 		assertFalse(languageFilter.contains("EN-IE"));
 		assertFalse(languageFilter.contains("EN-US"));
 		
 		//assertTrue(languageFilter.contains("EN"));
 		//assertTrue(languageFilter.contains("FR-CA"));
 		
 		// TOKENS_ALL, token filter not empty
 		params.setTokenMode(LanguageAndTokenParameters.TOKENS_ALL);
 		tokenFilter = ts.getTokenFilter();
 		assertNotNull(tokenFilter);
 		assertTrue(tokenFilter.size() > 0);
 		
 		// TOKENS_SELECTED, resets to TOKENS_ALL at start batch if token list is not set 
 		params.setTokenMode(LanguageAndTokenParameters.TOKENS_SELECTED);
 		tokenFilter = ts.getTokenFilter();
 		assertNotNull(tokenFilter);
 		assertTrue(tokenFilter.size() > 0);		
 		
 		ts.handleEvent(new Event(EventType.START_BATCH));
 		assertEquals(LanguageAndTokenParameters.TOKENS_ALL, params.getTokenMode());
 		ts.handleEvent(new Event(EventType.END_BATCH));
 		
 		params.setTokenMode(LanguageAndTokenParameters.TOKENS_SELECTED);		
 		params.setTokenNames(ListUtil.arrayAsList((new String[] {"WORD", "NUMBER", "PUNCTUATION"})));
 		ts.handleEvent(new Event(EventType.START_BATCH));
 		assertEquals(LanguageAndTokenParameters.TOKENS_SELECTED, params.getTokenMode());
 		ts.handleEvent(new Event(EventType.END_BATCH));
 		tokenFilter = ts.getTokenFilter();
 		assertNotNull(tokenFilter);
 		assertEquals(3, tokenFilter.size());
 	}
 	
 	@Test
 	public void testTokenizer1() {
 		
 		ts.setConfiguration(this.getClass(), "test_config1.tprm");
 		//Parameters params = (Parameters) ts.getParameters();
 		
 		assertNotNull(ts.getTokenFilter().size());
 		assertTrue(ts.getTokenFilter().size() > 0);
 		
 		List<ILexer> lexers = ts.getLexers();
 		assertEquals(1, lexers.size());
 		
 		tokens = tokenizeText();
 		assertEquals(183, tokens.size());
 		
 		// listTokens(tokens);
 	}
 	
 	@Test
 	public void testJavaCC() {
 		
 		StringReader sr = new StringReader("This is a 1248-th test. U.S.A.F. read-through\n didn't AT&T, P&G, Johnson&Johnson \n\nadmin@yahoo.com 192.168.0.7");
 		SimpleCharStream stream = new SimpleCharStream(sr);
 		
 		WordTokenizer tokenizer = new WordTokenizer(new WordTokenizerTokenManager(stream));
 		
 		net.sf.okapi.steps.tokenization.engine.javacc.Token token = null;
 		
 		do {
 			try {
 			token = tokenizer.nextToken();
 			} catch (ParseException e) {
 
 				e.printStackTrace();
 				break;
 				
 			} catch (IOException e) {
 
 				e.printStackTrace();
 				break;
 			}
 			
 			if (token == null) break;
 			//System.out.println(((Manager)tokenizer.token_source).input_stream.getBeginColumn());
 			//System.out.println(jj_input_stream.getBeginColumn());	
 						
 			System.out.println(String.format("%d  %15s (%d, %d - %d, %d)\t%d - %d", token.kind, token.image, 
 					stream.getBeginColumn(), 
 					stream.getBeginLine(), 
 					stream.getEndColumn(), 
 					stream.getEndLine(),
 					stream.bufpos + 1 - token.image.length(),
 					stream.bufpos + 1));
 			
 			
 		} while (token != null);
 	}
 	
 	
 	@Test
 	public void testRetainRemove() {
 		
 		List<String> list = new ArrayList<String> ();
 		list.add("A");
 		list.add("B");
 		list.add("C");
 		
 		List<String> whiteList = new ArrayList<String> ();
 		whiteList.add("A");
 		whiteList.add("B");
 						
 		List<String> blackList = new ArrayList<String> ();
 		blackList.add("B");
 
 		assertEquals(3, list.size());
 		assertEquals("A", list.get(0));
 		assertEquals("B", list.get(1));
 		assertEquals("C", list.get(2));
 		
 		list.retainAll(whiteList);
 		assertEquals(2, list.size());
 		assertEquals("A", list.get(0));
 		assertEquals("B", list.get(1));
 		
 		list.removeAll(blackList);
 		assertEquals(1, list.size());
 		assertEquals("A", list.get(0));
 	}
 	
 	@Test
 	public void testFormRbbiRules() {
 		
 		String rules = null;
 		String expected = null;
 		
 		try {
			expected = Util.normalizeNewlines(streamAsString(this.getClass().getResourceAsStream("rbbi_custom.txt")));
			rules = streamAsString(this.getClass().getResourceAsStream("rbbi_default.txt"));
 			
 			//rules = RbbiLexer.formatCaption(rules, "Custom rules");
 			
 			rules = RbbiLexer.formatRule(rules, 
 					"Abbreviation", 
 					"Abbreviation: Uppercase alpha chars separated by period and optionally followed by a period", 
 					"[A-Z0-9](\\.[A-Z0-9])+(\\.)*",
 					500);
 			rules = RbbiLexer.formatRule(rules, 
 					"HyphenatedWord", 
 					"Hyphenated Word : sequence of letter or digit, (punctuated by - or _, with following letter or digit sequence)+", 
 					"[A-Za-z0-9]+([\\-_][A-Za-z0-9]+)+", 
 					501);
 			rules = RbbiLexer.formatRule(rules, 
 					"EmailAddress", 
 					"Email address: sequence of letters, digits and punctuation followed by @ and followed by another sequence", 
 					"[A-Za-z0-9_\\-\\.]+\\@[A-Za-z][A-Za-z0-9_]+\\.[a-z]+",
 					502);
 			rules = RbbiLexer.formatRule(rules, 
 					"InternetAddress", 
 					"Internet Addresses: http://www.foo.com(/bar)", 
 					"[a-z]+\\:\\/\\/[a-z0-9]+(\\.[a-z0-9]+)+(\\/[a-z0-9][a-z0-9\\.]+)", 
 					503);
 			rules = RbbiLexer.formatRule(rules, 
 					"XmlMarkup", 
 					"XML markup: A run begins with < and ends with the first matching >", 
 					"\\<[^\\>]+\\>", 
 					504);
 			rules = RbbiLexer.formatRule(rules, 
 					"Emoticon", 
 					"Emoticon: A run that starts with :;B8{[ and contains only one or more of the following -=/{})(", 
 					"[B8\\:\\;\\{\\[][-=\\/\\{\\}\\)\\(]+",
 					505);
 			
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
 		
 		assertEquals(expected, rules);
 	}
 	
 	@Test
 	public void testRange() {
 	
 		Range r1 = new Range(1, 5);
 		Range r2 = new Range(1, 5);
 		
 		assertFalse(r1 == r2);
 		assertFalse(r1.equals(r2));
 		assertFalse(r1.hashCode() == r2.hashCode());
 		assertFalse(r1.toString() == r2.toString());
 	}
 }
