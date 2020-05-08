 /*
  * Copyright (C) 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.client.ui.details;
 
 import java.util.ArrayList;
 
 import org.glom.web.client.OnlineGlomServiceAsync;
 import org.glom.web.client.ui.OnlineGlomConstants;
 import org.glom.web.client.ui.cell.NavigationButtonCell;
 import org.glom.web.client.ui.list.ListTable;
 import org.glom.web.shared.DataItem;
 import org.glom.web.shared.TypedDataItem;
 import org.glom.web.shared.libglom.layout.LayoutItemPortal;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Style.Visibility;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortList;
 import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.Range;
 import com.google.gwt.view.client.RowCountChangeEvent;
 
 /**
  *
  */
 public class RelatedListTable extends ListTable {
 
 	// OnlineGlomConstants.java is generated in the target/ directory,
 	// from OnlineGlomConstants.properties
 	// by the gwt-maven-plugin's i18n (mvn:i18n) goal.
 	private OnlineGlomConstants constants = GWT.create(OnlineGlomConstants.class);
 
 	// These represent the minimum and maximum number of rows in the cell table not the number of rows with data.
 	private static final int MAX_TABLE_ROWS = 5;
 	private static final int MIN_TABLE_ROWS = MAX_TABLE_ROWS;
 
 	private TypedDataItem foreignKeyValue;
 	private String relationshipName;
 	private int numNonEmptyRows = 0;
 
 	private final static int expectedHeight = initializeExepectedHeight();
 
 	public RelatedListTable(String documentID, LayoutItemPortal layoutItemPortal, TypedDataItem foreignKeyValue,
 			NavigationButtonCell navigationButtonCell) {
 
 		super(documentID);
 
 		// These variables need to be set before the createCellTable() method is called so that the data provider can
 		// use them.
 		this.foreignKeyValue = foreignKeyValue;
		this.relationshipName = layoutItemPortal.getRelationshipNameUsed();
 
 		// TODO: Is tableName set here?
 		createCellTable(layoutItemPortal, tableName, MAX_TABLE_ROWS, constants.open(), navigationButtonCell);
 
 		// The FixedLayout property tells the browser that we want to manually specify the column widths and don't want
 		// the table to overflow it's container. Browsers will make columns with equal widths since we're currently not
 		// manally specifying the column widths.
 		cellTable.setWidth("100%", true);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.ui.list.ListTable#getDataProvider()
 	 */
 	@Override
 	protected AsyncDataProvider<DataItem[]> getDataProvider() {
 		AsyncDataProvider<DataItem[]> dataProvider = new AsyncDataProvider<DataItem[]>() {
 
 			@Override
 			@SuppressWarnings("unchecked")
 			protected void onRangeChanged(final HasData<DataItem[]> display) {
 				// setup the callback object
 				final Range range = display.getVisibleRange();
 				final int start = range.getStart();
 				AsyncCallback<ArrayList<DataItem[]>> callback = new AsyncCallback<ArrayList<DataItem[]>>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						// TODO: create a way to notify users of asynchronous callback failures
 						GWT.log("AsyncCallback Failed: OnlineGlomService.get(Sorted)RelatedListData(): "
 								+ caught.getMessage());
 					}
 
 					@Override
 					public void onSuccess(ArrayList<DataItem[]> result) {
 						// keep track of the number of non-empty rows (rows with data)
 						numNonEmptyRows = result.size();
 						// Add empty rows if required.
 						int numEmptyRows = MIN_TABLE_ROWS - numNonEmptyRows;
 						for (int i = 0; i < numEmptyRows; i++) {
 							// A row that has one null item will be rendered as an empty row.
 							result.add(new DataItem[1]);
 						}
 						updateRowData(start, result);
 
 						// Note: numNonEmptyRows is not used by the RowCountChangeEvent handler but we need to fire the
 						// event to get ListTable.ListTablePage.createText() to run and update the pager text using
 						// getNumNonEmptyRows().
 						RowCountChangeEvent.fire(display, numNonEmptyRows, true);
 					}
 				};
 
 				// get data from the server
 				ColumnSortList colSortList = cellTable.getColumnSortList();
 				if (colSortList.size() > 0) {
 					// ColumnSortEvent has been requested by the user
 					ColumnSortInfo info = colSortList.get(0);
 
 					OnlineGlomServiceAsync.Util.getInstance().getSortedRelatedListData(documentID, tableName,
 							relationshipName, foreignKeyValue, start, range.getLength(),
 							cellTable.getColumnIndex((Column<DataItem[], ?>) info.getColumn()), info.isAscending(),
 							callback);
 
 				} else {
 					OnlineGlomServiceAsync.Util.getInstance().getRelatedListData(documentID, tableName,
 							relationshipName, foreignKeyValue, start, range.getLength(), callback);
 
 				}
 
 			}
 		};
 
 		return dataProvider;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.ui.list.ListTable#getMinNumVisibleRows()
 	 */
 	@Override
 	public int getMinNumVisibleRows() {
 		return MIN_TABLE_ROWS;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.ui.list.ListTable#getNumNonEmptyRows()
 	 */
 	@Override
 	public int getNumNonEmptyRows() {
 		return numNonEmptyRows;
 	}
 
 	/**
 	 * Gets the expected height of a RelatedListTable.
 	 * 
 	 * @return the expect height of a RelatedListTable in pixels
 	 */
 	public static int getExpectedHeight() {
 		return expectedHeight;
 	}
 
 	// called while class is being initialized
 	private static int initializeExepectedHeight() {
 		// TODO Use a real RelatedListTable instead of building one manually. It's probably better to do this when
 		// RelatedListTables are created in Portal instead of DetailsActivity.
 
 		// This table simulates a related list with one row containing a Text cell and a Button cell.
 		SafeHtmlBuilder tableBuilder = new SafeHtmlBuilder();
 		tableBuilder.append(SafeHtmlUtils
 				.fromSafeConstant("<table class=\"data-list\"><thead><tr><th>TH</th><th>BH</th></tr></thead><tbody>"));
 		for (int i = 0; i < MAX_TABLE_ROWS; i++) {
 			tableBuilder.append(SafeHtmlUtils
 					.fromSafeConstant("<tr><td>T</td><td><button type=\"button\">B</button></td></tr>"));
 		}
 		tableBuilder.append(SafeHtmlUtils.fromSafeConstant("</tbody></head>"));
 		HTML table = new HTML(tableBuilder.toSafeHtml());
 
 		// The pager
 		SimplePager pager = new SimplePager();
 		pager.addStyleName("pager");
 
 		// Pack the table and pager as they are found in the details view.
 		FlowPanel group = new FlowPanel();
 		group.setStyleName("group");
 		FlowPanel subgroup = new FlowPanel();
 		subgroup.setStyleName("portal");
 		subgroup.add(table);
 		subgroup.add(pager);
 		group.add(subgroup);
 
 		// Calculate the height similar to Utils.getWidgetHeight().
 		Document doc = Document.get();
 		com.google.gwt.dom.client.Element div = doc.createDivElement();
 		div.getStyle().setVisibility(Visibility.HIDDEN);
 		div.appendChild(group.getElement().<com.google.gwt.user.client.Element> cast());
 		doc.getBody().appendChild(div);
 		int relatedListTableHeight = group.getElement().getFirstChildElement().getOffsetHeight();
 
 		// remove the div from the from the document
 		doc.getBody().removeChild(div);
 		div = null;
 
 		return relatedListTableHeight;
 	}
 }
