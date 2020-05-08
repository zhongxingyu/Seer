 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceReference;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.BufferManager;
 import org.eclipse.dltk.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.internal.core.ExternalSourceModule;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.ui.DLTKUIMessages;
 import org.eclipse.dltk.internal.ui.IDLTKStatusConstants;
 import org.eclipse.dltk.internal.ui.editor.DocumentAdapter;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.internal.ui.editor.ISourceModuleDocumentProvider;
 import org.eclipse.dltk.internal.ui.editor.SourceModuleDocumentProvider;
 import org.eclipse.dltk.internal.ui.editor.WorkingCopyManager;
 import org.eclipse.dltk.internal.ui.text.hover.EditorTextHoverDescriptor;
import org.eclipse.dltk.launching.sourcelookup.RemoteScriptSourceLookupDirector.RemoteSourceModule;
 import org.eclipse.dltk.ui.text.completion.ContentAssistHistory;
 import org.eclipse.dltk.ui.viewsupport.ImageDescriptorRegistry;
 import org.eclipse.dltk.ui.viewsupport.ProblemMarkerManager;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.navigator.ICommonMenuConstants;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.texteditor.ConfigurationElementSorter;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class DLTKUIPlugin extends AbstractUIPlugin {
 
 	public static final String PLUGIN_ID = "org.eclipse.dltk.ui";
 	public static final String ID_SCRIPTEXPLORER = "org.eclipse.dltk.ui.ScriptExplorer";
 	public static final String ID_TYPE_HIERARCHY = "org.eclipse.dltk.ui.TypeHierarchy";
 	// The shared instance.
 	private static DLTKUIPlugin plugin;
 
 	private MembersOrderPreferenceCache fMembersOrderPreferenceCache;
 
 	/**
 	 * Content assist history.
 	 * 
 	 * 
 	 */
 	private ContentAssistHistory fContentAssistHistory;
 
 	/**
 	 * The constructor.
 	 */
 	public DLTKUIPlugin() {
 		plugin = this;
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 
 		WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
 			public IBuffer createBuffer(ISourceModule workingCopy) {
 				ISourceModule original = workingCopy.getPrimary();
 				IResource resource = original.getResource();
 				if (resource != null) {
 					if (resource instanceof IFile)
 						return new DocumentAdapter(workingCopy,
 								(IFile) resource);
 				} else if (original instanceof ExternalSourceModule) {
 					IProjectFragment fragment = (IProjectFragment) original
 							.getAncestor(IModelElement.PROJECT_FRAGMENT);
 					if (!fragment.isArchive()) {
 						IPath path = original.getPath();
 						return new DocumentAdapter(workingCopy, path);
 					}
 					return BufferManager.getDefaultBufferManager()
 							.createBuffer(original);
 				}
 
				if (original instanceof RemoteSourceModule) {
 					return BufferManager.getDefaultBufferManager()
 							.createBuffer(original);
 				}
 
 				if (original instanceof BuiltinSourceModule) {
 					// IPath path = original.getPath();
 					// return new DocumentAdapter(workingCopy, path);
 					return BufferManager.getDefaultBufferManager()
 							.createBuffer(original);
 				}
 				return DocumentAdapter.NULL;
 			}
 		});
 
 		// must add here to guarantee that it is the first in the listener list
 
 		IPreferenceStore store = getPreferenceStore();
 		fMembersOrderPreferenceCache = new MembersOrderPreferenceCache();
 		fMembersOrderPreferenceCache.install(store);
 
 		// to initialize launching
 		DLTKLaunchingPlugin.getDefault();
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	public void stop(BundleContext context) throws Exception {
 		if (fMembersOrderPreferenceCache != null) {
 			fMembersOrderPreferenceCache.dispose();
 			fMembersOrderPreferenceCache = null;
 		}
 		super.stop(context);
 		plugin = null;
 	}
 
 	private IWorkbenchPage internalGetActivePage() {
 		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
 		if (window == null)
 			return null;
 		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static DLTKUIPlugin getDefault() {
 		return plugin;
 	}
 
 	public static IWorkbenchPage getActivePage() {
 		return getDefault().internalGetActivePage();
 	}
 
 	public static IWorkbenchWindow getActiveWorkbenchWindow() {
 		return getDefault().getWorkbench().getActiveWorkbenchWindow();
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given plug-in
 	 * relative path.
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return AbstractUIPlugin.imageDescriptorFromPlugin(
 				"org.eclipse.dltk.ui", path);
 	}
 
 	private IWorkingCopyManager fWorkingCopyManager;
 	private ISourceModuleDocumentProvider fSourceModuleDocumentProvider;
 	private ProblemMarkerManager fProblemMarkerManager;
 	private ImageDescriptorRegistry fImageDescriptorRegistry;
 
 	public synchronized IWorkingCopyManager getWorkingCopyManager() {
 		if (fWorkingCopyManager == null) {
 			ISourceModuleDocumentProvider provider = getSourceModuleDocumentProvider();
 			fWorkingCopyManager = new WorkingCopyManager(provider);
 		}
 		return fWorkingCopyManager;
 	}
 
 	public synchronized ISourceModuleDocumentProvider getSourceModuleDocumentProvider() {
 
 		if (fSourceModuleDocumentProvider == null) {
 			fSourceModuleDocumentProvider = new SourceModuleDocumentProvider();
 		}
 		return fSourceModuleDocumentProvider;
 	}
 
 	public static ISourceModuleDocumentProvider getDocumentProvider() {
 		return DLTKUIPlugin.getDefault().getSourceModuleDocumentProvider();
 	}
 
 	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
 		return getDefault().internalGetImageDescriptorRegistry();
 	}
 
 	private ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
 		if (fImageDescriptorRegistry == null)
 			fImageDescriptorRegistry = new ImageDescriptorRegistry();
 		return fImageDescriptorRegistry;
 	}
 
 	/**
 	 * Returns the model element wrapped by the given editor input.
 	 * 
 	 * @param editorInput
 	 *            the editor input
 	 * @return the model element wrapped by <code>editorInput</code> or
 	 *         <code>null</code> if none
 	 */
 	public static IModelElement getEditorInputModelElement(
 			IEditorInput editorInput) {
 		// Performance: check working copy manager first: this is faster
 		IModelElement je = getDefault().getWorkingCopyManager().getWorkingCopy(
 				editorInput);
 		if (je != null) {
 			return je;
 		}
 
 		return (IModelElement) editorInput.getAdapter(IModelElement.class);
 	}
 
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 
 	public static void log(Throwable e) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID,
 				IDLTKStatusConstants.INTERNAL_ERROR,
 				DLTKUIMessages.ScriptPlugin_internal_error, e));
 	}
 
 	public static void logErrorMessage(String message) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID,
 				IDLTKStatusConstants.INTERNAL_ERROR, message, null));
 	}
 
 	public static void logErrorStatus(String message, IStatus status) {
 		if (status == null) {
 			logErrorMessage(message);
 			return;
 		}
 		MultiStatus multi = new MultiStatus(PLUGIN_ID,
 				IDLTKStatusConstants.INTERNAL_ERROR, message, null);
 		multi.add(status);
 		log(multi);
 	}
 
 	public static Shell getActiveWorkbenchShell() {
 		IWorkbenchWindow window = getActiveWorkbenchWindow();
 		if (window != null) {
 			return window.getShell();
 		}
 		return null;
 	}
 
 	/**
 	 * Creates the DLTK plug-in's standard groups for view context menus.
 	 * 
 	 * @param menu
 	 *            the menu manager to be populated
 	 */
 	public static void createStandardGroups(IMenuManager menu) {
 		if (!menu.isEmpty())
 			return;
 
 		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
 		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
 		menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
 		menu.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
 		menu.add(new Separator(ICommonMenuConstants.GROUP_EDIT));
 		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
 		menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
 		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
 		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
 		menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
 		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
 		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
 	}
 
 	public synchronized MembersOrderPreferenceCache getMemberOrderPreferenceCache() {
 		// initialized on startup
 		return fMembersOrderPreferenceCache;
 	}
 
 	public static String getPluginId() {
 		return PLUGIN_ID;
 	}
 
 	/**
 	 * Returns the Script content assist history.
 	 * 
 	 * @return the Script content assist history
 	 * 
 	 */
 	public ContentAssistHistory getContentAssistHistory() {
 		if (fContentAssistHistory == null) {
 			try {
 				fContentAssistHistory = ContentAssistHistory.load(
 						getPluginPreferences(),
 						PreferenceConstants.CODEASSIST_LRU_HISTORY);
 			} catch (CoreException x) {
 				log(x);
 			}
 			if (fContentAssistHistory == null)
 				fContentAssistHistory = new ContentAssistHistory();
 		}
 
 		return fContentAssistHistory;
 	}
 
 	private EditorTextHoverDescriptor[] fEditorTextHoverDescriptors;
 
 	/**
 	 * Resets editor text hovers contributed to the workbench.
 	 * <p>
 	 * This will force a rebuild of the descriptors the next time a client asks
 	 * for them.
 	 * </p>
 	 * 
 	 * 
 	 */
 	public void resetEditorTextHoverDescriptors() {
 		fEditorTextHoverDescriptors = null;
 	}
 
 	/**
 	 * Returns all editor text hovers contributed to the workbench.
 	 * 
 	 * @param store
 	 *            preference store to initialize settings from
 	 * @return an array of EditorTextHoverDescriptor *
 	 */
 	public EditorTextHoverDescriptor[] getEditorTextHoverDescriptors(
 			IPreferenceStore store) {
 		if (fEditorTextHoverDescriptors == null) {
 			fEditorTextHoverDescriptors = EditorTextHoverDescriptor
 					.getContributedHovers(store);
 			ConfigurationElementSorter sorter = new ConfigurationElementSorter() {
 				/*
 				 * @see org.eclipse.ui.texteditor.ConfigurationElementSorter#getConfigurationElement(java.lang.Object)
 				 */
 				public IConfigurationElement getConfigurationElement(
 						Object object) {
 					return ((EditorTextHoverDescriptor) object)
 							.getConfigurationElement();
 				}
 			};
 			sorter.sort(fEditorTextHoverDescriptors);
 
 		}
 
 		return fEditorTextHoverDescriptors;
 	}
 
 	/**
 	 * Opens an editor on the given Java element in the active page. Valid
 	 * elements are all Java elements that are {@link ISourceReference}. For
 	 * elements inside a compilation unit or class file, the parent is opened in
 	 * the editor is opened and the element revealed. If there already is an
 	 * open Java editor for the given element, it is returned.
 	 * 
 	 * @param element
 	 *            the input element; either a compilation unit (<code>ICompilationUnit</code>)
 	 *            or a class file (<code>IClassFile</code>) or source
 	 *            references inside.
 	 * @return returns the editor part of the opened editor or <code>null</code>
 	 *         if the element is not a {@link ISourceReference} or the file was
 	 *         opened in an external editor.
 	 * @exception PartInitException
 	 *                if the editor could not be initialized or no workbench
 	 *                page is active
 	 * @exception JavaModelException
 	 *                if this element does not exist or if an exception occurs
 	 *                while accessing its underlying resource
 	 */
 	public static IEditorPart openInEditor(IModelElement element)
 			throws ModelException, PartInitException {
 		return openInEditor(element, true, true);
 	}
 
 	/**
 	 * Opens an editor on the given Java element in the active page. Valid
 	 * elements are all Java elements that are {@link ISourceReference}. For
 	 * elements inside a compilation unit or class file, the parent is opened in
 	 * the editor is opened. If there already is an open Java editor for the
 	 * given element, it is returned.
 	 * 
 	 * @param element
 	 *            the input element; either a compilation unit (<code>ICompilationUnit</code>)
 	 *            or a class file (<code>IClassFile</code>) or source
 	 *            references inside.
 	 * @param activate
 	 *            if set, the editor will be activated.
 	 * @param reveal
 	 *            if set, the element will be revealed.
 	 * @return returns the editor part of the opened editor or <code>null</code>
 	 *         if the element is not a {@link ISourceReference} or the file was
 	 *         opened in an external editor.
 	 * @exception PartInitException
 	 *                if the editor could not be initialized or no workbench
 	 *                page is active
 	 * @exception JavaModelException
 	 *                if this element does not exist or if an exception occurs
 	 *                while accessing its underlying resource
 	 * @since 3.3
 	 */
 	public static IEditorPart openInEditor(IModelElement element,
 			boolean activate, boolean reveal) throws ModelException,
 			PartInitException {
 		if (!(element instanceof ISourceReference)) {
 			return null;
 		}
 		IEditorPart part = EditorUtility.openInEditor(element, activate);
 		if (reveal && part != null) {
 			EditorUtility.revealInEditor(part, element);
 		}
 		return part;
 	}
 
 	public synchronized ProblemMarkerManager getProblemMarkerManager() {
 		if (fProblemMarkerManager == null)
 			fProblemMarkerManager = new ProblemMarkerManager();
 		return fProblemMarkerManager;
 	}
 
 	public static boolean isDebug() {
 		return DLTKCore.DEBUG;
 	}
 }
