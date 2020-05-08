 /*
  * Copyright (C) 2011 Openismus GmbH
  * Copyright (c) 2011 Ben Konrath <ben@bagu.org>
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
 
 package org.glom.web.client.ui.list;
 
 import java.util.List;
 
 import org.glom.web.client.Utils;
 import org.glom.web.client.ui.cell.BooleanCell;
 import org.glom.web.client.ui.cell.NavigationButtonCell;
 import org.glom.web.client.ui.cell.NumericCell;
 import org.glom.web.client.ui.cell.TextCell;
 import org.glom.web.shared.DataItem;
 import org.glom.web.shared.libglom.Field.GlomFieldType;
 import org.glom.web.shared.libglom.NumericFormat;
 import org.glom.web.shared.libglom.layout.Formatting;
 import org.glom.web.shared.libglom.layout.LayoutGroup;
 import org.glom.web.shared.libglom.layout.LayoutItem;
 import org.glom.web.shared.libglom.layout.LayoutItemField;
 
import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.dom.client.Style.Visibility;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
 import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
 import com.google.gwt.user.cellview.client.SafeHtmlHeader;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.AbstractDataProvider;
 import com.google.gwt.view.client.ProvidesKey;
 
 /**
  * 
  */
 public abstract class ListTable extends Composite {
 
 	private class ListTablePager extends SimplePager {
 		public ListTablePager() {
 			super(SimplePager.TextLocation.CENTER);
 			setStyleName("pager");
 		}
 
 		/*
 		 * A custom version of createText to display the correct number of data row when empty rows have been added to
 		 * the CellTable. This method is needed because the row count change event handler
 		 * (AbstractPager.handleRowCountChange()) doesn't use the row count that is sent along with the
 		 * RowCountChangeEvent.
 		 * 
 		 * @see com.google.gwt.user.cellview.client.AbstractPager#handleRowCountChange(int, boolean)
 		 * 
 		 * @see com.google.gwt.user.cellview.client.SimplePager#createText()
 		 */
 		@Override
 		protected String createText() {
 			final int numNonEmptyRows = getNumNonEmptyRows();
 			if (numNonEmptyRows < getMinNumVisibleRows()) {
 				final NumberFormat formatter = NumberFormat.getFormat("#,###");
 				return formatter.format(1) + "-" + formatter.format(numNonEmptyRows) + " of "
 						+ formatter.format(numNonEmptyRows);
 			} else {
 				return super.createText();
 			}
 		}
 	}
 
 	final private FlowPanel mainPanel = new FlowPanel();
 	final private ListTablePager pager = new ListTablePager();
 	protected String documentID;
 	protected String tableName;
 	protected String quickFind;
 	protected CellTable<DataItem[]> cellTable;
 	protected EventBus eventBus;
 	Column<DataItem[], String> navigationButtonColumn;
 	private int cellTableBodyHeight = 0;
 
 	abstract protected AbstractDataProvider<DataItem[]> getDataProvider();
 
 	@SuppressWarnings("unused")
 	private ListTable() {
 		// disable default constructor
 	}
 
 	public ListTable(final String documentID) {
 		this.documentID = documentID;
 	}
 
 	protected void createCellTable(final LayoutGroup layoutGroup, final String tableName, final int numVisibleRows,
 			final String navigationButtonLabel, final NavigationButtonCell navigationButtonCell) {
 		this.tableName = tableName;
 		final List<LayoutItem> layoutItems = layoutGroup.getItems();
 
		ProvidesKey<DataItem[]> keyProvider = null;
 		final int primaryKeyIndex = layoutGroup.getPrimaryKeyIndex();
		if (primaryKeyIndex >= layoutItems.size()) {
			GWT.log("createCellTable(): primaryKeyIndex is out of range.");
		} else {
			final LayoutItem primaryKeyItem = layoutItems.get(primaryKeyIndex);
			if (!(primaryKeyItem instanceof LayoutItemField)) {
				GWT.log("createCellTable(): primaryKeyItem is not a LayoutItemField.");
			} else {
				final LayoutItemField primaryKeyLayoutItem = (LayoutItemField) primaryKeyItem;
				final GlomFieldType primaryKeyFieldType = primaryKeyLayoutItem.getGlomType();

				keyProvider = new ProvidesKey<DataItem[]>() {
					@Override
					public Object getKey(final DataItem[] row) {
						if (row.length == 1 && row[0] == null)
							// an empty row
							return null;
						return Utils.getTypedDataItem(primaryKeyFieldType, row[primaryKeyIndex]);
					}
				};
 			}
		}
 
 		// create the CellTable with the requested number of rows and the key provider
 		cellTable = new CellTable<DataItem[]>(numVisibleRows, keyProvider);
 
 		// set some style
 		cellTable.setStyleName("data-list");
 		cellTable.getElement().getStyle().setProperty("whiteSpace", "nowrap"); // this prevents the header and row text
 		// from wrapping
 
 		// add columns to the CellTable and deal with the case of the hidden primary key
 		final int numItems = /* TODO: layoutGroup.hasHiddenPrimaryKey() ? layoutItems.size() - 1 : */layoutItems.size();
 		for (int i = 0; i < numItems; i++) {
 			final LayoutItem layoutItem = layoutItems.get(i);
 
 			// only add columns for LayoutItemField types
 			if (layoutItem instanceof LayoutItemField) {
 				addColumn((LayoutItemField) layoutItem);
 			}
 
 		}
 
 		// add the navigation buttons as the last column
 		addNavigationButtonColumn(navigationButtonLabel, navigationButtonCell);
 
 		// create and set the data provider
 		final AbstractDataProvider<DataItem[]> dataProvider = getDataProvider();
 		dataProvider.addDataDisplay(cellTable);
 
 		// add an AsyncHandler to activate sorting for the data provider
 		cellTable.addColumnSortHandler(new AsyncHandler(cellTable));
 
 		// pack the widgets into the container
 		pager.setDisplay(cellTable);
 		mainPanel.add(cellTable);
 		mainPanel.add(pager);
 
 		/*
 		 * Update the height of the loading indicator widget to match the body of the CellTable so that the pager widget
 		 * doesn't bounce up and down while paging. This code also ensures that loading indicator GIF is in the centre
 		 * of the table.
 		 * 
 		 * TODO: Make this work with related lists in Notebooks. These related list tables will have the original bouncy
 		 * behaviour because CellTable.getBodyHeight() of a related list table in an unselected notebook tab returns 0.
 		 * 
 		 * TODO: Fix the bounce when paging to the first or last page that doesn't fall on a natural page boundary. This
 		 * happens in the first and last page when dataSize % pageSize != 0.
 		 */
 		cellTable.addLoadingStateChangeHandler(new LoadingStateChangeEvent.Handler() {
 
 			@Override
 			public void onLoadingStateChanged(final LoadingStateChangeEvent event) {
 				// LoadingState.LOADED means the data has been received but not necessarily rendered.
 				if (event.getLoadingState() == LoadingState.LOADED) {
 					new Timer() {
 
 						@Override
 						public void run() {
 							if (cellTable.isAttached()) {
 								final int bodyHeight = cellTable.getBodyHeight();
 								/*
 								 * Modify the indicator widget only if body height is bigger than the body height that
 								 * has already been set. This is just a safety check for the case where the timer isn't
 								 * long enough and the body height is calculated to be smaller than its full size. In
 								 * practice this is not expected to happen.
 								 * 
 								 * Since cellTableBodyHeight is initialised to 0, the indicator widget will not be
 								 * modified when the body height cannot be calculated (e.g. when a related list table is
 								 * in an unselected notebook tab).
 								 */
 								if (bodyHeight > cellTableBodyHeight) {
 									final Widget loadingIndicator = cellTable.getLoadingIndicator();
 
 									// Set the margin of the parent div to zero.
 									final Element parent = loadingIndicator.getElement().getParentElement();
 									parent.getStyle().setMargin(0, Unit.PX);
 
 									// Set the height of the table cell that holds the loading indicator GIF.
 									final Element cell = parent.getParentElement().getParentElement()
 											.getParentElement();
 									cell.getStyle().setPadding(0, Unit.PX);
 									cell.getStyle().setHeight(bodyHeight, Unit.PX);
 
 									// save the new body height
 									cellTableBodyHeight = bodyHeight;
 
 								}
 							}
 						}
 					}.schedule(200); // 200 ms should be enough
 				}
 
 			}
 		});
 
 		// initialize composite widget
 		initWidget(mainPanel);
 	}
 
 	private void addColumn(final LayoutItemField layoutItemField) {
 		// Setup the default alignment of the column.
 		HorizontalAlignmentConstant columnAlignment;
 		final Formatting formatting = layoutItemField.getFormatting();
 		switch (formatting.getHorizontalAlignment()) {
 		case HORIZONTAL_ALIGNMENT_LEFT:
 			columnAlignment = HasHorizontalAlignment.ALIGN_LEFT;
 			break;
 		case HORIZONTAL_ALIGNMENT_RIGHT:
 			columnAlignment = HasHorizontalAlignment.ALIGN_RIGHT;
 			break;
 		case HORIZONTAL_ALIGNMENT_AUTO:
 		default:
 			columnAlignment = HasHorizontalAlignment.ALIGN_DEFAULT;
 			break;
 		}
 
 		// create a new column
 		Column<DataItem[], ?> column = null;
 		final int j = cellTable.getColumnCount();
 		switch (layoutItemField.getGlomType()) {
 
 		case TYPE_BOOLEAN:
 			column = new Column<DataItem[], Boolean>(new BooleanCell()) {
 				@Override
 				public Boolean getValue(final DataItem[] row) {
 					if (row.length == 1 && row[0] == null)
 						// an empty row
 						return null;
 					return row[j].getBoolean();
 				}
 			};
 			// override the configured horizontal alignment
 			columnAlignment = HasHorizontalAlignment.ALIGN_CENTER;
 			break;
 
 		case TYPE_NUMERIC:
 			// create a GWT NumberFormat for the column
 			final NumericFormat numericFormat = formatting.getNumericFormat();
 			final NumberFormat gwtNumberFormat = Utils.getNumberFormat(numericFormat);
 
 			// create the actual column
 			column = new Column<DataItem[], Double>(new NumericCell(formatting.getTextFormatColourForeground(),
 					formatting.getTextFormatColourBackground(), gwtNumberFormat,
 					numericFormat.getUseAltForegroundColorForNegatives(), numericFormat.getCurrencySymbol())) {
 				@Override
 				public Double getValue(final DataItem[] row) {
 					if (row.length == 1 && row[0] == null)
 						// an empty row
 						return null;
 					return row[j].getNumber();
 				}
 			};
 			break;
 
 		default:
 			// use a text rendering cell for types we don't know about but log an error
 			// TODO log error here
 		case TYPE_DATE:
 		case TYPE_IMAGE:
 		case TYPE_INVALID:
 		case TYPE_TIME:
 		case TYPE_TEXT:
 			column = new Column<DataItem[], String>(new TextCell(formatting.getTextFormatColourForeground(),
 					formatting.getTextFormatColourBackground())) {
 				@Override
 				public String getValue(final DataItem[] row) {
 					if (row.length == 1 && row[0] == null)
 						// an empty row
 						return null;
 					return row[j].getText();
 				}
 			};
 			break;
 		}
 
 		// set column properties and add to cell cellTable
 		column.setHorizontalAlignment(columnAlignment);
 		column.setSortable(true);
 		cellTable.addColumn(column, new SafeHtmlHeader(SafeHtmlUtils.fromString(layoutItemField.getTitle())));
 	}
 
 	private void addNavigationButtonColumn(final String navigationButtonLabel,
 			final NavigationButtonCell navigationButtonCell) {
 
 		navigationButtonColumn = new Column<DataItem[], String>(navigationButtonCell) {
 			@Override
 			public String getValue(final DataItem[] row) {
 				if (row.length == 1 && row[0] == null)
 					// an empty row
 					return null;
 				return navigationButtonLabel;
 			}
 		};
 
 		// Firefox, Chrome, and Safari only support the span and width attributes of the col element so we need to set
 		// the alignment with code
 		navigationButtonColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 
 		cellTable.addColumn(navigationButtonColumn, "");
 
 		// the style name for the details column is set on the col element
 		cellTable.addColumnStyleName(cellTable.getColumnCount() - 1, "details");
 
 	}
 
 	/**
 	 * Sets the row count for the pager.
 	 */
 	public void setRowCount(final int rowCount) {
 		cellTable.setRowCount(rowCount);
 	}
 
 	public void hideNavigationButtons() {
 		if (navigationButtonColumn != null) {
 			cellTable.setColumnWidth(navigationButtonColumn, 0, Unit.PX);
 		}
 	}
 
 	/**
 	 * Gets the minimum number of rows the should be displayed. Empty rows will be added when the query returns fewer
 	 * rows than this minimum.
 	 * 
 	 * @return The minimum number of rows that should be displayed.
 	 */
 	public abstract int getMinNumVisibleRows();
 
 	public abstract int getNumNonEmptyRows();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.google.gwt.user.client.ui.Widget#onLoad()
 	 */
 	@Override
 	protected void onLoad() {
 
 		/*
 		 * Set the width of the navigation button column to be as small as possible.
 		 */
 		// The navigationButtonColumn width will be null if it hasn't been set. This indicates that the column width
 		// hasn't been disabled with the hideNavigationButtons() method or been set with this method. The width of the
 		// navigation button column shouldn't be changed once it's set.
 		if (navigationButtonColumn != null && cellTable.getColumnWidth(navigationButtonColumn) == null) {
 
 			// Use the NavigationButtonCell to get the button HTML and find the width. I'm doing this because the
 			// CellTable widget is highly dynamic and there's no way to guarantee that we can access the navigation
 			// button HTML by using the actual CellTable.
 			final String buttonLabel = navigationButtonColumn.getValue(new DataItem[2]); // a hack to get the button
 																							// label
 			final SafeHtmlBuilder buttonBuilder = new SafeHtmlBuilder();
 			navigationButtonColumn.getCell().render(null, buttonLabel, buttonBuilder);
 			Element navigationButton = new HTML(buttonBuilder.toSafeHtml()).getElement().getFirstChildElement();
 
 			// Calculate the width similar to Utils.getWidgetHeight().
 			final Document doc = Document.get();
 			navigationButton.getStyle().setVisibility(Visibility.HIDDEN);
 			doc.getBody().appendChild(navigationButton);
 			final int buttonWidth = navigationButton.getOffsetWidth();
 
 			// remove the div from the from the document
 			doc.getBody().removeChild(navigationButton);
 			navigationButton = null;
 
 			// set the width
 			if (buttonWidth > 0) {
 				cellTable.setColumnWidth(navigationButtonColumn, buttonWidth + 6, Unit.PX);
 				navigationButtonColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 			}
 		}
 	}
 
 }
