 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.editors;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.internal.resources.ResourceException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.palette.PaletteViewerProvider;
 import org.eclipse.gef.ui.views.palette.PalettePage;
 import org.eclipse.gef.ui.views.palette.PaletteViewerPage;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.IPostSelectionProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.jsf.common.ui.internal.utils.ResourceUtils;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.dnd.internal.DesignerSourceMouseTrackAdapter;
 import org.eclipse.jst.pagedesigner.editors.pagedesigner.PageDesignerResources;
 import org.eclipse.jst.pagedesigner.editors.palette.impl.PaletteItemManager;
 import org.eclipse.jst.pagedesigner.jsp.core.pagevar.IPageVariablesProvider;
 import org.eclipse.jst.pagedesigner.jsp.core.pagevar.adapter.IDocumentPageVariableAdapter;
 import org.eclipse.jst.pagedesigner.parts.DocumentEditPart;
 import org.eclipse.jst.pagedesigner.preview.PreviewHandlerNew;
 import org.eclipse.jst.pagedesigner.preview.WindowsIEBrowser;
 import org.eclipse.jst.pagedesigner.tools.RangeSelectionTool;
 import org.eclipse.jst.pagedesigner.ui.common.PartActivationHandler;
 import org.eclipse.jst.pagedesigner.ui.common.sash.SashEditorPart;
 import org.eclipse.jst.pagedesigner.utils.PreviewUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.MultiPageSelectionProvider;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.wst.common.ui.properties.internal.provisional.ITabbedPropertySheetPageContributor;
 import org.eclipse.wst.common.ui.provisional.editors.PostMultiPageEditorSite;
 import org.eclipse.wst.common.ui.provisional.editors.PostMultiPageSelectionProvider;
 import org.eclipse.wst.common.ui.provisional.editors.PostSelectionMultiPageEditorPart;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.sse.ui.internal.IExtendedMarkupEditor;
 import org.eclipse.wst.sse.ui.internal.provisional.extensions.ISourceEditingTextTools;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.ui.internal.provisional.IDOMSourceEditingTextTools;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * The HTMLEditor is a multi paged editor. It will use the StructuredTextEditor
  * as the chief editor, and delegate most operations to it.
  * 
  * @author mengbo
  */
 public class HTMLEditor extends PostSelectionMultiPageEditorPart implements
 		IPropertyListener, ITabbedPropertySheetPageContributor {
 	// private static final String PAGE_NAME_DESIGN = "Design"; //$NON-NLS-1$
 	// private static final String PAGE_NAME_SOURCE = "Source"; //$NON-NLS-1$
 	private final static String CONTRIBUTOR_ID = "org.eclipse.jst.pagedesigner.pageDesigner.tabPropertyContributor"; //$NON-NLS-1$
 
 	// four different modes for the designer when displayed in a sash editor.
 	public static final int MODE_SASH_VERTICAL = 0;
 
 	public static final int MODE_SASH_HORIZONTAL = 1;
 
 	public static final int MODE_DESIGNER = 2;
 
 	public static final int MODE_SOURCE = 3;
 
 	private Logger _log = PDPlugin.getLogger(HTMLEditor.class);
 
 	private boolean _sash = true;
 
 	private int _mode = 0;
 
 	private SashEditorPart _sashEditorPart = null;
 
 	private int _previewPageIndex;
 
 	/** The design viewer */
 	private SimpleGraphicalEditor _designViewer;
 
 	/** The text editor. */
 	private StructuredTextEditor _textEditor;
 
 	private PartActivationHandler _partListener;
 
 	private PaletteViewerPage _paletteViewerPage;
 
 	private DefaultEditDomain _editDomain;
 
 	private WindowsIEBrowser _browser;
 
 	private List PREVIEW_FILES_LIST = new ArrayList();
 
     // TODO:This class is never used locally
 //	private class TextInputListener implements ITextInputListener {
 //		public void inputDocumentAboutToBeChanged(IDocument oldInput,
 //				IDocument newInput) {
 //            // do nothing
 //		}
 //
 //		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
 //			if (_designViewer != null && newInput != null)
 //				_designViewer.setModel(getModel());
 //		}
 //	}
 
 	public HTMLEditor() {
 		super();
 	}
 
 	/*
 	 * This method is just to make firePropertyChanged accessbible from some
 	 * (anonomous) inner classes.
 	 */
 	protected void _firePropertyChange(int property) {
 		super.firePropertyChange(property);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.xtools.common.ui.properties.ITabbedPropertySheetPageContributor#getContributorId()
 	 */
 	public String getContributorId() {
 		return CONTRIBUTOR_ID;
 	}
 
 	private void connectSashPage() {
 		ISelectionProvider selectionProvider = _sashEditorPart.getSite()
 				.getSelectionProvider();
 		if (selectionProvider instanceof IPostSelectionProvider) {
 			((IPostSelectionProvider) selectionProvider)
 					.addPostSelectionChangedListener(new ISelectionChangedListener() {
 						public void selectionChanged(SelectionChangedEvent event) {
 							((PostMultiPageSelectionProvider) getSite()
 									.getSelectionProvider())
 									.firePostSelectionChanged(event);
 						}
 					});
 		} else {
 			selectionProvider
 					.addSelectionChangedListener(new ISelectionChangedListener() {
 						public void selectionChanged(SelectionChangedEvent event) {
 							((MultiPageSelectionProvider) getSite()
 									.getSelectionProvider())
 									.fireSelectionChanged(event);
 						}
 					});
 		}
 	}
 
 	/**
 	 * Creates the source page of the multi-page editor.
 	 */
 	protected void sash_createAndAddDesignSourcePage() throws PartInitException {
 		// create source page
 		_textEditor = createTextEditor();
 		_textEditor.setEditorPart(this);
 		_textEditor.addPropertyListener(this);
 		// create design page
 		_designViewer = new SimpleGraphicalEditor(this, getEditDomain());
 
 		// create SashEditor
 		_sashEditorPart = new SashEditorPart() {
 			protected void createPages() throws PartInitException {
 				addPage(_designViewer, getEditorInput());
 				addPage(_textEditor, getEditorInput());
 			}
 		};
 		int sashIndex = addPage(_sashEditorPart, getEditorInput());
 
 		setPageText(sashIndex, PDPlugin.getResourceString("HTMLEditor.Design"));
 
 		// the update's critical, to get viewer selection manager and
 		// highlighting to work
 		_textEditor.update();
 
 		firePropertyChange(PROP_TITLE);
 
 		// Changes to the Text Viewer's document instance should also force an
 		// input refresh
 		// _textEditor.getTextViewer().addTextInputListener(new
 		// TextInputListener());
 		connectSashPage();
 	}
 
 	/**
 	 * @see org.eclipse.ui.part.MultiPageEditorPart#createSite(org.eclipse.ui.IEditorPart)
 	 */
 	protected IEditorSite createSite(IEditorPart editor) {
 		return new PostMultiPageEditorSite(this, editor);
 	}
 
 	protected void tabbed_createAndAddDesignSourcePage()
 			throws PartInitException {
 		// create source page
 		_textEditor = createTextEditor();
 		_textEditor.setEditorPart(this);
 		_textEditor.addPropertyListener(this);
 
 		// create design page
 		SimpleGraphicalEditor editor = new SimpleGraphicalEditor(this,
 				getEditDomain());
 
 		// add design page
 		int designPageIndex = addPage(editor, null);
 
 		_designViewer = editor;
 		// // note: By adding the design page as a Control instead of an
 		// // IEditorPart, page switches will indicate
 		// // a "null" active editor when the design page is made active
 		setPageText(designPageIndex, PDPlugin
 				.getResourceString("HTMLEditor.Design"));
 
 		// add source page
 		int sourcePageIndex = addPage(_textEditor, getEditorInput());
 		setPageText(sourcePageIndex, PDPlugin
 				.getResourceString("HTMLEditor.Source"));
 		// the update's critical, to get viewer selection manager and
 		// highlighting to work
 		_textEditor.update();
 
 		firePropertyChange(PROP_TITLE);
 
 		// Changes to the Text Viewer's document instance should also force an
 		// input refresh
 		// _textEditor.getTextViewer().addTextInputListener(new
 		// TextInputListener());
 	}
 
 	protected void createAndAddPreviewPage() {
 		Composite composite = new Composite(getContainer(), 0);
 		FillLayout filllayout = new FillLayout();
 		composite.setLayout(filllayout);
 		_browser = new WindowsIEBrowser();
 		if (_browser != null) {
 			_browser.create(composite, SWT.NONE);
 			_previewPageIndex = addPage(composite);
 			// JSPSourceEditor.Page.Preview.PageText=Preview
 			setPageText(_previewPageIndex, PageDesignerResources.getInstance()
 					.getString("JSPSourceEditor.Page.Preview.PageText")); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Connects the design viewer with the viewer selection manager. Should be
 	 * done after createSourcePage() is done because we need to get the
 	 * ViewerSelectionManager from the TextEditor. setModel is also done here
 	 * because getModel() needs to reference the TextEditor.
 	 */
 	protected void connectDesignPage() {
 		if (_designViewer != null) {
 			_designViewer.setModel(getModel());
 			// _designViewer.getSynchronizer().listenToModel(getModel());
 			ISelectionProvider designSelectionProvider = _designViewer
 					.getSite().getSelectionProvider();
 			if (designSelectionProvider instanceof IPostSelectionProvider) {
 				((IPostSelectionProvider) designSelectionProvider)
 						.addPostSelectionChangedListener(new ISelectionChangedListener() {
 							public void selectionChanged(
 									SelectionChangedEvent event) {
 								if (getActiveEditor() != _textEditor) {
 									_designViewer.getSynchronizer()
 											.selectionChanged(event);
 								}
 							}
 						});
 			} else {
 				designSelectionProvider
 						.addSelectionChangedListener(new ISelectionChangedListener() {
 							public void selectionChanged(
 									SelectionChangedEvent event) {
 								if (getActiveEditor() != _textEditor) {
 									_designViewer.getSynchronizer()
 											.selectionChanged(event);
 								}
 							}
 						});
 			}
 			ISelectionProvider textSelectionProvider = _textEditor.getSite()
 					.getSelectionProvider();
 			if (textSelectionProvider instanceof IPostSelectionProvider) {
 				((IPostSelectionProvider) textSelectionProvider)
 						.addPostSelectionChangedListener(new ISelectionChangedListener() {
 							public void selectionChanged(
 									SelectionChangedEvent event) {
 								if (event.getSelection() instanceof TextSelection) {
 									TextSelection textSelection = ((TextSelection) event
 											.getSelection());
 									_designViewer
 											.getSynchronizer()
 											.textSelectionChanged(
 													textSelection.getOffset(),
 													textSelection.getOffset()
 															+ textSelection
 																	.getLength());
 								}
 							}
 						});
 			} else {
 				textSelectionProvider
 						.addSelectionChangedListener(new ISelectionChangedListener() {
 							public void selectionChanged(
 									SelectionChangedEvent event) {
 								TextSelection textSelection = ((TextSelection) event
 										.getSelection());
 								_designViewer.getSynchronizer()
 										.textSelectionChanged(
 												textSelection.getOffset(),
 												textSelection.getOffset()
 														+ textSelection
 																.getLength());
 							}
 						});
 			}
 		}
 	}
 
 	/**
 	 * Creates the pages of this multi-page editor.
 	 * <p>
 	 * Subclasses of <code>MultiPageEditor</code> must implement this method.
 	 * </p>
 	 */
 	protected void createPages() {
 		try {
 			// source page MUST be created before design page, now
 			if (_sash) {
 				sash_createAndAddDesignSourcePage();
 			} else {
 				tabbed_createAndAddDesignSourcePage();
 			}
 			connectDesignPage();
 			createAndAddPreviewPage();
 			DesignerSourceMouseTrackAdapter adapter = new DesignerSourceMouseTrackAdapter(
 					_textEditor, getEditDomain());
 			_textEditor.getTextViewer().getTextWidget().addMouseListener(
 					adapter);
 			_textEditor.getTextViewer().getTextWidget().addMouseMoveListener(
 					adapter);
 		} catch (PartInitException exception) {
 			//$NON-NLS-1$ = "An error has occurred when initializing the input for the the editor's source page."
 			if (_log != null) {
 				// throw new SourceEditingRuntimeException(
 				// "An error has occurred when initializing the input for the
 				// the editor's source page.");
 			}
 		}
 		// TODO: add a catch block here for any exception the design
 		// page throws and convert it into a more informative message.
 	}
 
 	/**
 	 * Method createTextEditor.
 	 * 
 	 * @return StructuredTextEditor
 	 */
 	protected StructuredTextEditor createTextEditor() {
 		return new DesignerStructuredTextEditorJSP();
 	}
 
 	protected void disconnectDesignPage() {
 		if (_designViewer != null) {
 			_designViewer.setModel(null);
 		}
 	}
 
 	public void dispose() {
 
 		deletePreviewFiles();
 
 		disconnectDesignPage();
 
 		IWorkbenchWindow window = getSite().getWorkbenchWindow();
 		window.getPartService().removePartListener(_partListener);
 		window.getShell().removeShellListener(_partListener);
 		getSite().getPage().removePartListener(_partListener);
 
 		if (_textEditor != null) {
 			_textEditor.removePropertyListener(this);
 		}
 
 		// moved to last when added window ... seems like
 		// we'd be in danger of losing some data, like site,
 		// or something.
 		super.dispose();
 		
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 		_textEditor.doSave(monitor);
 	}
 
 	/*
 	 * (non-Javadoc) Saves the contents of this editor to another object. <p>
 	 * Subclasses must override this method to implement the open-save-close
 	 * lifecycle for an editor. For greater details, see <code> IEditorPart
 	 * </code></p>
 	 * 
 	 * @see IEditorPart
 	 */
 	public void doSaveAs() {
 		_textEditor.doSaveAs();
 	}
 
 	private void editorInputIsAcceptable(IEditorInput input)
 			throws PartInitException {
 		if (input instanceof IFileEditorInput) {
 			// verify that it can be opened
 			CoreException[] coreExceptionArray = new CoreException[1];
 			if (fileDoesNotExist((IFileEditorInput) input, coreExceptionArray)) {
 				// todo use message formatter for {0}
 				Throwable coreException = coreExceptionArray[0];
 				// FIXME: WTF?
 				if (coreException instanceof ResourceException) {
 					// I'm assuming this is always 'does not exist'
 					// we'll refresh local go mimic behavior of default
 					// editor, where the
 					// troublesome file is refreshed (and will cause it to
 					// 'disappear' from Navigator.
 					try {
 						((IFileEditorInput) input).getFile()
 								.refreshLocal(IResource.DEPTH_ZERO,
 										new NullProgressMonitor());
 					} catch (CoreException ce) {
 						if (_log != null) {
 							_log.error("Error.HTMLEditor.0", ce); //$NON-NLS-1$
 						}
 					}
 					throw new PartInitException("Resource " + input.getName()
 							+ " does not exist.");
 				}
                 throw new PartInitException("Editor could not be open on "
                 		+ input.getName());
 			}
 		} else if (input instanceof IStorageEditorInput) {
 			InputStream contents = null;
 			try {
 				contents = ((IStorageEditorInput) input).getStorage()
 						.getContents();
 			} catch (CoreException noStorageExc) {
 				// Error in geting storage contents
 				_log.error("Error.HTMLEditor.1", noStorageExc); //$NON-NLS-1$
 			}
 			if (contents == null) {
 				// throw new
 				// PartInitException(ResourceHandler.getString("32concat_EXC_",
 				// (new Object[] {
 				// input.getName()}))); //$NON-NLS-1$
 				throw new PartInitException("Editor could not be open on "
 						+ input.getName());
 			}
             ResourceUtils.ensureClosed(contents);
 		}
 	}
 
 	/*
 	 * (non-Javadoc) Initializes the editor part with a site and input. <p>
 	 * Subclasses of <code> EditorPart </code> must implement this method.
 	 * Within the implementation subclasses should verify that the input type is
 	 * acceptable and then save the site and input. Here is sample code: </p><pre>
 	 * if (!(input instanceof IFileEditorInput)) throw new
 	 * PartInitException("Invalid Input: Must be IFileEditorInput");
 	 * setSite(site); setInput(editorInput); </pre>
 	 */
 	protected boolean fileDoesNotExist(IFileEditorInput input,
 			Throwable[] coreException) {
 		boolean result = false;
 		InputStream inStream = null;
 		if ((!(input.exists())) || (!(input.getFile().exists()))) {
 			result = true;
 		} else {
 			try {
 				inStream = input.getFile().getContents(true);
 			} catch (CoreException e) {
 				// very likely to be file not found
 				result = true;
 				coreException[0] = e;
 				// The core has exception
 				_log.error("Error.HTMLEditor.3", e); //$NON-NLS-1$
 			} finally {
 				if (input != null) {
 					ResourceUtils.ensureClosed(inStream);
 				}
 			}
 		}
 		return result;
 	}
 
 	public Object getAdapter(Class key) {
 		Object result = null;
 		if (key == IDesignViewer.class) {
 			result = _designViewer;
 		} else if (key == PalettePage.class) {
 			return getPaletteViewerPage();
 		} else if (key == IPropertySheetPage.class) {
 			// XXX: we can delegate this to the fTextEditor, but that use some
 			// more
 			// complicate mechanism, and don't work with page designer well, so
 			// do it simple now, fix later.
 			// return _textEditor.getAdapter(key);
 			return getPropertySheetPage();
 		} else if (key == IContentOutlinePage.class) {
 			if (_textEditor != null) {
 				result = _textEditor.getAdapter(key);
 			}
 		} else if (key == IPageVariablesProvider.class) {
 			Object obj = ((IDOMModel) getModel()).getDocument().getAdapterFor(
 					IDocumentPageVariableAdapter.class);
 			if (obj instanceof IPageVariablesProvider) {
 				return obj;
 			}
             return null;
 		} else {
 			// DMW: I'm bullet-proofing this because
 			// its been reported (on 4.03 version) a null pointer sometimes
 			// happens here on startup, when an editor has been left
 			// open when workbench shutdown.
 			if (_textEditor != null) {
 				result = _textEditor.getAdapter(key);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * IExtendedMarkupEditor method XXX:No reference to the method.
 	 */
 	public Node getCaretNode() {
 		if (getTextEditor() == null) {
 			return null;
 		}
 
 		ISourceEditingTextTools adapter = (ISourceEditingTextTools) _textEditor
 				.getAdapter(ISourceEditingTextTools.class);
 		if (adapter instanceof IDOMSourceEditingTextTools) {
 			IDOMSourceEditingTextTools domTools = (IDOMSourceEditingTextTools) adapter;
 			try {
 				return domTools.getNode(domTools.getCaretOffset());
 			} catch (BadLocationException e) {
 				_log.error("BadLocationException", e);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * IExtendedSimpleEditor method
 	 */
 	public int getCaretPosition() {
 		if (getTextEditor() == null) {
 			return -1;
 		}
 
 		// return getExtendedTextEditor().getCaretPosition();
 		Object apapter = _textEditor.getAdapter(ISourceEditingTextTools.class);
 		if (apapter != null) {
 			return ((ISourceEditingTextTools) apapter).getCaretOffset();
 		}
 		return -1;
 	}
 
 	/**
 	 * IExtendedSimpleEditor method
 	 */
 	public IDocument getDocument() {
 		if (getTextEditor() == null) {
 			return null;
 		}
 
 		Object apapter = _textEditor.getAdapter(ISourceEditingTextTools.class);
 		if (apapter != null) {
 			return ((ISourceEditingTextTools) apapter).getDocument();
 		}
 
 		return null;
 	}
 
 	/**
 	 * IExtendedMarkupEditor method
 	 */
 	public Document getDOMDocument() {
 		if (getTextEditor() == null) {
 			return null;
 		}
 
 		Object adapter = _textEditor.getAdapter(ISourceEditingTextTools.class);
 		if (adapter instanceof IDOMSourceEditingTextTools) {
 			return ((IDOMSourceEditingTextTools) adapter).getDOMDocument();
 		}
 		return null;
 	}
 
 	/**
 	 * IExtendedSimpleEditor method
 	 */
 	public IEditorPart getEditorPart() {
 		return this;
 	}
 
 	public IStructuredModel getModel() {
 		IStructuredModel model = null;
 		if (_textEditor != null) {
 			model = ((DesignerStructuredTextEditorJSP) _textEditor).getModel();
 		}
 		return model;
 	}
 
 	/**
 	 * IExtendedMarkupEditor method
 	 */
 	public List getSelectedNodes() {
 		if (getTextEditor() == null) {
 			return null;
 		}
 		// FIXME: when designer as current view, what to return?
 		return getExtendedTextEditor().getSelectedNodes();
 	}
 
 	public StructuredTextEditor getTextEditor() {
 		return _textEditor;
 	}
 
 	public IExtendedMarkupEditor getExtendedTextEditor() {
 		if (_textEditor instanceof IExtendedMarkupEditor) {
 			return (IExtendedMarkupEditor) _textEditor;
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IWorkbenchPart.
 	 */
 	public String getTitle() {
 		String title = null;
 		if (getTextEditor() == null) {
 			if (getEditorInput() != null) {
 				title = getEditorInput().getName();
 			}
 		} else {
 			title = getTextEditor().getTitle();
 		}
 		if (title == null) {
 			title = getPartName();
 		}
 		return title;
 	}
 
 	/*
 	 * (non-Javadoc) Sets the cursor and selection state for this editor to the
 	 * passage defined by the given marker. <p> Subclasses may override. For
 	 * greater details, see <code> IEditorPart </code></p>
 	 * 
 	 * @see IEditorPart
 	 */
 	public void gotoMarker(IMarker marker) {
 		// (pa) 20020217 this was null when opening an editor that was
 		// already open
 		if (_textEditor != null) {
 			IGotoMarker markerGotoer = (IGotoMarker) _textEditor
 					.getAdapter(IGotoMarker.class);
 			markerGotoer.gotoMarker(marker);
 		}
 	}
 
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		super.init(site, input);
 		editorInputIsAcceptable(input);
 		try {
 			// super.init(site, input);
 			// setSite(site);
 			setInput(input);
 			if (_partListener == null) {
 				_partListener = new PartActivationHandler(this) {
 					public void handleActivation() {
 						safelySanityCheckState();
 					}
 				};
 			}
 			// we want to listen for our own activation
 			IWorkbenchWindow window = getSite().getWorkbenchWindow();
 			window.getPartService().addPartListener(_partListener);
 			window.getShell().addShellListener(_partListener);
			
			// TODO: is this the right place to do this?
			// enable our editor context
			IContextService contextService = (IContextService) getSite()
			  .getService(IContextService.class);
			contextService.activateContext("org.eclipse.jst.pagedesigner.editorContext");

 		} catch (Exception e) {
 			// Error in editor initialization
 			_log.error("Error.HTMLEditor.5", e); //$NON-NLS-1$
 		}
 		setPartName(input.getName());
 	}
 
 	/*
 	 * (non-Javadoc) Returns whether the "save as" operation is supported by
 	 * this editor. <p> Subclasses must override this method to implement the
 	 * open-save-close lifecycle for an editor. For greater details, see <code>
 	 * IEditorPart </code></p>
 	 * 
 	 * @see IEditorPart
 	 */
 	public boolean isSaveAsAllowed() {
 		return _textEditor != null && _textEditor.isSaveAsAllowed();
 	}
 
 	/*
 	 * (non-Javadoc) Returns whether the contents of this editor should be saved
 	 * when the editor is closed. <p> This method returns <code> true </code> if
 	 * and only if the editor is dirty ( <code> isDirty </code> ). </p>
 	 */
 	public boolean isSaveOnCloseNeeded() {
 		// overriding super class since it does a lowly isDirty!
 		if (_textEditor != null) {
 			return _textEditor.isSaveOnCloseNeeded();
 		}
 		return isDirty();
 	}
 
 	/**
 	 * Posts the update code "behind" the running operation.
 	 */
 	protected void postOnDisplayQue(Runnable runnable) {
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
 		if (windows != null && windows.length > 0) {
 			Display display = windows[0].getShell().getDisplay();
 			display.asyncExec(runnable);
 		} else {
 			runnable.run();
 		}
 	}
 
 	/**
 	 * Indicates that a property has changed.
 	 * 
 	 * @param source
 	 *            the object whose property has changed
 	 * @param propId
 	 *            the id of the property which has changed; property ids are
 	 *            generally defined as constants on the source class
 	 */
 	public void propertyChanged(Object source, int propId) {
 		switch (propId) {
 		// had to implement input changed "listener" so that
 		// strucutedText could tell it containing editor that
 		// the input has change, when a 'resource moved' event is
 		// found.
 		case IEditorPart.PROP_INPUT: {
 			if (source == _textEditor) {
 				if (_textEditor.getEditorInput() != getEditorInput()) {
 					setInput(_textEditor.getEditorInput());
 					// title should always change when input changes.
 					// create runnable for following post call
 					Runnable runnable = new Runnable() {
 						public void run() {
 							_firePropertyChange(IWorkbenchPart.PROP_TITLE);
 						}
 					};
 					// Update is just to post things on the display queue
 					// (thread). We have to do this to get the dirty
 					// property to get updated after other things on the
 					// queue are executed.
 					postOnDisplayQue(runnable);
 				}
 			}
 			break;
 		}
 		case IWorkbenchPart.PROP_TITLE: {
 			// // update the input if the title is changed. why? It seems input
 			// change event will be fired at last.
 			// if (source == _textEditor)
 			// {
 			// if (_textEditor.getEditorInput() != getEditorInput())
 			// {
 			// setInput(_textEditor.getEditorInput());
 			// }
 			// }
 			// break;
 		}
 		default: {
 			// propagate changes. Is this needed? Answer: Yes.
 			// PROP_PART_NAME, PROP_DIRTY etc.
 			if (source == _textEditor) {
 				firePropertyChange(propId);
 			}
 			break;
 		}
 		}
 
 	}
 
 	protected void safelySanityCheckState() {
 		// If we're called before editor is created, simply ignore since we
 		// delegate this function to our embedded TextEditor
 		if (getTextEditor() == null) {
 			return;
 		}
 
 		getTextEditor().safelySanityCheckState(getEditorInput());
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
 	 */
 	protected void setInput(IEditorInput input) {
 		// If driven from the Source page, it's "model" may not be up to date
 		// with the input just yet. We'll rely on later notification from the
 		// TextViewer to set us straight
 		super.setInput(input);
 		if (_designViewer != null) {
 
 			_designViewer.setModel(getModel());
 		}
 		setPartName(input.getName());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.part.EditorPart#isDirty()
 	 */
 	public boolean isDirty() {
 		if (getTextEditor() == null) {
 			return false;
 		}
 		return getTextEditor().isDirty();
 	}
 
 	protected IPropertySheetPage getPropertySheetPage() {
 		return new org.eclipse.jst.pagedesigner.properties.DesignerTabbedPropertySheetPage(
 				this, this);
 	}
 
 	protected PaletteViewerPage getPaletteViewerPage() {
 		if (null == _paletteViewerPage) {
 			DefaultEditDomain editDomain = getEditDomain();
 			PaletteItemManager manager = PaletteItemManager
 					.getInstance(getCurrentProject(getEditorInput()));
 			manager.reset();
             PaletteRoot paletteRoot = _designViewer.getPaletteRoot();
             editDomain.setPaletteRoot(paletteRoot);
             
             _paletteViewerPage = (PaletteViewerPage) _designViewer.getAdapter(PalettePage.class);
             // if possible, try to use the 
             if (_paletteViewerPage == null)
             {
                 PaletteViewerProvider provider = _designViewer.getPaletteViewerProvider2();
                 _paletteViewerPage = new PaletteViewerPage(provider);
             }
 		}
 		return _paletteViewerPage;
 	}
 
 	/**
 	 * @return
 	 */
 	public DefaultEditDomain getEditDomain() {
 		if (_editDomain == null) {
 			_editDomain = new DefaultEditDomain(this);
 
 			// XXX: if i don't do the following line, system will default use
 			// SelectionTool. Don't know where else to set this. Since it is
 			// kind of duplicate
 			// to the DesignerPaletteRoot.
 			_editDomain.setDefaultTool(new RangeSelectionTool());
 			_editDomain.loadDefaultTool();
 
 			// next config the _editDomain
 			// _editDomain.setPaletteRoot(new JSFPaletteRoot());
 		}
 		return _editDomain;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
 	 */
 	protected void pageChange(int newPageIndex) {
 		super.pageChange(newPageIndex);
 
 		deletePreviewFiles();
 
 		if (newPageIndex == _previewPageIndex) {
 			// preview page activate, need to regenerate the preview text and
 			// display it.
 			StringBuffer result = new StringBuffer();
 			try {
 				// PreviewHandler.generatePreview(this.getModel(),
 				// this.getEditorInput(), result);
 				DocumentEditPart part = (DocumentEditPart) this._designViewer
 						.getGraphicViewer().getContents();
 				PreviewHandlerNew.generatePreview(part, result);
 			} catch (Exception ex) {
 				result = new StringBuffer();
 				result
 						.append(this.getModel().getStructuredDocument()
 								.getText());
 				// Error in page changing
 				_log.info("Error.HTMLEditor.6", ex); //$NON-NLS-1$
 				ex.printStackTrace();
 			}
 			File file = PreviewUtil.toFile(result, getEditorInput());
 			if (file != null) {
 				PREVIEW_FILES_LIST.add(file);
 				_browser.loadFile(file);
 			} else {
 				_browser.getBrowser().setUrl("about:blank"); //$NON-NLS-1$
 			}
 		}
 	}
 
 	/**
 	 * @return Returns the _designViewer.
 	 */
 	public IDesignViewer getDesignViewer() {
 		return _designViewer;
 	}
 
 	/**
 	 * @param mode
 	 */
 	public void setDesignerMode(int mode) {
 		if (_sashEditorPart != null) {
 			switch (mode) {
 			case MODE_SASH_HORIZONTAL:
 				_sashEditorPart.setOrientation(SWT.HORIZONTAL);
 				break;
 			case MODE_DESIGNER:
 				_sashEditorPart.setMaximizedEditor(this._designViewer);
 				break;
 			case MODE_SOURCE:
 				_sashEditorPart.setMaximizedEditor(this._textEditor);
 				break;
 			case MODE_SASH_VERTICAL:
 			default:
 				_sashEditorPart.setOrientation(SWT.VERTICAL);
 			}
 		}
 		this._mode = mode;
 	}
 
 	public int getDesignerMode() {
 		return this._mode;
 	}
 
 	private IProject getCurrentProject(IEditorInput input) {
 		IProject curProject = null;
 		IFile inputFile = null;
 		if (input instanceof IFileEditorInput) {
 			inputFile = ((IFileEditorInput) input).getFile();
 			curProject = inputFile.getProject();
 		}
 		return curProject;
 	}
 
 	public IEditorPart getActiveEditor() {
 		IEditorPart result = null;
 		if (_sash) {
 			result = _sashEditorPart.getActiveEditor();
 		} else {
 			if (_designViewer.getGraphicViewer().getControl().isFocusControl()) {
 				result = _designViewer;
 			} else if (_textEditor.getTextViewer().getControl()
 					.isFocusControl()) {
 				result = _textEditor;
 			}
 		}
 		return result;
 	}
 
 	public String getPartName() {
 		if (_textEditor != null) {
 			return _textEditor.getPartName();
 		}
         return super.getPartName();
 	}
 
 	private void deletePreviewFiles() {
 		Iterator itPreviewFiles = PREVIEW_FILES_LIST.iterator();
 		while (itPreviewFiles.hasNext()) {
 			File file = (File)itPreviewFiles.next();
 			if (file != null && file.exists()) {
 				file.delete();
 			}
 		}
 		PREVIEW_FILES_LIST.clear();
 	}
 
 }
