 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.mixin;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.builder.IScriptBuilder;
 import org.eclipse.dltk.core.mixin.IMixinParser;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.index.Index;
 import org.eclipse.dltk.core.search.indexing.IndexManager;
 import org.eclipse.dltk.core.search.indexing.InternalSearchDocument;
 import org.eclipse.dltk.core.search.indexing.ReadWriteMonitor;
 import org.eclipse.dltk.internal.core.BuiltinProjectFragment;
 import org.eclipse.dltk.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.internal.core.ExternalProjectFragment;
 import org.eclipse.dltk.internal.core.ExternalSourceModule;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.SourceModule;
 import org.eclipse.dltk.internal.core.search.DLTKSearchDocument;
 
 public class MixinBuilder implements IScriptBuilder {
 	public IStatus[] buildResources(IScriptProject project, List resources,
 			IProgressMonitor monitor) {
 		return null;
 	}
 
 	public List getDependencies(IScriptProject project, List resources) {
 		return null;
 	}
 
 	public IStatus[] buildModelElements(IScriptProject project, List elements,
 			IProgressMonitor monitor) {
 		return this.buildModelElements(project, elements, monitor, true);
 	}
 
 	public IStatus[] buildModelElements(IScriptProject project, List elements,
 			final IProgressMonitor monitor, boolean saveIndex) {
 		IndexManager manager = ModelManager.getModelManager().getIndexManager();
 		final int elementsSize = elements.size();
 		IDLTKLanguageToolkit toolkit = null;
 		IMixinParser parser = null;
 		try {
 			toolkit = DLTKLanguageManager.getLanguageToolkit(project);
 			if (toolkit != null) {
 				parser = MixinManager.getMixinParser(toolkit.getNatureId());
 			}
 		} catch (CoreException e1) {
 			if (DLTKCore.DEBUG) {
 				e1.printStackTrace();
 			}
 		}
 
 		if (parser == null || toolkit == null) {
 			return null;
 		}
 		Map indexes = new HashMap();
 		// Map imons = new HashMap();
 		Index mixinIndex = null;
 		ReadWriteMonitor imon = null;
 		try {
 			// waitUntilIndexReady(toolkit);
 			IPath fullPath = project.getProject().getFullPath();
 
 			mixinIndex = manager.getSpecialIndex("mixin", /* project.getProject() */
 			fullPath.toString(), fullPath.toOSString());
 			imon = mixinIndex.monitor;
 			imon.enterWrite();
 			String name = "Building runtime model for "
 					+ project.getElementName();
 			monitor.beginTask(name, elementsSize);
 			int fileIndex = 0;
 
 			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
 				ISourceModule element = (ISourceModule) iterator.next();
 
 				Index currentIndex = mixinIndex;
 				if (monitor.isCanceled()) {
 					return null;
 				}
 
 				String taskTitle = "Building runtime model for "
 						+ project.getElementName() + " ("
 						+ (elements.size() - fileIndex) + "):"
 						+ element.getElementName();
 				++fileIndex;
 				monitor.subTask(taskTitle);
 				// monitor.beginTask(taskTitle, 1);
 
 				IProjectFragment projectFragment = (IProjectFragment) element
 						.getAncestor(IModelElement.PROJECT_FRAGMENT);
 				IPath containerPath = project.getPath();
 				if (projectFragment instanceof ExternalProjectFragment
 						|| projectFragment instanceof BuiltinProjectFragment) {
 					IPath path = projectFragment.getPath();
 					if (indexes.containsKey(path)) {
 						currentIndex = (Index) indexes.get(path);
 						containerPath = path;
 					} else {
 						Index index = manager.getSpecialIndex("mixin", path
 								.toString(), path.toOSString());
 						if (index != null) {
 							currentIndex = index;
 							if (!indexes.values().contains(index)) {
 								index.monitor.enterWrite();
 								indexes.put(path, index);
 							}
 							containerPath = path;
 						}
 					}
 				}
 
 				char[] source = element.getSourceAsCharArray();
 				SearchParticipant participant = SearchEngine
 						.getDefaultSearchParticipant();
 
 				DLTKSearchDocument document;
 				document = new DLTKSearchDocument(element.getPath()
 						.toOSString(), containerPath, source, participant,
 						element instanceof ExternalSourceModule);
 				// System.out.println("mixin indexing:" + document.getPath());
 				((InternalSearchDocument) document).toolkit = toolkit;
 				String containerRelativePath = null;
 
 				if (element instanceof ExternalSourceModule) {
 					containerRelativePath = (element.getPath()
 							.removeFirstSegments(containerPath.segmentCount())
 							.setDevice(null).toString());
 				} else if (element instanceof SourceModule) {
 					containerRelativePath = (element.getPath()
 							.removeFirstSegments(1).toOSString());
 				} else if (element instanceof BuiltinSourceModule) {
 					containerRelativePath = document.getPath();
 					// (element.getPath()
 					// .removeFirstSegments().toOSString());
 				}
 				((InternalSearchDocument) document)
 						.setContainerRelativePath(containerRelativePath);
 				currentIndex.remove(containerRelativePath);
 				((InternalSearchDocument) document).setIndex(currentIndex);
 
//				new MixinIndexer(document, element,
//						currentIndex).indexDocument();
 				monitor.worked(1);
 			}
 			monitor.done();
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		} finally {
 			if (mixinIndex != null) {
 				imon.exitWrite();
 				if (saveIndex) {
 					try {
 						manager.saveIndex(mixinIndex);
 					} catch (IOException e) {
 						if (DLTKCore.DEBUG) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			Iterator iterator = indexes.values().iterator();
 			while (iterator.hasNext()) {
 				Index index = (Index) iterator.next();
 				index.monitor.exitWrite();
 				if (saveIndex) {
 					try {
 						manager.saveIndex(index);
 					} catch (IOException e) {
 						if (DLTKCore.DEBUG) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private static MixinBuilder builder = new MixinBuilder();
 
 	public static MixinBuilder getDefault() {
 		return builder;
 	}
 }
