 /*
  * <copyright>
  *
  * Copyright (c) 2005-2006 Sven Efftinge and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sven Efftinge - Initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.gmf.internal.xpand.util;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.gmf.internal.xpand.Activator;
 import org.eclipse.gmf.internal.xpand.expression.AnalysationIssue;
 import org.eclipse.gmf.internal.xpand.util.ParserException.ErrorLocationInfo;
 
 /**
  * FIXME fix syntax elements to keep not (only) line-relative column info, but buffer-related - otherwise
  * it makes no much sense for us to show markers
  */
 public class OawMarkerManager {
 
 	public static void addMarkers(final IFile file, AnalysationIssue... issues) {
 		MarkerData[] data = new MarkerData[issues.length];
 		int i = 0;
 		for (AnalysationIssue issue : issues) {
 			data[i++] = createMarkerData(issue);
 		}
 		internalAddMarker(file, data);
 	}
 
 	public static void addMarkers(IFile file, ErrorLocationInfo... issues) {
 		MarkerData[] data = new MarkerData[issues.length];
 		int i = 0;
 		for (ErrorLocationInfo issue : issues) {
			data[i++] = new MarkerData(issue.message, IMarker.SEVERITY_ERROR, -1, -1, issue.startLine);
 		}
 		internalAddMarker(file, data);
 	}
 
 	public static void addErrorMarker(final IFile file, final String message, final int start, final int end) {
 		internalAddMarker(file, new MarkerData(message, IMarker.SEVERITY_ERROR, start, end));
 	}
 
 	private static MarkerData createMarkerData(AnalysationIssue issue) {
 		int start = -1, end = -1, line = -1;
 		if (issue.getElement() != null) {
 			start = issue.getElement().getStart() - 1;
 			end = issue.getElement().getEnd() - 1;
 			line = issue.getElement().getLine();
 		}
 		return new MarkerData(issue.getMessage(), IMarker.SEVERITY_ERROR, start, end, line);
 	}
 
 	private static class MarkerData {
 		final String message;
 		final int severity;
 		final int start;
 		final int end;
 		final int line;
 
 		MarkerData(String message, int severity, int start, int end) {
 			this(message, severity, start, end, -1);
 		}
 
 		MarkerData(String message, int severity, int start, int end, int line) {
 			this.message = message;
 			this.severity = severity;
 			this.start = start;
 			this.end = end;
 			this.line = line;
 		}
 	}
 
 	private static final String getMARKER_TYPE() {
 		return Activator.getId() + ".problem";
 	}
 
 	private final static void internalAddMarker(final IFile file, final MarkerData... markerData) {
         try {
             file.getWorkspace().run(new IWorkspaceRunnable() {
 
             	public void run(IProgressMonitor monitor) throws CoreException {
 					for (MarkerData d : markerData) {
 						createMarker(d);
 					}
                 }
 
 				private void createMarker(MarkerData data) throws CoreException {
 					final IMarker marker = file.createMarker(getMARKER_TYPE());
                     marker.setAttribute(IMarker.MESSAGE, data.message);
                     marker.setAttribute(IMarker.SEVERITY, data.severity);
                     if (data.line != -1) {
                     	marker.setAttribute(IMarker.LINE_NUMBER, data.line);
                         marker.setAttribute(IMarker.LOCATION, toLocationString(data));
                     } else {
                     	// "else" clause here because in case we possess line number info, most probably
                     	// start and end are relative to that line, and are not file buffer positions (as it seems to be assumed by CHAR_START|END).
                     	if (data.start != -1 && data.end != -1) {
                     		marker.setAttribute(IMarker.CHAR_START, data.start);
                     		marker.setAttribute(IMarker.CHAR_END, data.end);
                     		marker.setAttribute(IMarker.LOCATION, toLocationString(data));
                     	}
                     }
 				}
 
 				private String toLocationString(MarkerData data) {
 					StringBuilder sb = new StringBuilder();
 					if (data.line != -1) {
 						sb.append("line: ");
 						sb.append(data.line);
 					}
 					if (data.start != -1 && data.end != -1) {
 						boolean theOnlyData = sb.length() == 0;
 						if (!theOnlyData) {
 							sb.append(" (");
 						}
 						sb.append(data.start);
 						sb.append(" .. ");
 						sb.append(data.end);
 						if (!theOnlyData) {
 							sb.append(")");
 						}
 					}
 					return sb.toString();
 				}
 
             }, file.getWorkspace().getRuleFactory().markerRule(file), 0, new NullProgressMonitor());
         } catch (final CoreException e) {
             Activator.log(e.getStatus());
         }
     }
 
 	public static void deleteMarkers(final IResource file) {
 		try {
 			if (!file.exists()) {
 				return;
 			}
 			file.getWorkspace().run(new IWorkspaceRunnable() {
 
 				public void run(final IProgressMonitor monitor) throws CoreException {
 					file.deleteMarkers(getMARKER_TYPE(), true, IResource.DEPTH_INFINITE);
 				}
 
 			}, file.getWorkspace().getRuleFactory().markerRule(file), 0, new NullProgressMonitor());
 		} catch (CoreException ce) {
 			Activator.log(ce.getStatus());
 		}
 	}
 }
