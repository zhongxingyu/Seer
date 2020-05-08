 package org.mule.transport.sftp;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.mule.api.endpoint.ImmutableEndpoint;
 
 /**
  * Contains reusable methods not directly related to usage of the jsch sftp library (they can be found in the class SftpClient).
  *
  * @author Magnus Larsson
  *
  */
 public class SftpUtil {
 	/** Logger */
 	private static final Logger logger = Logger.getLogger(SftpUtil.class);
 
 	private SftpConnector connector;
 	private ImmutableEndpoint endpoint;
 
 	public SftpUtil(ImmutableEndpoint endpoint) {
 		this.endpoint = endpoint;
 		this.connector = (SftpConnector)endpoint.getConnector();
 	}
 
     public String createUniqueSuffix(String filename) {
 
 		// TODO. Add code for handling no '.'
 		int fileTypeIdx = filename.lastIndexOf('.');
 		String fileType = filename.substring(fileTypeIdx); // Let the fileType include the leading '.'
 
 		filename = filename.substring(0, fileTypeIdx); // Strip off the leading '/' from the filename
 
     	SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
 		String timstampStr = '_' + timestampFormatter.format(new Date());
 
 		return filename + timstampStr + fileType;
 	}
 
 
     public String getTempDir() {
         String tempDir = connector.getTempDir();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_TEMP_DIR);
         if(v != null) {
         	tempDir = (String)v;
         }
 
         return tempDir;
 	}
 
 	public boolean isUseTempDir()
 	{
 		return getTempDir() != null;
 	}
 
 	public void cleanupTempDir(SftpClient sftpClient, String transferFileName) {
 		String tempDir = getTempDir();
 		String tempDirAbs = sftpClient.getAbsolutePath(endpoint.getEndpointURI().getPath() + "/" + tempDir);
 		try
 		{
 			sftpClient.changeWorkingDirectory(tempDirAbs);
 			sftpClient.deleteFile(transferFileName);
 		} catch (Exception e)
 		{
			logger.error("Could not delete the file '" + transferFileName + "' from the temp directory '" + tempDirAbs + "'", e);
 		}
 	}
 
     public long getSizeCheckWaitTime() {
         long sizeCheckWaitTime = connector.getSizeCheckWaitTime();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_SIZE_CHECK_WAIT_TIME);
         if(v != null) {
         	sizeCheckWaitTime = Long.valueOf((String)v);
         }
 
         return sizeCheckWaitTime;
 	}
 
     public String getArchiveDir() {
         String archiveDir = connector.getArchiveDir();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_ARCHIVE_DIR);
         if(v != null) {
         	archiveDir = (String)v;
         }
 
         return archiveDir;
 	}
 
     public String getArchiveTempReceivingDir() {
         String archiveTempReceivingDir = connector.getArchiveTempReceivingDir();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_ARCHIVE_TEMP_RECEIVING_DIR);
         if(v != null) {
         	archiveTempReceivingDir = (String)v;
         }
 
         return archiveTempReceivingDir;
 	}
 
     public String getArchiveTempSendingDir() {
         String archiveTempSendingDir = connector.getArchiveTempSendingDir();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_ARCHIVE_TEMP_SENDING_DIR);
         if(v != null) {
         	archiveTempSendingDir = (String)v;
         }
 
         return archiveTempSendingDir;
 	}
 
     public boolean isUseTempFileTimestampSuffix() {
         boolean useTempFileTimestampSuffix = connector.isUseTempFileTimestampSuffix();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_USE_TEMP_FILE_TIMESTAMP_SUFFIX);
         if(v != null) {
         	useTempFileTimestampSuffix = Boolean.valueOf((String)v);
         }
 
         return useTempFileTimestampSuffix;
 	}
 
     public String getDuplicateHandling() {
         String duplicateHandling = connector.getDuplicateHandling();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_DUPLICATE_HANDLING);
         if(v != null) {
         	duplicateHandling = (String)v;
         }
 
         return duplicateHandling;
 	}
 
     public String getIdentityFile() {
         String identityFile = connector.getIdentityFile();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_IDENTITY_FILE);
         if(v != null) {
         	identityFile = (String)v;
         }
 
         return identityFile;
 	}
 
     public String getPassphrase() {
         String passphrase = connector.getPassphrase();
 
         // Override the value from the endpoint?
         Object v = endpoint.getProperty(SftpConnector.PROPERTY_PASS_PHRASE);
         if(v != null) {
         	passphrase = (String)v;
         }
 
         return passphrase;
 	}
 
 	public void createSftpDirIfNotExists(SftpClient sftpClient, String endpointDir) throws IOException
 	{
 		String tempDir = getTempDir();
 
 		String tempDirAbs = sftpClient.getAbsolutePath(endpointDir + "/" + tempDir);
 
 		// Try to change directory to the temp dir, if it fails - create it
 		try
 		{
 			// This method will throw an exception if the directory does not exist.
 			sftpClient.changeWorkingDirectory(tempDirAbs);
 		} catch (IOException e)
 		{
 			logger.info("Got an exception when trying to change the working directory to the temp dir. " +
 				"Will try to create the directory " + tempDirAbs);
 			sftpClient.changeWorkingDirectory(endpointDir);
 			sftpClient.mkdir(tempDir);
 			// Now it should exist!
 			sftpClient.changeWorkingDirectory(tempDirAbs);
 		}
 	}
 
   public boolean isKeepFileOnError() {
     boolean keepFileOnError = connector.isKeepFileOnError();
 
     // Override the value from the endpoint?
     Object v = endpoint.getProperty(SftpConnector.PROPERTY_KEEP_FILE_ON_ERROR);
     if (v != null) {
       return Boolean.valueOf((String) v);
     }
 
     return keepFileOnError;
   }
 }
