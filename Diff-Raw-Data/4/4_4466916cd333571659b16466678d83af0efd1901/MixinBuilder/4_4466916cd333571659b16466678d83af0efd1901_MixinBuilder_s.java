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
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
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
 	public IStatus buildResources(IScriptProject project, List resources,
 			IProgressMonitor monitor, int status) {
 		return null;
 	}
 
 	public IStatus buildModelElements(IScriptProject project, List elements,
 			IProgressMonitor monitor, int status) {
 		return this.buildModelElements(project, elements, monitor, true);
 	}
 
 	public IStatus buildModelElements(IScriptProject project, List elements,
 			final IProgressMonitor monitor, boolean saveIndex) {
 		if (elements.size() == 0) {
 			return null;
 		}
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
 			String fullContainerPath = ((fullPath.getDevice() == null) ? fullPath
 					.toString()
 					: fullPath.toOSString());
 
 			mixinIndex = manager.getSpecialIndex(
 					"mixin", //$NON-NLS-1$
 					/* project.getProject() */fullPath.toString(),
 					fullContainerPath);
 			imon = mixinIndex.monitor;
 			imon.enterWrite();
 			String name = MessageFormat.format(
 					Messages.MixinBuilder_buildingRuntimeModelFor,
 					new Object[] { project.getElementName() });
 			if (monitor != null) {
 				monitor.beginTask(name, elementsSize);
 			}
 			int fileIndex = 0;
 
 			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
 				ISourceModule element = (ISourceModule) iterator.next();
 
 				Index currentIndex = mixinIndex;
 				if (monitor != null) {
 					if (monitor.isCanceled()) {
 						return null;
 					}
 				}
 
 				String taskTitle = MessageFormat.format(
 						Messages.MixinBuilder_buildingRuntimeModelFor2,
 						new Object[] { project.getElementName(),
 								new Integer(elements.size() - fileIndex),
 								element.getElementName() });
 				++fileIndex;
 				if (monitor != null) {
 					monitor.subTask(taskTitle);
 				}
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
						String contPath = ((path.getDevice() == null) ? path
								.toString() : path.toOSString());

 						Index index = manager.getSpecialIndex("mixin", //$NON-NLS-1$
 								path.toString(), contPath);
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
 
 				SearchParticipant participant = SearchEngine
 						.getDefaultSearchParticipant();
 
 				DLTKSearchDocument document = new DLTKSearchDocument(element
 						.getPath().toString(), containerPath, null,
 						participant, element instanceof ExternalSourceModule);
 				// System.out.println("mixin indexing:" + document.getPath());
 				((InternalSearchDocument) document).toolkit = toolkit;
 				String containerRelativePath = null;
 
 				if (element instanceof ExternalSourceModule) {
 					containerRelativePath = (element.getPath()
 							.removeFirstSegments(containerPath.segmentCount())
 							.setDevice(null).toString());
 				} else if (element instanceof SourceModule) {
 					containerRelativePath = (element.getPath()
 							.removeFirstSegments(1).toPortableString());
 				} else if (element instanceof BuiltinSourceModule) {
 					containerRelativePath = document.getPath();
 					// (element.getPath()
 					// .removeFirstSegments().toOSString());
 				}
 				((InternalSearchDocument) document)
 						.setContainerRelativePath(containerRelativePath);
 				currentIndex.remove(containerRelativePath);
 				((InternalSearchDocument) document).setIndex(currentIndex);
 
 				new MixinIndexer(document, element, currentIndex)
 						.indexDocument();
 				if (monitor != null) {
 					monitor.worked(1);
 				}
 			}
 		} finally {
 			final Set saveIndexesSet = new HashSet();
 
 			if (mixinIndex != null) {
 				if (saveIndex) {
 					saveIndexesSet.add(mixinIndex);
 				} else {
 					imon.exitWrite();
 				}
 			}
 			Iterator iterator = indexes.values().iterator();
 			while (iterator.hasNext()) {
 				Index index = (Index) iterator.next();
 				if (saveIndex) {
 					saveIndexesSet.add(index);
 				} else {
 					index.monitor.exitWrite();
 				}
 			}
 			if (saveIndex) {
 				for (Iterator ind = saveIndexesSet.iterator(); ind.hasNext();) {
 					Index index = (Index) ind.next();
 					if (monitor != null) {
 						String containerPath = index.containerPath;
 						if (containerPath.startsWith("#special#")) { //$NON-NLS-1$
 							containerPath = containerPath.substring(
 									containerPath.lastIndexOf("#"), //$NON-NLS-1$
 									containerPath.length());
 						}
 						monitor.subTask(MessageFormat.format(
 								Messages.MixinBuilder_savingIndexFor,
 								new Object[] { containerPath }));
 					}
 					try {
 						index.save();
 					} catch (IOException e) {
 						if (DLTKCore.DEBUG) {
 							e.printStackTrace();
 						}
 					} finally {
 						index.monitor.exitWrite();
 					}
 				}
 			}
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 
 		return null;
 	}
 
 	private static MixinBuilder builder = new MixinBuilder();
 
 	public static MixinBuilder getDefault() {
 		return builder;
 	}
 
 	public int estimateElementsToBuild(List elements) {
 		return elements.size();
 	}
 
 	public Set getDependencies(IScriptProject project, Set resources,
 			Set allResources, Set oldExternalFolders, Set externalFolders) {
 		return null;
 	}
 }
