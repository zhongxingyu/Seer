 
 // JUnit Assert framework can be used for verification
 
 import net.sf.sahi.client.Browser;
 import static junit.framework.Assert.*;
 
 public class SearchWorkflow {
 
 	private Browser browser;
 
 	public SearchWorkflow(Browser browser) {
 		this.browser = browser;
 	}
 
 	public void givenISelectTheTalent(String string1) throws Exception {
 		selectTalent(string1);
 	}
 
 	public void andISelectTheTalent(String string1) throws Exception {
 		selectTalent(string1);
 	}
 
 	private void selectTalent(String talent) {
 		if (!talent.isEmpty())	{
			browser.select("listLeft").choose(talent);
			browser.click(browser.button("Add >"));
 		}
 	}
 	
 
 
 	public void whenIHitSearchButton() throws Exception {
 		browser.submit("Search").click();
 	
 	}
 
 	public void thenShowsInSearchResults(String string1) throws Exception {
 		assertEquals(string1, browser.cell(string1).text());
 	
 	}
 
 	public void andISelectMinimumAgeAs(String string1) throws Exception {
 		if (!string1.isEmpty()) 
 		{
 		browser.select("ageFrom").choose(string1);
 		}
 	}
 
 	public void andISelectMaximumAgeAs(String string1) throws Exception {
 		if (!string1.isEmpty()) 
 		{
 		browser.select("ageTo").choose(string1);
 		}
 	
 	}
 
 	public void andISelectReligionAs(String string1) throws Exception {
 		if (!string1.isEmpty()) 
 		{
 		browser.select("religion").choose(string1);
 		}
 	}
 
 	public void andISelectClassAs(String string1) throws Exception {
 		if (!string1.isEmpty()) 
 		{
 		browser.select("studentClass").choose(string1);
 		}
 	}
 
 	public void andISelectGenderAs(String string1) throws Exception {
 		if (!string1.isEmpty()) 
 		{
 		browser.select("gender").choose(string1);
 		}
 	
 	}
 
 	public void andISelectCasteAs(String string1) throws Exception {
 		if (!string1.isEmpty()) 
 		{
 		browser.select("caste").choose(string1);
 		}
 	}
 
 	public void andISelectCommunityLocationAs(String string1) throws Exception {
 		if (!string1.isEmpty()) 
 		{
 			browser.select("communityLocation").choose(string1);
 		}
 		
 	
 	}
 
 	public void andShowsInSearchResults(String string1) throws Exception {
 		assertEquals(string1, browser.cell(string1).text());					
 	}
 
 	public void thenDoesNotShowInSearchResults(String string1) throws Exception {
 		assertFalse(browser.cell(string1).exists());
 	}
 
 	public void andDoesNotShowInSearchResults(String string1) throws Exception {
 		thenDoesNotShowInSearchResults(string1);
 	}
 
 
 }
