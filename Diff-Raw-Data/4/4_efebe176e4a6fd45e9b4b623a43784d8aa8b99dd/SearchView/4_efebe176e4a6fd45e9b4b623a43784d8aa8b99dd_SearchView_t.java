 /*******************************************************************************
  * Copyright (c) 2009 EclipseSource Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the 
  * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v1.0 
  * which accompanies this distribution. The Eclipse Public License is available at 
  * http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License 
  * is available at http://www.eclipse.org/org/documents/edl-v10.php.
  *
  * Contributors: 
  *     EclipseSource Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.toast.backend.rap;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import org.eclipse.examples.toast.backend.controlcenter.IControlCenter;
 import org.eclipse.examples.toast.backend.data.IVehicle;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.rwt.lifecycle.WidgetUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.ViewPart;
 
 public class SearchView extends ViewPart {
 
 	private static final int COL_VEHICLE_ID = 0;
 	private static final int COL_DRIVER_NAME = 1;
 
 	public static final String ID = "org.eclipse.examples.toast.backend.rap.searchView";
 
 	private Text filterText;
 	private TableViewer viewer;
 	private CellLabelProvider labelProvider = new VehicleLabelProvider();
 	private VehicleFilter viewerFilter = new VehicleFilter();
 	public static IControlCenter controlCenter;
 
 	static final class VehicleLabelProvider extends CellLabelProvider {
 
		private static Image CAR_IMAGE = Activator.getImageDescriptor("/icons/sample3.gif").createImage();
		private static Image CAR_PROBLEM_IMAGE = Activator.getImageDescriptor("/icons/sample2.gif").createImage();
 
 		public void update(final ViewerCell cell) {
 			IVehicle vehicle = (IVehicle) cell.getElement();
 			int columnIndex = cell.getColumnIndex();
 			switch (columnIndex) {
 				case COL_VEHICLE_ID :
 					cell.setText(vehicle.getName());
 					Image image = vehicle.getCurrentLocation().getSpeed() < 80 ? CAR_IMAGE : CAR_PROBLEM_IMAGE;
 					cell.setImage(image);
 					break;
 				case COL_DRIVER_NAME :
 					cell.setText(vehicle.getDriver().getLastName() + ", " + vehicle.getDriver().getFirstName());
 					break;
 			}
 		}
 	}
 
 	static final class VehicleComparator extends ViewerComparator implements Comparator {
 
 		private final boolean ascending;
 		private final int property;
 
 		public VehicleComparator(final int property, final boolean ascending) {
 			this.property = property;
 			this.ascending = ascending;
 		}
 
 		public int compare(final Viewer viewer, final Object object1, final Object object2) {
 			return compare(object1, object2);
 		}
 
 		public boolean isSorterProperty(final Object elem, final String property) {
 			return true;
 		}
 
 		public int compare(final Object object1, final Object object2) {
 			IVehicle vehicle1 = (IVehicle) object1;
 			IVehicle vehicle2 = (IVehicle) object2;
 			int result = 0;
 			if (property == COL_VEHICLE_ID) {
 				result = vehicle1.getName().compareTo(vehicle2.getName());
 			} else if (property == COL_DRIVER_NAME) {
 				result = vehicle1.getDriver().getLastName().compareTo(vehicle2.getDriver().getLastName());
 			}
 			if (!ascending) {
 				result = result * -1;
 			}
 			return result;
 		}
 	}
 
 	static final class VehicleFilter extends ViewerFilter {
 
 		private String text;
 
 		public void setText(final String string) {
 			this.text = string.toLowerCase();
 		}
 
 		public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
 			boolean result = true;
 			IVehicle vehicle = (IVehicle) element;
 			if (text != null && text.length() > 0) {
 				result = false;
 				String vid = vehicle.getName().toLowerCase();
 				result |= vid.indexOf(text) != -1;
 				String driverName = (vehicle.getDriver().getLastName() + ", " + vehicle.getDriver().getFirstName()).toLowerCase();
 				result |= driverName.indexOf(text) != -1;
 			}
 			return result;
 		}
 
 		public boolean isFilterProperty(final Object element, final String prop) {
 			return true;
 		}
 	}
 
 	public void createPartControl(final Composite parent) {
 		GridLayout mainLayout = new GridLayout();
 		mainLayout.marginHeight = 0;
 		mainLayout.marginWidth = 0;
 		parent.setLayout(mainLayout);
 		Control filterComposite = createFilterComposite(parent);
 		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		if (viewer != null && !viewer.getControl().isDisposed()) {
 			viewer.getControl().dispose();
 		}
 		Composite resizer = new Composite(parent, SWT.NONE);
 		resizer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		viewer = new TableViewer(resizer, SWT.MULTI);
 		viewer.setUseHashlookup(true);
 		viewer.setContentProvider(new ArrayContentProvider());
 		createVehicleIdColumn();
 		createDriverNameColumn();
 		viewer.setLabelProvider(labelProvider);
 		viewer.addFilter(viewerFilter);
 		Table table = viewer.getTable();
 		TableColumnLayout tableLayout = new TableColumnLayout();
 		tableLayout.setColumnData(table.getColumn(COL_VEHICLE_ID), new ColumnWeightData(50));
 		tableLayout.setColumnData(table.getColumn(COL_DRIVER_NAME), new ColumnWeightData(50));
 		resizer.setLayout(tableLayout);
 		table.setHeaderVisible(true);
 		addTableContextMenu(table);
 		getSite().setSelectionProvider(viewer);
 		// TODO need to fix this to listen for the control center coming around and 
 		// then set the input when it arrives.
 		if (controlCenter != null)
 			viewer.setInput(controlCenter.getVehicles());
 		// TODO [rst] hard coded dummy text to fill status bar
 		String message = "Tue, Mar 24 2009,  7:21 am: " + " Vehicle: CA-SAN FRANCISCO-10 reports emergency at 37,7897, -122.3942";
 		IStatusLineManager statusLineMgr = getViewSite().getActionBars().getStatusLineManager();
 		statusLineMgr.setMessage(message);
 	}
 
 	public void setFocus() {
 		filterText.setFocus();
 	}
 
 	private Control createFilterComposite(final Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(LayoutUtil.createGridLayout(3, false, 5, 2));
 		Label label = new Label(composite, SWT.NONE);
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		label.setText("Filter: ");
 		filterText = new Text(composite, SWT.BORDER);
 		filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		filterText.addModifyListener(new ModifyListener() {
 
 			public void modifyText(final ModifyEvent event) {
 				Text text = (Text) event.widget;
 				viewerFilter.setText(text.getText());
 				viewer.refresh();
 			}
 		});
 		Button clearButton = new Button(composite, SWT.PUSH);
 		clearButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		clearButton.setData(WidgetUtil.CUSTOM_VARIANT, "clearButton");
 		Image clearImage = Activator.getImageDescriptor("/icons/clear.png").createImage();
 		clearButton.setImage(clearImage);
 		clearButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				filterText.setText("");
 			}
 		});
 		return composite;
 	}
 
 	private TableViewerColumn createVehicleIdColumn() {
 		TableViewerColumn result = new TableViewerColumn(viewer, SWT.LEFT);
 		result.setLabelProvider(labelProvider);
 		TableColumn column = result.getColumn();
 		column.setText("Vehicle Id");
 		column.setWidth(120);
 		column.setMoveable(true);
 		column.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(final SelectionEvent event) {
 				int sortDirection = updateSortDirection((TableColumn) event.widget);
 				sort(viewer, COL_VEHICLE_ID, sortDirection == SWT.DOWN);
 			}
 		});
 		return result;
 	}
 
 	private TableViewerColumn createDriverNameColumn() {
 		TableViewerColumn result = new TableViewerColumn(viewer, SWT.LEFT);
 		result.setLabelProvider(labelProvider);
 		TableColumn column = result.getColumn();
 		column.setText("Driver Name");
 		column.setWidth(150);
 		column.setMoveable(true);
 		column.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(final SelectionEvent event) {
 				int sortDirection = updateSortDirection((TableColumn) event.widget);
 				sort(viewer, COL_DRIVER_NAME, sortDirection == SWT.DOWN);
 			}
 		});
 		return result;
 	}
 
 	private void addTableContextMenu(Table table) {
 		Menu menu = new Menu(table);
 		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
 		menuItem.setText("Context Menu");
 		table.setMenu(menu);
 	}
 
 	private static int updateSortDirection(final TableColumn column) {
 		Table table = column.getParent();
 		if (column == table.getSortColumn()) {
 			if (table.getSortDirection() == SWT.UP) {
 				table.setSortDirection(SWT.DOWN);
 			} else {
 				table.setSortDirection(SWT.UP);
 			}
 		} else {
 			table.setSortColumn(column);
 			table.setSortDirection(SWT.DOWN);
 		}
 		return table.getSortDirection();
 	}
 
 	private static void sort(final TableViewer viewer, final int property, final boolean ascending) {
 		List input = (List) viewer.getInput();
 		if (input == null)
 			return;
 		Collections.sort(input, new VehicleComparator(property, ascending));
 		viewer.refresh();
 	}
 }
