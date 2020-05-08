 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.views.scriptpad.actions;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.ContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuCreator;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.events.MenuAdapter;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IModelListener;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.listener.ModelAdapter;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.tcf.ui.views.scriptpad.ScriptPad;
 import org.eclipse.ui.IActionDelegate2;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
 import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
 
 /**
  * Peers selection sub menu action implementation.
  */
 public class PeersSubMenuAction extends Action implements IMenuCreator, IViewActionDelegate, IActionDelegate2, IWorkbenchAction, IWorkbenchWindowPulldownDelegate2 {
 	// Reference to the action proxy
 	/* default */ IAction actionProxy;
 	// Reference to the locator model listener
 	/* default */ IModelListener listener;
 
 	// Parent view part
 	/* default */ IViewPart view;
 
 	/**
      * Constructor.
      */
     public PeersSubMenuAction() {
     	super();
 
     	// Create and register the locator model listener
     	listener = new ModelAdapter() {
     		@Override
             public void locatorModelChanged(ILocatorModel model, IPeerModel peer, boolean added) {
     			// Re-evaluate the enablement
     			if (actionProxy != null) {
     				IPeerModel[] peers = Model.getModel().getPeers();
     				actionProxy.setEnabled(peers != null && peers.length > 0);
 
     				// If the peer is not set to the view yet, but the action get's
     				// enabled, than force the first peer in the list to be the selected one.
     				if (actionProxy.isEnabled() && view instanceof ScriptPad && ((ScriptPad)view).getPeerModel() == null) {
     					((ScriptPad)view).setPeerModel(peers[0]);
     				}
     			}
     		}
     	};
 
     	Protocol.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 		    	Model.getModel().addListener(listener);
 			}
 		});
     }
 
 	/* (non-Javadoc)
      * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
      */
     @Override
     public void init(IWorkbenchWindow window) {
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
      */
     @Override
     public void init(IViewPart view) {
     	this.view = view;
    	// If the action proxy is already set, it means that the init(IAction)
    	// has been called before. Re-trigger the action enablement.
    	if (actionProxy != null) {
    		listener.locatorModelChanged(Model.getModel(), null, false);
    	}
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
      */
     @Override
     public void init(IAction action) {
     	this.actionProxy = action;
     	if (action != null) {
         	action.setMenuCreator(this);
 
         	// Determine the enablement. The action is disabled
         	// if no peers are available.
         	IPeerModel[] peers = Model.getModel().getPeers();
         	if (peers != null && peers.length > 0) {
         		action.setEnabled(true);
 				if (view instanceof ScriptPad) ((ScriptPad)view).setPeerModel(peers[0]);
         	} else {
         		action.setEnabled(false);
 				if (view instanceof ScriptPad) ((ScriptPad)view).setPeerModel(null);
         	}
     	}
     }
 
 	/* (non-Javadoc)
      * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
      */
     @Override
     public void run(IAction action) {
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
      */
     @Override
     public void runWithEvent(IAction action, Event event) {
     }
 
 	/* (non-Javadoc)
      * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
      */
     @Override
     public void selectionChanged(IAction action, ISelection selection) {
     }
 
 	/* (non-Javadoc)
      * @see org.eclipse.jface.action.IMenuCreator#dispose()
      */
     @Override
     public void dispose() {
     	if (listener != null) {
         	Protocol.invokeLater(new Runnable() {
     			@Override
     			public void run() {
     				Model.getModel().removeListener(listener);
     				listener = null;
     			}
     		});
     	}
     }
 
 	/* (non-Javadoc)
      * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
      */
     @Override
     public Menu getMenu(Control parent) {
 		Menu menu = new Menu(parent);
 		return getSubMenu(menu);
     }
 
 	/* (non-Javadoc)
      * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
      */
     @Override
     public Menu getMenu(Menu parent) {
 		Menu menu = new Menu(parent);
 		return getSubMenu(menu);
    }
 
 	/*
 	 * Create the new sub menu.
 	 */
 	private Menu getSubMenu(Menu menu) {
 		menu.addMenuListener(new MenuAdapter() {
 			/* (non-Javadoc)
 			 * @see org.eclipse.swt.events.MenuAdapter#menuShown(org.eclipse.swt.events.MenuEvent)
 			 */
 			@Override
 			public void menuShown(MenuEvent e) {
 				ContributionItem item;
 				// dispose all "old" menu items before fill up the menu with new ones
 				Menu m = (Menu)e.widget;
 				MenuItem[] items = m.getItems();
 				for (MenuItem item2 : items) {
 					item2.dispose();
 				}
 
 				// Get the selected peer model
 				IPeerModel selected = null;
 				if (view instanceof ScriptPad) selected = ((ScriptPad)view).getPeerModel();
 
 				boolean selectFirst = selected == null;
 
 				IPeerModel[] peers = Model.getModel().getPeers();
 				if (peers != null && peers.length > 0) {
 					for (IPeerModel peer : peers) {
 						Action action = new PeerAction(view, peer);
 						if (selectFirst) {
 							action.setChecked(true);
 							selectFirst = false;
 							if (view instanceof ScriptPad) ((ScriptPad)view).setPeerModel(peer);
 						} else if (selected != null && selected.equals(peer)) {
 							action.setChecked(true);
 							if (view instanceof ScriptPad) ((ScriptPad)view).setPeerModel(peer);
 						}
 						item = new ActionContributionItem(action);
 						item.fill(m, -1);
 					}
 				}
 			}
 		});
 
 		return menu;
 	}
 }
