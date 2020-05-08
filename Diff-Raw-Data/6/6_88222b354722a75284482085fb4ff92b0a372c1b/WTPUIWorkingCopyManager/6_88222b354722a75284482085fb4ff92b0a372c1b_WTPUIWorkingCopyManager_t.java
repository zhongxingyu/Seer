 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.common.jdt.internal.integration.ui;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.IClassFile;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;
 import org.eclipse.jdt.internal.ui.javaeditor.InternalClassFileEditorInput;
 import org.eclipse.jdt.ui.IWorkingCopyManager;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jst.common.jdt.internal.integration.WTPWorkingCopyManager;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.wst.common.frameworks.internal.SaveFailedException;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 /**
  * Insert the type's description here. Creation date: (4/25/2001 7:05:36 PM)
  * 
  * @author: Administrator
  */
 public class WTPUIWorkingCopyManager extends WTPWorkingCopyManager {
 	private IWorkingCopyManager javaWorkingCopyManager;
 	private ICompilationUnitDocumentProvider cuDocumentProvider;
 	private HashMap editorInputs;
 	private CoreException lastError;
 
 	/**
 	 * WTPUIWorkingCopyManager constructor comment.
 	 */
 	public WTPUIWorkingCopyManager() {
 		super();
 		cuDocumentProvider = JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
 		javaWorkingCopyManager = JavaUI.getWorkingCopyManager();
 	}
 
 	protected void syncConnect(final IEditorInput input, final ICompilationUnit cu) throws CoreException {
 		Display d = Display.getCurrent();
 		if (d != null) {
 			lastError = null;
 			d.syncExec(new Runnable() {
 				public void run() {
 					try {
 						connect(input, cu);
 					} catch (CoreException e) {
 						lastError = e;
 					}
 				}
 			});
 		} else
 			connect(input, cu);
 		if (lastError != null)
 			throw lastError;
 	}
 
 	/**
 	 * Connect the CompilationUnitDocumentProvider to the
 	 * 
 	 * @input and connect the annotation model from the provider to the IDocument of the
 	 * @input.
 	 */
 	protected void connect(IEditorInput input, ICompilationUnit cu) throws CoreException {
		if (input != null && javaWorkingCopyManager != null && cuDocumentProvider != null ) {
 			javaWorkingCopyManager.connect(input);
 			getEditorInputs().put(cu, input);
 			IDocument doc = cuDocumentProvider.getDocument(input);
			if (doc != null && cuDocumentProvider.getAnnotationModel(input)!= null)
				cuDocumentProvider.getAnnotationModel(input).connect(doc);
 		}
 	}
 
 	protected void revertWorkingCopies() {
 		if (getEditorInputs().isEmpty())
 			return;
 		Iterator it = getEditorInputs().values().iterator();
 		IEditorInput input;
 		while (it.hasNext()) {
 			input = (IEditorInput) it.next();
 			revert(input);
 		}
 	}
 
 	/**
 	 * Disonnect the CompilationUnitDocumentProvider from the
 	 * 
 	 * @input and disconnect the annotation model from the provider from the IDocument of the
 	 * @input.
 	 */
 	protected void disconnect(IEditorInput input) {
 		IDocument doc = cuDocumentProvider.getDocument(input);
 		cuDocumentProvider.getAnnotationModel(input).disconnect(doc);
 		javaWorkingCopyManager.disconnect(input);
 	}
 
 	protected void revert(IEditorInput input) {
 		try {
 			cuDocumentProvider.resetDocument(input);
 		} catch (CoreException e) {
 			Logger.getLogger().logError(e);
 		}
 		IDocument doc = cuDocumentProvider.getDocument(input);
 		IAnnotationModel model = cuDocumentProvider.getAnnotationModel(input);
 
 		if (model instanceof AbstractMarkerAnnotationModel) {
 			AbstractMarkerAnnotationModel markerModel = (AbstractMarkerAnnotationModel) model;
 			markerModel.resetMarkers();
 		}
 		model.disconnect(doc);
 		javaWorkingCopyManager.disconnect(input);
 	}
 
 	protected void disconnectEditorInputs() {
 		Iterator it = getEditorInputs().values().iterator();
 		IEditorInput input;
 		while (it.hasNext()) {
 			input = (IEditorInput) it.next();
 			disconnect(input);
 		}
 	}
 
 	protected void discardExistingCompilationUnits() {
 		if (getEditorInputs().isEmpty())
 			return;
 		Iterator it = getEditorInputs().values().iterator();
 		IEditorInput input;
 		while (it.hasNext()) {
 			input = (IEditorInput) it.next();
 			disconnect(input);
 		}
 	}
 
 	public Set getAffectedFiles() {
 		Set aSet = new HashSet();
 		Iterator it = getEditorInputs().keySet().iterator();
 		ICompilationUnit unit = null;
 		IResource resource = null;
 		while (it.hasNext()) {
 			unit = (ICompilationUnit) it.next();
 			if (isDirty(unit)) {
 				try {
 					resource = unit.getUnderlyingResource();
 				} catch (JavaModelException ignore) {
 					continue;
 				}
 				if (resource instanceof IFile)
 					aSet.add(resource);
 			}
 		}
 		return aSet;
 	}
 
 	protected IEditorInput getEditorInput(ICompilationUnit cu) {
 		IEditorInput input = primGetEditorInput(cu);
 		if (input == null) {
 			try {
 				input = getEditorInput((IJavaElement) cu);
 			} catch (JavaModelException e) {
 				//Ignore
 			}
 		}
 		return input;
 	}
 
 	protected IEditorInput getEditorInput(IJavaElement element) throws JavaModelException {
 		while (element != null) {
 			switch (element.getElementType()) {
 				case IJavaElement.COMPILATION_UNIT : {
 					ICompilationUnit cu = (ICompilationUnit) element;
 					if (cu.isWorkingCopy())
 						cu = cu.getPrimary();
 					IResource resource = cu.getUnderlyingResource();
 					if (resource.getType() == IResource.FILE)
 						return new FileEditorInput((IFile) resource);
 					break;
 				}
 				case IJavaElement.CLASS_FILE :
 					return new InternalClassFileEditorInput((IClassFile) element);
 			}
 			element = element.getParent();
 		}
 		return null;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (4/25/2001 7:30:20 PM)
 	 * 
 	 * @return java.util.HashMap
 	 */
 	protected java.util.HashMap getEditorInputs() {
 		if (editorInputs == null)
 			editorInputs = new HashMap(20);
 		return editorInputs;
 	}
 
 	/**
 	 * Returns the working copy remembered for the compilation unit encoded in the given editor
 	 * input. Does not connect the edit model to the working copy.
 	 * 
 	 * @param input
 	 *            ICompilationUnit
 	 * @return the working copy of the compilation unit, or <code>null</code> if the input does
 	 *         not encode an editor input, or if there is no remembered working copy for this
 	 *         compilation unit
 	 */
 	public org.eclipse.jdt.core.ICompilationUnit getExistingWorkingCopy(ICompilationUnit cu) throws CoreException {
 		if (cu == null || cu.isWorkingCopy()) {
 			return cu;
 		}
 		ICompilationUnit newCU = super.getExistingWorkingCopy(cu);
 		if (newCU != null)
 			return newCU;
 		IEditorInput editorInput = getEditorInput(cu);
 		return javaWorkingCopyManager.getWorkingCopy(editorInput);
 	}
 
 	/**
 	 * Returns the working copy remembered for the compilation unit.
 	 * 
 	 * @param input
 	 *            ICompilationUnit
 	 * @return the working copy of the compilation unit, or <code>null</code> if there is no
 	 *         remembered working copy for this compilation unit
 	 */
 	public org.eclipse.jdt.core.ICompilationUnit getWorkingCopy(ICompilationUnit cu, boolean forNewCU) throws org.eclipse.core.runtime.CoreException {
 		if (forNewCU)
 			return super.getWorkingCopy(cu, forNewCU);
 		return primGetWorkingCopy(cu);
 	}
 
 	public boolean isDirty(ICompilationUnit cu) {
 		if (cu == null)
 			return false;
 		IDocumentProvider p = cuDocumentProvider;
 		return p == null ? false : p.canSaveDocument(getEditorInput(cu));
 	}
 
 	/**
 	 * mustSaveDocument method comment.
 	 */
 	public boolean isSaveNeeded() {
 		Iterator it = getEditorInputs().entrySet().iterator();
 		while (it.hasNext()) {
 			if (cuDocumentProvider.mustSaveDocument(it.next()))
 				return true;
 		}
 		return false;
 	}
 
 	protected void primDispose() {
 		super.primDispose();
 		discardExistingCompilationUnits();
 		editorInputs = null;
 		javaWorkingCopyManager = null;
 	}
 
 	protected void primRevert() {
 		super.primRevert();
 		revertWorkingCopies();
 		editorInputs = null;
 		javaWorkingCopyManager = null;
 	}
 
 	protected IEditorInput primGetEditorInput(ICompilationUnit cu) {
 		return (IEditorInput) getEditorInputs().get(cu);
 	}
 
 	/**
 	 * Returns the working copy remembered for the compilation unit encoded in the given editor
 	 * input.
 	 * 
 	 * @param input
 	 *            ICompilationUnit
 	 * @return the working copy of the compilation unit, or <code>null</code> if the input does
 	 *         not encode an editor input, or if there is no remembered working copy for this
 	 *         compilation unit
 	 */
 	protected org.eclipse.jdt.core.ICompilationUnit primGetWorkingCopy(ICompilationUnit cu) throws CoreException {
 		if (cu == null) {
 			return cu;
 		}
 		ICompilationUnit primary = cu.getPrimary();
 		ICompilationUnit newCU = getNewCompilationUnitWorkingCopy(primary);
 		if (newCU != null)
 			return newCU;
 		IEditorInput editorInput = primGetEditorInput(primary);
 		if (editorInput == null) {
 			editorInput = getEditorInput(cu);
 			syncConnect(editorInput, cu);
 		}
 		if (cu.isWorkingCopy())
 			return cu;
 		return javaWorkingCopyManager.getWorkingCopy(editorInput);
 	}
 
 	/**
 	 * This will save all of the referenced CompilationUnits to be saved.
 	 */
 	protected void primSaveCompilationUnits(org.eclipse.core.runtime.IProgressMonitor monitor) {
 		super.primSaveCompilationUnits(null);
 		saveExistingCompilationUnits(monitor);
 	}
 
 	protected void primSaveDocument(IEditorInput input, IDocument doc, IProgressMonitor monitor) throws CoreException {
 		try {
 			cuDocumentProvider.saveDocument(monitor, input, doc, true); // overwrite if needed
 		} catch (CoreException ex) {
 			if (!isFailedWriteFileFailure(ex))
 				throw ex;
 			IResource resource = (IResource) input.getAdapter(IRESOURCE_CLASS);
 			if (resource == null || resource.getType() != IResource.FILE || !resource.getResourceAttributes().isReadOnly())
 				throw ex;
 
 			if (getSaveHandler().shouldContinueAndMakeFileEditable((IFile) resource))
 				cuDocumentProvider.saveDocument(monitor, input, doc, false);
 			else
 				throw ex;
 		}
 	}
 
 	protected void saveDocument(IEditorInput input, IProgressMonitor monitor) {
 		IDocument doc = cuDocumentProvider.getDocument(input);
 		boolean canSave = cuDocumentProvider.canSaveDocument(input);
 		try {
 			if (canSave) {
 				ICompilationUnit unit = javaWorkingCopyManager.getWorkingCopy(input);
 				synchronized (unit) {
 					cuDocumentProvider.aboutToChange(input);
 					primSaveDocument(input, doc, monitor);
 				}
 			}
 		} catch (CoreException e) {
 			WTPCommonPlugin.getDefault().getLogger().logError(e);
 			throw new SaveFailedException(e);
 		} finally {
 			if (canSave)
 				cuDocumentProvider.changed(input);
 		}
 	}
 
 	/**
 	 * This will save all of the referenced CompilationUnits to be saved.
 	 */
 	protected void saveExistingCompilationUnits(org.eclipse.core.runtime.IProgressMonitor monitor) {
 		if (getEditorInputs().isEmpty())
 			return;
 		if (!validateState()) {
 			if (monitor != null)
 				monitor.setCanceled(true);
 			return;
 		}
 		Iterator it = getEditorInputs().entrySet().iterator();
 		Map.Entry entry;
 		//	ICompilationUnit cu;
 		IEditorInput input;
 		try {
 			while (it.hasNext()) {
 				entry = (Map.Entry) it.next();
 				//			cu = (ICompilationUnit) entry.getKey();
 				input = (IEditorInput) entry.getValue();
 				try {
 					saveDocument(input, null);
 				} finally {
 					disconnect(input);
 				}
 			}
 		} finally {
 			getEditorInputs().clear();
 		}
 	}
 
 	/**
 	 * Call validateEdit for all read only IFiles corresponding to each WorkingCopy.
 	 * 
 	 * @return boolean
 	 */
 	private boolean validateState() {
 		List readOnlyFiles = getReadOnlyModifiedFiles();
 		if (readOnlyFiles != null && !readOnlyFiles.isEmpty()) {
 			IFile[] files = new IFile[readOnlyFiles.size()];
 			readOnlyFiles.toArray(files);
 			IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 			Object ctx = win == null ? null : win.getShell();
 			IStatus status = ResourcesPlugin.getWorkspace().validateEdit(files, ctx);
 			return status.isOK();
 		}
 		return true;
 	}
 
 	private List getReadOnlyModifiedFiles() {
 		List readOnlyFiles = null;
 		IFile readOnlyFile = null;
 		Iterator it = getEditorInputs().entrySet().iterator();
 		Map.Entry entry;
 		//	ICompilationUnit cu;
 		IEditorInput input;
 		IDocumentProvider docProv = cuDocumentProvider;
 		while (it.hasNext()) {
 			readOnlyFile = null;
 			entry = (Map.Entry) it.next();
 			//		cu = (ICompilationUnit) entry.getKey();
 			input = (IEditorInput) entry.getValue();
 			if (docProv.canSaveDocument(input))
 				readOnlyFile = getReadOnlyFile(input);
 			if (readOnlyFile != null) {
 				if (readOnlyFiles == null)
 					readOnlyFiles = new ArrayList();
 				readOnlyFiles.add(readOnlyFile);
 			}
 		}
 		return readOnlyFiles;
 	}
 
 	private IFile getReadOnlyFile(IEditorInput input) {
 		if (input instanceof IFileEditorInput) {
 			IFileEditorInput finput = (IFileEditorInput) input;
 			IFile file = finput.getFile();
 			if (file.isReadOnly())
 				return file;
 		}
 		return null;
 	}
 
 
 	protected void addDeletedCompilationUnit(ICompilationUnit cu) {
 		IEditorInput input = primGetEditorInput(cu);
 		if (input != null)
 			disconnect(input);
 		getEditorInputs().remove(cu);
 		super.addDeletedCompilationUnit(cu);
 	}
 
 	/**
 	 * @see com.ibm.etools.j2ee.workbench.IJ2EEWorkingCopyManager#hasWorkingCopies()
 	 */
 	public boolean hasWorkingCopies() {
 		return super.hasWorkingCopies() || (editorInputs != null && !editorInputs.isEmpty());
 	}
 
 }
