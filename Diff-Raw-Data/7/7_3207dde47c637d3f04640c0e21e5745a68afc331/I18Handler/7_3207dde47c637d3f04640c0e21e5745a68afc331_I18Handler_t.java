 /*******************************************************************************
  * Copyright (c) 2007-2010 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.jsp.i18n.handlers;
 
 import java.util.Map;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.HandlerEvent;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.IElementUpdater;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.menus.UIElement;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.jboss.tools.jst.jsp.i18n.ExternalizeStringsDialog;
 import org.jboss.tools.jst.jsp.i18n.ExternalizeStringsUtils;
 import org.jboss.tools.jst.jsp.i18n.ExternalizeStringsWizard;
 import org.w3c.dom.Attr;
 
 /**
  * Externalization command handler
  * @author mareshkau
  *
  */
 public class I18Handler extends AbstractHandler implements IElementUpdater{
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		IEditorPart activeEditor= PlatformUI
 		.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(activeEditor instanceof ITextEditor){
 			ITextEditor txtEditor = (ITextEditor) activeEditor;
 			ISelection selection = txtEditor.getSelectionProvider().getSelection();
 			return getExternalizeStringsCommandEnabled(selection);
 		}
 		return false;
 	}
 	/**
 	 * 
 	 */
 	public I18Handler() {
 		setBaseEnabled(true);
 	}
 	/**
 	 * Calculates the state of ext command
 	 * @param selection
 	 * @return
 	 */
 	private static boolean getExternalizeStringsCommandEnabled(ISelection selection) {
 		boolean enabled=false;
 		String stringToUpdate = ""; //$NON-NLS-1$
 		if (ExternalizeStringsUtils.isSelectionCorrect(selection)) {
 			String text = ""; //$NON-NLS-1$
 			TextSelection textSelection = null;
 			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 			textSelection = (TextSelection) selection;
 			text = textSelection.getText();
 			Object selectedElement = structuredSelection.getFirstElement();
 			/*
 			 * When selected text is empty parse selected element and find a
 			 * string to replace..
 			 */
 			if ((text.trim().length() == 0)) {
 				if (selectedElement instanceof org.w3c.dom.Text) {
 					/*
 					 * ..it could be a plain text
 					 */
 					org.w3c.dom.Text textNode = (org.w3c.dom.Text) selectedElement;
 					if (textNode.getNodeValue().trim().length() > 0) {
 						stringToUpdate = textNode.getNodeValue();
 					}
 				} else if (selectedElement instanceof Attr) {
 					/*
 					 * ..or an attribute's value
 					 */
 					Attr attrNode = (Attr) selectedElement;
 					if (attrNode.getNodeValue().trim().length() > 0) {
 						stringToUpdate = attrNode.getNodeValue();
 					}
 				}
 			} else {
 				stringToUpdate = text;
 			}
 		}
 		if ((stringToUpdate.length() > 0)) {
 			enabled=true;
 		} 
 		return enabled;
 	}
 
 	/**
 	 * the command has been executed, so extract extract the needed information
 	 * from the application context.
 	 */
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		IEditorPart activeEditor = HandlerUtil.getActiveEditorChecked(event);
 		
 		if(activeEditor instanceof ITextEditor){
 			ExternalizeStringsDialog dlg = new ExternalizeStringsDialog(
 					PlatformUI.getWorkbench().getDisplay().getActiveShell(),
 					new ExternalizeStringsWizard((ITextEditor)activeEditor, 
 							null));
 			dlg.open();
 		}
 		return null;
 	}
 
 	public void updateElement(UIElement element, Map parameters) {
 		fireHandlerChanged(new HandlerEvent(this, true, false));
 		
 	}
 }
