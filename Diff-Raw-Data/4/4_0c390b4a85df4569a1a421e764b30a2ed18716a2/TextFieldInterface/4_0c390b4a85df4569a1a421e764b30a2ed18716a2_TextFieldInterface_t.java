 
 package edu.common.dynamicextensions.domaininterface.userinterface;
 
 /**
  * TextFieldInterface stores necessary information for generating TextField control on
  * dynamically generated user interface.  
  * @author geetika_bangard
  */
 public interface TextFieldInterface extends ControlInterface
 {
 
 	/**
 	 * @return Returns the columns.
 	 */
 	Integer getColumns();
 
 	/**
 	 * @param columns The columns to set.
 	 */
 	void setColumns(Integer columns);
 
 	/**
 	 * @return Returns the isPassword.
 	 */
 	Boolean getIsPassword();
 
 	/**
 	 * @param isPassword The isPassword to set.
 	 */
 	void setIsPassword(Boolean isPassword);
 
 	/**
 	 * This method returns the Boolean value that decides whether the value of this control should be treated as normal text or URL.
	 * @return
 	 */
 	Boolean getIsUrl();
 
 	/**
 	 * This method sets the value that decides whether the value of this control should be treated as normal text or URL.
 	 * @param isUrl the Boolean value	true - value is URL
 	 * 									false - value is normal text.
 	 */
 	void setIsUrl(Boolean isUrl);
 }
