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
 package org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest;
 import org.eclipse.mylyn.docs.intent.parser.test.utils.FileToStringConverter;
 
 /**
  * <p>
  * Ensures that making modifications on the document cannot lead to errors during calls to
  * ECoreUtils.resolve().
  * </p>
  * <p>
  * Relevant issues :
  * <ul>
  * <li>https://bugs.eclipse.org/bugs/show_bug.cgi?id=379390 : Making several changes inside the Intent
  * Document can lead to a stackOverflow in ECoreUtil.resolve()</li>
  * </ul>
  * </p>
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentDocumentationUpdateDoesNotCauseResolvingIssuesTest extends AbstractIntentUITest {
 	private static final String INTENT_DOC_PATH = "data/unit/documents/empty.intent";
 
 	private IntentEditor editor;
 
 	private IntentEditorDocument document;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		// Step 1 : Generic set up
 		setUpIntentProject("intentProject", INTENT_DOC_PATH, true);
 
 		// Step 2 : open an editor on the root document
 		editor = openIntentEditor();
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 	}
 
 	public void testSimpleModifications() {
 		document.set("Document {\n\tChapter Title {\n\t\tText\n\n\t\tSection Title {\n\t\t\tText\n\t\t}\n\t}\n\tChapter Title {\n\t\tText\n\t}\n}");
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 
 		document.set("Document {\n\tChapter C1 {\n\t\tText\n\n\t\tSection C11 {\n\t\t\tText\n\t\t}\n\t}\n\tChapter C2 {\n\t\tText\n\t}\n}");
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 	}
 
	public void testSectionRenamming() throws IOException {
 		String intialContent = FileToStringConverter.getFileAsString(new File(
 				"data/unit/documents/scenario/documentUpdate/documentUpdate01.intent"));
 		String renamedContent = FileToStringConverter.getFileAsString(new File(
 				"data/unit/documents/scenario/documentUpdate/documentUpdate02.intent"));
 		document.set(intialContent);
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 
 		document.set(renamedContent);
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 	}
 
	public void testMultipleContainmentSectionRenamming() throws IOException {
 		String intialContent = FileToStringConverter.getFileAsString(new File(
 				"data/unit/documents/scenario/documentUpdate/multipleContainmentSection.intent"));
 		String renamedContent = intialContent.replace("Title", "A");
 		document.set(intialContent);
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 
 		document.set(renamedContent);
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		if (editor != null) {
 			editor.close(false);
 		}
 		super.tearDown();
 	}
 }
