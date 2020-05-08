 /**
  * Copyright (c) 2009, 2010 Anyware Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Anyware Technologies - initial API and implementation
  *     Sebastien Moran <SMoran@sierrawireless.com> - bug 308802
  *
 * $Id: EmfMasterDetailBlock.java,v 1.23 2010/04/12 08:51:10 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.editor;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.property.value.IValueProperty;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.ui.dnd.*;
 import org.eclipse.emf.edit.ui.provider.*;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.util.JFaceProperties;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.pde.emfforms.editor.actions.RemoveAction;
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
 
 	public static final int DEFAULT_VIEWER_OPTIONS = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
 
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
 	private IAction removeAction;
 
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
 		FilteredTree ft = new FilteredTree(client, getViewerOptions(), new PatternFilter());
 		treeViewer = ft.getViewer();
 
 		// Prevent scrollbars to be managed by the editor's root composite
 		GridDataFactory.fillDefaults().grab(true, true).hint(50, 50).applyTo(treeViewer.getTree());
 
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
 
 		if (showToolbarButtons()) {
 			toolBarManager = PDEFormToolkit.createSectionToolBarManager(section);
 			Action addAction = createCustomToolbarAddAction();
 			if (addAction != null) {
 				toolBarManager.add(addAction);
 			}
 
 			if (removeAction != null) {
 				toolBarManager.add(removeAction);
 			}
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
 
 			IValueProperty p = JFaceProperties.value(IAction.class, IAction.ENABLED, IAction.ENABLED);
 			bindingContext.bindValue(SWTObservables.observeEnabled(getRemoveButton()), p.observe(removeAction));
 
 			//Generic action for remove button
 			getRemoveButton().addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					if (removeAction != null)
 						removeAction.run();
 				}
 			});
 		}
 
 		createContextMenuFor(treeViewer);
 
 		//update Editor selection
 		getEditor().addViewerToListenTo(getTreeViewer());
 
 		section.setClient(client);
 	}
 
 	/**
 	 * Default styles : SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
 	 * 
 	 * @return int, style used to create the TreeViewer
 	 */
 	protected int getViewerOptions() {
 		return DEFAULT_VIEWER_OPTIONS;
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
 
 	/**
 	 * Create the Action to be performed when a deletion should be performed.
 	 * Default implementation create a {@link RemoveAction} that delegates the job to the DELETE action
 	 * registered through the {@link EmfActionBarContributor} of the current {@link EmfFormEditor} 
 	 * Subclasses may override this method in order to provide their own Action
 	 * @return Action the delete action to perform 
 	 */
 	protected Action createCustomToolbarRemoveAction() {
 		return new RemoveAction(this);
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
 	protected ViewerFilter getTreeFilter() {
 		return new ViewerFilter() {
 			@Override
 			public boolean select(Viewer viewer, Object parentElement, Object element) {
 				return true;
 			}
 		};
 	}
 
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
