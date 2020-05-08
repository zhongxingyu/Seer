 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ui.editor;
 
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.dltk.core.CorrectionEngine;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptModelMarker;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.ui.texteditor.MarkerAnnotation;
 import org.eclipse.ui.texteditor.MarkerUtilities;
 
 public class ScriptMarkerAnnotation extends MarkerAnnotation implements IScriptAnnotation {
 
 	public static final String JAVA_MARKER_TYPE_PREFIX= "org.eclipse.jdt"; //$NON-NLS-1$
	public static final String ERROR_ANNOTATION_TYPE= "org.eclipse.dltk.ui.error"; //$NON-NLS-1$
	public static final String WARNING_ANNOTATION_TYPE= "org.eclipse.dltk.ui.warning"; //$NON-NLS-1$
	public static final String INFO_ANNOTATION_TYPE= "org.eclipse.dltk.ui.info"; //$NON-NLS-1$
 	public static final String TASK_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.task"; //$NON-NLS-1$
 
 	private IScriptAnnotation fOverlay;
 
 
 	public ScriptMarkerAnnotation(IMarker marker) {
 		super(marker);
 	}
 
 	/*
 	 * @see IJavaAnnotation#getArguments()
 	 */
 	public String[] getArguments() {
 		IMarker marker= getMarker();
 		if (marker != null && marker.exists() && isProblem())
 			return CorrectionEngine.getProblemArguments(marker);
 		return null;
 	}
 
 	/*
 	 * @see IJavaAnnotation#getId()
 	 */
 	public int getId() {
 		IMarker marker= getMarker();
 		if (marker == null  || !marker.exists())
 			return -1;
 
 		if (isProblem())
 			return marker.getAttribute(IScriptModelMarker.ID, -1);
 
 //		if (TASK_ANNOTATION_TYPE.equals(getAnnotationType())) {
 //			try {
 //				if (marker.isSubtypeOf(IScriptModelMarker.TASK_MARKER)) {
 //					return IProblem.Task;
 //				}
 //			} catch (CoreException e) {
 //				DLTKUIPlugin.log(e); // should no happen, we test for marker.exists
 //			}
 //		}
 
 		return -1;
 	}
 
 	/*
 	 * @see IJavaAnnotation#isProblem()
 	 */
 	public boolean isProblem() {
 		String type= getType();
 		return WARNING_ANNOTATION_TYPE.equals(type) || ERROR_ANNOTATION_TYPE.equals(type);
 	}
 
 	/**
 	 * Overlays this annotation with the given javaAnnotation.
 	 *
 	 * @param javaAnnotation annotation that is overlaid by this annotation
 	 */
 	public void setOverlay(IScriptAnnotation javaAnnotation) {
 		if (fOverlay != null)
 			fOverlay.removeOverlaid(this);
 
 		fOverlay= javaAnnotation;
 		if (!isMarkedDeleted())
 			markDeleted(fOverlay != null);
 
 		if (fOverlay != null)
 			fOverlay.addOverlaid(this);
 	}
 
 	/*
 	 * @see IJavaAnnotation#hasOverlay()
 	 */
 	public boolean hasOverlay() {
 		return fOverlay != null;
 	}
 
 	/*
 	 * @see org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getOverlay()
 	 */
 	public IScriptAnnotation getOverlay() {
 		return fOverlay;
 	}
 
 	/*
 	 * @see IJavaAnnotation#addOverlaid(IJavaAnnotation)
 	 */
 	public void addOverlaid(IScriptAnnotation annotation) {
 		// not supported
 	}
 
 	/*
 	 * @see IJavaAnnotation#removeOverlaid(IJavaAnnotation)
 	 */
 	public void removeOverlaid(IScriptAnnotation annotation) {
 		// not supported
 	}
 
 	/*
 	 * @see IJavaAnnotation#getOverlaidIterator()
 	 */
 	public Iterator getOverlaidIterator() {
 		// not supported
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getCompilationUnit()
 	 */
 	public ISourceModule getSourceModule() {
 		IModelElement element= DLTKCore.create(getMarker().getResource());
 		if (element instanceof ISourceModule) {
 			return (ISourceModule)element;
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getMarkerType()
 	 */
 	public String getMarkerType() {
 		IMarker marker= getMarker();
 		if (marker == null  || !marker.exists())
 			return null;
 		
 		return  MarkerUtilities.getMarkerType(getMarker());
 	}
 }
