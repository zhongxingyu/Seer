 package edu.illinois.concurrentaccessview.views;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsole;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.TextConsoleViewer;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.ViewPart;
 
 import sabazios.domains.ConcurrentAccess;
 import sabazios.domains.ConcurrentAccesses;
 import sabazios.domains.Loop;
 import sabazios.domains.ObjectAccess;
 import sabazios.util.CodeLocation;
 
 /**
  * Main class for the concurrent access view, which is used to display
  * concurrent accesses in code.
  */
 public class ConcurrentAccessView extends ViewPart {
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "edu.illinois.concurrentaccessview.views.ConcurrentAccessView";
 
 	/**
 	 * A tree viewer used to display concurrent accesses.
 	 */
 	private TreeViewer viewer;
 	
 	/**
 	 * A console used to display the trace of a concurrent access.
 	 */
 	private JavaStackTraceConsole console;
 
 	/**
 	 * Run when the user double-clicks an item in the tree viewer.
 	 */
 	private Action doubleClickAction;
 
 	/**
 	 * The file in which concurrent accesses are being analyzed.
 	 */
 	private IFile file;
 	
 	/**
 	 * A map of code locations to warning markers.
 	 */
 	private Map<CodeLocation, IMarker> markers = new HashMap<CodeLocation, IMarker>();
 
 	/**
 	 * Clear all the markers in the current file.
 	 */
 	private void clearMarkers() {
 		for (IMarker marker : this.markers.values()) {
 			try {
 				marker.delete();
 			} catch (CoreException e) {
 				// do nothing
 			}
 		}
 		this.markers.clear();
 	}
 
 	/**
 	 * Create a warning marker from the given concurrent access.
 	 * @param oa A concurrent access object
 	 * @throws CoreException
 	 */
 	private void createMarker(ObjectAccess oa) throws CoreException {
 		CodeLocation cl = oa.getCodeLocation();
 		IMarker marker = this.getFile().createMarker(IMarker.PROBLEM);
 		marker.setAttributes(
 				new String[] {IMarker.MESSAGE, IMarker.SEVERITY, IMarker.LINE_NUMBER, IDE.EDITOR_ID_ATTR},
 				new Object[] {"Concurrent access detected: " + oa.toString(), IMarker.SEVERITY_WARNING, new Integer(cl.getLineNo()), "org.eclipse.jdt.ui.CompilationUnitEditor"});
 		this.markers.put(cl, marker);
 	}
 
 	/**
 	 * Create warning markers from the given concurrent accesses.
 	 * @param cas A concurrent accesses object
 	 */
 	private void createMarkers(ConcurrentAccesses cas) {
 		for (Object obj : cas.values()) {
 			if (obj instanceof Set) {
 				for (Object obj2 : (Set) obj) {
 					if (obj2 instanceof ConcurrentAccess) {
 						ConcurrentAccess ca = (ConcurrentAccess) obj2;
 						for (Object obj3 : ca.alphaAccesses) {
 							if (obj3 instanceof ObjectAccess) {
 								ObjectAccess oa = (ObjectAccess) obj3;
 								try {
 									this.createMarker(oa);
 								} catch (CoreException e) {
 									// do nothing
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		// Set the layout of the parent control.
 		parent.setLayout(new FillLayout());
 		
 		// Create a sash form with two equal-width columns.
 		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
 		form.setLayout(new FillLayout());
 		
 		// Create the tree viewer.
 		Composite child1 = new Composite(form, SWT.NONE);
 		child1.setLayout(new FillLayout());
 		viewer = new TreeViewer(child1, SWT.NONE);
 		viewer.setContentProvider(new ViewContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
 		viewer.setSorter(new NameSorter());
 		viewer.setInput(getViewSite());
 		
 		// Create and hook all actions.
 		makeActions();
 		hookDoubleClickAction();
 		
 		// Create the trace console.
 		console = new JavaStackTraceConsole();
         IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
 		consoleManager.addConsoles(new IConsole[]{console});
 		Composite child2 = new Composite(form, SWT.NONE);
 		child2.setLayout(new FillLayout());
 		new TextConsoleViewer(child2, console);
		
		// Make sash form have two equal-width columns.
		form.setWeights(new int[] {50, 50});
 	}
 
 	public IFile getFile() {
 		return file;
 	}
 
 	/**
 	 * Hooks a double-click listener to the tree viewer.
 	 */
 	private void hookDoubleClickAction() {
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				doubleClickAction.run();
 			}
 		});
 	}
 	
 	/**
 	 * Creates all actions needed by this view.
 	 */
 	private void makeActions() {
 		
 		// Create the double-click action.
 		doubleClickAction = new Action() {
 			public void run() {
 				
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection) selection).getFirstElement();
 				
 				IDocument doc = console.getDocument();
 				doc.set(obj.toString());
 				
 				try {
 					if (obj instanceof Map.Entry) {
 						Object obj2 = ((Map.Entry) obj).getKey();
 						if (obj2 instanceof Loop) {
 							Loop loop = (Loop) obj2;
 							openCodeLocation(loop.getCodeLocation());
 						}
 					} else if (obj instanceof ObjectAccess) {
 						ObjectAccess oa = (ObjectAccess) obj;
 						openCodeLocation(oa.getCodeLocation());
 					}
 				} catch (CoreException e) {
 					// do nothing
 				}
 			}
 		};
 	}
 
 	/**
 	 * Open the selected file in the Java editor and put a marker at the given
 	 * code location.
 	 * @param codeLocation A code location in the currently selected file.
 	 * @throws CoreException
 	 */
 	private void openCodeLocation(CodeLocation codeLocation) throws CoreException {
 		
 		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		IMarker marker;
 		
 		if (this.markers.containsKey(codeLocation)) {
 			marker = this.markers.get(codeLocation);
 		} else {
 			marker = this.getFile().createMarker(IMarker.TEXT);
 			marker.setAttributes(
 					new String[] {IMarker.LINE_NUMBER, IDE.EDITOR_ID_ATTR},
 					new Object[] {new Integer(codeLocation.getLineNo()), "org.eclipse.jdt.ui.CompilationUnitEditor"});
 		}
 		
 		IDE.openEditor(activePage, marker);
 	}
 	
 	public void setFile(IFile file) {
 		this.file = file;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
 	 */
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	/**
 	 * Sets the given object as the input of the tree viewer.
 	 * @param input
 	 */
 	public void setInput(Object input) {
 		this.viewer.setInput(input);
 		if (input instanceof ConcurrentAccesses) {
 			this.clearMarkers();
 			this.createMarkers((ConcurrentAccesses) input);
 		}
 	}
 }
