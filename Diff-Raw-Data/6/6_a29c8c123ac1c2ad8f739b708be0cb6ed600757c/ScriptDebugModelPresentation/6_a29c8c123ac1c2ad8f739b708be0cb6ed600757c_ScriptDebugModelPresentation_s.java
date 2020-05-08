 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui;
 
 import java.net.URI;
 import java.util.HashMap;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IDebugElement;
 import org.eclipse.debug.core.model.IErrorReportingExpression;
 import org.eclipse.debug.core.model.IExpression;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.core.model.ISourceLocator;
 import org.eclipse.debug.core.model.IThread;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.debug.core.model.IVariable;
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.debug.ui.IValueDetailListener;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.debug.core.DLTKDebugConstants;
 import org.eclipse.dltk.debug.core.ScriptDebugManager;
 import org.eclipse.dltk.debug.core.model.IScriptBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptExceptionBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptLineBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptSpawnpoint;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptType;
 import org.eclipse.dltk.debug.core.model.IScriptValue;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 import org.eclipse.dltk.debug.core.model.IScriptWatchpoint;
 import org.eclipse.dltk.debug.ui.breakpoints.BreakpointUtils;
 import org.eclipse.dltk.internal.core.Openable;
 import org.eclipse.dltk.internal.core.util.HandleFactory;
 import org.eclipse.dltk.internal.debug.ui.DetailFormatter;
 import org.eclipse.dltk.internal.debug.ui.ExternalFileEditorInput;
 import org.eclipse.dltk.internal.debug.ui.ScriptDetailFormattersManager;
 import org.eclipse.dltk.internal.debug.ui.ScriptEvaluationContextManager;
 import org.eclipse.dltk.internal.ui.editor.ExternalStorageEditorInput;
 import org.eclipse.dltk.internal.ui.search.DLTKSearchScopeFactory;
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.launching.debug.DebuggingEngineManager;
 import org.eclipse.dltk.launching.debug.IDebuggingEngine;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 
 public abstract class ScriptDebugModelPresentation extends LabelProvider
 		implements IDebugModelPresentation {
 
 	private HashMap fAttributes = new HashMap();
 
 	// TODO: move to properties file
 	protected static final String SUSPENDED_LABEL = "suspended"; //$NON-NLS-1$
 	protected static final String RUNNING_LABEL = "running"; //$NON-NLS-1$
 
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
 			return NLS.bind(
 					Messages.ScriptDebugModelPresentation_debugTargetText,
 					engine.getName(), target.getSessionId());
 		}
 
 		return target.toString();
 	}
 
 	// Text
 	protected String getThreadText(IScriptThread thread) {
 		try {
 			return NLS.bind(Messages.ScriptDebugModelPresentation_threadText,
 					thread.getName(), thread.isSuspended() ? SUSPENDED_LABEL
 							: RUNNING_LABEL);
 
 		} catch (DebugException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return thread.toString();
 	}
 
 	protected static IPath getStackFrameRelativePath(
 			IScriptStackFrame stackFrame) throws CoreException {
 		final URI uri = stackFrame.getSourceURI();
 		final IProject project = getProject(stackFrame);
 
 		final IPath projectPath = new Path(project.getLocationURI().getPath());
 		final IPath realPath = new Path(uri.getPath());
 
 		if (projectPath.isPrefixOf(realPath)) {
 			IPath path = Path.EMPTY;
 			int index = projectPath.segmentCount();
 			while (index < realPath.segmentCount()) {
 				path = path.append(realPath.segment(index));
 				++index;
 			}
 			return path;
 		} else {
 			return realPath;
 		}
 	}
 
 	protected String getStackFrameText(IScriptStackFrame stackFrame) {
 		// TODO: improve later
 		try {
 			String sourceLine = stackFrame.getSourceLine();
 			final URI sourceURI = stackFrame.getSourceURI();
 
 			// Check if source line is empty
 			if (sourceLine == null || sourceLine.length() == 0) {
 
 				final ILaunch launch = stackFrame.getLaunch();
 				final ISourceLocator sourceLocator = launch.getSourceLocator();
 				if (sourceLocator != null) {
 					final Object object = sourceLocator
 							.getSourceElement(stackFrame);
 
 					if (object instanceof IFile) {
 						final IDocumentProvider provider = DLTKUIPlugin
 								.getDocumentProvider();
 
 						final IDocument document = provider
 								.getDocument(new FileEditorInput((IFile) object));
 
 						if (document != null) {
 							try {
 								sourceLine = retrieveStackFrameLine(stackFrame,
 										document);
 							} catch (BadLocationException e) {
 								DLTKDebugUIPlugin.log(e);
 							}
 						}
 					} else if (DLTKDebugConstants.UNKNOWN_SCHEME
 							.equalsIgnoreCase(sourceURI.getScheme())) {
 						sourceLine = sourceURI.getFragment();
 					} else {
 						sourceLine = toString(sourceURI);
 					}
 				}
 			}
 
 			// Check if source line is empty (again)
 			if (sourceLine == null || sourceLine.length() == 0) {
 				final int level = stackFrame.getStack().size()
 						- stackFrame.getLevel() - 1;
 				sourceLine = NLS.bind(
 						Messages.ScriptDebugModelPresentation_stackFrameText,
 						new Integer(level));
 			}
 
 			// TODO: may be make external option for file:line
 			if (stackFrame.getLineNumber() > 0) {
 				if (DLTKDebugConstants.FILE_SCHEME.equalsIgnoreCase(sourceURI
 						.getScheme())) {
 					final IPath path = getStackFrameRelativePath(stackFrame);
 					return NLS
 							.bind(
 									Messages.ScriptDebugModelPresentation_stackFrameText2,
 									new Object[] {
 											sourceLine,
 											path.toString(),
 											new Integer(stackFrame
 													.getLineNumber()) });
 				} else {
 					return NLS
 							.bind(
 									Messages.ScriptDebugModelPresentation_stackFrameText3,
 									sourceLine, new Integer(stackFrame
 											.getLineNumber()));
 				}
 			} else {
 				return sourceLine;
 			}
 		} catch (CoreException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return stackFrame.toString();
 	}
 
 	/**
 	 * @param uri
 	 * @return
 	 */
 	private static String toString(URI uri) {
 		StringBuffer sb = new StringBuffer();
 		if (uri.getScheme() != null) {
 			sb.append(uri.getScheme());
 			sb.append(':');
 		}
 		if (uri.isOpaque()) {
 			sb.append(uri.getSchemeSpecificPart());
 		} else {
 			final String host = uri.getHost();
 			if (host != null) {
 				sb.append("//"); //$NON-NLS-1$
 				if (uri.getUserInfo() != null) {
 					sb.append(uri.getUserInfo());
 					sb.append('@');
 				}
 				boolean needBrackets = (host.indexOf(':') >= 0)
 						&& !host.startsWith("[") && !host.endsWith("]"); //$NON-NLS-1$ //$NON-NLS-2$
 				if (needBrackets)
 					sb.append('[');
 				sb.append(host);
 				if (needBrackets)
 					sb.append(']');
 				if (uri.getPort() != -1) {
 					sb.append(':');
 					sb.append(uri.getPort());
 				}
 			} else if (uri.getAuthority() != null) {
 				sb.append("//"); //$NON-NLS-1$
 				sb.append(uri.getAuthority());
 			}
 			if (uri.getPath() != null)
 				sb.append(uri.getPath());
 			if (uri.getQuery() != null) {
 				sb.append('?');
 				sb.append(uri.getQuery());
 			}
 		}
 		if (uri.getFragment() != null) {
 			sb.append('#');
 			sb.append(uri.getFragment());
 		}
 		return sb.toString();
 	}
 
 	private static String retrieveStackFrameLine(IScriptStackFrame frame,
 			final IDocument document) throws BadLocationException,
 			DebugException {
 		if (frame.getBeginLine() > 0 && frame.getEndLine() > 0) {
 			final IRegion region = document.getLineInformation(frame
 					.getBeginLine() - 1);
 			final int start = region.getOffset() + frame.getBeginColumn();
 			final int len;
 			if (frame.getBeginLine() == frame.getEndLine()) {
 				len = frame.getEndColumn() - frame.getBeginColumn() + 1;
 			} else {
 				len = region.getLength() - frame.getBeginColumn();
 			}
 			return document.get(start, len).trim();
 		}
 		final IRegion region = document.getLineInformation(frame
 				.getLineNumber() - 1);
 		return document.get(region.getOffset(), region.getLength()).trim();
 	}
 
 	/**
 	 * Returns the text that will be displayed for the variable name when the
 	 * 'Show Columns' layout option <strong>is</strong> enabled.
 	 */
 	public String getVariableName(IVariable variable) throws DebugException {
 		return variable.getName();
 	}
 
 	/**
 	 * Returns the text that will be displayed in the Variable view when the
 	 * 'Show Columns' layout option <strong>is not</strong> enabled.
 	 */
 	public String getVariableText(IScriptVariable variable) {
 		try {
 			String name = getVariableName(variable);
 			IScriptValue value = (IScriptValue) variable.getValue();
 			if (value != null) {
 				if (isShowVariableTypeNames()) {
 					IScriptType type = value.getType();
 					if (type != null) {
 						String typeName = getTypeNameText(type);
 						name = typeName + " " + name; //$NON-NLS-1$
 					}
 				}
 
 				String valueText = getValueText(value);
 
 				if (valueText != null && valueText.length() > 0) {
 					return name + " = " + valueText; //$NON-NLS-1$
 				}
 			}
 
 			return name;
 		} catch (DebugException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return variable.toString();
 	}
 
 	/**
 	 * Returns <code>true</code> if the contents of the variable should be
 	 * displayed in the Variables view instead of information about the variable
 	 * type.
 	 */
 	protected boolean isShowLabelDetails(IScriptValue value) {
 		IScriptDebugTarget target = (IScriptDebugTarget) value.getDebugTarget();
 		String natureId = target.getLanguageToolkit().getNatureId();
 
 		IPreferenceStore store = DLTKDebugUILanguageManager.getLanguageToolkit(
 				natureId).getPreferenceStore();
 		String details = store
 				.getString(IDLTKDebugUIPreferenceConstants.PREF_SHOW_DETAILS);
 
 		boolean showDetails = false;
 		if (IDLTKDebugUIPreferenceConstants.INLINE_ALL.equals(details)) {
 			showDetails = true;
 		} else if (IDLTKDebugUIPreferenceConstants.INLINE_FORMATTERS
 				.equals(details)) {
 			DetailFormatter formatter = ScriptDetailFormattersManager
 					.getDefault(natureId).getDetailFormatter(value);
 			showDetails = (formatter != null && formatter.isEnabled());
 		}
 
 		return showDetails;
 	}
 
 	/**
 	 * Returns the text that will be displayed in the Variable view when the
 	 * 'Show Columns' layout option <strong>is</strong> enabled.
 	 */
 	protected String getValueText(IScriptValue value) {
 		String text = null;
 		if (isShowLabelDetails(value)) {
 			// the same value shown in the details pane will be displayed here
 			text = getVariableDetail(value);
 		} else {
 			try {
 				text = value.getValueString();
 			} catch (DebugException e) {
 				DebugPlugin.log(e);
 				text = value.toString();
 			}
 		}
 
 		return text;
 	}
 
 	private String getVariableDetail(IScriptValue value) {
 		final String[] detail = new String[1];
 		final Object lock = new Object();
 		computeDetail(value, new IValueDetailListener() {
 			public void detailComputed(IValue computedValue, String result) {
 				synchronized (lock) {
 					detail[0] = result;
 					lock.notifyAll();
 				}
 			}
 		});
 		synchronized (lock) {
 			if (detail[0] == null) {
 				try {
 					lock.wait(5000);
 				} catch (InterruptedException e1) {
 					// Fall through
 				}
 			}
 		}
 		return detail[0];
 	}
 
 	/**
 	 * Returns the text that will be displayed in the 'details' pane of the
 	 * Variables view. This method will be invoked when no detail formatter is
 	 * available for the script variable.
 	 */
 	public String getDetailPaneText(IScriptValue value) {
 		return value.getDetailsString();
 	}
 
 	protected String renderUnknownValue(IScriptValue value)
 			throws DebugException {
 		return value.getValueString();
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
 				if (lineNumber >= 0 && file != null) {
 					sb
 							.append(NLS
 									.bind(
 											Messages.ScriptDebugModelPresentation_breakpointText,
 											new Object[] { language, file,
 													new Integer(lineNumber),
 													fieldName }));
 				} else {
 					sb
 							.append(NLS
 									.bind(
 											Messages.ScriptDebugModelPresentation_breakpointNoResourceText,
 											language, fieldName));
 				}
 			} else if (breakpoint instanceof IScriptLineBreakpoint) { // IScriptLineBreakpoint
 				IScriptLineBreakpoint b = (IScriptLineBreakpoint) breakpoint;
 
 				final String file = b.getResourceName();
 				final int lineNumber = b.getLineNumber();
 
 				sb
 						.append(NLS
 								.bind(
 										Messages.ScriptDebugModelPresentation_breakpointText2,
 										new Object[] { language, file,
 												new Integer(lineNumber) }));
 			} else if (breakpoint instanceof IScriptExceptionBreakpoint) {
 				IScriptExceptionBreakpoint b = (IScriptExceptionBreakpoint) breakpoint;
 				String typeName = b.getTypeName();
 				if (b.isSuspendOnSubclasses()) {
 					typeName += Messages.ScriptDebugModelPresentation_breakpointText3;
 				}
 
 				sb.append(NLS.bind(
 						Messages.ScriptDebugModelPresentation_breakpointText4,
 						language, typeName));
 
 				/*
 				 * TODO: Uncomment this comment when add support for caught and
 				 * uncaught exceptions
 				 * 
 				 * String state; boolean c= b.isCaught(); boolean u=
 				 * b.isUncaught(); if (c && u) { state= "caught and uncaught"; }
 				 * else if (c) { state= "caught"; } else if (u) { state=
 				 * "uncaught"; } else { state= ": must specify caught or
 				 * uncaught exception"; } sb.append(MessageFormat .format("{0}:
 				 * {1}: {2}", new Object[] { language, typeName, state}));
 				 */
 			}
 
 			if (hitCount != -1) {
 				sb.append(NLS.bind(
 						Messages.ScriptDebugModelPresentation_breakpointText5,
 						new Integer(hitCount)));
 			}
 
 			return sb.toString();
 		} catch (CoreException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 
 		return breakpoint.toString();
 	}
 
 	protected String getExpressionText(IExpression expression) {
 		String expressionText = expression.getExpressionText();
 
 		if (expression instanceof IErrorReportingExpression) {
 			IErrorReportingExpression errorExpression = (IErrorReportingExpression) expression;
 			if (errorExpression.hasErrors()) {
 				return expressionText;
 			}
 		}
 
 		IScriptValue value = (IScriptValue) expression.getValue();
 		if (value != null) {
 			if (isShowVariableTypeNames()) {
 				IScriptType type = value.getType();
 				if (type != null) {
 					String typeName = getTypeNameText(type);
 					expressionText = typeName + " " + expressionText; //$NON-NLS-1$
 				}
 			}
 
 			return NLS.bind(
 					Messages.ScriptDebugModelPresentation_expressionText,
 					expressionText, getValueText(value));
 		}
 
 		return expressionText;
 	}
 
 	public String getTypeNameText(IScriptType type) {
 		return type.getName();
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
 		} else if (element instanceof IScriptValue) {
 			return getValueText((IScriptValue) element);
 		} else if (element instanceof IExpression) {
 			return getExpressionText((IExpression) element);
 		} else if (element instanceof IScriptType) {
 			return getTypeNameText((IScriptType) element);
 		}
 
 		return null;
 	}
 
 	// Details
 	public void computeDetail(IValue value, IValueDetailListener listener) {
 		if (value instanceof IScriptValue) {
 			IScriptDebugTarget target = (IScriptDebugTarget) value
 					.getDebugTarget();
 			IScriptThread thread = getEvaluationThread(target);
 			if (thread == null) {
 				listener.detailComputed(value,
 						getValueText((IScriptValue) value));
 			} else {
 				String natureId = target.getLanguageToolkit().getNatureId();
 				ScriptDetailFormattersManager.getDefault(natureId)
 						.computeValueDetail((IScriptValue) value, thread,
 								listener);
 			}
 		} else {
 			listener.detailComputed(value, value.toString());
 		}
 	}
 
 	public void setAttribute(String id, Object value) {
 		if (value == null) {
 			return;
 		}
 		synchronized (fAttributes) {
 			fAttributes.put(id, value);
 		}
 	}
 
 	protected boolean isShowVariableTypeNames() {
 		synchronized (fAttributes) {
 			Boolean show = (Boolean) fAttributes
 					.get(DISPLAY_VARIABLE_TYPE_NAMES);
 			show = show == null ? Boolean.FALSE : show;
 			return show.booleanValue();
 		}
 	}
 
 	// Images
 	protected Image getBreakpointImage(IScriptBreakpoint breakpoint) {
 		if (breakpoint instanceof IScriptSpawnpoint) {
 			try {
 				return ScriptDebugImages
 						.get(breakpoint.isEnabled() ? ScriptDebugImages.IMG_OBJS_SPAWNPOINT
 								: ScriptDebugImages.IMG_OBJS_SPAWNPOINT_DISABLED);
 			} catch (CoreException e) {
 				DLTKDebugUIPlugin.log(e);
 			}
 		}
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
 		if (element instanceof IFileHandle) {
 			return new ExternalFileEditorInput((IFileHandle) element);
 		} else if (element instanceof IFile) {
 			return new FileEditorInput((IFile) element);
 		} else if (element instanceof ILineBreakpoint) {
 			return getLineBreakpointEditorInput(element);
 		} else if (element instanceof IStorage) {
 			return new ExternalStorageEditorInput((IStorage) element);
 		}
 		return null;
 	}
 
 	private IEditorInput getLineBreakpointEditorInput(Object element) {
 		ILineBreakpoint bp = (ILineBreakpoint) element;
 		IMarker marker = bp.getMarker();
 		IResource resource = marker.getResource();
 		if (resource instanceof IFile) {
 			return new FileEditorInput((IFile) resource);
 		}
 
 		// else
 		try {
 			final String location = (String) marker
 					.getAttribute(IMarker.LOCATION);
 			if (location == null) {
 				return null;
 			}
 			IPath path = Path.fromPortableString(location);
 			String debugModelId = bp.getModelIdentifier();
 			String natureId = ScriptDebugManager.getInstance()
 					.getNatureByDebugModel(debugModelId);
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 					.getLanguageToolkit(natureId);
 			if (toolkit != null) {
 				HandleFactory fac = new HandleFactory();
 				IDLTKSearchScope scope = DLTKSearchScopeFactory.getInstance()
 						.createWorkspaceScope(true, toolkit);
 				Openable openable = fac.createOpenable(path.toString(), scope);
 
 				if (openable instanceof IStorage) {
 					return new ExternalStorageEditorInput((IStorage) openable);
 				}
 			}
 		} catch (CoreException e) {
 			DLTKUIPlugin.log(e);
 		}
 		return null;
 	}
 
 	public abstract String getEditorId(IEditorInput input, Object element);
 }
