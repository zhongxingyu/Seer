 /**
  * Service Web Archive
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.service.admin.actions.admin;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.Customer.LicenseType;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyOptions;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.logger.SplunkLogger;
 import com.photon.phresco.service.admin.actions.ServiceBaseAction;
 import com.photon.phresco.util.ServiceConstants;
 
 public class Customers extends ServiceBaseAction  { 
 
 	private static final long serialVersionUID = 6801037145464060759L;
 
 	private static final SplunkLogger LOGGER = SplunkLogger.getSplunkLogger(Customers.class.getName());
 	//private static final Logger S_LOGGER = Logger.getLogger(Customers.class);
 	private static Boolean isDebugEnabled = LOGGER.isDebugEnabled();
 	private static Map<String, InputStream> inputStreamMap = new HashMap<String, InputStream>();
 
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
 	private List<String> options = null;
 
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
 	private String contextError = "";
 	private boolean errorFound = false;
 	private boolean tempError = false;
 	private static String uploadIconName = "";
 	private static byte[] loginIconByteArray = null;
 	private String icon = "";
 	private String snapshotRepoUrl = "";
 	private String groupRepoUrl = "";
 	private String baseRepoUrl = "";
 	private List<String> appliesTo = new ArrayList<String>();
 	List<ApplicationType> applicableAppTypes = new ArrayList<ApplicationType>();
 	
 	private String loginLogoMargin = "";
 	private String pageLogoPadding = "";
 	private String headerLinkColor = "";
 	private String headerActiveLinkColor = ""; 
 	private String editNavigationLink = "";
 	private String buttonBackGroundColor = "";
 	private String consoleHeaderColor = "";
 	private String copyrightLabelColor = "";
 	private String headerBackGroundcolorTop = "";
 	private String headerBackGroundcolorBottom = "";
 	private String footerBackGroundcolorTop = "";
 	private String footerBackGroundcolorBottom = "";
 	private String pageTitleBackGroundTop = "";
 	private String pageTitleBackGroundBottom = "";
 	private String editNavigationActiveBackGroundTop = "";
 	private String editNavigationActiveBackGroundBottom = "";
 	private String bottomButtonPanelTop = "";
 	private String bottomButtonPanelBottom = "";
 	private String customerBaseColor = "";
 	private String welcomeUserIcon = "";
 	private String context = "";
 	private String PageTitleColor = "";
 	private String copyrightText = "";
 	private String CustomerTitle = "";
 
 	private String fromPage = "";
 
 	private String oldName = "";
 	private String oldContext = "";
 
 	/**
 	 * To get the all the customers from the DB
 	 * @return List of Customer
 	 * @throws PhrescoException
 	 */
 	public String list() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.list : Entry");
 		}
 		try {
 			List<Customer> customers = getServiceManager().getCustomers();            
 			if (CollectionUtils.isNotEmpty(customers)) {
 				Collections.sort(customers, sortCustomerInAlphaOrder());
 			}
 			setReqAttribute(REQ_CUST_CUSTOMERS, customers);
 			setSessionAttribute(REQ_CUST_CUSTOMERS, customers);
 		} catch (PhrescoException e) {
 			if(isDebugEnabled) {
 				LOGGER.error("Customers.list", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_LIST));
 		}
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.list : Exit");
 		}
 		return ADMIN_CUSTOMER_LIST;	
 	}
 
 	private Comparator sortCustomerInAlphaOrder() {
 		return new Comparator() {
 			public int compare(Object firstObject, Object secondObject) {
 				Customer cus1 = (Customer) firstObject;
 				Customer cus2 = (Customer) secondObject;
 				return cus1.getName().compareToIgnoreCase(cus2.getName());
 			}
 		};
 	}
 
 	/**
 	 * To return the page to add customer 
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public String add() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.add : Entry");
 		}
 		List<TechnologyOptions> options = getServiceManager().getCustomerOptions();
 		List<Technology> technologies = getServiceManager().getArcheTypes(ServiceConstants.DEFAULT_CUSTOMER_NAME);
 		setReqAttribute(REQ_TECHNOLOGY_OPTION, options);
 		setReqAttribute(REQ_ARCHE_TYPES, technologies);
 		setReqAttribute(REQ_FROM_PAGE, ADD);
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.add : Exit");
 		}
 		return ADMIN_CUSTOMER_ADD;
 	}
 
 	/**
 	 * To return the edit page with the details of the selected customer
 	 * @param customerId
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public String edit() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.edit : Entry");
 		}
 		try {
 			if(isDebugEnabled) {
 				if (StringUtils.isEmpty(getCustomerId())) {
 					LOGGER.warn("Customers.add", "status=\"Bad Request\"", "message=\"CustomerId is empty\"");
 					return showErrorPopup(new PhrescoException("CustomerId is empty"), getText(EXCEPTION_CUSTOMERS_ADD));
 				}
 				LOGGER.info("Customers.add", "customerId=" + "\"" + getCustomerId()  + "\"");
 			}
 			Customer customer = getServiceManager().getCustomer(getCustomerId());
 			List<TechnologyOptions> options = getServiceManager().getCustomerOptions();
 			setReqAttribute(REQ_TECHNOLOGY_OPTION, options);
 			List<Technology> technologies = getServiceManager().getArcheTypes(ServiceConstants.DEFAULT_CUSTOMER_NAME);
 			setReqAttribute(REQ_ARCHE_TYPES, technologies);
 			setReqAttribute(REQ_CUST_CUSTOMER, customer);
 			setReqAttribute(REQ_FROM_PAGE, EDIT);
 		} catch (PhrescoException e) {
 			if(isDebugEnabled) {
 				LOGGER.error("Customers.edit", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_ADD));
 		}
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.edit : Exit");
 		}
 		return ADMIN_CUSTOMER_ADD;
 	}
 
 	/**
 	 * To create a customer with the provided details
 	 * @return List of customers
 	 * @throws PhrescoException
 	 */
 	public String save() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.save : Entry");
 		}
 		try {
 			Customer customer = createCustomer();
 			if (loginIconByteArray != null) {
 				inputStreamMap.put(getCustomerId(), new ByteArrayInputStream(loginIconByteArray));
 			}
			getServiceManager().createCustomers(customer, inputStreamMap);
 			addActionMessage(getText(CUSTOMER_ADDED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
 			if(isDebugEnabled) {
 				LOGGER.error("Customers.save", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_SAVE));
 		}
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.save : Exit");
 		}
 		return list();
 	}
 
 	/**
 	 * To update image Icon  selected customer
 	 * @param cutomerId
 	 * @throws PhrescoException
 	 */	
 	public String uploadLoginLogoImage() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.uploadImage : Entry");
 		}
 		PrintWriter writer = null;
 		try {
 			writer = getHttpResponse().getWriter();
 			loginIconByteArray = getByteArray();
 			writer.print(SUCCESS_TRUE);
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			//If upload fails it will be shown in UI, so need not to throw error popup
 			if(isDebugEnabled) {
 				LOGGER.error("Customers.uploadImage", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
 			writer.print(SUCCESS_FALSE);
 		}
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.uploadImage : Exit");
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * remove the icon image from byteArray and Map 
 	 * @return
 	 * @throws PhrescoException
 	 */
 	public String removeImage() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.removeImage : Entry");
 		}
 		inputStreamMap.clear();
 		loginIconByteArray = null;
 
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.removeImage : Exit");
 		}
 
 		return SUCCESS;
 	}
 
 
 	/**
 	 * To update the details of the selected customer
 	 * @param cutomerId
 	 * @return List of customers
 	 * @throws PhrescoException
 	 */
 	public String update() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.update : Entry");
 		}
 		try {
 			Customer customer = createCustomer();
 			if (loginIconByteArray != null) {
 				inputStreamMap.put(getCustomerId(), new ByteArrayInputStream(loginIconByteArray));
 			}
			getServiceManager().createCustomers(customer, inputStreamMap);
 			addActionMessage(getText(CUSTOMER_UPDATED, Collections.singletonList(getName())));
 		} catch (PhrescoException e) {
 			if(isDebugEnabled) {
 				LOGGER.error("Customers.update", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_UPDATE));
 		}
 
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.update : Exit");
 		}
 
 		return list();
 	}
 
 
 	/**
 	 * To the customer object with the given details
 	 * @return customer object
 	 * @throws PhrescoException 
 	 */
 	private Customer createCustomer() throws PhrescoException {
 		Customer customer = new Customer();
 		try {
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
 			customer.setOptions(getOptions());
 			RepoInfo repoInfo = new RepoInfo();
 			if (StringUtils.isNotEmpty(getCustomerId())) {
 				customer.setId(getCustomerId());
 				repoInfo.setCustomerId(getCustomerId());
 			}
 			repoInfo.setReleaseRepoURL(getRepoURL());
 			repoInfo.setRepoPassword(getRepoPassword());
 			repoInfo.setRepoUserName(getRepoUserName());
 			repoInfo.setRepoName(getRepoName());
 			if (StringUtils.isNotEmpty(getSnapshotRepoUrl())){
 				repoInfo.setSnapshotRepoURL(getSnapshotRepoUrl());
 			}
 			if (StringUtils.isNotEmpty(getGroupRepoUrl())){
 				repoInfo.setGroupRepoURL(getGroupRepoUrl());
 			}
 			if (StringUtils.isNotEmpty(getBaseRepoUrl())){
 				repoInfo.setBaseRepoURL(getBaseRepoUrl());
 			}
 			customer.setRepoInfo(repoInfo);
 			List<String> appliesTo = getAppliesTo();
 			customer.setApplicableTechnologies(appliesTo);
 			Map<String, String> frameworkTheme = new HashMap<String, String>();
 			frameworkTheme.put("loginLogoMargin" , getLoginLogoMargin());
 			frameworkTheme.put("pageLogoPadding", getPageLogoPadding());
 			frameworkTheme.put("headerLinkColor", getHeaderLinkColor());
 			frameworkTheme.put("headerActiveLinkColor", getHeaderActiveLinkColor());
 			frameworkTheme.put("editNavigationLink", getEditNavigationLink());
 			frameworkTheme.put("buttonBackGroundColor", getButtonBackGroundColor());
 			frameworkTheme.put("consoleHeaderColor", getConsoleHeaderColor());
 			frameworkTheme.put("copyrightLabelColor", getCopyrightLabelColor());
 			frameworkTheme.put("headerBackGroundcolorTop", getHeaderBackGroundcolorTop());
 			frameworkTheme.put("headerBackGroundcolorBottom", getHeaderBackGroundcolorBottom());
 			frameworkTheme.put("footerBackGroundcolorTop", getFooterBackGroundcolorTop());
 			frameworkTheme.put("footerBackGroundcolorBottom", getFooterBackGroundcolorBottom());
 			frameworkTheme.put("pageTitleBackGroundTop", getPageTitleBackGroundTop());
 			frameworkTheme.put("pageTitleBackGroundBottom", getPageTitleBackGroundBottom());
 			frameworkTheme.put("editNavigationActiveBackGroundTop", getEditNavigationActiveBackGroundTop());
 			frameworkTheme.put("editNavigationActiveBackGroundBottom", getEditNavigationActiveBackGroundBottom());
 			frameworkTheme.put("bottomButtonPanelTop", getBottomButtonPanelTop());
 			frameworkTheme.put("bottomButtonPanelBottom", getBottomButtonPanelBottom());
 			frameworkTheme.put("customerBaseColor", getBottomButtonPanelBottom());
 			frameworkTheme.put("welcomeUserIcon", getWelcomeUserIcon());
 			frameworkTheme.put("pageTitleColor", getPageTitleColor());
 			frameworkTheme.put("copyRightLabel", getCopyrightText());
 			frameworkTheme.put("customerTitle", getCustomerTitle());
 			customer.setFrameworkTheme(frameworkTheme);
 			customer.setContext(context);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return customer;
 	}
 
 	/**
 	 * To delete the selected customers
 	 * @param List of customerIds
 	 * @return list of customers
 	 * @throws PhrescoException
 	 */
 	public String delete() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.delete : Entry");
 		}
 		try {
 			String[] customerIds = getHttpRequest().getParameterValues(REQ_CUST_CUSTOMER_ID);
 			if(isDebugEnabled) {
 				if (customerIds == null) {
 					LOGGER.warn("Customers.delete", "status=\"Bad Request\"", "message=\"CustomerIds is empty\"");
 					return showErrorPopup(new PhrescoException("CustomerIds is empty"), getText(CUSTOMER_DELETED));
 				}
 				LOGGER.info("Customers.delete", "customerIds=" + "\"" + customerIds);
 			}
 			if (ArrayUtils.isNotEmpty(customerIds)) {
 				for (String customerid : customerIds) {
 					getServiceManager().deleteCustomer(customerid);
 				}
 				addActionMessage(getText(CUSTOMER_DELETED));
 			}
 		} catch (PhrescoException e) {
 			if(isDebugEnabled) {
 				LOGGER.error("Customers.delete", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_DELETE));
 		}
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.delete : Exit");
 		}
 		return list();
 	}
 
 	/**
 	 * To validate the form values passed from the jsp
 	 * @return vaidation true/false
 	 * @throws PhrescoException
 	 */
 	public String validateForm() throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.validateForm : Entry");
 		}
 		try {
 			boolean isError = false;
 
 			//Empty validation for name
 			isError = nameValidation(isError);
 
 			//Empty validation for email
 			isError = emailValidation(isError); 
 
 			//EmailId format validation
 			isError = emailIdFormatValidation(isError);
 
 			//Empty validation for address
 			isError = addressValidation(isError); 
 
 			//Empty validation for zip code
 			isError = zipCodeValidation(isError); 
 
 			//Empty validation for contact number
 			isError = contactNumberValidation(isError); 
 
 			//Empty validation for fax
 			isError = faxValidation(isError); 
 
 			//Empty validation for country
 			isError = countryValidation(isError); 
 
 			//Empty validation for license type
 			isError = licenseTypeValidation(isError);
 
 			//Empty validation for repo name
 			isError = repoNameValidation(isError);
 
 			isError = repoUrlValidation(isError);
 
 			//Empty vaildation for context
 			isError = contextValidation();
 
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
 			if(isDebugEnabled) {
 				LOGGER.error("Customers.validateForm", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			return showErrorPopup(e, getText(EXCEPTION_CUSTOMERS_VALIDATE));
 		}
 		if (isDebugEnabled) {
 			LOGGER.debug("Customers.validateForm : Exit");
 		}
 		return SUCCESS;
 	}
 
 	public boolean repoUrlValidation(boolean isError) {
 		if (StringUtils.isNotEmpty(getRepoURL())) {
 			String urlPattern = "^(http|https|ftp)://.*$";
 			Pattern pattern = Pattern.compile(urlPattern);
 			Matcher matcher = pattern.matcher(getRepoURL());
 			boolean matchFound = matcher.matches();
 			if (!matchFound) {
 				setRepoURLError(getText(KEY_I18N_ERR_REPO_URL_INVALID));
 				tempError = true;
 			}
 		}
 		return tempError;
 	}
 
 	public boolean repoNameValidation(boolean isError) {
 		if (StringUtils.isEmpty(getRepoName())) {
 			setRepoNameError(getText(KEY_I18N_ERR_REPO_NAME_EMPTY));
 			tempError = true;
 		}
 		return tempError;
 	}
 
 	public boolean licenseTypeValidation(boolean isError) {
 		if (StringUtils.isEmpty(getLicence())) {
 			setLicenError(getText(KEY_I18N_ERR_LICEN_EMPTY));
 			tempError = true;
 		}
 		return tempError;
 	}
 
 	public boolean countryValidation(boolean isError) {
 		if (StringUtils.isEmpty(getCountry())) {
 			setConError(getText(KEY_I18N_ERR_COUN_EMPTY));
 			tempError = true;
 		}
 		return tempError;
 	}
 
 	public boolean faxValidation(boolean isError) {
 		if (StringUtils.isEmpty(getFax())) {
 			setFaxError(getText(KEY_I18N_ERR_FAXNUM_EMPTY));
 			tempError = true;
 		}
 		return tempError;
 	}
 
 	public boolean contactNumberValidation(boolean isError) {
 		if (StringUtils.isEmpty(getNumber())) {
 			setNumError(getText(KEY_I18N_ERR_CONTNUM_EMPTY));
 			tempError = true;
 		}
 		return tempError;
 	}
 
 	public boolean zipCodeValidation(boolean isError) {
 		if (StringUtils.isEmpty(getZipcode())) {
 			setZipError(getText(KEY_I18N_ERR_ZIPCODE_EMPTY));
 			tempError = true;
 		}
 		return tempError;
 	}
 
 	public boolean addressValidation(boolean isError) {
 		if (StringUtils.isEmpty(getAddress())) {
 			setAddressError(getText(KEY_I18N_ERR_ADDRS_EMPTY));
 			tempError = true;
 		}
 		return tempError;
 	}
 
 	public boolean emailIdFormatValidation(boolean isError) {
 		if (StringUtils.isNotEmpty(getEmail())) {
 			Pattern p = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
 			Matcher m = p.matcher(getEmail());
 			boolean b = m.matches();
 			if (!b) {
 				setMailError(getText(INVALID_EMAIL));
 				tempError = true;
 			}
 		}
 		return tempError;
 	}
 
 	public boolean emailValidation(boolean isError) {
 		if (StringUtils.isEmpty(getEmail())) {
 			setMailError(getText(KEY_I18N_ERR_EMAIL_EMPTY));
 			tempError = true;
 		}
 		return tempError;
 	}
 
 	public boolean nameValidation(boolean isError) throws PhrescoException {
 		if (StringUtils.isEmpty(getName())) { 
 			setNameError(getText(KEY_I18N_ERR_NAME_EMPTY));
 			tempError = true;
 		} else if (ADD.equals(getFromPage()) || (!getName().equals(getOldName()))) {
 			// to check duplication of name
 			List<Customer> customers = getServiceManager().getCustomers();
 			if (CollectionUtils.isNotEmpty(customers)) {
 				for (Customer customer : customers) {
 					if (customer.getName().equalsIgnoreCase(getName())) {
 						setNameError(getText(KEY_I18N_ERR_NAME_ALREADY_EXIST));
 						tempError = true;
 						break;
 					}
 				}
 			}
 		}
 		return tempError;
 	}
 
 	private boolean contextValidation() throws PhrescoException {
 		if (StringUtils.isEmpty(getContext())) {
 			setContextError(getText(KEY_I18N_ERR_CONTEXT_EMPTY));
 			tempError = true;
 		} else if (ADD.equals(getFromPage()) || (!getContext().equals(getOldContext()))) {
 			List<Customer> customers = getServiceManager().getCustomers();
 			if (CollectionUtils.isNotEmpty(customers)) {
 				for (Customer customer : customers) {
 					if (customer.getContext().equalsIgnoreCase(getContext())) {
 						setContextError(getText(KEY_18N_ERR_CONTEXT_ALREADY_EXIST));
 						tempError = true;
 						break;
 					}		
 				}
 			}
 		}
 		return tempError;
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
 
 	
 	public String getCustomerBaseColor() {
 		return customerBaseColor;
 	}
 
 	public void setCustomerBaseColor(String customerBaseColor) {
 		this.customerBaseColor = customerBaseColor;
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
 
 	public String getIcon() {
 		return icon;
 	}
 
 	public List<ApplicationType> getApplicableAppTypes() {
 		return applicableAppTypes;
 	}
 
 	public void setIcon(String icon) {
 		this.icon = icon;
 	}
 
 	public void setApplicableAppTypes(List<ApplicationType> applicableAppTypes) {
 		this.applicableAppTypes = applicableAppTypes;
 	}
 
 	public List<String> getAppliesTo() {
 		return appliesTo;
 	}
 
 	public void setAppliesTo(List<String> appliesTo) {
 		this.appliesTo = appliesTo;
 	}
 
 
 	public String getSnapshotRepoUrl() {
 		return snapshotRepoUrl;
 	}
 
 	public void setSnapshotRepoUrl(String snapshotRepoUrl) {
 		this.snapshotRepoUrl = snapshotRepoUrl;
 	}
 
 	public String getGroupRepoUrl() {
 		return groupRepoUrl;
 	}
 
 	public void setGroupRepoUrl(String groupRepoUrl) {
 		this.groupRepoUrl = groupRepoUrl;
 	}
 
 	public String getBaseRepoUrl() {
 		return baseRepoUrl;
 	}
 
 	public void setBaseRepoUrl(String baseRepoUrl) {
 		this.baseRepoUrl = baseRepoUrl;
 	}
 
 	public void setOptions(List<String> options) {
 		this.options = options;
 	}
 
 	public List<String> getOptions() {
 		return options;
 	}
 
 	public void setContext(String context) {
 		this.context = context;
 	}
 
 	public String getContext() {
 		return context;
 	}
 
 	public void setContextError(String contextError) {
 		this.contextError = contextError;
 	}
 
 	public String getContextError() {
 		return contextError;
 	}
 
 	public void setOldContext(String oldContext) {
 		this.oldContext = oldContext;
 	}
 
 	public String getOldContext() {
 		return oldContext;
 	}
 
 	public static String getUploadIconName() {
 		return uploadIconName;
 	}
 
 	public static void setUploadIconName(String uploadIconName) {
 		Customers.uploadIconName = uploadIconName;
 	}
 
 	public String getLoginLogoMargin() {
 		return loginLogoMargin;
 	}
 
 	public void setLoginLogoMargin(String loginLogoMargin) {
 		this.loginLogoMargin = loginLogoMargin;
 	}
 
 	public String getPageLogoPadding() {
 		return pageLogoPadding;
 	}
 
 	public void setPageLogoPadding(String pageLogoPadding) {
 		this.pageLogoPadding = pageLogoPadding;
 	}
 
 	public String getHeaderLinkColor() {
 		return headerLinkColor;
 	}
 
 	public void setHeaderLinkColor(String headerLinkColor) {
 		this.headerLinkColor = headerLinkColor;
 	}
 
 	public String getHeaderActiveLinkColor() {
 		return headerActiveLinkColor;
 	}
 
 	public void setHeaderActiveLinkColor(String headerActiveLinkColor) {
 		this.headerActiveLinkColor = headerActiveLinkColor;
 	}
 
 	public String getEditNavigationLink() {
 		return editNavigationLink;
 	}
 
 	public void setEditNavigationLink(String editNavigationLink) {
 		this.editNavigationLink = editNavigationLink;
 	}
 
 	public String getButtonBackGroundColor() {
 		return buttonBackGroundColor;
 	}
 
 	public void setButtonBackGroundColor(String buttonBackGroundColor) {
 		this.buttonBackGroundColor = buttonBackGroundColor;
 	}
 
 	public String getConsoleHeaderColor() {
 		return consoleHeaderColor;
 	}
 
 	public void setConsoleHeaderColor(String consoleHeaderColor) {
 		this.consoleHeaderColor = consoleHeaderColor;
 	}
 
 	public String getCopyrightLabelColor() {
 		return copyrightLabelColor;
 	}
 
 	public void setCopyrightLabelColor(String copyrightLabelColor) {
 		this.copyrightLabelColor = copyrightLabelColor;
 	}
 
 	public String getHeaderBackGroundcolorTop() {
 		return headerBackGroundcolorTop;
 	}
 
 	public void setHeaderBackGroundcolorTop(String headerBackGroundcolorTop) {
 		this.headerBackGroundcolorTop = headerBackGroundcolorTop;
 	}
 
 	public String getHeaderBackGroundcolorBottom() {
 		return headerBackGroundcolorBottom;
 	}
 
 	public void setHeaderBackGroundcolorBottom(String headerBackGroundcolorBottom) {
 		this.headerBackGroundcolorBottom = headerBackGroundcolorBottom;
 	}
 
 	public String getFooterBackGroundcolorTop() {
 		return footerBackGroundcolorTop;
 	}
 
 	public void setFooterBackGroundcolorTop(String footerBackGroundcolorTop) {
 		this.footerBackGroundcolorTop = footerBackGroundcolorTop;
 	}
 
 	public String getFooterBackGroundcolorBottom() {
 		return footerBackGroundcolorBottom;
 	}
 
 	public void setFooterBackGroundcolorBottom(String footerBackGroundcolorBottom) {
 		this.footerBackGroundcolorBottom = footerBackGroundcolorBottom;
 	}
 
 	public String getPageTitleBackGroundTop() {
 		return pageTitleBackGroundTop;
 	}
 
 	public void setPageTitleBackGroundTop(String pageTitleBackGroundTop) {
 		this.pageTitleBackGroundTop = pageTitleBackGroundTop;
 	}
 
 	public String getPageTitleBackGroundBottom() {
 		return pageTitleBackGroundBottom;
 	}
 
 	public void setPageTitleBackGroundBottom(String pageTitleBackGroundBottom) {
 		this.pageTitleBackGroundBottom = pageTitleBackGroundBottom;
 	}
 
 	public String getEditNavigationActiveBackGroundTop() {
 		return editNavigationActiveBackGroundTop;
 	}
 
 	public void setEditNavigationActiveBackGroundTop(
 			String editNavigationActiveBackGroundTop) {
 		this.editNavigationActiveBackGroundTop = editNavigationActiveBackGroundTop;
 	}
 
 	public String getEditNavigationActiveBackGroundBottom() {
 		return editNavigationActiveBackGroundBottom;
 	}
 
 	public void setEditNavigationActiveBackGroundBottom(
 			String editNavigationActiveBackGroundBottom) {
 		this.editNavigationActiveBackGroundBottom = editNavigationActiveBackGroundBottom;
 	}
 	
 	public String getBottomButtonPanelTop() {
 		return bottomButtonPanelTop;
 	}
 
 	public void setBottomButtonPanelTop(String bottomButtonPanelTop) {
 		this.bottomButtonPanelTop = bottomButtonPanelTop;
 	}
 
 	public String getBottomButtonPanelBottom() {
 		return bottomButtonPanelBottom;
 	}
 
 	public void setBottomButtonPanelBottom(String bottomButtonPanelBottom) {
 		this.bottomButtonPanelBottom = bottomButtonPanelBottom;
 	}
 
 
 	public String getWelcomeUserIcon() {
 		return welcomeUserIcon;
 	}
 
 	public void setWelcomeUserIcon(String welcomeUserIcon) {
 		this.welcomeUserIcon = welcomeUserIcon;
 	}
 
 	public String getPageTitleColor() {
 		return PageTitleColor;
 	}
 
 	public void setPageTitleColor(String pageTitleColor) {
 		PageTitleColor = pageTitleColor;
 	}
 
 	public String getCustomerTitle() {
 		return CustomerTitle;
 	}
 
 	public void setCustomerTitle(String customerTitle) {
 		CustomerTitle = customerTitle;
 	}
 
 	public void setCopyrightText(String copyrightText) {
 		this.copyrightText = copyrightText;
 	}
 
 	public String getCopyrightText() {
 		return copyrightText;
 	}
 }
