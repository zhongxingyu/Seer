 package org.sukrupa.student;
 
 import net.sf.sahi.client.Browser;
 
 public class OpenAdminPage {
 
 	private Browser browser;
 
 	public OpenAdminPage(Browser browser) {
 		this.browser = browser;
 	}
 
 	public void setUp() throws Exception {
		browser.navigateTo("http://twu-staging:8080/sukrupa/app/students");
 	}
 
 	public void tearDown() throws Exception {
 		
 
 	}
 
 }
