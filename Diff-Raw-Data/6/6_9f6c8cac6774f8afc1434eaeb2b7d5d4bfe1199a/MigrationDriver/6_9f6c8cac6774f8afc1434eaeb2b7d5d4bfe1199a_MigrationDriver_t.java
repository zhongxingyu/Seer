 /*L
  *  Copyright Ekagra Software Technologies Ltd.
  *  Copyright SAIC, SAIC-Frederick
  *
  *  Distributed under the OSI-approved BSD 3-Clause License.
  *  See http://ncip.github.com/common-security-module/LICENSE.txt for details.
  */
 
 package gov.nih.nci.migration;
 
 import gov.nih.nci.security.util.StringEncrypter;
 import gov.nih.nci.security.util.StringUtilities;
 import gov.nih.nci.security.util.StringEncrypter.EncryptionException;
 import gov.nih.nci.security.util.AESEncryption;
 import gov.nih.nci.security.util.DESEncryption;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.Calendar;
 import org.apache.commons.lang.time.DateUtils;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 
 public class MigrationDriver {
 	static String PROPERTIES_FILE_NAME = "";
 	static String DATABASE_SERVER_NAME = "localhost";
 	static String DATABASE_SERVER_PORT_NUMBER = "3306";
 	// The Type of Database. Use one of the three values 'MySQL', 'Oracle', 'PostgreSQL'.
 	static String DATABASE_TYPE = "MySQL";
 	//	Name of the Database.
 	static String DATABASE_NAME = "upt32";
 	// Database User name
 	static String DATABASE_USERNAME = "csmadmin";
 	static String DATABASE_PASSWORD = "csmadmin";
 	static String DATABASE_DRIVER = "org.gjt.mm.mysql.Driver";
 	static String DATABASE_URL = "jdbc:mysql://localhost:3306/csm42";
 
 	static String DATABASE_URL_PREFIX = "jdbc:mysql";
 	static String DATABASE_URL_SEPARATOR = "//";
 	static String DATABASE_NAME_SEPARATOR = "/";
 
 	private	AESEncryption aesEncryption = null;
 	private DESEncryption desEncryption = null;
 	private String expiryDays = null;
 	private Connection connection = null;
 
 	public static void main(String[] args) {
 
 		try
 		{
 			if (args.length > 0 )
 			{
 				PROPERTIES_FILE_NAME=args[0];
 			}
 
 			MigrationDriver migrationDriver = new MigrationDriver();
 			migrationDriver.encryptDecryptUserInformation();
 			migrationDriver.encryptDecryptApplicationInformation();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		catch (EncryptionException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public MigrationDriver() throws EncryptionException, SQLException
 	{
 			setProperties();
 			setAESEncryption();
 			setDESEncryption();
 	}
 
 	private void encryptDecryptUserInformation() throws  EncryptionException, SQLException
 	{
 			Connection connection = getConnection();
			Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			connection.setAutoCommit(false);
 			ResultSet resultSet = null;
 			if("oracle".equals(DATABASE_TYPE)) {
 				 resultSet = stmt.executeQuery("SELECT CSM_USER.* FROM CSM_USER FOR UPDATE");
 			} else {
 				 resultSet = stmt.executeQuery("SELECT * FROM CSM_USER");
 			}
 			String userPassword = null;
 			String firstName = null;
 			String lastName = null;
 			String organization = null;
 			String department = null;
 			String title = null;
 			String phoneNumber = null;
 			String emailId = null;
 			String encryptedUserPassword = null;
 			Date expiryDate = null;
 
 			while(resultSet.next())
 			{
 				userPassword = resultSet.getString("PASSWORD");
 				firstName = resultSet.getString("FIRST_NAME");
 				lastName = resultSet.getString("LAST_NAME");
 				organization = resultSet.getString("ORGANIZATION");
 				department = resultSet.getString("DEPARTMENT");
 				title = resultSet.getString("TITLE");
 				phoneNumber = resultSet.getString("PHONE_NUMBER");
 				emailId = resultSet.getString("EMAIL_ID");
 
 
 				if(!StringUtilities.isBlank(userPassword)){
 					String orgPasswordStr = desEncryption.decrypt(userPassword);
 					encryptedUserPassword = 	aesEncryption.encrypt(orgPasswordStr);
 					if(!StringUtilities.isBlank(encryptedUserPassword)){
 						  resultSet.updateString("PASSWORD", encryptedUserPassword);
 					}
 				}
 				if(!StringUtilities.isBlank(firstName))
 					resultSet.updateString("FIRST_NAME", aesEncryption.encrypt(firstName));
 				if(!StringUtilities.isBlank(lastName))
 					resultSet.updateString("LAST_NAME", aesEncryption.encrypt(lastName));
 				if(!StringUtilities.isBlank(organization))
 					resultSet.updateString("ORGANIZATION", aesEncryption.encrypt(organization));
 				if(!StringUtilities.isBlank(department))
 					resultSet.updateString("DEPARTMENT", aesEncryption.encrypt(department));
 				if(!StringUtilities.isBlank(title))
 					resultSet.updateString("TITLE", aesEncryption.encrypt(title));
 				if(!StringUtilities.isBlank(phoneNumber))
 					resultSet.updateString("PHONE_NUMBER", aesEncryption.encrypt(phoneNumber));
 				if(!StringUtilities.isBlank(emailId))
 					resultSet.updateString("EMAIL_ID", aesEncryption.encrypt(emailId));
 
 
 				expiryDate=DateUtils.addDays(Calendar.getInstance().getTime(),Integer.parseInt(getExpiryDays()));
 				resultSet.updateDate("PASSWORD_EXPIRY_DATE",new java.sql.Date(expiryDate.getTime()));
 				System.out.println("Updating user:" +resultSet.getString("LOGIN_NAME"));
 				resultSet.updateRow();
 			}
			connection.commit();
 	}
 
 	private void encryptDecryptApplicationInformation() throws EncryptionException, SQLException
 	{
 
 			Connection connection = getConnection();
 			Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
 
 			ResultSet resultSet = null;
 			if("oracle".equals(DATABASE_TYPE)) {
 				 resultSet = stmt.executeQuery("SELECT CSM_APPLICATION.* FROM CSM_APPLICATION FOR UPDATE");
 			} else {
 				 resultSet = stmt.executeQuery("SELECT * FROM CSM_APPLICATION");
 			}
 
 			String databasePassword = null;
 			String encryptedDatabasePassword = null;
 
 			while(resultSet.next())
 			{
 				databasePassword = resultSet.getString("DATABASE_PASSWORD");
 
 				if(!StringUtilities.isBlank(databasePassword)){
 							String orgPasswordStr = desEncryption.decrypt(databasePassword);
 							encryptedDatabasePassword = aesEncryption.encrypt(orgPasswordStr);
 							if(!StringUtilities.isBlank(encryptedDatabasePassword)){
 								  resultSet.updateString("DATABASE_PASSWORD", encryptedDatabasePassword);
 							}
 					}
 				System.out.println("Updating Application:" +resultSet.getString("APPLICATION_NAME"));
 				resultSet.updateRow();
 			}
 
 	}
 
 	private void setAESEncryption() throws EncryptionException, SQLException
 	{
 		Connection connection = getConnection();
 		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
 
 
 		ResultSet resultSet = stmt.executeQuery("SELECT * FROM CSM_CONFIGURATION_PROPS");
 		String aesEncryptionKey = null;
 		String md5HashKey = null;
 		while(resultSet.next())
 		{
 				String propertyKey = resultSet.getString("PROPERTY_KEY");
 				if("AES_ENCRYPTION_KEY".equals(propertyKey))
 					aesEncryptionKey = resultSet.getString("PROPERTY_VALUE");
 				if("MD5_HASH_KEY".equals(propertyKey))
 					md5HashKey = resultSet.getString("PROPERTY_VALUE");
 				if("PASSWORD_EXPIRY_DAYS".equals(propertyKey))
 					expiryDays = resultSet.getString("PROPERTY_VALUE");
 		}
 		setExpiryDays(expiryDays);
 		aesEncryption = new AESEncryption(aesEncryptionKey,  Boolean.parseBoolean(md5HashKey));
 	}
 
 	private void setDESEncryption() throws EncryptionException
 	{
 		desEncryption =  new DESEncryption();
 	}
 
 	private Connection getConnection() {
 		 Connection connection = null;
 		    try {
 		        // Load the JDBC driver
 		        Class.forName(DATABASE_DRIVER);
 
 		        // Create a connection to the database
 		        String url = DATABASE_URL;
 		        /*
 		        String url ="";
 		        if("MySQL".equalsIgnoreCase(DATABASE_TYPE)){
 		        	url = "jdbc:mysql://" + DATABASE_SERVER_NAME  +":"+DATABASE_SERVER_PORT_NUMBER+  "/" + DATABASE_NAME; // a JDBC url
 		        }
 		        if("Oracle".equalsIgnoreCase(DATABASE_TYPE)){
 		        	url = "jdbc:oracle:thin:@" + DATABASE_SERVER_NAME + ":" + DATABASE_SERVER_PORT_NUMBER + ":" + DATABASE_NAME;
 		        }
 		        if("SQLServer".equalsIgnoreCase(DATABASE_TYPE)){
 		        	 url = "jdbc:JSQLConnect://" + DATABASE_SERVER_NAME + ":" + DATABASE_SERVER_PORT_NUMBER; // a JDBC url
 		        }
 		        */
 		        connection = DriverManager.getConnection(url, DATABASE_USERNAME, DATABASE_PASSWORD);
 		    } catch (ClassNotFoundException e) {
 		    	e.printStackTrace();
 		        // Could not find the database driver
 		    } catch (SQLException e) {
 		    	e.printStackTrace();
 		        // Could not connect to the database
 		    }
 /*
 		    try {
 
 		        DatabaseMetaData dmd = connection.getMetaData();
 		        if (dmd.supportsResultSetConcurrency(
 		            ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
 		            // Updatable result sets are supported
 		        } else {
 		            // Updatable result sets are not supported
 		        	return null;
 		        }
 		    } catch (SQLException e) {
 		    }
 */
 
 		return connection;
 	}
 
 
 	private void setProperties() {
 
 				try {
 			        //Reading properties file
 			        Properties props = new Properties();
 			        FileInputStream fis = new FileInputStream(PROPERTIES_FILE_NAME);
 
 			        //loading properties from properties file
 			        props.load(fis);
 
 			        //reading properties
 			        DATABASE_SERVER_NAME = props.getProperty("upt.central.database.server");
 			        DATABASE_SERVER_PORT_NUMBER = props.getProperty("upt.central.database.port");
 			        DATABASE_TYPE = props.getProperty("database.type");
 			        DATABASE_NAME = props.getProperty("upt.central.database.name");
 			        DATABASE_USERNAME = props.getProperty("upt.central.database.user");
 			        DATABASE_PASSWORD = props.getProperty("upt.central.database.password");
 			        DATABASE_DRIVER = props.getProperty("upt.central.database.driver.class");
 
 					DATABASE_URL_PREFIX = props.getProperty("upt.central.database.url.prefix");
 
 					if("oracle".equals(DATABASE_TYPE)) {
 						DATABASE_URL_SEPARATOR = "@";
 						DATABASE_NAME_SEPARATOR = ":";
 					}
 
 			        DATABASE_URL = DATABASE_URL_PREFIX+":"+DATABASE_URL_SEPARATOR+DATABASE_SERVER_NAME+":"
 			        				+DATABASE_SERVER_PORT_NUMBER+DATABASE_NAME_SEPARATOR+DATABASE_NAME;
 
 			        System.out.println("*****Database URL: " + DATABASE_URL);
 			        System.out.println("*****Database Driver: " + DATABASE_DRIVER);
 			    } catch (FileNotFoundException e) {
 		    		e.printStackTrace();
 		        	// Could not find the properties file
 		    	} catch (IOException e) {
 		    		e.printStackTrace();
 		        	// Could not open the properties file
 		    	}
 		}
 
 	public String getExpiryDays() {
 		return expiryDays;
 	}
 
 	public void setExpiryDays(String expiryDays) {
 		this.expiryDays = expiryDays;
 	}
 
 }
