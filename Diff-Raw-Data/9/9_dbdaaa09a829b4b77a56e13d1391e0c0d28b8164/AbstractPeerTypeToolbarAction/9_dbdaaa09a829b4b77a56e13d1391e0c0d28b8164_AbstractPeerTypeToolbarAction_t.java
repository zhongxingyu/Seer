 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.actions;
 
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MenuAdapter;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
 import org.eclipse.tcf.te.runtime.persistence.history.HistoryManager;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.ISelectionService;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.tcf.ui.dialogs.AgentSelectionDialog;
 import org.eclipse.tcf.te.ui.views.editor.EditorInput;
 import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
 import org.eclipse.tcf.te.ui.views.navigator.ViewerSorter;
 import org.eclipse.tcf.te.ui.wizards.newWizard.NewWizardRegistry;
 import org.eclipse.ui.IActionDelegate2;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.wizards.IWizardDescriptor;
 
 /**
  * Abstract Target connections toolbar pull down menu action implementation.
  */
 public abstract class AbstractPeerTypeToolbarAction extends Action implements IActionDelegate2, IWorkbenchWindowPulldownDelegate2, IEventListener {
 
 	IWorkbenchWindow window;
 	IAction action;
 
 	private Menu menuCtlParent;
 	private Menu menuMenuParent;
 
 	/* default */ final AtomicBoolean recreateMenuCtlParent = new AtomicBoolean();
 	/* default */ final AtomicBoolean recreateMenuMenuParent = new AtomicBoolean();
 
 	/**
 	 * Constructor.
 	 */
 	public AbstractPeerTypeToolbarAction() {
 		super();
 	}
 
 	protected abstract String getNewWizardId();
 
 	protected abstract String getPeerTypeId();
 
 	protected abstract String getNewActionLabel();
 	protected abstract ImageDescriptor getNewActionImageDescriptor();
 
 	protected abstract String getSelectExistingActionLabel();
 	protected abstract ImageDescriptor getSelectExistingActionImageDescriptor();
 	protected abstract String getSelectExistingDialogTitle();
 	protected abstract String getSelectExistingDialogDescription();
 	protected abstract String getSelectExistingDialogReachableOnlyLabel();
 	protected abstract ViewerFilter getSelectExistingDialogViewerFilter();
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
 	 */
 	@Override
 	public void init(IWorkbenchWindow window) {
 		this.window = window;
 		EventManager.getInstance().addEventListener(this, ChangeEvent.class);
 		if (action != null) {
 			EventManager.getInstance().fireEvent(new ChangeEvent(HistoryManager.getInstance(), ChangeEvent.ID_CHANGED, getPeerTypeId(), getPeerTypeId()));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
 	 */
 	@Override
 	public void init(IAction action) {
 		this.action = action;
 		if (window != null) {
 			EventManager.getInstance().fireEvent(new ChangeEvent(HistoryManager.getInstance(), ChangeEvent.ID_CHANGED, getPeerTypeId(), getPeerTypeId()));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate2#dispose()
 	 */
 	@Override
 	public void dispose() {
 		window = null;
 		EventManager.getInstance().removeEventListener(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
 	 */
 	@Override
 	public void eventFired(EventObject event) {
 		if (event instanceof ChangeEvent) {
 			ChangeEvent e = (ChangeEvent)event;
 			if (e.getSource() instanceof HistoryManager && getPeerTypeId().equals(e.getNewValue())) {
 				recreateMenuCtlParent.set(true);
 				recreateMenuMenuParent.set(true);
 				IPeerModel peerModel = getPeerModel(HistoryManager.getInstance().getFirst(getPeerTypeId()));
 				if (action != null) {
 					if (peerModel != null) {
 						ILabelProvider labelProvider = getLabelProvider(peerModel);
 						if (labelProvider != null) {
 							action.setToolTipText("Open " + labelProvider.getText(peerModel)); //$NON-NLS-1$
 							return;
 						}
 
 					}
 					action.setToolTipText(null);
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
 	 */
 	@Override
 	public Menu getMenu(Control parent) {
 		if (menuCtlParent != null) {
 			menuCtlParent.dispose();
 		}
 		menuCtlParent = new Menu(parent);
 		fillMenu(menuCtlParent);
 		initMenu(menuCtlParent, recreateMenuCtlParent);
 		return menuCtlParent;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate2#getMenu(org.eclipse.swt.widgets.Menu)
 	 */
 	@Override
 	public Menu getMenu(Menu parent) {
 		if (menuMenuParent != null) {
 			menuMenuParent.dispose();
 		}
 		menuMenuParent = new Menu(parent);
 		fillMenu(menuMenuParent);
 		initMenu(menuMenuParent, recreateMenuMenuParent);
 		return menuMenuParent;
 	}
 
 	/**
 	 * Creates the menu for the action
 	 */
 	private void initMenu(final Menu menu, final AtomicBoolean recreateMenu) {
 		// Add listener to re-populate the menu each time
 		// it is shown because of dynamic history list
 		menu.addMenuListener(new MenuAdapter() {
 			@Override
 			public void menuShown(MenuEvent e) {
 				if (recreateMenu.get()) {
 					Menu m = (Menu)e.widget;
 					MenuItem[] items = m.getItems();
 					for (MenuItem item : items) {
 						item.dispose();
 					}
 					fillMenu(m);
 					recreateMenu.set(false);
 				}
 			}
 		});
 	}
 
 	private IPeerModel getPeerModel(final String peerId) {
 		if (peerId != null) {
 			final AtomicReference<IPeerModel> peerModel = new AtomicReference<IPeerModel>();
 
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					ILocatorModel model = Model.getModel();
 					Assert.isNotNull(model);
 					peerModel.set(model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peerId));
 				}
 			};
 
 			if (Protocol.isDispatchThread()) {
 				runnable.run();
 			}
 			else {
 				Protocol.invokeAndWait(runnable);
 			}
 
 			return peerModel.get();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Fills the drop-down menu with favorites and launch history
 	 *
 	 * @param menu the menu to fill
 	 */
 	@SuppressWarnings("unused")
 	protected void fillMenu(Menu menu) {
 		int accelerator = 1;
 		List<IPeerModel> peerModels = new ArrayList<IPeerModel>();
 		for (String id : HistoryManager.getInstance().getHistory(getPeerTypeId())) {
 			IPeerModel peerModel = getPeerModel(id);
 			if (peerModel != null && !peerModels.contains(peerModel)) {
 				peerModels.add(peerModel);
 			}
 		}
 		for (final IPeerModel peerModel: peerModels) {
 			ILabelProvider labelProvider = getLabelProvider(peerModel);
 			String label = peerModel.getPeer().getName();
 			if (labelProvider != null) {
 				label = labelProvider.getText(peerModel);
 				if (labelProvider instanceof ILabelDecorator) {
 					label = ((ILabelDecorator)labelProvider).decorateText(label, peerModel);
 				}
 			}
 			Action action = new Action(label) {
 				@Override
 				public void run() {
 					// Get the active page
 					IWorkbenchPage page = window.getActivePage();
 					// Create the editor input object
 					IEditorInput input = new EditorInput(peerModel);
 					try {
 						// Opens the configuration editor
 						page.openEditor(input, IUIConstants.ID_EDITOR);
 					}
 					catch (PartInitException e) {
 					}
 					HistoryManager.getInstance().add(getPeerTypeId(), peerModel.getPeerId());
 					ISelectionService selService = ServiceManager.getInstance().getService(ISelectionService.class);
 					if (selService != null) {
 						selService.setDefaultSelection(peerModel);
 					}
 				}
 			};
 			if (labelProvider != null) {
 				action.setImageDescriptor(ImageDescriptor.createFromImage(labelProvider.getImage(peerModel)));
 			}
 			addToMenu(menu, action, accelerator);
 			accelerator++;
 		}
 
 		// Separator between favorites and history
 		if (accelerator > 1) {
 			new MenuItem(menu, SWT.SEPARATOR);
 		}
 
 		action = new Action(getSelectExistingActionLabel()) {
 			@Override
 			public void run() {
 				// Get the active page
 				IWorkbenchPage page = window.getActivePage();
 				// Create the agent selection dialog
 				AgentSelectionDialog dialog = new AgentSelectionDialog(null) {
 					@Override
 					protected String getTitle() {
 					    return getSelectExistingDialogTitle();
 					}
 					@Override
 					protected String getDefaultMessage() {
 					    return getSelectExistingDialogDescription();
 					}
 					@Override
 					protected String getShowOnlyReachableLabel() {
 					    return getSelectExistingDialogReachableOnlyLabel();
 					}
 					@Override
 					protected boolean supportsMultiSelection() {
 						return false;
 					}
 					@Override
 					protected void configureTableViewer(TableViewer viewer) {
 						super.configureTableViewer(viewer);
 						viewer.addFilter(getSelectExistingDialogViewerFilter());
 						viewer.setSorter(new ViewerSorter());
 					}
 				};
 
 				// Open the dialog
 				if (dialog.open() == Window.OK) {
 					// Get the selected proxy from the dialog
 					ISelection selection = dialog.getSelection();
 					if (selection instanceof IStructuredSelection && !selection.isEmpty() && ((IStructuredSelection)selection).getFirstElement() instanceof IPeerModel) {
 						// Create the editor input object
 						IEditorInput input = new EditorInput(((IStructuredSelection)selection).getFirstElement());
 						try {
 							// Opens the configuration editor
 							page.openEditor(input, IUIConstants.ID_EDITOR);
 						}
 						catch (PartInitException e) {
 						}
						if (((IStructuredSelection)selection).getFirstElement() instanceof IPeerModel) {
							IPeerModel peerModel = (IPeerModel)((IStructuredSelection)selection).getFirstElement();
							HistoryManager.getInstance().add(getPeerTypeId(), peerModel.getPeerId());
							ISelectionService selService = ServiceManager.getInstance().getService(ISelectionService.class);
							if (selService != null) {
								selService.setDefaultSelection(peerModel);
							}

						}
 					}
 				}
 			}
 		};
 		action.setImageDescriptor(getSelectExistingActionImageDescriptor());
 		addToMenu(menu, action, -1);
 
 		action = new Action(getNewActionLabel()) {
 			@SuppressWarnings("restriction")
             @Override
 			public void run() {
 				IWizardDescriptor wizardDesc = NewWizardRegistry.getInstance().findWizard(getNewWizardId());
 				new org.eclipse.ui.internal.actions.NewWizardShortcutAction(window, wizardDesc).run();
 			}
 		};
 		action.setImageDescriptor(getNewActionImageDescriptor());
 		addToMenu(menu, action, -1);
 	}
 
 	/**
 	 * Adds the given action to the specified menu with an accelerator specified
 	 * by the given number.
 	 *
 	 * @param menu the menu to add the action to
 	 * @param action the action to add
 	 * @param accelerator the number that should appear as an accelerator
 	 */
 	protected void addToMenu(Menu menu, IAction action, int accelerator) {
 		StringBuffer label= new StringBuffer();
 		if (accelerator >= 0 && accelerator < 10) {
 			//add the numerical accelerator
 			label.append('&');
 			label.append(accelerator);
 			label.append(' ');
 		}
 		label.append(action.getText());
 		action.setText(label.toString());
 		ActionContributionItem item= new ActionContributionItem(action);
 		item.fill(menu, -1);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
 	 */
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
 	 */
 	@SuppressWarnings("restriction")
     @Override
 	public void run(IAction action) {
 		IPeerModel peerModel = getPeerModel(HistoryManager.getInstance().getFirst(getPeerTypeId()));
 		if (peerModel != null) {
 			// Get the active page
 			IWorkbenchPage page = window.getActivePage();
 			// Create the editor input object
 			IEditorInput input = new EditorInput(peerModel);
 			try {
 				// Opens the configuration editor
 				page.openEditor(input, IUIConstants.ID_EDITOR);
 			}
 			catch (PartInitException e) {
 			}
 			return;
 		}
 		IWizardDescriptor wizardDesc = NewWizardRegistry.getInstance().findWizard(getNewWizardId());
 		new org.eclipse.ui.internal.actions.NewWizardShortcutAction(window, wizardDesc).run();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
 	 */
 	@Override
 	public void runWithEvent(IAction action, Event event) {
 		run(action);
 	}
 
 	/**
 	 * Get the label provider for a peer model node.
 	 *
 	 * @param peerModel The peer model node.
 	 * @return The label provider or <code>null</code>.
 	 */
 	protected ILabelProvider getLabelProvider(IPeerModel peerModel) {
 		ILabelProvider labelProvider = (ILabelProvider)peerModel.getAdapter(ILabelProvider.class);
 		if (labelProvider == null) {
 			labelProvider = (ILabelProvider)Platform.getAdapterManager().loadAdapter(peerModel, ILabelProvider.class.getName());
 		}
 		return labelProvider;
 	}
 }
