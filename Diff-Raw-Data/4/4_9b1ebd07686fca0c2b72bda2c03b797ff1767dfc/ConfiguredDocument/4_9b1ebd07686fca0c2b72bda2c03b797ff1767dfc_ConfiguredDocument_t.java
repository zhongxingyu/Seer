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
 
 package org.glom.web.server;
 
 import java.beans.PropertyVetoException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang3.StringUtils;
 import org.glom.web.server.database.DetailsDBAccess;
 import org.glom.web.server.database.ListViewDBAccess;
 import org.glom.web.server.database.RelatedListDBAccess;
 import org.glom.web.server.database.RelatedListNavigation;
 import org.glom.web.server.libglom.Document;
 import org.glom.web.shared.DataItem;
 import org.glom.web.shared.DocumentInfo;
 import org.glom.web.shared.NavigationRecord;
 import org.glom.web.shared.Reports;
 import org.glom.web.shared.TypedDataItem;
 import org.glom.web.shared.libglom.CustomTitle;
 import org.glom.web.shared.libglom.Field;
 import org.glom.web.shared.libglom.Relationship;
 import org.glom.web.shared.libglom.Report;
 import org.glom.web.shared.libglom.Translatable;
 import org.glom.web.shared.libglom.layout.LayoutGroup;
 import org.glom.web.shared.libglom.layout.LayoutItem;
 import org.glom.web.shared.libglom.layout.LayoutItemField;
 import org.glom.web.shared.libglom.layout.LayoutItemPortal;
 import org.glom.web.shared.libglom.layout.UsesRelationship;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 /**
  * A class to hold configuration information for a given Glom document. This class retrieves layout information from
  * libglom and data from the underlying PostgreSQL database.
  */
 final class ConfiguredDocument {
 
 	private Document document;
 	private ComboPooledDataSource cpds;
 	private boolean authenticated = false;
 	private String documentID = "";
 	private String defaultLocaleID = "";
 
 	private static class LayoutLocaleMap extends Hashtable<String, List<LayoutGroup>> {
 		private static final long serialVersionUID = 6542501521673767267L;
 	};
 
 	private static class TableLayouts {
 		public LayoutLocaleMap listLayouts;
 		public LayoutLocaleMap detailsLayouts;
 	}
 
 	private static class TableLayoutsForLocale extends Hashtable<String, TableLayouts> {
 		private static final long serialVersionUID = -1947929931925049013L;
 
 		public LayoutGroup getListLayout(final String tableName, final String locale) {
 			final List<LayoutGroup> groups = getLayout(tableName, locale, false);
 			if (groups == null) {
 				return null;
 			}
 
 			if (groups.isEmpty()) {
 				return null;
 			}
 
 			return groups.get(0);
 		}
 
 		public List<LayoutGroup> getDetailsLayout(final String tableName, final String locale) {
 			return getLayout(tableName, locale, true);
 		}
 
 		public void setListLayout(final String tableName, final String locale, final LayoutGroup layout) {
 			final List<LayoutGroup> list = new ArrayList<LayoutGroup>();
 			list.add(layout);
 			setLayout(tableName, locale, list, false);
 		}
 
 		public void setDetailsLayout(final String tableName, final String locale, final List<LayoutGroup> layout) {
 			setLayout(tableName, locale, layout, true);
 		}
 
 		private List<LayoutGroup> getLayout(final String tableName, final String locale, final boolean details) {
 			final LayoutLocaleMap map = getMap(tableName, details);
 
 			if (map == null) {
 				return null;
 			}
 
 			return map.get(locale);
 		}
 
 		private LayoutLocaleMap getMap(final String tableName, final boolean details) {
 			final TableLayouts tableLayouts = get(tableName);
 			if (tableLayouts == null) {
 				return null;
 			}
 
 			LayoutLocaleMap map = null;
 			if (details) {
 				map = tableLayouts.detailsLayouts;
 			} else {
 				map = tableLayouts.listLayouts;
 			}
 
 			return map;
 		}
 
 		private LayoutLocaleMap getMapWithAdd(final String tableName, final boolean details) {
 			TableLayouts tableLayouts = get(tableName);
 			if (tableLayouts == null) {
 				tableLayouts = new TableLayouts();
 				put(tableName, tableLayouts);
 			}
 
 			LayoutLocaleMap map = null;
 			if (details) {
 				if (tableLayouts.detailsLayouts == null) {
 					tableLayouts.detailsLayouts = new LayoutLocaleMap();
 				}
 
 				map = tableLayouts.detailsLayouts;
 			} else {
 				if (tableLayouts.listLayouts == null) {
 					tableLayouts.listLayouts = new LayoutLocaleMap();
 				}
 
 				map = tableLayouts.listLayouts;
 			}
 
 			return map;
 		}
 
 		private void setLayout(final String tableName, final String locale, final List<LayoutGroup> layout,
 				final boolean details) {
 			final LayoutLocaleMap map = getMapWithAdd(tableName, details);
 			if (map != null) {
 				map.put(locale, layout);
 			}
 		}
 	}
 
 	private final TableLayoutsForLocale mapTableLayouts = new TableLayoutsForLocale();
 
 	@SuppressWarnings("unused")
 	private ConfiguredDocument() {
 		// disable default constructor
 	}
 
 	ConfiguredDocument(final Document document) throws PropertyVetoException {
 
 		// load the jdbc driver
 		cpds = createAndSetupDataSource(document);
 
 		this.document = document;
 	}
 
 	/**
 	 * @param document
 	 * @return
 	 */
 	private static ComboPooledDataSource createAndSetupDataSource(final Document document) {
 		final ComboPooledDataSource cpds = new ComboPooledDataSource();
 
 		// We don't support sqlite or self-hosting yet.
 		if ((document.getHostingMode() != Document.HostingMode.HOSTING_MODE_POSTGRES_CENTRAL)
 				&& (document.getHostingMode() != Document.HostingMode.HOSTING_MODE_POSTGRES_SELF)) {
 			// TODO: We allow self-hosting here, for testing,
 			// but maybe the startup of self-hosting should happen here.
 			Log.fatal("Error configuring the database connection." + " Only PostgreSQL hosting is supported.");
 			// FIXME: Throw exception?
 		}
 
 		try {
 			cpds.setDriverClass("org.postgresql.Driver");
 		} catch (final PropertyVetoException e) {
 			Log.fatal("Error loading the PostgreSQL JDBC driver."
 					+ " Is the PostgreSQL JDBC jar available to the servlet?", e);
 			return null;
 		}
 
 		// setup the JDBC driver for the current glom document
 		String jdbcURL = "jdbc:postgresql://" + document.getConnectionServer() + ":" + document.getConnectionPort();
 
 		String db = document.getConnectionDatabase();
 		if (StringUtils.isEmpty(db)) {
 			// Use the default PostgreSQL database, because ComboPooledDataSource.connect() fails otherwise.
 			db = "template1";
 		}
 		jdbcURL += "/" + db; // TODO: Quote the database name?
 
 		cpds.setJdbcUrl(jdbcURL);
 
 		return cpds;
 	}
 
 	/**
 	 * Sets the username and password for the database associated with the Glom document.
 	 * 
 	 * @return true if the username and password works, false otherwise
 	 */
 	boolean setUsernameAndPassword(final String username, final String password) throws SQLException {
 		cpds.setUser(username);
 		cpds.setPassword(password);
 
 		final int acquireRetryAttempts = cpds.getAcquireRetryAttempts();
 		cpds.setAcquireRetryAttempts(1);
 		Connection conn = null;
 		try {
 			// FIXME find a better way to check authentication
 			// it's possible that the connection could be failing for another reason
 			conn = cpds.getConnection();
 			authenticated = true;
 		} catch (final SQLException e) {
 			Log.info(Utils.getFileName(document.getFileURI()), e.getMessage());
 			Log.info(Utils.getFileName(document.getFileURI()),
 					"Connection Failed. Maybe the username or password is not correct.");
 			authenticated = false;
 		} finally {
 			if (conn != null) {
 				conn.close();
 			}
 			cpds.setAcquireRetryAttempts(acquireRetryAttempts);
 		}
 		return authenticated;
 	}
 
 	Document getDocument() {
 		return document;
 	}
 
 	ComboPooledDataSource getCpds() {
 		return cpds;
 	}
 
 	boolean isAuthenticated() {
 		return authenticated;
 	}
 
 	String getDocumentID() {
 		return documentID;
 	}
 
 	void setDocumentID(final String documentID) {
 		this.documentID = documentID;
 	}
 
 	String getDefaultLocaleID() {
 		return defaultLocaleID;
 	}
 
 	void setDefaultLocaleID(final String localeID) {
 		this.defaultLocaleID = localeID;
 	}
 
 	/**
 	 * @return
 	 */
 	DocumentInfo getDocumentInfo(final String localeID) {
 		final DocumentInfo documentInfo = new DocumentInfo();
 
 		// get arrays of table names and titles, and find the default table index
 		final List<String> tablesVec = document.getTableNames();
 
 		final int numTables = Utils.safeLongToInt(tablesVec.size());
 		// we don't know how many tables will be hidden so we'll use half of the number of tables for the default size
 		// of the ArrayList
 		final ArrayList<String> tableNames = new ArrayList<String>(numTables / 2);
 		final ArrayList<String> tableTitles = new ArrayList<String>(numTables / 2);
 		boolean foundDefaultTable = false;
 		int visibleIndex = 0;
 		for (int i = 0; i < numTables; i++) {
 			final String tableName = tablesVec.get(i);
 			if (!document.getTableIsHidden(tableName)) {
 				tableNames.add(tableName);
 				// JNI is "expensive", the comparison will only be called if we haven't already found the default table
 				if (!foundDefaultTable && tableName.equals(document.getDefaultTable())) {
 					documentInfo.setDefaultTableIndex(visibleIndex);
 					foundDefaultTable = true;
 				}
 				tableTitles.add(document.getTableTitle(tableName, localeID));
 				visibleIndex++;
 			}
 		}
 
 		// set everything we need
 		documentInfo.setTableNames(tableNames);
 		documentInfo.setTableTitles(tableTitles);
 		documentInfo.setTitle(document.getDatabaseTitle(localeID));
 
 		// Fetch arrays of locale IDs and titles:
 		final List<String> localesVec = document.getTranslationAvailableLocales();
 		final int numLocales = Utils.safeLongToInt(localesVec.size());
 		final ArrayList<String> localeIDs = new ArrayList<String>(numLocales);
 		final ArrayList<String> localeTitles = new ArrayList<String>(numLocales);
 		for (int i = 0; i < numLocales; i++) {
 			final String this_localeID = localesVec.get(i);
 			localeIDs.add(this_localeID);
 
 			// Use java.util.Locale to get a title for the locale:
 			final String[] locale_parts = this_localeID.split("_");
 			String locale_lang = this_localeID;
 			if (locale_parts.length > 0) {
 				locale_lang = locale_parts[0];
 			}
 			String locale_country = "";
 			if (locale_parts.length > 1) {
 				locale_country = locale_parts[1];
 			}
 
 			final Locale locale = new Locale(locale_lang, locale_country);
 			final String title = locale.getDisplayName(locale);
 			localeTitles.add(title);
 		}
 		documentInfo.setLocaleIDs(localeIDs);
 		documentInfo.setLocaleTitles(localeTitles);
 
 		return documentInfo;
 	}
 
 	/*
 	 * Gets the layout group for the list view using the defined layout list in the document or the table fields if
 	 * there's no defined layout group for the list view.
 	 */
 	private LayoutGroup getValidListViewLayoutGroup(final String tableName, final String localeID) {
 
 		// Try to return a cached version:
 		final LayoutGroup result = mapTableLayouts.getListLayout(tableName, localeID);
 		if (result != null) {
 			updateLayoutGroupExpectedResultSize(result, tableName);
 			return result;
 		}
 
 		final List<LayoutGroup> layoutGroupVec = document.getDataLayoutGroups("list", tableName);
 
 		final int listViewLayoutGroupSize = Utils.safeLongToInt(layoutGroupVec.size());
 		LayoutGroup libglomLayoutGroup = null;
 		if (listViewLayoutGroupSize > 0) {
 			// A list layout group is defined.
 			// We use the first group as the list.
 			if (listViewLayoutGroupSize > 1) {
 				Log.warn(documentID, tableName, "The size of the list layout group is greater than 1. "
 						+ "Attempting to use the first item for the layout list view.");
 			}
 
 			libglomLayoutGroup = layoutGroupVec.get(0);
 		} else {
 			// A list layout group is *not* defined; we are going make a LayoutGroup from the list of fields.
 			// This is unusual.
 			Log.info(documentID, tableName,
 					"A list layout is not defined for this table. Displaying a list layout based on the field list.");
 
 			final List<Field> fieldsVec = document.getTableFields(tableName);
 			libglomLayoutGroup = new LayoutGroup();
 			for (int i = 0; i < fieldsVec.size(); i++) {
 				final Field field = fieldsVec.get(i);
 				final LayoutItemField layoutItemField = new LayoutItemField();
 				layoutItemField.setFullFieldDetails(field);
 				libglomLayoutGroup.addItem(layoutItemField);
 			}
 		}
 
 		// TODO: Clone the group and change the clone, to discard unwanted information (such as translations)
 		// store some information that we do not want to calculate on the client side.
 
 		// Note that we don't use clone() here, because that would need clone() implementations
 		// in classes which are also used in the client code (though the clone() methods would
 		// not be used) and that makes the GWT java->javascript compilation fail.
 		final LayoutGroup cloned = (LayoutGroup) Utils.deepCopy(libglomLayoutGroup);
 		if (cloned != null) {
 			updateTopLevelListLayoutGroup(cloned, tableName, localeID);
 
 			// Discard unwanted translations so that getTitle(void) returns what we want.
 			updateTitlesForLocale(cloned, localeID);
 		}
 
 		// Store it in the cache for next time.
 		mapTableLayouts.setListLayout(tableName, localeID, cloned);
 
 		return cloned;
 	}
 
 	/**
 	 * @param libglomLayoutGroup
 	 */
 	private void updateTopLevelListLayoutGroup(final LayoutGroup layoutGroup, final String tableName,
 			final String localeID) {
 		final List<LayoutItem> layoutItemsVec = layoutGroup.getItems();
 
 		int primaryKeyIndex = -1;
 
 		final int numItems = Utils.safeLongToInt(layoutItemsVec.size());
 		for (int i = 0; i < numItems; i++) {
 			final LayoutItem layoutItem = layoutItemsVec.get(i);
 
 			if (layoutItem instanceof LayoutItemField) {
 				final LayoutItemField layoutItemField = (LayoutItemField) layoutItem;
 				final Field field = layoutItemField.getFullFieldDetails();
 				if ((field != null) && field.getPrimaryKey()) {
 					primaryKeyIndex = i;
 				}
 			}
 		}
 
 		// Set the primary key index for the table
 		if (primaryKeyIndex < 0) {
 			// Add a LayoutItemField for the primary key to the end of the item list in the LayoutGroup because it
 			// doesn't already contain a primary key.
 			Field primaryKey = null;
 			final List<Field> fieldsVec = document.getTableFields(tableName);
 			for (int i = 0; i < Utils.safeLongToInt(fieldsVec.size()); i++) {
 				final Field field = fieldsVec.get(i);
 				if (field.getPrimaryKey()) {
 					primaryKey = field;
 					break;
 				}
 			}
 
 			if (primaryKey != null) {
 				final LayoutItemField layoutItemField = new LayoutItemField();
 				layoutItemField.setName(primaryKey.getName());
 				layoutItemField.setFullFieldDetails(primaryKey);
 				layoutGroup.addItem(layoutItemField);
 				layoutGroup.setPrimaryKeyIndex(layoutGroup.getItems().size() - 1);
 				layoutGroup.setHiddenPrimaryKey(true);
 			} else {
 				Log.error(document.getDatabaseTitleOriginal(), tableName,
 						"A primary key was not found in the FieldVector for this table. Navigation buttons will not work.");
 			}
 		} else {
 			layoutGroup.setPrimaryKeyIndex(primaryKeyIndex);
 		}
 	}
 
 	private void updateLayoutGroupExpectedResultSize(final LayoutGroup layoutGroup, final String tableName) {
 		final ListViewDBAccess listViewDBAccess = new ListViewDBAccess(document, documentID, cpds, tableName,
 				layoutGroup);
 		layoutGroup.setExpectedResultSize(listViewDBAccess.getExpectedResultSize());
 	}
 
 	/**
 	 * 
 	 * @param tableName
 	 * @param quickFind
 	 * @param start
 	 * @param length
 	 * @param useSortClause
 	 * @param sortColumnIndex
 	 *            The index of the column to sort by, or -1 for none.
 	 * @param isAscending
 	 * @return
 	 */
 	ArrayList<DataItem[]> getListViewData(String tableName, final String quickFind, final int start, final int length,
 			final boolean useSortClause, final int sortColumnIndex, final boolean isAscending) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		// Get the LayoutGroup that represents the list view.
 		// TODO: Performance: Avoid calling this again:
 		final LayoutGroup libglomLayoutGroup = getValidListViewLayoutGroup(tableName, "" /* irrelevant locale */);
 
 		// Create a database access object for the list view.
 		final ListViewDBAccess listViewDBAccess = new ListViewDBAccess(document, documentID, cpds, tableName,
 				libglomLayoutGroup);
 
 		// Return the data.
 		return listViewDBAccess.getData(quickFind, start, length, sortColumnIndex, isAscending);
 	}
 
 	DataItem[] getDetailsData(String tableName, final TypedDataItem primaryKeyValue) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		final DetailsDBAccess detailsDBAccess = new DetailsDBAccess(document, documentID, cpds, tableName);
 
 		return detailsDBAccess.getData(primaryKeyValue);
 	}
 
 	/**
 	 * 
 	 * @param tableName
 	 * @param portal
 	 * @param foreignKeyValue
 	 * @param start
 	 * @param length
 	 * @param sortColumnIndex
 	 *            The index of the column to sort by, or -1 for none.
 	 * @param isAscending
 	 * @return
 	 */
 	ArrayList<DataItem[]> getRelatedListData(String tableName, final LayoutItemPortal portal,
 			final TypedDataItem foreignKeyValue, final int start, final int length, final int sortColumnIndex,
 			final boolean isAscending) {
 		if (portal == null) {
 			Log.error("getRelatedListData(): portal is null");
 			return null;
 		}
 
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		// Create a database access object for the related list
 		final RelatedListDBAccess relatedListDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
 				portal);
 
 		// Return the data
 		return relatedListDBAccess.getData(start, length, foreignKeyValue, sortColumnIndex, isAscending);
 	}
 
 	List<LayoutGroup> getDetailsLayoutGroup(String tableName, final String localeID) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		// Try to return a cached version:
 		final List<LayoutGroup> result = mapTableLayouts.getDetailsLayout(tableName, localeID);
 		if (result != null) {
 			updatePortalsExtras(result, tableName); // Update expected results sizes.
 			return result;
 		}
 
 		final List<LayoutGroup> listGroups = document.getDataLayoutGroups("details", tableName);
 
 		// Clone the group and change the clone, to discard unwanted information (such as translations)
 		// and to store some information that we do not want to calculate on the client side.
 
 		// Note that we don't use clone() here, because that would need clone() implementations
 		// in classes which are also used in the client code (though the clone() methods would
 		// not be used) and that makes the GWT java->javascript compilation fail.
 		final List<LayoutGroup> listCloned = new ArrayList<LayoutGroup>();
 		for (final LayoutGroup group : listGroups) {
 			final LayoutGroup cloned = (LayoutGroup) Utils.deepCopy(group);
 			if (cloned != null) {
 				listCloned.add(cloned);
 			}
 		}
 
 		updatePortalsExtras(listCloned, tableName);
 		updateFieldsExtras(listCloned, tableName);
 
 		// Discard unwanted translations so that getTitle(void) returns what we want.
 		updateTitlesForLocale(listCloned, localeID);
 
 		// Store it in the cache for next time.
 		mapTableLayouts.setDetailsLayout(tableName, localeID, listCloned);
 
 		return listCloned;
 	}
 
 	/**
 	 * @param result
 	 * @param tableName
 	 */
 	private void updatePortalsExtras(final List<LayoutGroup> listGroups, final String tableName) {
 		for (final LayoutGroup group : listGroups) {
 			updatePortalsExtras(group, tableName);
 		}
 
 	}
 
 	/**
 	 * @param result
 	 * @param tableName
 	 */
 	private void updateFieldsExtras(final List<LayoutGroup> listGroups, final String tableName) {
 		for (final LayoutGroup group : listGroups) {
 			updateFieldsExtras(group, tableName);
 		}
 	}
 
 	/**
 	 * @param result
 	 * @param tableName
 	 */
 	private void updateTitlesForLocale(final List<LayoutGroup> listGroups, final String localeID) {
 		for (final LayoutGroup group : listGroups) {
 			updateTitlesForLocale(group, localeID);
 		}
 	}
 
 	private void updatePortalsExtras(final LayoutGroup group, final String tableName) {
 		if (group instanceof LayoutItemPortal) {
 			final LayoutItemPortal portal = (LayoutItemPortal) group;
 			final String tableNameUsed = portal.getTableUsed(tableName);
 			updateLayoutGroupExpectedResultSize(portal, tableNameUsed);
 
			//Do not add a primary key field if there is already one:
			if(portal.getPrimaryKeyIndex() == -1 )
				return;

 			final Relationship relationship = portal.getRelationship();
 			if (relationship != null) {
 
 				// Cache the navigation information:
 				// layoutItemPortal.set_name(libglomLayoutItemPortal.get_relationship_name_used());
 				// layoutItemPortal.setTableName(relationship.get_from_table());
 				// layoutItemPortal.setFromField(relationship.get_from_field());
 
 				// get the primary key for the related list table
 				final String toTableName = relationship.getToTable();
 				if (!StringUtils.isEmpty(toTableName)) {
 
 					// get the LayoutItemField with details from its Field in the document
 					final List<Field> fields = document.getTableFields(toTableName); // TODO_Performance: Cache this.
 					for (final Field field : fields) {
 						// check the names to see if they're the same
 						if (field.getPrimaryKey()) {
 							final LayoutItemField layoutItemField = new LayoutItemField();
 							layoutItemField.setName(field.getName());
 							layoutItemField.setFullFieldDetails(field);
 							portal.addItem(layoutItemField);
 							portal.setPrimaryKeyIndex(portal.getItems().size() - 1);
 							portal.setHiddenPrimaryKey(true); // always hidden in portals
 							break;
 						}
 					}
 				}
 			}
 
 		}
 
 		final List<LayoutItem> childItems = group.getItems();
 		for (final LayoutItem item : childItems) {
 			if (item instanceof LayoutGroup) {
 				final LayoutGroup childGroup = (LayoutGroup) item;
 				updatePortalsExtras(childGroup, tableName);
 			}
 		}
 
 	}
 
 	private void updateFieldsExtras(final LayoutGroup group, final String tableName) {
 
 		final List<LayoutItem> childItems = group.getItems();
 		for (final LayoutItem item : childItems) {
 			if (item instanceof LayoutGroup) {
 				// Recurse:
 				final LayoutGroup childGroup = (LayoutGroup) item;
 				updateFieldsExtras(childGroup, tableName);
 			} else if (item instanceof LayoutItemField) {
 				final LayoutItemField field = (LayoutItemField) item;
 
 				// Set whether the field should have a navigation button,
 				// because it identifies a related record.
 				final String navigationTableName = document.getLayoutItemFieldShouldHaveNavigation(tableName, field);
 				if (navigationTableName != null) {
 					field.setNavigationTableName(navigationTableName);
 				}
 			}
 		}
 	}
 
 	private void updateTitlesForLocale(final LayoutGroup group, final String localeID) {
 
 		updateItemTitlesForLocale(group, localeID);
 
 		final List<LayoutItem> childItems = group.getItems();
 		for (final LayoutItem item : childItems) {
 
 			// Call makeTitleOriginal on all Translatable items and all special
 			// Translatable items that they use:
 			if (item instanceof LayoutItemField) {
 				final LayoutItemField layoutItemField = (LayoutItemField) item;
 
 				final Field field = layoutItemField.getFullFieldDetails();
 				if (field != null) {
 					field.makeTitleOriginal(localeID);
 				}
 
 				final CustomTitle customTitle = layoutItemField.getCustomTitle();
 				if (customTitle != null) {
 					customTitle.makeTitleOriginal(localeID);
 				}
 			}
 
 			updateItemTitlesForLocale(item, localeID);
 
 			if (item instanceof LayoutGroup) {
 				// Recurse:
 				final LayoutGroup childGroup = (LayoutGroup) item;
 				updateTitlesForLocale(childGroup, localeID);
 			}
 		}
 	}
 
 	private void updateItemTitlesForLocale(final LayoutItem item, final String localeID) {
 		if (item instanceof UsesRelationship) {
 			final UsesRelationship usesRelationship = (UsesRelationship) item;
 			final Relationship rel = usesRelationship.getRelationship();
 
 			if (rel != null) {
 				rel.makeTitleOriginal(localeID);
 			}
 
 			final Relationship relatedRel = usesRelationship.getRelatedRelationship();
 			if (relatedRel != null) {
 				relatedRel.makeTitleOriginal(localeID);
 			}
 		}
 
 		if (item instanceof Translatable) {
 			final Translatable translatable = item;
 			translatable.makeTitleOriginal(localeID);
 		}
 	}
 
 	/*
 	 * Gets the expected row count for a related list.
 	 */
 	int getRelatedListRowCount(String tableName, final LayoutItemPortal portal, final TypedDataItem foreignKeyValue) {
 		if (portal == null) {
 			Log.error("getRelatedListData(): portal is null");
 			return 0;
 		}
 
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		// Create a database access object for the related list
 		final RelatedListDBAccess relatedListDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
 				portal);
 
 		// Return the row count
 		return relatedListDBAccess.getExpectedResultSize(foreignKeyValue);
 	}
 
 	NavigationRecord getSuitableRecordToViewDetails(String tableName, final LayoutItemPortal portal,
 			final TypedDataItem primaryKeyValue) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 
 		final RelatedListNavigation relatedListNavigation = new RelatedListNavigation(document, documentID, cpds,
 				tableName, portal);
 
 		return relatedListNavigation.getNavigationRecord(primaryKeyValue);
 	}
 
 	LayoutGroup getListViewLayoutGroup(String tableName, final String localeID) {
 		// Validate the table name.
 		tableName = getTableNameToUse(tableName);
 		return getValidListViewLayoutGroup(tableName, localeID);
 	}
 
 	/**
 	 * Gets the table name to use when accessing the database and the document. This method guards against SQL injection
 	 * attacks by returning the default table if the requested table is not in the database or if the table name has not
 	 * been set.
 	 * 
 	 * @param tableName
 	 *            The table name to validate.
 	 * @return The table name to use.
 	 */
 	private String getTableNameToUse(final String tableName) {
 		if (StringUtils.isEmpty(tableName) || !document.getTableIsKnown(tableName)) {
 			return document.getDefaultTable();
 		}
 		return tableName;
 	}
 
 	/**
 	 * @param tableName
 	 * @param localeID
 	 * @return
 	 */
 	public Reports getReports(final String tableName, final String localeID) {
 		final Reports result = new Reports();
 
 		final List<String> names = document.getReportNames(tableName);
 
 		final int count = Utils.safeLongToInt(names.size());
 		for (int i = 0; i < count; i++) {
 			final String name = names.get(i);
 			final Report report = document.getReport(tableName, name);
 			if (report == null) {
 				continue;
 			}
 
 			final String title = report.getTitle(localeID);
 			result.addReport(name, title);
 		}
 
 		return result;
 	}
 }
