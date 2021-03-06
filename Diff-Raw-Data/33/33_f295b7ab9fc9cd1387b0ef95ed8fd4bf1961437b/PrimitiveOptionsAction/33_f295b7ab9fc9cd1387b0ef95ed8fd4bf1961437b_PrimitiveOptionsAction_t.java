 package org.eclipse.jdt.internal.debug.ui.actions;
 
 /**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
 
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.debug.ui.IDebugView;
 import org.eclipse.jdt.debug.core.JDIDebugModel;
 import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
 import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.ui.IActionDelegate2;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 
 /**
  * Allows setting of primitive display options for java variables
  */
 public class PrimitiveOptionsAction implements IViewActionDelegate, IActionDelegate2 {
 	
 	private IViewPart fView;
 	private IAction fAction;
 
 	public PrimitiveOptionsAction() {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
 	 */
 	public void init(IViewPart view) {
 		fView = view;
 		applyPreferences();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
 	 */
 	public void init(IAction action) {
 		fAction = action;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate2#dispose()
 	 */
 	public void dispose() {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
 	 */
 	public void runWithEvent(IAction action, Event event) {
 		run(action);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
 	 */
 	public void run(IAction action) {
 		// open dialog
 		final StructuredViewer viewer = getStructuredViewer();
 		PrimitiveOptionsDialog dialog = new PrimitiveOptionsDialog(viewer.getControl().getShell(), getView().getSite().getId());
 		int res = dialog.open();
 		if (res == Dialog.OK) {
 			BusyIndicator.showWhile(viewer.getControl().getDisplay(), new Runnable() {
 				public void run() {
 					applyPreferences();
 					viewer.refresh();
 					JDIDebugUIPlugin.getDefault().savePluginPreferences();						
 				}
 			});			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 	}
 
 	protected IPreferenceStore getPreferenceStore() {
 		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
 	}
 	
 	/**
 	 * Returns the value of this filters preference (on/off) for the given
 	 * view.
 	 * 
 	 * @param part
 	 * @return boolean
 	 */
 	public static boolean getPreferenceValue(String id, String preference) {
		String compositeKey = id + "." + preference; //$NON-NLS-1$
 		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
 		boolean value = false;
 		if (store.contains(compositeKey)) {
 			value = store.getBoolean(compositeKey);
 		} else {
 			value = store.getBoolean(preference);
 		}
 		return value;		
 	}
 	
 	protected IViewPart getView() {
 		return fView;
 	}
 	
 	protected StructuredViewer getStructuredViewer() {
 		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
 		if (view != null) {
 			Viewer viewer = view.getViewer();
 			if (viewer instanceof StructuredViewer) {
 				return (StructuredViewer)viewer;
 			}
 		}		
 		return null;
 	}
 	
 	protected void applyPreferences() {
 		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
 		if (view != null) {
 			IDebugModelPresentation presentation = view.getPresentation(JDIDebugModel.getPluginIdentifier());
 			if (presentation != null) {
 				applyPreference(IJDIPreferencesConstants.PREF_SHOW_HEX, JDIModelPresentation.SHOW_HEX_VALUES, presentation);
 				applyPreference(IJDIPreferencesConstants.PREF_SHOW_CHAR, JDIModelPresentation.SHOW_CHAR_VALUES, presentation);
 				applyPreference(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED, JDIModelPresentation.SHOW_UNSIGNED_VALUES, presentation);
 			}
 		}
 	}
 	
 	/**
 	 * Sets the display attribute associated with the given preference.
 	 * 
 	 * @param preference preference key
 	 * @param attribute attribute key
 	 * @param presentation the model presentation to update
 	 */
 	protected void applyPreference(String preference, String attribute, IDebugModelPresentation presentation) {
 		boolean on = getPreferenceValue(getView().getSite().getId(), preference);
 		presentation.setAttribute(attribute, (on ? Boolean.TRUE : Boolean.FALSE));
 	}
 	
 }
