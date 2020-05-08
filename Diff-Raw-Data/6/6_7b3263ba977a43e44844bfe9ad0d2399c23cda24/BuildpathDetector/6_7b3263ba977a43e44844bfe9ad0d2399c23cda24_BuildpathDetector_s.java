 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.wizards;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKContentTypeManager;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.launching.ScriptRuntime;
 
 import com.ibm.icu.text.Collator;
 
 /**
  */
 public class BuildpathDetector {
 	private HashMap fSourceFolders;
 	private List fSourceFiles;
 	private HashSet fZIPFiles;
 	private IProject fProject;
 	private IBuildpathEntry[] fResultBuildpath;
 	private IProgressMonitor fMonitor;
 	private IDLTKLanguageToolkit fToolkit;
 
 	private static class BPSorter implements Comparator {
 		private Collator fCollator = Collator.getInstance();
 
 		public int compare(Object o1, Object o2) {
 			IBuildpathEntry e1 = (IBuildpathEntry) o1;
 			IBuildpathEntry e2 = (IBuildpathEntry) o2;
 			return fCollator.compare(e1.getPath().toString(), e2.getPath()
 					.toString());
 		}
 	}
 
 	public BuildpathDetector(IProject project, IDLTKLanguageToolkit toolkit)
 			throws CoreException {
 		fSourceFolders = new HashMap();
 		fZIPFiles = new HashSet(10);
 		fSourceFiles = new ArrayList(100);
 		fProject = project;
 		fResultBuildpath = null;
 		fToolkit = toolkit;
 	}
 
 	private boolean isNested(IPath path, Iterator iter) {
 		while (iter.hasNext()) {
 			IPath other = (IPath) iter.next();
 			if (other.isPrefixOf(path)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method detectBuildpath.
 	 * 
 	 * @param monitor
 	 *            The progress monitor (not null)
 	 * @throws CoreException
 	 */
 	public void detectBuildpath(IProgressMonitor monitor) throws CoreException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		try {
 			monitor.beginTask(Messages.BuildpathDetector_detectingBuildpath,
 					120);
 			fMonitor = monitor;
 			final List correctFiles = new ArrayList();
 			fProject.accept(new IResourceProxyVisitor() {
 				public boolean visit(IResourceProxy proxy) throws CoreException {
 					return BuildpathDetector.this.visit(proxy, correctFiles);
 				}
 			}, IResource.NONE);
 			monitor.worked(10);
 			SubProgressMonitor sub = new SubProgressMonitor(monitor, 80);
 			processSources(correctFiles, sub);
 			sub.done();
 			ArrayList cpEntries = new ArrayList();
 			detectSourceFolders(cpEntries);
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			monitor.worked(10);
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			monitor.worked(10);
 			detectLibraries(cpEntries);
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			monitor.worked(10);
 			addInterpreterContainer(cpEntries);
 			if (cpEntries.size() == 1) {
 				IBuildpathEntry entry = (IBuildpathEntry) cpEntries.get(0);
 				if (entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER) {
					cpEntries.add(0, DLTKCore.newSourceEntry(fProject.getFullPath()));
 				}
 
 			}
 			if (cpEntries.isEmpty() && fSourceFiles.isEmpty()) {
 				return;
 			}
 
 			IBuildpathEntry[] entries = (IBuildpathEntry[]) cpEntries
 					.toArray(new IBuildpathEntry[cpEntries.size()]);
 			if (!BuildpathEntry.validateBuildpath(DLTKCore.create(fProject),
 					entries).isOK()) {
 				return;
 			}
 			fResultBuildpath = entries;
 		} finally {
 			monitor.done();
 		}
 	}
 
 	protected void processSources(List correctFiles, SubProgressMonitor sub) {
 	}
 
 	protected void addInterpreterContainer(ArrayList cpEntries) {
 		cpEntries.add(DLTKCore.newContainerEntry(new Path(
 				ScriptRuntime.INTERPRETER_CONTAINER)));
 	}
 
 	private void detectLibraries(ArrayList cpEntries) {
 		ArrayList res = new ArrayList();
 		Set sourceFolderSet = fSourceFolders.keySet();
 		for (Iterator iter = fZIPFiles.iterator(); iter.hasNext();) {
 			IPath path = (IPath) iter.next();
 			if (isNested(path, sourceFolderSet.iterator())) {
 				continue;
 			}
 			IBuildpathEntry entry = DLTKCore.newLibraryEntry(path);
 			res.add(entry);
 		}
 		Collections.sort(res, new BPSorter());
 		cpEntries.addAll(res);
 	}
 
 	private void detectSourceFolders(ArrayList resEntries) {
 		ArrayList res = new ArrayList();
 		Set sourceFolderSet = fSourceFolders.keySet();
 		for (Iterator iter = sourceFolderSet.iterator(); iter.hasNext();) {
 			IPath path = (IPath) iter.next();
 			ArrayList excluded = new ArrayList();
 			for (Iterator inner = sourceFolderSet.iterator(); inner.hasNext();) {
 				IPath other = (IPath) inner.next();
 				if (!path.equals(other) && path.isPrefixOf(other)) {
 					IPath pathToExclude = other.removeFirstSegments(
 							path.segmentCount()).addTrailingSeparator();
 					excluded.add(pathToExclude);
 				}
 			}
 			IPath[] excludedPaths = (IPath[]) excluded
 					.toArray(new IPath[excluded.size()]);
 			IBuildpathEntry entry = DLTKCore
 					.newSourceEntry(path, excludedPaths);
 			res.add(entry);
 		}
 		Collections.sort(res, new BPSorter());
 		resEntries.addAll(res);
 	}
 
 	private void addToMap(HashMap map, IPath folderPath, IPath relPath) {
 		List list = (List) map.get(folderPath);
 		if (list == null) {
 			list = new ArrayList(50);
 			map.put(folderPath, list);
 		}
 		list.add(relPath);
 	}
 
 	private IPath getFolderPath(IPath packPath, IPath relpath) {
 		int remainingSegments = packPath.segmentCount()
 				- relpath.segmentCount();
 		if (remainingSegments >= 0) {
 			IPath common = packPath.removeFirstSegments(remainingSegments);
 			if (common.equals(relpath)) {
 				return packPath.uptoSegment(remainingSegments);
 			}
 		}
 		return null;
 	}
 
 	private boolean hasExtension(String name, String ext) {
 		return name.endsWith(ext) && (ext.length() != name.length());
 	}
 
 	private boolean isValidResource(IResource res) {
 		return DLTKContentTypeManager.isValidResourceForContentType(
 				this.fToolkit, res);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
 	 */
 	public boolean visit(IResourceProxy proxy, List files) {
 		if (fMonitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		if (proxy.getType() == IResource.FILE) {
 			String name = proxy.getName();
 			IResource res = proxy.requestResource();
 			if (isValidResource(res)) {
 				if (visitSourceModule((IFile) proxy.requestResource())) {
 					files.add(proxy.requestResource());
 				}
 			} else if (hasExtension(name, ".zip")) { //$NON-NLS-1$
 				fZIPFiles.add(proxy.requestFullPath());
 			}
 			return false;
 		}
 		return true;
 	}
 
 	protected boolean visitSourceModule(IFile file) {
 		if (DLTKContentTypeManager.isValidFileNameForContentType(fToolkit, file
				.getFullPath())) {
 			IPath packPath = file.getParent().getFullPath();
 			String cuName = file.getName();
 			addToMap(fSourceFolders, packPath, new Path(cuName));
 			return true;
 		}
 		return false;
 	}
 
 	public IBuildpathEntry[] getBuildpath() {
 		return fResultBuildpath;
 	}
 }
