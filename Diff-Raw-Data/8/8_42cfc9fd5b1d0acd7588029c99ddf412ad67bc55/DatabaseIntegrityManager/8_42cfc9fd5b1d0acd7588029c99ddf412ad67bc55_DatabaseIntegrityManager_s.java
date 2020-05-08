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
 
 package org.sammelbox.controller.managers;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.UUID;
 
 import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
 import org.sammelbox.controller.filesystem.FileSystemLocations;
 import org.sammelbox.model.database.DatabaseStringUtilities;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class DatabaseIntegrityManager {
 	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseIntegrityManager.class);
 	/** The extension used on file names for autosaves */
 	private static final String AUTO_SAVE_EXTENSION = "autosave";
 	/** Regular expression describing the file name format including the extension of auto saves */
 	private static final String AUTO_SAVE_FILE_REGEX = "(\\w)+(\\u005F([0-9]+)+\\." + AUTO_SAVE_EXTENSION + ")$";
 	/** A prefix for those databases that have been corrupted */
 	static final String CORRUPT_DATABASE_SNAPSHOT_PREFIX = "corruptDatabaseSnapshot_";
 	/** The maximum amount of autosaves that can be stored until the existing autosaves are overwritten */
 	private static final int AUTO_SAVE_LIMIT = 5;
 	/** The last change time in milliseconds */
 	private static long lastChangeTimeStampInMillis = -1;
	private static final String SAVEPOINT = "SAVEPOINT";
	private static final String ROLLBACK_TO = "ROLLBACK TO";
	private static final String BACKUP_TO = "BACKUP_TO";
	private static final String RESTORE_FROM = "RESTORE FROM"; 
 	private static final String REGEX_BEGIN_OF_LINE = "^";
 	private static final String REGEX_END_OF_LINE = "$";
 	private static final String REGEX_OR = "|";
 	private static final String LOCK_FILE_REGEX = REGEX_BEGIN_OF_LINE + "\\.lock" + REGEX_END_OF_LINE;	
 	private static final String DATABASE_FILE_REGEX = REGEX_BEGIN_OF_LINE + FileSystemLocations.DATABASE_NAME + REGEX_END_OF_LINE;
 	private DatabaseIntegrityManager() {
 		// not needed
 	}
 	
 	/**
 	 * Creates a savepoint to which the database state can be rolled back to. A new transaction is started.
 	 * @return The name of the created savepoint or null in case of failure. The create savepoint should only be 
 	 * used in public methods to avoid overhead with nested savepoints. 
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public static String createSavepoint() throws DatabaseWrapperOperationException {
 		String savepointName = UUID.randomUUID().toString();
 	
 		try (PreparedStatement createSavepointStatement = ConnectionManager.getConnection().prepareStatement(
 				SAVEPOINT + DatabaseStringUtilities.encloseNameWithQuotes(savepointName));) {			
 			createSavepointStatement.execute();
 			return savepointName;
 		} catch (SQLException e) {
 			LOGGER.error("Creating the savepoint {} failed", savepointName);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 		}
 	}
 	
 	/**
 	 * Removes the savepoint from the internal stack such that it cannot be used for any future rollbacks.
 	 * If the transaction stack is empty all changes done since the creation of the saveopoint will be committed.
 	 * If the stack of transaction is not empty (i.e. inner transaction) then no changes are comitted but the savepoint
 	 * is removed nonetheless from the stack. The release should only be used in public methods to avoid ovehead
 	 * with nested savepoints. 
 	 * @param The name of the savepoint to be released. Must not be null or empty! 
 	 * @throws DatabaseWrapperOperationException If the errorstate within is ErrorWithDirtyState that means the release was not possible 
 	 */
 	public static void releaseSavepoint(String savepointName) throws DatabaseWrapperOperationException {
 	
 		if (savepointName == null || savepointName.isEmpty()) {
 			LOGGER.error("The savepoint could not be released since the name string is null or empty");
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE);
 		}
 		
 		try (PreparedStatement releaseSavepointStatement = ConnectionManager.getConnection().prepareStatement("RELEASE SAVEPOINT " + 
 				DatabaseStringUtilities.encloseNameWithQuotes(savepointName));) {			
 			releaseSavepointStatement.execute();
 		} catch (SQLException sqlEx) {
 			LOGGER.error("Releasing the savepoint {} failed", savepointName);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlEx);
 		}
 	}
 	
 	/**
 	 * The rollback should only be used in public methods to avoid overhead with nested savepoints. 
 	 * @param savepointName
 	 * * @throws DatabaseWrapperOperationException If the error state within is ErrorWithDirtyState that means the release was not possible
 	 */
 	public static void rollbackToSavepoint(String savepointName) throws DatabaseWrapperOperationException {
 	
 		if (savepointName == null || savepointName.isEmpty()){
 			LOGGER.error("The savepoint could not be rolledback to since the name string is null or empty");
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE);
 		}	
 	
 		try (PreparedStatement rollbackToSavepointStatement = ConnectionManager.getConnection().prepareStatement(
 				ROLLBACK_TO + SAVEPOINT + DatabaseStringUtilities.encloseNameWithQuotes(savepointName))){
 			rollbackToSavepointStatement.execute();
 		} catch (SQLException sqlEx) {
 			LOGGER.error("Rolling back the savepoint {} failed", savepointName);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlEx);
 		}
 	}
 	
 	/**
 	 * Backs the database entries along the properties and pictures up to the specified file.
 	 * @param filePath The path ending with the file name under which the backup will be stored.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public static void backupToFile(String filePath) throws DatabaseWrapperOperationException {
 		// create temporary directory which includes backup files
 		String tempDirName = java.util.UUID.randomUUID().toString();
 		File tempDir = new File(System.getProperty("user.home") + File.separator, tempDirName);
 		if (!tempDir.exists() && !tempDir.mkdir()) {
 			LOGGER.error("Could not create temp directory");
 		}
 	
 		// backup home directory
 		File tempAppDataDir = new File(tempDir.getPath());
 		File sourceAppDataDir = new File(FileSystemLocations.getActiveHomeDir());
 		try {
 			String excludeRegex = LOCK_FILE_REGEX + REGEX_OR + DATABASE_FILE_REGEX; 
 			FileSystemAccessWrapper.copyDirectory(sourceAppDataDir, tempAppDataDir, excludeRegex);
 		} catch (IOException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE,e);
 		}
 	
 		// backup database to file
 		try (Statement statement = ConnectionManager.getConnection().createStatement()){				
 			statement.executeUpdate(BACKUP_TO  + "'" + tempDir.getPath() + File.separatorChar + FileSystemLocations.DATABASE_TO_RESTORE_NAME + "'");
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE,e);
 		}
 	
 		// zip the whole temp folder
 		FileSystemAccessWrapper.zipFolderToFile(tempDir.getPath(), filePath);
 	
 		// delete temp folder
 		FileSystemAccessWrapper.deleteDirectoryRecursively(tempDir);
 	}
 	
 	/**
 	 * Restores the database entries along the properties and pictures from the specified backup file
 	 * @param filePath The path of the file ending with the file name from which the backup will be restored.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public static void restoreFromFile(String filePath) throws DatabaseWrapperOperationException {
 		FileSystemAccessWrapper.clearHomeDirectory();
 		FileSystemAccessWrapper.unzipFileToFolder(filePath, FileSystemLocations.getActiveHomeDir());
 	
 		try (Statement statement = ConnectionManager.getConnection().createStatement()) {			
 			statement.executeUpdate(RESTORE_FROM + " '" + FileSystemLocations.getDatabaseRestoreFile() + "'");
 			try {
 				DatabaseIntegrityManager.lastChangeTimeStampInMillis = DatabaseIntegrityManager.extractTimeStamp(new File(filePath));
 			} catch (DatabaseWrapperOperationException e) {
 				DatabaseIntegrityManager.lastChangeTimeStampInMillis = System.currentTimeMillis();
 			}
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE,e);
 		}
 	
 		if (!FileSystemAccessWrapper.deleteDatabaseRestoreFile()) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE);
 		}
 	
 		if (!FileSystemAccessWrapper.updateSammelboxFileStructure()) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE);
 		}
 	
 		if (!FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.getConnection())) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE);
 		}
 		
 		// Update timestamp
 		DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
 	}
 	
 	/**
 	 * Gets the time stamp when the last change to the database happened.
 	 * @return The time in milliseconds when the last change to the database occured. -1 If not initialized.
 	 */
 	public static long getLastDatabaseChangeTimeStamp() {
 		return DatabaseIntegrityManager.lastChangeTimeStampInMillis;
 	}
 	
 	public static void updateLastDatabaseChangeTimeStamp() {
 		DatabaseIntegrityManager.lastChangeTimeStampInMillis = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Gets the list of existing autosaves sorted by filename timestamp, newest to oldest.
 	 * @return List of files of previous autosaves. Empty list if none exist
 	 */
 	public static List<File> getAllAutoSaves() throws DatabaseWrapperOperationException{
 		List<File> autoSaves = FileSystemAccessWrapper.getAllMatchingFilesInHomeDirectory(DatabaseIntegrityManager.AUTO_SAVE_FILE_REGEX);
 		Collections.sort(autoSaves, new Comparator<File>() {
 			@Override
 			public int compare(File file1, File file2) {
 				try {
 					return Long.compare(DatabaseIntegrityManager.extractTimeStamp(file2),  DatabaseIntegrityManager.extractTimeStamp(file1));
 				} catch (DatabaseWrapperOperationException e) {
 					return -1;
 				}
 			}
 		});
 		
 		return autoSaves;
 	}
 	
 	/**
 	 * Extracts the database last change timestamp from the autosave file. Requires the correct format of the name.
 	 * @param autoSaveFile
 	 * @return A long integer representing the last change of the database.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	 static long extractTimeStamp(File autoSaveFile) throws DatabaseWrapperOperationException {	
 		String fileName;
 		try {
 			fileName = autoSaveFile.getCanonicalFile().getName();
 		} catch (IOException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 		}
 		
 		if (!fileName.matches(DatabaseIntegrityManager.AUTO_SAVE_FILE_REGEX)) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
 		}
 		
 		try {
 			return Long.parseLong(fileName.substring(fileName.indexOf('_') + 1, fileName.indexOf('.')));
 		} catch (NumberFormatException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 		}
 	}
 	
 	/**
 	 * Creates an automatic backup when the current state of the database is newer than the most recent autosave.  
 	 * To reduce the memory footprint of the backup (i.e. in case of a large amount of pictures) only the db file is
 	 * backed up. 
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public static void backupAutoSave() throws DatabaseWrapperOperationException {		
 		String programVersion = BuildInformationManager.instance().getApplicationName() 
 				+ "_" + BuildInformationManager.instance().getVersion()
 				+ "_" + BuildInformationManager.instance().getBuildTimeStamp();
 		String timeStamp = Long.toString(getLastDatabaseChangeTimeStamp());		
 	
 		String autoSaveFilePath = FileSystemLocations.getBackupDir() + 
 				File.separator + "PERIODICAL_BACKUP_" + programVersion + "_";
 	
 		List<File> previousAutoSaveList = getAllAutoSaves();
 	
 		if (previousAutoSaveList.isEmpty()) {
 			// When no changes were made then the timestamp is the current time
 			if (getLastDatabaseChangeTimeStamp() == -1) {
 				timeStamp = Long.toString(System.currentTimeMillis());
 			}
 			autoSaveFilePath = autoSaveFilePath + timeStamp + "." + DatabaseIntegrityManager.AUTO_SAVE_EXTENSION;
 			try {
 				FileSystemAccessWrapper.copyFile(new File(FileSystemLocations.getDatabaseFile()), new File(autoSaveFilePath));
 			} catch (IOException e) {
 				LOGGER.error("Autosave - backup failed");
 				throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
 			}
 		// Auto-saves detected
 		} else {
 			// No need to overwrite the last auto-save when no changes were made.
 			if (getLastDatabaseChangeTimeStamp() == -1) {
 				return;
 			}
 	
 			// Auto save limit reached, delete the oldest
 			if (previousAutoSaveList.size() >= DatabaseIntegrityManager.AUTO_SAVE_LIMIT) {
 				File oldestAutoSave = previousAutoSaveList.get(previousAutoSaveList.size()-1);
 				if (oldestAutoSave.exists() && !oldestAutoSave.delete()) {
 					LOGGER.error("Autosave - cannot delete old autosave");
 					throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE);
 				}
 			}
 			autoSaveFilePath = autoSaveFilePath + timeStamp + "." + DatabaseIntegrityManager.AUTO_SAVE_EXTENSION;
 	
 			try {
 				FileSystemAccessWrapper.copyFile(new File(FileSystemLocations.getDatabaseFile()), new File(autoSaveFilePath));
 			} catch (IOException e) {
 				LOGGER.error("Autosave - backup failed");
 				throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
 			}
 		}
 	}
 }
