 package com.laboki.eclipse.plugin.jcolon.inserter;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 
 import lombok.ToString;
 import lombok.extern.java.Log;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartService;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 import com.google.common.collect.Lists;
 
 @Log
 @ToString
 public enum EditorContext {
 	INSTANCE;
 
 	private static final String LINK_SLAVE = "org.eclipse.ui.internal.workbench.texteditor.link.slave";
 	private static final String LINK_MASTER = "org.eclipse.ui.internal.workbench.texteditor.link.master";
 	private static final String LINK_TARGET = "org.eclipse.ui.internal.workbench.texteditor.link.target";
 	private static final String LINK_EXIT = "org.eclipse.ui.internal.workbench.texteditor.link.exit";
 	public static final Display DISPLAY = EditorContext.getDisplay();
 	private static final String JDT_ANNOTATION_ERROR = "org.eclipse.jdt.ui.error";
 	private static final FlushEventsRunnable FLUSH_EVENTS_RUNNABLE = new EditorContext.FlushEventsRunnable();
 	public static final IJobManager JOB_MANAGER = Job.getJobManager();
 	public static final String TASK_FAMILY_NAME = "SEMI_COLON_ERROR_CHECKER";
 	public static final int DELAY_TIME_IN_MILLISECONDS = 250;
 	private static final List<String> LINK_ANNOTATIONS = Lists.newArrayList(EditorContext.LINK_EXIT, EditorContext.LINK_TARGET, EditorContext.LINK_MASTER, EditorContext.LINK_SLAVE);
 
 	public static Display getDisplay() {
 		return PlatformUI.getWorkbench().getDisplay();
 	}
 
 	public static Shell getShell() {
 		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
 	}
 
 	public static void asyncExec(final Runnable runnable) {
 		if (EditorContext.isNull(EditorContext.DISPLAY) || EditorContext.DISPLAY.isDisposed()) return;
 		EditorContext.DISPLAY.asyncExec(runnable);
 	}
 
 	public static void flushEvents() {
 		EditorContext.asyncExec(EditorContext.FLUSH_EVENTS_RUNNABLE);
 	}
 
 	private static final class FlushEventsRunnable implements Runnable {
 
 		public FlushEventsRunnable() {}
 
 		@Override
 		public void run() {
 			while (EditorContext.DISPLAY.readAndDispatch())
 				EditorContext.DISPLAY.update();
 			EditorContext.DISPLAY.update();
 		}
 	}
 
 	public static IEditorPart getEditor() {
 		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 	}
 
 	public static StyledText getBuffer(final IEditorPart editor) {
 		return (StyledText) editor.getAdapter(Control.class);
 	}
 
 	public static SourceViewer getView() {
 		return (SourceViewer) EditorContext.getEditor().getAdapter(ITextOperationTarget.class);
 	}
 
 	public static SourceViewer getView(final IEditorPart editor) {
 		return (SourceViewer) editor.getAdapter(ITextOperationTarget.class);
 	}
 
 	public static boolean hasJDTErrors(final IEditorPart editor) {
 		return EditorContext.hasJDTAnnotationError(editor);
 	}
 
 	static void syncFile(final IEditorPart editor) {
 		EditorContext.flushEvents();
 		EditorContext.tryToSyncFile(editor);
 	}
 
 	private static void tryToSyncFile(final IEditorPart editor) {
 		try {
 			EditorContext.getFile(editor).refreshLocal(IResource.DEPTH_INFINITE, null);
 		} catch (final CoreException e) {
 			EditorContext.log.log(Level.FINEST, "Failed to sync IFile resource", e);
 		} finally {
 			EditorContext.flushEvents();
 		}
 	}
 
 	public static IFile getFile(final IEditorPart editor) {
 		try {
 			return ((FileEditorInput) editor.getEditorInput()).getFile();
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	private static boolean hasJDTAnnotationError(final IEditorPart editor) {
 		try {
 			return EditorContext._hasJDTAnnotationError(editor);
 		} catch (final Exception e) {}
 		return false;
 	}
 
 	private static boolean _hasJDTAnnotationError(final IEditorPart editor) {
 		final Iterator<Annotation> iterator = EditorContext.getView(editor).getAnnotationModel().getAnnotationIterator();
 		while (iterator.hasNext())
 			if (EditorContext.isJdtError(iterator)) return true;
 		return false;
 	}
 
 	private static boolean isJdtError(final Iterator<Annotation> iterator) {
 		return iterator.next().getType().equals(EditorContext.JDT_ANNOTATION_ERROR);
 	}
 
 	public static boolean isNotAJavaEditor(final IEditorPart part) {
 		return !EditorContext.isAJavaEditor(part);
 	}
 
 	public static boolean isAJavaEditor(final IEditorPart part) {
 		final IFile file = EditorContext.getFile(part);
 		if (file == null) return false;
 		return JavaCore.isJavaLikeFileName(file.getName());
 	}
 
 	public static IDocument getDocument(final IEditorPart editor) {
 		final ITextEditor textEditor = (ITextEditor) editor;
 		return textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
 	}
 
 	public static IPartService getPartService() {
 		return (IPartService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IPartService.class);
 	}
 
 	public static boolean isNotNull(final Object object) {
 		return !EditorContext.isNull(object);
 	}
 
 	public static boolean isNull(final Object object) {
 		return object == null;
 	}
 
 	public static void cancelJobsBelongingTo(final String jobName) {
 		EditorContext.JOB_MANAGER.cancel(jobName);
 	}
 
 	public static boolean isInEditMode(final IEditorPart editor) {
		return EditorContext.hasSelection(editor) || EditorContext.hasBlockSelection(editor) || EditorContext.isInLinkMode(editor);
 	}
 
 	public static boolean isInLinkMode(final IEditorPart editor) {
 		EditorContext.syncFile(editor);
 		return EditorContext.hasLinkAnnotations(editor);
 	}
 
 	private static boolean hasLinkAnnotations(final IEditorPart editor) {
 		final Iterator<Annotation> iterator = EditorContext.getView(editor).getAnnotationModel().getAnnotationIterator();
 		while (iterator.hasNext())
 			if (EditorContext.isLinkModeAnnotation(iterator)) return true;
 		return false;
 	}
 
 	private static boolean isLinkModeAnnotation(final Iterator<Annotation> iterator) {
 		if (EditorContext.LINK_ANNOTATIONS.contains(iterator.next().getType())) return true;
 		return false;
 	}
 
 	public static boolean hasSelection(final IEditorPart editor) {
 		return EditorContext.getBuffer(editor).getSelectionCount() > 0;
 	}
 
 	public static boolean hasBlockSelection(final IEditorPart editor) {
 		return EditorContext.getBuffer(editor).getBlockSelection();
 	}
 }
