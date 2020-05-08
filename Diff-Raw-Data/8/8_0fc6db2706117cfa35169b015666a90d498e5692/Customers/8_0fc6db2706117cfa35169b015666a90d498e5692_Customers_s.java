 /*
  * ###
  * Service Web Archive
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.service.admin.actions.admin;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.Customer.LicenseType;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 
 public class Customers extends ServiceBaseAction  { 
 	
 	private static final long serialVersionUID = 6801037145464060759L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(Customers.class);
 	private static Boolean s_isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private String customerId = "";
 	
 	private String name = "";
 	private String description = "";
 	private String email = "";
 	private String address = "";
 	private String country = "";
     private String state = "";
     private String zipcode = "";
     private String number = "";
     private String fax = "";
 	private String helpText = "";
     private String licence = "";
     private Date validFrom = null;
     private Date validUpTo = null;
     private String repoName = "";
 	private String repoUserName = "";
 	private String repoPassword = "";
 	private String repoURL = "";
 	
 	private String nameError = "";
 	private String mailError = "";
 	private String addressError = "";
 	private String zipError = "";
 	private String numError = "";
 	private String faxError = "";
 	private String conError = "";
 	private String licenError = "";
 	private String repoNameError = "";
 	private String repoUserNameError = "";
 	private String repoPasswordError = "";
 	private String repoURLError = "";
 	private boolean errorFound = false;
 	
 	private String fromPage = "";
 	
 	private String oldName = "";
 
     /**
      * To get the all the customers from the DB
      * @return List of Customer
      * @throws PhrescoException
      */
     public String list() throws PhrescoException {
 	    if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Customers.list()");
 	    }
 		
 		try {
             List<Customer> customers = getServiceManager().getCustomers();
 			setReqAttribute(REQ_CUST_CUSTOMERS, customers);
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_LIST));
 		}
 		
 		return ADMIN_CUSTOMER_LIST;	
 	}
 	
 	/**
 	 * To return the page to add customer 
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public String add() throws PhrescoException {
 	    if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Customers.add()");
 	    }
 	    setReqAttribute(REQ_FROM_PAGE, ADD);
 	    
 		return ADMIN_CUSTOMER_ADD;
 	}
 	
 	/**
 	 * To return the edit page with the details of the selected customer
 	 * @param customerId
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public String edit() throws PhrescoException {
 		if (s_isDebugEnabled) {
 			S_LOGGER.debug("Entering Method Customers.edit()");
 		}
 
 		try {
 			Customer customer = getServiceManager().getCustomer(getCustomerId());
 			setReqAttribute(REQ_CUST_CUSTOMER, customer);
 			setReqAttribute(REQ_FROM_PAGE, EDIT);
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_ADD));
 		    
 		}
 		
 		return ADMIN_CUSTOMER_ADD;
 	}
 	
 	/**
 	 * To create a customer with the provided details
 	 * @return List of customers
 	 * @throws PhrescoException
 	 */
 	public String save() throws PhrescoException {
 	    if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Customers.save()");
 	    }
 	    
 		try {
 			List<Customer> customers = new ArrayList<Customer>();
 			customers.add(createCustomer());
 			getServiceManager().createCustomers(customers);
 			addActionMessage(getText(CUSTOMER_ADDED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_SAVE));
 		}
 		
 		return list();
 	}
 
 	/**
 	 * To update the details of the selected customer
 	 * @param cutomerId
 	 * @return List of customers
 	 * @throws PhrescoException
 	 */
 	public String update() throws PhrescoException {
 	    if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Customers.update()");
 	    }

 		try {
 			getServiceManager().updateCustomer(createCustomer(), getCustomerId());
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_UPDATE));
 		}
 		
 		return list();
 	}
 	
 	/**
 	 * To the customer object with the given details
 	 * @return customer object
 	 */
 	private Customer createCustomer() {
         Customer customer = new Customer();
//        customer.setId(getCustomerId());
         customer.setName(getName());
         customer.setDescription(getDescription());
         customer.setEmailId(getEmail());
         customer.setAddress(getAddress());
         customer.setCountry(getCountry());
         customer.setState(getState());
         customer.setZipcode(getZipcode());
         customer.setContactNumber(getNumber());
         customer.setFax(getFax());
         customer.setHelpText(getHelpText());
         LicenseType licenceType = LicenseType.valueOf(getLicence());
         customer.setType(licenceType);
         customer.setValidFrom(getValidFrom());
         customer.setValidUpto(getValidUpTo());
         RepoInfo repoInfo = new RepoInfo();
         repoInfo.setReleaseRepoURL(getRepoURL());
         repoInfo.setRepoPassword(getRepoPassword());
         repoInfo.setRepoUserName(getRepoUserName());
         repoInfo.setRepoName(getRepoName());
         customer.setRepoInfo(repoInfo);
         return customer;
     }
 	
 	/**
 	 * To delete the selected customers
 	 * @param List of customerIds
 	 * @return list of customers
 	 * @throws PhrescoException
 	 */
 	public String delete() throws PhrescoException {
 	    if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Customers.delete()");
 	    }
 		
 		try {
 			String[] customerIds = getHttpRequest().getParameterValues(REQ_CUST_CUSTOMER_ID);
 			if (ArrayUtils.isNotEmpty(customerIds)) {
 				for (String customerId : customerIds) {
 			    	getServiceManager().deleteCustomer(customerId);
 				}
 				addActionMessage(getText(CUSTOMER_DELETED));
 			}
 		} catch (PhrescoException e) {
 		    return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_DELETE));
 		}
 		
 		return list();
 	}
 	
 	/**
 	 * To validate the form values passed from the jsp
 	 * @return vaidation true/false
 	 * @throws PhrescoException
 	 */
 	public String validateForm() throws PhrescoException {
 	    if (s_isDebugEnabled) {
 	        S_LOGGER.debug("Entering Method Customers.validateForm()");
         }
 	    
 	    try {
     		boolean isError = false;
     		if (StringUtils.isEmpty(getName())) { //Empty validation for name
     			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY));
     			isError = true;
     		} else if (StringUtils.isEmpty(getFromPage()) || (!getName().equals(getOldName()))) {
     			// to check duplication of name
     			List<Customer> customers = getServiceManager().getCustomers();
     			if (CollectionUtils.isNotEmpty(customers)) {
     				for (Customer customer : customers) {
     					if (customer.getName().equalsIgnoreCase(getName())) {
     						setNameError(getText(KEY_I18N_ERR_NAME_ALREADY_EXIST));
     			    		isError = true;
     						break;
     					}
     				}
     			}
     		}
     		
     		//Empty validation for email
     		if (StringUtils.isEmpty(getEmail())) {
     			setMailError(getText(KEY_I18N_ERR_EMAIL_EMPTY));
     			isError = true;
     		} 
     		
     		//EmailId format validation
     		if (StringUtils.isNotEmpty(getEmail())) {
     	   		Pattern p = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
     	   		Matcher m = p.matcher(getEmail());
     	   		boolean b = m.matches();
     	   		if (!b) {
     	   			setMailError(getText(INVALID_EMAIL));
     	   			isError = true;
     	   		}
     	   	}
     		
     		//Empty validation for address
     		if (StringUtils.isEmpty(getAddress())) {
     			setAddressError(getText(KEY_I18N_ERR_ADDRS_EMPTY));
     			isError = true;
     		} 
     		
     		//Empty validation for zip code
     		if (StringUtils.isEmpty(getZipcode())) {
     			setZipError(getText(KEY_I18N_ERR_ZIPCODE_EMPTY));
     			isError = true;
     		} 
     		
     		//Empty validation for contact number
     		if (StringUtils.isEmpty(getNumber())) {
     			setNumError(getText(KEY_I18N_ERR_CONTNUM_EMPTY));
     			isError = true;
     		} 
     		
     		//Empty validation for fax
     		if (StringUtils.isEmpty(getFax())) {
     			setFaxError(getText(KEY_I18N_ERR_FAXNUM_EMPTY));
     			isError = true;
     		} 
     		
     		//Empty validation for country
     		if (StringUtils.isEmpty(getCountry())) {
     			setConError(getText(KEY_I18N_ERR_COUN_EMPTY));
     			isError = true;
     		} 
     		
     		//Empty validation for license type
     		if (StringUtils.isEmpty(getLicence())) {
     			setLicenError(getText(KEY_I18N_ERR_LICEN_EMPTY));
     			isError = true;
     		}
     		
     		//Empty validation for license type
     		if (StringUtils.isEmpty(getRepoName())) {
     			setRepoNameError(getText(KEY_I18N_ERR_REPO_NAME_EMPTY));
     			isError = true;
     		}
     		
     		if (StringUtils.isNotEmpty(getRepoURL())) {
     			String urlPattern = "^(http|https|ftp)://.*$";
     			Pattern pattern = Pattern.compile(urlPattern);
     			Matcher matcher = pattern.matcher(getRepoURL());
     	   		boolean matchFound = matcher.matches();
     	   		if (!matchFound) {
     	   			setRepoURLError(getText(KEY_I18N_ERR_REPO_URL_INVALID));
     	   			isError = true;
     	   		}
     		}
     		
     		if (StringUtils.isNotEmpty(getRepoURL())) {
     			//Empty validation for repo username
         		if (StringUtils.isEmpty(getRepoUserName())) {
         			setRepoUserNameError(getText(KEY_I18N_ERR_REPO_USERNAME_EMPTY));
         			isError = true;
         		}
         		//Empty validation for repo password
         		if (StringUtils.isEmpty(getRepoPassword())) {
         			setRepoPasswordError(getText(KEY_I18N_ERR_REPO_PASSWORD_EMPTY));
         			isError = true;
         		}
     		}
     		
     		if (isError) {
                 setErrorFound(true);
             }
 	    } catch (PhrescoException e) {
 	    	e.printStackTrace();
 	        return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_VALIDATE));
 	    }
 		
 		return SUCCESS;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getNameError() {
 		return nameError;
 	}
 	
 	public void setNameError(String nameError) {
 		this.nameError = nameError;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getMailError() {
 		return mailError;
 	}
 
 	public void setMailError(String mailError) {
 		this.mailError = mailError;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getAddressError() {
 		return addressError;
 	}
 
 	public void setAddressError(String addressError) {
 		this.addressError = addressError;
 	}
 	
 	public String getZipcode() {
 		return zipcode;
 	}
 
 	public void setZipcode(String zipcode) {
 		this.zipcode = zipcode;
 	}
 
 	public String getZipError() {
 		return zipError;
 	}
 
 	public void setZipError(String zipError) {
 		this.zipError = zipError;
 	}
 
 	public String getNumber() {
 		return number;
 	}
 
 	public void setNumber(String number) {
 		this.number = number;
 	}
 
 	public String getNumError() {
 		return numError;
 	}
 
 	public void setNumError(String numError) {
 		this.numError = numError;
 	}
 
 	public String getFax() {
 		return fax;
 	}
 
 	public void setFax(String fax) {
 		this.fax = fax;
 	}
 
 	public String getFaxError() {
 		return faxError;
 	}
 
 	public void setFaxError(String faxError) {
 		this.faxError = faxError;
 	}
 	
 	public String getCountry() {
 		return country;
 	}
 
 	public void setCountry(String country) {
 		this.country = country;
 	}
 
 	public String getConError() {
 		return conError;
 	}
 
 	public void setConError(String conError) {
 		this.conError = conError;
 	}
 
 	public String getLicence() {
 		return licence;
 	}
 
 	public void setLicence(String licence) {
 		this.licence = licence;
 	}
 
 	public String getLicenError() {
 		return licenError;
 	}
 
 	public void setLicenError(String licenError) {
 		this.licenError = licenError;
 	}
 	
 	public boolean isErrorFound() {
 		return errorFound;
 	}
 	
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	public Date getValidFrom() {
 		return validFrom;
 	}
 
 	public void setValidFrom(Date validFrom) {
 		this.validFrom = validFrom;
 	}
 
 	public Date getValidUpTo() {
 		return validUpTo;
 	}
 
 	public void setValidUpTo(Date validUpTo) {
 		this.validUpTo = validUpTo;
 	}
 	
 	public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 
 	public String getCustomerId() {
 		return customerId;
 	}
 
 	public void setCustomerId(String customerId) {
 		this.customerId = customerId;
 	}
 	
 	public String getOldName() {
 		return oldName;
 	}
 
 	public void setOldName(String oldName) {
 		this.oldName = oldName;
 	}
 	
 	public String getState() {
         return state;
     }
 
     public void setState(String state) {
         this.state = state;
     }
     
     public String getHelpText() {
         return helpText;
     }
 
     public void setHelpText(String helpText) {
         this.helpText = helpText;
     }
 
 	public void setRepoURL(String repoURL) {
 		this.repoURL = repoURL;
 	}
 
 	public String getRepoURL() {
 		return repoURL;
 	}
 
 	public void setRepoPassword(String repoPassword) {
 		this.repoPassword = repoPassword;
 	}
 
 	public String getRepoPassword() {
 		return repoPassword;
 	}
 
 	public void setRepoUserName(String repoUserName) {
 		this.repoUserName = repoUserName;
 	}
 
 	public String getRepoUserName() {
 		return repoUserName;
 	}
 
 	public void setRepoName(String repoName) {
 		this.repoName = repoName;
 	}
 
 	public String getRepoName() {
 		return repoName;
 	}
 	
 	public String getRepoNameError() {
 		return repoNameError;
 	}
 
 	public void setRepoNameError(String repoNameError) {
 		this.repoNameError = repoNameError;
 	}
 
 	public void setRepoUserNameError(String repoUserNameError) {
 		this.repoUserNameError = repoUserNameError;
 	}
 
 	public String getRepoUserNameError() {
 		return repoUserNameError;
 	}
 
 	public void setRepoPasswordError(String repoPasswordError) {
 		this.repoPasswordError = repoPasswordError;
 	}
 
 	public String getRepoPasswordError() {
 		return repoPasswordError;
 	}
 
 	public void setRepoURLError(String repoURLError) {
 		this.repoURLError = repoURLError;
 	}
 
 	public String getRepoURLError() {
 		return repoURLError;
 	}
 }
