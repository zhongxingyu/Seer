 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation
  *     xored software, Inc. - Search All occurences bugfix, 
  *     						  hilight only class name when class is in search results ( Alex Panchenko <alex@xored.com>)
  *******************************************************************************/
 package org.eclipse.dltk.core.search.matching;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.declarations.FieldDeclaration;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.compiler.env.INameEnvironment;
 import org.eclipse.dltk.compiler.env.ISourceType;
 import org.eclipse.dltk.compiler.env.lookup.Scope;
 import org.eclipse.dltk.compiler.util.SimpleLookupTable;
 import org.eclipse.dltk.compiler.util.SimpleSet;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.BasicSearchEngine;
 import org.eclipse.dltk.core.search.FieldDeclarationMatch;
 import org.eclipse.dltk.core.search.FieldReferenceMatch;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.IMatchLocatorParser;
 import org.eclipse.dltk.core.search.MethodDeclarationMatch;
 import org.eclipse.dltk.core.search.MethodReferenceMatch;
 import org.eclipse.dltk.core.search.SearchDocument;
 import org.eclipse.dltk.core.search.SearchMatch;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.TypeDeclarationMatch;
 import org.eclipse.dltk.core.search.TypeReferenceMatch;
 import org.eclipse.dltk.core.search.index.Index;
 import org.eclipse.dltk.internal.compiler.env.AccessRestriction;
 import org.eclipse.dltk.internal.compiler.impl.ITypeRequestor;
 import org.eclipse.dltk.internal.compiler.lookup.LookupEnvironment;
 import org.eclipse.dltk.internal.compiler.lookup.SourceModuleScope;
 import org.eclipse.dltk.internal.core.ArchiveProjectFragment;
 import org.eclipse.dltk.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.internal.core.ExternalSourceModule;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.ModelStatus;
 import org.eclipse.dltk.internal.core.NameLookup;
 import org.eclipse.dltk.internal.core.Openable;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.dltk.internal.core.SourceModule;
 import org.eclipse.dltk.internal.core.SourceRefElement;
 import org.eclipse.dltk.internal.core.search.DLTKSearchDocument;
 import org.eclipse.dltk.internal.core.search.IndexQueryRequestor;
 import org.eclipse.dltk.internal.core.search.IndexSelector;
 import org.eclipse.dltk.internal.core.search.matching.AndPattern;
 import org.eclipse.dltk.internal.core.search.matching.InternalSearchPattern;
 import org.eclipse.dltk.internal.core.search.matching.MatchingNodeSet;
 import org.eclipse.dltk.internal.core.search.matching.PossibleMatchSet;
 import org.eclipse.dltk.internal.core.util.HandleFactory;
 import org.eclipse.dltk.internal.core.util.Util;
 
 public class MatchLocator implements ITypeRequestor {
 	public static final int MAX_AT_ONCE;
 	static {
 		long maxMemory = Runtime.getRuntime().maxMemory();
 		int ratio = (int) Math.round(((double) maxMemory) / (64 * 0x100000));
 		switch (ratio) {
 		case 0:
 		case 1:
 			MAX_AT_ONCE = 100;
 			break;
 		case 2:
 			MAX_AT_ONCE = 200;
 			break;
 		case 3:
 			MAX_AT_ONCE = 300;
 			break;
 		default:
 			MAX_AT_ONCE = 400;
 			break;
 		}
 	}
 
 	// permanent state
 	public SearchPattern pattern;
 
 	public PatternLocator patternLocator;
 
 	public int matchContainer;
 
 	public SearchRequestor requestor;
 
 	public IDLTKSearchScope scope;
 
 	public IProgressMonitor progressMonitor;
 
 	public org.eclipse.dltk.core.ISourceModule[] workingCopies;
 
 	public HandleFactory handleFactory;
 
 	// cache of all super type names if scope is hierarchy scope
 	public char[][][] allSuperTypeNames;
 
 	// the following is valid for the current project
 	public IMatchLocatorParser parser;
 
 	// private Parser basicParser;
 	public INameEnvironment nameEnvironment;
 
 	public NameLookup nameLookup;
 
 	public LookupEnvironment lookupEnvironment;
 
 	// management of PossibleMatch to be processed
 	public int numberOfMatches; // (numberOfMatches - 1) is the last unit in
 
 	// matchesToProcess
 	public PossibleMatch[] matchesToProcess;
 
 	public PossibleMatch currentPossibleMatch;
 
 	/*
 	 * Time spent in the IJavaSearchResultCollector
 	 */
 	public long resultCollectorTime = 0;
 
 	// Progress information
 	protected int progressStep;
 
 	protected int progressWorked;
 
 	// Binding resolution and cache
 	protected SourceModuleScope unitScope;
 
 	protected SimpleLookupTable bindings;
 
 	// Cache for handles
 	private HashSet handles;
 
 	public static class WorkingCopyDocument extends DLTKSearchDocument {
 		public org.eclipse.dltk.core.ISourceModule workingCopy;
 
 		WorkingCopyDocument(org.eclipse.dltk.core.ISourceModule workingCopy,
 				SearchParticipant participant, boolean external) {
 			super(workingCopy.getPath().toString(), getContents(workingCopy),
 					participant, external, workingCopy.getScriptProject()
 							.getProject());
 			this.workingCopy = workingCopy;
 		}
 
 		private static char[] getContents(
 				org.eclipse.dltk.core.ISourceModule workingCopy) {
 			try {
 				return workingCopy.getSourceAsCharArray();
 			} catch (ModelException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 				return new char[0];
 			}
 		}
 
 		public String toString() {
 			return "WorkingCopyDocument for " + getPath(); //$NON-NLS-1$
 		}
 	}
 
 	static public class WrappedCoreException extends RuntimeException {
 		private static final long serialVersionUID = 8354329870126121212L; // backward
 
 		// compatible
 		public CoreException coreException;
 
 		public WrappedCoreException(CoreException coreException) {
 			this.coreException = coreException;
 		}
 	}
 
 	public static SearchDocument[] addWorkingCopies(
 			InternalSearchPattern pattern, SearchDocument[] indexMatches,
 			org.eclipse.dltk.core.ISourceModule[] copies,
 			SearchParticipant participant) {
 		// working copies take precedence over corresponding compilation units
 		HashMap workingCopyDocuments = workingCopiesThatCanSeeFocus(copies,
 				pattern.focus, pattern.isPolymorphicSearch(), participant);
 		SearchDocument[] matches = null;
 		int length = indexMatches.length;
 		for (int i = 0; i < length; i++) {
 			SearchDocument searchDocument = indexMatches[i];
 			if (searchDocument.getParticipant() == participant) {
 				SearchDocument workingCopyDocument = (SearchDocument) workingCopyDocuments
 						.remove(searchDocument.getPath());
 				if (workingCopyDocument != null) {
 					if (matches == null) {
 						System
 								.arraycopy(indexMatches, 0,
 										matches = new SearchDocument[length],
 										0, length);
 					}
 					matches[i] = workingCopyDocument;
 				}
 			}
 		}
 		if (matches == null) { // no working copy
 			matches = indexMatches;
 		}
 		int remainingWorkingCopiesSize = workingCopyDocuments.size();
 		if (remainingWorkingCopiesSize != 0) {
 			System.arraycopy(matches, 0, matches = new SearchDocument[length
 					+ remainingWorkingCopiesSize], 0, length);
 			Iterator iterator = workingCopyDocuments.values().iterator();
 			int index = length;
 			while (iterator.hasNext()) {
 				matches[index++] = (SearchDocument) iterator.next();
 			}
 		}
 		return matches;
 	}
 
 	public static void setFocus(InternalSearchPattern pattern,
 			IModelElement focus) {
 		pattern.focus = focus;
 	}
 
 	/*
 	 * Returns the working copies that can see the given focus.
 	 */
 	private static HashMap workingCopiesThatCanSeeFocus(
 			org.eclipse.dltk.core.ISourceModule[] copies, IModelElement focus,
 			boolean isPolymorphicSearch, SearchParticipant participant) {
 		if (copies == null)
 			return new HashMap();
 		if (focus != null) {
 			while (!(focus instanceof IScriptProject)
 					&& !(focus instanceof ArchiveProjectFragment)) {
 				focus = focus.getParent();
 			}
 		}
 		HashMap result = new HashMap();
 		for (int i = 0, length = copies.length; i < length; i++) {
 			org.eclipse.dltk.core.ISourceModule workingCopy = copies[i];
 			IPath projectOrArchive = MatchLocator.getProjectOrArchive(
 					workingCopy).getPath();
 			if (focus == null
 					|| IndexSelector.canSeeFocus(focus, isPolymorphicSearch,
 							projectOrArchive)) {
 				boolean external = false;
 				IProjectFragment frag = (IProjectFragment) workingCopy
 						.getAncestor(IModelElement.PROJECT_FRAGMENT);
 				if (frag != null) {
 					external = frag.isExternal();
 				}
 
 				result.put(workingCopy.getPath().toString(),
 						new WorkingCopyDocument(workingCopy, participant,
 								external));
 			}
 		}
 		return result;
 	}
 
 	public static SearchPattern createAndPattern(
 			final SearchPattern leftPattern, final SearchPattern rightPattern) {
 		Assert.isNotNull(leftPattern.getToolkit());
 		Assert.isTrue(leftPattern.getToolkit()
 				.equals(rightPattern.getToolkit()));
 		return new AndPattern(0/* no kind */, 0/* no rule */, leftPattern
 				.getToolkit()) {
 			SearchPattern current = leftPattern;
 
 			public SearchPattern currentPattern() {
 				return this.current;
 			}
 
 			protected boolean hasNextQuery() {
 				if (this.current == leftPattern) {
 					this.current = rightPattern;
 					return true;
 				}
 				return false;
 			}
 
 			protected void resetQuery() {
 				this.current = leftPattern;
 			}
 		};
 	}
 
 	/**
 	 * Query a given index for matching entries. Assumes the sender has opened
 	 * the index and will close when finished.
 	 */
 	public static void findIndexMatches(InternalSearchPattern pattern,
 			Index index, IndexQueryRequestor requestor,
 			SearchParticipant participant, IDLTKSearchScope scope,
 			IProgressMonitor monitor) throws IOException {
 		pattern.findIndexMatches(index, requestor, participant, scope, monitor);
 	}
 
 	public static IModelElement getProjectOrArchive(IModelElement element) {
 		while (!(element instanceof IScriptProject)
 				&& !(element instanceof ArchiveProjectFragment)) {
 			element = element.getParent();
 		}
 		return element;
 	}
 
 	public static boolean isPolymorphicSearch(InternalSearchPattern pattern) {
 		return pattern.isPolymorphicSearch();
 	}
 
 	public static IModelElement projectOrArchiveFocus(
 			InternalSearchPattern pattern) {
 		return pattern == null || pattern.focus == null ? null
 				: getProjectOrArchive(pattern.focus);
 	}
 
 	public MatchLocator(SearchPattern pattern, SearchRequestor requestor,
 			IDLTKSearchScope scope, IProgressMonitor progressMonitor) {
 		this.pattern = pattern;
 		this.patternLocator = PatternLocator.patternLocator(this.pattern, scope
 				.getLanguageToolkit());
 		this.matchContainer = this.patternLocator.matchContainer();
 		this.requestor = requestor;
 		this.scope = scope;
 		this.progressMonitor = progressMonitor;
 	}
 
 	/**
 	 * Add an additional compilation unit into the loop -> build compilation
 	 * unit declarations, their bindings and record their results.
 	 */
 	public void accept(ISourceModule sourceUnit,
 			AccessRestriction accessRestriction) {
 	}
 
 	/**
 	 * Add additional source types
 	 */
 	public void accept(ISourceType[] sourceTypes,
 			AccessRestriction accessRestriction) {
 	}
 
 	/*
 	 * / Computes the super type names of the focus type if any.
 	 */
 	protected char[][][] computeSuperTypeNames(IType focusType) {
 		return null;
 	}
 
 	/**
 	 * Creates an IMethod from the given method declaration and type.
 	 */
 	protected IModelElement createHandle(MethodDeclaration method,
 			IModelElement parent) {
 		// if (!(parent instanceof IType)) return parent;
 		if (parent instanceof IType) {
 			IType type = (IType) parent;
 			return createMethodHandle(type, method.getName());
 		} else if (parent instanceof ISourceModule) {
 			return createMethodHandle((ISourceModule) parent, method.getName());
 		}
 		return null;
 	}
 
 	/**
 	 * Creates an IMethod from the given method declaration and type.
 	 */
 	protected IModelElement createHandle(FieldDeclaration field,
 			IModelElement parent) {
 		// if (!(parent instanceof IType)) return parent;
 		if (parent instanceof IType) {
 			IType type = (IType) parent;
 			return createFieldHandle(type, field.getName());
 		} else if (parent instanceof ISourceModule) {
 			return createFieldHandle((ISourceModule) parent, field.getName());
 		}
 		return null;
 	}
 
 	/*
 	 * Create method handle. Store occurences for create handle to retrieve
 	 * possible duplicate ones.
 	 */
 	protected IModelElement createMethodHandle(IType type, String methodName) {
 		IMethod methodHandle = type.getMethod(methodName);
 		resolveDuplicates(methodHandle);
 		return methodHandle;
 	}
 
 	/**
 	 * Increment the {@link SourceRefElement#occurrenceCount} until the
 	 * specified handle is unique.
 	 * 
 	 * @param handle
 	 */
 	protected void resolveDuplicates(IMember handle) {
 		if (handle instanceof SourceRefElement) {
 			while (this.handles.contains(handle)) {
 				((SourceRefElement) handle).occurrenceCount++;
 			}
 			this.handles.add(handle);
 		}
 	}
 
 	protected IModelElement createTypeHandle(IType parent, String name) {
 		final IType typeHandle = parent.getType(name);
 		resolveDuplicates(typeHandle);
 		return typeHandle;
 	}
 
 	/*
 	 * Create method handle. Store occurrences for create handle to retrieve
 	 * possible duplicate ones.
 	 */
 	protected IModelElement createMethodHandle(ISourceModule module,
 			String methodName) {
 
 		IMethod methodHandle = module.getMethod(methodName);
 		resolveDuplicates(methodHandle);
 		return methodHandle;
 	}
 
 	/*
 	 * Create method handle. Store occurences for create handle to retrieve
 	 * possible duplicate ones.
 	 */
 	protected IModelElement createFieldHandle(IType type, String methodName) {
 		IField fieldHandle = type.getField(methodName);
 		resolveDuplicates(fieldHandle);
 		return fieldHandle;
 	}
 
 	/*
 	 * Create method handle. Store occurences for create handle to retrieve
 	 * possible duplicate ones.
 	 */
 	protected IModelElement createFieldHandle(ISourceModule module,
 			String methodName) {
 
 		IField fieldHandle = module.getField(methodName);
 		resolveDuplicates(fieldHandle);
 		return fieldHandle;
 	}
 
 	/**
 	 * Creates an IType from the given simple top level type name.
 	 */
 	protected IType createTypeHandle(String simpleTypeName) {
 		Openable openable = this.currentPossibleMatch.openable;
 		IType type = null;
 		if (openable instanceof SourceModule)
 			type = ((SourceModule) openable).getType(simpleTypeName);
 		else if (openable instanceof ExternalSourceModule) {
 			type = ((ExternalSourceModule) openable).getType(simpleTypeName);
 		} else if (openable instanceof BuiltinSourceModule) {
 			type = ((BuiltinSourceModule) openable).getType(simpleTypeName);
 		}
 		resolveDuplicates(type);
 		return type;
 	}
 
 	/**
 	 * Creates an IType from the given simple top level type name.
 	 */
 	protected ISourceModule createSourceModuleHandle() {
 		Openable openable = this.currentPossibleMatch.openable;
 		if (openable instanceof ISourceModule)
 			return ((ISourceModule) openable);
 		return null;
 	}
 
 	/**
 	 * Creates an IType from the given simple top level type name.
 	 */
 	protected IMethod createMethodHandle(String simpleTypeName) {
 		Openable openable = this.currentPossibleMatch.openable;
 		IMethod method = null;
 		if (openable instanceof SourceModule)
 			method = ((SourceModule) openable).getMethod(simpleTypeName);
 		if (openable instanceof ExternalSourceModule) {
 			method = ((ExternalSourceModule) openable)
 					.getMethod(simpleTypeName);
 		}
 		resolveDuplicates(method);
 		return method;
 	}
 
 	/**
 	 * Creates an IType from the given simple top level type name.
 	 */
 	protected IField createFieldHandle(String simpleTypeName) {
 		Openable openable = this.currentPossibleMatch.openable;
 		IField field;
		if (openable instanceof SourceModule)
 			field = ((SourceModule) openable).getField(simpleTypeName);
		if (openable instanceof ExternalSourceModule) {
 			field = ((ExternalSourceModule) openable).getField(simpleTypeName);
 		} else {
 			field = null;
 		}
 		resolveDuplicates(field);
 		return field;
 	}
 
 	protected boolean encloses(IModelElement element) {
 		return element != null && this.scope.encloses(element);
 	}
 
 	protected void getMethodBodies(ModuleDeclaration unit,
 			MatchingNodeSet nodeSet) {
 
 		try {
 			this.parser.setNodeSet(nodeSet);
 			this.parser.parseBodies(unit);
 		} finally {
 			this.parser.setNodeSet(null);
 		}
 	}
 
 	/**
 	 * Create a new parser for the given project, as well as a lookup
 	 * environment.
 	 */
 	public void initialize(ScriptProject project, int possibleMatchSize)
 			throws ModelException {
 		// clean up name environment only if there are several possible match as
 		// it is
 		// reused
 		// when only one possible match (bug 58581)
 		if (this.nameEnvironment != null && possibleMatchSize != 1)
 			this.nameEnvironment.cleanup();
 
 		ISearchableEnvironment searchableEnvironment = project
 				.newSearchableNameEnvironment(this.workingCopies);
 
 		// if only one possible match, a file name environment costs too much,
 		// so use the existing searchable environment which will populate the
 		// scriptmodel
 		// only for this possible match and its required types.
 
 		this.nameEnvironment = possibleMatchSize == 1 ? (INameEnvironment) searchableEnvironment
 				: null;// (INameEnvironment)
 
 		this.lookupEnvironment = new LookupEnvironment(this, /* problemReporter, */
 		this.nameEnvironment);
 
 		IDLTKLanguageToolkit tk = null;
 		tk = DLTKLanguageManager.getLanguageToolkit(project);
 		if (tk == null) {
 			throw new ModelException(new ModelStatus(
 					IModelStatusConstants.INVALID_PROJECT, project,
 					Messages.MatchLocator_languageToolkitNotFoundForProject));
 		}
 		this.parser = DLTKLanguageManager.createMatchParser(tk.getNatureId(),
 				this);
 
 		// remember project's name lookup
 		this.nameLookup = searchableEnvironment.getNameLookup();
 
 		// initialize queue of units
 		this.numberOfMatches = 0;
 		this.matchesToProcess = new PossibleMatch[possibleMatchSize];
 	}
 
 	protected void locateMatches(ScriptProject scriptProject,
 			PossibleMatch[] possibleMatches, int start, int length)
 			throws CoreException {
 		initialize(scriptProject, length);
 		// create and resolve binding (equivalent to beginCompilation() in
 		// Compiler)
 		for (int i = start, maxUnits = start + length; i < maxUnits; i++) {
 			PossibleMatch possibleMatch = possibleMatches[i];
 			try {
 				if (!parse(possibleMatch))
 					continue;
 				if (this.progressMonitor != null) {
 					this.progressWorked++;
 					if ((this.progressWorked % this.progressStep) == 0)
 						this.progressMonitor.worked(this.progressStep);
 				}
 				process(possibleMatch);
 				if (this.numberOfMatches > 0
 						&& this.matchesToProcess[this.numberOfMatches - 1] == possibleMatch) {
 					// forget last possible match as it was processed
 					this.numberOfMatches--;
 				}
 			} finally {
 				possibleMatch.cleanUp();
 			}
 		}
 	}
 
 	private boolean parse(PossibleMatch possibleMatch) {
 		if (this.progressMonitor != null && this.progressMonitor.isCanceled())
 			throw new OperationCanceledException();
 
 		try {
 			if (BasicSearchEngine.VERBOSE)
 				System.out
 						.println("Parsing " + possibleMatch.openable.toStringWithAncestors()); //$NON-NLS-1$
 
 			this.parser.setNodeSet(possibleMatch.nodeSet);
 			ModuleDeclaration parsedUnit = this.parser.parse(possibleMatch);
 			if (parsedUnit != null) {
 
 				// if (hasAlreadyDefinedType(parsedUnit)) return false; // skip
 				// type has it is hidden so not visible
 				getMethodBodies(parsedUnit, possibleMatch.nodeSet);
 
 				// add the possibleMatch with its parsedUnit to matchesToProcess
 				possibleMatch.parsedUnit = parsedUnit;
 				int size = this.matchesToProcess.length;
 				if (this.numberOfMatches == size)
 					System
 							.arraycopy(
 									this.matchesToProcess,
 									0,
 									this.matchesToProcess = new PossibleMatch[size == 0 ? 1
 											: size * 2], 0,
 									this.numberOfMatches);
 				this.matchesToProcess[this.numberOfMatches++] = possibleMatch;
 			}
 		} finally {
 			this.parser.setNodeSet(null);
 		}
 		return true;
 	}
 
 	/**
 	 * Locate the matches amongst the possible matches.
 	 */
 	protected void locateMatches(ScriptProject scriptProject,
 			PossibleMatchSet matchSet, int expected) throws CoreException {
 		PossibleMatch[] possibleMatches = matchSet
 				.getPossibleMatches(scriptProject.getProjectFragments());
 		int length = possibleMatches.length;
 		// increase progress from duplicate matches not stored in matchSet while
 		// adding...
 		if (this.progressMonitor != null && expected > length) {
 			this.progressWorked += expected - length;
 			this.progressMonitor.worked(expected - length);
 		}
 		// locate matches (processed matches are limited to avoid problem while
 		// using Interpreter default memory heap size)
 		for (int index = 0; index < length;) {
 			int max = Math.min(MAX_AT_ONCE, length - index);
 			locateMatches(scriptProject, possibleMatches, index, max);
 			index += max;
 		}
 		this.patternLocator.clear();
 	}
 
 	/**
 	 * Locate the matches in the given files and report them using the search
 	 * requestor.
 	 */
 	public void locateMatches(SearchDocument[] searchDocuments)
 			throws CoreException {
 		int docsLength = searchDocuments.length;
 		if (BasicSearchEngine.VERBOSE) {
 			System.out.println("Locating matches in documents ["); //$NON-NLS-1$
 			for (int i = 0; i < docsLength; i++)
 				System.out.println("\t" + searchDocuments[i]); //$NON-NLS-1$
 			System.out.println("]"); //$NON-NLS-1$
 		}
 		// init infos for progress increasing
 		int n = docsLength < 1000 ? Math.min(Math.max(docsLength / 200 + 1, 2),
 				4) : 5 * (docsLength / 1000);
 		this.progressStep = docsLength < n ? 1 : docsLength / n; // step
 		// should
 		// not be 0
 		this.progressWorked = 0;
 		// extract working copies
 		ArrayList copies = new ArrayList();
 		for (int i = 0; i < docsLength; i++) {
 			SearchDocument document = searchDocuments[i];
 			if (document instanceof WorkingCopyDocument) {
 				copies.add(((WorkingCopyDocument) document).workingCopy);
 			}
 		}
 		int copiesLength = copies.size();
 		this.workingCopies = new org.eclipse.dltk.core.ISourceModule[copiesLength];
 		copies.toArray(this.workingCopies);
 		ModelManager manager = ModelManager.getModelManager();
 		this.bindings = new SimpleLookupTable();
 		try {
 			// optimize access to zip files during search operation
 			manager.cacheZipFiles();
 			// initialize handle factory (used as a cache of handles so as to
 			// optimize space)
 			if (this.handleFactory == null)
 				this.handleFactory = new HandleFactory();
 			if (this.progressMonitor != null) {
 				this.progressMonitor.beginTask("", searchDocuments.length); //$NON-NLS-1$
 			}
 			// initialize pattern for polymorphic search (ie. method reference
 			// pattern)
 			this.patternLocator.initializePolymorphicSearch(this);
 			ScriptProject previousScriptProject = null;
 			PossibleMatchSet matchSet = new PossibleMatchSet();
 			Util.sort(searchDocuments, new Util.Comparer() {
 				public int compare(Object a, Object b) {
 					return ((SearchDocument) a).getPath().compareTo(
 							((SearchDocument) b).getPath());
 				}
 			});
 			int displayed = 0; // progress worked displayed
 			String previousPath = null;
 			for (int i = 0; i < docsLength; i++) {
 				if (this.progressMonitor != null
 						&& this.progressMonitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				// skip duplicate paths
 				SearchDocument searchDocument = searchDocuments[i];
 				searchDocuments[i] = null; // free current document
 				String pathString = searchDocument.getPath();
 				if (i > 0 && pathString.equals(previousPath)) {
 					if (this.progressMonitor != null) {
 						this.progressWorked++;
 						if ((this.progressWorked % this.progressStep) == 0)
 							this.progressMonitor.worked(this.progressStep);
 					}
 					displayed++;
 					continue;
 				}
 				previousPath = pathString;
 				Openable openable;
 				org.eclipse.dltk.core.ISourceModule workingCopy = null;
 				if (searchDocument instanceof WorkingCopyDocument) {
 					workingCopy = ((WorkingCopyDocument) searchDocument).workingCopy;
 					openable = (Openable) workingCopy;
 				} else {
 					openable = this.handleFactory.createOpenable(pathString,
 							this.scope);
 				}
 				if (openable == null) {
 					if (this.progressMonitor != null) {
 						this.progressWorked++;
 						if ((this.progressWorked % this.progressStep) == 0)
 							this.progressMonitor.worked(this.progressStep);
 					}
 					displayed++;
 					continue; // match is outside buildpath
 				}
 				// create new parser and lookup environment if this is a new
 				// project
 				IResource resource = null;
 				ScriptProject scriptProject = (ScriptProject) openable
 						.getScriptProject();
 				resource = workingCopy != null ? workingCopy.getResource()
 						: openable.getResource();
 				if (resource == null)
 					resource = scriptProject.getProject(); // case of a file in
 				// an external jar
 				if (!scriptProject.equals(previousScriptProject)) {
 					// locate matches in previous project
 					if (previousScriptProject != null) {
 						try {
 							locateMatches(previousScriptProject, matchSet, i
 									- displayed);
 							displayed = i;
 						} catch (ModelException e) {
 							// problem with buildpath in this project -> skip it
 							DLTKCore.error("error in locateMatches", e); //$NON-NLS-1$
 						}
 						matchSet.reset();
 					}
 					previousScriptProject = scriptProject;
 				}
 				matchSet.add(new PossibleMatch(this, resource, openable,
 						searchDocument));
 			}
 			// last project
 			if (previousScriptProject != null) {
 				try {
 					locateMatches(previousScriptProject, matchSet, docsLength
 							- displayed);
 				} catch (ModelException e) {
 					// problem with buildpath in last project -> ignore
 					DLTKCore.error("error in locateMatches", e); //$NON-NLS-1$
 				}
 			}
 			if (this.progressMonitor != null)
 				this.progressMonitor.done();
 		} finally {
 			if (this.nameEnvironment != null)
 				this.nameEnvironment.cleanup();
 			manager.flushZipFiles();
 			this.bindings = null;
 		}
 	}
 
 	public ISourceModule[] locateModules(SearchDocument[] searchDocuments) {
 		List modules = new ArrayList();
 		int docsLength = searchDocuments.length;
 		if (BasicSearchEngine.VERBOSE) {
 			System.out.println("Locating matches in documents ["); //$NON-NLS-1$
 			for (int i = 0; i < docsLength; i++)
 				System.out.println("\t" + searchDocuments[i]); //$NON-NLS-1$
 			System.out.println("]"); //$NON-NLS-1$
 		}
 		// init infos for progress increasing
 		int n = docsLength < 1000 ? Math.min(Math.max(docsLength / 200 + 1, 2),
 				4) : 5 * (docsLength / 1000);
 		this.progressStep = docsLength < n ? 1 : docsLength / n; // step
 		// should
 		// not be 0
 		this.progressWorked = 0;
 		// extract working copies
 		ArrayList copies = new ArrayList();
 		for (int i = 0; i < docsLength; i++) {
 			SearchDocument document = searchDocuments[i];
 			if (document instanceof WorkingCopyDocument) {
 				copies.add(((WorkingCopyDocument) document).workingCopy);
 			}
 		}
 		int copiesLength = copies.size();
 		this.workingCopies = new org.eclipse.dltk.core.ISourceModule[copiesLength];
 		copies.toArray(this.workingCopies);
 		ModelManager manager = ModelManager.getModelManager();
 		this.bindings = new SimpleLookupTable();
 		try {
 			// optimize access to zip files during search operation
 			manager.cacheZipFiles();
 			// initialize handle factory (used as a cache of handles so as to
 			// optimize space)
 			if (this.handleFactory == null)
 				this.handleFactory = new HandleFactory();
 			if (this.progressMonitor != null) {
 				this.progressMonitor.beginTask("", searchDocuments.length); //$NON-NLS-1$
 			}
 			// initialize pattern for polymorphic search (ie. method reference
 			// pattern)
 			this.patternLocator.initializePolymorphicSearch(this);
 			Util.sort(searchDocuments, new Util.Comparer() {
 				public int compare(Object a, Object b) {
 					return ((SearchDocument) a).getPath().compareTo(
 							((SearchDocument) b).getPath());
 				}
 			});
 			int displayed = 0; // progress worked displayed
 			String previousPath = null;
 			for (int i = 0; i < docsLength; i++) {
 				if (this.progressMonitor != null
 						&& this.progressMonitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				// skip duplicate paths
 				SearchDocument searchDocument = searchDocuments[i];
 				searchDocuments[i] = null; // free current document
 				String pathString = searchDocument.getPath();
 				if (i > 0 && pathString.equals(previousPath)) {
 					if (this.progressMonitor != null) {
 						this.progressWorked++;
 						if ((this.progressWorked % this.progressStep) == 0)
 							this.progressMonitor.worked(this.progressStep);
 					}
 					displayed++;
 					continue;
 				}
 				previousPath = pathString;
 				Openable openable;
 				org.eclipse.dltk.core.ISourceModule workingCopy = null;
 				if (searchDocument instanceof WorkingCopyDocument) {
 					workingCopy = ((WorkingCopyDocument) searchDocument).workingCopy;
 					openable = (Openable) workingCopy;
 				} else {
 					openable = this.handleFactory.createOpenable(pathString,
 							this.scope);
 				}
 				if (openable == null) {
 					if (this.progressMonitor != null) {
 						this.progressWorked++;
 						if ((this.progressWorked % this.progressStep) == 0)
 							this.progressMonitor.worked(this.progressStep);
 					}
 					displayed++;
 					continue; // match is outside buildpath
 				}
 				// create new parser and lookup environment if this is a new
 				// project
 				IResource resource = null;
 				ScriptProject scriptProject = (ScriptProject) openable
 						.getScriptProject();
 				resource = workingCopy != null ? workingCopy.getResource()
 						: openable.getResource();
 				if (resource == null)
 					resource = scriptProject.getProject(); // case of a file in
 				if (!modules.contains(openable)) {
 					modules.add(openable);
 				}
 			}
 			if (this.progressMonitor != null)
 				this.progressMonitor.done();
 		} finally {
 			if (this.nameEnvironment != null)
 				this.nameEnvironment.cleanup();
 			manager.flushZipFiles();
 			this.bindings = null;
 		}
 		return (ISourceModule[]) modules.toArray(new ISourceModule[modules
 				.size()]);
 	}
 
 	public SearchMatch newDeclarationMatch(IModelElement element, int accuracy,
 			int offset, int length) {
 		SearchParticipant participant = getParticipant();
 		IResource resource = this.currentPossibleMatch.resource;
 		return newDeclarationMatch(element, accuracy, offset, length,
 				participant, resource);
 	}
 
 	public SearchMatch newDeclarationMatch(IModelElement element, int accuracy,
 			int offset, int length, SearchParticipant participant,
 			IResource resource) {
 		switch (element.getElementType()) {
 		case IModelElement.TYPE:
 			return new TypeDeclarationMatch(element, accuracy, offset, length,
 					participant, resource);
 		case IModelElement.FIELD:
 			return new FieldDeclarationMatch(element, accuracy, offset, length,
 					participant, resource);
 
 		case IModelElement.METHOD:
 			return new MethodDeclarationMatch(element, accuracy, offset,
 					length, participant, resource);
 		default:
 			return null;
 		}
 	}
 
 	public SearchMatch newFieldReferenceMatch(IModelElement enclosingElement,
 			int accuracy, int offset, int length, ASTNode reference) {
 		SearchParticipant participant = getParticipant();
 		IResource resource = this.currentPossibleMatch.resource;
 		boolean insideDocComment = false;
 		boolean isReadAccess = false;
 		boolean isWriteAccess = false;
 
 		if (enclosingElement instanceof IParent
 				&& reference instanceof FieldDeclaration) {
 			IParent parent = (IParent) enclosingElement;
 			IModelElement[] children;
 			try {
 				FieldDeclaration decl = (FieldDeclaration) reference;
 				children = parent.getChildren();
 				boolean found = false;
 				for (int i = 0; i < children.length; i++) {
 					if (children[i].getElementName().equals(decl.getName())
 							&& children[i] instanceof IField) {
 						enclosingElement = children[i];
 						found = true;
 						break;
 					}
 				}
 				if (!found) {
 					return null;
 				}
 			} catch (ModelException e) {
 				return null;
 			}
 		}
 		return new FieldReferenceMatch(enclosingElement, reference, accuracy,
 				offset, length, isReadAccess, isWriteAccess, insideDocComment,
 				participant, resource);
 	}
 
 	public SearchMatch newMethodReferenceMatch(IModelElement enclosingElement,
 			int accuracy, int offset, int length, boolean isConstructor,
 			boolean isSynthetic, ASTNode reference) {
 		SearchParticipant participant = getParticipant();
 		IResource resource = this.currentPossibleMatch.resource;
 		return new MethodReferenceMatch(enclosingElement, accuracy, offset,
 				length, isConstructor, isSynthetic, false, participant,
 				resource, reference);
 	}
 
 	public TypeReferenceMatch newTypeReferenceMatch(
 			IModelElement enclosingElement, int accuracy, int offset,
 			int length, ASTNode reference) {
 		SearchParticipant participant = getParticipant();
 		IResource resource = this.currentPossibleMatch.resource;
 		return new TypeReferenceMatch(enclosingElement, accuracy, offset,
 				length, false, participant, resource);
 	}
 
 	public TypeReferenceMatch newTypeReferenceMatch(
 			IModelElement enclosingElement, int accuracy, ASTNode reference) {
 		return newTypeReferenceMatch(enclosingElement, accuracy, reference
 				.sourceStart(), reference.sourceEnd() - reference.sourceStart()
 				+ 1, reference);
 	}
 
 	/*
 	 * Process a compilation unit already parsed and build.
 	 */
 	protected void process(PossibleMatch possibleMatch) throws CoreException {
 		this.currentPossibleMatch = possibleMatch;
 		ModuleDeclaration unit = possibleMatch.parsedUnit;
 		try {
 			if (unit == null || unit.isEmpty()) {
 				return;
 			}
 			reportMatching(unit);
 		} finally {
 			this.currentPossibleMatch = null;
 		}
 	}
 
 	public SearchParticipant getParticipant() {
 		return this.currentPossibleMatch.document.getParticipant();
 	}
 
 	protected void report(SearchMatch match) throws CoreException {
 		long start = -1;
 		if (BasicSearchEngine.VERBOSE) {
 			start = System.currentTimeMillis();
 			System.out.println("Reporting match"); //$NON-NLS-1$
 			System.out.println("\tResource: " + match.getResource());//$NON-NLS-1$
 			System.out
 					.println("\tPositions: [offset=" + match.getOffset() + ", length=" + match.getLength() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			try {
 				ModelElement modelElement = (ModelElement) match.getElement();
 				System.out
 						.println("\tJava element: " + modelElement.toStringWithAncestors()); //$NON-NLS-1$
 				if (!modelElement.exists())
 					System.out
 							.println("\t\tWARNING: this element does NOT exist!"); //$NON-NLS-1$
 			} catch (Exception e) {
 				// it's just for debug purposes... ignore all exceptions in this
 				// area
 			}
 			if (match instanceof TypeReferenceMatch) {
 				try {
 					TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) match;
 					ModelElement local = (ModelElement) typeRefMatch
 							.getLocalElement();
 					if (local != null) {
 						System.out
 								.println("\tLocal element: " + local.toStringWithAncestors()); //$NON-NLS-1$
 					}
 					IModelElement[] others = typeRefMatch.getOtherElements();
 					if (others != null) {
 						int length = others.length;
 						if (length > 0) {
 							System.out.println("\tOther elements:"); //$NON-NLS-1$
 							for (int i = 0; i < length; i++) {
 								ModelElement other = (ModelElement) others[i];
 								System.out
 										.println("\t\t- " + other.toStringWithAncestors()); //$NON-NLS-1$
 							}
 						}
 					}
 				} catch (Exception e) {
 					// it's just for debug purposes... ignore all exceptions in
 					// this area
 				}
 			}
 			System.out
 					.println(match.getAccuracy() == SearchMatch.A_ACCURATE ? "\tAccuracy: EXACT_MATCH" //$NON-NLS-1$
 							: "\tAccuracy: POTENTIAL_MATCH"); //$NON-NLS-1$
 			System.out.print("\tRule: "); //$NON-NLS-1$
 			if (match.isExact()) {
 				System.out.println("EXACT"); //$NON-NLS-1$
 			} else if (match.isEquivalent()) {
 				System.out.println("EQUIVALENT"); //$NON-NLS-1$
 			} else if (match.isErasure()) {
 				System.out.println("ERASURE"); //$NON-NLS-1$
 			} else {
 				System.out.println("INVALID RULE"); //$NON-NLS-1$
 			}
 			System.out.println("\tRaw: " + match.isRaw()); //$NON-NLS-1$
 		}
 		if (this.requestor != null) {
 			this.requestor.acceptSearchMatch(match);
 		}
 		if (BasicSearchEngine.VERBOSE)
 			this.resultCollectorTime += System.currentTimeMillis() - start;
 	}
 
 	private void resolvePotentialMatches(MatchingNodeSet nodeSet) {
 		Object[] nodes = nodeSet.possibleMatchingNodesSet.values;
 		for (int i = 0, l = nodes.length; i < l; i++) {
 			ASTNode node = (ASTNode) nodes[i];
 			if (node != null) {
 				nodeSet.addMatch(node, PatternLocator.ACCURATE_MATCH);
 				/**
 				 * FIXME originally it was
 				 * 
 				 * <pre>
 				 * nodeSet.addMatch(node, this.patternLocator.resolveLevel(node));
 				 * </pre>
 				 * 
 				 * but resolveLevel() are not ported
 				 */
 			}
 		}
 		nodeSet.possibleMatchingNodesSet = new SimpleSet(3);
 		if (BasicSearchEngine.VERBOSE) {
 			int size = nodeSet.matchingNodes == null ? 0
 					: nodeSet.matchingNodes.elementSize;
 			System.out.print("	- node set: accurate=" + size); //$NON-NLS-1$
 			size = nodeSet.possibleMatchingNodesSet == null ? 0
 					: nodeSet.possibleMatchingNodesSet.elementSize;
 			System.out.println(", possible=" + size); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Visit the given resolved parse tree and report the nodes that match the
 	 * search pattern.
 	 */
 	protected void reportMatching(ModuleDeclaration unit) throws CoreException {
 		MatchingNodeSet nodeSet = this.currentPossibleMatch.nodeSet;
 
 		if (BasicSearchEngine.VERBOSE) {
 			System.out.println("Report matching: "); //$NON-NLS-1$
 			int size = nodeSet.matchingNodes == null ? 0
 					: nodeSet.matchingNodes.elementSize;
 			System.out.print("	- node set: accurate=" + size); //$NON-NLS-1$
 			size = nodeSet.possibleMatchingNodesSet == null ? 0
 					: nodeSet.possibleMatchingNodesSet.elementSize;
 			System.out.println(", possible=" + size); //$NON-NLS-1$			
 
 		}
 
 		/*
 		 * move the possible matching nodes that exactly match the search
 		 * pattern to the matching nodes set
 		 */
 		resolvePotentialMatches(nodeSet);
 
 		this.unitScope = null;
 		if (nodeSet.matchingNodes.elementSize == 0)
 			return; // no matching nodes were found
 		this.handles = new HashSet();
 		boolean matchedUnitContainer = (this.matchContainer & PatternLocator.COMPILATION_UNIT_CONTAINER) != 0;
 		// report references in javadoc
 
 		TypeDeclaration[] types = unit.getTypes();
 		if (types != null) {
 			for (int i = 0, l = types.length; i < l; i++) {
 				if (nodeSet.matchingNodes.elementSize == 0)
 					return; // reported all the matching nodes
 				TypeDeclaration type = types[i];
 				Integer level = (Integer) nodeSet.matchingNodes
 						.removeKey(parser.processType(type));
 				int accuracy = (level != null && matchedUnitContainer) ? level
 						.intValue() : -1;
 				reportMatching(type, null, accuracy, nodeSet, 1);
 			}
 		}
 		// Visit functions
 		MethodDeclaration[] methods = unit.getFunctions();
 		if (methods != null) {
 			for (int i = 0, l = methods.length; i < l; i++) {
 				if (nodeSet.matchingNodes.elementSize == 0)
 					return; // reported all the matching nodes
 				MethodDeclaration method = methods[i];
 				Integer level = (Integer) nodeSet.matchingNodes
 						.removeKey(parser.processMethod(method));
 				int accuracy = (level != null && matchedUnitContainer) ? level
 						.intValue() : -1;
 				reportMatching(unit, method, null, accuracy, nodeSet);
 			}
 		}
 		// Visit global variables
 		FieldDeclaration[] fields = unit.getVariables();
 		if (fields != null) {
 			for (int i = 0, l = fields.length; i < l; i++) {
 				if (nodeSet.matchingNodes.elementSize == 0)
 					return; // reported all the matching nodes
 				FieldDeclaration method = fields[i];
 				Integer level = (Integer) nodeSet.matchingNodes
 						.removeKey(method);
 				int accuracy = (level != null && matchedUnitContainer) ? level
 						.intValue() : -1;
 				reportMatching(unit, method, null, accuracy, nodeSet);
 			}
 		}
 
 		// references in this module
 		ASTNode[] nodes = nodeSet.matchingNodes(unit.sourceStart(), unit
 				.sourceEnd());
 		if (nodes != null) {
 			if ((this.matchContainer & PatternLocator.COMPILATION_UNIT_CONTAINER) != 0) {
 				ISourceModule enclosingElement = createSourceModuleHandle();
 				if (encloses(enclosingElement)) {
 					for (int i = 0, l = nodes.length; i < l; i++) {
 						ASTNode node = nodes[i];
 						Integer level = (Integer) nodeSet.matchingNodes
 								.removeKey(node);
 						if (DLTKCore.DEBUG) {
 							System.out
 									.println("TODO: Searching. Add scope support."); //$NON-NLS-1$
 						}
 						this.patternLocator.matchReportReference(node,
 								enclosingElement, (Scope) null, level
 										.intValue(), this);
 
 					}
 				}
 			}
 			for (int i = 0, l = nodes.length; i < l; i++)
 				nodeSet.matchingNodes.removeKey(nodes[i]);
 		}
 
 		// Clear handle cache
 		this.handles = null;
 		this.bindings.removeKey(this.pattern);
 	}
 
 	/**
 	 * type Visit the given type declaration and report the nodes that match
 	 * exactly the search pattern (ie. the ones in the matching nodes set)
 	 */
 	protected void reportMatching(TypeDeclaration type, IModelElement parent,
 			int accuracy, MatchingNodeSet nodeSet, int occurrenceCount)
 			throws CoreException {
 		// create type handle
 		IModelElement enclosingElement = parent;
 		if (enclosingElement == null) {
 			enclosingElement = createTypeHandle(type.getName());
 		} else if (enclosingElement instanceof IType) {
 			enclosingElement = createTypeHandle((IType) parent, type.getName());
 		} else if (enclosingElement instanceof IMember) {
 			IMember member = (IMember) parent;
 			enclosingElement = member.getType(type.getName(), occurrenceCount);
 		}
 		if (enclosingElement == null)
 			return;
 		boolean enclosesElement = encloses(enclosingElement);
 		// report the type declaration
 		if (accuracy > -1 && enclosesElement) {
 			SearchMatch match = this.patternLocator.newDeclarationMatch(type,
 					enclosingElement, accuracy, this);
 			report(match);
 		}
 		boolean matchedClassContainer = (this.matchContainer & PatternLocator.CLASS_CONTAINER) != 0;
 
 		// filter out element not in hierarchy scope
 		if (DLTKCore.DEBUG) {
 			System.out
 					.println("TODO: Searching. add variable handling here..."); //$NON-NLS-1$
 		}
 
 		boolean typeInHierarchy = true;// type.binding == null ||
 
 		// Visit methods
 		MethodDeclaration[] methods = type.getMethods();
 		if (methods != null) {
 			if (nodeSet.matchingNodes.elementSize == 0)
 				return; // end as all matching nodes were reported
 			for (int i = 0, l = methods.length; i < l; i++) {
 				MethodDeclaration method = methods[i];
 				Integer level = (Integer) nodeSet.matchingNodes
 						.removeKey(parser.processMethod(method));
 				int value = (level != null && matchedClassContainer) ? level
 						.intValue() : -1;
 				reportMatching(type, method, enclosingElement, value, true,
 						nodeSet);
 			}
 		}
 		// Visit types
 		TypeDeclaration[] memberTypes = type.getTypes();
 		if (memberTypes != null) {
 			for (int i = 0, l = memberTypes.length; i < l; i++) {
 				if (nodeSet.matchingNodes.elementSize == 0)
 					return; // end as all matching nodes were reported
 				TypeDeclaration memberType = memberTypes[i];
 				Integer level = (Integer) nodeSet.matchingNodes
 						.removeKey(parser.processType(memberType));
 				int value = (level != null && matchedClassContainer) ? level
 						.intValue() : -1;
 				reportMatching(memberType, enclosingElement, value, nodeSet, 1);
 			}
 		}
 
 		// Visit variables
 		FieldDeclaration[] fields = type.getVariables();
 		if (fields != null) {
 			for (int i = 0, l = fields.length; i < l; i++) {
 				if (nodeSet.matchingNodes.elementSize == 0)
 					return; // reported all the matching nodes
 				FieldDeclaration field = fields[i];
 				Integer level = (Integer) nodeSet.matchingNodes
 						.removeKey(field);
 				int value = (level != null && matchedClassContainer) ? level
 						.intValue() : -1;
 				reportMatching(type, field, enclosingElement, value, true,
 						nodeSet);
 			}
 		}
 
 		// references in this type
 		if (typeInHierarchy) {
 			ASTNode[] nodes = nodeSet.matchingNodes(type.sourceStart(), type
 					.sourceEnd());
 			if (nodes != null) {
 				if ((this.matchContainer & PatternLocator.METHOD_CONTAINER) != 0) {
 					if (encloses(enclosingElement)) {
 						for (int i = 0, l = nodes.length; i < l; i++) {
 							ASTNode node = nodes[i];
 							Integer level = (Integer) nodeSet.matchingNodes
 									.removeKey(node);
 							if (DLTKCore.DEBUG) {
 								System.out
 										.println("TODO: Searching. Add scope support."); //$NON-NLS-1$
 							}
 							this.patternLocator.matchReportReference(node,
 									enclosingElement, (Scope) null, level
 											.intValue(), this);
 						}
 						return;
 					}
 				}
 				for (int i = 0, l = nodes.length; i < l; i++)
 					nodeSet.matchingNodes.removeKey(nodes[i]);
 			}
 		}
 	}
 
 	/**
 	 * Visit the given method declaration and report the nodes that match
 	 * exactly the search pattern (ie. the ones in the matching nodes set) Note
 	 * that the method declaration has already been checked.
 	 */
 	protected void reportMatching(TypeDeclaration type,
 			MethodDeclaration method, IModelElement parent, int accuracy,
 			boolean typeInHierarchy, MatchingNodeSet nodeSet)
 			throws CoreException {
 		IModelElement enclosingElement = null;
 		if (accuracy > -1) {
 			enclosingElement = createHandle(method, parent);
 			if (enclosingElement != null) { // skip if unable to find method
 				if (encloses(enclosingElement)) {
 					SearchMatch match = null;
 					if (DLTKCore.DEBUG) {
 						System.out
 								.println("TODO: AST Add constructor support."); //$NON-NLS-1$
 					}
 					match = this.patternLocator.newDeclarationMatch(method,
 							enclosingElement, accuracy, this);
 					// }
 					if (match != null) {
 						report(match);
 					}
 				}
 			}
 		}
 
 		// references in this method
 		if (typeInHierarchy) {
 			ASTNode[] nodes = nodeSet.matchingNodes(method.sourceStart(),
 					method.sourceEnd());
 			if (nodes != null) {
 				if ((this.matchContainer & PatternLocator.CLASS_CONTAINER) != 0) {
 					if (enclosingElement == null)
 						enclosingElement = createHandle(method, parent);
 					if (encloses(enclosingElement)) {
 						for (int i = 0, l = nodes.length; i < l; i++) {
 							ASTNode node = nodes[i];
 							Integer level = (Integer) nodeSet.matchingNodes
 									.removeKey(node);
 							if (DLTKCore.DEBUG) {
 								System.out
 										.println("TODO: Searching. Add scope support."); //$NON-NLS-1$
 							}
 							this.patternLocator.matchReportReference(node,
 									enclosingElement, (Scope) null, level
 											.intValue(), this);
 						}
 						return;
 					}
 				}
 				for (int i = 0, l = nodes.length; i < l; i++)
 					nodeSet.matchingNodes.removeKey(nodes[i]);
 			}
 		}
 	}
 
 	/**
 	 * Visit the given method declaration and report the nodes that match
 	 * exactly the search pattern (ie. the ones in the matching nodes set) Note
 	 * that the method declaration has already been checked.
 	 */
 	protected void reportMatching(TypeDeclaration type, FieldDeclaration field,
 			IModelElement parent, int accuracy, boolean typeInHierarchy,
 			MatchingNodeSet nodeSet) throws CoreException {
 		IModelElement enclosingElement = null;
 		if (accuracy > -1) {
 			enclosingElement = createHandle(field, parent);
 			if (enclosingElement != null) { // skip if unable to find method
 				if (encloses(enclosingElement)) {
 					SearchMatch match = null;
 					if (DLTKCore.DEBUG) {
 						System.out
 								.println("TODO: AST Add constructor support."); //$NON-NLS-1$
 					}
 					match = this.patternLocator.newDeclarationMatch(field,
 							enclosingElement, accuracy, this);
 					// }
 					if (match != null) {
 						report(match);
 					}
 				}
 			}
 		}
 
 		// references in this method
 		if (typeInHierarchy) {
 			ASTNode[] nodes = nodeSet.matchingNodes(field.sourceStart(), field
 					.sourceEnd());
 			if (nodes != null) {
 				if ((this.matchContainer & PatternLocator.CLASS_CONTAINER) != 0) {
 					if (enclosingElement == null)
 						enclosingElement = createHandle(field, parent);
 					if (encloses(enclosingElement)) {
 						for (int i = 0, l = nodes.length; i < l; i++) {
 							ASTNode node = nodes[i];
 							Integer level = (Integer) nodeSet.matchingNodes
 									.removeKey(node);
 							if (DLTKCore.DEBUG) {
 								System.out
 										.println("TODO: Searching. Add scope support."); //$NON-NLS-1$
 							}
 							this.patternLocator.matchReportReference(node,
 									enclosingElement, (Scope) null, level
 											.intValue(), this);
 						}
 						return;
 					}
 				}
 				for (int i = 0, l = nodes.length; i < l; i++)
 					nodeSet.matchingNodes.removeKey(nodes[i]);
 			}
 		}
 	}
 
 	/**
 	 * Visit the given method declaration and report the nodes that match
 	 * exactly the search pattern (ie. the ones in the matching nodes set) Note
 	 * that the method declaration has already been checked.
 	 */
 	protected void reportMatching(ModuleDeclaration module,
 			FieldDeclaration field, IModelElement parent, int accuracy,
 			MatchingNodeSet nodeSet) throws CoreException {
 		IModelElement enclosingElement = null;
 		if (accuracy > -1) {
 			if (parent == null) {
 				parent = createSourceModuleHandle();
 			}
 			enclosingElement = createHandle(field, parent);
 			if (enclosingElement == null) {
 				enclosingElement = createFieldHandle(field.getName());
 			}
 			if (enclosingElement != null) { // skip if unable to find method
 				if (encloses(enclosingElement)) {
 					SearchMatch match = null;
 					if (DLTKCore.DEBUG) {
 						System.out
 								.println("TODO: AST Add constructor support."); //$NON-NLS-1$
 					}
 					match = this.patternLocator.newDeclarationMatch(field,
 							enclosingElement, accuracy, this);
 					// }
 					if (match != null) {
 						report(match);
 					}
 				}
 			}
 		}
 
 		// references in this method
 		ASTNode[] nodes = nodeSet.matchingNodes(field.sourceStart(), field
 				.sourceEnd());
 		if (nodes != null) {
 			if (parent == null) {
 				parent = createSourceModuleHandle();
 			}
 			if ((this.matchContainer & PatternLocator.METHOD_CONTAINER) != 0) {
 				if (enclosingElement == null)
 					enclosingElement = createHandle(field, parent);
 				if (encloses(enclosingElement)) {
 					for (int i = 0, l = nodes.length; i < l; i++) {
 						ASTNode node = nodes[i];
 						Integer level = (Integer) nodeSet.matchingNodes
 								.removeKey(node);
 						if (DLTKCore.DEBUG) {
 							System.out
 									.println("TODO: Searching. Add scope support."); //$NON-NLS-1$
 						}
 						this.patternLocator.matchReportReference(node,
 								enclosingElement, (Scope) null, level
 										.intValue(), this);
 					}
 					return;
 				}
 			}
 			for (int i = 0, l = nodes.length; i < l; i++)
 				nodeSet.matchingNodes.removeKey(nodes[i]);
 		}
 	}
 
 	protected void reportMatching(ModuleDeclaration module,
 			MethodDeclaration method, IModelElement parent, int accuracy,
 			MatchingNodeSet nodeSet) throws CoreException {
 		IModelElement enclosingElement = null;
 		if (accuracy > -1) {
 			if (parent == null) {
 				parent = createSourceModuleHandle();
 			}
 			enclosingElement = createHandle(method, parent);
 			if (enclosingElement == null) {
 				enclosingElement = createMethodHandle(method.getName());
 			}
 			if (enclosingElement != null) { // skip if unable to find method
 				if (encloses(enclosingElement)) {
 					SearchMatch match = null;
 					if (DLTKCore.DEBUG) {
 						System.out
 								.println("TODO: AST Add constructor support."); //$NON-NLS-1$
 					}
 					match = this.patternLocator.newDeclarationMatch(method,
 							enclosingElement, accuracy, this);
 					// }
 					if (match != null) {
 						report(match);
 					}
 				}
 			}
 		}
 
 		// references in this method
 		ASTNode[] nodes = nodeSet.matchingNodes(method.sourceStart(), method
 				.sourceEnd());
 		if (nodes != null) {
 			if (parent == null) {
 				parent = createSourceModuleHandle();
 			}
 			if ((this.matchContainer & PatternLocator.METHOD_CONTAINER) != 0) {
 				if (enclosingElement == null)
 					enclosingElement = createHandle(method, parent);
 				if (encloses(enclosingElement)) {
 					for (int i = 0, l = nodes.length; i < l; i++) {
 						ASTNode node = nodes[i];
 						Integer level = (Integer) nodeSet.matchingNodes
 								.removeKey(node);
 						if (DLTKCore.DEBUG) {
 							System.out
 									.println("TODO: Searching. Add scope support."); //$NON-NLS-1$
 						}
 						this.patternLocator.matchReportReference(node,
 								enclosingElement, (Scope) null, level
 										.intValue(), this);
 					}
 					return;
 				}
 			}
 			for (int i = 0, l = nodes.length; i < l; i++)
 				nodeSet.matchingNodes.removeKey(nodes[i]);
 		}
 	}
 
 	protected IType findTypeFrom(IModelElement[] childs, String name,
 			String parentName, char delimiter) {
 		try {
 			for (int i = 0; i < childs.length; ++i) {
 				if (childs[i] instanceof IType) {
 					IType type = (IType) childs[i];
 					String qname = name + delimiter + type.getElementName();
 					if (qname.equals(parentName)) {
 						return type;
 					}
 					IType val = findTypeFrom(type.getChildren(), qname,
 							parentName, delimiter);
 					if (val != null) {
 						return val;
 					}
 				}
 			}
 		} catch (ModelException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 }
