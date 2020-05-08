 /*
  * Created on Jan 19, 2006
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package net.nansore.cedalion.eclipse;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.nansore.cedalion.execution.ExecutionContext;
 import net.nansore.cedalion.execution.ExecutionContextException;
 import net.nansore.cedalion.execution.Notifier;
 import net.nansore.cedalion.execution.TermInstantiationException;
 import net.nansore.cedalion.figures.TermFigure;
 import net.nansore.cedalion.figures.VisualTerm;
 import net.nansore.prolog.Compound;
 import net.nansore.prolog.PrologException;
 import net.nansore.prolog.PrologProxy;
 import net.nansore.prolog.Variable;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.jface.bindings.keys.KeyStroke;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 import org.eclipse.jface.fieldassist.TextContentAdapter;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 /**
  * @author boaz
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class CedalionEditor extends EditorPart implements ISelectionProvider, TermContext, DisposeListener, IViewOpener {
 
 	private final class CedalionProposalProvider implements IContentProposalProvider {
 		public IContentProposal[] getProposals(String incompleteText, int pos) {
 			if(currentTermFigure == null)
 				return new IContentProposal[]{};
 			return currentTermFigure.getProposals(incompleteText, pos);
 		}
 	}
 
 	private VisualTermWidget editorWidget;
     private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();
     private ISelection selection;
     private Map<Object, List<TermFigure>> termFigures = new HashMap<Object, List<TermFigure>>();
     private IFileEditorInput input;
 	private Font normalFont;
 	protected Font symbolFont;
 	private VisualTerm currentTermFigure;
 
     /* (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void doSave(IProgressMonitor monitor) {
 	    try {
 	    	PrologProxy prolog = Activator.getProlog();
 			ExecutionContext exe = new ExecutionContext(prolog);
 			exe.runProcedure(prolog.createCompound("cpi#saveFile", getResource(), input.getFile().getLocation().toString()));
 	        firePropertyChange(PROP_DIRTY);
 	        // Reload the content
 	        Activator.getDefault().loadResource(input.getFile());
 	        refresh();
 	        System.gc();
         } catch (PrologException e) {
             e.printStackTrace();
 		} catch (TermInstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionContextException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TermVisualizationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
 	 */
 	public void doSaveAs() {
 	    // TODO: Implement
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
 	 */
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
 		setInput(input);
 		this.input = (IFileEditorInput)input;
 		setPartName(input.getName());
 		
 		site.setSelectionProvider(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#isDirty()
 	 */
 	public boolean isDirty() {
 		try {
 			PrologProxy prolog = Activator.getProlog();
 			return prolog.hasSolution(prolog.createCompound("cpi#isModified", getResource()));
 		} catch (PrologException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
 	 */
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createPartControl(Composite parent) {
 	    editorWidget = new VisualTermWidget(parent, SWT.NONE);	
 	    editorWidget.addDisposeListener(this);
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
 
 			public void propertyChange(PropertyChangeEvent event) {
 				if(event.getProperty().equals("normalFont")) {
 					normalFont.dispose();
 					normalFont = createFont("normalFont");
 				} else if(event.getProperty().equals("symbolFont")) {
 					symbolFont.dispose();
 					symbolFont = createFont("symbolFont");
 				} 
  
 			}});
 		normalFont = createFont("normalFont");
 		symbolFont = createFont("symbolFont");
 
 		update.registerEditor(this);
 	    
 	    try {
 			open();
 		} catch (PrologException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TermInstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionContextException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TermVisualizationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
         new ContentProposalAdapter(getTextEditor(), new CedalionTextContentAdapter(), new CedalionProposalProvider(), KeyStroke.getInstance(SWT.CTRL, ' '), new char[] {});
 		
 
 	}
 
 	private void open() throws PrologException, TermInstantiationException, ExecutionContextException, TermVisualizationException {
 		// Open the file
 		IFile res = input.getFile();
 		PrologProxy prolog = Activator.getProlog();
 		ExecutionContext exe = new ExecutionContext(prolog);
 		exe.runProcedure(prolog.createCompound("cpi#openFile", res.getLocation().toString(), getResource(), res.getParent().getFullPath().toString()));
		// Get the root type
		Variable varType = new Variable("RootType");
		Object rootType = prolog.getSolution(prolog.createCompound("cpi#rootType", varType)).get(varType);
 		// Set the root path
 		Compound path = prolog.createCompound("cpi#path", getResource(), prolog.createCompound("[]"));
 		Compound descriptor = prolog.createCompound("cpi#descriptor", path, new Variable(), prolog.createCompound("[]"));
		Compound tterm = prolog.createCompound("::", descriptor, rootType);
 		editorWidget.setTerm(prolog.createCompound("cpi#vis", tterm), this);
 	}
 
 	private Font createFont(final String fontType) {
 		String fontDesc = Activator.getDefault().getPreferenceStore().getString(fontType);
 		if(fontDesc.equals(""))
 			return editorWidget.getFont();
 		FontData fontData = PreferenceConverter.getFontData(Activator.getDefault().getPreferenceStore(), fontType);
 		Font normalFont = new Font(editorWidget.getDisplay(), fontData);
 		return normalFont;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
 	 */
 	public void setFocus() {
 		getSite().getPage().activate(this);
 		Activator.getDefault().registerViewOpener(this);
 		Activator.getDefault().registerCurrentContext(this);
 	}
 
     /* (non-Javadoc)
      * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
      */
     public void addSelectionChangedListener(ISelectionChangedListener listener) {
         listeners.add(listener);
         
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
      */
     public ISelection getSelection() {
         return selection;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
      */
     public void removeSelectionChangedListener(ISelectionChangedListener listener) {
         listeners.remove(listener);
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
      */
     public void setSelection(ISelection selection) {
         this.selection = selection;
         for(Iterator<ISelectionChangedListener> i = listeners.iterator(); i.hasNext(); ) {
             i.next().selectionChanged(new SelectionChangedEvent(this, selection));
         }
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkbenchPart#dispose()
      */
     public void dispose() {
         System.out.println("Disposing editor for " + input.getFile() + " resource: " + getResource());
         try {
 			close();
 		} catch (PrologException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TermInstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionContextException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
 	private void close() throws PrologException, TermInstantiationException, ExecutionContextException {
 		IFile res = input.getFile();
 		PrologProxy prolog = Activator.getProlog();
 		ExecutionContext exe = new ExecutionContext(prolog);
 		exe.runProcedure(prolog.createCompound("cpi#closeFile", getResource()));
 	}
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#getTextEditor()
      */
     public Text getTextEditor() {
         return editorWidget.getText();
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#bindFigure(net.nansore.visualterm.figures.TermFigure)
      */
     public void bindFigure(TermFigure figure) {
         // Do nothing
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#focusChanged(net.nansore.visualterm.figures.TermFigure)
      */
     public void selectionChanged(TermFigure figure) {
         setSelection(new StructuredSelection(figure));
         if(figure instanceof VisualTerm)
         	currentTermFigure = (VisualTerm)figure;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
      */
     public void widgetDisposed(DisposeEvent e) {
         update.unregisterEditor(this);
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#registerTermFigure(long, net.nansore.visualterm.figures.TermFigure)
      */
     public synchronized void registerTermFigure(Object termID, TermFigure figure) {
         Object key = termID;
         List<TermFigure> figures = termFigures.get(key);
         if(figures == null) {
             figures = new ArrayList<TermFigure>();
             termFigures.put(key, figures);
         }
         figures.add(figure);        
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#getColor()
      */
     public Color getColor() {
         return new Color(editorWidget.getDisplay(), 0, 0, 0);
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#getFont()
      */
     public Font getFont(int fontType) {
     	if(fontType == TermContext.NORMAL_FONT)
     		return normalFont;
     	else
     		return symbolFont;
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#registerDispose(net.nansore.visualterm.Disposable)
      */
     public void registerDispose(TermFigure disp) {
         // TODO Auto-generated method stub
         
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#figureUpdated()
      */
     public synchronized void figureUpdated() {
         firePropertyChange(PROP_DIRTY);
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#unregisterTermFigure(int, net.nansore.visualterm.figures.TermFigure)
      */
     public synchronized void unregisterTermFigure(Object termID, TermFigure figure) {
         List<TermFigure> figures = (List<TermFigure>) termFigures.get(termID);
         if(figures != null) {
             figures.remove(figure);
         }
     }
 
 	public String getResource() {
 		return input.getFile().getFullPath().toString();
 	}
 
     public void handleClick(MouseEvent me) {
     }
 
     public Control getCanvas() {
         return editorWidget.getCanvas();
     }
 
 	public IWorkbenchPart getWorkbenchPart() {
 		return this;
 	}
 
     public void performDefaultAction() {
     }
 
     public void refresh() throws TermVisualizationException, TermInstantiationException, PrologException {
         editorWidget.refresh();
         Notifier.instance().printRefCount();
         System.gc();
     }
 
 	public String getPackage() {
 		return input.getFile().getParent().getFullPath().toString();
 	}
 
 	@Override
 	public CedalionView openView() throws PartInitException {
 		return (CedalionView)getSite().getPage().showView("net.nansore.cedalion.CedalionView");
 	}
 }
