 
 import net.sf.sahi.client.Browser;
 
 public class NavigateToSearchPage {
 
 	private Browser browser;
 
 	public NavigateToSearchPage(Browser browser) {
 		this.browser = browser;
 	}
 
 	public void setUp() throws Exception {
		browser.navigateTo("http://twu-staging:8080/sukrupa/app/students/search");
 	}
 
 	public void tearDown() throws Exception {
 		
 
 	}
 
 }
