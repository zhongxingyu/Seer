 /*******************************************************************************
  * Copyright (c) 2007-2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.jsp.test.ca;
 
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.jboss.tools.common.base.test.contentassist.AbstractContentAssistantTestCase;
 import org.jboss.tools.jst.jsp.contentassist.AutoContentAssistantProposal;
 import org.jboss.tools.jst.jsp.jspeditor.JSPMultiPageEditor;
 import org.jboss.tools.jst.jsp.jspeditor.JSPTextEditor;
 
 public class ContentAssistantTestCase extends AbstractContentAssistantTestCase {
 	protected JSPMultiPageEditor jspEditor = null;
 	protected JSPTextEditor jspTextEditor = null;
 
 	protected void obtainTextEditor(IEditorPart editorPart) {
 		if (editorPart instanceof JSPMultiPageEditor)
 			jspEditor = (JSPMultiPageEditor) editorPart;
 
 		assertNotNull("Cannot get the JSP Text Editor instance for page \"" //$NON-NLS-1$
 						+ fileName + "\"", jspEditor);
 		
 		// clean deffered events 
 		while (Display.getCurrent().readAndDispatch());
 
 		textEditor = jspTextEditor = jspEditor.getJspEditor();
 	}
 
	protected ISourceViewer getTextViewer() {
		return ((StructuredTextEditor)textEditor).getTextViewer();
	}

 	protected boolean isRelevantProposal(ICompletionProposal proposal) {
 		return proposal instanceof AutoContentAssistantProposal;
 	}
 
 	/**
 	 * @return the jspEditor
 	 */
 	public JSPMultiPageEditor getJspEditor() {
 		return jspEditor;
 	}
 
 	/**
 	 * @param jspEditor the jspEditor to set
 	 */
 	public void setJspEditor(JSPMultiPageEditor jspEditor) {
 		this.jspEditor = jspEditor;
 	}
 
 	/**
 	 * @return the jspTextEditor
 	 */
 	public JSPTextEditor getJspTextEditor() {
 		return jspTextEditor;
 	}
 
 	/**
 	 * @param jspTextEditor the jspTextEditor to set
 	 */
 	public void setJspTextEditor(JSPTextEditor jspTextEditor) {
 		this.jspTextEditor = jspTextEditor;
 	}
 
 }
