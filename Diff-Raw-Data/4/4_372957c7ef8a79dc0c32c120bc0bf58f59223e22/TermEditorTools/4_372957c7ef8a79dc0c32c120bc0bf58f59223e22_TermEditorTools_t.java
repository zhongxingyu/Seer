 package org.meta_environment.eclipse.terms;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.editor.UniversalEditor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.meta_environment.eclipse.Tool;
 import org.meta_environment.eclipse.sdf.Activator;
 
 import aterm.ATerm;
 import aterm.ATermAppl;
 import aterm.ATermList;
 
 public class TermEditorTools extends Tool {
 	private static TermEditorTools sInstance;
 	
 	private Map<String, Map<String, String>> actions = new HashMap<String, Map<String, String>>();
 	
 	private TermEditorTools(){
 		super("term-language-registrar");
 	}
 	
 	public static TermEditorTools getInstance() {
 		if (sInstance == null) {
 			sInstance = new TermEditorTools();
 			sInstance.connect();
 		}
 		return sInstance;
 	}
 
 	public List<String> getLanguages() {
 		ATermAppl response = sendRequest(factory.make("get-languages"));
 		
 		ATermList result = (ATermList) response.getArgument(0);
 		List<String> list = new LinkedList<String>();
 		
 		for (; !result.isEmpty(); result = result.getNext()) {
 			list.add(((ATermAppl) result.getFirst()).getName());
 		}
 		
 		return list;
 	}
 	
 	public String getLanguage(String filename) {
 		ATermAppl response = sendRequest(factory.make("get-language(<str>)", filename));
 		ATerm language = response.getArgument(0);
 		
 		return ((ATermAppl) language).getName();
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
 	
 	public void registerAction(String language, String label, String tooltip, String action) {
 		System.err.println("registering " + language + " " + label + " " + action);	
 		Map<String, String> map = getActionMap(language);
 		map.put(label, action);
 	}
 
 	private String canonical(String label) {
 		int i = label.lastIndexOf('/');
 		if (i != -1) {
 			return label.substring(i+1);
 		}
		return label;
 	}
 
 	private Map<String, String> getActionMap(String language) {
 		Map<String, String> map = actions.get(canonical(language));
 		
 		if (map == null) {
 			map = new HashMap<String,String>();
 			actions.put(canonical(language), map);
 		}
 		
 		return map;
 	}
 	
 	public List<Action> getDynamicActions(final String language, final String filename) {
 	  Map<String, String> map = getActionMap(language);
 	  List<Action> result = new LinkedList<Action>();
 	  
 	  for (String label : map.keySet()) {
 		  final String action = map.get(label);
 		  result.add(new Action(label) {
 			public void run() {
 				System.err.println("dynamic term action triggered: " + action + " for language " + language + " on file " + filename);
 				  performAction(action, language, filename);
 			}
 		  });
 	  }
 	  
 	  return result;
 	}
 	
 	public void unregisterAction(String language, String label) {
 		System.err.println("unregister " + language + " " + label);
 		Map<String, String> map = getActionMap(language);
 		map.remove(label);
 	}
 	
 	private void performAction (String Action, String language, String Filename) {
 		this.sendEvent(factory.make("perform-action(<str>,<str>,<str>)", Action, language, Filename));
 	}
 }
