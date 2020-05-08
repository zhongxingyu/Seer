 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package org.sammelbox.model.database.operations;
 
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
import org.sammelbox.controller.GuiController;
 import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
 import org.sammelbox.controller.filters.ItemFieldFilter;
import org.sammelbox.controller.filters.MetaItemFieldFilter;
 import org.sammelbox.controller.managers.ConnectionManager;
 import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.GuiState;
 import org.sammelbox.model.album.AlbumItem;
 import org.sammelbox.model.album.AlbumItemPicture;
 import org.sammelbox.model.album.FieldType;
 import org.sammelbox.model.album.ItemField;
 import org.sammelbox.model.album.MetaItemField;
 import org.sammelbox.model.album.OptionType;
 import org.sammelbox.model.database.DatabaseStringUtilities;
 import org.sammelbox.model.database.QueryBuilder;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class UpdateOperations {
 	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateOperations.class);
 	
 	private static final int FIRST_PARAM_INDEX = 1;	
 	private static final int NEW_ALBUM_NAME_PARAM_INDEX = 1;
 	private static final int NEW_ALBUM_TABLE_NAME_PARAM_INDEX = 2;
 	private static final int PICTURE_ALBUM_FLAG_PARAM_INDEX = 3;
 	private static final int OLD_ALBUM_NAME_PARAM_INDEX = 4;
 	
 	private UpdateOperations() {
 		// use static methods
 	}
 	
 	static void renameAlbum(String oldAlbumName, String newAlbumName) throws DatabaseWrapperOperationException {		
 		String savepointName =  DatabaseIntegrityManager.createSavepoint();
 		try {
 			// Rename the album table
 			renameTable(oldAlbumName, newAlbumName);
 			
 			// Rename the type info table		
 			String oldTypeInfoTableName = DatabaseStringUtilities.generateTypeInfoTableName(oldAlbumName);
 			String newTypeInfoTableName = DatabaseStringUtilities.generateTypeInfoTableName(newAlbumName);
 			renameTable(oldTypeInfoTableName, newTypeInfoTableName);
 			
 			// Rename the picture table
 			String oldPictureTableName = DatabaseStringUtilities.generatePictureTableName(oldAlbumName);
 			String newPictureTableName = DatabaseStringUtilities.generatePictureTableName(newAlbumName);
 			renameTable(oldPictureTableName, newPictureTableName);
 			
 			// Rename the picture folder
 			FileSystemAccessWrapper.renameAlbumPictureFolder(oldAlbumName, newAlbumName);
 			
 			// Change the entry in the album master table. OptionType.UNKNOWN indicates no change of the picture storing 
 			updateAlbumInAlbumMasterTable(oldAlbumName, newAlbumName, OptionType.UNKNOWN);			
 	
 			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
 		} catch (DatabaseWrapperOperationException e) {
 			if (e.getErrorState().equals(DBErrorState.ERROR_DIRTY_STATE)) {
 				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 			}
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);
 		}
 	}
 	
 	/**
 	 * Renames a table. All referenced columns and indices
 	 * @param oldTableName The name of the table to be renamed.
 	 * @param newTableName The new name of the table.
 	 * @return True if the operation was successful, false otherwise.
 	 */
 	private static boolean renameTable(String oldTableName, String newTableName) {
 		boolean success = true;
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("ALTER TABLE ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(oldTableName)));
 		sb.append(" RENAME TO ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(newTableName)));
 		String renameTableSQLString = sb.toString();
 
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(renameTableSQLString);) {
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			success = false;
 		}
 		
 		return success;
 	}
 	
 	static void renameAlbumItemField(String albumName, MetaItemField oldMetaItemField, MetaItemField newMetaItemField) throws DatabaseWrapperOperationException {
 
 		// Check if the specified columns exists.
 		List<MetaItemField> metaInfos = QueryOperations.getAllAlbumItemMetaItemFields(albumName);
 		if (!metaInfos.contains(oldMetaItemField) || oldMetaItemField.getType().equals(FieldType.ID)) {
 			if (metaInfos.contains(new MetaItemField(oldMetaItemField.getName(), oldMetaItemField.getType(), !oldMetaItemField.isQuickSearchable()))){
 				LOGGER.error("The specified meta item field's quicksearch flag is not set appropriately!");
 			} else {
 				LOGGER.error("The specified meta item field is not part of the album");
 			}
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
 		}
 		
 		String savepointName = DatabaseIntegrityManager.createSavepoint();		
 		try {
 			// Backup the old data in java objects
 			List<AlbumItem> albumItems = QueryOperations.getAlbumItems(QueryBuilder.createSelectStarQuery(albumName));
 
 			// Create the new table pointing to new typeinfo
 			boolean hasPictureField = QueryOperations.isPictureAlbum(albumName);
 			List<MetaItemField> newFields = QueryOperations.getAlbumItemFieldNamesAndTypes(albumName);
 			newFields = renameFieldInMetaItemList(oldMetaItemField, newMetaItemField, newFields);
 		
 			// Drop the old table + typeTable
 			DeleteOperations.dropTable(albumName);
 			DeleteOperations.dropTable(DatabaseStringUtilities.generateTypeInfoTableName(albumName));
 
 			// the following three columns are automatically created by createNewAlbumTable
 			newFields = removeFieldFromMetaItemList(new MetaItemField("id", FieldType.ID), newFields);
 			newFields = removeFieldFromMetaItemList(new MetaItemField(DatabaseConstants.TYPE_INFO_COLUMN_NAME, FieldType.ID), newFields);
 
 			CreateOperations.createNewAlbumTable(newFields, albumName, 
 					DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName)), hasPictureField);	
 
 			// Restore the old data from the java objects in the new tables [rename column]
 			renameFieldInAlbumItemList(oldMetaItemField, newMetaItemField, albumItems);
 		
 			// Re-add all album items
 			for (AlbumItem albumItem : albumItems) {
 				CreateOperations.addAlbumItem(albumItem, false, false);
 			}
 			
 			rebuildIndexForTable(albumName, newFields);
 			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
 		} catch (DatabaseWrapperOperationException e) {
 			if (e.getErrorState().equals(DBErrorState.ERROR_DIRTY_STATE)) {
 				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 				throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 			}
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);
 		}		
 	}
 
 	static void reorderAlbumItemField(String albumName, MetaItemField metaItemField, MetaItemField preceedingField) throws DatabaseWrapperOperationException {
 		// Check if the specified columns exists.
 		List<MetaItemField> metaInfos = QueryOperations.getAllAlbumItemMetaItemFields(albumName);
 		if (!metaInfos.contains(metaItemField)) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
 		}
 
 		String savepointName = DatabaseIntegrityManager.createSavepoint();
 		try {
 			// Backup the old data in java objects
 			List<AlbumItem> albumItems = QueryOperations.getAlbumItems(QueryBuilder.createSelectStarQuery(albumName));
 			// Create the new table pointing to new typeinfo
 			boolean hasPictureField = QueryOperations.isPictureAlbum(albumName);
 			List<MetaItemField> newFields = QueryOperations.getAlbumItemFieldNamesAndTypes(albumName);
 			newFields = reorderFieldInMetaItemList(metaItemField, preceedingField, newFields);
 
 			// Drop the old table + typeTable
 			DeleteOperations.dropTable(DatabaseStringUtilities.generateTableName(albumName));
 			DeleteOperations.dropTable(DatabaseStringUtilities.generateTypeInfoTableName(albumName));
 
 			// Create the new table pointing to new typeinfo
 			CreateOperations.createNewAlbumTable(newFields, albumName, 
 					DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName)), hasPictureField);
 
 			// Restore the old data from the temporary tables in the new tables [reorder column]
 			List<AlbumItem> newAlbumItems = reorderFieldInAlbumItemList(metaItemField, preceedingField, albumItems);
 			// replace the empty picField with the saved raw PicField 
 			for (AlbumItem albumItem : newAlbumItems) {
 				albumItem.setAlbumName(albumName);
 				CreateOperations.addAlbumItem(albumItem, false, false);				
 			}
 
 			rebuildIndexForTable(albumName, newFields);
 			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
 		} catch (DatabaseWrapperOperationException e) {
 			if (e.getErrorState().equals(DBErrorState.ERROR_DIRTY_STATE)) {
 				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 				throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 			}
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);
 		}
 	}
 
 	static void updateQuickSearchable(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
 		String savepointName = DatabaseIntegrityManager.createSavepoint();
 		try {
 			List<String> quickSearchableColumnNames = 
 					QueryOperations.getIndexedColumnNames(DatabaseStringUtilities.generateTableName(albumName));
 			
 			if (metaItemField.isQuickSearchable() && !quickSearchableColumnNames.contains(metaItemField.getName())) {
 				// Enable for quicksearch feature
 				quickSearchableColumnNames.add(metaItemField.getName());			
 			} else if (!metaItemField.isQuickSearchable()){	
 				// Disable for quicksearch feature
 				quickSearchableColumnNames.remove(metaItemField.getName());
 			}
 			
 			// update index for album
 			DeleteOperations.dropIndex(albumName);
 			CreateOperations.createIndex(albumName, quickSearchableColumnNames);
 			
 			updateSchemaVersion(albumName);
 			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
 		} catch (DatabaseWrapperOperationException dwoe) {
 			if (dwoe.getErrorState().equals(DBErrorState.ERROR_DIRTY_STATE)) {
 				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 				throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, dwoe);
 			}
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);
 		}		
 	}
 	
 	/**
 	 * Removes a MetaItemField from a list by using an equals test. Returns the result in a new list.
 	 * @param metaItemField The field to be removed from the list.
 	 * @param fieldList The list of which the metaItemField will be removed.
 	 * @return The parameter fieldList with the specified field removed.
 	 */
 	static List<MetaItemField> removeFieldFromMetaItemList(MetaItemField metaItemField, final List<MetaItemField> fieldList) {
 		List<MetaItemField> newFieldList = fieldList;
 		newFieldList.remove(metaItemField);
 		return newFieldList; 
 	}
 
 	/**
 	 * Renames a metaItemField for all the fields in the list.Type changes are currently not supported. Returns the result in a new list.
 	 * @param oldMetaItemField The metaItemField to be renamed.
 	 * @param newMetaItemField The metaItemField containing the new name.
 	 * @param fieldList The list of fields to be renamed.
 	 * @return The list of renamed fields.
 	 */
 	private static List<MetaItemField> renameFieldInMetaItemList(MetaItemField oldMetaItemField, MetaItemField newMetaItemField, final List<MetaItemField> fieldList) {
 		List<MetaItemField> newFieldList = fieldList;
 		int index = newFieldList.indexOf(oldMetaItemField);
 		MetaItemField renameMetaItemField = newFieldList.get(index);
 		renameMetaItemField.setName(newMetaItemField.getName());
 		return newFieldList; 
 	}
 
 	/**
 	 * Moves the metaItemField after the specified moveAfterField. In case the latter is null, move to beginning of list. 
 	 * Returns the result in a new list.
 	 * @param metaItemField The field to be moved to a new position.
 	 * @param precedingField The field which will precede the field in the new ordering. If null the field will be inserted at the beginning
 	 * of the list.
 	 * @param fieldList List of fields containing the field in the old ordering.
 	 * @return The list of fields in the new ordering.
 	 */
 	private static List<MetaItemField> reorderFieldInMetaItemList(MetaItemField metaItemField, MetaItemField precedingField, final List<MetaItemField> fieldList) {
 		List<MetaItemField> newFieldList = fieldList;
 		newFieldList.remove(metaItemField);
 		if (precedingField == null) {
 			newFieldList.add(0,metaItemField);
 		} else {
 
 			int insertAfterIndex = newFieldList.indexOf(precedingField);
 			if (insertAfterIndex==-1) {
 				newFieldList.add(metaItemField);
 			}else {
 				newFieldList.add(insertAfterIndex+1, metaItemField);
 			}
 		}
 		return newFieldList; 
 	}
 	
 	/**
 	 * Renames a field in all the album items in the list. Returns the result in a new list.
 	 * @param oldMetaItemField The metaItemField to be renamed.
 	 * @param newMetaItemField The metaItemField containing the new name.
 	 * @param albumList The list of album items whose field is to be renamed.
 	 * @return The new list of album items.
 	 */
 	private static List<AlbumItem> renameFieldInAlbumItemList(MetaItemField oldMetaItemField, MetaItemField newMetaItemField, final List<AlbumItem> albumList) {
 		List<AlbumItem> newAlbumItemList = albumList;
 		for (AlbumItem albumItem: newAlbumItemList) {
 			albumItem.renameField(oldMetaItemField, newMetaItemField);
 		}
 		return newAlbumItemList; 
 	}
 
 	/**
 	 * Moves a field of an album item to a new position. Returns the result in a new list. 
 	 * @param metaItemField The field to be moved.
 	 * @param precedingField The field which will precede the specified item after the reordering. If null the item will be moved to the top
 	 * of the list. 
 	 * @param albumList The list of album items in their original ordering.
 	 * @return The list of album items in their new order. Content remains untouched.
 	 */
 	private static List<AlbumItem> reorderFieldInAlbumItemList(MetaItemField metaItemField, MetaItemField precedingField, final List<AlbumItem> albumList) {
 		List<AlbumItem> newAlbumItemList = albumList;
 		for (AlbumItem albumItem: newAlbumItemList) {
 			albumItem.reorderField(metaItemField, precedingField);
 		}
 		return newAlbumItemList; 
 	}
 	
 	static void appendNewAlbumField(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
 		if (metaItemField == null || metaItemField.getType().equals(FieldType.ID) 
 				|| !QueryOperations.isItemFieldNameAvailable(albumName, metaItemField.getName())) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
 		}
 
 		String savepointName = DatabaseIntegrityManager.createSavepoint();
 		try {
 			appendNewTableColumn(albumName, metaItemField);
 			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
 		} catch (DatabaseWrapperOperationException dwoe) {
 			if (dwoe.getErrorState().equals(DBErrorState.ERROR_DIRTY_STATE)) {
 				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 				throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, dwoe);
 			}
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);			
 		}
 	}
 	
 	/**
 	 * Appends a new column to the album table. This internal method does allows to add any type of column, even id and picture column.
 	 * An exception is that you cannot add an additional picture column to an table.
 	 * To prevent accidental corruption of the tables, perform checks in the enclosing methods.
 	 * @param albumName The name of the album to be modified.
 	 * @param metaItemField he metaItemFields to be appended to the album.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	private static void appendNewTableColumn(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
 		// Prepare the append column string for the main table.
 		StringBuilder sb = new StringBuilder("ALTER TABLE ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(
 				DatabaseStringUtilities.generateTableName(albumName)));
 		sb.append(" ADD COLUMN ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(metaItemField.getName()));
 		sb.append(" ");
 		sb.append(FieldType.TEXT.toDatabaseTypeString());
 
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())) {
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
 		}
 
 		updateTableColumnWithDefaultValue(DatabaseStringUtilities.generateTableName(albumName), metaItemField);
 
 		// Append and update column for type table.
 		appendNewTypeInfoTableColumn(albumName, metaItemField);
 
 		updateSchemaVersion(albumName);
 	}
 
 	static void setAlbumPictureFunctionality(String albumName, boolean albumPicturesEnabled) throws DatabaseWrapperOperationException {
 		String savepointName = DatabaseIntegrityManager.createSavepoint();
 		
 		if (albumPicturesEnabled && QueryOperations.isPictureAlbum(albumName)) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, "Album " + albumName + " already contains pictures");
 		}
 
 		try {
 			if (albumPicturesEnabled) {			
 				updateAlbumInAlbumMasterTable(albumName, albumName, OptionType.YES);
 			} else {
 				updateAlbumInAlbumMasterTable(albumName, albumName, OptionType.NO);
 				DeleteOperations.clearPictureTable(albumName);
 			}
 		} catch ( DatabaseWrapperOperationException e) {
 			if (e.getErrorState().equals(DBErrorState.ERROR_DIRTY_STATE)) {
 				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 			}
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);
 		}
 	}
 	
 	/**
 	 * Appends a new column to the typeInfoTable. Necessary when the main table is altered to have additional columns.
 	 * @param tableName The name of the table to which the column belongs.
 	 * @param metaItemField The metadata of the new column.
 	 * @throws SQLException Exception thrown if any part of the operation fails.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	private static void appendNewTypeInfoTableColumn(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
 		String typeInfoTableName = DatabaseStringUtilities.generateTypeInfoTableName(albumName);
 		String columnName = DatabaseStringUtilities.encloseNameWithQuotes(metaItemField.getName());
 		// Prepare the append column string for the type table.
 		StringBuilder sb = new StringBuilder("ALTER TABLE ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(typeInfoTableName));
 		sb.append(" ADD COLUMN ");
 		sb.append(columnName);
 		sb.append(" TEXT");
 
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())) {
 			preparedStatement.executeUpdate();					
 		} catch (SQLException sqlEx) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlEx);
 		}
 		
 		sb.delete(0,sb.length());
 		sb.append("UPDATE ");
 		sb.append(typeInfoTableName);
 		sb.append(" SET ");
 		sb.append(columnName);
 		sb.append(" = ?");
 		
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())){
 			preparedStatement.setString(1, metaItemField.getType().toString());
 			preparedStatement.executeUpdate();
 		}catch (SQLException sqlEx) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlEx);
 		}		
 
 		updateSchemaVersion(albumName);
 	}
 
 	/**
 	 * Helper method which adds an entry to the typeInfo table to indicate the types used in the main table and updates the 
 	 * schema version UUID if properly included in the metafields.
 	 * @param item the item describing the newly created main table. Making up the content of the typeInfoTable.
 	 * @return True if the operation was successful. False otherwise.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	static void addTypeInfo(String typeInfoTableName, List<MetaItemField> metafields) throws DatabaseWrapperOperationException {
 		StringBuilder sb = new StringBuilder("INSERT INTO ");
 		sb.append(typeInfoTableName);
 		sb.append(" ( ");
 
 		// The 'while iterator loop' is used here because it is cheaper and more reliable than a foreach
 		// to add commas ',' in between elements
 		Iterator<MetaItemField> it = metafields.iterator();		
 		while(it.hasNext()) {
 			String fieldName = DatabaseStringUtilities.encloseNameWithQuotes(it.next().getName()); 
 			sb.append(fieldName);
 			if (it.hasNext())
 			{
 				sb.append(", ");
 			}
 		}
 		sb.append(" ) VALUES ( ");
 
 		it = metafields.iterator();		
 		while(it.hasNext()) {
 			it.next();
 			sb.append("?");
 			if (it.hasNext())
 			{
 				sb.append(", ");
 			}
 		}
 
 		sb.append(") ");
 		
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())) {			
 			// Replace the wildcard character '?' by the real type values
 			int parameterIndex = 1;
 			for (MetaItemField metaItemField : metafields){
 				String columnValue = metaItemField.getType().toString();
 
 				// Generate a new schema version UUID a table is created or modified.
 				if ( metaItemField.getType().equals(FieldType.UUID) && metaItemField.getName().equals(DatabaseConstants.SCHEMA_VERSION_COLUMN_NAME) ) {
 					columnValue = UUID.randomUUID().toString();
 				}
 
 				preparedStatement.setString(parameterIndex, columnValue);
 				parameterIndex++;
 			}
 
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);			
 		}
 	}
 
 	/**
 	 * Rebuilds the index for a table after an alter table operation. 
 	 * @param albumName The album to which these fields belong.
 	 * @param items The items containing the information of whether they are quicksearchable.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	static void rebuildIndexForTable(String albumName, List<MetaItemField> fields) throws DatabaseWrapperOperationException {
 		List<String> quicksearchColumnNames = new ArrayList<String>();
 		for (MetaItemField metaItemField : fields) {
 			if (metaItemField.isQuickSearchable()) {
 				quicksearchColumnNames.add(metaItemField.getName());
 			}
 		}
 		if (!quicksearchColumnNames.isEmpty()){
 			String savepointName = DatabaseIntegrityManager.createSavepoint();
 			try {
 				CreateOperations.createIndex(albumName, quicksearchColumnNames);
 			} catch (DatabaseWrapperOperationException e) {
 				if (e.getErrorState().equals(DBErrorState.ERROR_DIRTY_STATE)) {
 					DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 				}
 			}finally {
 				DatabaseIntegrityManager.releaseSavepoint(savepointName);
 			}
 		}
 	}
 	
 	static void updateAlbumItem(AlbumItem albumItem) throws DatabaseWrapperOperationException {
 		
 		// Updating items with no fields results in query with no arguments in the SET part of the query
 		if (ItemFieldFilter.getValidItemFields(albumItem.getFields()).isEmpty()) {
 			return;
 		}
 		
 		// Check if the item contains a albumName
 		if (albumItem.getAlbumName().isEmpty()) {
 			LOGGER.error("Album item {} has no albumName", albumItem);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
 		}
 
 		// Get the id and make sure the field exists
 		ItemField idField = albumItem.getField("id");
 
 		if (idField == null) {
 			LOGGER.error("The album item {} which should be updated has no id field", albumItem);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
 		}
 
 		// Build the string with place-holders '?'
 		StringBuilder sb = new StringBuilder("UPDATE ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(
 				DatabaseStringUtilities.generateTableName(albumItem.getAlbumName())));
 		sb.append(" SET ");
 
 		// Add each field to be update by the query
 		Iterator<ItemField> it = albumItem.getFields().iterator();
 		boolean firstAppended = true;
 		while (it.hasNext()) {
 			ItemField next = it.next();
 			// Exclude the id and fid fields
 			if (!next.getType().equals(FieldType.ID)){
 				if (!firstAppended) {
 					sb.append(", ");
 
 				}
 				sb.append(DatabaseStringUtilities.encloseNameWithQuotes(next.getName()));
 				sb.append("=? ");
 				firstAppended = false;				
 			}
 
 		}
 		sb.append("WHERE id=?");
 		
 		String savepointName =  DatabaseIntegrityManager.createSavepoint();		
 		
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())) {
 			// Replace the wildcards
 			int parameterIndex = 1;
 			for (ItemField next : albumItem.getFields()) {
 				// Exclude the id and id fields
 				if (!next.getType().equals(FieldType.ID)){
 					HelperOperations.setValueToPreparedStatement(preparedStatement, parameterIndex, next, albumItem.getAlbumName());
 					parameterIndex++;
 				}
 			}
 
 			// Replace wildcard char '?' in WHERE id=? clause
 			Long id = idField.getValue();
 			preparedStatement.setString(parameterIndex, id.toString());
 			preparedStatement.executeUpdate();
 
 			// Get those physical picture files that are currently still referenced
 			List<AlbumItemPicture> picturesBeforeUpdate = QueryOperations.getAlbumItemPictures(albumItem.getAlbumName(), albumItem.getItemID());
 			
 			// Remove those physical pictures that are no longer needed. However, the table records will remain for the moment
 			for (AlbumItemPicture stillReferencedPicture : picturesBeforeUpdate) {
 				boolean pictureIsNoLongerNeeded = true;
 				
 				for (AlbumItemPicture albumItemPicture : albumItem.getPictures()) {
 					if (stillReferencedPicture.getOriginalPictureName().equals(albumItemPicture.getOriginalPictureName())) {
 						pictureIsNoLongerNeeded = false;
 					}
 				}
 				
 				if (pictureIsNoLongerNeeded) {
 					FileSystemAccessWrapper.deleteFile(stillReferencedPicture.getThumbnailPicturePath());
 					FileSystemAccessWrapper.deleteFile(stillReferencedPicture.getOriginalPicturePath());
 				}
 			}
 			
 			// Update picture table by first deleting all pictures for this album item, and then rewriting the references
 			DeleteOperations.removeAllPicturesForAlbumItemFromPictureTable(albumItem);
 			for (AlbumItemPicture albumItemPicture : albumItem.getPictures()) {				
 				albumItemPicture.setAlbumItemID(albumItem.getItemID());
 				CreateOperations.addAlbumItemPicture(albumItemPicture);
 			}
 			
 			updateContentVersion(albumItem.getAlbumName(), id, UUID.randomUUID());
 			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
 		} catch (DatabaseWrapperOperationException e) {
 			if (e.getErrorState().equals(DBErrorState.ERROR_DIRTY_STATE)) {
 				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 			}
 		} catch (SQLException e) {
 			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);			
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);
 		}		
 	}
 	
 	static void updateContentVersion(String albumName, long itemID, UUID newUuid) throws DatabaseWrapperOperationException {	
 		String savepointName = DatabaseIntegrityManager.createSavepoint();
 		
 		StringBuilder sb = new StringBuilder("UPDATE ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName)));
 		sb.append(" SET ");
 		sb.append(DatabaseConstants.CONTENT_VERSION_COLUMN_NAME);
 		sb.append(" = ? ");
 		sb.append("WHERE id = ?");
 		
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())){			
 			preparedStatement.setString(1, newUuid.toString());
 			preparedStatement.setLong(2, itemID);
 			preparedStatement.executeUpdate();
 		} catch (SQLException sqlEx) {
 			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlEx);
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);
 		}
 	}
 
 	private static void updateSchemaVersion(String albumName) throws DatabaseWrapperOperationException  {
 		String savepointName = DatabaseIntegrityManager.createSavepoint();
 		
 		String typeInfoTableName = DatabaseStringUtilities.generateTypeInfoTableName(albumName);
 		StringBuilder sb = new StringBuilder("UPDATE ");
 		sb.append(typeInfoTableName);
 		sb.append(" SET ");
 		sb.append(DatabaseConstants.SCHEMA_VERSION_COLUMN_NAME);
 		sb.append(" = ?");
 		
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())) {		
 			preparedStatement.setString(1, UUID.randomUUID().toString());
 			preparedStatement.executeUpdate();
 		} catch (SQLException sqlEx) {
 			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlEx);
 		} finally {
 			DatabaseIntegrityManager.releaseSavepoint(savepointName);
 		}
 	}
 	
 	/**
 	 * Updates a table entry with a default value for the specific type of that column.
 	 * @param tableName The name of the table which will be updated.
 	 * @param columnMetaInfo The metadata specifying the name and type of the column entry to be updated.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	private static void updateTableColumnWithDefaultValue(String tableName, MetaItemField columnMetaInfo) throws DatabaseWrapperOperationException {		
 		String sqlString = " UPDATE " + tableName + 
 						   " SET " + DatabaseStringUtilities.encloseNameWithQuotes(columnMetaInfo.getName()) + "=?";
 		
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sqlString)) {						
 			
 			switch (columnMetaInfo.getType()) {
 			case TEXT: 
 				preparedStatement.setString(FIRST_PARAM_INDEX, (String) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case DECIMAL: 
 				preparedStatement.setDouble(FIRST_PARAM_INDEX, (Double) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case INTEGER: 
 				preparedStatement.setInt(FIRST_PARAM_INDEX, (Integer) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case DATE: 
 				preparedStatement.setDate(FIRST_PARAM_INDEX, (Date) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case TIME:
 				preparedStatement.setTime(FIRST_PARAM_INDEX, (Time) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case OPTION: 
 				String option = columnMetaInfo.getType().getDefaultValue().toString();
 				preparedStatement.setString(FIRST_PARAM_INDEX, option);
 				break;
 			case URL: 
 				String url = columnMetaInfo.getType().getDefaultValue().toString();
 				preparedStatement.setString(FIRST_PARAM_INDEX, url);
 				break;
 			case STAR_RATING: 
 				String rating = columnMetaInfo.getType().getDefaultValue().toString();
 				preparedStatement.setString(FIRST_PARAM_INDEX, rating);
 				break;
 			default:
 				break;
 			}
 			preparedStatement.execute();
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE,e);
 		}
 	}
 	
 	static void addNewAlbumToAlbumMasterTable(String albumName, boolean hasPictures) throws DatabaseWrapperOperationException {		
 		StringBuilder sb = new StringBuilder("INSERT INTO ");
 		sb.append(DatabaseConstants.ALBUM_MASTER_TABLE_NAME);
 		sb.append(" (");
 		sb.append(DatabaseConstants.ALBUMNAME_IN_ALBUM_MASTER_TABLE);
 		sb.append(", ");
 		sb.append(DatabaseConstants.ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);		
 		sb.append(", ");
 		sb.append(DatabaseConstants.HAS_PICTURES_COLUMN_IN_ALBUM_MASTER_TABLE);
 		sb.append(") VALUES( ?, ?, ?)");
 
 		String addAlbumQuery = sb.toString();
 
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(addAlbumQuery)){			
 			// New album name
 			preparedStatement.setString(NEW_ALBUM_NAME_PARAM_INDEX, DatabaseStringUtilities.removeQuotesEnclosingName(albumName));
 			// New album table name
 			preparedStatement.setString(NEW_ALBUM_TABLE_NAME_PARAM_INDEX, DatabaseStringUtilities.generateTableName(
 					DatabaseStringUtilities.removeQuotesEnclosingName(albumName)));
 			// New album contains picture flag
 			OptionType hasPictureFlag = hasPictures ? OptionType.YES : OptionType.NO ; 
 			preparedStatement.setString(PICTURE_ALBUM_FLAG_PARAM_INDEX, hasPictureFlag.toString());
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
 		}
 	}
 
 	static void removeAlbumFromAlbumMasterTable(String albumName) throws DatabaseWrapperOperationException  {
 		StringBuilder sb = new StringBuilder("DELETE FROM ");	
 		sb.append(DatabaseConstants.ALBUM_MASTER_TABLE_NAME);
 		sb.append(" WHERE ");
 		sb.append(DatabaseConstants.ALBUMNAME_IN_ALBUM_MASTER_TABLE);
 		sb.append(" = ?");
 
 		String unRegisterNewAlbumFromAlbumMasterableString = sb.toString();		
 
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(unRegisterNewAlbumFromAlbumMasterableString)){  			
 			// WHERE album name
 			preparedStatement.setString(FIRST_PARAM_INDEX, albumName);
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
 		}
 	}
 
 	/**
 	 * Updates the album name and the album table reference in the album master table
 	 * @param oldAlbumName the original album name which should be updated
 	 * @param newAlbumName the new album name
 	 * @param newHasPicturesFlagOptionType.UNKNOWN will be ignored. Yes and no will be set accordingly
 	 */
 	private static void updateAlbumInAlbumMasterTable(String oldAlbumName, String newAlbumName, OptionType newHasPicturesFlag) throws DatabaseWrapperOperationException  {
 
 		StringBuilder sb = new StringBuilder("UPDATE ");		
 		sb.append(DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ALBUM_MASTER_TABLE_NAME));
 		sb.append(" SET ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseConstants.ALBUMNAME_IN_ALBUM_MASTER_TABLE));
 		sb.append(" = ?, ");
 		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseConstants.ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE));
 		sb.append(" = ? ");
 		if (newHasPicturesFlag != OptionType.UNKNOWN) {
 			sb.append(", " + DatabaseStringUtilities.encloseNameWithQuotes(DatabaseConstants.HAS_PICTURES_COLUMN_IN_ALBUM_MASTER_TABLE));
 			sb.append(" = ? ");
 		}
 		sb.append("WHERE ");
 		sb.append(DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ALBUMNAME_IN_ALBUM_MASTER_TABLE));
 		sb.append(" = ?");
 
 		String unRegisterNewAlbumFromAlbumMasterableString = sb.toString();
 
 		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(unRegisterNewAlbumFromAlbumMasterableString);){			
 			// New album name
 			preparedStatement.setString(NEW_ALBUM_NAME_PARAM_INDEX, newAlbumName);
 			// New album table name
 			preparedStatement.setString(NEW_ALBUM_TABLE_NAME_PARAM_INDEX, DatabaseStringUtilities.generateTableName(newAlbumName));
 			if (newHasPicturesFlag != OptionType.UNKNOWN) {
 				// New hasPictures flag
 				preparedStatement.setString(PICTURE_ALBUM_FLAG_PARAM_INDEX, newHasPicturesFlag.toString());				
 				// Where old album name
 				preparedStatement.setString(OLD_ALBUM_NAME_PARAM_INDEX, oldAlbumName);
 			} else {		
 				// Where old album name
 				preparedStatement.setString(PICTURE_ALBUM_FLAG_PARAM_INDEX, oldAlbumName);
 			}
 			
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
 		}
 	}
 }
