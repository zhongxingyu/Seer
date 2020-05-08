 package org.spoofax.dummyeditor.editors;
 
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.terms.TermFactory;
 import org.spoofax.views.properties.StrategoTermSelection;
import org.spoofax.views.properties.SelectionProvider;
 
 public class XMLEditor extends TextEditor {
 
 	private ColorManager colorManager;
 
 	public XMLEditor() {
 		super();
 		colorManager = new ColorManager();
 		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
 		setDocumentProvider(new XMLDocumentProvider());
 	}
 	public void dispose() {
 		colorManager.dispose();
 		super.dispose();
 	}
 	
 	/*
 	 * @see IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
 	 */
 	@Override
 	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
 		super.init(site, input);
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		ISelectionProvider textSelectionProvider = getSite().getSelectionProvider();
 		textSelectionProvider.addSelectionChangedListener(new TextSelectionChangedListener());
		getSite().setSelectionProvider(new org.spoofax.views.properties.SelectionProvider());
 	}
 	
 	private class TextSelectionChangedListener implements ISelectionChangedListener {
 
 		@Override
 		public void selectionChanged(SelectionChangedEvent event) {
 			assert event.getSelection() instanceof ITextSelection;
 			ITextSelection textSelection = (ITextSelection) event.getSelection();
 			
 			TermFactory f = new TermFactory();
 			IStrategoTerm p1 = f.makeTuple(f.makeString("p1"), f.makeString("v1"));
 			IStrategoTerm p2 = f.makeTuple(f.makeString("p2"), f.makeString("v2"));
 			
 			IStrategoTerm p4 = f.makeTuple(f.makeString("p4"), f.makeString("v4"));
 			IStrategoTerm p5 = f.makeTuple(f.makeString("p5"), f.makeString("v5"));
 			IStrategoTerm p3 = f.makeTuple(f.makeString("p3"), f.makeList(p4, p5));
 			
 			IStrategoTerm properties = f.makeList(p1, p2, p3);
 			
			ISelection selection = new StrategoTermSelection(properties, textSelection);
 			getSite().getSelectionProvider().setSelection(selection);
 		}
 	}
 }
