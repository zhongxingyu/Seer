 /**
  * Copyright (c) 2009 Anyware Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Anyware Technologies - initial API and implementation
  *
 * $Id: EmfActionBarContributor.java,v 1.7 2009/12/02 11:24:33 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.editor;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.ui.action.*;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.pde.emfforms.internal.Activator;
 import org.eclipse.ui.*;
 import org.eclipse.ui.actions.ActionFactory;
 
 /**
  * A specialized {@link EditingDomainActionBarContributor} very similar
  * to what EMF codegen generates in the editor layer.
  */
 public class EmfActionBarContributor extends EditingDomainActionBarContributor implements ISelectionChangedListener {
 
 	/** The create child menu manager. */
 	private MenuManager createChildMenuManager;
 
 	/** The create child menu manager. */
 	private MenuManager createSiblingMenuManager;
 
 	/** The active editor part. */
 	private IEditorPart activeEditorPart;
 
 	/** The selection provider. */
 	private ISelectionProvider selectionProvider;
 
 	/** The create child actions. */
 	private Collection<IAction> createChildActions;
 
 	/** The create sibling actions */
 	protected Collection<IAction> createSiblingActions;
 
 	@Override
 	public void init(IActionBars actionBars) {
 		super.init(actionBars);
 		// initially disable every global action redirection
 		disableGlobalHandlers();
 	}
 
 	public void disableGlobalHandlers() {
 		getActionBars().clearGlobalActionHandlers();
 		getActionBars().updateActionBars();
 	}
 
 	public void enableGlobalHandlers() {
 		IActionBars actionBars = getActionBars();
 		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
 		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), cutAction);
 		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
 		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
 		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
 		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
 		actionBars.updateActionBars();
 	}
 
 	/** The show properties view action. */
 	protected IAction showPropertiesViewAction = new Action("Open properties") {
 		@Override
 		public void run() {
 			try {
 				getPage().showView("org.eclipse.ui.views.PropertySheet");
 			} catch (PartInitException exception) {
 				Activator.logErrorMessage("The properties view is not available");
 			}
 		}
 	};
 
 	/** The filter for create child actions . */
 	private IFilter createChildMenuFilter = AcceptAllFilter.getInstance();
 
 	/** The filter for create sibling actions. */
 	private IFilter createSiblingMenuFilter = AcceptAllFilter.getInstance();
 
 	private String menuID;
 
 	public void setMenuID(String menuID) {
 		this.menuID = menuID;
 	}
 
 	/**
 	 * Instantiates a new emf action bar contributor.
 	 */
 	public EmfActionBarContributor() {
 		super(ADDITIONS_LAST_STYLE);
 		loadResourceAction = new LoadResourceAction();
 		validateAction = new ValidateAction();
 		controlAction = new ControlAction();
 	}
 
 	public EmfActionBarContributor(String menuID) {
 		this();
 		setMenuID(menuID);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
 	 */
 	@Override
 	public void contributeToMenu(IMenuManager menuManager) {
 		super.contributeToMenu(menuManager);
 
 		IMenuManager submenuManager = new MenuManager("", menuID);
 		menuManager.insertAfter("additions", submenuManager);
 		submenuManager.add(new Separator("settings"));
 		submenuManager.add(new Separator("actions"));
 		submenuManager.add(new Separator("additions"));
 		submenuManager.add(new Separator("additions-end"));
 
 		// Prepare for CreateChild item addition or removal.
 		createChildMenuManager = new MenuManager("New");
 		submenuManager.insertBefore("additions", createChildMenuManager);
 
 		// Force an update because Eclipse hides empty menus now.
 		submenuManager.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager menuManager) {
 				menuManager.updateAll(true);
 			}
 		});
 		addGlobalActions(submenuManager);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
 	 */
 	@Override
 	public void setActiveEditor(IEditorPart part) {
 		super.setActiveEditor(part);
 		activeEditorPart = part;
 
 		// Switch to the new selection provider.
 		if (selectionProvider != null) {
 			selectionProvider.removeSelectionChangedListener(this);
 		}
 		if (part == null) {
 			selectionProvider = null;
 		} else {
 			selectionProvider = part.getSite().getSelectionProvider();
 			selectionProvider.addSelectionChangedListener(this);
 
 			// Fake a selection changed event to update the menus.
 			if (selectionProvider.getSelection() != null) {
 				selectionChanged(new SelectionChangedEvent(selectionProvider, selectionProvider.getSelection()));
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		// Remove any menu items for old selection.
 		if (createChildMenuManager != null) {
 			depopulateManager(createChildMenuManager, createChildActions);
 		}
 		if (createSiblingMenuManager != null) {
 			depopulateManager(createSiblingMenuManager, createSiblingActions);
 		}
 
 		// Query the new selection for appropriate new child/sibling descriptors
 		Collection<?> newChildDescriptors = null;
 		Collection<?> newSiblingDescriptors = null;
 
 		ISelection selection = event.getSelection();
 		if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
 			Object object = ((IStructuredSelection) selection).getFirstElement();
 
 			EditingDomain domain = ((IEditingDomainProvider) activeEditorPart).getEditingDomain();
 			newChildDescriptors = domain.getNewChildDescriptors(object, null);
 			newSiblingDescriptors = domain.getNewChildDescriptors(null, object);
 		}
 
 		// Generate actions for selection; populate and redraw the menus.
 		createChildActions = generateCreateChildActions(newChildDescriptors, selection);
 		createSiblingActions = generateCreateSiblingActions(newSiblingDescriptors, selection);
 
 		if (createChildMenuManager != null) {
 			populateManager(createChildMenuManager, createChildActions, null);
 			createChildMenuManager.update(true);
 		}
 		if (createSiblingMenuManager != null) {
 			populateManager(createSiblingMenuManager, createSiblingActions, null);
 			createSiblingMenuManager.update(true);
 		}
 
 	}
 
 	/**
 	 * Populate manager.
 	 * 
 	 * @param manager the manager
 	 * @param actions the actions
 	 * @param contributionID the contribution id
 	 */
 	private void populateManager(MenuManager manager, Collection<? extends IAction> actions, String contributionID) {
 		if (actions != null) {
 			for (IAction action : actions) {
 				if (contributionID != null) {
 					manager.insertBefore(contributionID, action);
 				} else {
 					manager.add(action);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Generate create actions for child menu.
 	 * 
 	 * @param childDescriptors the child descriptors
 	 * @param selection the selection
 	 * 
 	 * @return the collection< i action>
 	 */
	public Collection<IAction> generateCreateChildActions(Collection<?> childDescriptors, ISelection selection) {
 		Collection<IAction> actions = new ArrayList<IAction>();
 		if (childDescriptors != null) {
 			for (Object descriptor : childDescriptors) {
 				if (this.createChildMenuFilter.select(((CommandParameter) descriptor).value))
 					actions.add(new CreateChildAction(activeEditorPart, selection, descriptor));
 			}
 		}
 
 		return actions;
 	}
 
 	/**
 	 * Generate create actions for sibling menu.
 	 * 
 	 * @param descriptors the sibling descriptors
 	 * @param selection the selection
 	 * 
 	 * @return the collection< i action>
 	 */
	public Collection<IAction> generateCreateSiblingActions(Collection<?> descriptors, ISelection selection) {
 		Collection<IAction> actions = new ArrayList<IAction>();
 		if (descriptors != null) {
 			for (Object descriptor : descriptors) {
 				if (this.createSiblingMenuFilter.select(((CommandParameter) descriptor).value))
 					actions.add(new CreateSiblingAction(activeEditorPart, selection, descriptor));
 			}
 		}
 		return actions;
 	}
 
 	/**
 	 * Depopulate manager.
 	 * 
 	 * @param manager the manager
 	 * @param actions the actions
 	 */
 	protected void depopulateManager(IContributionManager manager, Collection<? extends IAction> actions) {
 		if (actions != null) {
 			IContributionItem[] items = manager.getItems();
 			for (int i = 0; i < items.length; i++) {
 				// Look into SubContributionItems
 				IContributionItem contributionItem = items[i];
 				while (contributionItem instanceof SubContributionItem) {
 					contributionItem = ((SubContributionItem) contributionItem).getInnerItem();
 				}
 
 				// Delete the ActionContributionItems with matching action.
 				if (contributionItem instanceof ActionContributionItem) {
 					IAction action = ((ActionContributionItem) contributionItem).getAction();
 					if (actions.contains(action)) {
 						manager.remove(contributionItem);
 					}
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
 	 */
 	@Override
 	public void menuAboutToShow(IMenuManager menuManager) {
 		super.menuAboutToShow(menuManager);
 		MenuManager submenuManager = null;
 
 		submenuManager = new MenuManager("New child");
 		populateManager(submenuManager, createChildActions, null);
 		menuManager.insertBefore("edit", submenuManager);
 
 		submenuManager = new MenuManager("New sibling");
 		populateManager(submenuManager, createSiblingActions, null);
 		menuManager.insertBefore("edit", submenuManager);
 
 	}
 
 	/**
 	 * Sets the filter used to selection actions to display in the create child menu.
 	 * 
 	 * @param filter the new filter
 	 */
 	public void setCreateChildMenuFilter(IFilter filter) {
 		this.createChildMenuFilter = filter;
 	}
 
 	/**
 	 * Sets the filter used to selection actions to display in the create sibling menu.
 	 * 
 	 * @param filter the new filter
 	 */
 	public void setCreateSiblingMenuFilter(IFilter filter) {
 		this.createSiblingMenuFilter = filter;
 	}
 
 }
