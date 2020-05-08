 // $Id: ApiAccountProfile.java 0 1970-01-01 00:00:00Z mkwayisi $
 package com.smsgh;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.math.BigDecimal;
 import com.smsgh.json.JsonObject;
 import com.smsgh.json.JsonValue;
 
 /**
  * Represents an API account profile.
  *
  * @author Michael Kwayisi
  */
 public class ApiAccountProfile {
 	private String     accountId;
 	private String     accountManager;
 	private long       accountNumber;
 	private String     accountStatus;
 	private BigDecimal balance;
 	private String     company;
 	private BigDecimal credit;
 	private String     emailAddress;
 	private Date       lastAccessed;
 	private String     mobileNumber;
 	private int        numberOfServices;
 	private String     primaryContact;
 	private BigDecimal unpostedBalance;
 	
 	/**
 	 * Initializes the data fields of this class from JSON.
 	 *
 	 * @param  json  a guaranteed instance of com.smsgh.json.JsonObject.
 	 */
 	public ApiAccountProfile(JsonObject json)
 	{
 		for (String name : json.names()) {
 			JsonValue val = json.get(name);
 			switch (name.toLowerCase()) {
 				case "accountid":
 					this.accountId = val.asString();
 					break;
 				case "accountmanager":
 					this.accountManager = val.asString();
 					break;
 				case "accountnumber":
 					this.accountNumber = val.asLong();
 					break;
 				case "accountstatus":
 					this.accountStatus = val.asString();
 					break;
 				case "balance":
 					this.balance = new BigDecimal(val.toString());
 					break;
 				case "company":
 					this.company = val.asString();
 					break;
 				case "credit":
 					this.credit = new BigDecimal(val.toString());
 					break;
 				case "emailaddress":
 					this.emailAddress = val.asString();
 					break;
 				case "lastaccessed":
 					this.lastAccessed = val.asDate();
 					break;
 				case "mobilenumber":
 					this.mobileNumber = val.asString();
 					break;
 				case "numberofservices":
 					this.numberOfServices = val.asInt();
 					break;
 				case "primarycontact":
 					this.primaryContact = val.asString();
 					break;
 				case "unpostedbalance":
 					this.unpostedBalance = new BigDecimal(val.toString());
 					break;
 			}
 		}
 	}
 	
 	/**
 	 * Gets the account ID of this API account profile.
 	 *
 	 * @return the account ID or zero if not set.
 	 */
 	public String getAccountId() {
 		return this.accountId;
 	}
 	
 	/**
 	 * Gets the account manager of this API account profile.
 	 *
 	 * @return the account manager or null if not set.
 	 */
 	public String getAccountManager() {
 		return this.accountManager;
 	}
 	
 	/**
 	 * Gets the account number of this API account profile.
 	 *
 	 * @return the account number or zero if not set.
 	 */
 	public long getAccountNumber() {
 		return this.accountNumber;
 	}
 	
 	/**
 	 * Gets the account status of this API account profile.
 	 *
 	 * @return the account status or null if not set.
 	 */
 	public String getAccountStatus() {
 		return this.accountStatus;
 	}
 	
 	/**
 	 * Gets the balance of this API account profile.
 	 *
	 * @return the balance or zero if not set.
 	 */
 	public BigDecimal getBalance() {
 		return this.balance;
 	}
 	
 	/**
 	 * Gets the company of this API account profile.
 	 *
 	 * @return the company or null if not set.
 	 */
 	public String getCompany() {
 		return this.company;
 	}
 	
 	/**
 	 * Gets the credit of this API account profile.
 	 *
	 * @return the credit or zero if not set.
 	 */
 	public BigDecimal getCredit() {
 		return this.credit;
 	}
 	
 	/**
 	 * Gets the email address of this API account profile.
 	 *
 	 * @return the email address or null if not set.
 	 */
 	public String getEmailAddress() {
 		return this.emailAddress;
 	}
 	
 	/**
 	 * Gets the last accessed date of this API account profile.
 	 *
 	 * @return the last accessed date or null if not set.
 	 */
 	public Date getLastAccessed() {
 		return this.lastAccessed;
 	}
 	
 	/**
 	 * Gets the mobile number of this API account profile.
 	 *
 	 * @return the mobile number or null if not set.
 	 */
 	public String getMobileNumber() {
 		return this.mobileNumber;
 	}
 	
 	/**
 	 * Gets the number of services on this API account profile.
 	 *
 	 * @return the number of services or zero if not set.
 	 */
 	public int getNumberOfServices() {
 		return this.numberOfServices;
 	}
 	
 	/**
 	 * Gets the primary contact of this API account profile.
 	 *
 	 * @return the primary contact or null if not set.
 	 */
 	public String getPrimaryContact() {
 		return this.primaryContact;
 	}
 	
 	/**
 	 * Gets the unposted balance of this API account profile.
 	 *
	 * @return the unposted balance or zero if not set.
 	 */
 	public BigDecimal getUnpostedBalance() {
 		return this.unpostedBalance;
 	}
 }
