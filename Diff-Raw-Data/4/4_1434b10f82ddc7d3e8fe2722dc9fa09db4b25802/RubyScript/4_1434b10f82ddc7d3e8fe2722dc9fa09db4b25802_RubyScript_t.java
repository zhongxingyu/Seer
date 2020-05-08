 /*
  * Author: C.Williams
  * 
  * Copyright (c) 2004 RubyPeople.
  * 
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
  * can get copy of the GPL along with further information about RubyPeople and
  * third party software bundled with RDT in the file
  * org.rubypeople.rdt.core_x.x.x/RDT.license or otherwise at
  * http://www.rubypeople.org/RDT.license.
  * 
  * RDT is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 package org.rubypeople.rdt.internal.core;
 
 import java.io.BufferedReader;
 import java.io.CharArrayReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.jruby.ast.Node;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.rdt.core.IBuffer;
 import org.rubypeople.rdt.core.ICodeAssist;
 import org.rubypeople.rdt.core.IImportContainer;
 import org.rubypeople.rdt.core.IImportDeclaration;
 import org.rubypeople.rdt.core.IOpenable;
 import org.rubypeople.rdt.core.IProblemRequestor;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyModelStatusConstants;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.ISourceRange;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.core.WorkingCopyOwner;
 import org.rubypeople.rdt.internal.core.buffer.BufferManager;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.util.Util;
 
 
 /**
  * @author Chris
  * 
  */
 public class RubyScript extends Openable implements IRubyScript {
 
 	private IFile underlyingFile;
 	public WorkingCopyOwner owner;
 	protected String name;
 
 	/**
 	 * @param name
 	 */
 	public RubyScript(RubyElement parent, IFile file, String name, WorkingCopyOwner owner) {
 		super(parent);
 		this.name = name;
 		this.owner = owner;
 		this.underlyingFile = file;
 	}
 
 	/**
 	 * Returns a new element info for this element.
 	 */
 	protected Object createElementInfo() {
 		return new RubyScriptElementInfo();
 	}
 
 	protected boolean buildStructure(OpenableElementInfo info, final IProgressMonitor pm, Map newElements, IResource underlyingResource) throws RubyModelException {
 		// check if this compilation unit can be opened
 		if (!isWorkingCopy()) { // no check is done on root kind or exclusion
 			// pattern for working copies
 			IStatus status = validateRubyScript(underlyingResource);
 			if (!status.isOK()) throw newRubyModelException(status);
 		}
 
 		// prevents reopening of non-primary working copies (they are closed
 		// when they are discarded and should not be reopened)
 		if (!isPrimary() && getPerWorkingCopyInfo() == null) { throw newNotPresentException(); }
 
 		RubyScriptElementInfo unitInfo = (RubyScriptElementInfo) info;
 
 		// get buffer contents
 		IBuffer buffer = getBufferManager().getBuffer(this);
 		if (buffer == null) {
 			buffer = openBuffer(pm, unitInfo); // open buffer independently
 			// from the info, since we are
 			// building the info
 		}
 		final char[] contents = buffer == null ? null : buffer.getCharacters();
 
 		RubyModelManager.PerWorkingCopyInfo perWorkingCopyInfo = getPerWorkingCopyInfo();
 		IRubyProject project = getRubyProject();
 		boolean computeProblems = RubyProject.hasRubyNature(project.getProject()) && perWorkingCopyInfo != null && perWorkingCopyInfo.isActive();
 
 		try {
 			RubyParser parser = new RubyParser();
 			Node node = parser.parse(underlyingFile, new CharArrayReader(contents));
 			RubyScriptStructureBuilder visitor = new RubyScriptStructureBuilder(this, unitInfo, newElements);
 			if (node != null) node.accept(visitor);
 			unitInfo.setIsStructureKnown(true);
 		} catch (SyntaxException e) {
 			unitInfo.setIsStructureKnown(false);
 			unitInfo.setSyntaxException(e) ;
 		} catch (Exception e) {
 			RubyCore.log(e);
 		}
 
 		// update timestamp (might be IResource.NULL_STAMP if original does not
 		// exist)
 		if (underlyingResource == null) {
 			underlyingResource = getResource();
 		}
 		unitInfo.timestamp = ((IFile) underlyingResource).getModificationStamp();
 
 		// compute other problems if needed
 		if (computeProblems) {
 			perWorkingCopyInfo.beginReporting();
 			RubyScriptProblemFinder.process(this, contents, perWorkingCopyInfo, pm);
 			perWorkingCopyInfo.endReporting();
 		}
 		return unitInfo.isStructureKnown();
 	}
 
 	/*
 	 * Assume that this is a working copy
 	 */
 	protected void updateTimeStamp(RubyScript original) throws RubyModelException {
 		long timeStamp = ((IFile) original.getResource()).getModificationStamp();
 		if (timeStamp == IResource.NULL_STAMP) { throw new RubyModelException(new RubyModelStatus(IRubyModelStatusConstants.INVALID_RESOURCE)); }
 		((RubyScriptElementInfo) getElementInfo()).timestamp = timeStamp;
 	}
 
 	protected IStatus validateRubyScript(IResource resource) {
 		// FIXME Validate the file and name!
 		return RubyModelStatus.VERIFIED_OK;
 	}
 
     /**
      * @see IRubyScript#getElementAt(int)
      */
     public IRubyElement getElementAt(int position) throws RubyModelException {
 
         IRubyElement e= getSourceElementAt(position);
         if (e == this) {
             return null;
         } else {
             return e;
         }
     }
     
 	public String getElementName() {
 		return this.name;
 	}
 
 	/**
 	 * @see IRubyElement
 	 */
 	public IResource getUnderlyingResource() {
 		if (isWorkingCopy() && !isPrimary()) return null;
 		return underlyingFile;
 	}
 
 	public IResource getResource() {
 		return underlyingFile;
 	}
 
 	/*
 	 * @see IOpenable#close
 	 */
 	public void close() throws RubyModelException {
 		if (getPerWorkingCopyInfo() != null) return; // a working copy must
 		// remain opened until it is discarded
 		super.close();
 	}
 
 	/*
 	 * @see Openable#closing
 	 */
 	protected void closing(Object info) {
 		if (getPerWorkingCopyInfo() == null) {
 			super.closing(info);
 		} // else the buffer of a working copy must remain open for the
 		// lifetime of the working copy
 	}
 
 	/*
 	 * @see IRubyScript#getOwner()
 	 */
 	public WorkingCopyOwner getOwner() {
 		return isPrimary() || !isWorkingCopy() ? null : this.owner;
 	}
 
 	/**
 	 * @see IRubyElement#getPath()
 	 */
 	public IPath getPath() {
 		return getResource().getFullPath();
 	}
 
 	/*
 	 * @see IRubyScript#getPrimary()
 	 */
 	public IRubyScript getPrimary() {
 		return (IRubyScript) getPrimaryElement(true);
 	}
 
 	/*
 	 * @see RubyElement#getPrimaryElement(boolean)
 	 */
 	public IRubyElement getPrimaryElement(boolean checkOwner) {
 		if (checkOwner && isPrimary()) return this;
 		return new RubyScript((RubyElement) getParent(), underlyingFile, getElementName(), DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	/*
 	 * (non-Rubydoc)
 	 * 
 	 * @see org.rubypeople.rdt.internal.core.parser.RubyElement#getElementType()
 	 */
 	public int getElementType() {
 		return RubyElement.SCRIPT;
 	}
 
 	/*
 	 * (non-Rubydoc)
 	 * 
 	 * @see org.rubypeople.rdt.core.IRubyScript#reconcile()
 	 */
 	public void reconcile() throws RubyModelException {
 		reconcile(false, null, null);
 	}
 
 	/*
 	 * (non-Rubydoc)
 	 * 
 	 * @see org.rubypeople.rdt.core.IRubyScript#reconcile()
 	 */
 	public void reconcile(boolean forceProblemDetection, WorkingCopyOwner workingCopyOwner, IProgressMonitor monitor) throws RubyModelException {
 		if (!isWorkingCopy()) return; // Reconciling is not supported on non
 		// working copies
 		if (workingCopyOwner == null) workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
 
 		ReconcileWorkingCopyOperation op = new ReconcileWorkingCopyOperation(this, forceProblemDetection, workingCopyOwner);
 		op.runOperation(monitor);
 	}
 
 	public IRubyScript getRubyScript() {
 		return this;
 	}
 
 	public char[] getContents() {
 		try {
 			IBuffer buffer = this.getBuffer();
 			return buffer == null ? null : buffer.getCharacters();
 		} catch (RubyModelException e) {
 			return new char[0];
 		}
 	}
 
 	/*
 	 * (non-Rubydoc)
 	 * 
 	 * @see org.rubypeople.rdt.core.ISourceReference#getSourceRange()
 	 */
 	public ISourceRange getSourceRange() throws RubyModelException {
 		return ((RubyScriptElementInfo) getElementInfo()).getSourceRange();
 	}
 
 	/**
 	 * @see IRubyScript#getType(String)
 	 */
 	public IType getType(String typeName) {
 		return new RubyType(this, typeName);
 	}
 
 	/*
 	 * (non-Rubydoc)
 	 * 
 	 * @see org.rubypeople.rdt.core.ISourceReference#getSource()
 	 */
 	public String getSource() {
 		// TODO Cache the contents and only reload if the file hasn't changed!
 		BufferedReader reader = null;
 		String source = null;
 		try {
 			StringBuffer buffer = new StringBuffer();
 			reader = new BufferedReader(new InputStreamReader(underlyingFile.getContents()));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				buffer.append(line);
 				buffer.append("\n");
 			}
 			source = buffer.toString();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (reader != null) reader.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return source;
 	}
 
 	/**
 	 * @param unitInfo
 	 * @see Openable#openBuffer(IProgressMonitor)
 	 */
 	protected IBuffer openBuffer(IProgressMonitor pm, Object info) throws RubyModelException {
 		boolean isWorkingCopy = isWorkingCopy();
 		IBuffer buffer = isWorkingCopy ? this.owner.createBuffer(this) : BufferManager.getDefaultBufferManager().createBuffer(this);
 		if (buffer == null) return null;
 
 		// set the buffer source
 		if (buffer.getCharacters() == null) {
 			if (isWorkingCopy) {
 				IRubyScript original;
 				if (!isPrimary() && (original = new RubyScript((RubyElement) getParent(), underlyingFile, getElementName(), DefaultWorkingCopyOwner.PRIMARY)).isOpen()) {
 					buffer.setContents(original.getSource());
 				} else {
 					IFile file = (IFile) getResource();
 					if (file == null || !file.exists()) {
 						// initialize buffer with empty contents
 						buffer.setContents(new char[0]);
 					} else {
 						buffer.setContents(Util.getResourceContentsAsCharArray(file));
 					}
 				}
 			} else {
 				IFile file = (IFile) this.getResource();
 				if (file == null || !file.exists()) throw newNotPresentException();
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
 
 	public boolean isPrimary() {
 		return this.owner == DefaultWorkingCopyOwner.PRIMARY;
 	}
 
 	/*
 	 * @see IRubyScript#isWorkingCopy()
 	 */
 	public boolean isWorkingCopy() {
 		return !isPrimary() || getPerWorkingCopyInfo() != null;
 	}
 
 	/*
 	 * Returns the per working copy info for the receiver, or null if none
 	 * exist. Note: the use count of the per working copy info is NOT
 	 * incremented.
 	 */
 	public RubyModelManager.PerWorkingCopyInfo getPerWorkingCopyInfo() {
 		return RubyModelManager.getRubyModelManager().getPerWorkingCopyInfo(this, false/*
 																						 * don't
 																						 * create
 																						 */, false/*
 					 * don't record usage
 					 */, null);
 	}
 
 	/**
 	 * @throws RubyModelException
 	 * @see IRubyScript#getWorkingCopy(IProgressMonitor)
 	 */
 	public IRubyScript getWorkingCopy(IProgressMonitor monitor) throws RubyModelException {
 		return getWorkingCopy(new WorkingCopyOwner() {/*
 														 * non shared working
 														 * copy
 														 */
 		}, null, monitor);
 	}
 
 	/**
 	 * @throws RubyModelException
 	 * @see IRubyScript#getWorkingCopy(WorkingCopyOwner, IProblemRequestor,
 	 *      IProgressMonitor)
 	 */
 	public IRubyScript getWorkingCopy(WorkingCopyOwner workingCopyOwner, IProblemRequestor problemRequestor, IProgressMonitor monitor) throws RubyModelException {
 		if (!isPrimary()) return this;
 
 		RubyModelManager manager = RubyModelManager.getRubyModelManager();
 
 		RubyScript workingCopy = new RubyScript((RubyElement) getParent(), underlyingFile, getElementName(), DefaultWorkingCopyOwner.PRIMARY);
 		RubyModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager.getPerWorkingCopyInfo(workingCopy, false/*
 																													 * don't
 																													 * create
 																													 */, true/*
 					 * record usage
 					 */, null);
 		if (perWorkingCopyInfo != null) { return perWorkingCopyInfo.getWorkingCopy(); // return
 		// existing
 		// handle instead of the
 		// one created above
 		}
 		BecomeWorkingCopyOperation op = new BecomeWorkingCopyOperation(workingCopy, problemRequestor);
 		op.runOperation(monitor);
 		return workingCopy;
 	}
 
 	/**
 	 * 
 	 */
 	public void becomeWorkingCopy(IProblemRequestor requestor, IProgressMonitor monitor) throws RubyModelException {
 		RubyModelManager manager = RubyModelManager.getRubyModelManager();
 		RubyModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager.getPerWorkingCopyInfo(this, false/*
 																											 * don't
 																											 * create
 																											 */, true
 		/* record usage */, null);
 		if (perWorkingCopyInfo == null) {
 			// close cu and its children
 			close();
 			BecomeWorkingCopyOperation operation = new BecomeWorkingCopyOperation(this, requestor);
 			operation.runOperation(monitor);
 		}
 	}
 
 	/**
 	 * @see IRubyScript#commitWorkingCopy(boolean, IProgressMonitor)
 	 */
 	public void commitWorkingCopy(boolean force, IProgressMonitor monitor) throws RubyModelException {
 		CommitWorkingCopyOperation op = new CommitWorkingCopyOperation(this, force);
 		op.runOperation(monitor);
 	}
 
 	/**
 	 * Returns true if this handle represents the same Java element as the given
 	 * handle.
 	 * 
 	 * @see Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj) {
 		if (!(obj instanceof RubyScript)) return false;
 		RubyScript other = (RubyScript) obj;
 		return this.owner.equals(other.owner) && super.equals(obj);
 	}
	
	public int hashCode() {
		return this.underlyingFile.hashCode();
	}
 
 	public boolean exists() {
 		// working copy always exists in the model until it is gotten rid of
 		// (even if not on classpath)
 		if (getPerWorkingCopyInfo() != null) return true;
 
 		// if not a working copy, it exists only if it is a primary compilation
 		// unit
 		return isPrimary();
 	}
 
 	/*
 	 * @see Openable#canBeRemovedFromCache
 	 */
 	public boolean canBeRemovedFromCache() {
 		if (getPerWorkingCopyInfo() != null) return false; // working copies
 		// should remain in
 		// the cache until
 		// they are
 		// destroyed
 		return super.canBeRemovedFromCache();
 	}
 
 	/*
 	 * @see Openable#canBufferBeRemovedFromCache
 	 */
 	public boolean canBufferBeRemovedFromCache(IBuffer buffer) {
 		if (getPerWorkingCopyInfo() != null) return false; // working copy
 		// buffers should
 		// remain in the
 		// cache until
 		// working copy is
 		// destroyed
 		return super.canBufferBeRemovedFromCache(buffer);
 	}
 
 	/**
 	 * @see Openable#hasBuffer()
 	 */
 	protected boolean hasBuffer() {
 		return true;
 	}
 
 	/*
 	 * @see IRubyScript#hasResourceChanged()
 	 */
 	public boolean hasResourceChanged() {
 		if (!isWorkingCopy()) return false;
 
 		// if resource got deleted, then #getModificationStamp() will answer
 		// IResource.NULL_STAMP, which is always different from the cached
 		// timestamp
 		Object info = RubyModelManager.getRubyModelManager().getInfo(this);
 		if (info == null) return false;
 		return ((RubyScriptElementInfo) info).timestamp != getResource().getModificationStamp();
 	}
 
 	/**
 	 * @see IOpenable#isConsistent()
 	 */
 	public boolean isConsistent() {
 		return RubyModelManager.getRubyModelManager().getElementsOutOfSynchWithBuffers().get(this) == null;
 	}
 
 	/**
 	 * @see IOpenable#makeConsistent(IProgressMonitor)
 	 */
 	public void makeConsistent(IProgressMonitor monitor) throws RubyModelException {
 		makeConsistent(false, monitor);
 	}
     
     public RubyScript makeConsistent(boolean createAST, IProgressMonitor monitor) throws RubyModelException {
         if (isConsistent()) return null;
             
         // create a new info and make it the current info
         // (this will remove the info and its children just before storing the new infos)
        // TODO When createAST is specified, actually do it!
 //        if (createAST) {
 //            ASTHolderCUInfo info = new ASTHolderCUInfo();
 //            openWhenClosed(info, monitor);
 //            RubyScript result = info.ast;
 //            info.ast = null;
 //            return result;
 //        } else {
             openWhenClosed(createElementInfo(), monitor);
             return null;
 //        }
     }
 
 	/*
 	 * @see IRubyScript#discardWorkingCopy
 	 */
 	public void discardWorkingCopy() throws RubyModelException {
 		// discard working copy and its children
 		DiscardWorkingCopyOperation op = new DiscardWorkingCopyOperation(this);
 		op.runOperation(null);
 	}
 
 	/**
 	 * @see IOpenable
 	 */
 	public void save(IProgressMonitor pm, boolean force) throws RubyModelException {
 		if (isWorkingCopy()) {
 			// no need to save the buffer for a working copy (this is a noop)
 			reconcile(); // not simply makeConsistent, also computes
 			// fine-grain deltas
 			// in case the working copy is being reconciled already (if not it
 			// would miss
 			// one iteration of deltas).
 		} else {
 			super.save(pm, force);
 		}
 	}
 
 	/**
 	 * @see IRubyScript#getImports()
 	 */
 	public IImportDeclaration[] getImports() throws RubyModelException {
 		IImportContainer container = getImportContainer();
 		if (container.exists()) {
 			IRubyElement[] elements = container.getChildren();
 			IImportDeclaration[] imprts = new IImportDeclaration[elements.length];
 			System.arraycopy(elements, 0, imprts, 0, elements.length);
 			return imprts;
 		} else if (!exists()) {
 			throw newNotPresentException();
 		} else {
 			return new IImportDeclaration[0];
 		}
 	}
 
 	/**
 	 * @see IRubyScript#getImport(String)
 	 */
 	public IImportDeclaration getImport(String importName) {
 		return new RubyImport((ImportContainer) getImportContainer(), importName);
 	}
 
 	/**
 	 * @see IRubyScript#getImportContainer()
 	 */
 	public IImportContainer getImportContainer() {
 		return new ImportContainer(this);
 	}
 	
 	/**
 	 * @see ICompilationUnit#getTypes()
 	 */
 	public IType[] getTypes() throws RubyModelException {
 		ArrayList list = getChildrenOfType(TYPE);
 		IType[] array= new IType[list.size()];
 		list.toArray(array);
 		return array;
 	}
 	
 	/**
 	 * @see IRubyScript#findPrimaryType()
 	 */
 	public IType findPrimaryType() {
 		String typeName = Util.getNameWithoutRubyLikeExtension(getElementName());
 		IType primaryType= getType(typeName);
 		if (primaryType.exists()) {
 			return primaryType;
 		}
 		return null;
 	}
 	
 	/**
 	 * @see ICodeAssist#codeSelect(int, int)
 	 */
 	public IRubyElement[] codeSelect(int offset, int length) throws RubyModelException {
 		return codeSelect(offset, length, DefaultWorkingCopyOwner.PRIMARY);
 	}
 	/**
 	 * @see ICodeAssist#codeSelect(int, int, WorkingCopyOwner)
 	 */
 	public IRubyElement[] codeSelect(int offset, int length, WorkingCopyOwner workingCopyOwner) throws RubyModelException {
 		return super.codeSelect(this, offset, length, workingCopyOwner);
 	}
 }
