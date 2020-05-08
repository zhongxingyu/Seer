 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.IProblemRequestor;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceModuleInfoCache;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 
 /**
  */
 public class SourceModule extends AbstractSourceModule implements ISourceModule {
 	private static int nextId = 1;
 
 	private final int id = nextId++;
 
 	// ~ Constructors
 
 	public SourceModule(ScriptFolder parent, String name, WorkingCopyOwner owner) {
 		super(parent, name, owner, false);
 
 		if (DLTKCore.VERBOSE) {
 			System.out.println("SourceModule.SourceModule#" + id + "()");
 		}
 	}
 
 	// ~ Methods
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceModule#becomeWorkingCopy(org.eclipse.dltk.core.IProblemRequestor,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void becomeWorkingCopy(IProblemRequestor problemRequestor,
 			IProgressMonitor monitor) throws ModelException {
 		ModelManager manager = ModelManager.getModelManager();
 		ModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager
 				.getPerWorkingCopyInfo(this, false /* don't create */,
 						true /* record usage */, null /*
 														 * no problem requestor
 														 * needed
 														 */);
 		if (perWorkingCopyInfo == null) {
 			// close cu and its children
 			close();
 
 			BecomeWorkingCopyOperation operation = new BecomeWorkingCopyOperation(
 					this, problemRequestor);
 			operation.runOperation(monitor);
 		}
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.Openable#canBeRemovedFromCache()
 	 */
 	public boolean canBeRemovedFromCache() {
 		if (getPerWorkingCopyInfo() != null) {
 			return false; // working copies should remain in the cache until
 		}
 		// they are destroyed
 		return super.canBeRemovedFromCache();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.Openable#canBufferBeRemovedFromCache(org.eclipse.dltk.core.IBuffer)
 	 */
 	public boolean canBufferBeRemovedFromCache(IBuffer buffer) {
 		if (getPerWorkingCopyInfo() != null) {
 			return false; // working copy buffers should remain in the cache
 		}
 		// until working copy is destroyed
 		return super.canBufferBeRemovedFromCache(buffer);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.ModelElement#close()
 	 */
 	public void close() throws ModelException {
 		if (getPerWorkingCopyInfo() != null) {
 			return; // a working copy must remain opened until it is discarded
 		}
 
 		super.close();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ICodeAssist#codeComplete(int,
 	 *      org.eclipse.dltk.core.CompletionRequestor)
 	 */
 	public void codeComplete(int offset, CompletionRequestor requestor)
 			throws ModelException {
 		codeComplete(offset, requestor, DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ICodeAssist#codeComplete(int,
 	 *      org.eclipse.dltk.core.CompletionRequestor,
 	 *      org.eclipse.dltk.core.WorkingCopyOwner)
 	 */
 	public void codeComplete(int offset, CompletionRequestor requestor,
 			WorkingCopyOwner owner) throws ModelException {
 		codeComplete(this, offset, requestor, owner);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceModule#commitWorkingCopy(boolean,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void commitWorkingCopy(boolean force, IProgressMonitor monitor)
 			throws ModelException {
 		CommitWorkingCopyOperation op = new CommitWorkingCopyOperation(this,
 				force);
 		op.runOperation(monitor);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceManipulation#delete(boolean,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void delete(boolean force, IProgressMonitor monitor)
 			throws ModelException {
 		IModelElement[] elements = new IModelElement[] { this };
 		getModel().delete(elements, force, monitor);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceModule#discardWorkingCopy()
 	 */
 	public void discardWorkingCopy() throws ModelException {
 		// discard working copy and its children
 		DiscardWorkingCopyOperation op = new DiscardWorkingCopyOperation(this);
 		op.runOperation(null);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj) {
 		if (!(obj instanceof SourceModule)) {
 			return false;
 		}
 
 		return super.equals(obj);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.Openable#exists()
 	 */
 	public boolean exists() {
 		// working copy always exists in the model until it is gotten rid of
 		// (even if not on buildpath)
 		if (getPerWorkingCopyInfo() != null) {
 			return true;
 		}
 
 		return super.exists();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.compiler.env.IDependent#getFileName()
 	 */
 	public char[] getFileName() {
 		return this.getPath().toOSString().toCharArray();
 	}
 
 	/*
 	 * Returns the per working copy info for the receiver, or null if none
 	 * exist. Note: the use count of the per working copy info is NOT
 	 * incremented.
 	 */
 	public ModelManager.PerWorkingCopyInfo getPerWorkingCopyInfo() {
 		// XXX: should be an interface method that allows a null
 		// don't create or record usage - no problem requestor required
 		return ModelManager.getModelManager().getPerWorkingCopyInfo(this,
 				false, false, null);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.IModelElement#getResource()
 	 */
 	public IResource getResource() {
 		ProjectFragment root = this.getProjectFragment();
 		if (root.isArchive()) {
 			return root.getResource();
 		}
 
 		return ((IContainer) this.getParent().getResource()).getFile(new Path(
 				this.getElementName()));
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceModule#getWorkingCopy(org.eclipse.dltk.core.WorkingCopyOwner,
 	 *      org.eclipse.dltk.core.IProblemRequestor,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ISourceModule getWorkingCopy(WorkingCopyOwner workingCopyOwner,
 			IProblemRequestor problemRequestor, IProgressMonitor monitor)
 			throws ModelException {
 		if (!isPrimary()) {
 			return this;
 		}
 
 		ModelManager manager = ModelManager.getModelManager();
 
 		SourceModule workingCopy = new SourceModule((ScriptFolder) getParent(),
 				getElementName(), workingCopyOwner);
 		ModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager
 				.getPerWorkingCopyInfo(workingCopy, false /* don't create */,
 						true /* record usage */, null /*
 														 * not used since don't
 														 * create
 														 */);
 		if (perWorkingCopyInfo != null) {
 			return perWorkingCopyInfo.getWorkingCopy(); // return existing
 			// handle instead of the
 			// one
 			// created above
 		}
 
 		BecomeWorkingCopyOperation op = new BecomeWorkingCopyOperation(
 				workingCopy, problemRequestor, null);
 		op.runOperation(monitor);
 		return workingCopy;
 	}
 
 	public boolean hasResourceChanged() {
 		// XXX: should be an interface method
 		if (!isWorkingCopy()) {
 			return false;
 		}
 
 		// if resource got deleted, then #getModificationStamp() will answer
 		// IResource.NULL_STAMP, which is always different from the cached
 		// timestamp
 		Object info = ModelManager.getModelManager().getInfo(this);
 		if (info == null) {
 			return false;
 		}
 
 		return ((SourceModuleElementInfo) info).timestamp != getResource()
 				.getModificationStamp();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceModule#isWorkingCopy()
 	 */
 	public boolean isWorkingCopy() {
 		// For backward compatibility, non primary working copies are always
 		// returning true; in removal
 		// delta, clients can still check that element was a working copy before
 		// being discarded.
 		return !isPrimary() || (getPerWorkingCopyInfo() != null);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.Openable#makeConsistent(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void makeConsistent(IProgressMonitor monitor) throws ModelException {
 		// makeConsistent(false/*don't create AST*/, 0, monitor);
 
 		// Remove AST Cache element
 		ISourceModuleInfoCache sourceModuleInfoCache = ModelManager
 				.getModelManager().getSourceModuleInfoCache();
 		// sourceModuleInfoCache.remove(this);
 		sourceModuleInfoCache.remove(this);
 		openWhenClosed(createElementInfo(), monitor);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceManipulation#move(org.eclipse.dltk.core.IModelElement,
 	 *      org.eclipse.dltk.core.IModelElement, java.lang.String, boolean,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void move(IModelElement container, IModelElement sibling,
 			String rename, boolean replace, IProgressMonitor monitor)
 			throws ModelException {
 		if (container == null) {
 			throw new IllegalArgumentException(Messages.operation_nullContainer);
 		}
 
 		IModelElement[] elements = new IModelElement[] { this };
 		IModelElement[] containers = new IModelElement[] { container };
 
 		String[] renamings = null;
 		if (rename != null) {
 			renamings = new String[] { rename };
 		}
 
 		getModel()
 				.move(elements, containers, null, renamings, replace, monitor);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceModule#reconcile(boolean,
 	 *      org.eclipse.dltk.core.WorkingCopyOwner,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void reconcile(boolean forceProblemDetection,
 			WorkingCopyOwner workingCopyOwner, IProgressMonitor monitor)
 			throws ModelException {
 		if (!isWorkingCopy()) {
 			return; // Reconciling is not supported on non working copies
 		}
 
 		if (workingCopyOwner == null) {
 			workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
 		}
 
 		ReconcileWorkingCopyOperation op = new ReconcileWorkingCopyOperation(
 				this, forceProblemDetection, workingCopyOwner);
 		// op.runOperation(monitor);
 		ModelManager manager = ModelManager.getModelManager();
 		try {
 			// cache zip files for performance (see
 			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=134172)
 			manager.cacheZipFiles();
 			op.runOperation(monitor);
 		} finally {
 			manager.flushZipFiles();
 		}
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.ISourceManipulation#rename(java.lang.String,
 	 *      boolean, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void rename(String newName, boolean replace, IProgressMonitor monitor)
 			throws ModelException {
 		if (newName == null) {
 			throw new IllegalArgumentException(Messages.operation_nullName);
 		}
 
 		IModelElement[] elements = new IModelElement[] { this };
 		IModelElement[] dests = new IModelElement[] { this.getParent() };
 		String[] renamings = new String[] { newName };
 		getModel().rename(elements, dests, renamings, replace, monitor);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.Openable#save(org.eclipse.core.runtime.IProgressMonitor,
 	 *      boolean)
 	 */
 	public void save(IProgressMonitor pm, boolean force) throws ModelException {
 		if (isWorkingCopy()) {
 			// no need to save the buffer for a working copy (this is a noop)
 			throw new RuntimeException("not implemented"); // not simply
 			// makeConsistent,
 			// also computes
 			// fine-grain deltas
 			// in case the working copy is being reconciled already (if not it
 			// would miss
 			// one iteration of deltas).
 		}
 
 		super.save(pm, force);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#preventReopen()
 	 */
 	protected boolean preventReopen() {
 		return super.preventReopen() && (getPerWorkingCopyInfo() == null);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#getProblemReporter(java.lang.String)
 	 */
 	protected IProblemReporter getProblemReporter(String natureId)
 			throws CoreException {
 		// check for the working copy reporter first
 		ModelManager.PerWorkingCopyInfo wcInfo = getPerWorkingCopyInfo();
 		if ((wcInfo != null) && (wcInfo.problemReporter != null)) {
 			return wcInfo.problemReporter;
 		}
 
 		return super.getProblemReporter(natureId);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#getNatureId()
 	 */
 	protected String getNatureId() throws CoreException {
 		IResource resource = this.getResource();
 		Object lookup = (resource == null) ? (Object) getPath() : resource;
 
		return lookupLanguageToolkit(lookup).getNatureId();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.Openable#closing(java.lang.Object)
 	 */
 	protected void closing(Object info) {
 		if (getPerWorkingCopyInfo() == null) {
 			super.closing(info);
 		}
 		// else the buffer of a working copy must remain open for the
 		// lifetime of the working copy
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#getBufferContent()
 	 */
 	protected char[] getBufferContent() throws ModelException {
 		IFile file = (IFile) this.getResource();
 		if (file == null || !file.exists()) {
 			// throw newNotPresentException();
 			// initialize buffer with empty contents
 			return CharOperation.NO_CHAR;
 		}
 
 		return Util.getResourceContentsAsCharArray(file);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#getModuleType()
 	 */
 	protected String getModuleType() {
 		return "DLTK Source Module: ";
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#getOriginalSourceModule()
 	 */
 	protected ISourceModule getOriginalSourceModule() {
 		return new SourceModule((ScriptFolder) getParent(), getElementName(),
 				DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	/*
 	 * Assume that this is a working copy
 	 */
 	protected void updateTimeStamp(SourceModule original) throws ModelException {
 		// XXX: should be an interface method
 		long timeStamp = ((IFile) original.getResource())
 				.getModificationStamp();
 		if (timeStamp == IResource.NULL_STAMP) {
 			throw new ModelException(new ModelStatus(
 					IModelStatusConstants.INVALID_RESOURCE));
 		}
 
 		((SourceModuleElementInfo) getElementInfo()).timestamp = timeStamp;
 	}
 }
