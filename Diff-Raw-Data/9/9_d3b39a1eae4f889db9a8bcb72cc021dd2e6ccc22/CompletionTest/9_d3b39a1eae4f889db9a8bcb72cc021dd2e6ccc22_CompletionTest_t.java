 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.editor;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContentAssistant;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest;
 
 /**
  * Tests the completion on the Intent demo.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class CompletionTest extends AbstractIntentUITest {
 
 	private static final String INTENT_DOC_PATH = "data/unit/demo/demo_as_text";
 
 	private static final String INTENT_DOC_WITH_ENUMS_PATH = "data/unit/documents/scenario/eenums/docWithEnums.intent";
 
 	private static final String KW_CHAPTER = "Chapter";
 
 	private static final String KW_SECTION = "Section";
 
 	private static final String TEMPLATE_DESC_CHAPTER = "Chapter - Chapter";
 
 	private static final String TEMPLATE_DESC_SECTION = "Section - Section";
 
 	private static final String TEMPLATE_DESC_MU = "Modeling Unit - Modeling Unit";
 
 	private static final String TEMPLATE_DESC_RESOURCE = "Resource - Declaration of a new Resource";
 
 	private static final String TEMPLATE_DESC_INST = "new - Declaration of a new entity";
 
	private static final String TEMPLATE_DESC_REF = "@ref - Declaration of a new internal entity (stored only inside the intent repository)";

 	private static final String TEMPLATE_DESC_URI = "Resource URI - URI indicating the Resource location";
 
 	private static final String TEMPLATE_DESC_CONTENT = "Resource Content - Add content to the Resource";
 
 	private static final String TEMPLATE_DESC_BOOL_VALUE = "value (of type EBoolean) - Default: false - Set a simple value of type EBoolean";
 
 	private IntentEditor editor;
 
 	private IContentAssistant contentAssistant;
 
 	private IDocument document;
 
 	/**
 	 * Ensures that completion behaves as expected when called on structural features with a EEnum value.
 	 * 
 	 * @throws BadLocationException
 	 */
 	// CHECKSTYLE:OFF
 	public void testCompletionOnEEnums() throws BadLocationException {
 		setUpIntentProject("completionTest", INTENT_DOC_WITH_ENUMS_PATH);
 		editor = openIntentEditor();
 		document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
 		contentAssistant = editor.getViewerConfiguration().getContentAssistant(editor.getProjectionViewer());
 
 		ICompletionProposal[] proposals = getCompletionProposals(112);
 		assertEquals(4, proposals.length);
 		String enumSuffix = " value (of type CompilationStatusSeverity) - Default: WARNING - Set a simple value of type CompilationStatusSeverity";
 		assertEquals("'WARNING'" + enumSuffix, proposals[0].getDisplayString());
 		assertEquals("'ERROR'" + enumSuffix, proposals[1].getDisplayString());
 		assertEquals("'INFO'" + enumSuffix, proposals[2].getDisplayString());
 		assertEquals("'OK'" + enumSuffix, proposals[3].getDisplayString());
 	}
 
 	/**
 	 * Tests the completion on various offsets.
 	 * 
 	 * @throws Exception
 	 */
 	public void testCompletion() throws Exception {
 		setUpIntentProject("completionTest", INTENT_DOC_PATH);
 		editor = openIntentEditor();
 		document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
 		contentAssistant = editor.getViewerConfiguration().getContentAssistant(editor.getProjectionViewer());
 
 		ICompletionProposal[] proposals = null;
 
 		/*
 		 * STRUCTURE
 		 */
 		// inside a document
 		proposals = getCompletionProposals(12);
 		assertEquals(1, proposals.length);
 		assertEquals(TEMPLATE_DESC_CHAPTER, proposals[0].getDisplayString());
 		proposals = getCompletionProposals(15);
 		assertEquals(2, proposals.length);
 		assertEquals(KW_CHAPTER, proposals[0].getDisplayString());
 		assertEquals(TEMPLATE_DESC_CHAPTER, proposals[1].getDisplayString());
 
 		// inside a chapter
 		proposals = getCompletionProposals(741);
 		assertEquals(1, proposals.length);
 		assertEquals(TEMPLATE_DESC_SECTION, proposals[0].getDisplayString());
 		proposals = getCompletionProposals(745);
 		assertEquals(2, proposals.length);
 		assertEquals(KW_SECTION, proposals[0].getDisplayString());
 		assertEquals(TEMPLATE_DESC_SECTION, proposals[1].getDisplayString());
 
 		// inside a section
 		proposals = getCompletionProposals(773);
 		assertEquals(2, proposals.length);
 		assertEquals(TEMPLATE_DESC_SECTION, proposals[0].getDisplayString());
 		assertEquals(TEMPLATE_DESC_MU, proposals[1].getDisplayString());
 
 		/*
 		 * MODELING UNITS
 		 */
 		// beginning of the modeling unit
 		proposals = getCompletionProposals(2138);
 		// We should propose to create a new resource, a new entity, or contribute to an existing entity
 		assertIsExpectedProposalsForEmptyModelingUnit(proposals);
 
 		// beginning of a named modeling unit
 		proposals = getCompletionProposals(3232);
 		assertIsExpectedProposalsForEmptyModelingUnit(proposals);
 
 		// resource declaration patterns
 		proposals = getCompletionProposals(3263);
 		assertEquals(2, proposals.length);
 		assertEquals(TEMPLATE_DESC_URI, proposals[0].getDisplayString());
 		assertEquals(TEMPLATE_DESC_CONTENT, proposals[1].getDisplayString());
 
 		// features proposals in instanciation
 		proposals = getCompletionProposals(3557);
 		assertEquals(2, proposals.length);
 		assertEquals("nsURI : EString [?] - Set the value EPackage.nsURI", proposals[0].getDisplayString());
 		assertEquals("nsPrefix : EString [?] - Set the value EPackage.nsPrefix",
 				proposals[1].getDisplayString());
 
 		// features proposals in contribution
 		proposals = getCompletionProposals(3848);
 		assertEquals(1, proposals.length);
 		assertEquals("eClassifiers : EClassifier [0,*] - Set the value match.eClassifiers",
 				proposals[0].getDisplayString());
 
 		// Boolean value
 		proposals = getCompletionProposals(4016);
 		assertEquals(1, proposals.length);
 		assertEquals(TEMPLATE_DESC_BOOL_VALUE, proposals[0].getDisplayString());
 
 		// Object value
 		proposals = getCompletionProposals(3985);
 		assertEquals(3, proposals.length);
 		// We should propose to create a new Element
 		assertEquals("new Element (of type EClassifier) - Set this new Element as value for eType",
 				proposals[0].getDisplayString());
 		// Use existing elements (of matching type) defined in the document
 		assertEquals("Reference to MatchElement - Set the MatchElement element as value for eType",
 				proposals[1].getDisplayString());
 		// And available Classifiers from the package registry
 		assertEquals("MatchElement - http://www.eclipse.org/emf/compare/match/1.1",
 				proposals[2].getDisplayString());
 
 		// instanciation proposals
 		proposals = getCompletionProposals(3865);
 		assertEquals(2, proposals.length);
 		assertEquals("EClass - http://www.eclipse.org/emf/2002/Ecore", proposals[0].getDisplayString());
 		assertEquals("EClassifier - http://www.eclipse.org/emf/2002/Ecore", proposals[1].getDisplayString());
 
 		// features proposals further in contribution
 		proposals = getCompletionProposals(4128);
 		assertEquals(1, proposals.length);
 		assertEquals(
 				"eStructuralFeatures : EStructuralFeature [0,*] - Set the value EClass.eStructuralFeatures",
 				proposals[0].getDisplayString());
 	}
 
 	private void assertIsExpectedProposalsForEmptyModelingUnit(ICompletionProposal[] proposals) {
		assertEquals(15, proposals.length);
 		assertEquals(TEMPLATE_DESC_RESOURCE, proposals[0].getDisplayString());
 		assertEquals(TEMPLATE_DESC_INST, proposals[1].getDisplayString());
		assertEquals(TEMPLATE_DESC_REF, proposals[2].getDisplayString());
		for (int i = 3; i < proposals.length; i++) {
 			assertTrue("We should propose to contribute to an existing element", proposals[i]
 					.getDisplayString().contains("(contribution"));
 		}
 	}
 
 	// CHECKSTYLE:ON
 
 	/**
 	 * Returns the completion proposals at the given offset.
 	 * 
 	 * @param offset
 	 *            the completion launch offset
 	 * @return the completion proposals at the given offset
 	 * @throws BadLocationException
 	 */
 	private ICompletionProposal[] getCompletionProposals(int offset) throws BadLocationException {
 		return contentAssistant.getContentAssistProcessor(document.getContentType(offset))
 				.computeCompletionProposals(editor.getProjectionViewer(), offset);
 	}
 }
