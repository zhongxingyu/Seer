 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.text;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.dltk.internal.ui.editor.ModelElementHyperlinkDetector;
 import org.eclipse.dltk.internal.ui.editor.ScriptSourceViewer;
 import org.eclipse.dltk.internal.ui.text.HTMLTextPresenter;
 import org.eclipse.dltk.internal.ui.text.ScriptCompositeReconcilingStrategy;
 import org.eclipse.dltk.internal.ui.text.ScriptElementProvider;
 import org.eclipse.dltk.internal.ui.text.ScriptReconciler;
 import org.eclipse.dltk.internal.ui.text.hover.EditorTextHoverDescriptor;
 import org.eclipse.dltk.internal.ui.text.hover.EditorTextHoverProxy;
 import org.eclipse.dltk.internal.ui.text.hover.ScriptInformationProvider;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.actions.IScriptEditorActionDefinitionIds;
 import org.eclipse.dltk.ui.text.completion.ContentAssistPreference;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.AbstractInformationControlManager;
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.ITextHover;
 import org.eclipse.jface.text.ITextViewerExtension2;
 import org.eclipse.jface.text.contentassist.ContentAssistant;
 import org.eclipse.jface.text.contentassist.IContentAssistant;
 import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
 import org.eclipse.jface.text.information.IInformationPresenter;
 import org.eclipse.jface.text.information.IInformationProvider;
 import org.eclipse.jface.text.information.InformationPresenter;
 import org.eclipse.jface.text.reconciler.IReconciler;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public abstract class ScriptSourceViewerConfiguration extends
 		TextSourceViewerConfiguration {
 
 	private IColorManager fColorManager;
 	private ITextEditor fTextEditor;
 	private String fDocumentPartitioning;
 
 	public ScriptSourceViewerConfiguration(IColorManager colorManager,
 			IPreferenceStore preferenceStore, ITextEditor editor,
 			String partitioning) {
 		super(preferenceStore);
 
 		fColorManager = colorManager;
 		fTextEditor = editor;
 		fDocumentPartitioning = partitioning;
 
 		initializeScanners();
 	}
 
 	protected void initializeScanners() {
 		
 	}
 
 	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
 		if (fDocumentPartitioning != null)
 			return fDocumentPartitioning;
 		return super.getConfiguredDocumentPartitioning(sourceViewer);
 	}
 
 	protected IColorManager getColorManager() {
 		return this.fColorManager;
 	}
 
 	public ITextEditor getEditor() {
 		return this.fTextEditor;
 	}
 
 	public IReconciler getReconciler(ISourceViewer sourceViewer) {
 		final ITextEditor editor = getEditor();
 		// if (editor != null && editor.isEditable()) {
 
 		ScriptCompositeReconcilingStrategy strategy = new ScriptCompositeReconcilingStrategy(
 				editor, getConfiguredDocumentPartitioning(sourceViewer));
 		ScriptReconciler reconciler = new ScriptReconciler(editor, strategy,
 				false);
 		reconciler.setIsIncrementalReconciler(false);
 		reconciler.setProgressMonitor(new NullProgressMonitor());
 		reconciler.setDelay(500);
 
 		return reconciler;
 		// }
 		// return null;
 	}
 
 	public  boolean affectsTextPresentation(PropertyChangeEvent event) {
 		return false;
 	}
 
 	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
 		
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(org.eclipse.jface.text.source.ISourceViewer,
 	 *      java.lang.String)
 	 */
 	public String[] getDefaultPrefixes(ISourceViewer sourceViewer,
 			String contentType) {
 		return new String[] { getCommentPrefix(), "" }; //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the comment prefix.
 	 * 
 	 * <p>
 	 * Default implementation returns a <code>#</code>, sub-classes may
 	 * override if their language uses a different prefix.
 	 * </p>
 	 */
 	protected String getCommentPrefix() {
 		return "#"; //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the outline presenter control creator. The creator is a factory
 	 * creating outline presenter controls for the given source viewer. This
 	 * implementation always returns a creator for
 	 * <code>ScriptOutlineInformationControl</code> instances.
 	 * 
 	 * @param sourceViewer
 	 *            the source viewer to be configured by this configuration
 	 * @param commandId
 	 *            the ID of the command that opens this control
 	 * @return an information control creator
 	 * 
 	 */
 	protected IInformationControlCreator getOutlinePresenterControlCreator(
 			ISourceViewer sourceViewer, final String commandId) {
 		return null;
 	}
 
 	public IInformationPresenter getOutlinePresenter(
 			ScriptSourceViewer sourceViewer, boolean doCodeResolve) {
 		InformationPresenter presenter;
 		if (doCodeResolve)
 			presenter = new InformationPresenter(
 					getOutlinePresenterControlCreator(sourceViewer,
 							IScriptEditorActionDefinitionIds.OPEN_STRUCTURE));
 		else
 			presenter = new InformationPresenter(
 					getOutlinePresenterControlCreator(sourceViewer,
 							IScriptEditorActionDefinitionIds.SHOW_OUTLINE));
 		presenter
 				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
 		presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
 		IInformationProvider provider = new ScriptElementProvider(getEditor(),
 				doCodeResolve);
 		presenter.setInformationProvider(provider,
 				IDocument.DEFAULT_CONTENT_TYPE);
 		initializeQuickOutlineContexts(presenter, provider);
 
 		presenter.setSizeConstraints(50, 20, true, false);
 		return presenter;
 	}
 
 	protected void initializeQuickOutlineContexts(
 			InformationPresenter presenter, IInformationProvider provider) {
 	}
 
 	public IInformationPresenter getHierarchyPresenter(
 			ScriptSourceViewer viewer, boolean b) {
 		return null;
 	}
 
 	protected IDialogSettings getSettings(String sectionName) {
 		IDialogSettings settings = DLTKUIPlugin.getDefault()
 				.getDialogSettings().getSection(sectionName);
 		if (settings == null)
 			settings = DLTKUIPlugin.getDefault().getDialogSettings()
 					.addNewSection(sectionName);
 
 		return settings;
 	}
 
 	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
 		if (!fPreferenceStore
 				.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED))
 			return null;
 
 		IHyperlinkDetector[] inheritedDetectors = super
 				.getHyperlinkDetectors(sourceViewer);
 
 		if (fTextEditor == null) {
 			return inheritedDetectors;
 		}
 
 		int inheritedDetectorsLength = inheritedDetectors != null ? inheritedDetectors.length
 				: 0;
 		IHyperlinkDetector[] detectors = new IHyperlinkDetector[inheritedDetectorsLength + 1];
 		detectors[0] = new ModelElementHyperlinkDetector(fTextEditor);
 		for (int i = 0; i < inheritedDetectorsLength; i++)
 			detectors[i + 1] = inheritedDetectors[i];
 
 		return detectors;
 	}
 
 	/*
 	 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer,
 	 *      String)
 	 * 
 	 */
 	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer,
 			String contentType) {
 		EditorTextHoverDescriptor[] hoverDescs = DLTKUIPlugin.getDefault()
 				.getEditorTextHoverDescriptors(fPreferenceStore);
 		int stateMasks[] = new int[hoverDescs.length];
 		int stateMasksLength = 0;
 		for (int i = 0; i < hoverDescs.length; i++) {
 			if (hoverDescs[i].isEnabled()) {
 				int j = 0;
 				int stateMask = hoverDescs[i].getStateMask();
 				while (j < stateMasksLength) {
 					if (stateMasks[j] == stateMask)
 						break;
 					j++;
 				}
 				if (j == stateMasksLength)
 					stateMasks[stateMasksLength++] = stateMask;
 			}
 		}
 		if (stateMasksLength == hoverDescs.length)
 			return stateMasks;
 
 		int[] shortenedStateMasks = new int[stateMasksLength];
 		System.arraycopy(stateMasks, 0, shortenedStateMasks, 0,
 				stateMasksLength);
 		return shortenedStateMasks;
 	}
 
 	/*
 	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
 	 * 
 	 */
 	public ITextHover getTextHover(ISourceViewer sourceViewer,
 			String contentType, int stateMask) {
 		EditorTextHoverDescriptor[] hoverDescs = DLTKUIPlugin.getDefault()
 				.getEditorTextHoverDescriptors(fPreferenceStore);
 		int i = 0;
 		while (i < hoverDescs.length) {
 			if (hoverDescs[i].isEnabled()
 					&& hoverDescs[i].getStateMask() == stateMask)
 				return new EditorTextHoverProxy(hoverDescs[i], getEditor(),
 						fPreferenceStore);
 			i++;
 		}
 
 		return null;
 	}
 
 	public ITextHover getTextHover(ISourceViewer sourceViewer,
 			String contentType) {
 		return getTextHover(sourceViewer, contentType,
 				ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
 	}
 
 	/**
 	 * Returns the information presenter control creator. The creator is a
 	 * factory creating the presenter controls for the given source viewer. This
 	 * implementation always returns a creator for
 	 * <code>DefaultInformationControl</code> instances.
 	 * 
 	 * @param sourceViewer
 	 *            the source viewer to be configured by this configuration
 	 * @return an information control creator
 	 * 
 	 */
 	private IInformationControlCreator getInformationPresenterControlCreator(
 			ISourceViewer sourceViewer) {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				int shellStyle = SWT.RESIZE | SWT.TOOL;
 				int style = SWT.V_SCROLL | SWT.H_SCROLL;
 				return new DefaultInformationControl(parent, shellStyle, style,
 						new HTMLTextPresenter(false));
 			}
 		};
 	}
 
 	public IInformationPresenter getInformationPresenter(
 			ISourceViewer sourceViewer) {
 		InformationPresenter presenter = new InformationPresenter(
 				getInformationPresenterControlCreator(sourceViewer));
 		presenter
 				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
 		IInformationProvider provider = new ScriptInformationProvider(
 				getEditor());
 		presenter.setInformationProvider(provider,
 				IDocument.DEFAULT_CONTENT_TYPE);
 
 		presenter.setSizeConstraints(60, 10, true, true);
 		return presenter;
 	}
 	
 	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
 		if (getEditor() != null) {
 			ContentAssistant assistant = new ContentAssistant();
 
 			assistant
 					.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
 			assistant
 					.setRestoreCompletionProposalSize(getSettings("completion_proposal_size")); //$NON-NLS-1$
 			assistant
 					.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
 			assistant
 					.setInformationControlCreator(getInformationControlCreator(sourceViewer));
 			
			alterContentAssistant(assistant);
			
 			getContentAssistPreference().configure(assistant, fPreferenceStore);
 			
 			
 			return assistant;
 		}
 
 		return null;
 	}
 	
 	protected abstract ContentAssistPreference getContentAssistPreference();
 	
 	protected void alterContentAssistant(ContentAssistant assistant) {
 		// empty implementation
 	}
 }
