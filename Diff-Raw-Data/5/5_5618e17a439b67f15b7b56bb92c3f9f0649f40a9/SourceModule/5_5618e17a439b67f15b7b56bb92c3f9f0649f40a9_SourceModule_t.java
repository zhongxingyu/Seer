 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.problem.IProblemFactory;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.IPackageDeclaration;
 import org.eclipse.dltk.core.IProblemRequestor;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceModuleInfoCache;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.internal.core.util.MementoTokenizer;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 import org.eclipse.dltk.utils.CorePrinter;
 
 
 public class SourceModule extends Openable implements ISourceModule, org.eclipse.dltk.compiler.env.ISourceModule {
 
 	public ISourceModule getSourceModule() {
 		return this;
 	}
 
 	private static final boolean DEBUG_PRINT_MODEL = DLTKCore.DEBUG_PRINT_MODEL;
 
 	protected String name;
 
 	public WorkingCopyOwner owner;
 	final private boolean fReadOnly;
 	
 	private static int nextId = 1;
 	private final int id = nextId++;
 	
 	public SourceModule(ScriptFolder parent, String name, WorkingCopyOwner owner) {
 		super(parent);
 		if( DLTKCore.VERBOSE ) {
 			System.out.println("SourceModule.SourceModule#" + id + "()");
 		}
 		this.name = name;
 		this.owner = owner;
 		this.fReadOnly = false;
 	}
 	
 	// XXX: never called
 	public SourceModule(ScriptFolder parent, String name, WorkingCopyOwner owner, boolean readOnly) {
 		super(parent);
 		this.name = name;
 		this.owner = owner;
 		this.fReadOnly = readOnly;
 	}
 	
 	/*
 	 * @see ISourceModule#getOwner()
 	 */
 	public WorkingCopyOwner getOwner() {
 		return isPrimary() || !isWorkingCopy() ? null : this.owner;
 	}
 
 	/**
 	 * @see ISourceModule#commitWorkingCopy(boolean, IProgressMonitor)
 	 */
 	public void commitWorkingCopy(boolean force, IProgressMonitor monitor) throws ModelException {
 		CommitWorkingCopyOperation op = new CommitWorkingCopyOperation(this, force);
 		op.runOperation(monitor);
 	}
 
 	/*
 	 * @see ISourceModule#becomeWorkingCopy(IProblemRequestor,
 	 *      IProgressMonitor)
 	 */
 	public void becomeWorkingCopy(IProblemRequestor problemRequestor, IProgressMonitor monitor) throws ModelException {
 		ModelManager manager = ModelManager.getModelManager();
 		ModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager.getPerWorkingCopyInfo(this, false/*don't create*/, true /* record usage */, null/* no problem requestor needed */);
 		if (perWorkingCopyInfo == null) {
 			// close cu and its children
 			close();
 			
 			BecomeWorkingCopyOperation operation = new BecomeWorkingCopyOperation(this, problemRequestor);
 			operation.runOperation(monitor);
 		}
 	}
 
 	protected boolean buildStructure(OpenableElementInfo info,
 			IProgressMonitor pm, Map newElements, IResource underlyingResource)
 			throws ModelException {
 
 		// check if this source module can be opened
 		if (!isWorkingCopy()) { // no check is done on root kind or
 			// exclusion
 			// pattern for working copies
 			IStatus status = validateSourceModule(underlyingResource);
 			if (!status.isOK())
 				throw newModelException(status);
 		}
 		// prevents reopening of non-primary working copies (they are closed
 		// when they are discarded and should not be reopened)
 		if (!isPrimary() && getPerWorkingCopyInfo() == null) {
 			throw newNotPresentException();
 		}
 		SourceModuleElementInfo moduleInfo = (SourceModuleElementInfo) info;
 
 		// get buffer contents
 		IBuffer buffer = getBufferManager().getBuffer(this);
 		if (buffer == null) {
 			buffer = openBuffer(pm, moduleInfo); // open buffer independently
 			// from the info, since we are building the info
 		}
 
 		final char[] contents = buffer == null ? null : buffer.getCharacters();
 		try {
 			// generate structure and compute syntax problems if needed
 			SourceModuleStructureRequestor requestor = new SourceModuleStructureRequestor(
 					this, moduleInfo, newElements);
 
 			IDLTKLanguageToolkit toolkit = null;
 			IResource resource = this.getResource();
 			if (resource == null) {
 				toolkit = DLTKLanguageManager.findToolkit(getPath());
 			} else {
 				toolkit = DLTKLanguageManager.findToolkit(getResource());
 			}
 			if (toolkit == null) {				
 				return false;
 			}
 			//System.out.println("==> Parsing: " + resource.getName());
 			ModelManager.PerWorkingCopyInfo wcInfo = getPerWorkingCopyInfo();
 			IProblemReporter problemReporter;
 			if (wcInfo != null && wcInfo.problemReporter != null) {
 				problemReporter = wcInfo.problemReporter;
 			}
 			else {
 //				problemReporter = toolkit
 //						.createProblemReporter(resource, toolkit
 //								.createProblemFactory());
 				IProblemFactory factory = DLTKLanguageManager.getProblemFactory(toolkit.getNatureID());
 				problemReporter = factory.createReporter(resource);
 			}
 
 			//problemReporter.reportTestProblem();
 
 			ISourceElementParser parser = DLTKLanguageManager.getSourceElementParser(toolkit.getNatureID());
 			parser.setRequestor(requestor);
 			parser.setReporter(problemReporter);
 
 			ISourceModuleInfoCache sourceModuleInfoCache = ModelManager.getModelManager().getSourceModuleInfoCache();
 //			sourceModuleInfoCache.remove(this);
 			ISourceModuleInfo mifo = sourceModuleInfoCache.get(this);
 			parser.parseSourceModule(contents, mifo);
 //			if( mifo.isEmpty()) {
 //				sourceModuleInfoCache.remove(this);
 //			}
 
 			if (SourceModule.DEBUG_PRINT_MODEL) {
 				System.out.println("Source Module Debug print:");
 				CorePrinter printer = new CorePrinter(System.out);
 				printNode(printer);
 				printer.flush();
 			}
 			// update timestamp (might be IResource.NULL_STAMP if original does
 			// not exist)
 			if (underlyingResource == null)
 				underlyingResource = getResource();
 			// underlying resource is null in the case of a working copy out of
 			// workspace
 			if (underlyingResource != null)
 				moduleInfo.timestamp = ((IFile) underlyingResource)
 						.getModificationStamp();
 			return moduleInfo.isStructureKnown();
 		} catch (CoreException e) {
 			throw new ModelException(e);
 		}
 	}
 
 	protected Object createElementInfo() {
 
 		return new SourceModuleElementInfo();
 	}
 
 	public int getElementType() {
 
 		return SOURCE_MODULE;
 	}
 
 	public IResource getResource() {
 
 		ProjectFragment root = this.getProjectFragment();
 		if (root.isArchive()) {
 			return root.getResource();
 		} else {
 			return ((IContainer) this.getParent().getResource()).getFile(new Path(this.getElementName()));
 		}
 	}
 
 	public IPath getPath() {
 
 		ProjectFragment root = this.getProjectFragment();
 		if (root.isArchive()) {
 			return root.getPath();
 		} else {
 			return this.getParent().getPath().append(this.getElementName());
 		}
 	}
 
 	public boolean isWorkingCopy() {
 
 		// For backward compatibility, non primary working copies are always
 		// returning true; in removal
 		// delta, clients can still check that element was a working copy before
 		// being discarded.
 		return !isPrimary() || getPerWorkingCopyInfo() != null;
 	}
 
 	public boolean isPrimary() {
 
 		return this.owner == DefaultWorkingCopyOwner.PRIMARY;
 	}
 
 	/*
 	 * Returns the per working copy info for the receiver, or null if none
 	 * exist. Note: the use count of the per working copy info is NOT
 	 * incremented.
 	 */
 	public ModelManager.PerWorkingCopyInfo getPerWorkingCopyInfo() {
 
 		return ModelManager.getModelManager().getPerWorkingCopyInfo(this, false/*
 																				 * don't
 																				 * create
 																				 */, false/*
 					 * don't record usage
 					 */, null/*
 					 * no problem requestor needed
 					 */);
 	}
 
 	public void discardWorkingCopy() throws ModelException {
 
 		// discard working copy and its children
 		DiscardWorkingCopyOperation op = new DiscardWorkingCopyOperation(this);
 		op.runOperation(null);
 	}
 
 	protected boolean hasBuffer() {
 
 		return true;
 	}
 
 	/**
 	 * Returns true if this handle represents the same element as the given
 	 * handle.
 	 * 
 	 * @see Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj) {
 
 		if (!(obj instanceof SourceModule))
 			return false;
 		SourceModule other = (SourceModule) obj;
 		return this.owner.equals(other.owner) && super.equals(obj);
 	}
 
 	protected IBuffer openBuffer(IProgressMonitor pm, Object info) throws ModelException {
 
 		// create buffer
 		boolean isWorkingCopy = isWorkingCopy();
 		IBuffer buffer = isWorkingCopy ? this.owner.createBuffer(this) : BufferManager.getDefaultBufferManager().createBuffer(this);
 		if (buffer == null)
 			return null;
 
 		// set the buffer source
 		if (buffer.getCharacters() == null) {
 			if (isWorkingCopy) {
 				ISourceModule original;
 				if (!isPrimary() && (original = new SourceModule((ScriptFolder) getParent(), getElementName(), DefaultWorkingCopyOwner.PRIMARY)).isOpen()) {
 					buffer.setContents(original.getSource());
 				} else {
 					IFile file = (IFile) getResource();
 					if (file == null || !file.exists()) {
 						// initialize buffer with empty contents
 						buffer.setContents(CharOperation.NO_CHAR);
 					} else {
 						buffer.setContents(Util.getResourceContentsAsCharArray(file));
 					}
 				}
 			} else {				
 				IFile file = (IFile) this.getResource();
 				if (file == null || !file.exists())
 					throw newNotPresentException();
 				buffer.setContents(Util.getResourceContentsAsCharArray(file));					
 			}
 		}
 
 		// add buffer to buffer cache
 		BufferManager bufManager = getBufferManager();
 		bufManager.addBuffer(buffer);
 
 		// listen to buffer changes
 		buffer.addBufferChangedListener(this);
 
 		return buffer;
 	}
 
 	public String getSource() throws ModelException {
 
 		IBuffer buffer = getBuffer();
 		if (buffer == null)
 			return ""; //$NON-NLS-1$
 		return buffer.getContents();
 	}
 	
 	public char[] getSourceAsCharArray() throws ModelException {
 
 		IBuffer buffer = getBuffer();
 		if (buffer == null)
 			return new char[0]; //$NON-NLS-1$
 		return buffer.getCharacters();
 	}
 
 	public String getElementName() {
 
 		return this.name;
 	}
 
 	public ISourceModule getWorkingCopy(IProgressMonitor monitor) throws ModelException {
 
 		return getWorkingCopy(new WorkingCopyOwner() {/*
 														 * non shared working
 														 * copy
 														 */
 		}, null/* no problem requestor */, monitor);
 	}
 
 	public ISourceModule getWorkingCopy(WorkingCopyOwner workingCopyOwner, IProblemRequestor problemRequestor, IProgressMonitor monitor) throws ModelException {
 		return getWorkingCopy(workingCopyOwner, problemRequestor, null, monitor);
 	}
 
 	public ISourceModule getWorkingCopy(WorkingCopyOwner workingCopyOwner, IProblemRequestor problemRequestor, IProblemReporter problemReporter, IProgressMonitor monitor) throws ModelException {
 		if (!isPrimary()) return this;
 		
 		ModelManager manager = ModelManager.getModelManager();
 		
 		SourceModule workingCopy = new SourceModule((ScriptFolder)getParent(), getElementName(), workingCopyOwner);
 		ModelManager.PerWorkingCopyInfo perWorkingCopyInfo = 
 			manager.getPerWorkingCopyInfo(workingCopy, false/*don't create*/, true/*record usage*/, null/*not used since don't create*/);
 		if (perWorkingCopyInfo != null) {
 			return perWorkingCopyInfo.getWorkingCopy(); // return existing handle instead of the one created above
 		}
 		BecomeWorkingCopyOperation op = new BecomeWorkingCopyOperation(workingCopy, problemRequestor, problemReporter);
 		op.runOperation(monitor);
 		return workingCopy;
 	}
 
 	public boolean exists() {
 
 		// working copy always exists in the model until it is gotten rid of
 		// (even if not on buildpath)
 		if (getPerWorkingCopyInfo() != null)
 			return true;
 
 		// if not a working copy, it exists only if it is a primary compilation
 		// unit
 		return isPrimary() && validateSourceModule(getResource()).isOK();
 	}
 
 	protected IStatus validateSourceModule(IResource resource) {
 		IProjectFragment root = getProjectFragment();
 		try {
 			if (root.getKind() != IProjectFragment.K_SOURCE) {
 				return new ModelStatus(IModelStatusConstants.INVALID_ELEMENT_TYPES, root);
 			}
 		} catch (ModelException e) {
 			return e.getModelStatus();
 		}
 		if (resource != null) {
 			char[][] inclusionPatterns = ((ProjectFragment)root).fullInclusionPatternChars();
 			char[][] exclusionPatterns = ((ProjectFragment)root).fullExclusionPatternChars();
 			if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) 
 				return new ModelStatus(IModelStatusConstants.ELEMENT_NOT_ON_BUILDPATH, this);
 			if (!resource.isAccessible())
 				return new ModelStatus(IModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this);
 		}
 		if(!root.isArchive()) { 
 			try {
 				IDLTKLanguageToolkit toolkit = DLTKLanguageManager.getLanguageToolkit(this);
 				if (toolkit != null) {
 					return toolkit.validateSourceModule(resource);
 				} else {
 					toolkit = DLTKLanguageManager.findToolkit(resource);
 					if (toolkit != null) {
 						return toolkit.validateSourceModule(resource);
 					}
 					return new ModelStatus(IModelStatusConstants.INVALID_RESOURCE, root);
 				}
 			} catch (CoreException ex) {
 				return new ModelStatus(ex);
 			}
 		}
 		else {
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager.findToolkit(resource);
 			if (toolkit != null) {
 				return toolkit.validateSourceModule(resource);
 			}
 			return new ModelStatus(IModelStatusConstants.INVALID_RESOURCE, root);
 		}
 	}
 
 	public boolean canBeRemovedFromCache() {
 
 		if (getPerWorkingCopyInfo() != null)
 			return false; // working copies should remain in the cache until
 		// they are destroyed
 		return super.canBeRemovedFromCache();
 	}
 
 	public boolean canBufferBeRemovedFromCache(IBuffer buffer) {
 
 		if (getPerWorkingCopyInfo() != null)
 			return false; // working copy buffers should remain in the cache
 		// until working copy is destroyed
 		return super.canBufferBeRemovedFromCache(buffer);
 	}
 
 	public void close() throws ModelException {
 
 		if (getPerWorkingCopyInfo() != null)
 			return; // a working copy must remain opened until it is discarded
 		super.close();
 	}
 
 	protected void closing(Object info) {
 
 		if (getPerWorkingCopyInfo() == null) {
 			super.closing(info);
 		} // else the buffer of a working copy must remain open for the
 		// lifetime of the working copy
 	}
 
 	public IModelElement getPrimaryElement(boolean checkOwner) {
 
 		if (checkOwner && isPrimary())
 			return this;
 		return new SourceModule((ScriptFolder) getParent(), getElementName(), DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	public IResource getUnderlyingResource() throws ModelException {
 
 		if (isWorkingCopy() && !isPrimary())
 			return null;
 		return super.getUnderlyingResource();
 	}
 
 	public boolean isConsistent() {
 
 		return !ModelManager.getModelManager().getElementsOutOfSynchWithBuffers().contains(this);
 	}
 
 	public void makeConsistent(IProgressMonitor monitor) throws ModelException {
 
 		// makeConsistent(false/*don't create AST*/, 0, monitor);
		
		//Remove AST Cache element
		ISourceModuleInfoCache sourceModuleInfoCache = ModelManager.getModelManager().getSourceModuleInfoCache();
//		sourceModuleInfoCache.remove(this);
		sourceModuleInfoCache.remove(this);
 		openWhenClosed(createElementInfo(), monitor);
 	}
 
 	protected void openParent(Object childInfo, HashMap newElements, IProgressMonitor pm) throws ModelException {
 
 		if (!isWorkingCopy())
 			super.openParent(childInfo, newElements, pm);
 		// don't open parent for a working copy to speed up the first
 		// becomeWorkingCopy
 	}
 
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
 		} else {
 			super.save(pm, force);
 		}
 	}
 
 	public void reconcile(boolean forceProblemDetection, WorkingCopyOwner workingCopyOwner, IProgressMonitor monitor) throws ModelException {
 
 		if (!isWorkingCopy())
 			return; // Reconciling is not supported on non working copies
 		if (workingCopyOwner == null)
 			workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;		
 
 		ReconcileWorkingCopyOperation op = new ReconcileWorkingCopyOperation(this, forceProblemDetection, workingCopyOwner);
 		//op.runOperation(monitor);
 		ModelManager manager = ModelManager.getModelManager();
 		try {
 			manager.cacheZipFiles(); // cache zip files for performance (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=134172)
 			op.runOperation(monitor);
 		} finally {
 			manager.flushZipFiles();
 		}
 	}
 
 	public ISourceModule getPrimary() {
 		return (ISourceModule) getPrimaryElement(true);
 	}
 
 	/*
 	 * Assume that this is a working copy
 	 */
 	protected void updateTimeStamp(SourceModule original) throws ModelException {
 		long timeStamp = ((IFile) original.getResource()).getModificationStamp();
 		if (timeStamp == IResource.NULL_STAMP) {
 			throw new ModelException(new ModelStatus(IModelStatusConstants.INVALID_RESOURCE));
 		}
 		((SourceModuleElementInfo) getElementInfo()).timestamp = timeStamp;
 	}
 
 	public boolean hasResourceChanged() {
 		if (!isWorkingCopy())
 			return false;
 
 		// if resource got deleted, then #getModificationStamp() will answer
 		// IResource.NULL_STAMP, which is always different from the cached
 		// timestamp
 		Object info = ModelManager.getModelManager().getInfo(this);
 		if (info == null)
 			return false;
 		return ((SourceModuleElementInfo) info).timestamp != getResource().getModificationStamp();
 	}
 
 	public IModelElement getElementAt(int position) throws ModelException {
 		IModelElement e = getSourceElementAt(position);
 		if (e == this) {
 			return null;
 		} else {
 			return e;
 		}
 	}
 
 	public ISourceRange getSourceRange() throws ModelException {
 		return ((SourceModuleElementInfo) getElementInfo()).getSourceRange();
 	}
 
 	public void printNode(CorePrinter output) {
 		output.formatPrint("DLTK Source Module:" + getElementName());
 		output.indent();
 		try {
 			IModelElement modelElements[] = this.getChildren();
 			for (int i = 0; i < modelElements.length; ++i) {
 				IModelElement element = modelElements[i];
 				if (element instanceof ModelElement) {
 					((ModelElement) element).printNode(output);
 				} else {
 					output.print("Unknown element:" + element);
 				}
 			}
 		} catch (ModelException ex) {
 			output.formatPrint(ex.getLocalizedMessage());
 		}
 		output.dedent();
 	}	
 	public IType getType(String typeName) {
 		return new SourceType(this, typeName);
 	}	
 	public IType[] getTypes() throws ModelException {
 		ArrayList list = getChildrenOfType(TYPE);
 		IType[] array= new IType[list.size()];
 		list.toArray(array);
 		return array;
 	}
 	public IModelElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
 		switch (token.charAt(0)) {
 			case JEM_IMPORTDECLARATION:
 				if (DLTKCore.DEBUG) {
 					System.err.println("Add import support in SourceModule getHandleFromMemento");
 				}
 //				ModelElement container = (ModelElement)getImportContainer();
 //				return container.getHandleFromMemento(token, memento, workingCopyOwner);
 				break;
 			case JEM_PACKAGEDECLARATION:
 				if (!memento.hasMoreTokens()) return this;
 				String pkgName = memento.nextToken();
 				ModelElement pkgDecl = (ModelElement)getPackageDeclaration(pkgName);
 				return pkgDecl.getHandleFromMemento(memento, workingCopyOwner);				
 			case JEM_TYPE:
 				if (!memento.hasMoreTokens()) return this;
 				String typeName = memento.nextToken();
 				ModelElement type = (ModelElement)getType(typeName);
 				return type.getHandleFromMemento(memento, workingCopyOwner);
 			case JEM_METHOD:
 				if (!memento.hasMoreTokens()) return this;
 				String methodName = memento.nextToken();
 				ModelElement method = (ModelElement)getMethod(methodName);
 				return method.getHandleFromMemento(memento, workingCopyOwner);
 			case JEM_FIELD:
 				if (!memento.hasMoreTokens()) return this;
 				String field = memento.nextToken();
 				ModelElement fieldE = (ModelElement)getField(field);
 				return fieldE.getHandleFromMemento(memento, workingCopyOwner);
 		}
 		return null;
 	}
 
 	protected char getHandleMementoDelimiter() {
 		return JEM_SOURCEMODULE;
 	}
 
 	public boolean isReadOnly() {
 		return this.fReadOnly;
 	}
 
 	public void copy(IModelElement container, IModelElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws ModelException {
 		if (container == null) {
 			throw new IllegalArgumentException(Messages.operation_nullContainer); 
 		}
 		IModelElement[] elements = new IModelElement[] {this};
 		IModelElement[] containers = new IModelElement[] {container};
 		String[] renamings = null;
 		if (rename != null) {
 			renamings = new String[] {rename};
 		}
 		getModel().copy(elements, containers, null, renamings, replace, monitor);
 		
 	}
 
 	public void delete(boolean force, IProgressMonitor monitor) throws ModelException {
 		IModelElement[] elements= new IModelElement[] {this};
 		getModel().delete(elements, force, monitor);		
 	}
 
 	public void move(IModelElement container, IModelElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws ModelException {
 		if (container == null) {
 			throw new IllegalArgumentException(Messages.operation_nullContainer); 
 		}
 		IModelElement[] elements= new IModelElement[] {this};
 		IModelElement[] containers= new IModelElement[] {container};
 		
 		String[] renamings= null;
 		if (rename != null) {
 			renamings= new String[] {rename};
 		}
 		getModel().move(elements, containers, null, renamings, replace, monitor);		
 	}
 
 	public void rename(String newName, boolean replace, IProgressMonitor monitor) throws ModelException {
 		if (newName == null) {
 			throw new IllegalArgumentException(Messages.operation_nullName); 
 		}
 		IModelElement[] elements= new IModelElement[] {this};
 		IModelElement[] dests= new IModelElement[] {this.getParent()};
 		String[] renamings= new String[] {newName};
 		getModel().rename(elements, dests, renamings, replace, monitor);		
 	}
 
 
 	public void codeComplete(int offset, CompletionRequestor requestor) throws ModelException {
 		codeComplete(offset, requestor, DefaultWorkingCopyOwner.PRIMARY);		
 	}
 
 	public void codeComplete(int offset, CompletionRequestor requestor, WorkingCopyOwner owner) throws ModelException {
 		codeComplete(this, offset, requestor, owner);
 	}
 
 	public IModelElement[] codeSelect(int offset, int length) throws ModelException {
 		return codeSelect(offset, length, DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	public IModelElement[] codeSelect(int offset, int length, WorkingCopyOwner owner) throws ModelException {
 		return super.codeSelect(this, offset, length, owner);
 	}
 
 	public String getSourceContents() {
 		IBuffer buffer = getBufferManager().getBuffer(this);
 		if (buffer == null) {
 			// no need to force opening of CU to get the content
 			// also this cannot be a working copy, as its buffer is never closed while the working copy is alive
 			try {
 				char[] cont= Util.getResourceContentsAsCharArray((IFile) getResource());
 				return cont.toString();
 			} catch (ModelException e) {
 				return "";
 			}
 		}
 		return buffer.getContents();		
 	}
 
 	public IPath getScriptFolder() {
 		return null;
 	}
 
 	public char[] getFileName() {
 		return this.getPath().toOSString().toCharArray();
 	}
 
 	public IModelElement getModelElement() {
 		return this;
 	}	
 	public IPackageDeclaration getPackageDeclaration(String pkg) {
 		return new PackageDeclaration(this, pkg);
 	}
 
 	public IPackageDeclaration[] getPackageDeclarations() throws ModelException {
 		ArrayList list = getChildrenOfType(PACKAGE_DECLARATION);
 		IPackageDeclaration[] array= new IPackageDeclaration[list.size()];
 		list.toArray(array);
 		return array;
 	}
 	public IField getField(String fieldName) {
 		return new SourceField(this, fieldName);
 	}
 
 	public IField[] getFields() throws ModelException {
 		ArrayList list = getChildrenOfType(FIELD);
 		IField[] array = new IField[list.size()];
 		list.toArray(array);
 		return array;
 	}
 
 	public IMethod getMethod(String selector) {
 		return new SourceMethod(this, selector);
 	}
 
 	public IMethod[] getMethods() throws ModelException {
 		ArrayList list = getChildrenOfType(METHOD);
 		IMethod[] array = new IMethod[list.size()];
 		list.toArray(array);
 		return array;
 	}
 	
 	
 	public IType[] getAllTypes() throws ModelException {
 		IModelElement[] types = getTypes();
 		int i;
 		ArrayList allTypes = new ArrayList(types.length);
 		ArrayList typesToTraverse = new ArrayList(types.length);
 		for (i = 0; i < types.length; i++) {
 			typesToTraverse.add(types[i]);
 		}
 		while (!typesToTraverse.isEmpty()) {
 			IType type = (IType) typesToTraverse.get(0);
 			typesToTraverse.remove(type);
 			allTypes.add(type);
 			types = type.getTypes();
 			for (i = 0; i < types.length; i++) {
 				typesToTraverse.add(types[i]);
 			}
 		} 
 		IType[] arrayOfAllTypes = new IType[allTypes.size()];
 		allTypes.toArray(arrayOfAllTypes);
 		return arrayOfAllTypes;
 	}		
 
 	public boolean isBuiltin() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 }
