 package com.nflath.hungry;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class HungryUtils {
 	
 	private static ITextEditor getEditorForEvent(ExecutionEvent event ) throws ExecutionException {
 		IEditorPart part = HandlerUtil.getActiveEditorChecked(event);
 		return (ITextEditor) part;
 	}
 
 	public static IDocument getCurrentDocument(ExecutionEvent event) throws ExecutionException {
 		IDocumentProvider dp = getEditorForEvent(event).getDocumentProvider();
 		return dp.getDocument(getEditorForEvent(event).getEditorInput());
 	}
 	
 	public static int getCurrentOffset(ExecutionEvent event) throws ExecutionException {
 		ITextSelection selection = (ITextSelection) getEditorForEvent(event).getSelectionProvider().getSelection();
 		return selection.getOffset();
 	}
 }

