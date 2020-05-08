 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.search.indexing;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.CRC32;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.compiler.util.SimpleLookupTable;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.BasicSearchEngine;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchDocument;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.index.Index;
 import org.eclipse.dltk.core.search.index.MixinIndex;
 import org.eclipse.dltk.internal.core.Model;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.dltk.internal.core.search.PatternSearchJob;
 import org.eclipse.dltk.internal.core.search.processing.IJob;
 import org.eclipse.dltk.internal.core.search.processing.JobManager;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 
 public class IndexManager extends JobManager implements IIndexConstants {
 
 	public SimpleLookupTable indexLocations = new SimpleLookupTable();
 	/*
 	 * key = an IPath, value = an Index
 	 */
 	private Map indexes = new HashMap(5);
 	/* need to save ? */
 	private boolean needToSave = false;
 	private static final CRC32 checksumCalculator = new CRC32();
 	private IPath javaPluginLocation = null;
 	/* can only replace a current state if its less than the new one */
 	private SimpleLookupTable indexStates = null;
 	private File savedIndexNamesFile = new File(
 			getScriptPluginWorkingLocation()
 					.append("savedIndexNames.txt").toOSString()); //$NON-NLS-1$
 	public static Integer SAVED_STATE = new Integer(0);
 	public static Integer UPDATING_STATE = new Integer(1);
 	public static Integer UNKNOWN_STATE = new Integer(2);
 	public static Integer REBUILDING_STATE = new Integer(3);
 
 	public static List sourcesToMixin = new ArrayList();
 
 	public synchronized void aboutToUpdateIndex(IPath containerPath,
 			Integer newIndexState) {
 		// newIndexState is either UPDATING_STATE or REBUILDING_STATE
 		// must tag the index as inconsistent, in case we exit before the update
 		// job is started
 		String indexLocation = computeIndexLocation(containerPath);
 		Object state = getIndexStates().get(indexLocation);
 		Integer currentIndexState = state == null ? UNKNOWN_STATE
 				: (Integer) state;
 		if (currentIndexState.equals(REBUILDING_STATE))
 			return; // already rebuilding the index
 		int compare = newIndexState.compareTo(currentIndexState);
 		if (compare > 0) {
 			// so UPDATING_STATE replaces SAVED_STATE and REBUILDING_STATE
 			// replaces everything
 			updateIndexState(indexLocation, newIndexState);
 		} else if (compare < 0 && this.indexes.get(indexLocation) == null) {
 			// if already cached index then there is nothing more to do
 			rebuildIndex(indexLocation, containerPath);
 		}
 	}
 
 	// /**
 	// * Trigger addition of a resource to an index Note: the actual operation
 	// is
 	// * performed in background
 	// */
 	// public void addExternal(IFile resource, IPath containerPath) {
 	// if (DLTKCore.getPlugin() == null)
 	// return;
 	// SearchParticipant participant =
 	// SearchEngine.getDefaultSearchParticipant();
 	// SearchDocument document =
 	// participant.getDocument(resource.getFullPath().toString());
 	// String indexLocation = computeIndexLocation(containerPath);
 	// scheduleDocumentIndexing(document, containerPath, indexLocation,
 	// participant);
 	// }
 
 	/**
 	 * Trigger addition of a resource to an index Note: the actual operation is
 	 * performed in background
 	 */
 	public void addSource(IFile resource, IPath containerPath,
 			ISourceElementParser parser, SourceIndexerRequestor requestor,
 			IDLTKLanguageToolkit toolkit) {
 		if (DLTKCore.getPlugin() == null)
 			return;
 		SearchParticipant participant = SearchEngine
 				.getDefaultSearchParticipant();
 		SearchDocument document = participant.getDocument(resource
 				.getFullPath().toString());
 		((InternalSearchDocument) document).parser = parser;
 		((InternalSearchDocument) document).requestor = requestor;
 		((InternalSearchDocument) document).toolkit = toolkit;
 		String indexLocation = computeIndexLocation(containerPath);
 		scheduleDocumentIndexing(document, containerPath, indexLocation,
 				participant);
 	}
 
 	/**
 	 * Trigger addition of a resource to an index Note: the actual operation is
 	 * performed in background
 	 */
 	public void addBinary(IFile resource, IPath containerPath) {
 		if (DLTKCore.getPlugin() == null)
 			return;
 		SearchParticipant participant = SearchEngine
 				.getDefaultSearchParticipant();
 		SearchDocument document = participant.getDocument(resource
 				.getFullPath().toString());
 		String indexLocation = computeIndexLocation(containerPath);
 		scheduleDocumentIndexing(document, containerPath, indexLocation,
 				participant);
 	}
 
 	/*
 	 * Removes unused indexes from disk.
 	 */
 	public void cleanUpIndexes() {
 		SimpleLookupTable knownPaths = new SimpleLookupTable();
 		IDLTKSearchScope scope = BasicSearchEngine.createWorkspaceScope(null);
 		PatternSearchJob job = new PatternSearchJob(null, SearchEngine
 				.getDefaultSearchParticipant(), scope, null);
 		Index[] selectedIndexes = job.getIndexes(null);
 		for (int j = 0, max = selectedIndexes.length; j < max; j++) {
 			// TODO should use getJavaPluginWorkingLocation()+index simple name
 			// to avoid bugs such as
 			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=62267
 			String path = selectedIndexes[j].getIndexFile().getAbsolutePath();
 			knownPaths.put(path, path);
 		}
 		if (indexStates != null) {
 			Object[] keys = indexStates.keyTable;
 			int keysLength = keys.length;
 			int updates = 0;
 			String locations[] = new String[keysLength];
 			for (int i = 0, l = keys.length; i < l; i++) {
 				String key = (String) keys[i];
 				if (key != null && !knownPaths.containsKey(key)) {
 					locations[updates++] = key;
 				}
 			}
 			if (updates > 0) {
 				removeIndexesState(locations);
 			}
 		}
 		File indexesDirectory = new File(getScriptPluginWorkingLocation()
 				.toOSString());
 		if (indexesDirectory.isDirectory()) {
 			File[] indexesFiles = indexesDirectory.listFiles();
 			if (indexesFiles != null) {
 				for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
 					String fileName = indexesFiles[i].getAbsolutePath();
 					if (!knownPaths.containsKey(fileName)
 							&& fileName.toLowerCase().endsWith(".index")) { //$NON-NLS-1$
 						if (VERBOSE)
 							Util
 									.verbose("Deleting index file " + indexesFiles[i]); //$NON-NLS-1$
 						indexesFiles[i].delete();
 					}
 				}
 			}
 		}
 	}
 
 	public String computeIndexLocation(IPath containerPath) {
 		String indexLocation = (String) this.indexLocations.get(containerPath);
 		if (indexLocation == null) {
 			String pathString = containerPath.toOSString();
 			checksumCalculator.reset();
 			checksumCalculator.update(pathString.getBytes());
 			String fileName = Long.toString(checksumCalculator.getValue())
 					+ ".index"; //$NON-NLS-1$
 			if (VERBOSE)
 				Util
 						.verbose("-> index name for " + pathString + " is " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
 			indexLocation = getScriptPluginWorkingLocation().append(fileName)
 					.toOSString();
 			this.indexLocations.put(containerPath, indexLocation);
 		}
 		return indexLocation;
 	}
 
 	/*
 	 * Creates an empty index at the given location, for the given container
 	 * path, if none exist.
 	 */
 	public void ensureIndexExists(String indexLocation, IPath containerPath) {
 		SimpleLookupTable states = getIndexStates();
 		Object state = states.get(indexLocation);
 		if (state == null) {
 			updateIndexState(indexLocation, REBUILDING_STATE);
 			getIndex(containerPath, indexLocation, true, true);
 		}
 	}
 
 	public SourceIndexerRequestor getSourceRequestor(
 			IScriptProject scriptProject) {
 		IDLTKLanguageToolkit toolkit = null;
 		try {
 			toolkit = DLTKLanguageManager.getLanguageToolkit(scriptProject);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		if (toolkit != null) {
 			return DLTKLanguageManager.createSourceRequestor(toolkit
 					.getNatureId());
 		}
 		return null;
 	}
 
 	public ISourceElementParser getSourceElementParser(IScriptProject project,
 			ISourceElementRequestor requestor) {
 		// disable task tags to speed up parsing
 		// Map options = project.getOptions(true);
 		// options.put(DLTKCore.COMPILER_TASK_TAGS, ""); //$NON-NLS-1$
 		IDLTKLanguageToolkit toolkit = null;
 		try {
 			toolkit = DLTKLanguageManager.getLanguageToolkit(project);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (toolkit != null) {
 			ISourceElementParser parser = null;
 			try {
 				parser = DLTKLanguageManager.getSourceElementParser(toolkit
 						.getNatureId());
 				parser.setRequestor(requestor);
 			} catch (CoreException e) {
 			}
 			return parser;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the index for a given project, according to the following
 	 * algorithm: - if index is already in memory: answers this one back - if
 	 * (reuseExistingFile) then read it and return this index and record it in
 	 * memory - if (createIfMissing) then create a new empty index and record it
 	 * in memory
 	 * 
 	 * Warning: Does not check whether index is consistent (not being used)
 	 */
 	public synchronized Index getIndex(IPath containerPath,
 			boolean reuseExistingFile, boolean createIfMissing) {
 		String indexLocation = computeIndexLocation(containerPath);
 		return getIndex(containerPath, indexLocation, reuseExistingFile,
 				createIfMissing);
 	}
 
 	/**
 	 * This indexes aren't required to be rebuilded.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public synchronized Index getSpecialIndex(String id, String path,
 			String containerPath/* , IProject project */) {
 		// String containerPath = project.getFullPath().toOSString();
 		// String path = project.getFullPath();
 
 		boolean mixin = id.equals("mixin");
 
 		String indexLocation = computeIndexLocation(new Path("#special#" + id
 				+ "#" + path));
 
 		Index index = (Index) indexes.get(indexLocation);
 
 		if (index == null) {
 			Object state = getIndexStates().get(indexLocation);
 			Integer currentIndexState = state == null ? UNKNOWN_STATE
 					: (Integer) state;
 			// index isn't cached, consider reusing an existing index file
 
 			File indexFile = new File(indexLocation);
 			if (indexFile.exists()) { // check before creating index so as
 				// to avoid creating a new empty
 				// index if file is missing
 				try {
 					if (mixin)
 						index = new MixinIndex(indexLocation, containerPath,
 								true
 						/* reuse index file */
 						);
 					else
 						index = new Index(indexLocation, containerPath, true /*
 																				 * reuse
 																				 * index
 																				 * file
 																				 */);
 					indexes.put(indexLocation, index);
 					return index;
 				} catch (IOException e) {
 					// failed to read the existing file or its no longer
 					// compatible
 					if (currentIndexState != REBUILDING_STATE) { // rebuild
 						/*
 						 * index if existing file is corrupt, unless the index
 						 * is already being rebuilt
 						 */
 						if (VERBOSE)
 							Util
 									.verbose("-> cannot reuse existing index: " + indexLocation + " path: " + id); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 					/* index = null; */// will fall thru to createIfMissing
 					// & create a empty index for the
 					// rebuild all job to populate
 				}
 			}
 
 			// index wasn't found on disk, consider creating an empty new one
 
 			try {
 				if (VERBOSE)
 					Util
 							.verbose("-> create empty index: " + indexLocation + " path: " + id); //$NON-NLS-1$ //$NON-NLS-2$
 
 				if (mixin)
 					index = new MixinIndex(indexLocation, containerPath, false
 					/*
 					 * do not reuse index file
 					 */
 					);
 				else
 					index = new Index(indexLocation, containerPath, false /*
 																			 * do
 																			 * not
 																			 * reuse
 																			 * index
 																			 * file
 																			 */);
 				indexes.put(indexLocation, index);
 				return index;
 			} catch (IOException e) {
 				if (VERBOSE)
 					Util
 							.verbose("-> unable to create empty index: " + indexLocation + " path: " + containerPath); //$NON-NLS-1$ //$NON-NLS-2$
 				// The file could not be created. Possible reason: the
 				// project has been deleted.
 				return null;
 			}
 		}
 		// System.out.println(" index name: " + path.toOSString() + " <----> " +
 		// index.getIndexFile().getName());
 		updateIndexState(indexLocation, REBUILDING_STATE);
 		return index;
 	}
 
 	/**
 	 * Returns the index for a given project, according to the following
 	 * algorithm: - if index is already in memory: answers this one back - if
 	 * (reuseExistingFile) then read it and return this index and record it in
 	 * memory - if (createIfMissing) then create a new empty index and record it
 	 * in memory
 	 * 
 	 * Warning: Does not check whether index is consistent (not being used)
 	 */
 	public synchronized Index getIndex(IPath containerPath,
 			String indexLocation, boolean reuseExistingFile,
 			boolean createIfMissing) {
 		boolean mixin = containerPath.toString().startsWith("#special#mixin#");
 		// Path is already canonical per construction
 		Index index = (Index) indexes.get(indexLocation);
 		if (index == null) {
 			Object state = getIndexStates().get(indexLocation);
 			Integer currentIndexState = state == null ? UNKNOWN_STATE
 					: (Integer) state;
 			if (currentIndexState.equals(UNKNOWN_STATE)) {
 				// should only be reachable for query jobs
 				// IF you put an index in the cache, then AddArchiveFileToIndex
 				// fails because it thinks there is nothing to do
 				rebuildIndex(indexLocation, containerPath);
				if( !mixin )
 				return null;
 			}
 			// index isn't cached, consider reusing an existing index file
 			String containerPathString = containerPath.getDevice() == null ? containerPath
 					.toString()
 					: containerPath.toOSString();
 			if (reuseExistingFile) {
 				File indexFile = new File(indexLocation);
 				if (indexFile.exists()) { // check before creating index so as
 					// to avoid creating a new empty
 					// index if file is missing
 					try {
 						if (mixin)
 							index = new MixinIndex(indexLocation,
 									containerPathString, true /*
 																 * reuse index
 																 * file
 																 */);
 						else
 							index = new Index(indexLocation,
 									containerPathString, true /*
 																 * reuse index
 																 * file
 																 */);
 						indexes.put(indexLocation, index);
 						return index;
 					} catch (IOException e) {
 						// failed to read the existing file or its no longer
 						// compatible
 						if (currentIndexState != REBUILDING_STATE) { // rebuild
 							/*
 							 * index if existing file is corrupt, unless the
 							 * index is already being rebuilt
 							 */
 							if (VERBOSE)
 								Util
 										.verbose("-> cannot reuse existing index: " + indexLocation + " path: " + containerPathString); //$NON-NLS-1$ //$NON-NLS-2$
 							rebuildIndex(indexLocation, containerPath);
 							return null;
 						}
 						/* index = null; */// will fall thru to createIfMissing
 						// & create a empty index for the
 						// rebuild all job to populate
 					}
 				}
 				if (currentIndexState == SAVED_STATE) { // rebuild index if
 					// existing file is
 					// missing
 					rebuildIndex(indexLocation, containerPath);
 					return null;
 				}
 			}
 			// index wasn't found on disk, consider creating an empty new one
 			if (createIfMissing) {
 				try {
 					if (VERBOSE)
 						Util
 								.verbose("-> create empty index: " + indexLocation + " path: " + containerPathString); //$NON-NLS-1$ //$NON-NLS-2$
 					if (mixin)
 						index = new MixinIndex(indexLocation,
 								containerPathString, false /*
 															 * do not reuse
 															 * index file
 															 */);
 					else
 						index = new Index(indexLocation, containerPathString,
 								false /*
 										 * do not reuse index file
 										 */);
 					indexes.put(indexLocation, index);
 					return index;
 				} catch (IOException e) {
 					if (VERBOSE)
 						Util
 								.verbose("-> unable to create empty index: " + indexLocation + " path: " + containerPathString); //$NON-NLS-1$ //$NON-NLS-2$
 					// The file could not be created. Possible reason: the
 					// project has been deleted.
 					return null;
 				}
 			}
 		}
 		// System.out.println(" index name: " + path.toOSString() + " <----> " +
 		// index.getIndexFile().getName());
 		return index;
 	}
 
 	public synchronized Index getIndex(String indexLocation) {
 		return (Index) indexes.get(indexLocation); // is null if unknown, call
 		// if the containerPath must
 		// be computed
 	}
 
 	public synchronized Index getIndexForUpdate(IPath containerPath,
 			boolean reuseExistingFile, boolean createIfMissing) {
 		String indexLocation = computeIndexLocation(containerPath);
 		if (getIndexStates().get(indexLocation) == REBUILDING_STATE)
 			return getIndex(containerPath, indexLocation, reuseExistingFile,
 					createIfMissing);
 		return null; // abort the job since the index has been removed from
 		// the REBUILDING_STATE
 	}
 
 	private SimpleLookupTable getIndexStates() {
 		if (indexStates != null)
 			return indexStates;
 		this.indexStates = new SimpleLookupTable();
 		char[] savedIndexNames = readIndexState();
 		if (savedIndexNames.length > 0) {
 			char[][] names = CharOperation.splitOn('\n', savedIndexNames);
 			if (names.length > 0) {
 				// check to see if workspace has moved, if so then do not trust
 				// saved indexes
 				File indexesDirectory = new File(
 						getScriptPluginWorkingLocation().toOSString());
 				char[] dirName = indexesDirectory.getAbsolutePath()
 						.toCharArray();
 				int delimiterPos = dirName.length;
 				if (CharOperation.match(names[0], 0, delimiterPos, dirName, 0,
 						delimiterPos, true)) {
 					for (int i = 0, l = names.length; i < l; i++) {
 						char[] name = names[i];
 						if (name.length > 0)
 							this.indexStates.put(new String(name), SAVED_STATE);
 					}
 				} else {
 					savedIndexNamesFile.delete(); // forget saved indexes &
 					// delete each index file
 					File[] files = indexesDirectory.listFiles();
 					if (files != null) {
 						for (int i = 0, l = files.length; i < l; i++) {
 							String fileName = files[i].getAbsolutePath();
 							if (fileName.toLowerCase().endsWith(".index")) { //$NON-NLS-1$
 								if (VERBOSE)
 									Util
 											.verbose("Deleting index file " + files[i]); //$NON-NLS-1$
 								files[i].delete();
 							}
 						}
 					}
 				}
 			}
 		}
 		return this.indexStates;
 	}
 
 	private IPath getScriptPluginWorkingLocation() {
 		if (this.javaPluginLocation != null)
 			return this.javaPluginLocation;
 		IPath stateLocation = DLTKCore.getPlugin().getStateLocation();
 		return this.javaPluginLocation = stateLocation;
 	}
 
 	public void indexDocument(SearchDocument searchDocument,
 			SearchParticipant searchParticipant, Index index,
 			IPath indexLocation) {
 		try {
 			((InternalSearchDocument) searchDocument).index = index;
 			searchParticipant.indexDocument(searchDocument, indexLocation);
 		} finally {
 			((InternalSearchDocument) searchDocument).index = null;
 		}
 	}
 
 	/**
 	 * Trigger addition of the entire content of a project Note: the actual
 	 * operation is performed in background
 	 */
 	public void indexAll(IProject project) {
 		if (DLTKCore.getPlugin() == null)
 			return;
 		// Also request indexing of binaries on the buildpath
 		// determine the new children
 		try {
 			Model model = ModelManager.getModelManager().getModel();
 			ScriptProject scriptProject = (ScriptProject) model
 					.getScriptProject(project);
 			// only consider immediate libraries - each project will do the same
 			// NOTE: force to resolve CP variables before calling indexer -
 			// 19303, so that initializers
 			// will be run in the current thread.
 			IBuildpathEntry[] entries = scriptProject.getResolvedBuildpath(
 					true/* ignoreUnresolvedEntry */,
 					false/* don't generateMarkerOnError */, false/*
 																	 * don't
 																	 * returnResolutionInProgress
 																	 */);
 			for (int i = 0; i < entries.length; i++) {
 				IBuildpathEntry entry = entries[i];
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY)
 					this.indexLibrary(entry.getPath(), project);
 			}
 		} catch (ModelException e) { // cannot retrieve buildpath info
 		}
 		// check if the same request is not already in the queue
 		IndexRequest request = new IndexAllProject(project, this);
 		if (!isJobWaiting(request))
 			this.request(request);
 	}
 
 	/**
 	 * Trigger addition of a library to an index Note: the actual operation is
 	 * performed in background
 	 * 
 	 * @param exclusionPatterns
 	 * @param inclusionPatterns
 	 */
 	public void indexLibrary(IPath path, IProject requestingProject) {
 		// requestingProject is no longer used to cancel jobs but leave it here
 		// just in case
 		if (DLTKCore.getPlugin() == null)
 			return;
 		Object target = Model.getTarget(ResourcesPlugin.getWorkspace()
 				.getRoot(), path, true);
 		IndexRequest request = null;
 		if (target instanceof IFile) {
 			// request = new AddArchiveFileToIndex((IFile) target, this);
 			return;
 		} else if (target instanceof java.io.File) {
 			if (((java.io.File) target).isFile()) {
 				// request = new AddArchiveFileToIndex(path, this);
 				return;
 			} else {
 				request = new AddExternalFolderToIndex(path, requestingProject,
 						null, null, this);
 			}
 		} else if (target instanceof IContainer) {
 			// request = new IndexContainerFolder((IContainer) target, this,
 			// requestingProject);
 			return;
 		} else if (target == null
 				&& path.toString().startsWith(
 						IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR)) {
 			request = new AddBuiltinFolderToIndex(path, requestingProject, this);
 		}
 		// check if the same request is not already in the queue
 		// TODO: Uncheck this. After adding some library indexing.
 		if (request != null) {
 			if (!isJobWaiting(request))
 				this.request(request);
 		}
 	}
 
 	/**
 	 * Index the content of the given source folder.
 	 */
 	public void indexSourceFolder(ScriptProject scriptProject,
 			IPath sourceFolder, char[][] inclusionPatterns,
 			char[][] exclusionPatterns) {
 		IProject project = scriptProject.getProject();
 		if (this.jobEnd > this.jobStart) {
 			// skip it if a job to index the project is already in the queue
 			IndexRequest request = new IndexAllProject(project, this);
 			if (isJobWaiting(request))
 				return;
 		}
 		this.request(new AddFolderToIndex(sourceFolder, project,
 				inclusionPatterns, exclusionPatterns, this));
 	}
 
 	public void jobWasCancelled(IPath containerPath) {
 		String indexLocation = computeIndexLocation(containerPath);
 		Object o = this.indexes.get(indexLocation);
 		if (o instanceof Index) {
 			((Index) o).monitor = null;
 			this.indexes.remove(indexLocation);
 		}
 		updateIndexState(indexLocation, UNKNOWN_STATE);
 	}
 
 	/**
 	 * Advance to the next available job, once the current one has been
 	 * completed. Note: clients awaiting until the job count is zero are still
 	 * waiting at this point.
 	 */
 	protected synchronized void moveToNextJob() {
 		// remember that one job was executed, and we will need to save indexes
 		// at some point
 		needToSave = true;
 		super.moveToNextJob();
 	}
 
 	/**
 	 * No more job awaiting.
 	 */
 	protected void notifyIdle(long idlingTime) {
 		if (idlingTime > 1000 && needToSave)
 			saveIndexes();
 	}
 
 	/**
 	 * Name of the background process
 	 */
 	public String processName() {
 		return Messages.process_name;
 	}
 
 	private void rebuildIndex(String indexLocation, IPath containerPath) {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		if (workspace == null)
 			return;
 
 		Object target = Model.getTarget(workspace.getRoot(), containerPath,
 				true);
 		if (target == null) {
 			return;
 		}
 		if (VERBOSE)
 			Util
 					.verbose("-> request to rebuild index: " + indexLocation + " path: " + containerPath.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
 		updateIndexState(indexLocation, REBUILDING_STATE);
 		IndexRequest request = null;
 		if (target instanceof IProject) {
 			IProject p = (IProject) target;
 			if (ScriptProject.hasScriptNature(p)) {
 				request = new IndexAllProject(p, this);
 			}
 		} else if (target instanceof IFolder) {
 			// request = new IndexBinaryFolder((IFolder) target, this);
 			return;
 		} else if (target instanceof IFile) {
 			// request = new AddArchiveFileToIndex((IFile) target, this);
 			return;
 		} else if (target instanceof java.io.File) {
 			// request = new AddArchiveFileToIndex(containerPath, this);
 			return;
 		}
 		if (request != null)
 			request(request);
 	}
 
 	/**
 	 * Recreates the index for a given path, keeping the same read-write
 	 * monitor. Returns the new empty index or null if it didn't exist before.
 	 * Warning: Does not check whether index is consistent (not being used)
 	 */
 	public synchronized Index recreateIndex(IPath containerPath) {
 		boolean mixin = containerPath.toString().startsWith("#special#mixin#");
 		// only called to over write an existing cached index...
 		String containerPathString = containerPath.getDevice() == null ? containerPath
 				.toString()
 				: containerPath.toOSString();
 		try {
 			// Path is already canonical
 			String indexLocation = computeIndexLocation(containerPath);
 			Index index = (Index) this.indexes.get(indexLocation);
 			ReadWriteMonitor monitor = index == null ? null : index.monitor;
 			if (VERBOSE)
 				Util
 						.verbose("-> recreating index: " + indexLocation + " for path: " + containerPathString); //$NON-NLS-1$ //$NON-NLS-2$
 			if (mixin)
 				index = new MixinIndex(indexLocation, containerPathString,
 						false /*
 								 * reuse index file
 								 */);
 			else
 				index = new Index(indexLocation, containerPathString, false /*
 																			 * do
 																			 * not
 																			 * reuse
 																			 * index
 																			 * file
 																			 */);
 			this.indexes.put(indexLocation, index);
 			index.monitor = monitor;
 			return index;
 		} catch (IOException e) {
 			// The file could not be created. Possible reason: the project has
 			// been deleted.
 			if (VERBOSE) {
 				Util
 						.verbose("-> failed to recreate index for path: " + containerPathString); //$NON-NLS-1$
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * Trigger removal of a resource to an index Note: the actual operation is
 	 * performed in background
 	 */
 	public void remove(String containerRelativePath, IPath indexedContainer) {
 		request(new RemoveFromIndex(containerRelativePath, indexedContainer,
 				this));
 	}
 
 	/**
 	 * Removes the index for a given path. This is a no-op if the index did not
 	 * exist.
 	 */
 	public synchronized void removeIndex(IPath containerPath) {
 		if (VERBOSE)
 			Util.verbose("removing index " + containerPath); //$NON-NLS-1$
 		String indexLocation = computeIndexLocation(containerPath);
 		File indexFile = new File(indexLocation);
 		if (indexFile.exists())
 			indexFile.delete();
 		Object o = this.indexes.get(indexLocation);
 		if (o instanceof Index)
 			((Index) o).monitor = null;
 		this.indexes.remove(indexLocation);
 		updateIndexState(indexLocation, null);
 	}
 
 	/**
 	 * Removes all indexes whose paths start with (or are equal to) the given
 	 * path.
 	 */
 	public synchronized void removeIndexPath(IPath path) {
 		Set keySet = this.indexes.keySet();
 		Iterator keys = keySet.iterator();
 		String[] locations = null;
 		int max = keySet.size();
 		int ptr = 0;
 		while (keys.hasNext()) {
 			String indexLocation = (String) keys.next();
 			IPath indexPath = new Path(indexLocation);
 			if (path.isPrefixOf(indexPath)) {
 				Index index = (Index) this.indexes.get(indexLocation);
 				if (index != null)
 					index.monitor = null;
 				if (locations == null)
 					locations = new String[max];
 				locations[ptr++] = indexLocation;
 				File indexFile = new File(indexLocation);
 				if (indexFile.exists()) {
 					indexFile.delete();
 				}
 			} else if (locations == null) {
 				max--;
 			}
 		}
 		if (locations != null) {
 			for (int i = 0; i < ptr; i++) {
 				this.indexes.remove(locations[i]);
 			}
 			removeIndexesState(locations);
 		}
 	}
 
 	/**
 	 * Removes all indexes whose paths start with (or are equal to) the given
 	 * path.
 	 */
 	public synchronized void removeIndexFamily(IPath path) {
 		// only finds cached index files... shutdown removes all non-cached
 		// index files
 		ArrayList toRemove = null;
 		Object[] containerPaths = this.indexLocations.keyTable;
 		for (int i = 0, length = containerPaths.length; i < length; i++) {
 			IPath containerPath = (IPath) containerPaths[i];
 			if (containerPath == null)
 				continue;
 			if (path.isPrefixOf(containerPath)) {
 				if (toRemove == null)
 					toRemove = new ArrayList();
 				toRemove.add(containerPath);
 			}
 		}
 		if (toRemove != null)
 			for (int i = 0, length = toRemove.size(); i < length; i++)
 				this.removeIndex((IPath) toRemove.get(i));
 	}
 
 	/**
 	 * Remove the content of the given source folder from the index.
 	 */
 	public void removeSourceFolderFromIndex(ScriptProject scriptProject,
 			IPath sourceFolder, char[][] inclusionPatterns,
 			char[][] exclusionPatterns) {
 		IProject project = scriptProject.getProject();
 		if (this.jobEnd > this.jobStart) {
 			// skip it if a job to index the project is already in the queue
 			IndexRequest request = new IndexAllProject(project, this);
 			if (isJobWaiting(request))
 				return;
 		}
 		this.request(new RemoveFolderFromIndex(sourceFolder, inclusionPatterns,
 				exclusionPatterns, project, this));
 	}
 
 	/**
 	 * Flush current state
 	 */
 	public synchronized void reset() {
 		super.reset();
 		if (this.indexes != null) {
 			this.indexes = new HashMap(5);
 			this.indexStates = null;
 		}
 		this.indexLocations = new SimpleLookupTable();
 		this.javaPluginLocation = null;
 	}
 
 	public synchronized void saveIndex(Index index) throws IOException {
 		// must have permission to write from the write monitor
 		if (index.hasChanged()) {
 			if (VERBOSE)
 				Util.verbose("-> saving index " + index.getIndexFile()); //$NON-NLS-1$
 			index.save();
 		}
 		// TODO should use getJavaPluginWorkingLocation()+index simple name to
 		// avoid bugs such as
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=62267
 		String indexLocation = index.getIndexFile().getPath();
 		if (this.jobEnd > this.jobStart) {
 			Object containerPath = this.indexLocations
 					.keyForValue(indexLocation);
 			if (containerPath != null) {
 				synchronized (this) {
 					for (int i = this.jobEnd; i > this.jobStart; i--) { // skip
 						// the
 						// current
 						// job
 						IJob job = this.awaitingJobs[i];
 						if (job instanceof IndexRequest)
 							if (((IndexRequest) job).containerPath
 									.equals(containerPath))
 								return;
 					}
 				}
 			}
 		}
 		updateIndexState(indexLocation, SAVED_STATE);
 	}
 
 	/**
 	 * Commit all index memory changes to disk
 	 */
 	public void saveIndexes() {
 		// only save cached indexes... the rest were not modified
 		ArrayList toSave = new ArrayList();
 		synchronized (this) {
 			for (Iterator iter = this.indexes.values().iterator(); iter
 					.hasNext();) {
 				Object o = iter.next();
 				if (o instanceof Index)
 					toSave.add(o);
 			}
 		}
 		boolean allSaved = true;
 		for (int i = 0, length = toSave.size(); i < length; i++) {
 			Index index = (Index) toSave.get(i);
 			ReadWriteMonitor monitor = index.monitor;
 			if (monitor == null)
 				continue; // index got deleted since acquired
 			try {
 				// take read lock before checking if index has changed
 				// don't take write lock yet since it can cause a deadlock (see
 				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=50571)
 				monitor.enterRead();
 				if (index.hasChanged()) {
 					if (monitor.exitReadEnterWrite()) {
 						try {
 							saveIndex(index);
 						} catch (IOException e) {
 							if (VERBOSE) {
 								Util
 										.verbose(
 												"-> got the following exception while saving:", System.err); //$NON-NLS-1$
 								e.printStackTrace();
 							}
 							allSaved = false;
 						} finally {
 							monitor.exitWriteEnterRead();
 						}
 					} else {
 						allSaved = false;
 					}
 				}
 			} finally {
 				monitor.exitRead();
 			}
 		}
 		this.needToSave = !allSaved;
 	}
 
 	public void scheduleDocumentIndexing(final SearchDocument searchDocument,
 			IPath container, final String indexLocation,
 			final SearchParticipant searchParticipant) {
 		request(new IndexRequest(container, this) {
 			public boolean execute(IProgressMonitor progressMonitor) {
 				if (this.isCancelled || progressMonitor != null
 						&& progressMonitor.isCanceled())
 					return true;
 				/* ensure no concurrent write access to index */
 				Index index = getIndex(this.containerPath, indexLocation, true, /*
 																				 * reuse
 																				 * index
 																				 * file
 																				 */true /*
 						 * create if none
 						 */);
 				if (index == null)
 					return true;
 				ReadWriteMonitor monitor = index.monitor;
 				if (monitor == null)
 					return true; // index got deleted since acquired
 				try {
 					monitor.enterWrite(); // ask permission to write
 					indexDocument(searchDocument, searchParticipant, index,
 							new Path(indexLocation));
 				} finally {
 					monitor.exitWrite(); // free write lock
 				}
 				return true;
 			}
 
 			public String toString() {
 				return "indexing " + searchDocument.getPath(); //$NON-NLS-1$
 			}
 		});
 	}
 
 	public String toString() {
 		StringBuffer buffer = new StringBuffer(10);
 		buffer.append(super.toString());
 		buffer.append("In-memory indexes:\n"); //$NON-NLS-1$
 		int count = 0;
 		for (Iterator iter = this.indexes.values().iterator(); iter.hasNext();) {
 			buffer.append(++count)
 					.append(" - ").append(iter.next().toString()).append('\n'); //$NON-NLS-1$
 		}
 		return buffer.toString();
 	}
 
 	private char[] readIndexState() {
 		try {
 			return org.eclipse.dltk.compiler.util.Util.getFileCharContent(
 					savedIndexNamesFile, null);
 		} catch (IOException ignored) {
 			if (VERBOSE)
 				Util.verbose("Failed to read saved index file names"); //$NON-NLS-1$
 			return new char[0];
 		}
 	}
 
 	private synchronized void removeIndexesState(String[] locations) {
 		getIndexStates(); // ensure the states are initialized
 		int length = locations.length;
 		boolean changed = false;
 		for (int i = 0; i < length; i++) {
 			if (locations[i] == null)
 				continue;
 			if ((indexStates.removeKey(locations[i]) != null)) {
 				changed = true;
 				if (VERBOSE) {
 					Util
 							.verbose("-> index state updated to: ? for: " + locations[i]); //$NON-NLS-1$
 				}
 			}
 		}
 		if (!changed)
 			return;
 		writeSavedIndexNamesFile();
 	}
 
 	private synchronized void updateIndexState(String indexLocation,
 			Integer indexState) {
 		getIndexStates(); // ensure the states are initialized
 		if (indexState != null) {
 			if (indexState.equals(indexStates.get(indexLocation)))
 				return; // not changed
 			indexStates.put(indexLocation, indexState);
 		} else {
 			if (!indexStates.containsKey(indexLocation))
 				return; // did not exist anyway
 			indexStates.removeKey(indexLocation);
 		}
 		writeSavedIndexNamesFile();
 		if (VERBOSE) {
 			String state = "?"; //$NON-NLS-1$
 			if (indexState == SAVED_STATE)
 				state = "SAVED"; //$NON-NLS-1$
 			else if (indexState == UPDATING_STATE)
 				state = "UPDATING"; //$NON-NLS-1$
 			else if (indexState == UNKNOWN_STATE)
 				state = "UNKNOWN"; //$NON-NLS-1$
 			else if (indexState == REBUILDING_STATE)
 				state = "REBUILDING"; //$NON-NLS-1$
 			Util
 					.verbose("-> index state updated to: " + state + " for: " + indexLocation); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	private void writeSavedIndexNamesFile() {
 		BufferedWriter writer = null;
 		try {
 			writer = new BufferedWriter(new FileWriter(savedIndexNamesFile));
 			Object[] keys = indexStates.keyTable;
 			Object[] states = indexStates.valueTable;
 			for (int i = 0, l = states.length; i < l; i++) {
 				if (states[i] == SAVED_STATE) {
 					writer.write((String) keys[i]);
 					writer.write('\n');
 				}
 			}
 		} catch (IOException ignored) {
 			if (VERBOSE)
 				Util.verbose(
 						"Failed to write saved index file names", System.err); //$NON-NLS-1$
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 		}
 	}
 
 	public synchronized void rebuild() {
 		disable();
 		File indexesDirectory = new File(getScriptPluginWorkingLocation()
 				.toOSString());
 		// this.
 		if (indexesDirectory.isDirectory()) {
 			File[] indexesFiles = indexesDirectory.listFiles();
 			if (indexesFiles != null) {
 				for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
 					indexesFiles[i].delete();
 				}
 			}
 		}
 		this.reset();
 		// this.indexAll(project)
 		
 		enable();
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IProject[] projects = root.getProjects();
 		for (int i = 0; i < projects.length; i++) {
 			if( DLTKLanguageManager.hasScriptNature(projects[i]) ) {
 				indexAll(projects[i]);
 			}
 		}
 	}
 
 }
