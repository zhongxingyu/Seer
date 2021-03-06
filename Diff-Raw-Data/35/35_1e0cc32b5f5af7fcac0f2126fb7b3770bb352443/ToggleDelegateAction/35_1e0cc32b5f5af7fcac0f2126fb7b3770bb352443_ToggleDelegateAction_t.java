 package org.eclipse.jdt.internal.debug.ui.actions;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
 
import org.eclipse.debug.ui.IDebugView;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 
 /**
  * A generic Toggle view action delegate, meant to be subclassed to provide
  * a specific filter.
  */
 public abstract class ToggleDelegateAction implements IViewActionDelegate {
 
 	/**
 	 * The viewer that this action works for
 	 */
 	private StructuredViewer fViewer;
 	
 	private IAction fAction;
 	private boolean fNeedsInitialization= true;
 
 	/**
 	 * @see IViewActionDelegate#init(IViewPart)
 	 */
 	public void init(IViewPart view) {
		IDebugView adapter= (IDebugView) view.getAdapter(IDebugView.class);
 		if (adapter != null && adapter.getViewer() instanceof StructuredViewer) {
 			setViewer((StructuredViewer)adapter.getViewer());
 		}
 	}
 
 	/**
 	 * Returns the appropriate tool tip text depending on
 	 * the state of the action.
 	 */
 	protected String getToolTipText(boolean on) {
 		return on ? getHideText() : getShowText();
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		valueChanged(action.isChecked());
 		String label= getToolTipText(action.isChecked());
 		action.setToolTipText(label);
 		action.setText(label);
 	}
 
 	protected abstract String getShowText();
 
 	protected abstract String getHideText();
 	
 	protected abstract void valueChanged(boolean on);
 	
 	protected abstract String getActionId();
 	
 	protected StructuredViewer getViewer() {
 		return fViewer;
 	}
 
 	protected void setViewer(StructuredViewer viewer) {
 		fViewer = viewer;
 	}
 	
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (fNeedsInitialization) {
 			setAction(action);
 			action.setId(getActionId());
 		}
 	}
 	
 	protected IAction getAction() {
 		return fAction;
 	}
 
 	protected void setAction(IAction action) {
 		fAction = action;
 	}
 }
