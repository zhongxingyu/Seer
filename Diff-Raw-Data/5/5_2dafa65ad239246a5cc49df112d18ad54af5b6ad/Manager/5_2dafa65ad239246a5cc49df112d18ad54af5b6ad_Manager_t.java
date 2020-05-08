 package com.upcloo.client;
 
 import com.upcloo.client.http.UpCloo;
 import com.upcloo.client.http.UpClooInterface;
 
 public class Manager {
 	
 	private static Manager instance;
 	
 	private String username;
 	private String password;
 	private String siteKey;
 	
 	private String[] virtualSiteKeys;
 	
 	private UpClooInterface client;
 	
 	private Manager() {}
 	
	public static Manager getInstance()
 	{
 		if (Manager.instance == null) {
 			Manager.instance = new Manager();
 			
 			//Default use UpCloo client
			Manager.instance.setClient(new UpCloo());
 		}
 		
 		return Manager.instance;
 	}
 	
 	public void setCredential(String username, String siteKey, String password, String... vSiteKeys)
 	{
 		setUsername(username);
 		setPassword(password);
 		setSiteKey(siteKey);
 		
 		setVirtualSiteKeys(vSiteKeys);
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getSiteKey() {
 		return siteKey;
 	}
 
 	public void setSiteKey(String siteKey) {
 		this.siteKey = siteKey;
 	}
 
 	public String[] getVirtualSiteKeys() {
 		return virtualSiteKeys;
 	}
 
 	public void setVirtualSiteKeys(String[] virtualSiteKeys) {
 		this.virtualSiteKeys = virtualSiteKeys;
 	}
 
 	public UpClooInterface getClient() {
 		return client;
 	}
 
 	public void setClient(UpClooInterface client) {
 		this.client = client;
 	}
 }
