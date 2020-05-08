 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.jspeditor;
 
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.gef.ui.views.palette.PalettePage;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.IPostSelectionProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorActionBarContributor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.INavigationLocation;
 import org.eclipse.ui.INavigationLocationProvider;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.contexts.IContextService;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.IStatusField;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.ui.texteditor.ITextEditorExtension;
 import org.eclipse.ui.texteditor.ITextEditorExtension2;
 import org.eclipse.ui.texteditor.ITextEditorExtension3;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.sse.ui.internal.contentoutline.ConfigurableContentOutlinePage;
 import org.jboss.tools.common.core.resources.XModelObjectEditorInput;
 import org.jboss.tools.common.model.XModelException;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.event.XModelTreeEvent;
 import org.jboss.tools.common.model.event.XModelTreeListener;
 import org.jboss.tools.common.model.filesystems.impl.DiscardFileHandler;
 import org.jboss.tools.common.model.filesystems.impl.FolderImpl;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.model.ui.editor.EditorDescriptor;
 import org.jboss.tools.common.model.ui.editor.IModelObjectEditorInput;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.model.util.XModelTreeListenerSWTASync;
 import org.jboss.tools.common.text.ext.IMultiPageEditor;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.bundle.BundleMap;
 import org.jboss.tools.jst.jsp.editor.IVisualEditor;
 import org.jboss.tools.jst.jsp.editor.IVisualEditorFactory;
 import org.jboss.tools.jst.jsp.preferences.IVpePreferencesPage;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.jboss.tools.jst.web.tld.VpeTaglibManagerProvider;
 
 // Fix for EXIN-232: The IMultiPageEditor interface implementation is added.
 public class JSPMultiPageEditor extends JSPMultiPageEditorPart implements
 		XModelTreeListener, ITextEditor, IGotoMarker, VpeTaglibManagerProvider,
 		IReusableEditor, ITextEditorExtension, ITextEditorExtension2,
 		ITextEditorExtension3, INavigationLocationProvider, IMultiPageEditor {
 
 	public static final String EDITOR_ID = "org.jboss.tools.jst.jsp.jspeditor.JSPTextEditor"; //$NON-NLS-1$
 
 	private static final String VPE_VISUAL_EDITOR_IMPL_ID = "org.jboss.tools.vpe.org.jboss.tools.vpe.editor.VpeEditorPartFactory"; //$NON-NLS-1$
 
 	private static final String VISUAL_EDITOR_IMPL_EXTENSION_POINT_NAME = "visulaEditorImplementations"; //$NON-NLS-1$
 
 	private IVisualEditor visualEditor;
 
 	private int visualSourceIndex;
 	
 	private JSPTextEditor sourceEditor;
 
 	private int sourceIndex;
 
 	/** composite control for default web-browser */
 	// private IVisualEditor previewWebBrowser;
 
 	/** index of tab contain default web-browser */
 	private int previewIndex=-1;//by default 1, so if there no visual editor impl, this tab will be not available
 
 	// private boolean osWindows = true;
 	
 	private BundleMap bundleMap;
 
 	protected XModelTreeListenerSWTASync syncListener = new XModelTreeListenerSWTASync(
 			this);
 
 	// private int oldPage = -1;
 
 	private ConfigurableContentOutlinePage outlinePage = null;
 
 	private XModelObject object;
 
 	private QualifiedName persistentTabQualifiedName = new QualifiedName("", //$NON-NLS-1$
 			"Selected_tab"); //$NON-NLS-1$
 
 	int selectedPageIndex = 0;
 
 	static IVisualEditorFactory visualEditorFactory;
 	
 	//added by Maksim Areshkau, notified externalize command that selection changed
 	private ISelectionChangedListener externalizeSelectionChangeListener = new ISelectionChangedListener() {
 		
 		public void selectionChanged(SelectionChangedEvent event) {
 			ICommandService commandService = (ICommandService) PlatformUI
 			.getWorkbench().getService(ICommandService.class);
 			commandService.refreshElements("org.jboss.tools.jst.jsp.commands.i18", null); //$NON-NLS-1$
 		}
 	};
 
 	static {
 		// Fix For JBIDE-2674
 		try {
 			IExtension visualEditorExtension = Platform.getExtensionRegistry()
 					.getExtension(JspEditorPlugin.PLUGIN_ID,
 							VISUAL_EDITOR_IMPL_EXTENSION_POINT_NAME,
 							VPE_VISUAL_EDITOR_IMPL_ID);
 			if (visualEditorExtension != null) {
 				IConfigurationElement[] configurationElements = visualEditorExtension
 						.getConfigurationElements();
 				if (configurationElements != null
 						&& configurationElements.length == 1) {
 					visualEditorFactory = (IVisualEditorFactory) configurationElements[0]
 							.createExecutableExtension("class"); //$NON-NLS-1$
 				} else {
 					JspEditorPlugin
 							.getPluginLog()
 							.logError(
 									"Visual Editor Extension Point not configured correctly"); //$NON-NLS-1$
 				}
 			} else {
 				JspEditorPlugin.getPluginLog().logError(
 						"Visual Editor Implementation not available"); //$NON-NLS-1$
 			}
 		} catch (CoreException e) {
 			JspEditorPlugin.getPluginLog().logError(
 					"Visual Editor Implementation not available" + e); //$NON-NLS-1$
 		}
 	}
 
 	private void loadSelectedTab() {
 		String defaultVpeTab = JspEditorPlugin.getDefault()
 				.getPreferenceStore().getString(
 						IVpePreferencesPage.DEFAULT_VPE_TAB);
 		if (IVpePreferencesPage.DEFAULT_VPE_TAB_VISUAL_SOURCE_VALUE
 				.equalsIgnoreCase(defaultVpeTab)) {
 			selectedPageIndex = 0;
 		} else if (IVpePreferencesPage.DEFAULT_VPE_TAB_SOURCE_VALUE
 				.equalsIgnoreCase(defaultVpeTab)) {
 			selectedPageIndex = 1;
 		} else if (IVpePreferencesPage.DEFAULT_VPE_TAB_PREVIEW_VALUE
 				.equalsIgnoreCase(defaultVpeTab)) {
 			selectedPageIndex = 2;
 		} else {
 			selectedPageIndex = 0;
 		}
 		//when implementation of visual editor not available we have only one tab
 		if(visualEditor==null){
 			selectedPageIndex=0;
 		}
 	}
 
 	public void superPageChange(int newPageIndex) {
 		Control control = getControl(visualSourceIndex);
 		if (control != null) {
 			control.setVisible(true);
 		}
 		setFocus();
 		IEditorPart activeEditor = getEditor(visualSourceIndex);
 		IEditorActionBarContributor contributor = getEditorSite()
 				.getActionBarContributor();
 		if (contributor != null
 				&& contributor instanceof MultiPageEditorActionBarContributor) {
 			((MultiPageEditorActionBarContributor) contributor)
 					.setActivePage(activeEditor);
 		}
 		if (activeEditor != null) {
 			ISelectionProvider selectionProvider = activeEditor.getSite()
 					.getSelectionProvider();
 			if (selectionProvider != null) {
 				SelectionChangedEvent event = new SelectionChangedEvent(
 						selectionProvider, selectionProvider.getSelection());
 				((JSPMultiPageSelectionProvider) getSite()
 						.getSelectionProvider()).fireSelectionChanged(event);
 			}
 		}
 	}
 
 	@Override
 	public void pageChange(int newPageIndex) {
 		selectedPageIndex = newPageIndex;
 		if (visualEditor != null) {
 			if (newPageIndex == visualSourceIndex) {
 				if (visualEditor.getVisualEditor() == null) {
 					visualEditor.createVisualEditor();
 				}
 				visualEditor.setVisualMode(IVisualEditor.VISUALSOURCE_MODE);
 			} else if (newPageIndex == sourceIndex)
 				visualEditor.setVisualMode(IVisualEditor.SOURCE_MODE);
 			else if (newPageIndex == getPreviewIndex()) {
 				if (visualEditor.getPreviewWebBrowser() == null) {
 					visualEditor.createPreviewBrowser();
 				}
 				visualEditor.setVisualMode(IVisualEditor.PREVIEW_MODE);
 			}
 		}
 		
 		ICommandService commandService = (ICommandService) PlatformUI
 		.getWorkbench().getService(ICommandService.class);
 		commandService.refreshElements("org.jboss.tools.jst.jsp.commands.showSelectionBar", null); //$NON-NLS-1$
 
 		
 		superPageChange(newPageIndex);
 		JspEditorPlugin.getDefault().getPreferenceStore().
 			setValue(IVpePreferencesPage.DEFAULT_VPE_TAB, selectedPageIndex);
 	}
 
 	public int getSelectedPageIndex() {
 		return selectedPageIndex;
 	}
 
 	public void setInput(IEditorInput input) {
 		super.setInput(XModelObjectEditorInput.checkInput(input));
 		if (getEditorInput() instanceof IModelObjectEditorInput) {
 			object = ((IModelObjectEditorInput) getEditorInput())
 					.getXModelObject();
 		}
 		if (sourceEditor != null
 				&& sourceEditor.getEditorInput() != getEditorInput()
 				&& sourceEditor.getEditorInput() != null) {
 			if (sourceEditor instanceof AbstractTextEditor) {
 				((AbstractTextEditor) sourceEditor).setInput(getEditorInput());
 			}
 			if (visualEditor != null) {
 				visualEditor.setInput(getEditorInput());
 			}
 			updateTitle();
 		}
 		updateFile();
 	}
 
 	public void setInput0(IEditorInput input) {
 		super.setInput(XModelObjectEditorInput.checkInput(input));
 		updateFile();
 		firePropertyChange(IEditorPart.PROP_INPUT);
 	}
 
 	private void updateFile() {
 		IFile file = getFile();
 		if (file == null)
 			return;
 		try {
 			file.refreshLocal(0, null);
 		} catch (CoreException e) {
 			JspEditorPlugin.getPluginLog().logWarning(e);
 		}
 	}
 
 	private IFile getFile() {
 		IEditorInput input = getEditorInput();
 		return (input instanceof IFileEditorInput) ? ((IFileEditorInput) input)
 				.getFile() : null;
 	}
 
 	public String getContentDescription() {
 		return ""; //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 */
 	private ISelectionProvider selectionProvider = null;
 
 	private JSPMultiPageEditorSite site;
 
 	protected IEditorSite createSite(IEditorPart editor) {
 		site = new JSPMultiPageEditorSite(this, editor) {
 			private ISelectionChangedListener postSelectionChangedListener = null;
 
 			private ISelectionChangedListener getPostSelectionChangedListener() {
 				if (postSelectionChangedListener == null) {
 					postSelectionChangedListener = new ISelectionChangedListener() {
 						public void selectionChanged(SelectionChangedEvent event) {
 							handlePostSelectionChanged(event);
 						}
 					};
 				}
 				return postSelectionChangedListener;
 			}
 
 			protected void handlePostSelectionChanged(
 					SelectionChangedEvent event) {
 				ISelectionProvider parentProvider = getMultiPageEditor()
 						.getSite().getSelectionProvider();
 				ISelection s = event.getSelection();
 				if (s == null || s.isEmpty())
 					return;
 				if (s instanceof ITextSelection) {
 					if (parentProvider instanceof JSPMultiPageSelectionProvider) {
 						((JSPMultiPageSelectionProvider) parentProvider)
 								.firePostSelectionChanged(event);
 					}
 				}
 			}
 
 			public String getId() {
 				return getSite().getId();
 			}
 
 			/**
 			 * 
 			 */
 			public ISelectionProvider getSelectionProvider() {
 				return selectionProvider;
 			}
 
 			/**
 			 * 
 			 */
 			public void setSelectionProvider(ISelectionProvider provider) {
 				ISelectionProvider oldSelectionProvider = getSelectionProvider();
 				if (oldSelectionProvider != null) {
 					if (oldSelectionProvider instanceof IPostSelectionProvider) {
 						((IPostSelectionProvider) oldSelectionProvider)
 								.removePostSelectionChangedListener(getPostSelectionChangedListener());
 					}
 				}
 
 				selectionProvider = provider;
 				if (oldSelectionProvider != null) {
 					oldSelectionProvider
 							.removeSelectionChangedListener(getSelectionChangedListener());
 				}
 				if (selectionProvider != null) {
 					selectionProvider
 							.addSelectionChangedListener(getSelectionChangedListener());
 					selectionProvider.addSelectionChangedListener(externalizeSelectionChangeListener);
 				}
 
 				if (provider != null) {
 					if (provider instanceof IPostSelectionProvider) {
 						((IPostSelectionProvider) provider)
 								.addPostSelectionChangedListener(getPostSelectionChangedListener());
 					}
 				}
 			}
 
 			public void dispose() {
 				ISelectionProvider provider = getSelectionProvider();
 				if (provider != null) {
 					provider
 							.removeSelectionChangedListener(getSelectionChangedListener());
 					provider.removeSelectionChangedListener(externalizeSelectionChangeListener);
 				}
 				if (provider instanceof IPostSelectionProvider
 						&& postSelectionChangedListener != null) {
 					((IPostSelectionProvider) provider)
 							.removePostSelectionChangedListener(postSelectionChangedListener);
 
 				}
 				postSelectionChangedListener = null;
 				super.dispose();
 			}
 		};
 		return site;
 	}
 
 	protected void createPages() {
 
 		// Sergey Vasilyev
 		/*
 		 * TODO to author of this class VPE work on linux! this check not need
 		 * in future
 		 */
 		createPagesForVPE();
 		loadSelectedTab();
 
 		// if (selectedPageIndex == sourceIndex) {
 		// visualEditor.setVisualMode(IVisualEditor.SOURCE_MODE);
 		// // switchOutlineToJSPEditor();
 		// }
 		// if (selectedPageIndex == 2) {
 		// setActivePage(0);
 		// pageChange(0);
 		// if (visualEditor != null)
 		// visualEditor.maximizeSource();
 		// selectedPageIndex=0;
 		// } else if (selectedPageIndex == 1) {
 		// setActivePage(0);
 		// pageChange(0);
 		// if (visualEditor != null)
 		// visualEditor.maximizeVisual();
 		// selectedPageIndex=0;
 		// } else {
 		// selectedPageIndex=0;
 		// setActivePage(selectedPageIndex);
 		// pageChange(selectedPageIndex);
 		// }
 		switch (selectedPageIndex) {
 
 		case 0: {
 			// source/visual mode
 			setActivePage(selectedPageIndex);
 			pageChange(selectedPageIndex);
 			break;
 		}
 		case 1: {
 			// source mode
 			setActivePage(selectedPageIndex);
 			pageChange(selectedPageIndex);
 			break;
 		}
 		case 2: {
 			// preview mode
 			setActivePage(selectedPageIndex);
 			pageChange(selectedPageIndex);
 			break;
 		}
 		default: {
 			// by default we sets source/visual mode
 			setActivePage(0);
 			pageChange(0);
 			break;
 		}
 		}
 		
 		IFile f = getFile();
 		if (f != null && f.exists()) {
 			new ResourceChangeListener(this, getContainer());
 		}
 		if (getModelObject() != null) {
 			getModelObject().getModel().addModelTreeListener(syncListener);
 		}
 	}
 
 	private void createPagesForVPE() {
 		/*
 		 * Create Source Editor and BundleMap
 		 */
 		sourceEditor = new JSPTextEditor(this);
 		/*
 		 * Create Bundle Map here but Initialize it  in the VpeController
 		 * or here if there is only the source part. 
 		 */
 		bundleMap = new BundleMap();
 		
 		if (visualEditorFactory != null) {
 			visualEditor = visualEditorFactory.createVisualEditor(this,
					sourceEditor, false, bundleMap);
 		}
 		try {
 			if (visualEditor != null) {
 				visualSourceIndex = addPage(visualEditor, getEditorInput(),sourceEditor);
 				setPageText(
 						visualSourceIndex,
 						JSPEditorMessages.JSPMultiPageEditor_TabLabel_VisualSource);
 				setPartName(visualEditor.getTitle());
 			}
 		} catch (PartInitException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		/*
 		 * try { visualIndex = addPage(visualEditor, getEditorInput());
 		 * setPageText(visualIndex, JSPEditorMessages
 		 * .getString(VISUAL_TAB_LABEL)); setPartName(visualEditor.getTitle());
 		 * } catch (PartInitException e) {
 		 * JspEditorPlugin.getPluginLog().logError(e); }
 		 */
 
 		try {
 			String sourceTabLabel = JSPEditorMessages.JSPMultiPageEditor_TabLabel_Source;
 			if (visualEditor != null) {
 				sourceIndex = addPage(visualEditor, getEditorInput(),sourceEditor);
 				setPageText(sourceIndex, sourceTabLabel);
 				setPartName(visualEditor.getTitle());
 			} else {
 				sourceIndex = addPage(sourceEditor, getEditorInput(),sourceEditor);
 				setPageText(sourceIndex, sourceTabLabel);
 				setPartName(sourceEditor.getTitle());
 			}
 		} catch (PartInitException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 
 		// Add tab contain default web-browser
 		try {
 			if (visualEditor != null) {
 				setPreviewIndex(addPage(visualEditor, getEditorInput(),sourceEditor));
 				setPageText(getPreviewIndex(),
 						JSPEditorMessages.JSPMultiPageEditor_TabLabel_Preview);
 				setPartName(visualEditor.getTitle());
 			}
 		} catch (PartInitException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		
 		/*
 		 * When there is only the source part --
 		 * then VpeController very likely hasn't been created.
 		 * Thus Bundle Map should be initialized here instead of
 		 * VpeController.
 		 */
 		if (visualEditor == null) {
 			bundleMap.init(sourceEditor.getEditorInput());
 		}
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 		sourceEditor.doSave(monitor);
 	}
 
 	class PCL implements IPropertyListener {
 		public void propertyChanged(Object source, int i) {
 			firePropertyChange(i);
 			if (i == IEditorPart.PROP_INPUT
 					&& getEditorInput() != sourceEditor.getEditorInput()) {
 				setInput(sourceEditor.getEditorInput());
 				setPartName(sourceEditor.getPartName());
 				setTitleToolTip(sourceEditor.getTitleToolTip());
 			}
 		}
 	}
 
 	public void doSaveAs() {
 		XModelObject old = getModelObject();
 		PCL pcl = new PCL();
 		sourceEditor.addPropertyListener(pcl);
 		sourceEditor.doSaveAs();
 		sourceEditor.removePropertyListener(pcl);
 		try {
 			if (old.isModified())
 				new DiscardFileHandler().executeHandler(old, new Properties());
 		} catch (XModelException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 	}
 
 	public void gotoMarker(final IMarker marker) {
 		// setActivePage(IVisualEditor.VISUALSOURCE_MODE);
 		// pageChange(IVisualEditor.VISUALSOURCE_MODE);
 		setActivePage(IVisualEditor.SOURCE_MODE);
 		pageChange(IVisualEditor.SOURCE_MODE);
 		IGotoMarker adapter = (IGotoMarker) sourceEditor
 				.getAdapter(IGotoMarker.class);
 		if (adapter != null) {
 			adapter.gotoMarker(marker);
 		}
 	}
 
 	public boolean isSaveAsAllowed() {
 		return sourceEditor.isSaveAsAllowed();
 	}
 
 	public JSPTextEditor getJspEditor() {
 		return sourceEditor;
 	}
 
 	public StructuredTextEditor getSourceEditor() {
 		return sourceEditor;
 	}
 
 	public IVisualEditor getVisualEditor() {
 		return visualEditor;
 	}
 
 	public IEditorPart getActivePageEditor() {
 		return getActiveEditor();
 	}
 
 	protected XModelObject getModelObject() {
 		return object;
 	}
 
 	public void dispose() {
 		IEditorActionBarContributor contributor = getEditorSite()
 				.getActionBarContributor();
 		if (contributor != null
 				&& contributor instanceof MultiPageEditorActionBarContributor) {
 			((MultiPageEditorActionBarContributor) contributor)
 					.setActivePage(null);
 		}
 		if (visualEditor != null) {
 			visualEditor.dispose();
 		}
 		site.dispose();
 		site = null;
 		outlinePage = null;
 		XModelObject o = getModelObject();
 		if (o != null) {
 			o.getModel().removeModelTreeListener(syncListener);
 		}
 		if (syncListener != null)
 			syncListener.dispose();
 		syncListener = null;
 		if (o != null && o.isModified() && o.isActive()) {
 			try {
 				((FolderImpl) o.getParent()).discardChildFile(o);
 			} catch (XModelException e) {
 				JspEditorPlugin.getPluginLog().logError(e);
 			}
 		}
 		if(palettePage != null) {
 			palettePage.dispose();
 			palettePage = null;
 		}
 		IContextService contextService = (IContextService) getSite()
 		  .getService(IContextService.class);
 		super.dispose();
 	}
 	
 	PalettePageImpl palettePage;
 
 	public Object getAdapter(Class adapter) {
 		if(PalettePage.class == adapter) {
 			if(palettePage == null) {
 				palettePage = new PalettePageImpl();
 			}
 			return palettePage;
 		}
 		if (IContentOutlinePage.class.equals(adapter)) {
 
 			if (visualEditor != null) {
 				if (outlinePage == null)
 					outlinePage = (ConfigurableContentOutlinePage) visualEditor
 							.getAdapter(adapter);
 				return outlinePage;
 			}
 
 		} else if (IPropertySheetPage.class.equals(adapter)) {
 			if (sourceEditor != null)
 				return sourceEditor.getAdapter(adapter);
 		} else if (adapter == EditorDescriptor.class)
 			return new EditorDescriptor(new String[] { "jsp", "html" }); //$NON-NLS-1$ //$NON-NLS-2$
 
 		if (sourceEditor != null) {
 			return sourceEditor.getAdapter(adapter);
 		}
 
 		return super.getAdapter(adapter);
 	}
 
 	public void nodeChanged(XModelTreeEvent event) {
 		if (event.getModelObject() == getModelObject()) {
 			setContentDescription(getEditorInput().getName());
 			if (sourceEditor != null)
 				sourceEditor.updateModification();
 		}
 	}
 
 	public void structureChanged(XModelTreeEvent event) {
 	}
 
 	public void close(boolean save) {
 		sourceEditor.close(save);
 	}
 
 	public void doRevertToSaved() {
 		sourceEditor.doRevertToSaved();
 	}
 
 	public IAction getAction(String actionId) {
 		return sourceEditor.getAction(actionId);
 	}
 
 	public IDocumentProvider getDocumentProvider() {
 		return sourceEditor.getDocumentProvider();
 	}
 
 	public IRegion getHighlightRange() {
 		return sourceEditor.getHighlightRange();
 	}
 
 	public ISelectionProvider getSelectionProvider() {
 		return sourceEditor.getSelectionProvider();
 	}
 
 	public boolean isEditable() {
 		return sourceEditor.isEditable();
 	}
 
 	public void removeActionActivationCode(String actionId) {
 		sourceEditor.removeActionActivationCode(actionId);
 	}
 
 	public void resetHighlightRange() {
 		sourceEditor.resetHighlightRange();
 	}
 
 	public void selectAndReveal(int offset, int length) {
 		sourceEditor.selectAndReveal(offset, length);
 	}
 
 	public void setAction(String actionID, IAction action) {
 		sourceEditor.setAction(actionID, action);
 	}
 
 	public void setActionActivationCode(String actionId,
 			char activationCharacter, int activationKeyCode,
 			int activationStateMask) {
 		sourceEditor.setActionActivationCode(actionId, activationCharacter,
 				activationKeyCode, activationStateMask);
 	}
 
 	public void setHighlightRange(int offset, int length, boolean moveCursor) {
 		sourceEditor.setHighlightRange(offset, length, moveCursor);
 	}
 
 	public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
 		sourceEditor.showHighlightRangeOnly(showHighlightRangeOnly);
 	}
 
 	public boolean showsHighlightRangeOnly() {
 		return sourceEditor.showsHighlightRangeOnly();
 	}
 
 	public VpeTaglibManager getTaglibManager() {
 		if (sourceEditor != null) {
 			if (sourceEditor.getPageContext() instanceof VpeTaglibManager)
 
 				return (VpeTaglibManager) sourceEditor.getPageContext();
 		}
 		return null;
 	}
 
 	void updateTitle() {
 		setPartName(getEditorInput().getName());
 	}
 
 	public void runDropCommand(String flavor, String data) {
 		if (sourceEditor != null)
 			sourceEditor.runDropCommand(flavor, data);
 	}
 
 	public void setStatusField(IStatusField field, String category) {
 		if (sourceEditor != null)
 			sourceEditor.setStatusField(field, category);
 	}
 
 	public boolean isEditorInputReadOnly() {
 		if (sourceEditor != null) {
 			return sourceEditor.isEditorInputReadOnly();
 		}
 		return false;
 	}
 
 	public void addRulerContextMenuListener(IMenuListener listener) {
 		if (sourceEditor != null)
 			sourceEditor.addRulerContextMenuListener(listener);
 	}
 
 	public void removeRulerContextMenuListener(IMenuListener listener) {
 		if (sourceEditor != null)
 			sourceEditor.removeRulerContextMenuListener(listener);
 	}
 
 	public boolean isEditorInputModifiable() {
 		if (sourceEditor != null) {
 			return sourceEditor.isEditorInputModifiable();
 		}
 		return false;
 	}
 
 	public boolean validateEditorInputState() {
 		if (sourceEditor != null) {
 			return sourceEditor.validateEditorInputState();
 		}
 		return false;
 	}
 
 	public InsertMode getInsertMode() {
 		if (sourceEditor != null) {
 			return sourceEditor.getInsertMode();
 		}
 		return null;
 	}
 
 	public void setInsertMode(InsertMode mode) {
 		if (sourceEditor != null)
 			sourceEditor.setInsertMode(mode);
 	}
 
 	public void showChangeInformation(boolean show) {
 		if (sourceEditor != null)
 			sourceEditor.showChangeInformation(show);
 	}
 
 	public boolean isChangeInformationShowing() {
 		if (sourceEditor != null) {
 			return sourceEditor.isChangeInformationShowing();
 		}
 		return false;
 	}
 
 	public INavigationLocation createEmptyNavigationLocation() {
 		if (sourceEditor != null) {
 			return sourceEditor.createEmptyNavigationLocation();
 		}
 		return null;
 	}
 
 	public INavigationLocation createNavigationLocation() {
 		if (sourceEditor != null) {
 			return sourceEditor.createNavigationLocation();
 		}
 		return null;
 	}
 	
 	public void updatePartAccordingToPreferences() {
 		String tabIndex = JspEditorPlugin.getDefault().getPreferenceStore()
 				.getString(IVpePreferencesPage.DEFAULT_VPE_TAB);
 		try {
 			int ind = Integer.parseInt(tabIndex);
 			getTabFolder().setSelection(ind);
 			pageChange(ind);
 		} catch (NumberFormatException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 	}
 
 	/**
 	 * @return the previewIndex
 	 */
 	public int getPreviewIndex() {
 		return previewIndex;
 	}
 
 	/**
 	 * @param previewIndex the previewIndex to set
 	 */
 	public void setPreviewIndex(int previewIndex) {
 		this.previewIndex = previewIndex;
 	}
 	
 	public void setVisualSourceIndex(int visualSourceIndex) {
 		this.visualSourceIndex = visualSourceIndex;
 	}
 
 	public int getVisualSourceIndex() {
 		return visualSourceIndex;
 	}
 
 class ResourceChangeListener implements IResourceChangeListener {
 	IEditorPart editorPart;
 
 	Composite container;
 
 	ResourceChangeListener(IEditorPart editorPart, Composite container) {
 		this.editorPart = editorPart;
 		this.container = container;
 		IWorkspace workspace = ModelUIPlugin.getWorkspace();
 		if (workspace == null)
 			return;
 		workspace.addResourceChangeListener(this);
 		container.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				IWorkspace workspace = ModelUIPlugin.getWorkspace();
 				if (workspace == null)
 					return;
 				workspace
 						.removeResourceChangeListener(ResourceChangeListener.this);
 			}
 		});
 	}
 
 	public void resourceChanged(IResourceChangeEvent event) {
 		final IEditorInput ei = editorPart.getEditorInput();
 		if (!(ei instanceof IFileEditorInput))
 			return;
 		IFileEditorInput fi = (IFileEditorInput) ei;
 		IFile f = fi.getFile();
 		if (f == null)
 			return;
 		IPath path = getPathChange(event, f);
 		if (path == null) {
 			if (f != null && !f.exists())
 				closeEditor();
 			return;
 		}
 		f = ModelPlugin.getWorkspace().getRoot().getFile(path);
 		XModelObject p = f == null ? null : EclipseResourceUtil
 				.getObjectByResource(f.getParent());
 		if (p instanceof FolderImpl) {
 			((FolderImpl) p).update();
 		}
 		final XModelObject o = EclipseResourceUtil.getObjectByResource(f);
 		if (f != null && f.exists() && o != null) {
 			if (editorPart instanceof JSPMultiPageEditor) {
 				final JSPMultiPageEditor e = (JSPMultiPageEditor) editorPart;
 				if (ei instanceof XModelObjectEditorInput) {
 					final IEditorInput e2 = XModelObjectEditorInput.createInstance(o);
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							e.setInput0(e2);
 							e.updateTitle();
 							if (e.getJspEditor() instanceof AbstractTextEditor) {
 								if (e.getJspEditor() != null
 										&& e.getJspEditor().getEditorInput() != e
 												.getEditorInput()) {
 									((AbstractTextEditor) e.getJspEditor())
 											.setInput(e2);
 								}
 								((XModelObjectEditorInput) ei).synchronize();
 							}
 						}
 					});
 				}
 			}
 		}
 		if (f == null || f.exists())
 			return;
 		closeEditor();
 	}
 
 	private void closeEditor() {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				editorPart.getSite().getPage().closeEditor(editorPart, false);
 			}
 		});
 	}
 
 	private IPath getPathChange(IResourceChangeEvent event, IFile f) {
 		return getPathChange(event.getDelta(), f.getFullPath());
 	}
 
 	private IPath getPathChange(IResourceDelta delta, IPath p) {
 		if (delta == null || delta.getFullPath() == null)
 			return null;
 		if (!delta.getFullPath().isPrefixOf(p))
 			return null;
 		if (delta != null && delta.getKind() == IResourceDelta.CHANGED) {
 			IResourceDelta[] ds = delta.getAffectedChildren();
 			if (ds == null)
 				return null;
 			if (ds.length > 1) {
 				IPath removed = null;
 				IPath added = null;
 				for (int i = 0; i < ds.length; i++) {
 					IPath d = ds[i].getFullPath();
 					if (ds[i].getKind() == IResourceDelta.REMOVED 
 							&& (d.equals(p) || d.isPrefixOf(p))) {
 						removed = d;
 					} else if (ds[i].getKind() == IResourceDelta.ADDED) {
 						added = d;
 					}
 				}
 				if (removed != null && added != null) {
 					IPath d = removed;
 					if (d.equals(p))
 						return added;
 					if (d.isPrefixOf(p)) {
 						return added.append(
 								p.removeFirstSegments(d.segmentCount()));
 					}
 				}
 			}
 			for (int i = 0; i < ds.length; i++) {
 				IPath ps = getPathChange(ds[i], p);
 				if (ps != null)
 					return ps;
 			}
 		}
 		return null;
 	}
 }}
