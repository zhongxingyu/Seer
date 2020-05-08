 package org.businessmanager.web.bean;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.businessmanager.domain.settings.ApplicationSetting;
 
 public class CompanySettingsBean {
 
 	private static String SETTING_COMPANY_NAME = "COMPANY_NAME";
 	private static String SETTING_COMPANY_OWNERS = "COMPANY_OWNERS";
 	private static String SETTING_COMPANY_LEGAL_FORM = "COMPANY_LEGAL_FORM";
 	private static String SETTING_COMPANY_STREET = "COMPANY_STREET";
	private static String SETTING_COMPANY_HOUSENUMBER = "COMPANY_HOUSENUMBER";
 	private static String SETTING_COMPANY_ZIP = "COMPANY_ZIP";
 	private static String SETTING_COMPANY_CITY = "COMPANY_CITY";
 	private static String SETTING_COMPANY_PHONE_NO = "COMPANY_PHONE_NO";
 	private static String SETTING_COMPANY_FAX_NO = "COMPANY_FAX_NO";
 	private static String SETTING_COMPANY_MOBILE_NO = "COMPANY_MOBILE_NO";
 	private static String SETTING_COMPANY_EMAIL = "COMPANY_EMAIL";
 	private static String SETTING_COMPANY_WEBSITE = "COMPANY_WEBSITE";
 	private static String SETTING_COMPANY_TAXID = "COMPANY_TAXID";
 	private static String SETTING_COMPANY_TAXAUTHORITY = "COMPANY_TAXAUTHORITY";
 	
 	private Map<String, String> settingsMap = new HashMap<String, String>();	
 	
 	public String getCompanyName() {
 		return getSetting(SETTING_COMPANY_NAME);
 	}
 	public void setCompanyName(String companyName) {
 		settingsMap.put(SETTING_COMPANY_NAME, companyName);
 	}
 	public String getOwners() {
 		return getSetting(SETTING_COMPANY_OWNERS);
 	}
 	public void setOwners(String owners) {
 		settingsMap.put(SETTING_COMPANY_OWNERS, owners);
 	}
 	public String getLegalForm() {
 		return getSetting(SETTING_COMPANY_LEGAL_FORM);
 	}
 	public void setLegalForm(String legalForm) {
 		settingsMap.put(SETTING_COMPANY_LEGAL_FORM, legalForm);
 	}
 	public String getStreet() {
 		return getSetting(SETTING_COMPANY_STREET);
 	}
 	public void setStreet(String street) {
 		settingsMap.put(SETTING_COMPANY_STREET, street);
 	}
	public String getHouseNumber() {
		return getSetting(SETTING_COMPANY_HOUSENUMBER);
	}
	public void setHouseNumber(String houseNumber) {
		settingsMap.put(SETTING_COMPANY_HOUSENUMBER, houseNumber);
	}
 	public String getZip() {
 		return getSetting(SETTING_COMPANY_ZIP);
 	}
 	public void setZip(String zip) {
 		settingsMap.put(SETTING_COMPANY_ZIP, zip);
 	}
 	public String getCity() {
 		return getSetting(SETTING_COMPANY_CITY);
 	}
 	public void setCity(String city) {
 		settingsMap.put(SETTING_COMPANY_CITY, city);
 	}
 	public String getPhoneNo() {
 		return getSetting(SETTING_COMPANY_PHONE_NO);
 	}
 	public void setPhoneNo(String phoneNo) {
 		settingsMap.put(SETTING_COMPANY_PHONE_NO, phoneNo);
 	}
 	public String getFaxNo() {
 		return getSetting(SETTING_COMPANY_FAX_NO);
 	}
 	public void setFaxNo(String faxNo) {
 		settingsMap.put(SETTING_COMPANY_FAX_NO, faxNo);
 	}
 	public String getMobileNo() {
 		return getSetting(SETTING_COMPANY_MOBILE_NO);
 	}
 	public void setMobileNo(String mobileNo) {
 		settingsMap.put(SETTING_COMPANY_MOBILE_NO, mobileNo);
 	}
 	public String getEmail() {
 		return getSetting(SETTING_COMPANY_EMAIL);
 	}
 	public void setEmail(String email) {
 		settingsMap.put(SETTING_COMPANY_EMAIL, email);
 	}
 	public String getWebsite() {
 		return getSetting(SETTING_COMPANY_WEBSITE);
 	}
 	public void setWebsite(String website) {
 		settingsMap.put(SETTING_COMPANY_WEBSITE, website);
 	}
 	public String getTaxId() {
 		return getSetting(SETTING_COMPANY_TAXID);
 	}
 	public void setTaxId(String taxId) {
 		settingsMap.put(SETTING_COMPANY_TAXID, taxId);
 	}
 	public String getTaxAuthority() {
 		return getSetting(SETTING_COMPANY_TAXAUTHORITY);
 	}
 	public void setTaxAuthority(String taxAuthority) {
 		settingsMap.put(SETTING_COMPANY_TAXAUTHORITY, taxAuthority);
 	}
 	
 	public Map<String, String> getSettingsMap() {
 		return settingsMap;
 	}
 	
 	public void setSettingsMap(Map<String, String> settingsMap) {
 		this.settingsMap = settingsMap;
 	}
 
 	public void setSettings(List<ApplicationSetting> applicationSettings) {
 		if(applicationSettings != null) {
 			this.settingsMap = new HashMap<String, String>();
 			for(ApplicationSetting setting : applicationSettings) {
 				settingsMap.put(setting.getParamKey(), setting.getParamValue());
 			}
 		}
 	}
 	
 	private String getSetting(String key) {
 		String result = this.settingsMap.get(key);
 		
 		if(result == null) {
 			return "";
 		}
 		return result;
 	}
 }
