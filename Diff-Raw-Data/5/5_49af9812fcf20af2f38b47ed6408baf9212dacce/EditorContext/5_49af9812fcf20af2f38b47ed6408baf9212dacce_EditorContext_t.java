 package com.laboki.eclipse.plugin.jcolon.main;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartService;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 import com.google.common.collect.Lists;
 import com.laboki.eclipse.plugin.jcolon.events.ScheduleCheckErrorEvent;
 import com.laboki.eclipse.plugin.jcolon.task.Task;
 
 public enum EditorContext {
 	INSTANCE;
 
 	private static final String LINK_SLAVE = "org.eclipse.ui.internal.workbench.texteditor.link.slave";
 	private static final String LINK_MASTER = "org.eclipse.ui.internal.workbench.texteditor.link.master";
 	private static final String LINK_TARGET = "org.eclipse.ui.internal.workbench.texteditor.link.target";
 	private static final String LINK_EXIT = "org.eclipse.ui.internal.workbench.texteditor.link.exit";
 	private static final String JDT_ANNOTATION_ERROR = "org.eclipse.jdt.ui.error";
 	public static final String LISTENER_TASK = "jcolon eclipse listener handler task";
 	public static final String ERROR_CHECKING_TASK = "jcolon semicolon error checking task";
 	private static final IWorkbench WORKBENCH = PlatformUI.getWorkbench();
 	public static final Display DISPLAY = EditorContext.WORKBENCH.getDisplay();
 	public static final IJobManager JOB_MANAGER = Job.getJobManager();
 	public static final int SHORT_DELAY_TIME = 60;
 	public static final int LONG_DELAY_TIME = 1000;
 	private static final List<String> LINK_ANNOTATIONS = Lists.newArrayList(EditorContext.LINK_EXIT, EditorContext.LINK_TARGET, EditorContext.LINK_MASTER, EditorContext.LINK_SLAVE);
 
 	public static Display getDisplay() {
 		return EditorContext.DISPLAY;
 	}
 
 	public static void flushEvents() {
 		try {
 			EditorContext.tryToFlushEvent();
 		} catch (final Exception e) {}
 	}
 
 	private static void tryToFlushEvent() {
 		while (EditorContext.DISPLAY.readAndDispatch())
 			EditorContext.DISPLAY.update();
 	}
 
 	public static void asyncExec(final Runnable runnable) {
 		if (EditorContext.isInvalidDisplay()) return;
 		EditorContext.DISPLAY.asyncExec(runnable);
 	}
 
 	public static void syncExec(final Runnable runnable) {
 		if (EditorContext.isInvalidDisplay()) return;
 		EditorContext.DISPLAY.syncExec(runnable);
 	}
 
 	private static boolean isInvalidDisplay() {
 		return (EditorContext.DISPLAY == null) || EditorContext.DISPLAY.isDisposed();
 	}
 
 	public static Shell getShell() {
 		return EditorContext.WORKBENCH.getModalDialogShellProvider().getShell();
 	}
 
 	public static IEditorPart getEditor() {
 		return EditorContext.WORKBENCH.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 	}
 
 	public static IPartService getPartService() {
 		return (IPartService) EditorContext.WORKBENCH.getActiveWorkbenchWindow().getService(IPartService.class);
 	}
 
 	public static Control getControl(final IEditorPart editor) {
 		return (Control) editor.getAdapter(Control.class);
 	}
 
 	public static StyledText getBuffer(final IEditorPart editor) {
 		return (StyledText) editor.getAdapter(Control.class);
 	}
 
 	public static SourceViewer getView(final IEditorPart editor) {
 		return (SourceViewer) editor.getAdapter(ITextOperationTarget.class);
 	}
 
 	public static IAnnotationModel getAnnotationModel() throws Exception {
 		return EditorContext.getView(EditorContext.getEditor()).getAnnotationModel();
 	}
 
 	public static boolean hasJDTErrors(final IEditorPart editor) {
 		return EditorContext.hasJDTAnnotationError(editor);
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
 
 	public static void syncFile(final IEditorPart editor) {
 		try {
 			EditorContext.getFile(editor).refreshLocal(IResource.DEPTH_INFINITE, null);
 		} catch (final Exception e) {}
 	}
 
 	public static boolean isNotAJavaEditor(final IEditorPart part) {
 		return !EditorContext.isAJavaEditor(part);
 	}
 
 	public static boolean isAJavaEditor(final IEditorPart part) {
 		try {
 			return JavaCore.isJavaLikeFileName(EditorContext.getFile(part).getName());
 		} catch (final Exception e) {
 			return false;
 		}
 	}
 
 	public static IFile getFile(final IEditorPart editor) throws Exception {
 		return ((FileEditorInput) editor.getEditorInput()).getFile();
 	}
 
 	public static IDocument getDocument(final IEditorPart editor) {
 		return ((ITextEditor) editor).getDocumentProvider().getDocument(((ITextEditor) editor).getEditorInput());
 	}
 
 	public static void cancelAllJobs() {
 		EditorContext.cancelJobsBelongingTo(EditorContext.LISTENER_TASK, EditorContext.ERROR_CHECKING_TASK);
 	}
 
 	public static void cancelErrorCheckingJobs() {
 		EditorContext.cancelJobsBelongingTo(EditorContext.ERROR_CHECKING_TASK);
 	}
 
 	public static void cancelJobsBelongingTo(final String... jobNames) {
 		for (final String jobName : jobNames)
 			EditorContext.JOB_MANAGER.cancel(jobName);
 	}
 
 	public static boolean isInEditMode(final IEditorPart editor) {
 		try {
 			return EditorContext.hasSelection(editor) || EditorContext.hasBlockSelection(editor) || EditorContext.isInLinkMode(editor);
 		} catch (final Exception e) {
 			return true;
 		}
 	}
 
 	public static boolean hasSelection(final IEditorPart editor) {
 		return EditorContext.getBuffer(editor).getSelectionCount() > 0;
 	}
 
 	public static boolean hasBlockSelection(final IEditorPart editor) {
 		return EditorContext.getBuffer(editor).getBlockSelection();
 	}
 
 	public static boolean isInLinkMode(final IEditorPart editor) {
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
 
 	public static void scheduleErrorChecking(final EventBus eventBus) {
		EditorContext.cancelJobsBelongingTo(EditorContext.ERROR_CHECKING_TASK);
		EditorContext.scheduleErrorCheckingTask(eventBus);
	}

	private static void scheduleErrorCheckingTask(final EventBus eventBus) {
 		new Task(EditorContext.ERROR_CHECKING_TASK, EditorContext.SHORT_DELAY_TIME) {
 
 			@Override
 			public boolean shouldSchedule() {
 				return EditorContext.taskDoesNotExist(EditorContext.ERROR_CHECKING_TASK);
 			}
 
 			@Override
 			public boolean shouldRun() {
 				return EditorContext.taskDoesNotExist(EditorContext.LISTENER_TASK);
 			}
 
 			@Override
 			public void execute() {
 				eventBus.post(new ScheduleCheckErrorEvent());
 			}
 		}.begin();
 	}
 
 	public static boolean taskDoesNotExist(final String name) {
 		return EditorContext.JOB_MANAGER.find(name).length == 0;
 	}
 }
