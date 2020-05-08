 /*******************************************************************************
  * Copyright (c) 2006-2013
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package de.devboost.commenttemplate.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import de.devboost.commenttemplate.compiler.CommentTemplateCompiler;
 
 public class CommentTemplateCompilerTest {
 
 	@Test
 	public void testLineSplitting() {
 		assertLineSplitting("\n ", new String[] {"", " "});
 		assertLineSplitting("\n\n", new String[] {"", "", ""});
 		assertLineSplitting("\n", new String[] {"", ""});
 
 		assertLineSplitting(" \n", new String[] {" ", ""});
 	}
 	
 	private void assertLineSplitting(String comment, String[] expected) {
		List<String> lines = new CommentTemplateCompiler().split(comment);
 		assertEquals(Arrays.asList(expected), lines);
 	}
 	
 	public void testCommentSplitting() {
 		assertCommentSplitting("/*abc*/", new String[] {"/*abc*/"});
 		assertCommentSplitting("\n/*abc*/", new String[] {"\n/*abc*/"});
 		assertCommentSplitting("\n\t/*abc*/", new String[] {"\n\t/*abc*/"});
 		assertCommentSplitting("/*abc*//*def*/", new String[] {"/*abc*/","/*def*/"});
 	}
 
 	private void assertCommentSplitting(String text, String[] expected) {
 		List<String> comments = new CommentTemplateCompiler().splitTextToComments(text);
 		assertEquals(Arrays.asList(expected), comments);
 	}
 	
 	public void testRegex() {
 		assertEquals("\r\n\r\n", "\r\n\r\n\r\n".replaceAll("\\r\\n\\z", ""));
 		assertEquals("\n\n", "\n\n\n".replaceAll("\\n\\z", ""));
 	}
 }
