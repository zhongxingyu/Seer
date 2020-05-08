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
 
 package org.glom.web.server;
 
 import java.beans.PropertyVetoException;
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.Locale;
 import java.util.Properties;
 
 import org.glom.libglom.Document;
 import org.glom.libglom.Field;
 import org.glom.libglom.FieldFormatting;
 import org.glom.libglom.FieldVector;
 import org.glom.libglom.Glom;
 import org.glom.libglom.LayoutFieldVector;
 import org.glom.libglom.LayoutGroupVector;
 import org.glom.libglom.LayoutItem;
 import org.glom.libglom.LayoutItemVector;
 import org.glom.libglom.LayoutItem_Field;
 import org.glom.libglom.NumericFormat;
 import org.glom.libglom.SortClause;
 import org.glom.libglom.SortFieldPair;
 import org.glom.libglom.StringVector;
 import org.glom.web.client.OnlineGlomService;
 import org.glom.web.shared.ColumnInfo;
 import org.glom.web.shared.GlomDocument;
 import org.glom.web.shared.GlomField;
 import org.glom.web.shared.LayoutListTable;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 import com.mchange.v2.c3p0.DataSources;
 
 @SuppressWarnings("serial")
 public class OnlineGlomServiceImpl extends RemoteServiceServlet implements OnlineGlomService {
 	private Document document = null;
 	private ComboPooledDataSource cpds = null;
 	// TODO implement locale
 	private final Locale locale = Locale.ROOT;
 	private boolean configured = false;
 
 	/*
 	 * This is called when the servlet is started or restarted.
 	 */
 	public OnlineGlomServiceImpl() {
 		Glom.libglom_init();
 	}
 
 	/*
 	 * The properties file can't be loaded in the constructor because the servlet is not yet initialised and
 	 * getServletContext() will return null. The work-around for this problem is to initialise the database and glom
 	 * document object when makes its first request.
 	 */
 	private void configureServlet() {
 		document = new Document();
 
 		Properties props = new Properties();
 		String propFileName = "/WEB-INF/OnlineGlom.properties";
 		try {
 			props.load(getServletContext().getResourceAsStream(propFileName));
 		} catch (IOException e) {
 			// TODO log fatal error, notify user of problem
 			e.printStackTrace();
 		}
 
 		File file = new File(props.getProperty("glomfile"));
 		document.set_file_uri("file://" + file.getAbsolutePath());
 		int error = 0;
 		@SuppressWarnings("unused")
 		boolean retval = document.load(error);
 		// TODO handle error condition
 
 		// load the jdbc driver
 		cpds = new ComboPooledDataSource();
 		try {
 			cpds.setDriverClass("org.postgresql.Driver");
 		} catch (PropertyVetoException e) {
 			// TODO log error, fatal error can't continue, user can be notified when db access doesn't work
 			e.printStackTrace();
 		}
 
 		cpds.setJdbcUrl("jdbc:postgresql://" + document.get_connection_server() + "/"
 				+ document.get_connection_database());
 		cpds.setUser(props.getProperty("dbusername"));
 		cpds.setPassword(props.getProperty("dbpassword"));
 		configured = true;
 	}
 
 	/*
 	 * This is called when the servlet is stopped or restarted.
 	 * 
 	 * @see javax.servlet.GenericServlet#destroy()
 	 */
 	@Override
 	public void destroy() {
 		Glom.libglom_deinit();
 		try {
 			if (configured)
 				DataSources.destroy(cpds);
 		} catch (SQLException e) {
 			// TODO log error, don't need to notify user because this is a clean up method
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * FIXME I think Swig is generating long on 64-bit machines and int on 32-bit machines - need to keep this constant
 	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
 	 */
 	private static int safeLongToInt(long l) {
 		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
 			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
 		}
 		return (int) l;
 	}
 
 	public GlomDocument getGlomDocument() {
 		if (!configured)
 			configureServlet();
 
 		GlomDocument glomDocument = new GlomDocument();
 
 		// get arrays of table names and titles, and find the default table index
 		StringVector tablesVec = document.get_table_names();
 
 		int numTables = safeLongToInt(tablesVec.size());
 		// we don't know how many tables will be hidden so we'll use half of the number of tables for the default size
 		// of the ArrayList
 		ArrayList<String> tableNames = new ArrayList<String>(numTables / 2);
 		ArrayList<String> tableTitles = new ArrayList<String>(numTables / 2);
 		boolean foundDefaultTable = false;
 		int visibleIndex = 0;
 		for (int i = 0; i < numTables; i++) {
 			String tableName = tablesVec.get(i);
 			if (!document.get_table_is_hidden(tableName)) {
 				tableNames.add(tableName);
 				// JNI is "expensive", the comparison will only be called if we haven't already found the default table
 				if (!foundDefaultTable && tableName.equals(document.get_default_table())) {
 					glomDocument.setDefaultTableIndex(visibleIndex);
 					foundDefaultTable = true;
 				}
 				tableTitles.add(document.get_table_title(tableName));
 				visibleIndex++;
 			}
 		}
 
 		// set everything we need
 		glomDocument.setTableNames(tableNames);
 		glomDocument.setTableTitles(tableTitles);
 		glomDocument.setTitle(document.get_database_title());
 
 		return glomDocument;
 	}
 
 	public LayoutListTable getLayoutListTable(String tableName) {
 		if (!configured)
 			configureServlet();
 
 		LayoutListTable tableInfo = new LayoutListTable();
 
 		// access the layout list
 		LayoutGroupVector layoutListVec = document.get_data_layout_groups("list", tableName);
 		ColumnInfo[] columns = null;
 		LayoutFieldVector layoutFields = new LayoutFieldVector();
 		if (layoutListVec.size() > 0) {
 			// a layout list is defined, we can use it to for the LayoutListTable
 			// TODO log warning when the layoutListVec.size() > 1 but still use the first list
 			LayoutItemVector layoutItemsVec = layoutListVec.get(0).get_items();
 
 			// find the defined layout list fields
 			int numItems = safeLongToInt(layoutItemsVec.size());
 			columns = new ColumnInfo[numItems];
 			for (int i = 0; i < numItems; i++) {
 				// TODO add support for other LayoutItems (Text, Image, Button)
 				LayoutItem item = layoutItemsVec.get(i);
 				LayoutItem_Field layoutItemField = LayoutItem_Field.cast_dynamic(item);
 				if (layoutItemField != null) {
 					layoutFields.add(layoutItemField);
 					FieldFormatting.HorizontalAlignment alignment = layoutItemField
 							.get_formatting_used_horizontal_alignment();
 					columns[i] = new ColumnInfo(layoutItemField.get_title_or_name(),
 							getColumnInfoHorizontalAlignment(alignment));
 				}
 			}
 		} else {
 			// no layout list is defined, use the table fields as the layout list
 			FieldVector fieldsVec = document.get_table_fields(tableName);
 
 			// find the fields to display in the layout list
 			int numItems = safeLongToInt(fieldsVec.size());
 			columns = new ColumnInfo[numItems];
 			for (int i = 0; i < numItems; i++) {
 				Field field = fieldsVec.get(i);
 				LayoutItem_Field layoutItemField = new LayoutItem_Field();
 				layoutItemField.set_full_field_details(field);
 				layoutFields.add(layoutItemField);
 				FieldFormatting.HorizontalAlignment alignment = layoutItemField
 						.get_formatting_used_horizontal_alignment();
 				columns[i] = new ColumnInfo(layoutItemField.get_title_or_name(),
 						getColumnInfoHorizontalAlignment(alignment));
 			}
 		}
 
 		tableInfo.setColumns(columns);
 
 		// get the size of the returned query for the pager

		// TODO Create a memory and time efficient way to get the row count. Right now the ResultSet is not being
		// returned in cursor mode because we're using TYPE_SCROLL_INSENSITIVE to able to seek to the last record of the
		// ResultSet. The PostgreSQL JDBC driver doesn't support moving around in the ResultSet when in cursor mode.
		// Here's the relevant PostgreSQL documentation:
		// http://jdbc.postgresql.org/documentation/83/query.html#query-with-cursor
		// An option might be to do a SELECT COUNT query using a method in libglom or a custom method in java-libglom to
		// build the query. If we do this, we wouldn't need to think about the two TODOs below.
		// See: https://bugzilla.gnome.org/show_bug.cgi?id=645110

 		// TODO since we're executing a query anyway, maybe we should return the rows that will be displayed on the
 		// first page

 		// TODO this code is really similar to code in getTableData, find a way to not duplicate the code
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		try {
 			// setup and execute the query
 			conn = cpds.getConnection();
 			st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			String query = Glom.build_sql_select_simple(tableName, layoutFields);
 			rs = st.executeQuery(query);
 
 			// get the number of rows in the query
 			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
 			rs.last();
 			tableInfo.setNumRows(rs.getRow());
 
 		} catch (SQLException e) {
 			// TODO log error
 			// we don't know how many rows are in the query
 			e.printStackTrace();
 			tableInfo.setNumRows(0);
 		} finally {
 			// cleanup everything that has been used
 			try {
 				rs.close();
 				st.close();
 				conn.close();
 			} catch (Exception e) {
 				// TODO log error
 				e.printStackTrace();
 			}
 		}
 
 		return tableInfo;
 	}
 
 	public ArrayList<GlomField[]> getTableData(String table, int start, int length) {
 		return getTableData(table, start, length, false, 0, false);
 	}
 
 	public ArrayList<GlomField[]> getSortedTableData(String table, int start, int length, int sortColumnIndex,
 			boolean isAscending) {
 		return getTableData(table, start, length, true, sortColumnIndex, isAscending);
 	}
 
 	private ArrayList<GlomField[]> getTableData(String table, int start, int length, boolean useSortClause,
 			int sortColumnIndex, boolean isAscending) {
 		if (!configured)
 			configureServlet();
 
 		// access the layout list using the defined layout list or the table fields if there's no layout list
 		LayoutGroupVector layoutListVec = document.get_data_layout_groups("list", table);
 		LayoutFieldVector layoutFields = new LayoutFieldVector();
 		SortClause sortClause = new SortClause();
 		if (layoutListVec.size() > 0) {
 			// a layout list is defined, we can use it to for the LayoutListTable
 			// TODO log warning when the layoutListVec.size() > 1 but still use the first list
 			LayoutItemVector layoutItemsVec = layoutListVec.get(0).get_items();
 
 			// find the defined layout list fields
 			int numItems = safeLongToInt(layoutItemsVec.size());
 			for (int i = 0; i < numItems; i++) {
 				// TODO add support for other LayoutItems (Text, Image, Button)
 				LayoutItem item = layoutItemsVec.get(i);
 				LayoutItem_Field layoutItemfield = LayoutItem_Field.cast_dynamic(item);
 				if (layoutItemfield != null) {
 					// use this field in the layout
 					layoutFields.add(layoutItemfield);
 
 					// create a sort clause if it's a primary key and we're not asked to sort a specific column
 					if (!useSortClause) {
 						Field details = layoutItemfield.get_full_field_details();
 						if (details != null && details.get_primary_key()) {
 							sortClause.addLast(new SortFieldPair(layoutItemfield, true)); // ascending
 						}
 					}
 				}
 			}
 		} else {
 			// no layout list is defined, use the table fields as the layout list
 			FieldVector fieldsVec = document.get_table_fields(table);
 
 			// find the fields to display in the layout list
 			int numItems = safeLongToInt(fieldsVec.size());
 			for (int i = 0; i < numItems; i++) {
 				Field field = fieldsVec.get(i);
 				LayoutItem_Field layoutItemField = new LayoutItem_Field();
 				layoutItemField.set_full_field_details(field);
 				layoutFields.add(layoutItemField);
 
 				// create a sort clause if it's a primary key and we're not asked to sort a specific column
 				if (!useSortClause) {
 					if (field.get_primary_key()) {
 						sortClause.addLast(new SortFieldPair(layoutItemField, true)); // ascending
 					}
 				}
 			}
 		}
 
 		// create a sort clause for the column we've been asked to sort
 		if (useSortClause) {
 			LayoutItem item = layoutFields.get(sortColumnIndex);
 			LayoutItem_Field field = LayoutItem_Field.cast_dynamic(item);
 			if (field != null)
 				sortClause.addLast(new SortFieldPair(field, isAscending));
 			// TODO: log error in the else condition
 		}
 
 		ArrayList<GlomField[]> rowsList = new ArrayList<GlomField[]>();
 		Connection conn = null;
 		Statement st = null;
 		ResultSet rs = null;
 		try {
			// Setup and execute the query. Special care needs to be take to ensure that the results will be based on a
			// cursor so that large amounts of memory are not consumed when the query retrieve a large amount of data.
			// Here's the relevant PostgreSQL documentation:
			// http://jdbc.postgresql.org/documentation/83/query.html#query-with-cursor
 			conn = cpds.getConnection();
			conn.setAutoCommit(false);
			st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(length);
			String query = Glom.build_sql_select_simple(table, layoutFields, sortClause) + " OFFSET " + start;
			// TODO Test memory usage before and after we execute the query that would result in a large ResultSet.
			// We need to ensure that the JDBC driver is in fact returning a cursor based result set that has a low
			// memory footprint. Check the difference between this value before and after the query:
			// Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
 			rs = st.executeQuery(query);
 
			// get the data we've been asked for
 			int rowCount = 0;
 			while (rs.next() && rowCount <= length) {
 				int layoutFieldsSize = safeLongToInt(layoutFields.size());
 				GlomField[] rowArray = new GlomField[layoutFieldsSize];
 				for (int i = 0; i < layoutFieldsSize; i++) {
 					// make a new GlomField to set the text and colours
 					rowArray[i] = new GlomField();
 
 					// get foreground and background colours
 					LayoutItem_Field field = layoutFields.get(i);
 					FieldFormatting formatting = field.get_formatting_used();
 					String fgcolour = formatting.get_text_format_color_foreground();
 					if (!fgcolour.isEmpty())
 						rowArray[i].setFGColour(convertGdkColorToHtmlColour(fgcolour));
 					String bgcolour = formatting.get_text_format_color_background();
 					if (!bgcolour.isEmpty())
 						rowArray[i].setBGColour(convertGdkColorToHtmlColour(bgcolour));
 
 					// convert field values are to strings based on the glom type
 					switch (field.get_glom_type()) {
 					case TYPE_TEXT:
 						String text = rs.getString(i + 1);
 						rowArray[i].setText(text != null ? text : "");
 						break;
 					case TYPE_BOOLEAN:
 						rowArray[i].setText(rs.getBoolean(i + 1) ? "TRUE" : "FALSE");
 						break;
 					case TYPE_NUMERIC:
 						// Take care of the numeric formatting before converting the number to a string.
 						NumericFormat numFormatGlom = formatting.getM_numeric_format();
 						// There's no isCurrency() method in the glom NumericFormat class so we're assuming that the
 						// number should be formatted as a currency if the currency code string is not empty.
 						String currencyCode = numFormatGlom.getM_currency_symbol();
 						NumberFormat numFormatJava = null;
 						boolean useGlomCurrencyCode = false;
 						if (currencyCode.length() == 3) {
 							// Try to format the currency using the Java Locales system.
 							try {
 								Currency currency = Currency.getInstance(currencyCode);
 								// Ignore the glom numeric formatting when a valid ISO 4217 currency code is being used.
 								int digits = currency.getDefaultFractionDigits();
 								numFormatJava = NumberFormat.getCurrencyInstance(locale);
 								numFormatJava.setCurrency(currency);
 								numFormatJava.setMinimumFractionDigits(digits);
 								numFormatJava.setMaximumFractionDigits(digits);
 							} catch (IllegalArgumentException e) {
 								// TODO: log warning
 								// The currency code is not this is not an ISO 4217 currency code.
 								// We're going to manually set the currency code and use the glom numeric formatting.
 								useGlomCurrencyCode = true;
 								numFormatJava = getJavaNumberFormat(numFormatGlom);
 							}
 						} else if (currencyCode.length() > 0) {
 							// The length of the currency code is > 0 and != 3; this is not an ISO 4217 currency code.
 							// We're going to manually set the currency code and use the glom numeric formatting.
 							useGlomCurrencyCode = true;
 							numFormatJava = getJavaNumberFormat(numFormatGlom);
 						} else {
 							// The length of the currency code is 0; the number is not a currency.
 							numFormatJava = getJavaNumberFormat(numFormatGlom);
 						}
 
 						// TODO: Do I need to do something with NumericFormat.get_default_precision() from libglom?
 
 						double number = rs.getDouble(i + 1);
 						if (number < 0) {
 							if (formatting.getM_numeric_format().getM_alt_foreground_color_for_negatives())
 								// overrides the set foreground colour
 								rowArray[i].setFGColour(convertGdkColorToHtmlColour(NumericFormat
 										.get_alternative_color_for_negatives()));
 						}
 
 						// Finally convert the number to text using the glom currency string if required.
 						if (useGlomCurrencyCode) {
 							rowArray[i].setText(currencyCode + " " + numFormatJava.format(number));
 						} else {
 							rowArray[i].setText(numFormatJava.format(number));
 						}
 						break;
 					case TYPE_DATE:
 						Date date = rs.getDate(i + 1);
 						if (date != null) {
 							DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
 							rowArray[i].setText(dateFormat.format(rs.getDate(i + 1)));
 						} else {
 							rowArray[i].setText("");
 						}
 						break;
 					case TYPE_TIME:
 						Time time = rs.getTime(i + 1);
 						if (time != null) {
 							DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
 							rowArray[i].setText(timeFormat.format(time));
 						} else {
 							rowArray[i].setText("");
 						}
 						break;
 					case TYPE_IMAGE:
 						byte[] image = rs.getBytes(i + 1);
 						if (image != null) {
 							// TODO implement field TYPE_IMAGE
 							rowArray[i].setText("Image (FIXME)");
 						} else {
 							rowArray[i].setText("");
 						}
 						break;
 					case TYPE_INVALID:
 					default:
 						// TODO log warning message
 						rowArray[i].setText("");
 						break;
 					}
 				}
 
 				// add the row of GlomFields to the ArrayList we're going to return and update the row count
 				rowsList.add(rowArray);
 				rowCount++;
 			}
 		} catch (SQLException e) {
 			// TODO: log error, notify user of problem
 			e.printStackTrace();
 		} finally {
 			// cleanup everything that has been used
 			try {
 				rs.close();
 				st.close();
 				conn.close();
 			} catch (Exception e) {
 				// TODO log error
 				e.printStackTrace();
 			}
 		}
 		return rowsList;
 	}
 
 	private NumberFormat getJavaNumberFormat(NumericFormat numFormatGlom) {
 		NumberFormat numFormatJava = NumberFormat.getInstance(locale);
 		if (numFormatGlom.getM_decimal_places_restricted()) {
 			int digits = safeLongToInt(numFormatGlom.getM_decimal_places());
 			numFormatJava.setMinimumFractionDigits(digits);
 			numFormatJava.setMaximumFractionDigits(digits);
 		}
 		numFormatJava.setGroupingUsed(numFormatGlom.getM_use_thousands_separator());
 		return numFormatJava;
 	}
 
 	/*
 	 * Converts a Gdk::Color (16-bits per channel) to an HTML colour (8-bits per channel) by disgarding the least
 	 * significant 8-bits in each channel.
 	 */
 	private String convertGdkColorToHtmlColour(String gdkColor) {
 		if (gdkColor.length() == 13)
 			return gdkColor.substring(0, 2) + gdkColor.substring(5, 6) + gdkColor.substring(9, 10);
 		else if (gdkColor.length() == 7)
 			// TODO: log warning because we're expecting a 13 character string
 			return gdkColor;
 		else
 			// TODO: log error
 			return "";
 	}
 
 	/*
 	 * This method converts a FieldFormatting.HorizontalAlignment to the equivalent ColumnInfo.HorizontalAlignment. The
 	 * need for this comes from the fact that the GWT HorizontalAlignment classes can't be used with RPC and there's no
 	 * easy way to use the java-libglom FieldFormatting.HorizontalAlignment enum with RPC. An enum indentical to
 	 * FieldFormatting.HorizontalAlignment is included in the ColumnInfo class.
 	 */
 	private ColumnInfo.HorizontalAlignment getColumnInfoHorizontalAlignment(
 			FieldFormatting.HorizontalAlignment alignment) {
 		int value = alignment.swigValue();
 		ColumnInfo.HorizontalAlignment[] columnInfoValues = ColumnInfo.HorizontalAlignment.class.getEnumConstants();
 		if (value < columnInfoValues.length && value >= 0)
 			return columnInfoValues[value];
 		// TODO: log error: value out of range, returning HORIZONTAL_ALIGNMENT_RIGHT
 		return columnInfoValues[FieldFormatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_RIGHT.swigValue()];
 	}
 
 }
