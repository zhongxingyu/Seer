 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 323155 - Check usage scenarios for DefaultPrintFeature and
  *            DefaultSaveImageFeature
  *    mwenz - Bug 339525 - Enrich paste context with location information
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.gef.ContextMenuProvider;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.actions.GEFActionConstants;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IPrintFeature;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.impl.CustomContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.internal.pref.GFPreferences;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.tb.ContextMenuEntry;
 import org.eclipse.graphiti.tb.IContextMenuEntry;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.action.CustomAction;
 import org.eclipse.graphiti.ui.internal.action.DeleteAction;
 import org.eclipse.graphiti.ui.internal.action.IAvailable;
import org.eclipse.graphiti.ui.internal.action.PasteAction;
 import org.eclipse.graphiti.ui.internal.action.RemoveAction;
 import org.eclipse.graphiti.ui.internal.action.SaveImageAction;
 import org.eclipse.graphiti.ui.internal.action.UpdateAction;
 import org.eclipse.graphiti.ui.internal.editor.DiagramEditorInternal;
 import org.eclipse.graphiti.ui.internal.feature.DebugFeature;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.graphiti.util.ILocationInfo;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.actions.ActionFactory;
 
 /**
  * Provides a standard-context-menu and adds it to the given EditPartViewer.
  * Another feature is, that it can set the menu-location to all Actions.
  * <p>
  * Some of the standard-menu-items are: undo/redo, delete, copy/paste,
  * alignment, zooming.
  * 
  */
 public class DiagramEditorContextMenuProvider extends ContextMenuProvider {
 
 	private final ActionRegistry actionRegistry;
 
 	private final IDiagramTypeProvider diagramTypeProvider;
 
 	/**
 	 * Creates a new DiagramEditorContextMenuProvider.
 	 * 
 	 * @param viewer
 	 *            The EditPartViewer, for which the context-menu shall be
 	 *            displayed.
 	 * @param registry
 	 *            The action-registry, which contains the actions corresponding
 	 *            to the menu-items.
 	 * @param configurationProvider
 	 *            the configuration provider
 	 * @since 0.9
 	 */
 	public DiagramEditorContextMenuProvider(EditPartViewer viewer, ActionRegistry registry, IDiagramTypeProvider diagramTypeProvider) {
 		super(viewer);
 		if (registry == null) {
 			throw new IllegalArgumentException("Argument registry must not be null"); //$NON-NLS-1$
 		}
 		this.actionRegistry = registry;
 		if (diagramTypeProvider == null) {
 			throw new IllegalArgumentException("Argument configurationProvider must not be null"); //$NON-NLS-1$
 		}
 		this.diagramTypeProvider = diagramTypeProvider;
 	}
 
 	/**
 	 * Adds the Actions to the given IMenuManager, which is displayed as a
 	 * context-menu.
 	 * 
 	 * @param manager
 	 *            the manager
 	 * @see ContextMenuProvider#buildContextMenu(IMenuManager)
 	 */
 	@Override
 	public void buildContextMenu(IMenuManager manager) {
 		GEFActionConstants.addStandardActionGroups(manager);
 		addDefaultMenuGroupUndo(manager);
 		addDefaultMenuGroupSave(manager);
 		addDefaultMenuGroupEdit(manager);
 		addDefaultMenuGroupPrint(manager);
 		addDefaultMenuGroupRest(manager);
 	}
 
 	// ====================== add default menu-groups =========================
 
 	/**
 	 * Adds the default menu group undo.
 	 * 
 	 * @param manager
 	 *            the manager
 	 */
 	protected void addDefaultMenuGroupUndo(IMenuManager manager) {
 		addActionToMenu(manager, ActionFactory.UNDO.getId(), GEFActionConstants.GROUP_UNDO);
 		addActionToMenu(manager, ActionFactory.REDO.getId(), GEFActionConstants.GROUP_UNDO);
 	}
 
 	/**
 	 * Adds the default menu group save.
 	 * 
 	 * @param manager
 	 *            the manager
 	 */
 	protected void addDefaultMenuGroupSave(IMenuManager manager) {
 		addActionToMenu(manager, SaveImageAction.ACTION_ID, GEFActionConstants.GROUP_SAVE);
 	}
 
 	/**
 	 * Adds the default menu group edit.
 	 * 
 	 * @param manager
 	 *            the manager
 	 */
 	protected void addDefaultMenuGroupEdit(IMenuManager manager) {
 		addActionToMenuIfAvailable(manager, ActionFactory.COPY.getId(), GEFActionConstants.GROUP_EDIT);
 		addActionToMenuIfAvailable(manager, ActionFactory.PASTE.getId(), GEFActionConstants.GROUP_EDIT);
 	}
 
 	/**
 	 * Adds the default menu group print.
 	 * 
 	 * @param manager
 	 *            the manager
 	 */
 	protected void addDefaultMenuGroupPrint(IMenuManager manager) {
 		IFeatureProvider fp = getDiagramTypeProvider().getFeatureProvider();
 		if (fp != null) {
 			IPrintFeature pf = fp.getPrintFeature();
 
 			if (pf != null) {
 				addActionToMenu(manager, ActionFactory.PRINT.getId(), GEFActionConstants.GROUP_PRINT);
 			}
 		}
 	}
 
 	/**
 	 * Adds the default menu group rest.
 	 * 
 	 * @param manager
 	 *            the manager
 	 */
 	protected void addDefaultMenuGroupRest(IMenuManager manager) {
 		addAlignmentSubMenu(manager, GEFActionConstants.GROUP_REST);
 
 		addActionToMenuIfAvailable(manager, UpdateAction.ACTION_ID, GEFActionConstants.GROUP_REST);
 		addActionToMenuIfAvailable(manager, RemoveAction.ACTION_ID, GEFActionConstants.GROUP_REST);
 		addActionToMenuIfAvailable(manager, DeleteAction.ACTION_ID, GEFActionConstants.GROUP_REST);
 
 		PictogramElement pes[] = getEditor().getSelectedPictogramElements();
 		ICustomContext context = new CustomContext(pes);
 
 		if (pes.length == 1) {
 			extendCustomContext(pes[0], (CustomContext) context);
 		}
 
 		IToolBehaviorProvider tb = getDiagramTypeProvider().getCurrentToolBehaviorProvider();
 
 		IContextMenuEntry[] contextMenuEntries = tb.getContextMenu(context);
 
 		if (GFPreferences.getInstance().areDebugActionsActive()) {
 			IFeatureProvider fp = getDiagramTypeProvider().getFeatureProvider();
 			ContextMenuEntry debugEntry = new ContextMenuEntry(null, context);
 			debugEntry.setText("Debug"); //$NON-NLS-1$
 			debugEntry.setSubmenu(true);
 			debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_FIGURE_DATA), context));
 			debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_PICTOGRAM_DATA), context));
 			debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_EDIT_PART_DATA), context));
 			debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_DUMP_ALL), context));
 			debugEntry.add(new ContextMenuEntry(new DebugFeature(fp, DebugFeature.TYPE_REFRESH), context));
 			IContextMenuEntry[] contextMenuEntries2 = new IContextMenuEntry[contextMenuEntries.length + 1];
 			System.arraycopy(contextMenuEntries, 0, contextMenuEntries2, 0, contextMenuEntries.length);
 			contextMenuEntries2[contextMenuEntries2.length - 1] = debugEntry;
 			contextMenuEntries = contextMenuEntries2;
 		}
 
 		addEntries(manager, contextMenuEntries, context, GEFActionConstants.GROUP_REST, null);
 	}
 
 	private void addEntries(IMenuManager manager, IContextMenuEntry[] contextMenuEntries, ICustomContext context, String groupID,
 			String textParentEntry) {
 
 		for (int i = 0; i < contextMenuEntries.length; i++) {
 			IContextMenuEntry cmEntry = contextMenuEntries[i];
 			String text = cmEntry.getText();
 			if (cmEntry.getChildren().length == 0) {
 				IFeature feature = cmEntry.getFeature();
 				if (feature instanceof ICustomFeature && feature.isAvailable(context)) {
 					IAction action = new CustomAction((ICustomFeature) feature, context, getEditor());
 					if (textParentEntry != null) {
 						text = textParentEntry + " " + text; //$NON-NLS-1$
 					}
 					action.setText(text);
 					action.setDescription(cmEntry.getDescription());
 					ImageDescriptor image = GraphitiUi.getImageService().getImageDescriptorForId(cmEntry.getIconId());
 					action.setImageDescriptor(image);
 					appendContributionItem(manager, groupID, new ActionContributionItem(action));
 				}
 			} else {
 				if (cmEntry.isSubmenu()) {
 
 					MenuManager subMenu = new MenuManager(text);
 					addEntries(subMenu, cmEntry.getChildren(), context, null, null);
 					if (!subMenu.isEmpty()) {
 						appendContributionItem(manager, groupID, subMenu);
 					}
 				} else {
 					appendContributionItem(manager, groupID, new Separator());
 					addEntries(manager, cmEntry.getChildren(), context, groupID, text);
 					appendContributionItem(manager, groupID, new Separator());
 				}
 			}
 		}
 	}
 
 	private void appendContributionItem(IMenuManager manager, String groupID, IContributionItem contributionItem) {
 		if (groupID != null) {
 			manager.appendToGroup(groupID, contributionItem);
 		} else {
 			manager.add(contributionItem);
 		}
 	}
 
 	// ====================== add single menu-entries =========================
 
 	private void extendCustomContext(PictogramElement pe, CustomContext context) {
 		Point location = getEditor().getMouseLocation();
 		int mX = location.x;
 		int mY = location.y;
 		context.setX(mX);
 		context.setY(mY);
 
 		if (pe instanceof Shape && !(pe instanceof Diagram)) {
 			GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 			if (ga != null) {
 				ILocation relLocation = Graphiti.getPeService().getLocationRelativeToDiagram((Shape) pe);
 				int x = relLocation.getX();
 				int y = relLocation.getY();
 				int width = ga.getWidth();
 				int height = ga.getHeight();
 
 				if (mX > x && mX < x + width && mY > y && mY < y + height) {
 					int relativeX = mX - x;
 					int relativeY = mY - y;
 					ILocationInfo locationInfo = Graphiti.getLayoutService().getLocationInfo((Shape) pe, relativeX, relativeY);
 					context.setInnerPictogramElement(locationInfo.getShape());
 					context.setInnerGraphicsAlgorithm(locationInfo.getGraphicsAlgorithm());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds the alignment sub menu.
 	 * 
 	 * @param manager
 	 *            the manager
 	 * @param group
 	 *            the group
 	 */
 	protected void addAlignmentSubMenu(IMenuManager manager, String group) {
 		IAction action;
 		MenuManager alignmentSubMenu = new MenuManager(Messages.GraphicsContextMenuProvider_0_xmen);
 
 		action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_LEFT);
 		if (action != null && action.isEnabled()) {
 			alignmentSubMenu.add(action);
 		}
 
 		action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_CENTER);
 		if (action != null && action.isEnabled()) {
 			alignmentSubMenu.add(action);
 		}
 
 		action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_RIGHT);
 		if (action != null && action.isEnabled()) {
 			alignmentSubMenu.add(action);
 		}
 
 		action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_TOP);
 		if (action != null && action.isEnabled()) {
 			alignmentSubMenu.add(action);
 		}
 
 		action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_MIDDLE);
 		if (action != null && action.isEnabled()) {
 			alignmentSubMenu.add(action);
 		}
 
 		action = this.actionRegistry.getAction(GEFActionConstants.ALIGN_BOTTOM);
 		if (action != null && action.isEnabled()) {
 			alignmentSubMenu.add(action);
 		}
 
 		action = this.actionRegistry.getAction(GEFActionConstants.MATCH_WIDTH);
 		if (action != null && action.isEnabled()) {
 			alignmentSubMenu.add(action);
 		}
 
 		action = this.actionRegistry.getAction(GEFActionConstants.MATCH_HEIGHT);
 		if (action != null && action.isEnabled()) {
 			alignmentSubMenu.add(action);
 		}
 
 		if (!alignmentSubMenu.isEmpty()) {
 			manager.appendToGroup(group, alignmentSubMenu);
 		}
 	}
 
 	/**
 	 * Adds the action to menu.
 	 * 
 	 * @param manager
 	 *            the manager
 	 * @param actionId
 	 *            the action id
 	 * @param menuGroup
 	 *            the menu group
 	 */
 	protected void addActionToMenu(IMenuManager manager, String actionId, String menuGroup) {
 		IAction action;
 		action = this.actionRegistry.getAction(actionId);
 		if (action != null && action.isEnabled()) {
 			manager.appendToGroup(menuGroup, action);
 		}
 	}
 
 	/**
 	 * Adds the action to menu if available.
 	 * 
 	 * @param manager
 	 *            the manager
 	 * @param actionId
 	 *            the action id
 	 * @param menuGroup
 	 *            the menu group
 	 */
 	protected void addActionToMenuIfAvailable(IMenuManager manager, String actionId, String menuGroup) {
 		IAction action = this.actionRegistry.getAction(actionId);
 		if (action instanceof IAvailable) {
 			if (((IAvailable) action).isAvailable()) {
 				manager.appendToGroup(menuGroup, action);
				//For Update Actions we have to trigger a refresh of the enablement, also if no selection change occured:
 				//e.g. update was triggered, then update has to be disabled.
 				if (action instanceof UpdateAction) {
 					UpdateAction updateAction = (UpdateAction) action;
 					updateAction.setEnabled(updateAction.isEnabled());

				} else if (action instanceof PasteAction) {
					// For the paste action we need the infomation at which
					// location the paste operation was triggered. This
					// infomation can only be retrieved from the mouse location
					// at the time the context menu is created
					((PasteAction) action).setPasteLocation(getEditor().getMouseLocation());
 				}
 			}
 		}
 	}
 
 	@Override
 	protected boolean allowItem(IContributionItem itemToAdd) {
 		boolean ret = super.allowItem(itemToAdd);
 		if (ret) {
 			String itemId = itemToAdd.getId();
 			if (itemId != null) {
 				if (itemId.startsWith("org.eclipse.debug.ui.contextualLaunch.")) { //$NON-NLS-1$
 					return false;
 				}
 			}
 		}
 		return ret;
 	}
 
 	private IDiagramTypeProvider getDiagramTypeProvider() {
 		return this.diagramTypeProvider;
 	}
 
 	private DiagramEditorInternal getEditor() {
 		return (DiagramEditorInternal) getDiagramTypeProvider().getDiagramEditor();
 	}
 }
