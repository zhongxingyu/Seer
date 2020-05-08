 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.parser.html.impl;
 
 import java.io.File;
 import java.net.URI;
 import java.util.Iterator;
 
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.io.temp.impl.TempFileManager;
 import org.paxle.core.norm.impl.ReferenceNormalizer;
 import org.paxle.parser.ParserContext;
 
 import junit.framework.TestCase;
 
 public class HtmlParserTest extends TestCase {
 	
 	private static final String[] TEST_CASES = {
 		"svgopen.org_index.html",
 		"javascript_test.html",
 		"baseHrefTest.html",
 		"draft-ietf-webdav-rfc2518bis-12-from-11.diff.html",
		"imdb_biographies_o.html", // XXX: produced an stack-overflow error with htmlparser 2006
 		"javascript_tcom.html",
 		"pc-welt_archiv02_knowhow.html",
 		"pc-welt_archiv07_knowhow.html"
		// "imdb_biographies_s.html" // XXX: you need to set Xmx to 128m to run this
 	};
 	
 	// FIXME: <a name="#bla" /> is only recognized as a starting not an ending a-tag
 	
 	public static void testHtmlBaseHref() throws Exception {
 		final File testResource = new File("src/test/resources/", "baseHrefTest.html");
 		final HtmlParser parser = new HtmlParser();
 		ParserContext.setCurrentContext(new ParserContext(null, null, null, new TempFileManager(), new ReferenceNormalizer()));
 		final IParserDocument pdoc = parser.parse(URI.create("http://www.example.org/baseHrefTest.html"), null, testResource);
 		assertNotNull(pdoc);
 		final Iterator<URI> it = pdoc.getLinks().keySet().iterator();
 		assertTrue(it.hasNext());
 		assertEquals(URI.create("http://www.example.net/test/blubb"), it.next());
 	}
 	
 	/** does not work as expected yet */
 	public static void testHtmlParser() throws Exception {
 		final File testResources = new File("src/test/resources/");
 		final HtmlParser parser = new HtmlParser();
 		ParserContext.setCurrentContext(new ParserContext(null, null, null, new TempFileManager(), new ReferenceNormalizer()));
 		for (final String testCase : TEST_CASES) {
 			// System.out.println(testCase);
 			final IParserDocument pdoc = parser.parse(new URI("http://www.example.org/" + testCase), null, new File(testResources, testCase));
 			assertNotNull(pdoc);
 			
 			/*
 			System.out.println(pdoc.getLinks().size());
 			System.out.println(pdoc.getTextFile().length());
 			System.out.println();*/
 		}
 		parser.close();
 	}
 	
 	public static void testHtmlParserThreaded() throws Exception {
 		final Thread[] threads = new Thread[TEST_CASES.length];
 		final File testResources = new File("src/test/resources/");
 		final HtmlParser parser = new HtmlParser();
 		final TempFileManager tfm = new TempFileManager();
 		final ReferenceNormalizer refNorm = new ReferenceNormalizer();
 		
 		for (int i=0; i<TEST_CASES.length; i++) {
 			final String testCase = TEST_CASES[i];
 			threads[i] = new Thread() {
 				@Override
 				public void run() {
 					super.setName("test-" + testCase);
 					try {
 						// System.out.println("started");
 						ParserContext.setCurrentContext(new ParserContext(null, null, null, tfm, refNorm));
 						final IParserDocument pdoc = parser.parse(new URI("http://www.example.org/" + testCase), null, new File(testResources, testCase));
 						assertNotNull(pdoc);
 						/*
 						System.out.println(testCase);
 						System.out.println(pdoc.getLinks().size());
 						System.out.println(pdoc.getTextFile().length());
 						System.out.println();*/
 					} catch (Exception e) { e.printStackTrace(); }
 				}
 			};
 			threads[i].start();
 		}
 		
 		for (int i=0; i<threads.length; i++)
 			threads[i].join();
 		parser.close();
 	}
 	
 	private static final String[][] REPL_CASES = {
 		{ "Il y a une &eacute;cole", "Il y a une \u00E9cole" },
 		{ "Da &amp; dort passierte &quot;etwas&quot;.", "Da & dort passierte \"etwas\"." }
 	};
 	
 	public static void testHtmlReplace() throws Exception {
 		for (int i=0; i<REPL_CASES.length; i++) {
 			final String repl = HtmlTools.deReplaceHTML(REPL_CASES[i][0]);
 			final String exp = REPL_CASES[i][1];
 			assertNotNull(repl);
 			assertEquals(exp, repl);
 		}
 	}
 }
