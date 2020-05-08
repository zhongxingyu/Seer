 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.splash.testing;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.services.AweProjectService;
 import org.amanzi.neo.services.nodes.AweProjectNode;
 import org.amanzi.neo.services.nodes.CellNode;
 import org.amanzi.neo.services.nodes.ColumnHeaderNode;
 import org.amanzi.neo.services.nodes.RowHeaderNode;
 import org.amanzi.neo.services.nodes.RubyProjectNode;
 import org.amanzi.neo.services.nodes.SplashDatabaseException;
 import org.amanzi.neo.services.nodes.SpreadsheetNode;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.splash.database.services.SpreadsheetService;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 /**
  * Tests for spreadsheet service: add, swap, delete operations for rows, columns and cells.
  * <p>
  *
  * </p>
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public class SpreadsheetServiceCellMovingTest {
     
     private static final String DATABASE_NAME = "neo_test";
     private static final String USER_HOME = "user.home";
     private static final String AMANZI_STR = ".amanzi";
     private static final String MAIN_DIRECTORY = "spsheet_serv_test";
     
     private static final String AWE_PROJECT_NAME = "";
     private static final String RUBY_PROJECT_NAME = "";
     private static final String SPREAD_SHEET_NAME = "";
     
     private static final String TEST_KEY_EMPTY = "empty.";
     private static final String TEST_KEY_EMPTY_NOT_ALL = "empty_not_all.";
     private static final String TEST_KEY_EMPTY_ONE = "empty_one.";
     private static final String TEST_KEY_AFTER_ALL = "after_all.";
     private static final String TEST_KEY_FILLED = "filled.";
     
     private static final String BUNDLE_PREFIX_INSERT_ROW = "test_spr-sheet-serv.insert_row.";
     private static final String BUNDLE_PREFIX_INSERT_COLUMN = "test_spr-sheet-serv.insert_column.";
     private static final String BUNDLE_PREFIX_DELETE_CELL = "test_spr-sheet-serv.delete_cell.";
     private static final String BUNDLE_PREFIX_DELETE_ROW = "test_spr-sheet-serv.delete_row.";
     private static final String BUNDLE_PREFIX_DELETE_COLUMN = "test_spr-sheet-serv.delete_column.";
     private static final String BUNDLE_PREFIX_SWAP_ROW = "test_spr-sheet-serv.swap_row.";
     private static final String BUNDLE_PREFIX_SWAP_COLUMN = "test_spr-sheet-serv.swap_column.";
     
     private static final String BUNDLE_SUB_KEY_EXEC_DATA = "exec_data";
     private static final String BUNDLE_SUB_KEY_FILL_DATA = "fill_data.";
     private static final String BUNDLE_SUB_KEY_CHECK_DATA = "check_data.";
     
     private static final String BUNDLE_KEY_SEPARATOR = ",";
     
     
     private static String mainDirectoryName;
     private static EmbeddedGraphDatabase neo;
     private static AweProjectService projectService;
     
     private SpreadsheetNode spreadsheet;
     private SpreadsheetService service;
     
     /**
      * Gets neo service.
      * @return EmbeddedNeo
      */
     public static EmbeddedGraphDatabase getNeo(){
         if (neo == null){
             neo = new EmbeddedGraphDatabase(getDbDirectoryName());
             NeoServiceProviderUi.initProvider(neo);
         }
         return neo;
     }
     
     /**
      * Initialize project service.
      */
     private static void initProjectService(){
         NeoServiceProviderUi.initProvider(getNeo());
         projectService = NeoCorePlugin.getDefault().getProjectService();
     }
     
     /**
      * Get name of data base directory.
      * (Create directory if it not exists)
      *
      * @return String
      */
     private static String getDbDirectoryName(){
         File dir = new File(mainDirectoryName,DATABASE_NAME);
         if(!dir.exists()){
             dir.mkdir();
         }
         return dir.getPath();
     }
     
     /**
      * Create new main directory.
      */
     protected static void initEmptyMainDirectory(){
         File dir = new File(getUserHome());
         if(!dir.exists()){
             dir.mkdir();
         }
         dir = new File(dir,AMANZI_STR);
         if(!dir.exists()){
             dir.mkdir();    
         }
         dir = new File(dir,MAIN_DIRECTORY);
         if(!dir.exists()){
             dir.mkdir();    
         }
         mainDirectoryName = dir.getPath();
     }
     
     /**
      * Get name of %USER_HOME% directory.
      *
      * @return String
      */
     private static String getUserHome() {
         return System.getProperty(USER_HOME);
     }
     
     /**
      * Delete main directory.
      */
     protected static void clearMainDirectory() {
         File dir = new File(getUserHome());
         if(dir.exists() && dir.isDirectory()){
             dir = new File(dir,AMANZI_STR);
             if(dir.exists() && dir.isDirectory()){
                 dir = new File(dir,MAIN_DIRECTORY);
                 if(dir.exists()){
                     if(dir.isDirectory()){
                         clearDirectory(dir);
                     }
                     dir.delete();
                 }
             }
         }
     }
     
     /**
      * Clear directory.
      * @param directory File (for clear)
      */
     protected static void clearDirectory(File directory){
         if(directory.exists()){
             for(File file : directory.listFiles()){
                 if(file.isDirectory()){
                     clearDirectory(file);
                 }
                 file.delete();
             }
         }
     }
     
     /**
      * Prepare operations for one test.
      */
     @Before
     public void prepare()throws SplashDatabaseException{
         clearMainDirectory();
         initEmptyMainDirectory();
         initProjectService();
         createSpreadsheet();
     }
     
     /**
      * Tests insert row into empty spreadsheet. 
      */
     @Test
     public void testInsertRowEmpty(){
         executeInserRow(TEST_KEY_EMPTY);
     }
     
     /**
      * Tests insert row after all filled ones.
      */
     @Test
     public void testInsertRowAfterAll(){
         executeInserRow(TEST_KEY_AFTER_ALL);
     }
     
     /**
      * Tests insert row between filled ones.
      */
     @Test
     public void testInsertRowBetween(){
         executeInserRow(TEST_KEY_FILLED);
     }
     
     /**
      * Tests insert column into empty spreadsheet.
      */
     @Test
     public void testInsertColumnEmpty(){
         executeInserCoulumn(TEST_KEY_EMPTY);
     }
     
     /**
      * Tests insert column after all filled ones.
      */
     @Test
     public void testInsertColumnAfterAll(){
         executeInserCoulumn(TEST_KEY_AFTER_ALL);
     }
     
     /**
      * Tests insert column between filled ones.
      */
     @Test
     public void testInsertColumnBetween(){
         executeInserCoulumn(TEST_KEY_FILLED);
     }
     
     /**
      * Tests delete cell in empty spreadsheet.
      */
     @Test
     public void testDeleteCellEmpty(){
         executeDeleteCell(TEST_KEY_EMPTY);
     }
     
     /**
      * Tests delete empty cell in not empty spreadsheet.
      */
     @Test
     public void testDeleteCellEmptyNotAll(){
         executeDeleteCell(TEST_KEY_EMPTY_NOT_ALL);
     }
     
     /**
      * Tests delete filled cell in spreadsheet.
      */
     @Test
     public void testDeleteCellFilled(){
         executeDeleteCell(TEST_KEY_FILLED);
     }
     
     /**
      * Tests delete row in empty spreadsheet.
      */
     @Test
     public void testDeleteRowEmpty(){
         executeDeleteRow(TEST_KEY_EMPTY);
     }
     
     /**
      * Tests delete empty row in not empty spreadsheet.
      */
     @Test
     public void testDeleteRowEmptyNotAll(){
         executeDeleteRow(TEST_KEY_EMPTY_NOT_ALL);
     }
     
     /**
      * Tests delete filled row in spreadsheet.
      */
     @Test
     public void testDeleteRowFilled(){
         executeDeleteRow(TEST_KEY_FILLED);
     }
     
     /**
      * Tests delete column in empty spreadsheet.
      */
     @Test
     public void testDeleteColumnEmpty(){
         executeDeleteCoulumn(TEST_KEY_EMPTY);
     }
     
     /**
      * Tests delete empty column in not empty spreadsheet.
      */
     @Test
     public void testDeleteColumnEmptyNotAll(){
         executeDeleteCoulumn(TEST_KEY_EMPTY_NOT_ALL);
     }
    
     /**
      * Tests delete filled column in spreadsheet.
      */
     @Test
     public void testDeleteColumnFilled(){
         executeDeleteCoulumn(TEST_KEY_FILLED);
     }
     
     /**
      * Tests swap rows in empty spreadsheet.
      */
     @Test
     public void testSwapRowsEmpty(){
         executeSwapRow(TEST_KEY_EMPTY);
     }
     
     /**
      * Tests swap rows, when one row is empty.
      */
     @Test
     public void testSwapRowsEmptyOne(){
         executeSwapRow(TEST_KEY_EMPTY_ONE);
     }
     
     /**
      * Tests swap rows, when both are filled.
      */
     @Test
     public void testSwapRowsFilled(){
         executeSwapRow(TEST_KEY_FILLED);
     }
     
     /**
      * Tests swap columns in empty spreadsheet.
      */
     @Test
     public void testSwapColumnsEmpty(){
         executeSwapCoulumn(TEST_KEY_EMPTY);
     }
     
     /**
      * Tests swap columns, when one column is empty.
      */
     @Test
     public void testSwapColumnsEmptyOne(){
         executeSwapCoulumn(TEST_KEY_EMPTY_ONE);
     }
     
     /**
      * Tests swap columns, when both are filled.
      */
     @Test
     public void testSwapColumnsFilled(){
         executeSwapCoulumn(TEST_KEY_FILLED);
     }
     
     /**
      * One test finished.
      */
     @After
     public void finishOne(){
         if(neo!=null){
             neo.shutdown();
             neo = null;
         } 
     }
     
     /**
      * All tests finished.
      */
     @AfterClass
     public static void finishAll(){
         clearMainDirectory();
     }
     
     /**
      * Create empty spreadsheet.
      */
     private void createSpreadsheet()throws SplashDatabaseException{
         AweProjectNode aweProject = projectService.findOrCreateAweProject(AWE_PROJECT_NAME);
         RubyProjectNode rubyProject = projectService.findOrCreateRubyProject(aweProject, RUBY_PROJECT_NAME);
         service = new SpreadsheetService(getNeo());
         spreadsheet = service.createSpreadsheet(rubyProject, SPREAD_SHEET_NAME);
     }
     
     /**
      * Execute insert row and assert results.
      * @param aTestKey String (test key)
      */
     private void executeInserRow(String aTestKey){
         String testKey = BUNDLE_PREFIX_INSERT_ROW+aTestKey;
         Transaction tx = getNeo().beginTx();
         try {
             fillDataToSpreadsheet(testKey);
             Integer data = parseStringToInteger(getProperty(testKey + BUNDLE_SUB_KEY_EXEC_DATA));
             service.insertRow(spreadsheet, data);
             assertSpreadsheet(testKey);
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Execute insert column and assert results.
      * @param aTestKey String (test key)
      */
     private void executeInserCoulumn(String aTestKey){
         String testKey = BUNDLE_PREFIX_INSERT_COLUMN+aTestKey;
         Transaction tx = getNeo().beginTx();
         try {
             fillDataToSpreadsheet(testKey);
             Integer data = parseStringToInteger(getProperty(testKey+BUNDLE_SUB_KEY_EXEC_DATA));
             service.insertColumn(spreadsheet, data);
             assertSpreadsheet(testKey);
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Execute delete cell and assert results.
      * @param aTestKey String (test key)
      */
     private void executeDeleteCell(String aTestKey){
         String testKey = BUNDLE_PREFIX_DELETE_CELL+aTestKey;
         Transaction tx = getNeo().beginTx();
         try {
             fillDataToSpreadsheet(testKey);
             List<Integer> data = parseStringToIntegerList(getProperty(testKey + BUNDLE_SUB_KEY_EXEC_DATA));
             service.deleteCell(spreadsheet, data.get(0), data.get(1));
             assertSpreadsheet(testKey);
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Execute delete row and assert results.
      * @param aTestKey String (test key)
      */
     private void executeDeleteRow(String aTestKey){
         String testKey = BUNDLE_PREFIX_DELETE_ROW+aTestKey;
         Transaction tx = getNeo().beginTx();
         try {
             fillDataToSpreadsheet(testKey);
             Integer data = parseStringToInteger(getProperty(testKey + BUNDLE_SUB_KEY_EXEC_DATA));
             service.deleteRow(spreadsheet, data);
             assertSpreadsheet(testKey);
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Execute delete column and assert results.
      * @param aTestKey String (test key)
      */
     private void executeDeleteCoulumn(String aTestKey){
         String testKey = BUNDLE_PREFIX_DELETE_COLUMN+aTestKey;
         Transaction tx = getNeo().beginTx();
         try {
             fillDataToSpreadsheet(testKey);
             Integer data = parseStringToInteger(getProperty(testKey + BUNDLE_SUB_KEY_EXEC_DATA));
             service.deleteColumn(spreadsheet, data);
             assertSpreadsheet(testKey);
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Execute swap row and assert results.
      * @param aTestKey String (test key)
      */
     private void executeSwapRow(String aTestKey){
         String testKey = BUNDLE_PREFIX_SWAP_ROW+aTestKey;
         Transaction tx = getNeo().beginTx();
         try {
             fillDataToSpreadsheet(testKey);
             List<Integer> data = parseStringToIntegerList(getProperty(testKey + BUNDLE_SUB_KEY_EXEC_DATA));
             service.swapRows(spreadsheet, data.get(0), data.get(1));
             assertSpreadsheet(testKey);
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Execute swap column and assert results.
      * @param aTestKey String (test key)
      */
     private void executeSwapCoulumn(String aTestKey){
         String testKey = BUNDLE_PREFIX_SWAP_COLUMN+aTestKey;
         Transaction tx = getNeo().beginTx();
         try {
             fillDataToSpreadsheet(testKey);
             List<Integer> data = parseStringToIntegerList(getProperty(testKey + BUNDLE_SUB_KEY_EXEC_DATA));
             service.swapColumns(spreadsheet, data.get(0), data.get(1));
             assertSpreadsheet(testKey);
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Fill test data into spreadsheet.
      *
      * @param aTestKey
      */
     private void fillDataToSpreadsheet(String aTestKey){
         String testKey = aTestKey + BUNDLE_SUB_KEY_FILL_DATA;
         Integer rowCount = parseStringToInteger(getProperty(testKey + "row_count"));
         for (int i = 0; i < rowCount; i++) {
             List<String> row = parseStringToStringList(getProperty(testKey + "row" + i));
             for (int j = 0; j < row.size(); j++) {                
                 String value = row.get(j);
                 if (!value.equals("null")) {
                     CellNode cell = service.createCell(spreadsheet, i, j);
                     cell.setValue(value);
                 }
             }
         }
     }
     
     /**
      * Assert spreadsheet after cell moving.
      *
      * @param aTestKey
      */
     private void assertSpreadsheet(String aTestKey){
         String testKey = aTestKey+BUNDLE_SUB_KEY_CHECK_DATA;
         Integer etalonRowCount = parseStringToInteger(getProperty(testKey+"row_count"));
         RowHeaderNode firstHeader = spreadsheet.getRowHeader(0);
         if (etalonRowCount == 0) {
             assertTrue("Spreadsheet must be empty by key <" + aTestKey + ">.", firstHeader == null);
             return;
         }
         Integer etalonColCount = parseStringToInteger(getProperty(testKey + "col_count"));
         String[][] etalonData = new String[etalonRowCount][etalonColCount];
         for (int i = 0; i < etalonRowCount; i++) {
             List<String> rowList = parseStringToStringList(getProperty(testKey + "row" + i));
             for (int j = 0; j < etalonColCount; j++) {
                 String value = rowList.get(j);
                 etalonData[i][j] = value.equals("null") ? null : value;
             }
         }
         assertSpreadsheetByRows(aTestKey, etalonData);
         assertSpreadsheetByColumns(aTestKey, etalonData);
     }
     
     /**
      * Assert data by rows.
      *
      * @param aTestKey
      * @param etalonData
      */
     private void assertSpreadsheetByRows(String aTestKey, String[][] etalonData){
         int rowCount = etalonData.length;
         for(int i=0; i<rowCount; i++){
             RowHeaderNode rowHeader = spreadsheet.getRowHeader(i+1);
             boolean rowIsEmpty = rowIsEmpty(etalonData, i);
             if(rowIsEmpty){
                 assertTrue("Row "+i+" must be empty by key <"+aTestKey+">.", rowHeader==null); 
                 continue;
             }else{
                 assertTrue("Row "+i+" must not be empty by key <"+aTestKey+">.", rowHeader!=null);
             }            
             int colCount = etalonData[i].length;
             CellNode lastInRow = rowHeader;
             for(int j=0; j<colCount;j++){
                 String etalon = etalonData[i][j];
                 CellNode cell = spreadsheet.getCell(i, j);
                 if(etalon==null){
                     assertTrue("Cell <"+i+","+j+"> must be empty by key <"+aTestKey+">.", cell==null);
                     continue;
                 }else{
                     assertTrue("Cell <"+i+","+j+"> must not be empty by key <"+aTestKey+">.", cell!=null);
                 }
                 assertEquals("Cell <"+i+","+j+"> has wrong value by key <"+aTestKey+">.", etalon, cell.getValue());
                 lastInRow = lastInRow.getNextCellInRow();
                 assertEquals("Wrong relationship order in row "+i+" (column "+j+") by key <"+aTestKey+">.", cell.getValue(), lastInRow.getValue());
             }            
         }
         RowHeaderNode rowHeader = spreadsheet.getRowHeader(rowCount+1);
         assertTrue("Row counts more than "+rowCount+" by key <"+aTestKey+">.", rowHeader==null);
     }
     
     /**
      * Assert data by columns.
      *
      * @param aTestKey
      * @param etalonData
      */
     private void assertSpreadsheetByColumns(String aTestKey, String[][] etalonData){
         int colCount = etalonData[0].length;
         int rowCount = etalonData.length;
         for(int i=0; i<colCount;i++){
             ColumnHeaderNode colHeader = spreadsheet.getColumnHeader(i+1);
             boolean columnIsEmpty = columnIsEmpty(etalonData, i);
             if(columnIsEmpty){
                 assertTrue("Column "+i+" must be empty by key <"+aTestKey+">.", colHeader==null); 
                 continue;
             }else{
                 assertTrue("Column "+i+" must not be empty by key <"+aTestKey+">.", colHeader!=null);
             }
             CellNode lastInColumn = colHeader;
             for(int j=0; j<rowCount;j++){
                 String etalon = etalonData[j][i];
                 CellNode cell = spreadsheet.getCell(j, i);
                 if(etalon==null){
                     assertTrue("Cell <"+j+","+i+"> must be empty by key <"+aTestKey+">.", cell==null);
                     continue;
                 }else{
                     assertTrue("Cell <"+j+","+i+"> must not be empty by key <"+aTestKey+">.", cell!=null);
                 }
                 assertEquals("Cell <"+j+","+i+"> has wrong value by key <"+aTestKey+">.", etalon, cell.getValue());
                 lastInColumn = lastInColumn.getNextCellInColumn();
                 assertEquals("Wrong relationship order in column "+i+" (row "+j+") by key <"+aTestKey+">.", cell.getValue(), lastInColumn.getValue());
             }
         }
         ColumnHeaderNode colHeader = spreadsheet.getColumnHeader(colCount+1);
         assertTrue("Column count more than "+colCount+" by key <"+aTestKey+">.", colHeader==null); 
     }
     
     /**
      * Is data row empty?
      *
      * @param etalonData
      * @param rowNumber
      * @return boolean
      */
     private boolean rowIsEmpty(String[][] etalonData, int rowNumber){
         for(String value : etalonData[rowNumber]){
             if(value != null){
                 return false;
             }
         }
         return true;
     }
     
     /**
      * Is data column empty?
      *
      * @param etalonData
      * @param colNumber
      * @return boolean
      */
     private boolean columnIsEmpty(String[][] etalonData, int colNumber){
         for(String[] row : etalonData){
             String value = row[colNumber];
             if(value != null){
                 return false;
             }
         }
         return true;
     }
     
     /**
      * Gets property from default resource bungle.
      * @param aKey String (key)
      * @return String (property)
      */
     private String getProperty(String aKey){
         return ResourceBundle.getBundle(getDefaultBungleName()).getString(aKey);
     }
     /**
      * Gets name of default resource bungle.
      * @return String 
      */
     private String getDefaultBungleName(){
         return this.getClass().getName();
     }
     
     /**
      * Convert string to integer.
      *
      * @param string
      * @return Integer
      */
     private Integer parseStringToInteger(String string){
         if(string==null || string.length()==0){
             return 0;
         }
         return Integer.parseInt(string);
     }
     
     /**
      * Convert string to list of integers.
      *
      * @param string
      * @return List of Integers
      */
     private List<Integer> parseStringToIntegerList(String string){
         if(string==null || string.length()==0){
             return null;
         }
         String[] strs = string.trim().split(BUNDLE_KEY_SEPARATOR);
         List<Integer> result = new ArrayList<Integer>(strs.length);
         for(String str : strs){
             result.add(Integer.parseInt(str));
         }
         return result;
     }
     
     /**
      * Convert string to list of strings.
      *
      * @param string
      * @return List of Strings
      */
     private List<String> parseStringToStringList(String string){
         if(string==null || string.length()==0){
             return null;
         }
         String[] strs = string.trim().split(BUNDLE_KEY_SEPARATOR);        
         return Arrays.asList(strs);
     }
 }
