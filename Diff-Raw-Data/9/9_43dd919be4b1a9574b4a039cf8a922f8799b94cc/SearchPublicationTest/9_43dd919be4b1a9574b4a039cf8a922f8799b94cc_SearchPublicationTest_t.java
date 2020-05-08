 /**
  * @author pandyas
  * 
 * $Id: SearchPublicationTest.java,v 1.2 2005-12-29 18:17:35 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
 * Revision 1.1  2005/12/13 19:17:00  pandyas
 * JUnit test case for Search Publication
 *
  * 
  */
 
 package web.search;
 
 import gov.nih.nci.camod.webapp.form.PublicationForm;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import web.base.BaseModelNeededTest;
 import web.util.TestUtil;
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebLink;
 import com.meterware.httpunit.WebResponse;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class SearchPublicationTest extends BaseModelNeededTest {
 	
 	public SearchPublicationTest(String arg0) {
 		super(arg0);
 	}
 
 	protected void setUp() throws Exception {
 
         ResourceBundle theBundle = ResourceBundle.getBundle("test");
 
         String theUsername = theBundle.getString("username");
         String thePassword = theBundle.getString("password");
         
         loginToApplication(theUsername, thePassword);
         createModel();		
 	}
 
 	protected void tearDown() throws Exception {
         deleteModel();
         logoutOfApplication();
 	}
 	
     public static Test suite() {
         TestSuite suite = new TestSuite(SearchPublicationTest.class);
         return suite;
     }
     
 	public void testSearchForPublication() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding a Publication
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Publications");
 		assertNotNull("Unable to find link to enter a Publication", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("For publications with a PubMed record");
 		WebForm theWebForm = theCurrentPage.getFormWithName("publicationForm");
		// set explicitly so validation works
		theWebForm.setParameter("firstTimeReported", "yes");		
 
 		PublicationForm theForm = new PublicationForm();
 		theForm.setFirstTimeReported("yes");
 		theForm.setPmid("16323327");
 		theForm.setYear("1999");
 		theForm.setStartPage("1111");
 		theForm.setEndPage("9999");
         
 		List theParamsToIgnore = new ArrayList();
 		theParamsToIgnore.add("ACellID");
 		theParamsToIgnore.add("ATherapyID");
 		theParamsToIgnore.add("APubID");
 		
 		/* Add parameters found on submit screen but not displayed on search screen  */
 		List theParamsToSkip = new ArrayList();
 		theParamsToSkip.add("firstTimeReported");		
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		
 		assertCurrentPageContains("You have successfully added a Publication to this model! ");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"PUBLICATIONS");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);	
 
 	}  
 
 }
