 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.parser.test.descriptionunit;
 
 import junit.framework.Assert;
 
 import org.eclipse.mylyn.docs.intent.parser.descriptionunit.DescriptionUnitParser;
 import org.junit.Test;
 
 /**
  * Ensure the good behavior of the DescriptionUnit Parser.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class DescriptionUnitParserTest {
 
 	/**
 	 * Tests the excractFirstString method with different reprensentative examples.
 	 */
 	@Test
	public void testExcratFirstString() {
 		DescriptionUnitParser parser = new DescriptionUnitParser();
 		// 'FirstString'
 		Assert.assertEquals("firstString", parser.extractFirstString("'firstString' and 'secondString'"));
 		// "FirstS'tring"
 		Assert.assertEquals("firstS'tring", parser.extractFirstString("\"firstS'tring\" and 'secondString'"));
 		// 'Firs\'String'
 		Assert.assertEquals("firs'tString", parser.extractFirstString("'firs\\'tString'"));
 		// "first\"String" => first"String
 		Assert.assertEquals("firs\"tString", parser.extractFirstString("\"firs\\\"tString\""));
 		// 'firs\\'tString' => firs\\
 		Assert.assertEquals("firs\\", parser.extractFirstString("'firs\\\\'tString'"));
 		// "'first\\\"String'" => 'first\"String'
 		Assert.assertEquals("'first\\\"String'", parser.extractFirstString("\"'first\\\\\\\"String'\""));
 		// => 'first\'String'
 		Assert.assertEquals("'first\\'String'", parser.extractFirstString("\"'first\\\\\\'String'\""));
 
 		Assert.assertEquals(null, parser.extractFirstString(" ' something"));
 
 	}
 }
