 package ca.ubc.ctlt.encryption;
 
 import java.util.Properties;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ronald
  * Date: 2013-10-21
  * Time: 5:31 PM
  * Manages the actual ctlt.encryption and decription process
  */
 public class EncryptManager {
 
 	Encryption encryptInstance = new Encryption();
 
 	public Properties encrypt(Properties propObj)
     {
     	String userIDString = propObj.getProperty("user_id");
     	String nameGiven = propObj.getProperty("lis_person_name_given");
     	String nameFamily = propObj.getProperty("lis_person_name_family");
     	String nameFull = propObj.getProperty("lis_person_name_full");
     	String[] emailPrimary = propObj.getProperty("lis_person_contact_email_primary").split("(?=@)");
     	String sourceDID = propObj.getProperty("lis_person_sourcedid");
     	 
     	 try {
 			propObj.setProperty("user_id", encryptInstance.encrypt(userIDString));
 			propObj.setProperty("lis_person_name_given", encryptInstance.encrypt(nameGiven));
 			propObj.setProperty("lis_person_name_family", encryptInstance.encrypt(nameFamily));
 			propObj.setProperty("lis_person_name_full", encryptInstance.encrypt(nameFull));
			propObj.setProperty("lis_person_contact_email_primary", emailPrimary.length > 1 ? (encryptInstance.encrypt(emailPrimary[0]) + emailPrimary[1]) : "");
 			propObj.setProperty("lis_person_sourcedid", encryptInstance.encrypt(sourceDID));
 		} catch (Exception e) {
 		}
     	return propObj;
     }
 }
