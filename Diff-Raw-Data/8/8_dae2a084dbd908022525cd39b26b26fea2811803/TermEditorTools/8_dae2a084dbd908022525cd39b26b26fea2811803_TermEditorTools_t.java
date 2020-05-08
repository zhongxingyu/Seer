 package org.meta_environment.eclipse.terms;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.editor.UniversalEditor;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.meta_environment.eclipse.Tool;
 import org.meta_environment.eclipse.sdf.Activator;
 
 import aterm.ATermAppl;
 import aterm.ATermList;
 
 public class TermEditorTools extends Tool {
 	private final static TermEditorTools sInstance = new TermEditorTools();
	static{
		sInstance.connect();
	}
 	
 	private TermEditorTools(){
 		super("term-language-registrar");
 	}
 	
 	public static TermEditorTools getInstance() {
 		return sInstance;
 	}
 
 	public List<String> getLanguages() {
 		ATermList result = (ATermList) sendRequest(factory.make("get-languages"));
 		List<String> list = new LinkedList<String>();
 		
 		for (; !result.isEmpty(); result = result.getNext()) {
 			list.add(((ATermAppl) result.getFirst()).getName());
 		}
 		
 		return list;
 	}
 	
 	public void setLanguage(String filename, String language) {
 		sendEvent(factory.make("set-language(<str>,<str>)", filename, language));
 	}
 	
 	public void open(String filename) {
 		IWorkbench wb = PlatformUI.getWorkbench();
 		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
 		
 		if (win != null) {
 		  IWorkbenchPage page = win.getActivePage();
 		  
 		  if (page != null) {
 			  IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
 			  try {
 				page.openEditor(new FileEditorInput(file), UniversalEditor.EDITOR_ID);
 			} catch (PartInitException e) {
 				Activator.getInstance().logException("Could not open editor for: " + filename, e);
 			}
 		  }
 		}
 	}
 }
