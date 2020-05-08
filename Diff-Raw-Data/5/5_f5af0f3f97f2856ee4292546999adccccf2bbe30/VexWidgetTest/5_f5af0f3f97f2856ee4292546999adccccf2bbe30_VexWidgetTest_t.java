 /*******************************************************************************
  * Copyright (c) 2010, Florian Thienel and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Florian Thienel - bug 315914, initial implementation
  *******************************************************************************/
 package org.eclipse.wst.xml.vex.core.internal.widget;
 
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 
 import junit.framework.TestCase;
 
 import org.eclipse.wst.xml.vex.core.internal.css.Rule;
 import org.eclipse.wst.xml.vex.core.internal.css.StyleSheet;
 import org.eclipse.wst.xml.vex.core.internal.dom.DTDValidatorTest;
 import org.eclipse.wst.xml.vex.core.internal.dom.Document;
 import org.eclipse.wst.xml.vex.core.internal.dom.Element;
 import org.eclipse.wst.xml.vex.core.internal.dom.RootElement;
 import org.eclipse.wst.xml.vex.core.internal.provisional.dom.I.VEXDocument;
 import org.eclipse.wst.xml.vex.core.internal.provisional.dom.I.Validator;
 import org.eclipse.wst.xml.vex.core.internal.validator.WTPVEXValidator;
 
 public class VexWidgetTest extends TestCase {
 
 	private VexWidgetImpl widget;
 
 	@Override
 	protected void setUp() throws Exception {
 		widget = new VexWidgetImpl(new MockHostComponent());
 	}
 	
 	private VEXDocument createDocument(final String rootElementName) {
 		final URL url = DTDValidatorTest.class.getResource("test1.dtd");
 		final Validator validator = WTPVEXValidator.create(url);
 		final VEXDocument document = new Document(new RootElement(rootElementName));
 		document.setValidator(validator);
 		return document;
 	}
 
 	public void testProvideOnlyAllowedElements() throws Exception {
 		widget.setDocument(createDocument("section"), new StyleSheet(Collections.<Rule> emptyList()));
 		assertCanInsertOnly("title", "para");
 		widget.insertElement(new Element("title"));
 		assertCanInsertOnly();
 		widget.moveBy(1);
 		assertCanInsertOnly("para");
 		widget.insertElement(new Element("para"));
 		widget.moveBy(1);
 		assertCanInsertOnly("para");
 	}
 	
 	private void assertCanInsertOnly(final String... elementNames) {
 		assertTrue(Arrays.equals(sortedCopyOf(elementNames), sortedCopyOf(widget.getValidInsertElements())));
 	}
 	
 	private static String[] sortedCopyOf(String[] strings) {
		final String[] result = new String[strings.length];
		System.arraycopy(strings, 0, result, 0, strings.length);
 		Arrays.sort(result);
 		return result;
 	}
 
	
 }
