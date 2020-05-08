 package org.generationcp.ibpworkbench.install4j.action;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.swing.JOptionPane;
 
 import org.generationcp.ibpworkbench.install4j.Crop;
 import org.generationcp.ibpworkbench.install4j.Install4JUtil;
 import org.generationcp.ibpworkbench.util.Install4JScriptRunner;
 
 import com.install4j.api.Util;
 import com.install4j.api.actions.AbstractInstallAction;
 import com.install4j.api.context.Context;
 import com.install4j.api.context.InstallerContext;
 import com.install4j.api.context.UserCanceledException;
 
 public class InitializeCentralDatabaseAction extends AbstractInstallAction {
     private static final long serialVersionUID = 1L;
     
     private final static String DATABASE_DATA_PATH = "database";
     private final static String DATABASE_CENTRAL_DATA_PATH = "database/central";
     private final static String DATABASE_CENTRAL_COMMON_DATA_PATH = "database/central/common";
     
     public boolean install(InstallerContext context) throws UserCanceledException {
         context.getProgressInterface().setIndeterminateProgress(true);
         
         // connect to MySQL
         Connection connection = Install4JUtil.connectToMySQL(context);
         if (connection == null) {
             return false;
         }
         
         try {
             // run scripts
             for (Crop crop : Crop.values()) {
                 if (Install4JUtil.isComponentSelected(context, crop)) {
                     boolean success = runScriptsForCrop(context, connection, crop);
                     if (!success) {
                         return false;
                     }
                 }
             }
             
             // register installed crop types
             if (!registerCrops(context, connection)) {
                 return false;
             }
             
             connection.commit();
         }
         catch (SQLException e) {
         }
         finally {
             try {
                 connection.close();
             }
             catch (SQLException e) {
             }
         }
         
         context.getProgressInterface().setStatusMessage(context.getMessage("deleting_temporary_files"));
         context.getProgressInterface().setDetailMessage("");
         
         // delete the crop database directory
         File databaseDataPath = new File(context.getInstallationDirectory(), DATABASE_DATA_PATH);
         databaseDataPath.delete();
         
         context.getProgressInterface().setStatusMessage(context.getMessage("done"));
         context.getProgressInterface().setDetailMessage("");
         
         context.getProgressInterface().setIndeterminateProgress(false);
         context.getProgressInterface().setPercentCompleted(100);
         
         return true;
     }
     
     protected boolean runScriptsForCrop(InstallerContext context, Connection conn, Crop crop) {
         String cropName = crop.getCropName();
         String databaseName = "ibdb_" + cropName + "_central";
         
         Object[] cropTitleParam = new Object[]{ context.getMessage(cropName) };
         
         // check if the central database is already installed
         boolean databaseExists = Install4JUtil.useDatabase(conn, databaseName);
         if (databaseExists) {
             String cropTitle = context.getMessage(cropName);
             String message = context.getMessage("confirm_central_database_update", new Object[] { cropTitle });
             String[] options = new String[]{context.getMessage("yes"), context.getMessage("no")};
             try {
                 int option = Util.showOptionDialog(message, options, JOptionPane.YES_NO_OPTION);
                 if (option != 0) {
                     return true;
                 }
             }
             catch (UserCanceledException e) {
                 return true;
             }
         }
         
         // create the database and user
         String userName = "central";
         String password = "central";
         String[] queries = new String[] {
                                          "DROP DATABASE IF EXISTS " + databaseName
                                          ,"CREATE DATABASE " + databaseName
                                          ,"GRANT ALL ON " + databaseName + ".* TO '" + userName + "'@'localhost' IDENTIFIED BY '" + password + "'"
                                          ,"FLUSH PRIVILEGES"
         };
         if (!Install4JUtil.executeUpdate(context, conn, true, queries)) {
             return false;
         }
         
         if (!Install4JUtil.useDatabase(conn, databaseName)) {
             Util.showErrorMessage(context.getMessage("cannot_initialize_database"));
             return false;
         }
         
         File centralDatabaseDir = new File(context.getInstallationDirectory(), DATABASE_CENTRAL_DATA_PATH);
         
         File centralCropDir = new File(centralDatabaseDir, cropName);
         File centralCropCommonDir = new File(context.getInstallationDirectory(), DATABASE_CENTRAL_COMMON_DATA_PATH);
         if (!runScriptsOnDirectory(context, conn, cropTitleParam, centralCropCommonDir)) {
             return false;
         }
         if (!runScriptsOnDirectory(context, conn, cropTitleParam, centralCropDir)) {
             return false;
         }
         
         return true;
     }
     
     protected boolean runScriptsOnDirectory(Context context, Connection conn, Object[] cropTitleParam, File cropDir) {
         // show installation error if the crop database directory does not exist
         if (!cropDir.exists()) {
             String errorMessage = context.getMessage("cannot_find_database_files", cropTitleParam);
             Util.showErrorMessage(errorMessage);
             return false;
         }
         
         // get the sql files
         File[] sqlFiles = cropDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".sql");
             }
         });
         
         String statusMessage = context.getMessage("importing_database", cropTitleParam);
         context.getProgressInterface().setStatusMessage(statusMessage);
 
         for (File sqlFile : sqlFiles) {
             BufferedReader br = null;
             
             try {
                 br = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFile)));
                 
                 Install4JScriptRunner runner = new Install4JScriptRunner(context, conn, false, true);
                 runner.runScript(br);
             }
             catch (IOException e1) {
                 e1.printStackTrace();
                 
                 Util.showErrorMessage(context.getMessage("cannot_initialize_database"));
                 return false;
             }
             finally {
                 if (br != null) {
                     try {
                         br.close();
                     }
                     catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
         
         return true;
     }
     
     /**
      * Register selected crops into workbench.workbench_crop table.
      * We SHOULD AVOID introducing any side effect into any existing workbench_crop table.
      * 
      * @param context
      * @param conn
      * @return
      */
     protected boolean registerCrops(InstallerContext context, Connection conn) {
         // we do our best effort to recreate the workbench_crop table
         for (Crop crop : Crop.values()) {
             if (!Install4JUtil.useDatabase(conn, crop.getCentralDatabaseName())) {
                 continue;
             }
             
             String insertCropSql = "REPLACE INTO workbench.workbench_crop (crop_name, central_db_name) VALUES (?, ?)";
             context.getProgressInterface().setDetailMessage(insertCropSql);
             
             PreparedStatement pstmt = null;
             try {
                 pstmt = conn.prepareStatement(insertCropSql);
 
                 pstmt.setString(1, crop.getCropName());
                 pstmt.setString(2, crop.getCentralDatabaseName());
                 pstmt.executeUpdate();
             }
             catch (SQLException e) {
             }
             finally {
                 try {
                     if (pstmt != null) {
                         pstmt.close();
                     }
                 }
                 catch (SQLException e2) {
                     // intentionally empty
                 }
             }
         }
         
         return true;
     }
 }
