 package gov.nih.nci.caintegrator.application.mail;
 
 import gov.nih.nci.caintegrator.exceptions.ValidationException;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 /**
  * 
  *  Manages sending of emails 
  *
  */
 public class MailManager {
 	
 	private static Logger logger = Logger.getLogger(MailManager.class);
 	private String mailProperties;
 	public MailManager(String mailPropertiesFilename){
 		mailProperties = mailPropertiesFilename;
 	}
 	/**
 	 * Sends an email notifying user that their files are ready for download
 	 * via FTP
 	 * 
 	 * @param mailTo - email address
 	 * @param fileName - filename
 	 * @param additionalText - This is used for sending the query parameters but can be null
 	 * @throws Exception
 	 */
 	public void sendFTPMail(String mailTo, List<String> fileNames, String additionalText)
 	{
 		try
 		{				
 		    // Part 1 is always included  
 		    String message = new MessageFormat(MailConfig.getInstance(mailProperties).getFtpUnformattedBody1()).format(
 		    	new String[] {MailConfig.getInstance(mailProperties).getFileRetentionPeriodInDays(),
 		    			      MailConfig.getInstance(mailProperties).getProject(),
 		    			      MailConfig.getInstance(mailProperties).getAcronym()});		  
 			
 		    // If there was additional text passed in then include that here
 		    if (additionalText != null)
 		    	message += additionalText + "\n\n";
 		    
 		    // Part 2 is only included if there are multiple fies
 		    if(fileNames.size() > 1)
 		    {
 		    	message += MailConfig.getInstance(mailProperties).getFtpUnformattedBody2();	
 		    }
 		    	
 		    // Part 3 appears once for each file
 		    for(String fileName : fileNames)
 		    {    	
 		    	message += new MessageFormat(MailConfig.getInstance(mailProperties).getFtpUnformattedBody3()).format(
 		    		new String[] {cleanFileName(fileName), MailConfig.getInstance(mailProperties).getFtpHostnameAndPort()});	
 		    }
 		    
 		    // Part 4 always appears  
 		    message += new MessageFormat(MailConfig.getInstance(mailProperties).getFtpUnformattedBody4()).format(
 		    	new String[] {MailConfig.getInstance(mailProperties).getAppSupportNumber(),
 		    			      MailConfig.getInstance(mailProperties).getTechSupportStartTime(),
 		    			      MailConfig.getInstance(mailProperties).getTechSupportEndTime(),
 		    			      MailConfig.getInstance(mailProperties).getAcronym(),
 		    			      MailConfig.getInstance(mailProperties).getTechSupportURL()});
 		    
 	        // Send the message
 	        new SendMail(mailProperties).sendMail(mailTo, null, message,formatFTPSubject());		
 		} 
 		catch (Exception e) 
 		{
 			logger.error("Send FTP mail error", e);
 		} 
 		catch (ValidationException e) 
 		{
 			logger.error("Send FTP mail error", e);
 		}
 	 }
 	
 	/**
 	 * sendFTPErrorMail is used to send a message to the user and copy the help desk when
 	 * a query was not able to successfully complete.
 	 * <P>
 	 * @param mailTo The email address of the user to be notified
 	 */
 	public void sendFTPErrorMail(String mailTo, String additionalText)
 	{
 		try
 		{				
 		    // Part 1 is always included  
 		    String message = new MessageFormat(MailConfig.getInstance(mailProperties).getFtpUnformattedErrorBody1()).format(
 		    	new String[] {MailConfig.getInstance(mailProperties).getFileRetentionPeriodInDays(),
 		    				  MailConfig.getInstance(mailProperties).getProject(),
 		    				  MailConfig.getInstance(mailProperties).getAcronym()});		  
 		    
		    // If there was additional text passed in then include that here
		    if (additionalText != null)
		    	message += additionalText + "\n\n";
		    
 		    // Part 2 always appears  
 		    message += new MessageFormat(MailConfig.getInstance(mailProperties).getFtpUnformattedErrorBody2()).format(
 		    	new String[] {MailConfig.getInstance(mailProperties).getAppSupportNumber(),
 		    				  MailConfig.getInstance(mailProperties).getTechSupportStartTime(),
 		    				  MailConfig.getInstance(mailProperties).getTechSupportEndTime(),
 		    				  MailConfig.getInstance(mailProperties).getAcronym(),
 		    				  MailConfig.getInstance(mailProperties).getTechSupportURL()});
 		    
 	        // Send the message
 		    String mailCC = MailConfig.getInstance(mailProperties).getTechSupportMail();
 		    
 	        new SendMail(mailProperties).sendMail(mailTo, mailCC, message, formatFTPErrorSubject());		
 		}
 		catch (Exception e)
 		{
 			logger.error("Send FTP mail error", e);
 		}
 		catch (ValidationException e)
 		{
 			logger.error("Send FTP mail error", e);
 		}
 	 }
 	
 	/**
 	 * sendUserRequestMail is used to send a message to the appropriate user approval email
 	 * address with the user request information.
 	 * <P>
 	 * @param msgBody The user request content to send to 
 	 */
 	public void sendUserRequestMail(String msgBody)
 	{
 		try
 		{				
 		    // Part 1 is always included  
 		    String message = new MessageFormat(MailConfig.getInstance(mailProperties).getRequestUnformattedBody()).format(new String[] {MailConfig.getInstance(mailProperties).getAcronym()});		  
 		    
 		    // Part 2 always appears  
 		    message += "\n\n" + msgBody;
 		    
 	        // Send the message
 		    String mailTo = MailConfig.getInstance(mailProperties).getUserRequestMail();
 		    String mailCC = MailConfig.getInstance(mailProperties).getUserRequestCC();
 		    
 	        new SendMail(mailProperties).sendMail(mailTo, mailCC, message, formatUserRequestSubject());		
 		}
 		catch (Exception e)
 		{
 			logger.error("Send user request mail error", e);
 		}
 		catch (ValidationException e)
 		{
 			logger.error("Send user request mail error", e);
 		}
 	 }
 
 
 	/**
 	 * Sends an email notifying user that their registration has been accepted
 	 * 
 	 * @param mailTo - email address
 	 * @param userId - user's login ID
 	 * @throws Exception
 	 */
 	public void sendConfirmationMail(String mailTo, String userId) throws Exception {
 	
 		try {
 		    String[] params = {userId, MailConfig.getInstance(mailProperties).getAppSupportNumber(), MailConfig.getInstance(mailProperties).getTechSupportStartTime(), MailConfig.getInstance(mailProperties).getTechSupportEndTime(),};
 			
 	        MessageFormat formatter = new MessageFormat(MailConfig.getInstance(mailProperties).getRegisterUnformattedBody());
 	        String formattedBody = formatter.format(params);		  
 				   
 	        new SendMail(mailProperties).sendMail(mailTo, null, formattedBody, formatFTPSubject());
 		} catch (Exception e) {
 			logger.error("Send confirmation mail error", e);
 		} catch (ValidationException e) {
 			logger.error("Send confirmation mail error", e);
 		}			
      }
 	
 
 	/**
 	 * The filename is sometimes sent in with a leading slash.  Clean it off.
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	private String cleanFileName(String fileName)
 	{
 		String nameWithoutPath;
 		if(fileName.lastIndexOf(File.separator)>0){
 			// Strip off path to just get the filename
 			nameWithoutPath = fileName.substring(fileName.lastIndexOf(File.separator)); 		
 		}
 		else{
 			nameWithoutPath = fileName; 		
 		}
 		return (nameWithoutPath.startsWith(File.separator)) ? nameWithoutPath.substring(1) : nameWithoutPath;
 	}
 	public String formatFromAddress(){
 		 String from = new MessageFormat(MailConfig.getInstance(mailProperties).getFrom()).format(new String[] {MailConfig.getInstance(mailProperties).getAcronym()});
 		 return from;
 	}
 	public String formatFTPSubject(){
 		 String ftpSubject = new MessageFormat(MailConfig.getInstance(mailProperties).getFtpSubject()).format(new String[] {MailConfig.getInstance(mailProperties).getProject(),MailConfig.getInstance(mailProperties).getAcronym()});
 		 return ftpSubject;
 	}
 	public String formatFTPErrorSubject(){
 		 String ftpSubject = new MessageFormat(MailConfig.getInstance(mailProperties).getFtpErrorSubject()).format(new String[] {MailConfig.getInstance(mailProperties).getProject(),MailConfig.getInstance(mailProperties).getAcronym()});
 		 return ftpSubject;
 	}
 	public String formatUserRequestSubject(){
 		 String subject = new MessageFormat(MailConfig.getInstance(mailProperties).getRequestSubject()).format(new String[] {MailConfig.getInstance(mailProperties).getProject(),MailConfig.getInstance(mailProperties).getAcronym()});
 		 return subject;
 	}
 
 }
