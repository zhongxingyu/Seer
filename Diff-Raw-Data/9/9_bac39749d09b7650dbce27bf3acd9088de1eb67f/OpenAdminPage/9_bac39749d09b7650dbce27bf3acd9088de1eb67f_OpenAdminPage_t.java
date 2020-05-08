 package org.sukrupa.student;
 
 import net.sf.sahi.client.Browser;
 
 public class OpenAdminPage {
 
 	private Browser browser;
 
 	public OpenAdminPage(Browser browser) {
 		this.browser = browser;
 	}
 
 	public void setUp() throws Exception {
		browser.navigateTo("http://localhost:8080/students");
 	}
 
 	public void tearDown() throws Exception {
 		
 
 	}
 
 }
