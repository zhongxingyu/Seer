 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui;
 
 import java.io.File;
 import java.net.URI;
 import java.text.MessageFormat;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IDebugElement;
 import org.eclipse.debug.core.model.IErrorReportingExpression;
 import org.eclipse.debug.core.model.IExpression;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.core.model.IThread;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.debug.ui.IValueDetailListener;
 import org.eclipse.dltk.debug.core.model.IScriptBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptLineBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptValue;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 import org.eclipse.dltk.debug.core.model.IScriptWatchpoint;
 import org.eclipse.dltk.debug.ui.breakpoints.BreakpointUtils;
 import org.eclipse.dltk.internal.debug.ui.ExternalFileEditorInput;
 import org.eclipse.dltk.internal.debug.ui.ScriptEvaluationContextManager;
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.launching.debug.DebuggingEngineManager;
 import org.eclipse.dltk.launching.debug.IDebuggingEngine;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 
 public abstract class ScriptDebugModelPresentation extends LabelProvider
 		implements IDebugModelPresentation {
 
 	// TODO: move to properties file
 	protected static final String SUSPENDED_LABEL = "suspended";
 	protected static final String RUNNING_LABEL = "running";
 
 	public static IDebuggingEngine getDebuggingEngine(IDebugElement element) {
 		final String id = element.getLaunch().getAttribute(
 				DebuggingEngineRunner.LAUNCH_ATTR_DEBUGGING_ENGINE_ID);
 		if (id == null) {
 			return null;
 		}
 
 		return DebuggingEngineManager.getInstance().getDebuggingEngine(id);
 	}
 
 	public static IProject getProject(IDebugElement element)
 			throws CoreException {
 		final ILaunchConfiguration configuration = element.getLaunch()
 				.getLaunchConfiguration();
 
 		final String projectName = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME,
 				(String) null);
 		if (projectName == null) {
 			return null;
 		}
 
 		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 	}
 
 	public static IScriptThread getEvaluationThread(IScriptDebugTarget target) {
 		IScriptThread thread = null;
 
 		IScriptStackFrame frame = ScriptEvaluationContextManager
 				.getEvaluationContext((IWorkbenchWindow) null);
 		if (frame != null) {
 			thread = (IScriptThread) frame.getThread();
 		}
 
 		if (thread != null
 				&& (!thread.getDebugTarget().equals(target) || (!thread
 						.isSuspended()))) {
 			thread = null; // can only use suspended threads in the same target
 		}
 		if (thread == null) {
 			try {
 				// Find first suspended thread
 				IThread[] threads = target.getThreads();
 				for (int i = 0; i < threads.length; i++) {
 					if (threads[i].isSuspended()) {
 						thread = (IScriptThread) threads[i];
 						break;
 					}
 				}
 			} catch (DebugException e) {
 				DLTKDebugUIPlugin.log(e);
 			}
 		}
 		return thread;
 	}
 
 	protected String getDebugTargetText(IScriptDebugTarget target) {
 		IDebuggingEngine engine = getDebuggingEngine(target);
 
 		if (engine != null) {
 			return MessageFormat.format("{0} [session id: {1}]", new Object[] {
 					engine.getName(), target.getSessionId() });
 		}
 
 		return target.toString();
 	}
 
 	// Text
 	protected String getThreadText(IScriptThread thread) {
 		try {
 			return MessageFormat.format("{0} ({1})", new Object[] {
 					thread.getName(),
 					thread.isSuspended() ? SUSPENDED_LABEL : RUNNING_LABEL });
 
 		} catch (DebugException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return thread.toString();
 	}
 
 	protected static IPath getStackFrameRelativePath(
 			IScriptStackFrame stackFrame) throws CoreException {
 		final URI uri = stackFrame.getSourceURI();
 		final IProject project = getProject(stackFrame);
 
 		final IPath projectPath = project.getLocation();
 		final IPath realPath = new Path(uri.getPath());
 
 		IPath path = realPath;
 		if (projectPath.isPrefixOf(realPath)) {
 			path = new Path("");
 			int index = projectPath.segmentCount();
 			while (index < realPath.segmentCount()) {
 				path = path.append(realPath.segment(index));
 				++index;
 			}
 		}
 
 		return path;
 	}
 
 	protected String getStackFrameText(IScriptStackFrame stackFrame) {
 		// TODO: improve later
 		try {
 			String sourceLine = stackFrame.getSourceLine();
 
 			// Check if source line is empty
 			if (sourceLine == null || sourceLine.length() == 0) {
 
 				final Object object = stackFrame.getLaunch().getSourceLocator()
 						.getSourceElement(stackFrame);
 
 				if (object instanceof IFile) {
 					final IDocumentProvider provider = DLTKUIPlugin
 							.getDocumentProvider();
 
 					final IDocument document = provider
 							.getDocument(new FileEditorInput((IFile) object));
 
 					if (document != null) {
 						try {
 							final IRegion region = document
 									.getLineInformation(stackFrame
 											.getLineNumber() - 1);
 
 							sourceLine = document.get(region.getOffset(),
 									region.getLength()).trim();
 
 						} catch (BadLocationException e) {
 							DLTKDebugUIPlugin.log(e);
 						}
 					}
 				}
 			}
 
 			// Check if source line is empty (again)
 			if (sourceLine == null || sourceLine.length() == 0) {
 				final int level = stackFrame.getStack().size()
 						- stackFrame.getLevel() - 1;
 				sourceLine = MessageFormat.format("Stack frame #{0}",
 						new Object[] { new Integer(level) });
 			}
 
 			// Compute stack frame relative path
 			final IPath path = getStackFrameRelativePath(stackFrame);
 
 			// TODO: may be make external option for file:line
 			return MessageFormat.format("{0} [{1}: {2}]", new Object[] {
 					sourceLine, path.toPortableString(),
 					new Integer(stackFrame.getLineNumber()) });
 		} catch (CoreException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return stackFrame.toString();
 	}
 
 	public String getVariableText(IScriptVariable variable) {
 		try {
 			String name = variable.getName();
 
 			if (!variable.hasChildren()) {
 				String value = variable.getValueString();
 				if (value != null && value.length() > 0) {
 					return name + " = " + value;
 				}
 			}
 
 			return name;
 		} catch (DebugException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return variable.toString();
 	}
 
 	protected String getBreakpointText(IScriptBreakpoint breakpoint) {
 		try {
 			StringBuffer sb = new StringBuffer();
 
 			final String language = BreakpointUtils.getLanguageToolkit(
 					breakpoint).getLanguageName();
 
 			final int hitCount = breakpoint.getHitCount();
 
 			if (breakpoint instanceof IScriptWatchpoint) { // IScriptWatchPoint
 				IScriptWatchpoint w = (IScriptWatchpoint) breakpoint;
 
 				final String file = w.getResourceName();
 				final int lineNumber = w.getLineNumber();
 				final String fieldName = w.getFieldName();
 
 				sb.append(MessageFormat.format(
 						"{0}: {1} [line: {2}], watch: {3}", new Object[] {
 								language, file, new Integer(lineNumber),
 								fieldName }));
 			} else if (breakpoint instanceof IScriptLineBreakpoint) { // IScriptLineBreakpoint
 				IScriptLineBreakpoint b = (IScriptLineBreakpoint) breakpoint;
 
 				final String file = b.getResourceName();
 				final int lineNumber = b.getLineNumber();
 
 				sb.append(MessageFormat
 						.format("{0}: {1}: [line: {2}]", new Object[] {
 								language, file, new Integer(lineNumber) }));
 			}
 
 			if (hitCount != -1) {
 				sb.append(", " + hitCount + " hits");
 			}
 
 			return sb.toString();
 		} catch (CoreException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return breakpoint.toString();
 	}
 
 	protected String getExpressionText(IExpression expression) {
 		final String expressionText = expression.getExpressionText();
 
 		try {
 			if (expression instanceof IErrorReportingExpression) {
 				IErrorReportingExpression errorExpression = (IErrorReportingExpression) expression;
 				if (errorExpression.hasErrors()) {
 					return expressionText;
 				}
 			}
 
 			IScriptValue value = (IScriptValue) expression.getValue();
 			if (value != null) {
 				return MessageFormat.format("{0} = {1}", new Object[] {
 						expressionText, value.getValueString() });
 			}
 		} catch (DebugException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return expressionText;
 	}
 
 	public final String getText(Object element) {
 		if (element instanceof IScriptDebugTarget) {
 			return getDebugTargetText((IScriptDebugTarget) element);
 		} else if (element instanceof IScriptBreakpoint) {
 			return getBreakpointText((IScriptBreakpoint) element);
 		} else if (element instanceof IScriptThread) {
 			return getThreadText((IScriptThread) element);
 		} else if (element instanceof IScriptStackFrame) {
 			return getStackFrameText((IScriptStackFrame) element);
 		} else if (element instanceof IScriptVariable) {
 			return getVariableText((IScriptVariable) element);
 		} else if (element instanceof IExpression) {
 			return getExpressionText((IExpression) element);
 		}
 
 		return null;
 	}
 
 	// Details
 	public void computeDetail(IValue value, IValueDetailListener listener) {
 		try {
 			listener.detailComputed(value, value.getValueString());
 		} catch (DebugException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setAttribute(String attribute, Object value) {
 	}
 
 	// Images
 	protected Image getBreakpointImage(IScriptBreakpoint breakpoint) {
 		return null;
 	}
 
 	protected Image getVariableImage(IScriptVariable variable) {
 		return null;
 	}
 
 	protected Image getThreadImage(IScriptThread thread) {
 		return null;
 	}
 
 	protected Image getStackFrameImage(IScriptStackFrame frame) {
 		return null;
 	}
 
 	public Image getImage(Object element) {
 		if (element instanceof IScriptBreakpoint) {
 			return getBreakpointImage((IScriptBreakpoint) element);
 		} else if (element instanceof IScriptVariable) {
 			return getVariableImage((IScriptVariable) element);
 		} else if (element instanceof IScriptThread) {
 			return getThreadImage((IScriptThread) element);
 		} else if (element instanceof IScriptStackFrame) {
 			return getStackFrameImage((IScriptStackFrame) element);
 		}
 
 		return null;
 	}
 
 	// Editor
 	public IEditorInput getEditorInput(Object element) {
 		if (element instanceof File) {
 			return new ExternalFileEditorInput((File) element);
 		} else if (element instanceof IFile) {
 			return new FileEditorInput((IFile) element);
 		} else if (element instanceof ILineBreakpoint) {
 			return new FileEditorInput((IFile) ((ILineBreakpoint) element)
 					.getMarker().getResource());
 		}
 		return null;
 	}
 
 	public abstract String getEditorId(IEditorInput input, Object element);
 }
