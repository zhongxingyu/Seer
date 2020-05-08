 package org.spoofax.modelware.gmf;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.commands.operations.OperationHistoryFactory;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
 import org.eclipse.imp.editor.UniversalEditor;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.text.undo.DocumentUndoManagerRegistry;
 import org.eclipse.text.undo.IDocumentUndoManager;
 import org.eclipse.ui.IEditorPart;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.modelware.emf.Language;
 import org.spoofax.modelware.emf.compare.CompareUtil;
 import org.spoofax.modelware.emf.trans.Tree2modelConverter;
 import org.spoofax.modelware.emf.utils.Utils;
 import org.spoofax.modelware.gmf.editorservices.DiagramSelectionChangedListener;
 import org.spoofax.modelware.gmf.editorservices.TextSelectionChangedListener;
 import org.spoofax.modelware.gmf.editorservices.UndoRedo;
 import org.spoofax.modelware.gmf.editorservices.UndoRedoEventGenerator;
 import org.spoofax.modelware.gmf.resource.SpoofaxGMFResource;
 import org.strategoxt.imp.runtime.EditorState;
 
 /**
  * An {@link EditorPair} holds a textual and a graphical editor and takes care of the synchronization between them. It also registers a set of
  * listeners for the purpose of integrating editor services such as selection sharing and undo-redo functionality.
  * 
  * @author Oskar van Rest
  */
 public class EditorPair {
 
 	Collection<EditorPairObserver> observers;
 
 	private UniversalEditor textEditor;
 	private DiagramEditor diagramEditor;
 	private Language language;
 
 	public EObject semanticModel;
 	private ModelChangeListener semanticModelContentAdapter;
 	private TextChangeListener textChangeListener;
 	private DiagramSelectionChangedListener GMFSelectionChangedListener;
 	private TextSelectionChangedListener spoofaxSelectionChangedListener;
 	public IStrategoTerm ASTgraph;
 
 	public EditorPair(UniversalEditor textEditor, DiagramEditor diagramEditor, Language language) {
 		this.observers = new ArrayList<EditorPairObserver>();
 
 		this.textEditor = textEditor;
 		this.diagramEditor = diagramEditor;
 		this.language = language;
 
 		checkEnabledStates();
 		
 		loadSemanticModel();
 		addSelectionChangeListeners();
 		textChangeListener = new TextChangeListener(this);
 
 		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedoEventGenerator(this));
 		// note: order of execution of the statements above and the one below matters
 		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(new UndoRedo(this));
 
 		SpoofaxGMFResource resource = (SpoofaxGMFResource) diagramEditor.getEditingDomain().getResourceSet().getResources().get(1);
 		textEditor.addOnSaveListener(resource.new SaveSynchronization(this));
 	}
 
 	private void addSelectionChangeListeners() {
 		diagramEditor.getEditorSite().getSelectionProvider().addSelectionChangedListener(GMFSelectionChangedListener = new DiagramSelectionChangedListener(this));
 		textEditor.getSite().getSelectionProvider().addSelectionChangedListener(spoofaxSelectionChangedListener = new TextSelectionChangedListener(this));
 	}
 
 	public void dispose() {
 		// TODO TextToModelOnSave
 		textChangeListener.dispose();
 		EditorPairUtil.getSemanticModel(diagramEditor).eAdapters().remove(semanticModelContentAdapter);
 		diagramEditor.getEditorSite().getSelectionProvider().removeSelectionChangedListener(GMFSelectionChangedListener);
 		textEditor.getSite().getSelectionProvider().removeSelectionChangedListener(spoofaxSelectionChangedListener);
 	}
 
 	// TODO refactor
 	public void loadSemanticModel() {
 		if (semanticModel != null && semanticModel.eAdapters().contains(semanticModelContentAdapter))
 			semanticModel.eAdapters().remove(semanticModelContentAdapter);
 
 		semanticModel = EditorPairUtil.getSemanticModel(diagramEditor);
 		if (semanticModel != null)
 			semanticModel.eAdapters().add(semanticModelContentAdapter = new ModelChangeListener(this));
 	}
 
 	public UniversalEditor getTextEditor() {
 		return textEditor;
 	}
 
 	public DiagramEditor getDiagramEditor() {
 		return diagramEditor;
 	}
 
 	public IEditorPart getPartner(IEditorPart editor) {
 		if (editor == textEditor)
 			return diagramEditor;
 		else if (editor == diagramEditor)
 			return textEditor;
 		else
 			return null;
 	}
 
 	public Language getLanguage() {
 		return language;
 	}
 
 	public void registerObserver(EditorPairObserver observer) {
 		observers.add(observer);
 	}
 
 	public void unregisterObserver(EditorPairObserver observer) {
 		observers.remove(observer);
 	}
 
 	public void notifyObservers(EditorPairEvent event) {
		for (EditorPairObserver observer : observers) {
			observer.notify(event);
 		}
 	}
 
 	public IUndoContext getTextUndoContext() {
 		IDocumentUndoManager documentUndoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()));
 		return documentUndoManager.getUndoContext();
 	}
 
 	public IUndoContext getDiagramUndoContext() {
 		return diagramEditor.getDiagramEditDomain().getDiagramCommandStack().getUndoContext();
 	}
 
 	public void doTerm2Model() {
 		notifyObservers(EditorPairEvent.PreTerm2Model);
 		EObject left = new Tree2modelConverter(EPackageRegistryImpl.INSTANCE.getEPackage(getLanguage().getNsURI())).convert(ASTgraph);
 		notifyObservers(EditorPairEvent.PostTerm2Model);
 
 		EObject right = EditorPairUtil.getSemanticModel(diagramEditor);
 
 		if (right == null)
 			return;
 
 		notifyObservers(EditorPairEvent.PreCompare);
 		Comparison comparison = CompareUtil.compare(left, right);
 		notifyObservers(EditorPairEvent.PostCompare);
 
 		notifyObservers(EditorPairEvent.PreMerge);
 		CompareUtil.merge(comparison, right);
 		notifyObservers(EditorPairEvent.PostMerge);
 
 		notifyObservers(EditorPairEvent.PreRender);
 		// Workaround for http://www.eclipse.org/forums/index.php/m/885469/#msg_885469
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				diagramEditor.getDiagramEditPart().addNotify();
 				notifyObservers(EditorPairEvent.PostRender);
 			}
 		});
 	}
 	
 	private void checkEnabledStates() {
 		EditorState editorState = EditorState.getEditorFor(textEditor);
 		textToDiagramSynchronizationEnabled = Utils.isTextToDiagramSynchronizationEnabled(editorState);
 		diagramToTextSynchronizationEnabled = Utils.isDiagramToTextSynchronizationEnabled(editorState);
 		textToDiagramSelectionEnabled = Utils.isTextToDiagramSelectionEnabled(editorState);
 		diagramToTextSelectionEnabled = Utils.isDiagramToTextSelectionEnabled(editorState);
 	}
 	
 	private boolean textToDiagramSynchronizationEnabled = true;
 	private boolean diagramToTextSynchronizationEnabled = true;
 	private boolean textToDiagramSelectionEnabled = true;
 	private boolean diagramToTextSelectionEnabled = true;
 
 	public void setTextToDiagramSynchronizationEnabled(Boolean enable) {
 		textToDiagramSynchronizationEnabled = enable;
 	}
 	
 	public void setDiagramToTextSynchronizationEnabled(Boolean enable) {
 		diagramToTextSynchronizationEnabled = enable;
 	}
 	
 	public boolean isTextToDiagramSynchronizationEnabled() {
 		return textToDiagramSynchronizationEnabled;
 	}
 	
 	public boolean isDiagramToTextSynchronizationEnabled() {
 		return diagramToTextSynchronizationEnabled;
 	}
 	
 	public void setTextToDiagramSelectionEnabled(Boolean enable) {
 		textToDiagramSelectionEnabled = enable;
 	}
 	
 	public void setDiagramToTextSelectionEnabled(Boolean enable) {
 		diagramToTextSelectionEnabled = enable;
 	}
 	
 	public boolean isTextToDiagramSelectionEnabled() {
 		return textToDiagramSelectionEnabled;
 	}
 	
 	public boolean isDiagramToTextSelectionEnabled() {
 		return diagramToTextSelectionEnabled;
 	}
 }
