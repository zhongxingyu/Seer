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
 
 import java.sql.DatabaseMetaData;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.util.Calendar;
 import java.util.TimeZone;
 import java.util.UUID;
 
 import org.sammelbox.controller.managers.ConnectionManager;
 import org.sammelbox.model.album.FieldType;
 import org.sammelbox.model.album.ItemField;
 import org.sammelbox.model.album.OptionType;
 import org.sammelbox.model.album.StarRating;
 import org.sammelbox.model.database.DatabaseStringUtilities;
 import org.sammelbox.model.database.QueryBuilder;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class HelperOperations {
 	private static final Logger LOGGER = LoggerFactory.getLogger(HelperOperations.class);
 	
 	private HelperOperations() {
 		// use static methods
 	}
 	
 	/**
 	 * This helper method sets a value based on type to a preparedStatement at the specified position
 	 * @param preparedStatement The JDBC prepared statement to which the value is to be set.
 	 * @param parameterIndex The index of the parameter to be set.
 	 * @param field The field containing the value to be set as well as the according meta-data.
 	 * @param albumName The name of the album to which the item of the field belongs.
 	 * @throws DatabaseWrapperOperationException Exception thrown if any part of the operation fails. 
 	 */
 	static void setValueToPreparedStatement(PreparedStatement preparedStatement, int parameterIndex,  ItemField field, String albumName) throws DatabaseWrapperOperationException {
 		try {
 			switch (field.getType()) {
 			case TEXT: 
 				// We don't want whitespaces at the beginning or end of the string
 				String text = field.getValue();
 				preparedStatement.setString(parameterIndex, text.trim());		
 				break;
 			case DECIMAL: 
 				Double real = field.getValue();
 				preparedStatement.setDouble(parameterIndex, real);		
 				break;
 			case DATE: 
 				Date date = field.getValue();
 				// Date needs to be truncated to be properly searchable 
 				Date truncatedDate = truncateTimePartOfDate(date);
 				preparedStatement.setDate(parameterIndex, truncatedDate, Calendar.getInstance(TimeZone.getTimeZone("UTC")));		
 				break;
 			case TIME: 
 				Time time = field.getValue();
 				preparedStatement.setTime(parameterIndex, time);		
 				break;
 			case OPTION: 
 				OptionType option = field.getValue();
 				// Textual representation of the enum is stored in the DB. 
 				preparedStatement.setString(parameterIndex, option.toString());		
 				break;
 			case URL: 
 				String	url = field.getValue();
 				preparedStatement.setString(parameterIndex, url);		
 				break;
 			case STAR_RATING: 
 				StarRating starRating = field.getValue();
 				preparedStatement.setInt(parameterIndex, starRating.getIntegerValue());		
 				break;
 			case ID:
 				Long longer = field.getValue();
 				preparedStatement.setLong(parameterIndex, longer);		
 				break;
 			case INTEGER: 
 				Integer	integer = field.getValue();
 				preparedStatement.setInt(parameterIndex, integer);		
 				break;
 			default:
 				break;
 			}			
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
 		}
 	}
 	
 	/** Treats the input date as a date with UTC time and truncates the time part. Truncating means the time part is set to all zeroes.
 	 *  If treated of a timezone time may differ from expected result!*/
 	private static Date truncateTimePartOfDate(Date date) {
 		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")); 
 		cal.setTime(date);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		long timeAsMillis = cal.getTimeInMillis();
 		Date truncatedDate = new Date(timeAsMillis);
 		
 		return truncatedDate;
 		
 	}
 
 	/**
 	 * A helper method to detect the collector FieldType using the type information in the separate typeInfo table.
 	 * @param tableName The name of the table to which the column belongs to. Do NOT escape!
 	 * @param columnName The name of the column whose type should be determined.
 	 * @return A FieldType expressing the type of the specified column in the resultSet.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	static FieldType detectDataType(String tableName, String columnName) throws DatabaseWrapperOperationException {
 		DatabaseMetaData dbmetadata = null;
 		
 		try {
 			dbmetadata = ConnectionManager.getConnection().getMetaData();
 		} catch (SQLException sqlEx) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, sqlEx);
 		}
 		
 		try (ResultSet dbmetars = dbmetadata.getImportedKeys(null, null, tableName)) {
 			// Get the primary and foreign keys
 			String primaryKey = dbmetars.getString(4);
 			String foreignKey = dbmetars.getString(8);
 			
 			if (columnName.equalsIgnoreCase(primaryKey) || columnName.equalsIgnoreCase(foreignKey)) {
 				return FieldType.ID; 
 			}
 			
 		} catch (SQLException sqlException) {
 			LOGGER.error("Could not detect fieldtype for column [" + columnName + "] in table [" + tableName + "]", sqlException);
 			return FieldType.TEXT;
 		}	
 
 		String dbtypeInfoTableName = DatabaseStringUtilities.generateTypeInfoTableName(tableName);
 		try (
 				Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 				ResultSet typeResultSet = statement.executeQuery(QueryBuilder.createSelectColumnQuery(dbtypeInfoTableName, columnName));) {			
 			return FieldType.valueOf(typeResultSet.getString(1));
 			
 		} catch (SQLException e) {
 			LOGGER.error("Could not detect fieldtype for column [" + columnName + "] in table [" + tableName + "]", e);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 		} catch (IllegalArgumentException e) {
 			LOGGER.error("Could not detect fieldtype for column [" + columnName + "] in table [" + tableName + "]", e);
 			return FieldType.TEXT;
 		}	
 		
 	}
 
 	static Object fetchFieldItemValue(ResultSet results, int columnIndex, FieldType type, String albumName) throws DatabaseWrapperOperationException {
 		Object value = null;
 		try {
 			switch (type) {
 			case ID:
 				value = results.getLong(columnIndex);
 				break;
 			case TEXT:
 				value = results.getString(columnIndex);
 				break;
 			case DECIMAL:
 				value = results.getDouble(columnIndex);
 				break;
 			case INTEGER:
 				value = results.getInt(columnIndex);
 				break;
 			case DATE:
 				value = results.getDate(columnIndex);
 				break;
 			case TIME:
 				value = results.getTime(columnIndex);
 				break;
 			case OPTION:
 				String optionValue = results.getString(columnIndex);
 				if (optionValue != null && !optionValue.isEmpty()) {
 					value =  OptionType.valueOf(optionValue);
 				} else {
 					LOGGER.error("Fetching option type for item field failed. Url string is unexpectantly null or empty");
 				}
 				break;
 			case URL:
 				String urlString = results.getString(columnIndex);
 				value =  urlString;
 				break;
 			case STAR_RATING:
 				value = StarRating.getByIntegerValue(results.getInt(columnIndex));
 				break;
 			case UUID:
 				value  = UUID.fromString(results.getString(columnIndex));
 				break;
 			default:
 				value = null;
 				break;
 			}
 			return value;
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 		}
 	}
 }
