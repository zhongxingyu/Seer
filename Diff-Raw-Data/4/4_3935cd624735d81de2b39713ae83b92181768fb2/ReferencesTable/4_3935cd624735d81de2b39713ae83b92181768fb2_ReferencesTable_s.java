 /*****************************************************************************
  * Copyright (c) 2008-2009 CEA LIST, Obeo.
  *
  *    
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
  *  Obeo
  *
  *****************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.eef.runtime.EMFPropertiesRuntime;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 
 /**
  * Base class for a complex section composite. This composite has a label, a table that describes a tree
  * structure, and four buttons on the side of the table to add an element into the table, remove selected
  * element(s), move up or down the selected element.
  * 
  * @author Remi SCHNEKENBURGER
  * @author Patrick Tessier
  * @author <a href="mailto:jerome.benois@obeo.fr">Jerome Benois</a>
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
  */
 public class ReferencesTable<T extends EObject> implements IPropertiesFilteredWidget {
 
 	/**
 	 * Image for the add element button.
 	 */
 	final protected static Image NEW_ELEMENT_IMG = EMFPropertiesRuntime
 			.getImage(EMFPropertiesRuntime.ICONS_16x16 + "Add_16x16.gif");
 
 	/**
 	 * Image for the delete element button.
 	 */
 	final protected static Image DELETE_ELEMENT_IMG = EMFPropertiesRuntime
 			.getImage(EMFPropertiesRuntime.ICONS_16x16 + "Delete_16x16.gif");
 
 	/**
 	 * Image for the up button.
 	 */
 	final protected static Image UP_ELEMENT_IMG = EMFPropertiesRuntime
 			.getImage(EMFPropertiesRuntime.ICONS_16x16 + "ArrowUp_16x16.gif");
 
 	/**
 	 * Image for the down button.
 	 */
 	final protected static Image DOWN_ELEMENT_IMG = EMFPropertiesRuntime
 			.getImage(EMFPropertiesRuntime.ICONS_16x16 + "ArrowDown_16x16.gif");
 
 	/** list of element that we want to display * */
 	private List<T> listElement;
 
 	/**
 	 * Label above the table.
 	 */
 	private Label label;
 
 	/**
 	 * Table that displays a property for the current element.
 	 */
 	private Table table;
 
 	/** the table viewer to associate the label provider * */
 	private TableViewer tableViewer;
 
 	/**
 	 * Button that adds an element.
 	 */
 	private Button addButton;
 
 	/**
 	 * Button that removes an element.
 	 */
 	private Button removeButton;
 
 	/**
 	 * button that moves the element up.
 	 */
 	private Button upButton;
 
 	/**
 	 * button that moves the element down.
 	 */
 	private Button downButton;
 
 	/**
 	 * Listener for the add button.
 	 */
 	private MouseListener addButtonlistener;
 
 	/**
 	 * Listener for the delete button.
 	 */
 	private MouseListener removeButtonlistener;
 
 	/**
 	 * Listener for the up button.
 	 */
 	private MouseListener upButtonlistener;
 
 	/**
 	 * Listener for the down button.
 	 */
 	private MouseListener downButtonlistener;
 
 	private Listener tableListener;
 
 	/**
 	 * The listener used by the client to handle business events (Add, Remove, Move, NavigateTo)
 	 */
 	private ReferencesTableListener<T> referencesTableListener;
 
 	private String labelToDisplay;
 
 	/**
 	 * The Form tool kit use to use this widget in an Eclipse Forms compliant mode
 	 */
 	private FormToolkit widgetFactory;
 
 	/**
 	 * The main composite
 	 */
 	private Composite composite;
 
 	/**
 	 * The adapter factory.
 	 */
 	protected AdapterFactory adapterFactory = new ComposedAdapterFactory(
 			ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 	/**
 	 * The help text
 	 */
 	private String helpText;
 
 	/** The business rules filters. */
 	protected List<ViewerFilter> bpFilters;
 
 	/** The filters. */
 	protected List<ViewerFilter> filters;
 
 	public void setHelpText(String helpText) {
 		this.helpText = helpText;
 	}
 
 	/**
 	 * the constructor
 	 * 
 	 * @param labeltoDisplay
 	 *            the label to display
 	 * @param the
 	 *            listener to handle Add, Remove, Move and NavigateTo events
 	 */
 	public ReferencesTable(String labeltoDisplay, ReferencesTableListener<T> referenceListener) {
 		this.labelToDisplay = labeltoDisplay;
 		this.addButtonlistener = new AddButtonlistener();
 		this.removeButtonlistener = new RemoveButtonlistener();
 		this.upButtonlistener = new UpButtonlistener();
 		this.downButtonlistener = new DownButtonlistener();
 		bpFilters = new ArrayList<ViewerFilter>();
 		filters = new ArrayList<ViewerFilter>();
 		addTableReferenceListener(referenceListener);
 	}
 
 	public void addTableReferenceListener(ReferencesTableListener<T> referenceListener) {
 		this.referencesTableListener = referenceListener;
 	}
 
 	public void createControls(Composite parent, FormToolkit widgetFactory) {
 		this.widgetFactory = widgetFactory;
 		createControls(parent);
 	}
 
 	private Composite createComposite(Composite parent) {
 		Composite composite;
 		if (widgetFactory == null) {
 			composite = new Composite(parent, SWT.NONE);
 		} else {
 			composite = widgetFactory.createComposite(parent);
 		}
 		return composite;
 	}
 
 	private Button createButton(Composite parent, String text, int style) {
 		Button button;
 		if (widgetFactory == null) {
 			button = new Button(parent, style);
 			button.setText(text);
 		} else {
 			button = widgetFactory.createButton(parent, text, style);
 		}
 		return button;
 	}
 
 	private Label createLabel(Composite parent, String text, int style) {
 		Label label;
 		if (widgetFactory == null) {
 			label = new Label(parent, SWT.PUSH);
 			label.setText(text);
 		} else {
 			label = widgetFactory.createLabel(parent, text, style);
 		}
 		return label;
 	}
 
 	private Table createTable(Composite parent, int style) {
 		Table table;
 		if (widgetFactory == null) {
 			table = new Table(parent, style);
 		} else {
 			table = widgetFactory.createTable(parent, style);
 		}
 		return table;
 	}
 
 	public void createControls(Composite parent) {
 		composite = createComposite(parent);
 		if (parent instanceof ExpandableComposite) {
 			((ExpandableComposite)parent).setClient(composite);
 		}
 		FormLayout formLayout = new FormLayout();
 		formLayout.marginTop = 7;
 		composite.setLayout(formLayout);
 
 		FormData data;
 
 		// Create Help Button
 		data = new FormData();
 		data.top = new FormAttachment(-2, 0);
 		data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
 		Control helpButton = null;
 		if (helpText != null) {
 			if (widgetFactory != null) {
 				helpButton = FormUtils.createHelpButton(widgetFactory, composite, helpText, null); //$NON-NLS-1$
 			} else {
 				helpButton = SWTUtils.createHelpButton(composite, helpText, null); //$NON-NLS-1$
 			}
 			helpButton.setLayoutData(data);
 		}
 
 		// ///////////////////////////////////////////////////////////////////////////
 		// Create and place button vertically on the left side
 		// Button : Add Element
 		// Button Delete Element
 		removeButton = createButton(composite, "", SWT.PUSH);
 		removeButton.setVisible(true);
 		removeButton.setImage(DELETE_ELEMENT_IMG);
 		removeButton.setToolTipText("Delete selected element(s)");
 		data = new FormData();
 		// data.top = new FormAttachment(addButton,
 		// ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(-6, 0);
 		if (helpText != null) {
 			data.right = new FormAttachment(helpButton, -ITabbedPropertyConstants.HSPACE);
 		} else {
 			data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
 		}
 		removeButton.setLayoutData(data);
 		removeButton.addMouseListener(removeButtonlistener);
 
 		addButton = createButton(composite, "", SWT.PUSH);
 		addButton.setVisible(true);
 		addButton.setImage(NEW_ELEMENT_IMG);
 		addButton.setToolTipText("Add a new element");
 
 		data = new FormData();
 		// data.top = new FormAttachment(label,
 		// ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(-6, 0);
 		data.right = new FormAttachment(removeButton, -ITabbedPropertyConstants.HSPACE);
 		addButton.setLayoutData(data);
 		addButton.addMouseListener(addButtonlistener);
 
 		// Button Up
 		upButton = createButton(composite, "", SWT.PUSH);
 		upButton.setVisible(true);
 		upButton.setImage(UP_ELEMENT_IMG);
 		upButton.setToolTipText("Up");
 
 		data = new FormData();
 		// data.top = new FormAttachment(removeButton,
 		// ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(-6, 0);
 		data.right = new FormAttachment(addButton, -ITabbedPropertyConstants.HSPACE);
 		upButton.setLayoutData(data);
 		upButton.addMouseListener(upButtonlistener);
 
 		// Button Down
 		downButton = createButton(composite, "", SWT.PUSH);
 		downButton.setVisible(true);
 		downButton.setImage(DOWN_ELEMENT_IMG);
 		downButton.setToolTipText("Down");
 
 		data = new FormData();
 		// data.top = new FormAttachment(upButton,
 		// ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(-6, 0);
 		data.right = new FormAttachment(upButton, -ITabbedPropertyConstants.HSPACE);
 		downButton.setLayoutData(data);
 		downButton.addMouseListener(downButtonlistener);
 
 		// Create label
 		label = createLabel(composite, labelToDisplay, SWT.NONE);
 		// label.setLayout(new FormLayout());
 		data = new FormData();
 		data.left = new FormAttachment(2, 0);
 		data.right = new FormAttachment(downButton, -ITabbedPropertyConstants.HSPACE - 5/* 50 */);
 		data.top = new FormAttachment(0, 0);
 		label.setLayoutData(data);
 
 		// ///////////////////////////////////////////////////////////////////////////
 		// Create and place Table
 		table = createTable(composite, SWT.MULTI | SWT.H_SCROLL | SWT.BORDER);
 		table.setLayout(new FormLayout());
 		table.setVisible(true);
 		table.addListener(SWT.MouseDoubleClick, tableListener = new EditItemListener());
 		// createTable
 		tableViewer = new TableViewer(table);
 
 		data = new FormData();
 		data.height = 100;
 		data.top = new FormAttachment(label, ITabbedPropertyConstants.VSPACE + 4);
 		data.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE);
 		data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
 
 		table.setLayoutData(data);
 		table.addMouseListener(new MouseListener() {
 
 			@SuppressWarnings("unchecked")
 			public void mouseDoubleClick(MouseEvent e) {
				if (table.getSelection() != null && table.getSelection()[0].getData() instanceof EObject) {
 					// Navigate
 					referencesTableListener.navigateTo((T)table.getSelection()[0].getData());
 				}
 			}
 
 			public void mouseDown(MouseEvent e) {
 			}
 
 			public void mouseUp(MouseEvent e) {
 			}
 		});
 		// tableViewer.refresh();
 		// table.pack();
 	}
 
 	/**
 	 * @param layoutData
 	 *            the layoutData to set
 	 */
 	public void setLayoutData(Object layoutData) {
 		composite.setLayoutData(layoutData);
 	}
 
 	public void refresh() {
 		tableViewer.refresh();
 	}
 
 	/**
 	 * display the content of the table
 	 */
 	public void initLabelProvider() {
 		if (!table.isDisposed()) {
 			// set the label provider
 			tableViewer.setLabelProvider(getLabelProvider());
 		}
 	}
 
 	/**
 	 * Sets the layout data to the main composite of this complex element.
 	 * 
 	 * @param data
 	 *            the new LayoutData
 	 */
 	// public void setLayoutData(Object data) {
 	// composite.setLayoutData(data);
 	// }
 	/**
 	 * Returns the label provider for the composite
 	 * 
 	 * @return the label provider or <code>null</code>
 	 */
 	public AdapterFactoryLabelProvider getLabelProvider() {
 		return new AdapterFactoryLabelProvider(adapterFactory);
 	}
 
 	/**
 	 * Returns the label provider for the composite
 	 * 
 	 * @return the label provider or <code>null</code>
 	 */
 	public IContentProvider getContentProvider() {
 		return new TableContentProvider();
 
 	}
 
 	/**
 	 * Disable Move capability (Hide Up and Down buttons)
 	 */
 	public void disableMove() {
 		upButton.setVisible(false);
 		downButton.setVisible(false);
 	}
 
 	/**
 	 * Listener for the Add Button Specific behavior is implemented in
 	 * {@link ReferencesTable#addButtonPressed()}.
 	 * 
 	 * @author Remi SCHNEKENBURGER
 	 */
 	private class AddButtonlistener implements MouseListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDoubleClick(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDown(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseUp(MouseEvent e) {
 			referencesTableListener.handleAdd();
 
 		}
 	}
 
 	/**
 	 * Listener for the Remove Button Specific behavior is implemented in
 	 * {@link ReferencesTable#removeButtonPressed()}.
 	 * 
 	 * @author Remi SCHNEKENBURGER
 	 */
 	private class RemoveButtonlistener implements MouseListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDoubleClick(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDown(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@SuppressWarnings("unchecked")
 		public void mouseUp(MouseEvent e) {
 			// Keep selection
 			TableItem[] tableItems = table.getSelection();
 
 			for (int i = (tableItems.length - 1); i >= 0; i--) {
 				// Remove
 				referencesTableListener.handleRemove((T)tableItems[i].getData());
 			}
 		}
 	}
 
 	/**
 	 * Listener for the Up Button Specific behavior is implemented in
 	 * {@link ReferencesTable#upButtonPressed()}.
 	 * 
 	 * @author Remi SCHNEKENBURGER
 	 */
 	private class UpButtonlistener implements MouseListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDoubleClick(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDown(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@SuppressWarnings("unchecked")
 		public void mouseUp(MouseEvent e) {
 			// Keep selection
 			TableItem[] tableItems = table.getSelection();
 
 			for (int i = (tableItems.length - 1); i >= 0; i--) {
 				// Get use case
 
 				int newIndex = listElement.indexOf(tableItems[i].getData()) - 1;
 				if (newIndex >= 0 && newIndex < listElement.size()) {
 					// Move
 					referencesTableListener.handleMove((T)tableItems[i].getData(), newIndex + 1, newIndex);
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Listener for the Down Button Specific behavior is implemented in
 	 * {@link ReferencesTable#downButtonPressed()}.
 	 * 
 	 * @author Remi SCHNEKENBURGER
 	 */
 	private class DownButtonlistener implements MouseListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDoubleClick(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void mouseDown(MouseEvent e) {
 			// do nothing
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@SuppressWarnings("unchecked")
 		public void mouseUp(MouseEvent e) {
 			TableItem[] tableItems = table.getSelection();
 			for (int i = (tableItems.length - 1); i >= 0; i--) {
 				// Get use case
 				int newIndex = listElement.indexOf(tableItems[i].getData()) + 1;
 				if (newIndex >= 0 && newIndex < listElement.size()) {
 					// Move
 					referencesTableListener.handleMove((T)tableItems[i].getData(), newIndex - 1, newIndex);
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private class EditItemListener implements Listener {
 
 		/** @{inheritDoc */
 		@SuppressWarnings("unchecked")
 		public void handleEvent(Event event) {
 			if (table.getSelection().length > 0) {
 				TableItem item = table.getSelection()[0];
 				// Edit
 				referencesTableListener.handleEdit((T)item.getData());
 			}
 		}
 	}
 
 	/**
 	 * removes listeners from buttons.
 	 */
 	public void dispose() {
 		if (addButton != null && !addButton.isDisposed())
 			addButton.removeMouseListener(addButtonlistener);
 		if (removeButton != null && !removeButton.isDisposed())
 			removeButton.removeMouseListener(removeButtonlistener);
 		if (upButton != null && !upButton.isDisposed())
 			upButton.removeMouseListener(upButtonlistener);
 		if (downButton != null && !downButton.isDisposed())
 			downButton.removeMouseListener(downButtonlistener);
 		if (table != null && !table.isDisposed())
 			table.removeListener(SWT.MouseDoubleClick, tableListener);
 		if (filters != null) {
 			filters.clear();
 			filters = null;
 		}
 		if (bpFilters != null) {
 			bpFilters.clear();
 			bpFilters = null;
 		}
 	}
 
 	/**
 	 * get the list of element to display
 	 * 
 	 * @return the list of element
 	 */
 	public List<T> getListElement() {
 		return listElement;
 	}
 
 	/**
 	 * set list of element to display
 	 * 
 	 * @param listElement
 	 */
 	public void setInput(List<T> listElement) {
 		this.listElement = listElement;
 		initLabelProvider();
 		tableViewer.setContentProvider(getContentProvider());
 		tableViewer.setInput(listElement);
 		for (ViewerFilter filter : filters) {
 			this.tableViewer.addFilter(filter);
 		}
 		for (ViewerFilter filter : bpFilters) {
 			this.tableViewer.addFilter(filter);
 		}
 
 	}
 
 	/**
 	 * this is the content provider to display the list of element
 	 * 
 	 * @author Patrick Tessier
 	 */
 	class TableContentProvider implements IStructuredContentProvider {
 
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 
 		public void dispose() {
 		}
 
 		public Object[] getElements(Object inputElement) {
 			return listElement.toArray();
 		}
 	}
 
 	public interface ReferencesTableListener<T extends EObject> {
 
 		void handleAdd();
 
 		void handleRemove(T element);
 
 		void handleMove(T element, int oldIndex, int newIndex);
 
 		void handleEdit(T element);
 
 		void navigateTo(T element);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#addBusinessRuleFilter(org.eclipse.
 	 * jface.viewers.ViewerFilter)
 	 */
 	public void addBusinessRuleFilter(ViewerFilter filter) {
 		this.bpFilters.add(filter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#addFilter(org.eclipse.jface.viewers
 	 * .ViewerFilter)
 	 */
 	public void addFilter(ViewerFilter filter) {
 		this.filters.add(filter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#removeBusinessRuleFilter(org.eclipse
 	 * .jface.viewers.ViewerFilter)
 	 */
 	public void removeBusinessRuleFilter(ViewerFilter filter) {
 		this.bpFilters.remove(filter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see
 	 * org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#removeFilter(org.eclipse.jface.viewers
 	 * .ViewerFilter)
 	 */
 	public void removeFilter(ViewerFilter filter) {
 		this.filters.remove(filter);
 	}
 
 	protected void refreshFilters() {
 	}
 
 	/**
 	 * Sets the tables readonly or not
 	 * 
 	 * @param enabled
 	 *            to set the table readonly or not
 	 */
 	public void setEnabled(boolean enabled) {
 		addButton.setEnabled(enabled);
 		downButton.setEnabled(enabled);
 		removeButton.setEnabled(enabled);
 		table.setEnabled(enabled);
 		upButton.setEnabled(enabled);
 	}
 
 	/**
 	 * Sets the tooltip text for the viewer
 	 * 
 	 * @param tooltip
 	 *            the tooltip text
 	 */
 	public void setToolTipText(String tooltip) {
 		addButton.setToolTipText(tooltip);
 		downButton.setToolTipText(tooltip);
 		removeButton.setToolTipText(tooltip);
 		table.setToolTipText(tooltip);
 		upButton.setToolTipText(tooltip);
 	}
 
 }
