 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.editor.actions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
 import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
 import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
 import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.TrayDialog;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.rmf.pror.reqif10.editor.presentation.ProrAdapterFactoryContentProvider;
 import org.eclipse.rmf.pror.reqif10.editor.presentation.Reqif10Editor;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Sash;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This modal dialog shows the tree hanging on a provided {@link EObject}. The
  * idea is to use this to allow the user to configure things selectively (e.g.
  * the columns of a {@link Specification}).
  * 
  * @author jastram
  * 
  */
 public class SubtreeDialog extends TrayDialog implements IMenuListener {
 
 	private static final int VALIDATE_ID = 99;
 	private final EObject input;
 	private final String title;
 	private final String helpContext;
 	private ISelectionProvider originalSelectionProvider;
 	private IAction[] actions = new IAction[] {};
 	private boolean presentAsDropdown;
 	private TreeViewer viewer;
 	private final List<ViewerFilter> filters = new ArrayList<ViewerFilter>();
 	private final AdapterFactory adapterFactory;
 	private final EditingDomain editingDomain;
 	private final Reqif10Editor rifEditor;
 
	protected SubtreeDialog(Reqif10Editor rifEditor, EObject input, String title,
 			String helpContext) {
 		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.rifEditor = rifEditor;
		this.editingDomain = rifEditor.getEditingDomain();
		this.adapterFactory = rifEditor.getAdapterFactory();
 		this.input = input;
 		this.title = title;
 		this.helpContext = helpContext;
 		setHelpAvailable(true);
 	}
 
 	void addFilter(ViewerFilter filter) {
 		filters.add(filter);
 	}
 
 	/**
 	 * We provide the default "Finish" button, instead of the default OK and
 	 * Cancel.
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID,
 				IDialogConstants.FINISH_LABEL, true);
 
 		createButton(parent, VALIDATE_ID, "Validate", false);
 	}
 
 	@Override
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == VALIDATE_ID) {
 			IStructuredSelection selection = (IStructuredSelection) viewer
 					.getSelection();
 			if (selection.getFirstElement() instanceof EObject) {
 				EObject element = (EObject) selection.getFirstElement();
 				// TODO this is not yet working...
 				Diagnostic diagnostic = Diagnostician.INSTANCE
 						.validate(element);
 				MessageDialog.openInformation(this.getShell(),
 						"Validation Report", diagnostic.getMessage());
 			}
 		} else {
 			super.buttonPressed(buttonId);
 		}
 	}
 
 	@Override
 	protected Point getInitialSize() {
 		return new Point(500, 600);
 	}
 
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		this.getShell().setText(title);
 
 		Composite composite = (Composite) super.createDialogArea(parent);
 		composite.setLayout(new FormLayout());
 
 		ToolBar toolbar = presentAsDropdown ? buildDropdownToolbar(composite)
 				: buildRowToolbar(composite);
 
 		// Enable help
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, helpContext);
 
 		final Sash sash = new Sash(composite, SWT.HORIZONTAL | SWT.BORDER);
 		FormData data = new FormData();
 		data.bottom = new FormAttachment(50, 0);
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0);
 		sash.setLayoutData(data);
 
 		// Without this, the sash wouldn't hold its new position
 		sash.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event e) {
 				sash.setBounds(e.x, e.y, e.width, e.height);
 				((FormData) sash.getLayoutData()).bottom = new FormAttachment(
 						0, e.y);
 				sash.getParent().layout();
 			}
 		});
 
 		viewer = new TreeViewer(composite, SWT.BORDER);
 		viewer.setContentProvider(new ProrAdapterFactoryContentProvider(
 				getAdapterFactory()));
 		viewer.setLabelProvider(new AdapterFactoryLabelProvider(
 				getAdapterFactory()));
 		viewer.setInput(input);
 		viewer.setFilters(filters.toArray(new ViewerFilter[] {}));
 		createContextMenuFor(viewer);
 
 		data = new FormData();
 		data.top = new FormAttachment(toolbar, 0);
 		data.bottom = new FormAttachment(sash, 0);
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0);
 		viewer.getControl().setLayoutData(data);
 
 		// Now that we have the TreeViewer, let's attach the Toolbar
 		data = new FormData();
 		data.bottom = new FormAttachment(viewer.getControl(), 0);
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0);
 		toolbar.setLayoutData(data);
 
 		final ExtendedPropertySheetPage propertySheet = new ExtendedPropertySheetPage(
 				(AdapterFactoryEditingDomain) editingDomain);
 		propertySheet.createControl(composite);
 		propertySheet
 				.setPropertySourceProvider(new ProrAdapterFactoryContentProvider(
 						getAdapterFactory()));
 
 		data = new FormData();
 		data.top = new FormAttachment(sash, 0);
 		data.bottom = new FormAttachment(100, 0);
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0);
 		propertySheet.getControl().setLayoutData(data);
 
 		// We just wire these two controls, to show the tree's properties.
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				propertySheet.selectionChanged(null, event.getSelection());
 			}
 		});
 
 		// The Editor must know about changes, so that the context
 		// menu contains the correct child creation actions.
 		originalSelectionProvider = getActionBarContributor().getActiveEditor()
 				.getSite().getSelectionProvider();
 		getActionBarContributor().getActiveEditor().getSite()
 				.setSelectionProvider(viewer);
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				((ISelectionProvider) getActionBarContributor()
 						.getActiveEditor()).setSelection(event.getSelection());
 			}
 		});
 		return composite;
 	}
 
 	/**
 	 * It's unbelievable that there is no better way for doing this! I
 	 * couldn't even use {@link MenuManager} to add the action, as that created
 	 * an additional menu hierarchy that I couldn't get rid of!
 	 * 
 	 * @param composite
 	 * @return
 	 */
 	private ToolBar buildDropdownToolbar(Composite composite) {
 		final ToolBar toolbar = new ToolBar(composite, SWT.PUSH);
 
 		final Menu menu = new Menu(getShell(), SWT.POP_UP);
 		for (final IAction action : actions) {
 			MenuItem newItem = new MenuItem(menu, SWT.PUSH);
 			newItem.setText(action.getText());
 			newItem.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					action.run();
 				}
 			});
 
 		}
 
 		final ToolItem item = new ToolItem(toolbar, SWT.DROP_DOWN);
 		item.setText("Select Action...");
 		item.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				// The following code would allow to distinguish arrow and
 				// button, something we don't need right now.
 				// if (event.detail == SWT.ARROW) {
 				Rectangle rect = item.getBounds();
 				Point pt = new Point(rect.x, rect.y + rect.height);
 				pt = toolbar.toDisplay(pt);
 				menu.setLocation(pt.x, pt.y);
 				menu.setVisible(true);
 			}
 			// }
 		});
 		return toolbar;
 	}
 
 	private ToolBar buildRowToolbar(Composite composite) {
 		ToolBar toolbar = new ToolBar(composite, SWT.PUSH);
 		ToolBarManager toolbarManager = new ToolBarManager(toolbar);
 		for (IAction action : actions) {
 			toolbarManager.add(action);
 		}
 
 		toolbarManager.update(true);
 		return toolbar;
 	}
 
 	private AdapterFactory getAdapterFactory() {
 		return adapterFactory;
 	}
 
 	public void createContextMenuFor(StructuredViewer viewer) {
 		MenuManager contextMenu = new MenuManager("#PopUp");
 		contextMenu.add(new Separator("additions"));
 		contextMenu.setRemoveAllWhenShown(true);
 		contextMenu.addMenuListener(this);
 		Menu menu = contextMenu.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 
 		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance() };
 		viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(
 				viewer));
 		viewer.addDropSupport(dndOperations, transfers,
 				new EditingDomainViewerDropAdapter(editingDomain, viewer));
 	}
 
 	@Override
 	public void menuAboutToShow(IMenuManager menuManager) {
 		getActionBarContributor().menuAboutToShow(menuManager);
 	}
 
 	private EditingDomainActionBarContributor getActionBarContributor() {
 		return rifEditor.getActionBarContributor();
 	}
 
 	@Override
 	public boolean close() {
 		getActionBarContributor().getActiveEditor().getSite()
 				.setSelectionProvider(originalSelectionProvider);
 		return super.close();
 	}
 
 	/**
 	 * If Actions are set before opening the Dialog, then these are presented in
 	 * a row or in a dropdown.
 	 * <p>
 	 * 
 	 * When shown in a row, the text is omitted and only the icon shown. This
 	 * makes sense for a small number of well-defined actions.
 	 * <p>
 	 * 
 	 * When shown in a dropdown, both icon and text are shown. This makes sense
 	 * when the text is important and/or the number of items is unknown.
 	 * 
 	 * @param actions
 	 * @param presentAsDropdown
 	 */
 	public void setActions(IAction[] actions, boolean presentAsDropdown) {
 		this.actions = actions;
 		this.presentAsDropdown = presentAsDropdown;
 	}
 }
