 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.css.test.jbide;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.wst.css.core.internal.document.CSSStructuredDocumentRegionContainer;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSStyleSheet;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.jboss.tools.jst.css.properties.CSSPropertyPage;
 import org.jboss.tools.jst.css.test.AbstractCSSViewTest;
 import org.jboss.tools.jst.css.view.CSSEditorView;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.StyleAttributes;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.Util;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.css.CSSRule;
 import org.w3c.dom.css.CSSStyleDeclaration;
 import org.w3c.dom.css.CSSStyleRule;
 
 /**
  * @author Sergey Dzmitrovich
  * 
  */
 public class InputFractionalValueTest_JBIDE4790 extends AbstractCSSViewTest {
 
 	public static final String TEST_PAGE_NAME = "JBIDE/4790/inputFractional.css"; //$NON-NLS-1$
 
 	public static final String TEST_CSS_ATTRIBUTE_NAME = "font-size"; //$NON-NLS-1$
 
 	public void testInputFractionalValue() throws CoreException {
 
 		IFile pageFile = getComponentPath(TEST_PAGE_NAME, getProjectName());
 
 		assertNotNull(pageFile);
 
 		StructuredTextEditor editor = (StructuredTextEditor) openEditor(
 				pageFile, CSS_EDITOR_ID);
 
 		assertNotNull(editor);
 
 		CSSEditorView view = (CSSEditorView) openView(CSS_EDITOR_VIEW);
 
 		assertNotNull(view);
 
 		CSSPropertyPage page = (CSSPropertyPage) view.getCurrentPage();
 
 		assertNotNull(page);
 
 		ICSSModel model = (ICSSModel) getStructuredModel(pageFile);
 
 		assertNotNull(model);
 
 		ICSSStyleSheet document = (ICSSStyleSheet) model.getDocument();
 
 		assertNotNull(document);
 
 		CSSRule cssRule = document.getCssRules().item(0);
 
 		assertNotNull(cssRule);
 
 		int offset = ((CSSStructuredDocumentRegionContainer) cssRule)
 				.getStartOffset();
 
 		setSelection(editor, offset, 0);
 
 		CSSStyleDeclaration declaration = ((CSSStyleRule) cssRule).getStyle();
 
 		String testedValue = declaration
 				.getPropertyValue(TEST_CSS_ATTRIBUTE_NAME);
 
 		assertNotNull(testedValue);
 
 		StyleAttributes styleAttributes = page.getStyleAttributes();
 
 		assertEquals(testedValue, styleAttributes
				.get(TEST_CSS_ATTRIBUTE_NAME));
 
 		String[] parsedTestValue = Util.convertExtString(testedValue);
 
 		assertEquals(parsedTestValue.length, 2);
 
 		String newTestedValue = parsedTestValue[0] + "." + parsedTestValue[1]; //$NON-NLS-1$
 
		styleAttributes.put(TEST_CSS_ATTRIBUTE_NAME,
 				newTestedValue);
 
 		testedValue = declaration.getPropertyValue(TEST_CSS_ATTRIBUTE_NAME);
 
 		assertNotNull(testedValue);
 
 		assertEquals(removeWhitespaces(newTestedValue),
 				removeWhitespaces(testedValue));
 
 		parsedTestValue = Util.convertExtString(testedValue);
 
 		assertEquals(parsedTestValue.length, 2);
 
 		newTestedValue = parsedTestValue[0] + "3" + parsedTestValue[1]; //$NON-NLS-1$
 
 		try {
			styleAttributes.put(TEST_CSS_ATTRIBUTE_NAME,
 					newTestedValue);
 
 		} catch (DOMException e) {
 			fail("Changing of attribute's value leads to DOMException. Probably it is problem concerned with of JBIDE-4790 "); //$NON-NLS-1$
 		}
 		testedValue = declaration.getPropertyValue(TEST_CSS_ATTRIBUTE_NAME);
 
 		assertNotNull(testedValue);
 
 		assertEquals(removeWhitespaces(testedValue),
 				removeWhitespaces(newTestedValue));
 
 	}
 
 	private String removeWhitespaces(String text) {
 		return text.replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 }
