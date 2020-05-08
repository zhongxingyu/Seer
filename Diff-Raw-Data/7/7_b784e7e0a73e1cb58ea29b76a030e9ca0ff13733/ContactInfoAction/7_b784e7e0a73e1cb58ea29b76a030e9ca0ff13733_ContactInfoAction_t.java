 /* SVN FILE: $Id: ContactInfoAction.java 5997 2013-01-10 05:58:37Z jal55 $ */
 package edu.psu.iam.cpr.ip.ui.action;
 
 import org.apache.struts2.convention.annotation.Action;
 import org.apache.struts2.convention.annotation.Result;
 
 import edu.psu.iam.cpr.ip.ui.validation.FieldUtility;
 
 /**
  * ContactInfoAction blah-blah. 
  * 
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To 
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative 
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @author $Author: jal55 $
  * @version $Rev: 5997 $
  * @lastrevision $Date: 2013-01-10 00:58:37 -0500 (Thu, 10 Jan 2013) $
  */
 public class ContactInfoAction extends BaseAction 
 {
 
 	private String email;
 	private String phoneNumber;
 	private String extension;
 	private String internationalNumber;
 	private String phoneType;
 	private String emailType;
 	private String dob;
 	
 	/* (non-Javadoc)
 	 * @see edu.psu.iam.cpr.ui.action.BaseAction#execute()
 	 */
 	@Override
 	@Action(value="contact_info",results={ @Result(name=SUCCESS,location="/verify_info",type=REDIRECT),
             @Result(name="Welcome"       ,location="/welcome"         ,type=REDIRECT),
             @Result(name="DataAccuracy"  ,location="/data_accuracy"   ,type=REDIRECT),
             @Result(name="LegalName"     ,location="/legal_name"      ,type=REDIRECT),
             @Result(name="CurrentAddress",location="/current_address" ,type=REDIRECT),
             @Result(name="ContactInfo"   ,location="/contact_info"    ,type=REDIRECT),
             @Result(name="PersonalInfo"  ,location="/personal_info"   ,type=REDIRECT),
             @Result(name="IdentityInfo"  ,location="/identity_info"   ,type=REDIRECT),
             @Result(name="VerifyInfo"    ,location="/verify_info"     ,type=REDIRECT),
 			                               @Result(name="stay on page",location="/jsp/contactInformation.jsp"),
 				                           @Result(name="verify",location="/verify_info",type=REDIRECT),
                                            @Result(name="failure",location="/jsp/endPage.jsp")
                                          })
 	public String execute() 
 	{
 		// TODO Business logic for Contact Information
 		if(!setup("con"))
 		{
 			return FAILURE;
 		}
 		
 		log.debug(String.format("%s ", getUniqueId()))  ;
 		
 		phoneType = "PERMANENT_PHONE";
 		emailType = "OTHER_EMAIL";
 		
 		String returnLocation = "success";
 
 		if(FieldUtility.fieldIsNotPresent(getBtnsubmit()))
 		{
 			returnLocation =  STAY_ON_PAGE;
 		}
 		
 		// Reformat DOB to the correct format.
		if (getDob() != null) {
			String[] parts = getDob().split("/");
			setDob(String.format("%02d/%02d/%04d", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
		}
 		
 		// Save form data to session
 		saveFieldsToSession(getPrefix());
 		
 		return filterSuccess(returnLocation);
 	}
 
 
 	/**
 	 * @return the email
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 
 	/**
 	 * @param email the email to set
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 
 	/**
 	 * @return the phoneNumber
 	 */
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 
 
 	/**
 	 * @param phoneNumber the phoneNumber to set
 	 */
 	public void setPhoneNumber(String phoneNumber) {
 		this.phoneNumber = phoneNumber;
 	}
 
 
 	/**
 	 * @return the extension
 	 */
 	public String getExtension() {
 		return extension;
 	}
 
 
 	/**
 	 * @param extension the extension to set
 	 */
 	public void setExtension(String extension) {
 		this.extension = extension;
 	}
 
 
 	/**
 	 * @return the internationalNumber
 	 */
 	public String getInternationalNumber() {
 		return internationalNumber;
 	}
 
 
 	/**
 	 * @param internationalNumber the internationalNumber to set
 	 */
 	public void setInternationalNumber(String internationalNumber) {
 		this.internationalNumber = internationalNumber;
 	}
 
 
 	/**
 	 * @return the phoneType
 	 */
 	public String getPhoneType() {
 		return phoneType;
 	}
 
 
 	/**
 	 * @param phoneType the phoneType to set
 	 */
 	public void setPhoneType(String phoneType) {
 		this.phoneType = phoneType;
 	}
 
 
 	/**
 	 * @return the emailType
 	 */
 	public String getEmailType() {
 		return emailType;
 	}
 
 
 	/**
 	 * @param emailType the emailType to set
 	 */
 	public void setEmailType(String emailType) {
 		this.emailType = emailType;
 	}
 
 
 	/**
 	 * @param dob the dob to set
 	 */
 	public void setDob(String dob) {
 		this.dob = dob;
 	}
 
 
 	/**
 	 * @return the dob
 	 */
 	public String getDob() {
 		return dob;
 	}
 
 
 }
