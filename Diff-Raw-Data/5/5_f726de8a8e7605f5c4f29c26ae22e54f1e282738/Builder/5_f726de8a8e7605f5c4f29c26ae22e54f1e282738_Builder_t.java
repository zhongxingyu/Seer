 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: Builder.java,v 1.23 2005-02-16 10:30:49 gasiunas Exp $
  */
 
 package org.caesarj.ui.builder;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.aspectj.bridge.ISourceLocation;
 import org.caesarj.ui.CaesarPlugin;
 import org.caesarj.ui.editor.CaesarOutlineView;
 import org.caesarj.ui.marker.AdviceMarker;
 import org.caesarj.ui.util.ProjectProperties;
 import org.caesarj.ui.views.CaesarHierarchyView;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Builder is responsible for building a caesar project. Steps done are: 1)
  * collect project properties 2) calling caesar compiler 3) displaying errors 4)
  * refreshing the outlineview
  * 
  * TODO [build] Incremental build? Is there support in caesarj compiler?
  * 
  * @author Ivica Aracic
  */
 
 public class Builder extends IncrementalProjectBuilder {
 
 	private static Logger log = Logger.getLogger(Builder.class);
 
 	/**
 	 * The last project we did a build for, needed by content outline view to
 	 * decide which updates to accept.
 	 */
 	private static IProject lastBuiltProject = null;
 
 	private static final Vector allBuildedProjects;
 
 	private ProjectProperties projectProperties;
 
 	private Collection errors = new LinkedList();
 
 	static {
 		allBuildedProjects = new Vector();
 	}
 
 	/**
 	 * What did we last build?
 	 */
 	public static IProject getLastBuildTarget() {
 		return lastBuiltProject;
 	}
 
 	/**
 	 * @see IncrementalProjectBuilder#build(int, Map, IProgressMonitor) kind is
 	 *      one of: FULL_BUILD, INCREMENTAL_BUILD or AUTO_BUILD currently we do
 	 *      a full build in every case!
 	 */
 	protected IProject[] build(int kind, Map args,
 			IProgressMonitor progressMonitor) {
 		try {
 		    JavaCore.getClasspathVariable(CaesarPlugin.CAESAR_HOME);
 		    
 			lastBuiltProject = getProject();
 			if (!allBuildedProjects.contains(lastBuiltProject))
 				allBuildedProjects.add(lastBuiltProject);
 			this.errors.clear();
 
 			this.projectProperties = new ProjectProperties(getProject());
 
 			log.debug("Building to '" + this.projectProperties.getOutputPath() + "'"); //$NON-NLS-1$//$NON-NLS-2$
 			
 			log.debug("kind: " + kind); //$NON-NLS-1$
 
 			log.debug("----\n" + this.projectProperties.toString() + "----\n"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			CaesarAdapter caesarAdapter = new CaesarAdapter(
 					this.projectProperties.getProjectLocation());
 
 			// build
 			caesarAdapter.compile(this.projectProperties.getSourceFiles(),
 					this.projectProperties.getClassPath(),
					this.projectProperties.getOutputPath().substring(1), this.errors,
 					progressMonitor);
 		} 
 		catch (Throwable t) {
 			t.printStackTrace();
 			errors.add("internal compiler error: " + t.toString());
 		}
 		
 		// update markers, show errors
 		deleteOldErrors();
 		showErrors();
 		
 		try {
 			/* ensure that the generated class files are recognized */
 			lastBuiltProject.refreshLocal(IResource.DEPTH_INFINITE, null);
 			
 			// update has to be executed from Workbenchs Thread
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					CaesarOutlineView.updateAll();
 					CaesarHierarchyView.updateAll();
 				}
 			});
 		} 
 		catch (Throwable t) {
 			t.printStackTrace();
 		}
 
 		IProject[] requiredResourceDeltasOnNextInvocation = null;
 		return requiredResourceDeltasOnNextInvocation;		
 	}
 	
 	public void deleteOldErrors() {
 		try {
 			/* delete unpositioned errors */
 			lastBuiltProject.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 			
 			/* delete positioned errors */
 			Collection sourceFiles = this.projectProperties.getSourceFiles();
 			for (Iterator it = sourceFiles.iterator(); it.hasNext();) {
 
 				String sourcePath = this.projectProperties.getProjectLocation()
 						+ it.next().toString();
 
 				IResource resource = ProjectProperties.findResource(sourcePath,
 						lastBuiltProject);
 
 				resource.deleteMarkers(IMarker.PROBLEM, true,
 						IResource.DEPTH_INFINITE);
 				resource.deleteMarkers(AdviceMarker.ADVICEMARKER, true,
 						IResource.DEPTH_INFINITE);
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// TODO [optimize] make it efficient
 	// TODO [feature] warnings are missing
 	public void showErrors() {
 		for (Iterator it = this.errors.iterator(); it.hasNext();) {
 			try {
 				Object err = it.next();
 				
 				/* errors can be represented by PositionedError, UnpositionedError or String */
 				if (err instanceof PositionedError) {
 					PositionedError error = (PositionedError)err;
 					TokenReference token = error.getTokenReference();
 	
 					if (token.getLine() > 0) {
 						log.debug("file: " + token.getFile() + ", " + "line: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 								+ token.getLine() + ", " + "path: " //$NON-NLS-1$//$NON-NLS-2$
 								+ token.getPath());
 	
 						IResource resource = ProjectProperties.findResource(token
 								.getPath().getAbsolutePath(), lastBuiltProject);
 	
 						IMarker marker = resource.createMarker(IMarker.PROBLEM);
 						marker.setAttribute(IMarker.LINE_NUMBER, token.getLine());
 						marker.setAttribute(IMarker.MESSAGE, error
 								.getFormattedMessage().getMessage());
 						marker.setAttribute(IMarker.SEVERITY, new Integer(
 								IMarker.SEVERITY_ERROR));
 					}
 				}
 				else { /* create unpositioned error at the scope of the project */					
 					String msg;
 					if (err instanceof UnpositionedError) {
 						msg = ((UnpositionedError)err).getFormattedMessage().getMessage();
 					}
 					else {
 						msg = (String)err; // for internal errors
 					}
 					
 					IMarker marker = lastBuiltProject.createMarker(IMarker.PROBLEM);
 					marker.setAttribute(IMarker.MESSAGE, msg);
 					marker.setAttribute(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
 				}
 			} 
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static IProject getProjectForSourceLocation(ISourceLocation location) {
 		IPath path = new Path(location.getSourceFile().getAbsolutePath());
 		Iterator iter = allBuildedProjects.iterator();
 		IProject ret = null;
 		IPath projectPath=null;
 		while (iter.hasNext()) {
 			ret = (IProject) iter.next();
 			projectPath = ret.getLocation();
 			if (projectPath.isPrefixOf(path))
 				return ret;
 		}
 		return null;
 	}}
