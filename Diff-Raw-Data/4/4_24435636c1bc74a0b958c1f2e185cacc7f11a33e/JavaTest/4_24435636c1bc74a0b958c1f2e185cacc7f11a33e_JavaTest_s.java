 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.synchronization;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotationMessageType;
 import org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.AbstractDemoTest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AnnotationUtils;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.test.utils.FileToStringConverter;
 
 /**
  * Tests the Intent demo, part 4: Java synchronization behavior.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class JavaTest extends AbstractDemoTest {
 
 	private static final String SYNC_WARNING_MSG = "The AcceptanceTest is defined in the <b>Working Copy</b> model<br/>but not in the <b>Current Document</b> model.";
 
 	private static final int[] EDITOR_SECTION_3_7 = new int[] {3, 7,
 	};
 
 	private static final int[] EDITOR_SECTION_7_1 = new int[] {7, 1,
 	};
 
 	private static final int[] EDITOR_SECTION_7_2 = new int[] {7, 2,
 	};
 
 	private static final String SECTION_37_V1_FILENAME = "data/unit/demo/Section_3.7_v1";
 
 	private static final String SECTION_37_V2_FILENAME = "data/unit/demo/Section_3.7_v2";
 
 	private static final String SECTION_37_V3_FILENAME = "data/unit/demo/Section_3.7_v3";
 
 	private static final String SECTION_72_FILENAME = "data/unit/demo/Section_7.2";
 
 	private static final String JAVA_TEST_FILENAME = "data/unit/demo/PatchCreationThroughCompareDialogTest.java";
 
 	/**
 	 * Ensures that synchronization errors between a document and java code are detected and can be fixed.
 	 * 
 	 * @throws IOException
 	 *             if an error occurs while getting test file contents
 	 * @throws CoreException
 	 *             if an error occurs during file copy
 	 */
 	public void testSynchronization() throws IOException, CoreException {
 
 		// Step 1 : open the editor at 3.7
 		IntentEditor editor37 = openIntentEditor(getIntentSection(EDITOR_SECTION_3_7));
 		IntentEditorDocument document37 = (IntentEditorDocument)editor37.getDocumentProvider().getDocument(
 				editor37.getEditorInput());
 
 		// Step 2 : create a modeling unit section, check for the annotation
 		document37.set(getFileContent(SECTION_37_V1_FILENAME));
 
 		repositoryListener.startRecording();
 		editor37.doSave(new NullProgressMonitor());
 		waitForCompiler();
 
 		assertTrue(TEST_COMPILER_NO_ERROR_MSG, AnnotationUtils.hasIntentAnnotation(editor37,
 				IntentAnnotationMessageType.COMPILER_INFO,
 				"-The required feature 'isTestedBy' of 'patchingDifferences' must be set", true));
 		assertTrue(TEST_COMPILER_NO_ERROR_MSG, AnnotationUtils.hasIntentAnnotation(editor37,
 				IntentAnnotationMessageType.COMPILER_INFO,
 				"-The required feature 'accessibleThrough' of 'patchingDifferences' must be set", true));
 
 		// Step 3 : update the modeling unit section, check that the last annotation disappeared
 		document37.set(getFileContent(SECTION_37_V2_FILENAME));
 
 		repositoryListener.startRecording();
 		editor37.doSave(new NullProgressMonitor());
 		waitForCompiler();
 
 		assertFalse(TEST_COMPILER_INVALID_ERROR_MSG, AnnotationUtils.hasIntentAnnotation(editor37,
 				IntentAnnotationMessageType.COMPILER_INFO,
 				"-The required feature 'accessibleThrough' of 'patchingDifferences' must be set", true));
 
 		// Step 4 : create a java test class
 		repositoryListener.startRecording();
 		File javaTestFile = new File(JAVA_TEST_FILENAME);
 		IProject testProject = ResourcesPlugin.getWorkspace().getRoot()
 				.getProject("org.eclipse.emf.compare.tests");
 
 		// TODO remove this work-aourdn : update WorkspaceUtils.unzipAllProjects()
 		IFolder patchPackage = testProject
 				.getFolder("src/org/eclipse/emf/compare/tests/acceptance/comparedialog/patch");
 		if (!patchPackage.exists()) {
 			patchPackage.create(true, true, new NullProgressMonitor());
 		}
 		IFile newJavaTestIFile = testProject
 				.getFile("src/org/eclipse/emf/compare/tests/acceptance/comparedialog/patch/PatchCreationThroughCompareDialogTest.java");
 		FileInputStream is = new FileInputStream(javaTestFile);
 		newJavaTestIFile.create(is, false, new NullProgressMonitor());
 		is.close();
 		newJavaTestIFile.getParent().refreshLocal(IContainer.DEPTH_INFINITE, new NullProgressMonitor());
 		waitForSynchronizer();
 
 		// Step 5 : open 7.1, then check for the annotation
 		IntentEditor editor71 = openIntentEditor(getIntentSection(EDITOR_SECTION_7_1));
 		assertTrue(TEST_SYNCHRONIZER_NO_WARNING_MSG, AnnotationUtils.hasIntentAnnotation(editor71,
 				IntentAnnotationMessageType.SYNC_WARNING, SYNC_WARNING_MSG, false));
 
 		// Step 6 : fix error from 7.1 in 7.2
 
 		// Step 6.1 : open 7.2
 		IntentEditor editor72 = openIntentEditor(getIntentSection(EDITOR_SECTION_7_2));
 		IntentEditorDocument document72 = (IntentEditorDocument)editor72.getDocumentProvider().getDocument(
 				editor72.getEditorInput());
 
 		// Step 6.2 : update 7.2
 		document72.set(getFileContent(SECTION_72_FILENAME));
 
 		repositoryListener.startRecording();
 		editor72.doSave(new NullProgressMonitor());
 		waitForCompiler();
 
 		// Step 6.3 : check 7.1
		assertFalse(TEST_SYNCHRONIZER_NO_WARNING_MSG, AnnotationUtils.hasIntentAnnotation(editor71,
				IntentAnnotationMessageType.COMPILER_INFO, SYNC_WARNING_MSG, true));
 
 		// Step 7 : fix error in 3.7
 		document37.set(getFileContent(SECTION_37_V3_FILENAME));
 		editor37.doSave(new NullProgressMonitor());
 		assertFalse(TEST_COMPILER_INVALID_ERROR_MSG, AnnotationUtils.hasIntentAnnotation(editor37,
 				IntentAnnotationMessageType.COMPILER_INFO,
 				"-The required feature 'isTestedBy' of 'patchingDifferences' must be set", true));
 	}
 
 	/**
 	 * Return the content of the given file as String.
 	 * 
 	 * @param filePath
 	 *            the file path
 	 * @return the file content
 	 * @throws IOException
 	 *             if the file cannot be read
 	 */
 	private String getFileContent(String filePath) throws IOException {
 		File file = new File(filePath);
 		return FileToStringConverter.getFileAsString(file);
 	}
 }
