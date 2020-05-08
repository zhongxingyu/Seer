 package org.sukrupa.twist.example;
 
 
 import net.sf.sahi.client.Browser;
 import static junit.framework.Assert.*;
 
 public class SearchForSukrupaWithSAHI {
 
 	private Browser browser;
 
 	public SearchForSukrupaWithSAHI(Browser browser) {
 		this.browser = browser;
 	}
 
	public void makeSureWeCanFindTheSukrupaWebsiteUsingBing() throws Exception {
 		browser.navigateTo("http://www.bing.com");
 		browser.textbox("q").setValue("sukrupa");
 		browser.submit("go").click();
		browser.link("Sukrupa Home Page").click();
 	
		assertTrue(browser.link("Click here to donate online safely and securely").exists());			
 	}
 
 }
