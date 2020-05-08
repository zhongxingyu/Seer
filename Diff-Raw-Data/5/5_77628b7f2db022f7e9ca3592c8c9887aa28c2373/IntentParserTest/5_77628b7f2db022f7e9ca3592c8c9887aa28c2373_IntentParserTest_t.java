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
 package org.eclipse.mylyn.docs.intent.parser.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import junit.framework.Assert;
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionBloc;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
 import org.eclipse.mylyn.docs.intent.markup.markup.Paragraph;
 import org.eclipse.mylyn.docs.intent.parser.IntentParser;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ParseException;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.test.utils.FileToStringConverter;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.test.utils.XMISaver;
 import org.eclipse.mylyn.docs.intent.serializer.IntentSerializer;
 import org.junit.Test;
 
 /**
  * Tests for the IntentParser.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentParserTest {
 
 	private IntentParser parser;
 
 	private IntentSerializer serializer;
 
 	/**
 	 * IntentParserTest constructor.
 	 */
 	public IntentParserTest() {
 		parser = new IntentParser();
 		serializer = new IntentSerializer();
 	}
 
 	@Test
 	public void testSerialization() {
 		try {
			File file = new File("dataTests/intentDocuments/intentdocumentspecification/document.intent");
 
 			String section = FileToStringConverter.getFileAsString(file);
 			EObject generated = parser.parse(section);
 			XMISaver.saveASXMI(generated, new File("expectedResults/intentDocuments/intentDocument.xmi"));
 			Assert.assertEquals(section, serializer.serialize(generated));
 		} catch (IOException e) {
 			throw new AssertionFailedError(e.getMessage());
 		} catch (ParseException e) {
 			throw new AssertionFailedError(e.getMessage());
 		}
 	}
 
 	/**
 	 * Tests parser and serializer behavior when faced to big documents.
 	 */
 	@Test
 	public void testScalability() {
 		try {
 			File file = new File("dataTests/intentDocuments/scalability/uml.intent");
 
 			String section = FileToStringConverter.getFileAsString(file);
 			EObject generated = parser.parse(section);
 			// XMISaver.saveASXMI(generated, new File("expectedResults/IntentDocuments/uml.xmi"));
 			Assert.assertEquals(section, serializer.serialize(generated));
 		} catch (IOException e) {
 			throw new AssertionFailedError(e.getMessage());
 		} catch (ParseException e) {
 			throw new AssertionFailedError(e.getMessage());
 		}
 	}
 
 	/**
 	 * Ensures that Description Units are correctly serialized.
 	 */
 	@Test
 	public void testDescriptionUnitOrder() {
 		try {
			File file = new File("dataTests/intentDocuments/intentdocumentspecification/du_order.intent");
 
 			String section = FileToStringConverter.getFileAsString(file);
 			EObject generated = parser.parse(section);
 			ArrayList arrayList = new ArrayList(
 					((Paragraph)((DescriptionBloc)((IntentDocument)generated).getChapters().iterator().next()
 							.getDescriptionUnits().iterator().next().getInstructions().iterator().next())
 							.getDescriptionBloc().getContent().iterator().next()).getContent());
 
 			Assert.assertEquals(section, serializer.serialize(generated));
 		} catch (IOException e) {
 			throw new AssertionFailedError(e.getMessage());
 		} catch (ParseException e) {
 			throw new AssertionFailedError(e.getMessage());
 		}
 	}
 
 }
