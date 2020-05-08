 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.ui.tests.text;
 
 import org.eclipse.core.runtime.Assert;
import org.eclipse.dltk.core.tests.model.SuiteOfTestCases;
 import org.eclipse.dltk.ruby.internal.ui.RubyPreferenceConstants;
 import org.eclipse.dltk.ruby.ui.tests.internal.TestUtils;
 import org.eclipse.dltk.ui.text.blocks.Balance;
 import org.eclipse.dltk.ui.text.blocks.BlocksConfiguration;
 import org.eclipse.dltk.ui.text.util.AutoEditUtils;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceStore;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.TextUtilities;
 
public class BalanceTest extends SuiteOfTestCases {
 	
 	public BalanceTest(String name) {
 		super(name);
 	}
 
 	IPreferenceStore fStore;
 	
 	protected void setUp() throws Exception {
     	fStore = new PreferenceStore();
     	RubyPreferenceConstants.initializeDefaultValues(fStore);
         super.setUp();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     
     public Balance doTest(String data, BlocksConfiguration blocks, String expected) throws Exception {
 	    Document doc = new Document(data);
 	    TestUtils.installStuff(doc);
 	    Balance b = Balance.calculateBalance(doc, 0, doc.getLength(), blocks, TestUtils.ALL_RANGES_ALLOWED, null);
 	    assertEquals(expected, b.toDebugString(doc));
 	    return b;
     }
     
     
     public void doBackwardMultilineTest(String data, BlocksConfiguration blocks) throws Exception {
     	Document doc = new Document(data);
     	TestUtils.installStuff(doc);
     	Balance prev = null;
     	for (int lineIndex = doc.getNumberOfLines() - 1; lineIndex >= 0; lineIndex--) {
 			String sumExpected = null, lineExpected = null;
 			
 			int lineStart = doc.getLineOffset(lineIndex);
 			int lineLength = doc.getLineLength(lineIndex);
 			String line = doc.get(lineStart, lineLength);
 			int eol = TextUtilities.indexOf(doc.getLegalLineDelimiters(), line, 0) [0];
 			int pos = line.indexOf("###");
 			if (pos >= 0) {
     			sumExpected = line.substring(pos+3).trim();
     			doc.replace(lineStart + pos, eol - pos, AutoEditUtils.getNSpaces(eol - pos));
     			
     			Assert.isTrue(lineLength == doc.getLineLength(lineIndex));
     			line = doc.get(lineStart, lineLength);
     		}
 			
 			eol = TextUtilities.indexOf(doc.getLegalLineDelimiters(), line, 0) [0];
 			pos = line.indexOf("##");
 			if (pos >= 0) {
 				lineExpected = line.substring(pos+2).trim();
 				doc.replace(lineStart + pos, eol - pos, AutoEditUtils.getNSpaces(eol - pos));
     			Assert.isTrue(lineLength == doc.getLineLength(lineIndex));
 			}
 			
 			Balance b = Balance.calculateBalance(doc, lineStart, lineLength, blocks, TestUtils.ALL_RANGES_ALLOWED, null);
 			if (lineExpected != null)
 				assertEquals("Line balance mismatch processing line#" + lineIndex + ": " + line, lineExpected, b.toDebugString(doc));
 			if (prev != null)
 				b.addAll(prev, null);
 			if (sumExpected != null)
 				assertEquals("Combined balance mismatch processing line#" + lineIndex + ": " + line, sumExpected, b.toDebugString(doc));
 			prev = b;
     	}
     }
     
     public void testEmpty() throws Exception {
     	doTest("", TestUtils.ALL_RUBY_BLOCKS, "");
     }
     
     public void testOpenIf() throws Exception {
     	doTest("if foo", TestUtils.ALL_RUBY_BLOCKS, "if");
     }
     
     public void testNonStatementIf() throws Exception {
     	doTest("foo if bar", TestUtils.ALL_RUBY_BLOCKS, "");
     }
     
     public void testSingleLineIf() throws Exception {
     	doTest("if foo then bar end", TestUtils.ALL_RUBY_BLOCKS, "");
     }
     
     public void testNestedIfInExpression() throws Exception {
     	doTest("if 10 + if foo then 20 else 30 end", TestUtils.ALL_RUBY_BLOCKS, "if");
     }
     
     public void testSequentialIfs() throws Exception {
     	doTest("if foo\nif bar\nif boz", TestUtils.ALL_RUBY_BLOCKS, "if if if");
     }
     
     public void testSemicolonSeparated() throws Exception {
     	doTest("if foo; if bar; while boz", TestUtils.ALL_RUBY_BLOCKS, "if if while");
     }
     
     public void testDirectlyNested() throws Exception {
     	doTest("if if bar then true else false end; puts 'x'", TestUtils.ALL_RUBY_BLOCKS, "if");
     }
     
     public void testDifferentStructures() throws Exception {
     	doTest("module Boz\nif foo\nclass bar\ndef zzz\nend\nend", TestUtils.ALL_RUBY_BLOCKS, "module if");
     }
     
     public void testCombining1() throws Exception {
     	doBackwardMultilineTest(TestUtils.getData("resources/balance1.rb"), TestUtils.ALL_RUBY_BLOCKS);
     }
     
     public void testCombining2() throws Exception {
     	doBackwardMultilineTest(TestUtils.getData("resources/balance2.rb"), TestUtils.ALL_RUBY_BLOCKS);
     }
     
 }
