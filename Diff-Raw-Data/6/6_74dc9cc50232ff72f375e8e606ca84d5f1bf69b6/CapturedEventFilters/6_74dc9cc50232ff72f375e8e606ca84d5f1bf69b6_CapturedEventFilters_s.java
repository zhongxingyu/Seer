 /*******************************************************************************
  * Copyright (c) 2013 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.e4.tools.event.spy.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.HashMap;
 
 import org.eclipse.e4.tools.event.spy.model.CapturedEventFilter;
 import org.eclipse.e4.tools.event.spy.model.ItemToFilter;
 import org.eclipse.e4.tools.event.spy.model.Operator;
 import org.eclipse.e4.tools.event.spy.util.CapturedEventFilterSerializer;
 import org.eclipse.e4.ui.workbench.UIEvents;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolTip;
 
 public class CapturedEventFilters {
 	private final static String NOT_SELECTED_VALUE = "-- expected value --";
 	
 	public final static String BASE_EVENT_TOPIC = UIEvents.UITopicBase + UIEvents.TOPIC_SEP + UIEvents.ALL_SUB_TOPICS;
 	
 	private final Composite control;
 
 	private Text valueText;
 
 	private Combo itemToFilterCombo;
 
 	private Combo operatorCombo;
 
 	private ToolTip validationErrorToolTip;
 
 	private Text baseTopicText;
 	
 	private List filters;
 
 	private java.util.List<CapturedEventFilter> rawFilters;
 	
 	private final Clipboard clipboard;
 
 
 	/* Layout scheme:
 	 *
 	 * +-- control --------------------------------------------+
 	 * | +-- New filter group -------------------------------+ |
 	 * | |                                                   | |
 	 * | | Base topic of... |text|reset to default           | |
 	 * | |                                                   | |	   	
 	 * | | Capture event when|combo|combo|text|add filter    | |
 	 * | |                                                   | |
 	 * | +---------------------------------------------------+ |
 	 * | +-- Defined filter group ---------------------------+ |
 	 * | |                                                   | |
 	 * | | List   |   +-- composite -----------------+       | |
 	 * | |        |   | remove selected | remove all |       | |
 	 * | |        |   +------------------------------+       | |
 	 * | |                                                   | |
 	 * | +---------------------------------------------------+ |
 	 * +-------------------------------------------------------+
 	 *
 	 * */
 
 	//TODO: Fix layout data for groups
 	public CapturedEventFilters(Composite outer) {
 		control = new Composite(outer, SWT.NONE);
 		RowLayout layout = new RowLayout(SWT.VERTICAL);
 		layout.marginLeft = 0;
 		layout.fill = true;
 		control.setLayout(layout);
 		rawFilters = new ArrayList<CapturedEventFilter>();
 		
 		clipboard = new Clipboard(control.getDisplay());
 		control.addDisposeListener(new DisposeListener() {			
 			public void widgetDisposed(DisposeEvent e) {
 				if (clipboard != null && !clipboard.isDisposed()) {
 					clipboard.dispose();
 				}
 			}
 		});
 		
 		createNewFilterGroup(control);
 		createDefinedFiltersGroup(control);
 	}
 
 	private void createNewFilterGroup(Composite parent) {
 		Group newFilterGroup = new Group(parent, SWT.NONE);
 		newFilterGroup.setText("New filter:");
 		newFilterGroup.setLayout(new RowLayout(SWT.VERTICAL));
 		
 		createBaseTopicSection(newFilterGroup);
 		createAddFilterSection(newFilterGroup);
 	}
 
 	private void createBaseTopicSection(Composite parent) {
 		parent = new Composite(parent, SWT.NONE);
 		parent.setLayout(new RowLayout(SWT.HORIZONTAL));
 		
 		Label label = new Label(parent, SWT.CENTER);
 		label.setText("Base topic of captured events:");
 		
 		baseTopicText = new Text(parent, SWT.BORDER);
 		baseTopicText.setLayoutData(new RowData(312, SWT.DEFAULT));
 		baseTopicText.setText(BASE_EVENT_TOPIC);
 		baseTopicText.addFocusListener(new FocusAdapter() {
 			public void focusLost(FocusEvent e) {
 				if (baseTopicText.getText().trim().length() == 0) {
 					baseTopicText.setText(BASE_EVENT_TOPIC);
 				}
 			}			
 		});
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
 		
 		Link link = new Link(composite, SWT.NONE);
 		link.setText("<a>Reset to default</a>");
 		link.addListener (SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				baseTopicText.setText(BASE_EVENT_TOPIC);
 			}
 		});
 	}
 	
 	private void createAddFilterSection(Composite parent) {
 		parent = new Composite(parent, SWT.NONE);
 		parent.setLayout(new RowLayout(SWT.HORIZONTAL));
 		
 		Label label = new Label(parent, SWT.CENTER);
 		label.setText("Capture event when:");
 
 		itemToFilterCombo = new Combo(parent, SWT.READ_ONLY);
 		for (ItemToFilter item: ItemToFilter.values()) {
 			itemToFilterCombo.add(item.toString());
 		}
 		itemToFilterCombo.select(0);
 
 		operatorCombo = new Combo(parent, SWT.READ_ONLY);
 		for (Operator operator: Operator.values()) {
 			operatorCombo.add(operator.toString());
 		}
 		operatorCombo.select(0);
 
 		valueText = new Text(parent, SWT.BORDER);
 		valueText.setLayoutData(new RowData(130, SWT.DEFAULT));
 		valueText.setText(NOT_SELECTED_VALUE);
 		valueText.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
 				if (valueText.getText().trim().length() == 0) {
 					valueText.setText(NOT_SELECTED_VALUE);
 				}
 			}
 			public void focusGained(FocusEvent e) {
 				if (NOT_SELECTED_VALUE.equals(valueText.getText())) {
 					valueText.setText("");
 				}
 			}
 		});
 
 		Link link = new Link(parent, SWT.NONE);
 		link.setText("<a>Add filter</a>");
 		link.addListener (SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				addFilter();
 			}
 		});
 	}
 	
 	private void createDefinedFiltersGroup(Composite parent) {
 		Group definedFiltersGroup = new Group(parent, SWT.NONE);
 		definedFiltersGroup.setText("Defined filters (relation between filters is AND):");
 		definedFiltersGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
 
 		filters = new List(definedFiltersGroup, SWT.BORDER | SWT.V_SCROLL);
 		filters.setLayoutData(new RowData(450, 84));
 		filters.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				selectFilterAt(((List) e.widget).getSelectionIndex());
 			}			
 		});
 		filters.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				int index = ((List) e.widget).getSelectionIndex();
 				boolean ctrlPressed = (e.stateMask & SWT.CTRL) == SWT.CTRL; 
 				if (ctrlPressed && e.keyCode == 'c') {
 					copyFilterAt(index);
 					e.doit = false;
 				} else if (ctrlPressed && e.keyCode == 'v') {
 					pasteFilterAt(index);
 				}
 			}
 		});
 
 		Composite composite = new Composite(definedFiltersGroup, SWT.NONE);
 		composite.setLayout(new RowLayout(SWT.VERTICAL));
 
 		Link link = new Link(composite, SWT.NONE);
 		link.setText("<a>Update selected</a>");
 		link.addListener (SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				updateFilterAt(filters.getSelectionIndex());
 			}
 		});
 		
 		link = new Link(composite, SWT.NONE);
 		link.setText("<a>Remove selected</a>");
 		link.addListener (SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				removeFilterAt(filters.getSelectionIndex());
 			}
 		});
 
 		link = new Link(composite, SWT.NONE);
 		link.setText("<a>Remove all</a>");
 		link.addListener (SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				removeAllFilters();
 			}
 		});
 	}
 
 	public Control getControl() {
 		return control;
 	}
 
 	public void setFilters(Collection<CapturedEventFilter> filters) {
 		if (filters != null) {
 			for (CapturedEventFilter filter: filters) {
 				this.filters.add(filter.toString());
 				rawFilters.add(filter);				
 			}
 		}
 	}
 	
 	public Collection<CapturedEventFilter> getFilters() {
		HashMap<Integer, CapturedEventFilter> result = new HashMap<Integer, CapturedEventFilter>();
 		for (CapturedEventFilter filter: rawFilters) {
 			result.put(filter.hashCode(), filter);
 		}
 		return result.values();
 	}
 
 	public void setBaseTopic(String baseTopic) {
 		if (baseTopic != null) {
 			baseTopicText.setText(baseTopic);
 		}
 	}
 	
 	public String getBaseTopic() {
 		return baseTopicText.getText();
 	}
 	
 	public boolean hasFilters() {
 		return !rawFilters.isEmpty();
 	}
 
 	public int getFiltersCount() {
 		return rawFilters.size();
 	}
 
 	private CapturedEventFilter createFilter() {
 		ItemToFilter selectedItemToFilter =
 				ItemToFilter.toItem(itemToFilterCombo.getItem(itemToFilterCombo.getSelectionIndex()));
 		if (ItemToFilter.NotSelected.equals(selectedItemToFilter)) {
 			getTooltip().setText(String.format("%s is not selected", getFieldName(ItemToFilter.NotSelected)));
 			getTooltip().setVisible(true);
 			return null;
 		}
 
 		Operator selectedOperator = Operator.toOperator(operatorCombo.getItem(operatorCombo.getSelectionIndex()));
 		if (Operator.NotSelected.equals(selectedOperator)) {
 			getTooltip().setText(String.format("%s is not selected", getFieldName(Operator.NotSelected)));
 			getTooltip().setVisible(true);
 			return null;
 		}
 
 		String value = valueText.getText();
 		if (value.length() == 0 || value.equals(NOT_SELECTED_VALUE)) {
 			getTooltip().setText(String.format("%s is empty", getFieldName(NOT_SELECTED_VALUE)));
 			getTooltip().setVisible(true);
 			return null;
 		}
 
 		try {
 			return new CapturedEventFilter(selectedItemToFilter, selectedOperator, value);
 		} catch(IllegalArgumentException exc) {
 			getTooltip().setText(exc.getMessage());
 			getTooltip().setVisible(true);				
 		}
 		return null;
 	}
 	
 	private ToolTip getTooltip() {
 		if (validationErrorToolTip == null) {
 			validationErrorToolTip = new ToolTip(Display.getCurrent().getActiveShell(), SWT.BALLOON | SWT.ICON_WARNING);
 		}
 		return validationErrorToolTip;
 	}
 
 	private void addFilter() {
 		addFilterAt(-1);
 	}
 	
 	private void addFilterAt(int index) {
 		CapturedEventFilter eventFilter = createFilter();
 		String filterAsString = eventFilter.toString();
 
 		if (index  > -1 && index < rawFilters.size()) {
 			filters.add(filterAsString, index);
 			rawFilters.add(index, eventFilter);			
 		} else {
 			filters.add(filterAsString);
 			rawFilters.add(eventFilter);
 		}
 
 		itemToFilterCombo.select(0);
 		operatorCombo.select(0);
 		valueText.setText(NOT_SELECTED_VALUE);
 	}
 
 	private void removeFilterAt(int index) {
 		if (index < 0) {
 			getTooltip().setText("Filter to remove is not selected");
 			getTooltip().setVisible(true);
 			return;
 		}
 
 		filters.remove(index);
 		rawFilters.remove(index);
 	}
 
 	private void selectFilterAt(int index) {
 		CapturedEventFilter filter = rawFilters.get(index);
 		itemToFilterCombo.setText(filter.getItemToFilter().toString());
 		operatorCombo.setText(filter.getOperator().toString());
 		valueText.setText(filter.getValue());
 	}
 	
 	private void updateFilterAt(int index) {
 		CapturedEventFilter newFilter = createFilter();
 		
 		CapturedEventFilter filter = rawFilters.get(index);
 		filter.setItemToFilter(newFilter.getItemToFilter());
 		filter.setOperator(newFilter.getOperator());
 		filter.setValue(newFilter.getValue());
 		
 		filters.setItem(index, newFilter.toString());
 	}
 	
 	private void copyFilterAt(int index) {
 		clipboard.setContents(new Object[] {CapturedEventFilterSerializer
 			.serialize(rawFilters.get(index))}, new Transfer[] {TextTransfer.getInstance()});
 	}
 	
 	private void pasteFilterAt(int index) {
 		String pastedFilter = (String) clipboard.getContents(TextTransfer.getInstance());
 		CapturedEventFilter filter = CapturedEventFilterSerializer.deserialize(pastedFilter);
 		if (filter != null && index > -1 && index < filters.getItemCount()) {
 			filters.add(filter.toString(), index);
 			rawFilters.add(index, filter);
 		} else if (filter != null) {
 			filters.add(filter.toString());
 			rawFilters.add(filter);
 		}			
 	}
 	
 	private void removeAllFilters() {
 		if (rawFilters == null || rawFilters.isEmpty()) {
 			getTooltip().setText("Filter list is empty");
 			getTooltip().setVisible(true);
 			return;
 		}
 		filters.removeAll();
 		rawFilters.clear();
 	}
 
 	private String getFieldName(Object notSelectedName) {
 		String fieldName = notSelectedName.toString().replaceAll("-", "").trim();
 		return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
 	}
 }
