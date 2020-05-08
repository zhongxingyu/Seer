 /*
  * Copyright (C) 2010, 2011 Openismus GmbH
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
 
 package org.glom.web.client.ui;
 
 import org.glom.web.client.place.DetailsPlace;
 import org.glom.web.client.ui.cell.NavigationButtonCell;
 import org.glom.web.client.ui.list.ListViewTable;
 import org.glom.web.shared.TypedDataItem;
 import org.glom.web.shared.libglom.layout.LayoutGroup;
 
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 
 public class ListViewImpl extends Composite implements ListView {
 
 	/*
 	 * Cell renderer for Details open buttons.
 	 */
 	private class ListViewNavigationButtonCell extends NavigationButtonCell {
 		private final String documentID;
 		private final String tableName;
 
 		public ListViewNavigationButtonCell(final String documentID, final String tableName) {
 			this.documentID = documentID;
 			this.tableName = tableName;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see com.google.gwt.cell.client.ButtonCell#onEnterKeyDown(com.google.gwt.cell.client.Cell.Context,
 		 * com.google.gwt.dom.client.Element, java.lang.String, com.google.gwt.dom.client.NativeEvent,
 		 * com.google.gwt.cell.client.ValueUpdater)
 		 */
 		@Override
 		protected void onEnterKeyDown(final Context context, final Element parent, final String value,
 				final NativeEvent event, final ValueUpdater<String> valueUpdater) {
			presenter.goTo(new DetailsPlace(documentID, tableName, (TypedDataItem) context.getKey()));
 		}
 
 	}
 
 	final private FlowPanel mainPanel = new FlowPanel();
 	private Presenter presenter;
 
 	public ListViewImpl() {
 		initWidget(mainPanel);
 	}
 
 	@Override
 	public void setPresenter(final Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 	@Override
 	public void setCellTable(final String documentID, final String tableName, final LayoutGroup layoutGroup,
 			final String quickFind) {
 		// This is not really in the MVP style because we're creating a new ListTable (really just a configured
 		// CellTable) for every document and table name change. The issue with creating a re-usable CellTable with
 		// methods like setColumnTitles() and setNumRows() is that the column objects (new Column<DataItem[],
 		// DataItem>(new GlomTextCell())) aren't destroyed when the column is removed from the CellTable and
 		// IndexOutOfBounds exceptions are encountered with invalid array indexes trying access the data in this line:
 		// return object[j]. There's probably a workaround that could be done to fix this but I'm leaving it until
 		// there's a reason to fix it (performance, ease of testing, alternate implementation or otherwise).
 		// Note: This comment refers to code that is now in the ListTable class.
 
 		mainPanel.clear();
 
 		final ListViewTable listViewTable = new ListViewTable(documentID, layoutGroup,
 				new ListViewNavigationButtonCell(documentID, tableName), quickFind);
 
 		if (layoutGroup.getExpectedResultSize() <= listViewTable.getMinNumVisibleRows()) {
 			// Set the table row count to the minimum row count if the data row count is less than or equal to
 			// the minimum row count. This ensures that data with fewer rows than the minimum will not create
 			// indexes in the underlying CellTable that will override the rendering of the empty rows.
 			listViewTable.setRowCount(listViewTable.getMinNumVisibleRows());
 		} else {
 			// Set the table row count to the data row count if it's larger than the minimum number of rows
 			// visible.
 			listViewTable.setRowCount(layoutGroup.getExpectedResultSize());
 		}
 
 		mainPanel.add(listViewTable);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.ui.ListView#clear()
 	 */
 	@Override
 	public void clear() {
 		mainPanel.clear();
 	}
 
 }
