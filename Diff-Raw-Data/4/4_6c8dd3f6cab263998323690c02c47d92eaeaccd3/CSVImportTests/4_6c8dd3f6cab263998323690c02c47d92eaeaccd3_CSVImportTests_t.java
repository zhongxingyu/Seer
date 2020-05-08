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
 
 package org.sammelbox.importing;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.sammelbox.TestExecuter;
 import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
 import org.sammelbox.controller.filesystem.FileSystemLocations;
 import org.sammelbox.controller.filesystem.importing.CSVImporter;
 import org.sammelbox.controller.filesystem.importing.ImportException;
 import org.sammelbox.controller.managers.ConnectionManager;
 import org.sammelbox.model.database.DatabaseStringUtilities;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.operations.DatabaseOperations;
 import org.sammelbox.utilities.TestQueries;
 
 public class CSVImportTests {
 	public static final String CSV_TEST_FOLDER = 
 			System.getProperty("user.dir") + File.separatorChar + "test" + 
 					File.separatorChar + "testdata" + File.separatorChar + "import-test-data";
 	
 	public static final String TEST_CSV_1 = CSV_TEST_FOLDER + File.separatorChar + "myCollection1.csv";
 	public static final String TEST_CSV_2 =	CSV_TEST_FOLDER + File.separatorChar + "myCollection2.csv";
 	public static final String TEST_CSV_3 =	CSV_TEST_FOLDER + File.separatorChar + "myCollection3.csv";
 	public static final String TEST_CSV_4_FAULTY = CSV_TEST_FOLDER + File.separatorChar + "myCollection4Faulty.csv";
 	
 	public static final String PICTURE_A = CSV_TEST_FOLDER + File.separatorChar + "pictureA.png";
 	public static final String PICTURE_B = CSV_TEST_FOLDER + File.separatorChar + "pictureB.png";
 	public static final String PICTURE_C = CSV_TEST_FOLDER + File.separatorChar + "pictureC.png";
 	public static final String PICTURE_D = CSV_TEST_FOLDER + File.separatorChar + "pictureD.png";
 	public static final String PICTURE_E = CSV_TEST_FOLDER + File.separatorChar + "pictureE.png";
 	
 	private static final String IMPORT_ALBUM_NAME = "Imported Albums";
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 				
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		TestExecuter.resetTestHome();
 	}
 
 	@Before
 	public void setUp() {
 		TestExecuter.resetTestHome();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		ConnectionManager.closeConnection();
 	}
 
 	private void replaceImagePlaceholdersAndWriteToOutputFilePath(String originalCsvWithPlacholdersPath, String outputFilePath) {
 		File csvFileWithAbsolutImagePathPlaceholders = new File(outputFilePath);
 		if (csvFileWithAbsolutImagePathPlaceholders.exists()) {
 			csvFileWithAbsolutImagePathPlaceholders.delete();
 		}
 		
 		String csvWithPicturePlaceholders = FileSystemAccessWrapper.readFileAsString(originalCsvWithPlacholdersPath);
 		String csvWithoutPicturePlaceholders = csvWithPicturePlaceholders
 				                                      .replace("$PICTURE_A", PICTURE_A)
 														.replace("$PICTURE_B", PICTURE_B)
 														 .replace("$PICTURE_C", PICTURE_C)
 														  .replace("$PICTURE_D", PICTURE_D)
 														   .replace("$PICTURE_E", PICTURE_E);
 		
 		FileSystemAccessWrapper.writeToFile(csvWithoutPicturePlaceholders, outputFilePath);
 	}
 	
 	@Test
 	public void testCSVImport() {		
 		try {
 			String tmpCSVFilePath = FileSystemLocations.TEMP_DIR + File.separatorChar + "csvFileWithAbsolutImagePathPlaceholders.csv";
 			replaceImagePlaceholdersAndWriteToOutputFilePath(TEST_CSV_1, tmpCSVFilePath);
 			
 			CSVImporter.importCSV(IMPORT_ALBUM_NAME, tmpCSVFilePath, ";", "IMG", "!", true);
 			
 			assertTrue("After the simulation, the album table should not have been created", 
 					!TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generateTableName(IMPORT_ALBUM_NAME)));
 			
 			CSVImporter.importCSV(IMPORT_ALBUM_NAME, tmpCSVFilePath, ";", "IMG", "!", false);
 			
 			assertTrue("The table should now be present", 
 					TestQueries.isDatabaseTablePresent(DatabaseStringUtilities.generateTableName(IMPORT_ALBUM_NAME)));
 			assertTrue("There should be four items after the import", 
 					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName(IMPORT_ALBUM_NAME)) == 4);
 			assertTrue("The imported album should be a picture album",
 					DatabaseOperations.isPictureAlbum(IMPORT_ALBUM_NAME));
 			
 		} catch (DatabaseWrapperOperationException | ImportException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testCSVImport2() {		
 		try {
 			String tmpCSVFilePath = FileSystemLocations.TEMP_DIR + File.separatorChar + "csvFileWithAbsolutImagePathPlaceholders.csv";
 			replaceImagePlaceholdersAndWriteToOutputFilePath(TEST_CSV_2, tmpCSVFilePath);
 			
			CSVImporter.importCSV(IMPORT_ALBUM_NAME, tmpCSVFilePath, "#", "pics", "%", true);
			CSVImporter.importCSV(IMPORT_ALBUM_NAME, tmpCSVFilePath, "#", "pics", "%", false);
 			
 			assertTrue("There should be four items after the import", 
 					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName(IMPORT_ALBUM_NAME)) == 4);
 			assertTrue("The imported album should be a picture album",
 					DatabaseOperations.isPictureAlbum(IMPORT_ALBUM_NAME));	
 		} catch (DatabaseWrapperOperationException | ImportException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testCSVImport3() {		
 		try {
 			CSVImporter.importCSV(IMPORT_ALBUM_NAME, TEST_CSV_3, ";", true);
 			CSVImporter.importCSV(IMPORT_ALBUM_NAME, TEST_CSV_3, ";", false);
 			
 			assertTrue("The imported album is not a picture album",
 					!DatabaseOperations.isPictureAlbum(IMPORT_ALBUM_NAME));			
 			assertTrue("There should be three items after the import", 
 					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName(IMPORT_ALBUM_NAME)) == 3);
 			
 		} catch (DatabaseWrapperOperationException | ImportException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testCSVImport4Faulty() {		
 		try {
 			CSVImporter.importCSV(IMPORT_ALBUM_NAME, TEST_CSV_4_FAULTY, ";", "pics", "#", true);
 			
 			fail();
 		} catch (ImportException e) {
 			// We have a success if the third import file fails, since it contains a format error
 			return;
 		}
 	}
 }
