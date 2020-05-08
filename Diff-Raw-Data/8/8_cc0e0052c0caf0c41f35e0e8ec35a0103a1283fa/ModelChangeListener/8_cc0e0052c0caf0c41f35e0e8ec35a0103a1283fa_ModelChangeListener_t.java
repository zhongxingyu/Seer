 package org.spoofax.modelware.gmf;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Vector;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.swt.widgets.Display;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.modelware.emf.tree2model.Model2Term;
 import org.spoofax.modelware.emf.utils.SpoofaxEMFConstants;
 import org.spoofax.modelware.emf.utils.SpoofaxEMFUtils;
 import org.spoofax.modelware.emf.utils.Subobject2Subterm;
 import org.spoofax.terms.AbstractTermFactory;
 import org.spoofax.terms.attachments.OriginAttachment;
 import org.strategoxt.imp.runtime.EditorState;
 import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
 import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
 import org.strategoxt.lang.Context;
 
 /**
  * Listens for changes in GMF's semantic model and perform a model-to-text
  * transformation upon such a change.
  * 
  * @author Oskar van Rest
  */
 public class ModelChangeListener extends EContentAdapter {
 
 	private EditorPair editorPair;
 	private boolean debounce;
 	private Vector<Notification> changes = new Vector<Notification>();
 	private Thread thread;
 	private String text;
 	private long timeOfLastModelChange;
 	private static final long TIMEOUT = 50;
 
 	public ModelChangeListener(EditorPair editorPair) {
 		this.editorPair = editorPair;
 		editorPair.registerObserver(new Debouncer());
 	}
 
 	public void notifyChanged(final Notification n) {
 		super.notifyChanged(n);
 		
 		if (debounce || n.getEventType() == Notification.REMOVING_ADAPTER) {
 			return;
 		}
 
 		changes.add(n);
 		
 		timeOfLastModelChange = System.currentTimeMillis();
 		if (thread == null || !thread.isAlive()) {		
 			thread = new Thread(new Timer());
 			thread.start();
 		}
 	}
 	private class Timer implements Runnable {
 		public void run() {
 			try {
 				long different = -1;
 				while (different < TIMEOUT) {
 					different = System.currentTimeMillis() - timeOfLastModelChange;
 					Thread.sleep(Math.max(0, TIMEOUT - different));
 				}
 				updateText();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public IStrategoTerm model2ASTtext(EObject model) {
 		EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());
 		IStrategoTerm ASTgraph = new Model2Term(SpoofaxEMFUtils.termFactory).convert(model);
 		return SpoofaxEMFUtils.getASTtext(ASTgraph, editorState);
 	}
 	
 	public String ASTtext2text(IStrategoTerm ASTtext) {	
 		EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());
 		IStrategoTerm oldAST = editorState.getParseController().parse(text, new NullProgressMonitor());
 		IStrategoTerm oldASTtext = oldAST; // oldASTtext should actually be the analyzed version of oldAST. However, analyzes will result in a term without parent attachments. The layout preservation algorithm needs parent attachments.
 		return SpoofaxEMFUtils.calculateTextReplacement(oldASTtext, ASTtext, editorState);
 	}
 	
 	private synchronized void updateText() {
 		final EditorState editorState = EditorState.getEditorFor(editorPair.getTextEditor());
 		AbstractTermFactory f = SpoofaxEMFUtils.termFactory;
 		IStrategoTerm ASTtext = null;
 		try {
 			ASTtext = editorState.getCurrentAnalyzedAst();
 			text = editorState.getDocument().get();	
 		} catch (BadDescriptorException e) {
 			e.printStackTrace();
 		}
 		
 		Iterator<Notification> it = changes.iterator();
 		while (it.hasNext()) {
 			Notification n = it.next();
 			it.remove();
 			IStrategoTerm newASTtext = null;
 			EObject model = EditorPairUtil.getSemanticModel(editorPair.getDiagramEditor());
 		
 			if (SpoofaxEMFUtils.strategyExists(editorState, SpoofaxEMFConstants.STRATEGY_ASTgraph_TO_ASTtext)) {
 				newASTtext = model2ASTtext(model);
 			}
 			else { 
 				IStrategoTerm oldASTtextNode = getEObjectOrigin((EObject) n.getNotifier(), model, editorPair.ASTgraph);
 				IStrategoTerm oldASTtextNodeParent = getEObjectOrigin(((EObject) n.getNotifier()).eContainer(), model, editorPair.ASTgraph);
 				IStrategoTerm featureName = f.makeString(((EStructuralFeature) n.getFeature()).getName());
 				
 				if (n.getEventType() == Notification.ADD) {
 					IStrategoTerm newValue = n.getNewValue() instanceof EObject ? new Model2Term(f).convert((EObject) n.getNewValue()) : toString(n.getNewValue());
 					newASTtext = SpoofaxEMFUtils.invokeStrategy(editorState, "ADD", ASTtext, oldASTtextNode, featureName, newValue);
 				}
 				else if (n.getEventType() == Notification.SET) {
 					IStrategoTerm oldValue = n.getOldValue() instanceof EObject ? getEObjectOrigin((EObject) n.getOldValue(), model, editorPair.ASTgraph) : toString(n.getOldValue());
 					IStrategoTerm newValue = n.getNewValue() instanceof EObject ? getEObjectOrigin((EObject) n.getNewValue(), model, editorPair.ASTgraph) : toString(n.getNewValue());
 					newASTtext = SpoofaxEMFUtils.invokeStrategy(editorState, "SET", ASTtext, oldASTtextNodeParent, oldASTtextNode, featureName, oldValue, newValue);
 				}
 				else if (n.getEventType() == Notification.REMOVE) {
 					IStrategoTerm oldValue = getEObjectOrigin((EObject) n.getNotifier(), model, editorPair.ASTgraph, SpoofaxEMFUtils.feature2index(((EObject) n.getNotifier()).eClass(), (EStructuralFeature) n.getFeature()), n.getPosition());
 					newASTtext = SpoofaxEMFUtils.invokeStrategy(editorState, "REMOVE", ASTtext, oldASTtextNode, featureName, oldValue);
 				}
 			}
			if (newASTtext != null) {
				ASTtext = newASTtext;
				text = ASTtext2text(ASTtext);
			}
 		}
 		
 		if (!editorState.getDocument().get().equals(text)) {
 			Display.getDefault().syncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					try {
 						IStrategoTerm oldASTtext = editorState.getCurrentAnalyzedAst();
 						editorState.getDocument().set(text);
 						while (oldASTtext == editorState.getCurrentAnalyzedAst()) {
 							Thread.sleep(30);
 						}
 					} catch (BadDescriptorException e) {
 						e.printStackTrace();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}					
 				}
 			});
 		}
 	}
 	
 	private IStrategoTerm toString(Object object) {
 		if (object == null) {
 			return SpoofaxEMFUtils.createNone();
 		}
 		else {
 			return SpoofaxEMFUtils.termFactory.makeString(object.toString());
 		}
 	}
 
 	private IStrategoTerm getEObjectOrigin(EObject eObject, EObject model, IStrategoTerm AST, int featureIndex, int position) {
 		List<Integer> path = Subobject2Subterm.object2path(eObject, model, new LinkedList<Integer>());
 		if (path != null) {
 			path.add(featureIndex); path.add(position);
 			IStrategoList strategoTermPath = StrategoTermPath.toStrategoPath(path);
 			IStrategoTerm term = StrategoTermPath.getTermAtPath(new Context(), AST, strategoTermPath);
 			return getEObjectOriginHelper(eObject, term);
 		}
 		return SpoofaxEMFUtils.createNone();
 	}
 	
 	private IStrategoTerm getEObjectOrigin(EObject eObject, EObject model, IStrategoTerm ast) {
 		if (eObject == null) {
 			return SpoofaxEMFUtils.createNone();
 		}
 		
 		IStrategoTerm term = Subobject2Subterm.object2subterm(eObject, model, ast);
 		return getEObjectOriginHelper(eObject, term);
 	}
 	
 	private IStrategoTerm getEObjectOriginHelper(EObject eObject, IStrategoTerm term) {
 		if (term != null) {
 			IStrategoTerm origin = OriginAttachment.getOrigin(term);
 			if (origin != null) {
 				return origin;
 			}
 		}
 		return new Model2Term(SpoofaxEMFUtils.termFactory).convert(eObject);
 	}
 
 	private class Debouncer implements EditorPairObserver {
 
 		@Override
 		public void notify(EditorPairEvent event) {
 			
 			if (event == EditorPairEvent.PreMerge) {
 				debounce = true;
 			}
 			else if (event == EditorPairEvent.PostMerge) {
 				debounce = false;
 			}
 
 			else if (event == EditorPairEvent.PreUndo || event == EditorPairEvent.PreRedo) {
 				debounce = true;
 			}
 			else if (event == EditorPairEvent.PostUndo || event == EditorPairEvent.PostRedo) {
 				debounce = false;
 			}
 		}
 	}
 }
