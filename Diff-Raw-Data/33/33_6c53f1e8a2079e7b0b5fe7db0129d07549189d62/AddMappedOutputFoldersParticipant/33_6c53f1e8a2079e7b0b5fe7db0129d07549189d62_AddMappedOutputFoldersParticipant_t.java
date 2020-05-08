 /*******************************************************************************
  * Copyright (c) 2009 Red Hat and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.common.internal.modulecore;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;
 import org.eclipse.jst.common.jdt.internal.javalite.IJavaProjectLite;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaCoreLite;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaLiteUtilities;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.flat.AbstractFlattenParticipant;
 import org.eclipse.wst.common.componentcore.internal.flat.FilterResourceParticipant;
import org.eclipse.wst.common.componentcore.internal.flat.FlatVirtualComponent.FlatComponentTaskModel;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatFile;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatResource;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlattenParticipant;
 import org.eclipse.wst.common.componentcore.internal.flat.VirtualComponentFlattenUtility;
 import org.eclipse.wst.common.componentcore.internal.flat.VirtualComponentFlattenUtility.ShouldIncludeUtilityCallback;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 
 public class AddMappedOutputFoldersParticipant extends AbstractFlattenParticipant implements ShouldIncludeUtilityCallback {
 	private List<IFlatResource> list;
 	private String[] filteredSuffixes = new String[]{}; 
 	
 	public AddMappedOutputFoldersParticipant() {
 		// intentionally blank
 	}
 
 	public AddMappedOutputFoldersParticipant(String[] filtered) {
 		this.filteredSuffixes = filtered;
 	}
 	
 	public boolean shouldAddComponentFile(IVirtualComponent component, IFlatFile file) {
 		IFlattenParticipant[] delegates = getDelegateParticipants();
 		for( int i = 0; i < delegates.length; i++ ) {
 			if(!delegates[i].shouldAddExportableFile(null, component, null, file))
 				return false;
 		}
 		return true;
 	}
 	
 	public IFlattenParticipant[] getDelegateParticipants() {
 		return new IFlattenParticipant[] {
 				FilterResourceParticipant.createSuffixFilterParticipant(filteredSuffixes)
 		};
 	}
 
 	@Override
 	public void finalize(IVirtualComponent component,
 			FlatComponentTaskModel dataModel, List<IFlatResource> resources) {
 		this.list = resources;
 		list.getClass();
 		if( !isApprovedComponent(component))
 			return;
 
 		HashMap<IContainer, IPath> mapped = getMappedJavaOutputContainers(component.getProject());
 		Iterator<IContainer> i = mapped.keySet().iterator();
 		while(i.hasNext()) {
 			IContainer next = i.next();
 			try {
 				new VirtualComponentFlattenUtility(list, this).addContainer(next, mapped.get(next));
 			} catch( CoreException ce) {}
 		}
 	}
 	
 	protected boolean isApprovedComponent(IVirtualComponent vc) {
 		// TODO
 		return true;
 	}
 
 	public final static HashMap<IContainer, IPath> getMappedJavaOutputContainers(IProject project) {
 		ComponentResourceProxy[] proxies = findAllMappingProxies(project);
 		IJavaProjectLite javaProjectLite = JavaCoreLite.create(project);
 
 		HashMap<IContainer, IPath> map = new HashMap<IContainer, IPath>();
		IFile classpathFile = project.getFile(JavaProject.CLASSPATH_FILENAME);
		if( javaProjectLite.exists() && classpathFile.exists()) {
			IClasspathEntry[] entries = javaProjectLite.readRawClasspath();
			for (IClasspathEntry entry : entries) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath cpePath = entry.getPath();
					for( int i = 0; i < proxies.length; i++ ) {
						if( cpePath.equals(new Path(project.getName()).append(proxies[i].source).makeAbsolute())) {
							IContainer outputContainer = JavaLiteUtilities.getJavaOutputContainer(javaProjectLite, entry);
							if (!map.containsKey(outputContainer)) {
								map.put(outputContainer, proxies[i].runtimePath);
							}
 						}
						// TODO 
 					}
 				}
 			}
 		}
 		return map;
 	}
 
 	/* 
 	 * This code below is also duplicated in common.ui in the generic page
 	 * to handle module assembly 
 	 */
 	
 	public static ComponentResourceProxy[] findAllMappingProxies(IProject project) {
 		ComponentResource[] allMappings = findAllMappings(project);
 		ComponentResourceProxy[] proxies = new ComponentResourceProxy[allMappings.length];
 		for( int i = 0; i < allMappings.length; i++ ) {
 			proxies[i] = new ComponentResourceProxy(
 					allMappings[i].getSourcePath(), 
 					allMappings[i].getRuntimePath());
 		}
 		return proxies;
 	}
 	
 	protected static ComponentResource[] findAllMappings(IProject project) {
 		StructureEdit structureEdit = null;
 		try {
 			structureEdit = StructureEdit.getStructureEditForRead(project);
 			WorkbenchComponent component = structureEdit.getComponent();
 			Object[] arr = component.getResources().toArray();
 			ComponentResource[] result = new ComponentResource[arr.length];
 			for( int i = 0; i < arr.length; i++ )
 				result[i] = (ComponentResource)arr[i];
 			return result;
 		} catch(Exception e) {
 		} finally {
 			if( structureEdit != null )
 				structureEdit.dispose();
 		}
 		return new ComponentResource[]{};
 	}
 	
 	public static class ComponentResourceProxy {
 		public IPath source, runtimePath;
 		public ComponentResourceProxy(IPath source, IPath runtimePath) {
 			this.source = source;
 			this.runtimePath = runtimePath;
 		}
 	}
 }
