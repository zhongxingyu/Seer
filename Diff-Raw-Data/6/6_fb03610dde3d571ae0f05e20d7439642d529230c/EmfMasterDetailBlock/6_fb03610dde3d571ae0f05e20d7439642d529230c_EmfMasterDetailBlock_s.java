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
 * $Id: EmfMasterDetailBlock.java,v 1.19 2009/09/24 07:57:10 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.editor;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.ui.dnd.*;
 import org.eclipse.emf.edit.ui.provider.*;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.pde.emfforms.internal.actions.RemoveAction;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.IEditorActionBarContributor;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.dialogs.PatternFilter;
 import org.eclipse.ui.forms.*;
 import org.eclipse.ui.forms.widgets.*;
 
 public abstract class EmfMasterDetailBlock extends MasterDetailsBlock implements IDetailsPageProvider, IMenuListener {
 
 	protected EmfFormEditor<?> parentEditor;
 
 	/**
 	 * Style constant to indicate that no generic buttons (neither toolbar nor push) should be displayed 
 	 */
 	public static final int NO_BUTTONS = 0;
 
 	/**
 	 * Style constant to indicate whether generic toolbar buttons should be displayed or not 
 	 */
 	public static final int USE_GENERIC_TOOLBAR_BUTTONS = 1 << 0;
 
 	/**
 	 * Style constant to indicate whether generic push buttons should be displayed on the
 	 * right-hand side of the tree viewer or not
 	 */
 	public static final int USE_GENERIC_PUSH_BUTTONS = 1 << 1;
 
 	/**
 	 * Style constant to indicate whether custom push buttons should be displayed on the
 	 * right-hand side of the tree viewer or not.
 	 * If the flag is set, the {@link EmfMasterDetailBlock#createCustomButtons(Composite)} will be called. 
 	 */
 	public static final int USE_CUSTOM_PUSH_BUTTONS = 1 << 2;
 
 	protected int buttonOption = USE_GENERIC_TOOLBAR_BUTTONS;
 
 	private String title;
 	private TreeViewer treeViewer;
 	private Button addButton;
 	private Button removeButton;
 
 	protected ToolBarManager toolBarManager;
 	private Action removeAction;
 
 	public EmfMasterDetailBlock(EmfFormEditor<?> editor, String title) {
 		this.title = title;
 		this.parentEditor = editor;
 	}
 
 	public EmfMasterDetailBlock(EmfFormEditor<?> editor, String title, int buttonOption) {
 		this(editor, title);
 		this.buttonOption = buttonOption;
 	}
 
 	@Override
 	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
 		FormToolkit toolkit = parentEditor.getToolkit();
 
 		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
 		section.setText(title);
 		section.setDescription("Edit " + title); //$NON-NLS-1$
 		section.marginWidth = 5;
 		section.setLayout(new FillLayout());
 		section.marginHeight = 5;
 
 		Composite client = toolkit.createComposite(section, SWT.WRAP);
 		GridLayoutFactory.fillDefaults().numColumns(showPushButtons() ? 2 : 1).applyTo(client);
 
 		// deliberate use of the 3.4 API
 		// TODO try to use the new look using a 3.5 fragment
 		FilteredTree ft = new FilteredTree(client, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter());
 		treeViewer = ft.getViewer();
 
 		//Buttons
 		if (showPushButtons()) {
 			Composite buttonComposite = new Composite(client, SWT.NONE);
 			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(buttonComposite);
 
 			if (showGenericPushButtons())
 				addButton = createButton(buttonComposite, "Add"); //$NON-NLS-1$
 			if (showCustomPushButtons())
 				createCustomButtons(buttonComposite);
 			if (showGenericPushButtons())
 				removeButton = createButton(buttonComposite, "Remove"); //$NON-NLS-1$
 
 			GridDataFactory.fillDefaults().grab(false, false).applyTo(buttonComposite);
 		}
 
 		//SectionToolBar
 		removeAction = createCustomToolbarRemoveAction();
 		if (removeAction == null) {
 			removeAction = new RemoveAction(this);
 		}
 
 		if (showToolbarButtons()) {
 			toolBarManager = PDEFormToolkit.createSectionToolBarManager(section);
 			Action addAction = createCustomToolbarAddAction();
 			if (addAction != null) {
 				toolBarManager.add(addAction);
 			}
 
 			toolBarManager.add(removeAction);
 			toolBarManager.update(true);
 			section.setTextClient(toolBarManager.getControl());
 		}
 
 		treeViewer.setContentProvider(new AdapterFactoryContentProvider(parentEditor.getAdapterFactory()));
 		treeViewer.setLabelProvider(new DecoratingLabelProvider(new AdapterFactoryLabelProvider(parentEditor.getAdapterFactory()), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
 		treeViewer.addFilter(getTreeFilter());
 
 		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		Transfer[] transfers = new Transfer[] {LocalTransfer.getInstance()};
 		treeViewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(treeViewer));
 		treeViewer.addDropSupport(dndOperations, transfers, new EditingDomainViewerDropAdapter(parentEditor.getEditingDomain(), treeViewer));
 
 		final SectionPart spart = new SectionPart(section);
 		managedForm.addPart(spart);
 
 		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				managedForm.fireSelectionChanged(spart, event.getSelection());
 			}
 		});
 
 		treeViewer.addOpenListener(new IOpenListener() {
 			public void open(OpenEvent event) {
 				detailsPart.setFocus();
 			}
 		});
 
 		// Add listeners to manage activation/deactivation of the treeViewer's
 		// ActionBarContributor's global handlers
 		configureActionBarManagement();
 
 		if (getRemoveButton() != null) {
 
 			DataBindingContext bindingContext = new DataBindingContext();
 
 			//Enable button when the tree selection is not empty
 			bindingContext.bindValue(ViewersObservables.observeSingleSelection(getTreeViewer()), SWTObservables.observeEnabled(getRemoveButton()), new EMFUpdateValueStrategy() {
 				/**
 				 * @see org.eclipse.core.databinding.UpdateValueStrategy#convert(java.lang.Object)
 				 */
 				@Override
 				public Object convert(Object value) {
 					return !(getTreeViewer().getSelection().isEmpty());
 				}
 			}, null);
 
 			//Generic action for remove button
 			getRemoveButton().addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					Object sel = ((IStructuredSelection) getTreeViewer().getSelection()).getFirstElement();
 					if (sel != null) {
 						Command c = RemoveCommand.create(getEditor().getEditingDomain(), sel);
 						getEditor().getEditingDomain().getCommandStack().execute(c);
 					}
 				}
 			});
 		}
 
 		if (showToolbarButtons()) {
 
 			//Enable action when the tree selection is not empty
 			ViewersObservables.observeSingleSelection(getTreeViewer()).addValueChangeListener(new IValueChangeListener() {
 				public void handleValueChange(ValueChangeEvent event) {
 					boolean bool = !(getTreeViewer().getSelection().isEmpty());
 					removeAction.setEnabled(bool);
 				}
 			});
 
 		}
 
 		createContextMenuFor(treeViewer);
 
 		//update Editor selection
 		getEditor().addViewerToListenTo(getTreeViewer());
 
 		section.setClient(client);
 	}
 
 	/**
 	 * Add listeners to manage activation/deactivation of the treeViewer's
 	 * ActionBarContributor's global handlers
 	 */
 	protected void configureActionBarManagement() {
 		final IEditorActionBarContributor actionBarContributor = getEditor().getEditorSite().getActionBarContributor();
 
 		if (actionBarContributor != null && actionBarContributor instanceof EmfActionBarContributor) {
 			treeViewer.getControl().addFocusListener(new FocusAdapter() {
 				@Override
 				public void focusGained(FocusEvent e) {
 					((EmfActionBarContributor) actionBarContributor).enableGlobalHandlers();
 				}
 
 				@Override
 				public void focusLost(FocusEvent e) {
 					((EmfActionBarContributor) actionBarContributor).disableGlobalHandlers();
 				}
 			});
 		}
 	}
 
 	private boolean showPushButtons() {
 		return showCustomPushButtons() || showGenericPushButtons();
 	}
 
 	private boolean showCustomPushButtons() {
 		return ((buttonOption & USE_CUSTOM_PUSH_BUTTONS) > 0);
 	}
 
 	private boolean showGenericPushButtons() {
 		return ((buttonOption & USE_GENERIC_PUSH_BUTTONS) > 0);
 	}
 
 	private boolean showToolbarButtons() {
 		return (buttonOption & USE_GENERIC_TOOLBAR_BUTTONS) > 0;
 	}
 
 	protected Action createCustomToolbarAddAction() {
 		// Subclass may override this method
 		return null;
 	}
 
 	protected Action createCustomToolbarRemoveAction() {
 		// Subclass may override this method
 		return null;
 	}
 
 	protected Button createButton(Composite parent, String btnText) {
 		Button btn = new Button(parent, SWT.FLAT | SWT.PUSH);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(btn);
 		btn.setText(btnText);
 
 		return btn;
 	}
 
 	/**
 	 * Return a ViewerFilter to apply on the treeViewer
 	 * 
 	 * @return a ViewerFilter to apply on the treeViewer
 	 */
 	protected abstract ViewerFilter getTreeFilter();
 
 	@Override
 	protected void createToolBarActions(IManagedForm managedForm) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	protected void registerPages(DetailsPart detailsPart) {
 		detailsPart.setPageProvider(this);
 	}
 
 	public Object getPageKey(Object object) {
 		return AdapterFactoryEditingDomain.unwrap(object).getClass();
 	}
 
 	public TreeViewer getTreeViewer() {
 		return treeViewer;
 	}
 
 	/**
 	 * @return The "Add..." button that can be used to hook an element creation wizard, or <code>null</code> if the {@link EmfMasterDetailBlock#useGenericButton} flag is set to <code>false</code> 
 	 */
 	public Button getGenericAddButton() {
 		return addButton;
 	}
 
 	protected void createCustomButtons(Composite parent) {
 		// Should be overriden by clients wanting to contribute their own "add" button(s) 
 	}
 
 	public Button getRemoveButton() {
 		return removeButton;
 	}
 
 	public void setAddButton(Button addButton) {
 		this.addButton = addButton;
 	}
 
 	public void setRemoveButton(Button removeButton) {
 		this.removeButton = removeButton;
 	}
 
 	protected void createContextMenuFor(StructuredViewer viewer) {
 		MenuManager contextMenu = new MenuManager("#PopUp"); //$NON-NLS-1$
 		contextMenu.add(new Separator("additions")); //$NON-NLS-1$
 		contextMenu.setRemoveAllWhenShown(true);
 		contextMenu.addMenuListener(this);
 		Menu menu = contextMenu.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		IEditorActionBarContributor actionBarContributor = parentEditor.getEditorSite().getActionBarContributor();
 		if (actionBarContributor != null && actionBarContributor instanceof EmfActionBarContributor) {
 			((EmfActionBarContributor) actionBarContributor).setCreateChildMenuFilter(getCreateChildContextMenuFilter());
 			((EmfActionBarContributor) actionBarContributor).setCreateSiblingMenuFilter(getCreateSiblingContextMenuFilter());
 		}
 		parentEditor.getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));
 	}
 
 	/**
 	 * TODO doc
 	 */
 	protected IFilter getCreateChildContextMenuFilter() {
 		return AcceptAllFilter.getInstance();
 	}
 
 	/**
 	 * TODO doc
 	 */
 	protected IFilter getCreateSiblingContextMenuFilter() {
 		return AcceptAllFilter.getInstance();
 	}
 
 	public void menuAboutToShow(IMenuManager manager) {
 		if (parentEditor.getEditorSite().getActionBarContributor() != null)
 			((IMenuListener) parentEditor.getEditorSite().getActionBarContributor()).menuAboutToShow(manager);
 	}
 
 	public EmfFormEditor<?> getEditor() {
 		return parentEditor;
 	}
 
 	public void setButtonOption(int buttonOption) {
 		this.buttonOption = buttonOption;
 	}
 
 }
