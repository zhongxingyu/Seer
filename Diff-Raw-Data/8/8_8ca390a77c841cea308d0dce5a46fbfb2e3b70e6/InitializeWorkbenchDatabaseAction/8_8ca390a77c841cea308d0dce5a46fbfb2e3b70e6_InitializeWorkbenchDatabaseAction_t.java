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
 
 import org.generationcp.ibpworkbench.install4j.Crop;
 import org.generationcp.ibpworkbench.install4j.Install4JUtil;
 import org.generationcp.ibpworkbench.util.Install4JScriptRunner;
 
 import com.install4j.api.Util;
 import com.install4j.api.actions.AbstractInstallAction;
 import com.install4j.api.context.InstallerContext;
 import com.install4j.api.context.UserCanceledException;
 
 public class InitializeWorkbenchDatabaseAction extends AbstractInstallAction {
     private static final long serialVersionUID = 1L;
     
     private final static String DATABASE_WORKBENCH_DATA_PATH = "database/workbench";
 
     public boolean install(InstallerContext context) throws UserCanceledException {
         context.getProgressInterface().setIndeterminateProgress(true);
         
         // connect to MySQL
         Connection connection = Install4JUtil.connectToMySQL(context);
         if (connection == null) {
             return false;
         }
         
         // run workbench scripts
         try {
             if (!runWorkbenchScripts(context, connection)) {
                 return false;
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
         
         context.getProgressInterface().setIndeterminateProgress(false);
         context.getProgressInterface().setPercentCompleted(100);
         
         return true;
     }
     
     protected boolean runWorkbenchScripts(InstallerContext context, Connection conn) {
         File workbenchDatabaseDir = new File(context.getInstallationDirectory(), DATABASE_WORKBENCH_DATA_PATH);
         Object[] workbenchTitleParam = new Object[]{ context.getMessage("workbench") };
         
         // check if the central database is already installed
        boolean databaseExists = Install4JUtil.useDatabase(conn, "workbench");
         if (databaseExists) {
             return true;
         }
         
         // create the database and user
         String[] queries = new String[] {
                                          "CREATE DATABASE IF NOT EXISTS workbench"
                                         ,"GRANT ALL ON workbench.* TO 'workbench'@'localhost' IDENTIFIED BY 'workbench'"
                                         ,"USE workbench"
         };
         if (!Install4JUtil.executeUpdate(context, conn, true, queries)) {
             return false;
         }
         
         // show installation error if the crop database directory does not exist
         if (!workbenchDatabaseDir.exists()) {
             String errorMessage = context.getMessage("cannot_find_database_files", workbenchTitleParam);
             Util.showErrorMessage(errorMessage);
             return false;
         }
         
         // get the sql files
         File[] sqlFiles = workbenchDatabaseDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".sql");
             }
         });
         
         String statusMessage = context.getMessage("initializing_database", workbenchTitleParam);
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
         
         // set the installation directory
         PreparedStatement pstmt = null;
         try {
             pstmt = conn.prepareStatement("INSERT INTO workbench.workbench_setting(installation_directory) VALUES (?);");
             pstmt.setString(1, context.getInstallationDirectory().getAbsolutePath());
             pstmt.executeUpdate();
         }
         catch (SQLException e1) {
             Util.showErrorMessage(context.getMessage("cannot_initialize_database"));
             return false;
         }
         finally {
             if (pstmt != null) {
                 try {
                     pstmt.close();
                 }
                 catch (SQLException e) {
                     // intentionally empty
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
         if (!Install4JUtil.executeUpdate(context, conn, true, "CREATE DATABASE IF NOT EXISTS workbench")) {
             return false;
         }
         
         // create the workbench_crop table if it does not exist
         String createWorkbenchCropIfNotExistSql = "CREATE TABLE IF NOT EXISTS workbench.workbench_crop(\n"
                                                 + "    crop_name VARCHAR(32) NOT NULL\n"
                                                 + "   ,central_db_name VARCHAR(64)\n"
                                                 + "   ,PRIMARY KEY(crop_name)\n"
                                                 + ") ENGINE=InnoDB";
         if (!Install4JUtil.executeUpdate(context, conn, true, createWorkbenchCropIfNotExistSql)) {
             return false;
         }
         
         // check if the workbench_crop table exists in the correct format
         String workbenchCropCheckSql = "SELECT crop_name, central_db_name FROM workbench.workbench_crop LIMIT 1";
         boolean workbenchCropCorrect = Install4JUtil.canExecuteQueries(context, conn, workbenchCropCheckSql);
         
         if (!workbenchCropCorrect) {
             // if the workbench_crop table is in an incompatible format, drop and re-create it
             String dropWorkbenchCropSql = "DROP TABLE IF EXISTS workbench.workbench_crop";
             String createWorkbenchCropSql = "CREATE TABLE workbench.workbench_crop(\n"
                                           + "    crop_name VARCHAR(32) NOT NULL\n"
                                           + "   ,central_db_name VARCHAR(64)\n"
                                           + "   ,PRIMARY KEY(crop_name)\n"
                                           + ") ENGINE=InnoDB";
             if (!Install4JUtil.executeUpdate(context, conn, true, dropWorkbenchCropSql, createWorkbenchCropSql)) {
                 return false;
             }
         }
         
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
                 e.printStackTrace();
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
