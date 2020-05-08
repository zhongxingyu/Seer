 /*******************************************************************************
  * Copyright (c) 2012 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.ui.tests.autoedit;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static org.eclipse.dltk.javascript.ui.tests.autoedit.Document.getEndOfLineOffset;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.tests.ProjectSetup;
 import org.eclipse.dltk.javascript.ui.tests.AllTests;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.tests.UICompletionUtil;
 import org.eclipse.dltk.ui.tests.UITestUtils;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.IEditorPart;
 import org.junit.After;
 import org.junit.Rule;
 import org.junit.Test;
 
 public class JSDocEditorTests extends JSAutoEditStrategyTestCase {
 
 	@Rule
 	public final ProjectSetup project = new ProjectSetup(AllTests.WORKSPACE,
 			"jsdoc");
 
 	@After
 	public void closeEditors() {
 		UITestUtils.closeAllEditors();
 	}
 
 	@Test
 	public void functionGeneratedTags() throws BadLocationException,
 			CoreException {
 		final ISourceModule module = checkNotNull(project.getSourceModule(
 				"src", "file1.js"));
 		final IEditorPart part = checkNotNull(DLTKUIPlugin.openInEditor(module));
 		final IDocument document = UICompletionUtil.on(part).getDocument();
 		execute(document, createCommand(ENTER, getEndOfLineOffset(document, 0)));
 		assertEquals(project.getFileContentsAsString("src/file1.txt"),
 				document.get());
 	}
	
	@Test
	public void functionGeneratedTagsWithReturn() throws BadLocationException,
			CoreException {
		final ISourceModule module = checkNotNull(project.getSourceModule(
				"src", "file4.js"));
		final IEditorPart part = checkNotNull(DLTKUIPlugin.openInEditor(module));
		final IDocument document = UICompletionUtil.on(part).getDocument();
		execute(document, createCommand(ENTER, getEndOfLineOffset(document, 0)));
		assertEquals(project.getFileContentsAsString("src/file4.txt"),
				document.get());
	}

 
 	@Test
 	public void functionInObjectLiteralGeneratedTags()
 			throws BadLocationException, CoreException {
 		final ISourceModule module = checkNotNull(project.getSourceModule(
 				"src", "file2.js"));
 		final IEditorPart part = checkNotNull(DLTKUIPlugin.openInEditor(module));
 		final IDocument document = UICompletionUtil.on(part).getDocument();
 		execute(document, createCommand(ENTER, getEndOfLineOffset(document, 1)));
 		assertEquals(project.getFileContentsAsString("src/file2.txt"),
 				document.get());
 	}
	
	@Test
	public void functionInObjectLiteralGeneratedTagsWithReturn()
			throws BadLocationException, CoreException {
		final ISourceModule module = checkNotNull(project.getSourceModule(
				"src", "file3.js"));
		final IEditorPart part = checkNotNull(DLTKUIPlugin.openInEditor(module));
		final IDocument document = UICompletionUtil.on(part).getDocument();
		execute(document, createCommand(ENTER, getEndOfLineOffset(document, 1)));
		assertEquals(project.getFileContentsAsString("src/file3.txt"),
				document.get());
	}
 
 }
