 /*******************************************************************************
  * Copyright (c) 2008, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.acceleo.internal.ide.ui.editors.template;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.acceleo.ide.ui.AcceleoUIActivator;
 import org.eclipse.acceleo.ide.ui.natures.AcceleoNature;
 import org.eclipse.acceleo.internal.ide.ui.AcceleoUIMessages;
 import org.eclipse.acceleo.parser.cst.CSTNode;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jdt.ui.PreferenceConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
 import org.eclipse.jface.text.source.projection.ProjectionSupport;
 import org.eclipse.jface.text.source.projection.ProjectionViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.texteditor.ChainedPreferenceStore;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 /**
  * The Acceleo template editor (Acceleo editor).
  * 
  * @author <a href="mailto:jonathan.musset@obeo.fr">Jonathan Musset</a>
  */
 public class AcceleoEditor extends TextEditor implements IResourceChangeListener {
 
 	/**
 	 * The Acceleo editor ID.
 	 */
 	public static final String ACCELEO_EDITOR_ID = "org.eclipse.acceleo.ide.ui.editors.template.AcceleoEditor"; //$NON-NLS-1$
 
 	/**
 	 * Preference key for matching brackets.
 	 */
 	private static final String MATCHING_BRACKETS = PreferenceConstants.EDITOR_MATCHING_BRACKETS;
 
 	/**
 	 * Preference key for matching brackets color.
 	 */
 	private static final String MATCHING_BRACKETS_COLOR = PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;
 
 	/**
 	 * The source content (The semantic content for this editor). It is used to create a CST model and is able
 	 * to do an incremental parsing of the text.
 	 */
 	private AcceleoSourceContent content;
 
 	/**
 	 * Color manager for the syntax highlighting of this editor.
 	 */
 	private ColorManager colorManager;
 
 	/**
 	 * Content outline page.
 	 */
 	private AcceleoOutlinePage contentOutlinePage;
 
 	/**
 	 * A listener which is notified when the outline's selection changes.
 	 */
 	private ISelectionChangedListener selectionChangedListener;
 
 	/**
 	 * The editor's blocks matcher.
 	 */
 	private AcceleoPairMatcher blockMatcher;
 
 	/** Allows us to enable folding support on this editor. */
 	private ProjectionSupport projectionSupport;
 
 	/** This will allow us to update the folding strucutre of the document. */
 	private ProjectionAnnotationModel annotationModel;
 
 	/**
 	 * Keeps a reference to the object last updated in the outline through a double-click on the editor. This
 	 * allows us to ignore the feedback &quot;updateSelection&quot; event from the outline.
 	 */
 	private EObject updatingOutline;
 
 	/**
 	 * Constructor.
 	 */
 	public AcceleoEditor() {
 		super();
 		content = new AcceleoSourceContent();
 		colorManager = new ColorManager();
 		blockMatcher = new AcceleoPairMatcher(this);
 	}
 
 	/**
 	 * Gets the source content. It stores the CST model that represents the semantic content of the text.
 	 * 
 	 * @return the source content
 	 */
 	public AcceleoSourceContent getContent() {
 		return content;
 	}
 
 	/**
 	 * Gets the color manager. It is often used for syntax highlighting.
 	 * 
 	 * @return the color manager
 	 */
 	public ColorManager getColorManager() {
 		return colorManager;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.editors.text.TextEditor#doSetInput(org.eclipse.ui.IEditorInput)
 	 */
 	@Override
 	protected void doSetInput(IEditorInput input) throws CoreException {
 		setSourceViewerConfiguration(createSourceViewerConfiguration());
 		setDocumentProvider(createDocumentProvider());
 		super.doSetInput(input);
 		IDocumentProvider provider = getDocumentProvider();
 		if (provider != null) {
 			IFile file = getFile();
 			IDocument document = provider.getDocument(getEditorInput());
 			initializeContent(document, file);
 		}
 	}
 
 	/**
 	 * Initializes the semantic content of the file, by creating the initial version of the CST model.
 	 * 
 	 * @param document
 	 *            is the document
 	 * @param file
 	 *            is the file
 	 */
 	private void initializeContent(IDocument document, IFile file) {
 		if (document != null && file != null) {
 			try {
 				if (file.getProject().hasNature(AcceleoNature.NATURE_ID)) {
 					content.init(new StringBuffer(document.get()), file);
 					content.createCST();
 				} else {
 					MessageDialog.openError(getSite().getShell(), AcceleoUIMessages
 							.getString("AcceleoEditor.MissingNatureTitle"), //$NON-NLS-1$
 							AcceleoUIMessages.getString("AcceleoEditor.MissingNatureDescription")); //$NON-NLS-1$
 					content.init(new StringBuffer(document.get()), file);
 					content.createCST();
 				}
 			} catch (CoreException e) {
 				AcceleoUIActivator.getDefault().getLog().log(
 						new Status(IStatus.ERROR, AcceleoUIActivator.PLUGIN_ID, e.getMessage(), e));
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
 	 */
 	@Override
 	protected void initializeKeyBindingScopes() {
 		setKeyBindingScopes(new String[] {"org.eclipse.acceleo.ide.ui.editors.template.editor"}); //$NON-NLS-1$
 	}
 
 	/**
 	 * Creates the source viewer configuration.
 	 * 
 	 * @return the source viewer configuration
 	 */
 	protected SourceViewerConfiguration createSourceViewerConfiguration() {
 		return new AcceleoConfiguration(this, getPreferenceStore());
 	}
 
 	/**
 	 * Creates the document provider.
 	 * 
 	 * @return the document provider
 	 */
 	protected IDocumentProvider createDocumentProvider() {
 		return new AcceleoDocumentProvider(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.editors.text.TextEditor#dispose()
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 		colorManager.dispose();
 		getContentOutlinePage().removeSelectionChangedListener(selectionChangedListener);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		/*
 		 * Dispose the block matcher
 		 */
 		if (blockMatcher != null) {
 			blockMatcher.dispose();
 			blockMatcher = null;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.editors.text.TextEditor#getAdapter(java.lang.Class)
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public Object getAdapter(Class type) {
 		if (type.equals(IContentOutlinePage.class)) {
 			return getContentOutlinePage();
 		}
 		return super.getAdapter(type);
 	}
 
 	/**
 	 * Returns the template content outline page. Creates the listener which is notified when the outline's
 	 * selection changes.
 	 * 
 	 * @return the template content outline page
 	 */
 	protected AcceleoOutlinePage getContentOutlinePage() {
 		if (contentOutlinePage == null) {
 			contentOutlinePage = createContentOutlinePage();
 			selectionChangedListener = createSelectionChangeListener();
 			contentOutlinePage.addSelectionChangedListener(selectionChangedListener);
 			ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 		}
 		return contentOutlinePage;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(final IResourceChangeEvent event) {
 		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
 				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null
 				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() != this) {
 			if (event.getType() == IResourceChangeEvent.POST_CHANGE
 					&& deltaMembers(event.getDelta()).contains(getFile())) {
 				try {
 					init(getEditorSite(), getEditorInput());
 				} catch (PartInitException e) {
 					AcceleoUIActivator.getDefault().getLog().log(
 							new Status(IStatus.ERROR, AcceleoUIActivator.PLUGIN_ID, e.getMessage(), e));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets all the modified files in the resource delta.
 	 * 
 	 * @param delta
 	 *            the resource delta represents changes in the state of a resource tree
 	 * @return all the modified files
 	 */
 	private List<IFile> deltaMembers(IResourceDelta delta) {
 		List<IFile> files = new ArrayList<IFile>();
 		IResource resource = delta.getResource();
 		if (resource instanceof IFile) {
 			if (delta.getKind() == IResourceDelta.CHANGED) {
 				files.add((IFile)resource);
 			}
 		}
 		IResourceDelta[] children = delta.getAffectedChildren();
 		for (int i = 0; i < children.length; i++) {
 			files.addAll(deltaMembers(children[i]));
 		}
 		return files;
 	}
 
 	/**
 	 * Creates the content outline page.
 	 * 
 	 * @return the content outline page
 	 */
 	protected AcceleoOutlinePage createContentOutlinePage() {
 		return new AcceleoOutlinePage(this);
 	}
 
 	/**
 	 * Creates a listener which is notified when the outline's selection changes.
 	 * 
 	 * @return the listener which is notified when the outline's selection changes
 	 */
 	protected ISelectionChangedListener createSelectionChangeListener() {
 		return new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				selectionChangedDetected(event);
 			}
 		};
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#configureSourceViewerDecorationSupport(org.eclipse.ui.texteditor.SourceViewerDecorationSupport)
 	 */
 	@Override
 	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
 		/*
 		 * Set the block matcher
 		 */
 		support.setCharacterPairMatcher(blockMatcher);
 		support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR);
 		// TODO it was JavaPlugin.getDefault().getPreferenceStore()
 		IPreferenceStore pref = AcceleoUIActivator.getDefault().getPreferenceStore();
 		IPreferenceStore[] stores = {getPreferenceStore(), pref};
 		setPreferenceStore(new ChainedPreferenceStore(stores));
 		support.install(getPreferenceStore());
 		super.configureSourceViewerDecorationSupport(support);
 	}
 
 	/**
 	 * Methods which is notified when the outline's selection changes.
 	 * 
 	 * @param event
 	 *            is the selection changed event
 	 */
 	protected void selectionChangedDetected(SelectionChangedEvent event) {
 		ISelection selection = event.getSelection();
 		Object selectedElement = ((IStructuredSelection)selection).getFirstElement();
 		if (selectedElement == updatingOutline) {
 			// Simply ignore the event
 			updatingOutline = null;
 		} else if (selectedElement instanceof CSTNode) {
 			int b = ((CSTNode)selectedElement).getStartPosition();
 			int e = ((CSTNode)selectedElement).getEndPosition();
 			if (b > -1 && e > -1) {
 				selectRange(b, e);
 			}
 		}
 	}
 
 	/**
 	 * Updates the outline selection by using the current offset in the text. It browses the CST model to find
 	 * the element that is defined at the given offset.
 	 * 
 	 * @param posBegin
 	 *            is the beginning index of the selected text
 	 * @param posEnd
 	 *            is the ending index of the selected text
 	 */
 	public void updateSelection(int posBegin, int posEnd) {
 		int e;
 		if (posEnd < posBegin) {
 			e = posBegin;
 		} else {
 			e = posEnd;
 		}
 		if (getContentOutlinePage() != null && posBegin > -1 && e > -1) {
 			// EObject
 			AcceleoSourceContent source = getContent();
 			if (source != null) {
 				EObject object = source.getCSTNode(posBegin, e);
 				if (object != null) {
 					updatingOutline = object;
 					getContentOutlinePage().setSelection(new StructuredSelection(object));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets the highlighted range of this text editor to the specified region.
 	 * 
 	 * @param begin
 	 *            is the beginning index
 	 * @param end
 	 *            is the ending index
 	 */
 	protected void selectRange(int begin, int end) {
 		if (begin > -1 && end >= begin) {
 			ISourceViewer viewer = getSourceViewer();
 			StyledText widget = viewer.getTextWidget();
 			widget.setRedraw(false);
 			setHighlightRange(begin, end - begin, true);
 			selectAndReveal(begin, end - begin);
 			widget.setRedraw(true);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void doSave(IProgressMonitor progressMonitor) {
 		super.doSave(progressMonitor);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSaveAs()
 	 */
 	@Override
 	public void doSaveAs() {
 		super.doSaveAs();
 	}
 
 	/**
 	 * Get the input file for this editor.
 	 * 
 	 * @return the editor input file
 	 */
 	public IFile getFile() {
 		return (IFile)getEditorInput().getAdapter(IFile.class);
 	}
 
 	/**
 	 * Updates the folding structure of the template. This will be called from the Acceleo template reconciler
 	 * in order to allow the folding of blocks to the user.
 	 * 
 	 * @param addedAnnotations
 	 *            These annotations have been added since the last reconciling operation.
 	 * @param deletedAnnotations
 	 *            This list represents the annotations that were deleted since we last reconciled.
 	 * @param modifiedAnnotations
 	 *            These annotations have seen their positions updated.
 	 */
 	public void updateFoldingStructure(Map<Annotation, Position> addedAnnotations,
 			List<Annotation> deletedAnnotations, Map<Annotation, Position> modifiedAnnotations) {
 		Annotation[] deleted = new Annotation[deletedAnnotations.size()];
 		for (int i = 0; i < deletedAnnotations.size(); i++) {
 			deleted[i] = deletedAnnotations.get(i);
 		}
		if (annotationModel != null) {
			annotationModel.modifyAnnotations(deleted, addedAnnotations, null);
			for (Map.Entry<Annotation, Position> entry : modifiedAnnotations.entrySet()) {
				annotationModel.modifyAnnotationPosition(entry.getKey(), entry.getValue());
			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
 
 		projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
 		projectionSupport.install();
 
 		// turn projection mode on
 		viewer.doOperation(ProjectionViewer.TOGGLE);
 
 		annotationModel = viewer.getProjectionAnnotationModel();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite,
 	 *      org.eclipse.jface.text.source.IVerticalRuler, int)
 	 */
 	@Override
 	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
 		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(),
 				isOverviewRulerVisible(), styles);
 
 		// ensure decoration support has been created and configured.
 		getSourceViewerDecorationSupport(viewer);
 
 		return viewer;
 	}
 }
