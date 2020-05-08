 /***************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * @author Kevin L. Manansala
  * 
  * This software is licensed for use under the terms of the 
  * GNU General Public License (http://bit.ly/8Ztv8M) and the 
  * provisions of Part F of the Generation Challenge Programme 
  * Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  **************************************************************/
 package org.generationcp.ibpworkbench.database;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.generationcp.commons.exceptions.InternationalizableException;
 import org.generationcp.commons.util.ResourceFinder;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.Person;
 import org.generationcp.middleware.pojos.User;
 import org.generationcp.middleware.pojos.workbench.CropType;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.generationcp.middleware.pojos.workbench.ProjectUserMysqlAccount;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class which takes care of adding mysql user accounts for members of a workbench project.
  * Patterned after IBDBGenerator.
  * 
  * @author Kevin L. Manansala
  *
  */
 public class MysqlAccountGenerator implements Serializable{
 
     private static final long serialVersionUID = -7078581017175422156L;
     
     private static final Logger LOG = LoggerFactory.getLogger(MysqlAccountGenerator.class);
     
     public static final String SQL_CREATE_USER = "CREATE USER";
     public static final String SQL_GRANT_SELECT_AND_EXECUTE = "GRANT SELECT, EXECUTE ON ";
     public static final String SQL_PERCENT_SIGN = "%";
     public static final String SPACE = " ";
     
     private CropType cropType;
     private Long projectId;
     private Set<User> projectMembers;
     private WorkbenchDataManager dataManager;
     
     private String workbenchHost;
     private String workbenchPort;
     private String workbenchUsername;
     private String workbenchPassword;
     private String workbenchURL;
     
     private Connection connection;
 
     public MysqlAccountGenerator(CropType cropType, Long projectId, Set<User> projectMembers, WorkbenchDataManager dataManager){
         this.cropType = cropType;
         this.projectId = projectId;
         this.projectMembers = projectMembers;
         this.dataManager = dataManager;
     }
     
     public boolean generateMysqlAccounts() throws InternationalizableException {
         boolean isGenerationSuccess = false;
         
         try{
             createLocalConnection();
             Map<Integer, String> usernames = createAccounts();
             executeGrantStatements(usernames);
             storeWokrbenchUserToMysqlAccountMappings(usernames);
             
             isGenerationSuccess = true;
         } catch (InternationalizableException e) {
             isGenerationSuccess = false;            
             throw e;
         } 
         
         return isGenerationSuccess;
     }
     
     private void createLocalConnection() throws InternationalizableException {
 
         if (this.connection == null) {
 
             Properties prop = new Properties();
 
             try {
                 InputStream in = null;
 
                 try {
                     in = new FileInputStream(new File(ResourceFinder.locateFile(IBDBGenerator.WORKBENCH_PROP).toURI()));
                 } catch (IllegalArgumentException ex) {
                     in = Thread.currentThread().getContextClassLoader().getResourceAsStream(IBDBGenerator.WORKBENCH_PROP);
                 }
                 prop.load(in);
 
                 workbenchHost = prop.getProperty(IBDBGenerator.WORKBENCH_PROP_HOST);
                 workbenchPort = prop.getProperty(IBDBGenerator.WORKBENCH_PROP_PORT);
                 workbenchUsername = prop.getProperty(IBDBGenerator.WORKBENCH_PROP_USER);
                 workbenchPassword = prop.getProperty(IBDBGenerator.WORKBENCH_PROP_PASSWORD);
                 workbenchURL = "jdbc:mysql://" + workbenchHost + ":" + workbenchPort;
 
                 in.close();
 
             } catch (URISyntaxException e) {
                 IBDBGenerator.handleConfigurationError(e);
             } catch (IOException e) {
                 IBDBGenerator.handleConfigurationError(e);
             }
 
             try {
                 connection = DriverManager.getConnection(workbenchURL, workbenchUsername, workbenchPassword);
             } catch (SQLException e) {
                 handleDatabaseError(e);
             }
 
         }
     }
     
     /**
      * Returns a hashmap with the keys being the workbench user ids and the value being the mysql usernames assigned.
      *  
      * @return
      * @throws InternationalizableException
      */
     private Map<Integer, String> createAccounts() throws InternationalizableException {
         //prepare the mysql username and passwords
         Map<Integer, String> usernames = new HashMap<Integer, String>();
         try{
             for(User member : projectMembers){
                 Person personForUser = this.dataManager.getPersonById(member.getPersonid());
                 
                 StringBuilder initials = new StringBuilder();
                 if(personForUser.getFirstName() != null){
                     initials.append(personForUser.getFirstName().charAt(0));
                 }
                if(personForUser.getMiddleName() != null) {
                     initials.append(personForUser.getMiddleName().charAt(0));
                 }
                 if(personForUser.getLastName() != null) {
                     initials.append(personForUser.getLastName().charAt(0));
                 }
                 String userInitials = initials.toString().toLowerCase();
                 
                 StringBuilder username = new StringBuilder();
                 username.append(userInitials);
                 username.append("_");
                 username.append(member.getUserid());
                 username.append("_");
                 username.append(this.projectId);
                 
                 usernames.put(member.getUserid(), username.toString());
             }
         } catch(MiddlewareQueryException ex) {
             LOG.error(ex.toString(), ex);
             throw new InternationalizableException(ex, 
                     Message.DATABASE_ERROR, Message.CONTACT_DEV_ERROR_DESC);
         }
         
         //execute create user statements
         Statement statement = null;
         
         try{
             statement = this.connection.createStatement();
         
             for(String username : usernames.values()){
                 StringBuilder createUserStatement = new StringBuilder();
                 
                 createUserStatement.append(SQL_CREATE_USER);
                 createUserStatement.append(SPACE);
                 createUserStatement.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 createUserStatement.append(username);
                 createUserStatement.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 createUserStatement.append(IBDBGenerator.SQL_AT_SIGN);
                 createUserStatement.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 createUserStatement.append(IBDBGenerator.DEFAULT_LOCAL_HOST);
                 createUserStatement.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 createUserStatement.append(IBDBGenerator.SQL_IDENTIFIED_BY);
                 createUserStatement.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 createUserStatement.append(username);
                 createUserStatement.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 
                 statement.addBatch(createUserStatement.toString());
             }
             
             statement.executeBatch();
         } catch (SQLException e) {
             handleDatabaseError(e);
         } finally {
             if (statement != null) {
                 try {
                     statement.close();
                 } catch (SQLException e) {
                     handleDatabaseError(e);
                 }
             }
         }
         
         return usernames;
     }
     
     private void executeGrantStatements(Map<Integer, String> usernames) throws InternationalizableException {
         //execute grant statements
         Statement statement = null;
         String centralDatabaseName = this.cropType.getCentralDbName();
         StringBuilder localDatabaseNameBuilder = new StringBuilder();
         localDatabaseNameBuilder.append(this.cropType.getCropName().toLowerCase());
         localDatabaseNameBuilder.append("_");
         localDatabaseNameBuilder.append(this.projectId);
         localDatabaseNameBuilder.append(IBDBGenerator.DB_LOCAL_NAME_SUFFIX);
         String localDatabaseName = localDatabaseNameBuilder.toString();
         
         try{
             statement = this.connection.createStatement();
             
             for(String username : usernames.values()){
                 StringBuilder grantStatementForCentral = new StringBuilder();
                 
                 //grant statement for central IBDB access
                 grantStatementForCentral.append(SQL_GRANT_SELECT_AND_EXECUTE);
                 grantStatementForCentral.append(centralDatabaseName);
                 grantStatementForCentral.append(IBDBGenerator.SQL_PERIOD);
                 grantStatementForCentral.append(IBDBGenerator.DEFAULT_ALL);
                 grantStatementForCentral.append(IBDBGenerator.SQL_TO);
                 grantStatementForCentral.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 grantStatementForCentral.append(username);
                 grantStatementForCentral.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 grantStatementForCentral.append(IBDBGenerator.SQL_AT_SIGN);
                 grantStatementForCentral.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 grantStatementForCentral.append(IBDBGenerator.DEFAULT_LOCAL_HOST);
                 grantStatementForCentral.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 
                 statement.addBatch(grantStatementForCentral.toString());
                 
                 //grant statement for local IBDB access
                 StringBuilder grantStatementForLocal = new StringBuilder();
                 
                 grantStatementForLocal.append(IBDBGenerator.SQL_GRANT_ALL);
                 grantStatementForLocal.append(localDatabaseName);
                 grantStatementForLocal.append(IBDBGenerator.SQL_PERIOD);
                 grantStatementForLocal.append(IBDBGenerator.DEFAULT_ALL);
                 grantStatementForLocal.append(IBDBGenerator.SQL_TO);
                 grantStatementForLocal.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 grantStatementForLocal.append(username);
                 grantStatementForLocal.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 grantStatementForLocal.append(IBDBGenerator.SQL_AT_SIGN);
                 grantStatementForLocal.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 grantStatementForLocal.append(IBDBGenerator.DEFAULT_LOCAL_HOST);
                 grantStatementForLocal.append(IBDBGenerator.SQL_SINGLE_QUOTE);
                 
                 statement.addBatch(grantStatementForLocal.toString());
             }
             
             statement.executeBatch();
             
         } catch (SQLException e) {
             handleDatabaseError(e);
         } finally {
             if (statement != null) {
                 try {
                     statement.close();
                 } catch (SQLException e) {
                     handleDatabaseError(e);
                 }
             }
         }
     }
     
     private void storeWokrbenchUserToMysqlAccountMappings(Map<Integer, String> usernames) throws InternationalizableException {
         List<ProjectUserMysqlAccount> mappingRecords = new ArrayList<ProjectUserMysqlAccount>();
         Project project = null;
         try{
             project = this.dataManager.getProjectById(this.projectId);
         } catch(MiddlewareQueryException ex) {
             LOG.error("Error with getting Project with id: " + this.projectId
                     + " in storing mappings of users to mysql accounts: " + ex.toString(), ex);
             throw new InternationalizableException(ex, 
                     Message.DATABASE_ERROR, Message.CONTACT_DEV_ERROR_DESC);
         }
         
         if(project != null){
             try{
                 for(Integer userid : usernames.keySet()){
                     User userRecord = this.dataManager.getUserById(userid.intValue());
                     String username = usernames.get(userid);
                     
                     if(userRecord != null){
                         ProjectUserMysqlAccount mappingRecord = new ProjectUserMysqlAccount();
                         mappingRecord.setProject(project);
                         mappingRecord.setUser(userRecord);
                         mappingRecord.setMysqlUsername(username);
                         mappingRecord.setMysqlPassword(username);
                         mappingRecords.add(mappingRecord);
                     }
                 }
                 
                 this.dataManager.addProjectUserMysqlAccounts(mappingRecords);
                 
             } catch(MiddlewareQueryException ex){
                 LOG.error("Error with saving mappings of user to mysql accounts: " 
                         + ex.toString(), ex);
                 throw new InternationalizableException(ex, 
                         Message.DATABASE_ERROR, Message.CONTACT_DEV_ERROR_DESC);
             }
         }             
     }
     
     private void handleDatabaseError(Exception e) throws InternationalizableException {
         LOG.error(e.toString(), e);
         throw new InternationalizableException(e, 
                 Message.DATABASE_ERROR, Message.CONTACT_ADMIN_ERROR_DESC);
     }
 }
