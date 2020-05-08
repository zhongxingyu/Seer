 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyDescriptor;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.observable.list.IListChangeListener;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.ListChangeEvent;
 import org.eclipse.core.databinding.observable.list.ListDiffEntry;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.jface.databinding.viewers.IViewerObservableList;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.viewers.AbstractTableViewer;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnPixelData;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.IBaseLabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.ViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Scrollable;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.ui.common.ISortableByColumn;
 import org.eclipse.riena.ui.core.marker.RowErrorMessageMarker;
 import org.eclipse.riena.ui.ridgets.IColumnFormatter;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITableFormatter;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTWidgetRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSelectableIndexedRidget;
 import org.eclipse.riena.ui.ridgets.swt.ColumnFormatter;
 
 /**
  * Abstract Ridget of a table widget.
  */
 public abstract class AbstractTableRidget extends AbstractSelectableIndexedRidget implements ITableRidget {
 
 	private final static Logger LOGGER = Log4r.getLogger(AbstractTableRidget.class);
 	private final static String TABLE_VIEWER_DATA_KEY = "TBL.VIEWER"; //$NON-NLS-1$
 
 	private AbstractTableViewer viewer;
 	private ColumnLayoutData[] columnWidths;
 	private ITableTreeWrapper tableWrapper;
 	protected final SelectionListener selectionTypeEnforcer;
 	protected SelectionListener sortListener;
 	protected String[] columnHeaders;
 	protected DataBindingContext dbc;
 	protected String[] renderingMethods;
 	private boolean viewConfigured;
 	private boolean nativeToolTip = true;
 	private boolean isSortedAscending;
 	private int sortedColumn;
 	private ITableFormatter tableFormatter;
 	private final Map<Integer, Boolean> sortableColumnsMap;
 	private final Map<Integer, Boolean> editableColumnsMap;
 	private final Map<Integer, Comparator<?>> comparatorMap;
 	private final Map<Integer, IColumnFormatter> formatterMap;
 	private final TableKeyListener keyListener;
 
 	/*
 	 * Binds the viewer's multiple selection to the multiple selection observable. This binding has to be disposed when the ridget is set to output-only, to
 	 * avoid updating the model. It has to be recreated when the ridget is set to not-output-only.
 	 */
 	private Binding viewerMSB;
 	/*
 	 * Data we received in bindToModel(...). May change without our doing.
 	 */
 	private IObservableList modelObservables;
 	/*
 	 * Data the viewer is bound to. It is updated from modelObservables on updateFromModel().
 	 */
 	protected IObservableList viewerObservables;
 	private Class<?> rowClass;
 	private boolean moveableColumns;
 	private StructuredViewerFilterHolder filterHolder;
 
 	/**
 	 * @return the viewConfigured
 	 */
 	protected boolean isViewConfigured() {
 		return viewConfigured;
 	}
 
 	public AbstractTableRidget() {
 		isSortedAscending = true;
 		sortedColumn = -1;
 		// maps
 		formatterMap = new HashMap<Integer, IColumnFormatter>();
 		sortableColumnsMap = new HashMap<Integer, Boolean>();
 		editableColumnsMap = new HashMap<Integer, Boolean>();
 		comparatorMap = new HashMap<Integer, Comparator<?>>();
 		// listener
 		keyListener = new TableKeyListener();
 		selectionTypeEnforcer = new SelectionTypeEnforcer();
 		addPropertyChangeListener(IMarkableRidget.PROPERTY_OUTPUT_ONLY, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				if (isOutputOnly()) {
 					disposeMultipleSelectionBinding();
 				} else {
 					createMultipleSelectionBinding();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Returns the number of columns in the table widget.
 	 * 
 	 * @return number of columns; -1 if the number of columns can't be determined
 	 */
 	public int getColumnCount() {
 		if (tableWrapper == null) {
 			return -1;
 		}
 		return tableWrapper.getColumnCount();
 	}
 
 	protected int getExpectedColumnCount() {
 		if (renderingMethods == null) {
 			return 0;
 		}
 		return renderingMethods.length;
 	}
 
 	/**
 	 * Returns the number of items contained in the table widget.
 	 * 
 	 * @return number of items
 	 */
 	private int getItemCount() {
 		if (tableWrapper == null) {
 			return -1;
 		}
 		return tableWrapper.getItemCount();
 	}
 
 	/**
 	 * Returns the item at the given point in the table or null if no such item exists. The point is in the coordinate system of the table.
 	 * 
 	 * @param point
 	 *            the point used to locate the item
 	 * @return the item at the given point
 	 */
 	protected final Item getItem(final Point point) {
 		if (tableWrapper == null) {
 			return null;
 		}
 		return tableWrapper.getItem(point);
 	}
 
 	/**
 	 * Returns the number of selected items contained in the table widget.
 	 * 
 	 * @return the number of selected items
 	 */
 	protected abstract int getUiSelectionCount();
 
 	/**
 	 * Sets the table widget's selection to the given item.
 	 * 
 	 * @param item
 	 *            the item to select
 	 */
 	protected abstract void setUiSelection(Widget item);
 
 	// protected abstract void setUiSelection(int selectionIndex);
 
 	// protected abstract int getUiSelectionIndex();
 
 	/**
 	 * Creates columns for every given column property. (see: {@link #bindToModel(Object, String, Class, String[], String[])}).
 	 */
 	protected abstract void applyColumns();
 
 	/**
 	 * Sets the text of the column headers.
 	 */
 	protected abstract void applyTableColumnHeaders();
 
 	/**
 	 * Sets whether the columns are movable or not.
 	 */
 	protected abstract void applyColumnsMovable();
 
 	/**
 	 * Sets the comparator for the column that is sorted. Form the other columns the comparator is removed.
 	 * 
 	 * @param comparatorMap
 	 *            map with all comparators of every column that has a comparator.
 	 */
 	protected abstract void applyComparator(final Map<Integer, Comparator<?>> comparatorMap);
 
 	/**
 	 * Adds a support for tool tips to display tool tips for single cell, error marker texts etc..
 	 */
 	protected abstract void updateToolTipSupport();
 
 	/**
 	 * Creates a JFace viewer for the table control.
 	 * 
 	 * @return table viewer
 	 */
 	protected abstract AbstractTableViewer createTableViewer();
 
 	protected abstract ITableTreeWrapper createTableWrapper();
 
 	protected abstract int getColumnStyle(int columnIndex);
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Implementation note: if the array is non-null, its elements must be {@link ColumnPixelData} or {@link ColumnWeightData} instances.
 	 * 
 	 * @throws RuntimeException
 	 *             if an unsupported array element is encountered
 	 */
 	public void setColumnWidths(final Object[] widths) {
 		columnWidths = ColumnUtils.copyWidths(widths);
 		applyColumnWidths();
 	}
 
 	protected final void applyColumnWidths() {
 		if (tableWrapper == null) {
 			return;
 		}
 		ColumnUtils.applyColumnWidths(tableWrapper, columnWidths);
 	}
 
 	protected void applyEditingSupport(final ViewerColumn viewerColumn, final int columnIndex) {
 		final Boolean editable = editableColumnsMap.get(columnIndex);
 		EditingSupport editingSupport = null;
 		if (Boolean.TRUE.equals(editable)) {
 			final PropertyDescriptor property = getPropertyDescriptor(renderingMethods[columnIndex]);
 			final int columnStyle = getColumnStyle(columnIndex);
 			// editingSupport = new InlineEditingSupport0(this, dbc, property, columnStyle);
 			editingSupport = new TableRidgetEditingSupport(this, property, columnStyle);
 		}
 		viewerColumn.setEditingSupport(editingSupport);
 	}
 
 	protected PropertyDescriptor getPropertyDescriptor(final String propertyName) {
 
 		PropertyDescriptor[] descriptors;
 		try {
 			descriptors = Introspector.getBeanInfo(rowClass).getPropertyDescriptors();
 			for (final PropertyDescriptor descriptor : descriptors) {
 				if (descriptor.getName().equals(propertyName)) {
 					return descriptor;
 				}
 			}
 		} catch (final IntrospectionException e) {
 			final String msg = "Could not detect descriptor of the property " + propertyName; //$NON-NLS-1$
 			LOGGER.log(LogService.LOG_ERROR, msg, e);
 		}
 
 		return null;
 
 	}
 
 	protected PropertyDescriptor getPropertyDescriptor(final int columnIndex) {
 		checkColumnRange(columnIndex);
 		return getPropertyDescriptor(renderingMethods[columnIndex]);
 	}
 
 	protected void checkColumnRange(final int columnIndex) {
 		final int range = getColumnCount();
 		if (range != -1) {
 			final String msg = "columnIndex out of range (0 - " + range + " ): " + columnIndex; //$NON-NLS-1$ //$NON-NLS-2$
 			Assert.isLegal(-1 < columnIndex, msg);
 			Assert.isLegal(columnIndex < range, msg);
 		}
 	}
 
 	public void setColumnFormatter(final int columnIndex, final IColumnFormatter formatter) {
 		checkColumnRange(columnIndex);
 		if (formatter != null) {
 			Assert.isLegal(formatter instanceof ColumnFormatter, "formatter must sublass ColumnFormatter"); //$NON-NLS-1$
 		}
 		final Integer key = Integer.valueOf(columnIndex);
 		formatterMap.put(key, formatter);
 	}
 
 	public void setTableFormatter(final ITableFormatter formatter) {
 		tableFormatter = formatter;
 	}
 
 	public void clearColumnFormatters() {
 		formatterMap.clear();
 	}
 
 	private IColumnFormatter[] getColumnFormatters(final int numColumns) {
 		Assert.isLegal(numColumns >= 0);
 		final IColumnFormatter[] result = new IColumnFormatter[numColumns];
 		for (int i = 0; i < numColumns; i++) {
 			final IColumnFormatter columnFormatter = formatterMap.get(Integer.valueOf(i));
 			if (columnFormatter != null) {
 				result[i] = columnFormatter;
 			}
 		}
 		return result;
 	}
 
 	private boolean isViewerConfigured() {
 		return (viewer.getLabelProvider() instanceof TableRidgetLabelProvider) && viewConfigured;
 	}
 
 	@Override
 	public int indexOfOption(final Object option) {
 		final int optionCount = getItemCount();
 		if (optionCount > 0) {
 			// implies viewer != null
 			for (int i = 0; i < optionCount; i++) {
 				if (viewer.getElementAt(i).equals(option)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	public void refresh(final Object element) {
 		if (viewer != null) {
 			viewer.refresh(element, true);
 		}
 	}
 
 	@Override
 	protected void bindUIControl() {
 		final Control control = getUIControl();
 		if (control != null) {
 			dbc = new DataBindingContext();
 			tableWrapper = createTableWrapper();
 			// try to reuse the table viewer 
 			viewer = (AbstractTableViewer) control.getData(TABLE_VIEWER_DATA_KEY);
 			if ((viewer == null) || (viewer.getControl() != control)) {
 				viewer = createTableViewer();
 				control.setData(TABLE_VIEWER_DATA_KEY, viewer);
 			}
 			configureControl();
 			if (getObservableList() != null) {
 				configureViewer(viewer);
 			}
 			// viewer to single selection binding
 			final IObservableValue viewerSelection = ViewersObservables.observeSingleSelection(viewer);
 			dbc.bindValue(viewerSelection, getSingleSelectionObservable(), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE)
 					.setAfterGetValidator(new OutputAwareValidator(this)), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 			control.addKeyListener(keyListener);
 			updateToolTipSupport();
 			// viewer to to multi-selection binding
 			viewerMSB = null;
 			if (!isOutputOnly()) {
 				createMultipleSelectionBinding();
 			}
 		}
 		getFilterHolder().activate(viewer);
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		super.unbindUIControl();
 		getFilterHolder().deactivate(viewer);
 		if (dbc != null) {
 			dbc.dispose();
 			dbc = null;
 		}
 		final Control control = getUIControl();
 		if (control != null) {
 			control.removeKeyListener(keyListener);
 		}
 		viewConfigured = false;
 		viewer = null;
 		tableWrapper = null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void updateFromModel() {
 		super.updateFromModel();
 		if (modelObservables != null) {
 			final List<Object> copy = new ArrayList<Object>(modelObservables);
 			viewerObservables = new WritableList(copy, rowClass);
 		}
 		if (viewer != null) {
 			if (!isViewerConfigured()) {
 				configureControl();
 				configureViewer(viewer);
 			} else {
 				refreshViewer(viewer);
 			}
 		} else {
 			refreshSelection();
 		}
 	}
 
 	private void configureControl() {
 		if (renderingMethods != null) {
 			applyColumns();
 		}
 		applyColumnsMovable();
 		applyTableColumnHeaders();
 	}
 
 	protected void configureViewer(final AbstractTableViewer viewer) {
 		final ObservableListContentProvider viewerCP = new ObservableListContentProvider();
 		final TableRidgetLabelProvider labelProvider = createLabelProvider(viewerCP);
 		viewer.setLabelProvider(labelProvider);
 		viewer.setContentProvider(viewerCP);
 		configureLableProvider(labelProvider, viewer);
 		viewer.setInput(viewerObservables);
 		applyComparator(comparatorMap);
 		viewConfigured = true;
 	}
 
 	private void refreshViewer(final AbstractTableViewer viewer) {
 		viewer.getControl().setRedraw(false); // prevent flicker during update
 		final boolean scrollBarVisibleBeforeRefresh = isVerticalScrollBarVisible(viewer.getControl());
 		final StructuredSelection currentSelection = new StructuredSelection(getSelection());
 		try {
 			configureLableProvider(viewer.getLabelProvider(), viewer);
 			viewer.setInput(viewerObservables);
 		} finally {
 			viewer.setSelection(currentSelection);
 			viewer.getControl().setRedraw(true);
 			layoutIfScrollBarVisibilityChanged(viewer.getControl(), scrollBarVisibleBeforeRefresh);
 		}
 	}
 
 	protected void configureLableProvider(final IBaseLabelProvider labelProvider, final AbstractTableViewer viewer) {
 		if (labelProvider instanceof TableRidgetLabelProvider) {
 			final TableRidgetLabelProvider tableLabelProvider = (TableRidgetLabelProvider) labelProvider;
 			final IColumnFormatter[] formatters = getColumnFormatters(tableLabelProvider.getColumnCount());
 			tableLabelProvider.setFormatters(formatters);
 			tableLabelProvider.setTableFormatter(tableFormatter);
 		}
 	}
 
 	@Override
 	protected StructuredViewerFilterHolder getFilterHolder() {
 		if (filterHolder == null) {
 			filterHolder = new StructuredViewerFilterHolder();
 		}
 		return filterHolder;
 	}
 
 	/**
 	 * Creates the label provider of this table.
 	 * 
 	 * @param viewerCP
 	 * @return label provider
 	 */
 	private TableRidgetLabelProvider createLabelProvider(final ObservableListContentProvider viewerCP) {
 		IObservableMap[] attrMap;
 		if (AbstractSWTWidgetRidget.isBean(rowClass)) {
 			attrMap = BeansObservables.observeMaps(viewerCP.getKnownElements(), rowClass, renderingMethods);
 		} else {
 			attrMap = PojoObservables.observeMaps(viewerCP.getKnownElements(), rowClass, renderingMethods);
 		}
 		final IColumnFormatter[] formatters = getColumnFormatters(attrMap.length);
 		final TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMap, formatters);
 		viewerObservables.addListChangeListener(new IListChangeListener() {
 			public void handleListChange(final ListChangeEvent event) {
 				if ((event.diff != null) && (!event.diff.isEmpty())) {
 					for (final ListDiffEntry diffEntry : event.diff.getDifferences()) {
 						if (!diffEntry.isAddition()) {
 							// an element of the table was removed, 
 							// dispose the according image (stored inside the label provider) 
 							final Object deletedElement = diffEntry.getElement();
 							if (deletedElement != null) {
 								labelProvider.disposeImageOfElement(deletedElement);
 							}
 						}
 					}
 				}
 			}
 		});
 		return labelProvider;
 	}
 
 	/**
 	 * Checks if the table scroll bar visibility changed and triggers a layout() if needed.
 	 * <p>
 	 * When using TableLayout, tables do not consider the presence of a vertical scroll bar and the space it needs in case that the table needs a vertical
 	 * scroll bar, also a horizontal bar will appear (swt/jface problem). This method works around that.
 	 */
 	protected void layoutIfScrollBarVisibilityChanged(final Control control, final boolean barVisibilityBefore) {
 		if (barVisibilityBefore != isVerticalScrollBarVisible(control)) {
 			if (control.getParent().getChildren().length == 1) {
 				control.getParent().layout(true, true);
 			}
 		}
 	}
 
 	/**
 	 * @return <code>true</code> if the given control is {@link Scrollable} and the vertical scroll bar is visible
 	 */
 	protected boolean isVerticalScrollBarVisible(final Control control) {
 		if (control instanceof Scrollable) {
 			final ScrollBar sb = ((Scrollable) control).getVerticalBar();
 			return sb != null && sb.isVisible();
 		}
 		return false;
 	}
 
 	public void bindToModel(final IObservableList rowObservables, final Class<? extends Object> aRowClass, final String[] columnPropertyNames,
 			final String[] columnHeaders) {
 		if (columnHeaders != null) {
 			final String msg = "Mismatch between number of columnPropertyNames and columnHeaders"; //$NON-NLS-1$
 			Assert.isLegal(columnPropertyNames.length == columnHeaders.length, msg);
 		}
 		unbindUIControl();
 
 		rowClass = aRowClass;
 		modelObservables = rowObservables;
 		viewerObservables = null;
 		renderingMethods = new String[columnPropertyNames.length];
 		System.arraycopy(columnPropertyNames, 0, renderingMethods, 0, renderingMethods.length);
 
 		if (columnHeaders != null) {
 			this.columnHeaders = new String[columnHeaders.length];
 			System.arraycopy(columnHeaders, 0, this.columnHeaders, 0, this.columnHeaders.length);
 		} else {
 			this.columnHeaders = null;
 		}
 
 		bindUIControl();
 	}
 
 	public void bindToModel(final Object listHolder, final String listPropertyName, final Class<? extends Object> rowClass, final String[] columnPropertyNames,
 			final String[] columnHeaders) {
 		IObservableList rowValues;
 		if (AbstractSWTWidgetRidget.isBean(rowClass)) {
 			rowValues = BeansObservables.observeList(listHolder, listPropertyName);
 		} else {
 			rowValues = PojoObservables.observeList(listHolder, listPropertyName);
 		}
 		bindToModel(rowValues, rowClass, columnPropertyNames, columnHeaders);
 	}
 
 	@Override
 	public Object getOption(final int index) {
 		if (getRowObservables() == null || index < 0 || index >= getOptionCount()) {
 			throw new IllegalArgumentException("index: " + index); //$NON-NLS-1$
 		}
 		if (viewer != null) {
 			return viewer.getElementAt(index); // sorted
 		}
 		return getRowObservables().get(index); // unsorted
 	}
 
 	@Override
 	protected java.util.List<?> getRowObservables() {
 		return viewerObservables;
 	}
 
 	public IObservableList getObservableList() {
 		return viewerObservables;
 	}
 
 	public boolean hasMoveableColumns() {
 		return moveableColumns;
 	}
 
 	public void setMoveableColumns(final boolean moveableColumns) {
 		if (this.moveableColumns != moveableColumns) {
 			this.moveableColumns = moveableColumns;
 			applyColumnsMovable();
 		}
 	}
 
 	public boolean isNativeToolTip() {
 		return nativeToolTip;
 	}
 
 	public void setNativeToolTip(final boolean nativeToolTip) {
 		if (this.nativeToolTip != nativeToolTip) {
 			this.nativeToolTip = nativeToolTip;
 			updateToolTipSupport();
 		}
 	}
 
 	public void setComparator(final int columnIndex, final Comparator<?> compi) {
 		checkColumnRange(columnIndex);
 		final Integer key = Integer.valueOf(columnIndex);
 		if (compi != null) {
 			comparatorMap.put(key, compi);
 		} else {
 			comparatorMap.remove(key);
 		}
 		if (columnIndex == sortedColumn) {
 			applyComparator(comparatorMap);
 		}
 	}
 
 	public boolean isColumnSortable(final int columnIndex) {
 		checkColumnRange(columnIndex);
 		boolean result = false;
 		final Integer key = Integer.valueOf(columnIndex);
 		final Boolean sortable = sortableColumnsMap.get(columnIndex);
 		if (!Boolean.FALSE.equals(sortable)) {
 			result = comparatorMap.get(key) != null;
 		}
 		return result;
 	}
 
 	protected int getSortDirection() {
 		final int direction = isSortedAscending() ? SWT.UP : SWT.DOWN;
 		return direction;
 	}
 
 	public int getSortedColumn() {
 		final boolean isSorted = sortedColumn != -1 && isColumnSortable(sortedColumn);
 		return isSorted ? sortedColumn : -1;
 	}
 
 	public boolean isSortedAscending() {
 		return getSortedColumn() != -1 && isSortedAscending;
 	}
 
 	public void setColumnSortable(final int columnIndex, final boolean sortable) {
 		checkColumnRange(columnIndex);
 		final Integer key = Integer.valueOf(columnIndex);
 		final Boolean newValue = Boolean.valueOf(sortable);
 		Boolean oldValue = sortableColumnsMap.put(key, newValue);
 		if (oldValue == null) {
 			oldValue = Boolean.TRUE;
 		}
 		if (!newValue.equals(oldValue)) {
 			firePropertyChange(ISortableByColumn.PROPERTY_COLUMN_SORTABILITY, null, columnIndex);
 		}
 	}
 
 	public void setColumnEditable(final int columnIndex, final boolean editable) {
 		checkColumnRange(columnIndex);
 		final Integer key = Integer.valueOf(columnIndex);
 		final Boolean newValue = Boolean.valueOf(editable);
 		Boolean oldValue = editableColumnsMap.put(key, newValue);
 		if (oldValue == null) {
 			oldValue = Boolean.TRUE;
 		}
 	}
 
 	public void setSortedAscending(final boolean ascending) {
 		if (isSortedAscending != ascending) {
 			final boolean oldSortedAscending = isSortedAscending;
 			isSortedAscending = ascending;
 			applyComparator(comparatorMap);
 			firePropertyChange(ISortableByColumn.PROPERTY_SORT_ASCENDING, oldSortedAscending, isSortedAscending);
 		}
 	}
 
 	public void setSortedColumn(final int columnIndex) {
 		if (columnIndex != -1) {
 			checkColumnRange(columnIndex);
 		}
 		if (sortedColumn != columnIndex) {
 			final int oldSortedColumn = sortedColumn;
 			sortedColumn = columnIndex;
 			applyComparator(comparatorMap);
 			firePropertyChange(ISortableByColumn.PROPERTY_SORTED_COLUMN, oldSortedColumn, sortedColumn);
 		}
 	}
 
 	protected AbstractTableViewer getTableViewer() {
 		return viewer;
 	}
 
 	/**
 	 * Always returns true because mandatory markers do not make sense for this ridget.
 	 */
 	@Override
 	public boolean isDisableMandatoryMarker() {
 		return true;
 	}
 
 	protected boolean isErrorMarked(final Widget item) {
 		final Object data = item.getData();
 		final Collection<RowErrorMessageMarker> markers = getMarkersOfType(RowErrorMessageMarker.class);
 		for (final RowErrorMessageMarker marker : markers) {
 			if (marker.getRowValue() == data) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void createMultipleSelectionBinding() {
 		if (viewerMSB == null && dbc != null && viewer != null) {
 			final StructuredSelection currentSelection = new StructuredSelection(getSelection());
 			final IViewerObservableList viewerSelections = ViewersObservables.observeMultiSelection(viewer);
 			viewerMSB = dbc.bindList(viewerSelections, getMultiSelectionObservable(), new UpdateListStrategy(UpdateListStrategy.POLICY_UPDATE),
 					new UpdateListStrategy(UpdateListStrategy.POLICY_UPDATE));
 			viewer.setSelection(currentSelection);
 		}
 	}
 
 	private void disposeMultipleSelectionBinding() {
		if (viewerMSB != null) { // implies dbc != null
 			viewerMSB.dispose();
			dbc.removeBinding(viewerMSB);
 			viewerMSB = null;
 		}
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Enforces selection in the control:
 	 * <ul>
 	 * <li>disallows selection changes when the ridget is "output only"</li>
 	 * <li>disallows multiple selection is the selection type of the ridget is {@link ISelectableRidget.SelectionType#SINGLE}</li>
 	 * </ul>
 	 */
 	private final class SelectionTypeEnforcer extends SelectionAdapter {
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			if (isOutputOnly()) {
 				// undo user selection when "output only"
 				final AbstractTableViewer tableViewer = getTableViewer();
 				if (tableViewer != null) {
 					tableViewer.setSelection(new StructuredSelection(getSelection()));
 				}
 			} else if (SelectionType.SINGLE.equals(getSelectionType())) {
 				if (getUiSelectionCount() > 1) {
 					Assert.isTrue(e.widget == getUIControl());
 					// ignore this event
 					e.doit = false;
 					// set selection to most recent item
 					setUiSelection(e.item);
 					// fire event
 					final Event event = new Event();
 					event.type = SWT.Selection;
 					event.doit = true;
 					getUIControl().notifyListeners(SWT.Selection, event);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Notifies the double-click listeners when either a space or CR has been pressed.
 	 */
 	private final class TableKeyListener extends KeyAdapter {
 
 		@Override
 		public void keyPressed(final KeyEvent e) {
 			if (e.character == ' ' || e.character == SWT.CR) {
 				// the passed event (null) will not be read by the ClickForwarder 
 				getClickForwarder().mouseDoubleClick(null);
 			}
 		}
 	}
 
 }
