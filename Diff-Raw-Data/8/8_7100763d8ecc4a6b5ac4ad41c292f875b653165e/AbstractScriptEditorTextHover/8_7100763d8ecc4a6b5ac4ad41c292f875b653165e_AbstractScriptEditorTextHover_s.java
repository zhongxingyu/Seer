 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.text.hover;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ICodeAssist;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.internal.ui.text.HTMLTextPresenter;
 import org.eclipse.dltk.internal.ui.text.ScriptWordFinder;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.IWorkingCopyManager;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.dltk.ui.actions.IScriptEditorActionDefinitionIds;
 import org.eclipse.dltk.ui.text.hover.IScriptEditorTextHover;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextHoverExtension;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Region;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.keys.IBindingService;
 import org.osgi.framework.Bundle;
 
 /**
  * Abstract class for providing hover information for Script elements.
  */
 public abstract class AbstractScriptEditorTextHover implements
 		IScriptEditorTextHover, ITextHoverExtension {
 
 	/**
 	 * The style sheet (css).
 	 */
 	private static String fgStyleSheet;
 	private IEditorPart fEditor;
 	private IPreferenceStore fStore;
 
 	private IBindingService fBindingService;
 	{
 		fBindingService = (IBindingService) PlatformUI.getWorkbench()
 				.getAdapter(IBindingService.class);
 	}
 
 	public void setPreferenceStore(IPreferenceStore store) {
 		fStore = store;
 	}
 
 	/**
 	 * @return the fStore
 	 */
 	protected IPreferenceStore getPreferenceStore() {
 		return fStore;
 	}
 
 	public void setEditor(IEditorPart editor) {
 		fEditor = editor;
 	}
 
 	protected IEditorPart getEditor() {
 		return fEditor;
 	}
 
 	protected ICodeAssist getCodeAssist() {
 		if (fEditor != null) {
 			IEditorInput input = fEditor.getEditorInput();
 
 			IWorkingCopyManager manager = DLTKUIPlugin.getDefault()
 					.getWorkingCopyManager();
 			return manager.getWorkingCopy(input, false);
 		}
 
 		return null;
 	}
 
 	public IRegion getHoverRegion(final ITextViewer textViewer, int offset) {
 
 		final IRegion[] result = new IRegion[] { ScriptWordFinder.findWord(
 				textViewer.getDocument(), offset) };
 
 		textViewer.getTextWidget().getDisplay().syncExec(new Runnable() {
 			public void run() {
 				Point selection = textViewer.getSelectedRange();
 				int off = selection.x;
 				int len = selection.y;
 
 				if (len > 0) {
 					result[0] = new Region(off, len);
 				}
 			}
 		});
 
 		return result[0];
 		// return ScriptWordFinder.findWord(textViewer.getDocument(), offset);
 	}
 
 	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
 
 		String nature = null;
 		try {
 			IModelElement inputModelElement = EditorUtility
 					.getEditorInputModelElement(this.fEditor, false);
 			if (inputModelElement == null)
 				return null;
			nature = DLTKLanguageManager.getLanguageToolkit(inputModelElement)
 					.getNatureId();
 		} catch (CoreException e) {
 			return null;
 		}
 		if (nature == null) {
 			return null;
 		}
 
 		ICodeAssist resolve = getCodeAssist();
 		if (resolve != null) {
 			try {
 				String content = null;
 				try {
 					content = textViewer.getDocument().get(
 							hoverRegion.getOffset(), hoverRegion.getLength());
 				} catch (BadLocationException e) {
 				}
 
 				IModelElement[] result = resolve.codeSelect(hoverRegion
 						.getOffset(), hoverRegion.getLength());
 
 				if (result == null) {
 					return null;
 				}
 
 				int nResults = result.length;
 
 				if (nResults == 0) {
 					if (content != null) {
 						return getHoverInfo(nature, content);
 					}
 					return null;
 				}
 
 				return getHoverInfo(nature, result);
 
 			} catch (ModelException x) {
 				return null;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Provides hover information for the given Script elements.
 	 * 
 	 * @param modelElements
 	 *            the Script elements for which to provide hover information
 	 * @return the hover information string
 	 * 
 	 */
 	protected String getHoverInfo(String nature, IModelElement[] modelElements) {
 		return null;
 	}
 
 	/**
 	 * Provides hover information for the keyword.
 	 * 
 	 * @param content
 	 *            text of the keyword
 	 * @return the hover information string
 	 * 
 	 */
 	protected String getHoverInfo(String nature, String content) {
 		return null;
 	}
 
 	public IInformationControlCreator getHoverControlCreator() {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				return new DefaultInformationControl(parent, SWT.NONE,
 						new HTMLTextPresenter(true),
 						getTooltipAffordanceString());
 			}
 		};
 	}
 
 	/**
 	 * Returns the tool tip affordance string.
 	 * 
 	 * @return the affordance string or <code>null</code> if disabled or no
 	 *         key binding is defined
 	 * 
 	 */
 	protected String getTooltipAffordanceString() {
 		if (this.getPreferenceStore() == null) {
 			return "{0}";
 		}
 		if (fBindingService == null
 				|| !getPreferenceStore().getBoolean(
 						PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
 			return null;
 
 		String keySequence = fBindingService
 				.getBestActiveBindingFormattedFor(IScriptEditorActionDefinitionIds.SHOW_DOCUMENTATION);
 		if (keySequence == null)
 			return null;
 
 		return Messages.format(
 				ScriptHoverMessages.ScriptTextHover_makeStickyHint,
 				keySequence == null ? "" : keySequence); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the style sheet.
 	 * 
 	 * 
 	 */
 	protected static String getStyleSheet() {
 		if (fgStyleSheet == null) {
 			Bundle bundle = Platform.getBundle(DLTKUIPlugin.getPluginId());
 			URL styleSheetURL = bundle
 					.getEntry("/DocumentationHoverStyleSheet.css"); //$NON-NLS-1$
 			if (styleSheetURL != null) {
 				try {
 					styleSheetURL = FileLocator.toFileURL(styleSheetURL);
 					BufferedReader reader = new BufferedReader(
 							new InputStreamReader(styleSheetURL.openStream()));
 					StringBuffer buffer = new StringBuffer(200);
 					String line = reader.readLine();
 					while (line != null) {
 						buffer.append(line);
 						buffer.append('\n');
 						line = reader.readLine();
 					}
 					fgStyleSheet = buffer.toString();
 				} catch (IOException ex) {
 					DLTKUIPlugin.log(ex);
 					fgStyleSheet = ""; //$NON-NLS-1$
 				}
 			}
 		}
 		return fgStyleSheet;
 	}
 }
