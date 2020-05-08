 /**
  * @author pandyas
  * 
 * $Id: SearchPopulateHistopathologyTest.java,v 1.4 2006-10-23 16:50:52 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.3  2006/05/08 14:23:23  georgeda
  * Reformat and clean up warnings
  *
  * Revision 1.2  2006/04/27 15:08:43  pandyas
  * Modified while testing caMod 2.1
  *
  * Revision 1.1  2006/01/09 16:59:53  pandyas
  * Modified to include methods to test if the populate method returns complete and correct data - initial modifications
  *
  * Revision 1.3  2005/12/29 22:20:48  pandyas
  * removed disabled="true" on methodOfObservation field
  *
  * Revision 1.2  2005/12/16 18:13:21  pandyas
  * Added an Assoc Met and Clinical Marker to script - there is one more convenience TODO that would be nice to have
  *
  * Revision 1.1  2005/12/14 17:17:42  pandyas
  * JUnit test case for Search Histopathology - depends on other changes in main tree that will NOT be uploaded until we go to production
  *
  * 
  */
 
 package web.search;
 
 import gov.nih.nci.camod.webapp.form.AssociatedMetastasisForm;
 import gov.nih.nci.camod.webapp.form.HistopathologyForm;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import web.base.BaseModelNeededTest;
 import web.util.TestUtil;
 
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebLink;
 import com.meterware.httpunit.WebResponse;
 
 public class SearchPopulateHistopathologyTest extends BaseModelNeededTest
 {
 
     public SearchPopulateHistopathologyTest(String arg0)
     {
         super(arg0);
     }
 
     protected void setUp() throws Exception
     {
 
         ResourceBundle theBundle = ResourceBundle.getBundle("test");
 
         String theUsername = theBundle.getString("username");
         String thePassword = theBundle.getString("password");
 
         loginToApplication(theUsername, thePassword);
         createModel();
     }
 
     protected void tearDown() throws Exception
     {
         deleteModel();
         logoutOfApplication();
     }
 
     public static Test suite()
     {
         TestSuite suite = new TestSuite(SearchPopulateHistopathologyTest.class);
         return suite;
     }
 
     public void testPopulateHistopathology() throws Exception
     {
 
         navigateToModelForEditing(myModelName);
 
         // Adding a Histopathology
         WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "Enter Histopathology");
         assertNotNull("Unable to find link to enter a Histopathology", theLink);
         WebResponse theCurrentPage = theLink.click();
         assertCurrentPageContains("Tumor Incidence");
         WebForm theWebForm = theCurrentPage.getFormWithName("histopathologyForm");
 
         HistopathologyForm theForm = new HistopathologyForm();
         theForm.setOrgan("Heart");
         theForm.setOrganTissueName("Heart");
         theForm.setOrganTissueCode("C22498");
         theForm.setTumorClassification("Lymphoid_Hyperplasia_of_the_Mouse_Intestinal_Tract");
         theForm.setDiagnosisName("Lymphoid Hyperplasia of the Intestinal Tract");
         theForm.setDiagnosisCode("C22100");
         theForm.setVolumeOfTumor("2");
         theForm.setWeightOfTumor("30");
         theForm.setTumorIncidenceRate("20");
 
         /* Add parameters found on submit screen but not displayed on search screen  */
         List<String> theParamsToSkip = new ArrayList<String>();
         theParamsToSkip.add("organTissueCode");
         theParamsToSkip.add("organTissueName");
         theParamsToSkip.add("tumorClassification");
         theParamsToSkip.add("diagnosisCode");
 
         TestUtil.setRandomValues(theForm, theWebForm, false);
         TestUtil.setValuesOnForm(theForm, theWebForm);
 
         theCurrentPage = theWebForm.submit();
         assertCurrentPageContains("You have successfully added a Histopathology to this model!");
 
         // Verify that populate method returns complete and correct data
         navigateToModelForEditing(myModelName);
 
         theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "Heart");
         assertNotNull("Unable to find link to populate the Histopathology", theLink);
         theCurrentPage = theLink.click();
         assertCurrentPageContains("Tumor Incidence");
         theWebForm = theCurrentPage.getFormWithName("histopathologyForm");
 
         //Add parameters found behind but not populate screen
         theParamsToSkip = new ArrayList<String>();
         theParamsToSkip.add("aHistopathologyID");
         theParamsToSkip.add("submitAction");
         theParamsToSkip.add("tumorClassification");
 
         verifyValuesOnPopulatePage(theWebForm, theParamsToSkip);
 
         /******* Adding Assoc Metas to Histopathology ***********/
         navigateToModelForEditing(myModelName);
 
         // Adding an Assoc Metastasis
         theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "Enter Assoc Metastasis");
         assertNotNull("Unable to find link to add an Assoc Metastasis to Histopathology", theLink);
         theCurrentPage = theLink.click();
         assertCurrentPageContains("Age of Metastasis Onset:");
         theWebForm = theCurrentPage.getFormWithName("associatedMetastasisForm");
 
         AssociatedMetastasisForm theAMForm = new AssociatedMetastasisForm();
         theAMForm.setOrgan("Mammary Gland");
         theAMForm.setOrganTissueName("Mammary Gland");
         theAMForm.setOrganTissueCode("C22549");
         theAMForm.setTumorClassification("Adenoma");
         theAMForm.setDiagnosisName("Adenoma");
         theAMForm.setDiagnosisCode("C21762");
         theAMForm.setVolumeOfTumor("2");
         theAMForm.setWeightOfTumor("30");
         theAMForm.setComments("Test Comments");
         theAMForm.setTumorIncidenceRate("20");
 
         //TODO: add check for histopathologyID so we can remove from skip list
         // Add parameters found on submit screen but not displayed on search screen  
         theParamsToSkip = new ArrayList<String>();
         theParamsToSkip.add("organTissueCode");
         theParamsToSkip.add("organTissueName");
         theParamsToSkip.add("tumorClassification");
         theParamsToSkip.add("diagnosisCode");
         theParamsToSkip.add("diagnosisName");
         theParamsToSkip.add("methodOfObservation");
         theParamsToSkip.add("aHistopathologyID");
 
         TestUtil.setRandomValues(theAMForm, theWebForm, false);
         TestUtil.setValuesOnForm(theAMForm, theWebForm);
 
         theCurrentPage = theWebForm.submit();
         assertCurrentPageContains("You have successfully added an Associated Metastasis to this model!");
 
         // Verify that populate method returns complete and correct data
         navigateToModelForEditing(myModelName);
 
         theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "Mammary Gland");
         assertNotNull("Unable to find link to populate the Assoc Metastasis", theLink);
         theCurrentPage = theLink.click();
        assertCurrentPageContains("Associated Metastasis:");
         theWebForm = theCurrentPage.getFormWithName("associatedMetastasisForm");
 
         //Add parameters found behind but not populate screen
         theParamsToSkip = new ArrayList<String>();
         theParamsToSkip.add("aHistopathologyID");
         theParamsToSkip.add("submitAction");
         theParamsToSkip.add("methodOfObservation");
         theParamsToSkip.add("aAssociatedMetastasisID");
 
         verifyValuesOnPopulatePage(theWebForm, theParamsToSkip);
 
         /******* Adding Clinical Marker to Histopathology ***********		
          navigateToModelForEditing(myModelName);
 
          // Adding an Clinical Marker		
          theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
          "Enter Clinical Marker");
          assertNotNull("Unable to find link to add a Clinical Marker to Histopathology", theLink);
          theCurrentPage = theLink.click();
          assertCurrentPageContains("Select Clinical");
          theWebForm = theCurrentPage.getFormWithName("clinicalMarkerForm");
 
          ClinicalMarkerForm theCMForm = new ClinicalMarkerForm();
          theCMForm.setName("Estrogen Receptor (ER)");
 
          //TODO: add check for histopathologyID so we can remove from skip list
          // Add parameters found on submit screen but not displayed on search screen  
          theParamsToSkip = new ArrayList<String>();		
          theParamsToSkip.add("aHistopathologyID");
 
          TestUtil.setRandomValues(theCMForm, theWebForm, false);
          TestUtil.setValuesOnForm(theCMForm, theWebForm);	
          
          theCurrentPage = theWebForm.submit();
          assertCurrentPageContains("You have successfully added a Clinical Marker to this model!");
 
          // Verify that populate method returns complete and correct data
          navigateToModelForEditing(myModelName);
 
          theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
          "Estrogen Receptor");
          assertNotNull("Unable to find link to populate the Clinical Marker", theLink);		
          theCurrentPage = theLink.click();
          assertCurrentPageContains("Select Clinical");
          theWebForm = theCurrentPage.getFormWithName("clinicalMarkerForm");
          
          //Add parameters found behind but not populate screen
          theParamsToSkip = new ArrayList<String>();
          theParamsToSkip.add("aHistopathologyID");
          theParamsToSkip.add("submitAction");
          theParamsToSkip.add("aClinicalMarkerID");
          //theParamsToSkip.add("submitAction");		
          
          verifyValuesOnPopulatePage(theWebForm, theParamsToSkip);		
          */
     }
 
     public void testSearchForHistopathology() throws Exception
     {
 
         navigateToModelForEditing(myModelName);
 
         // Adding a Histopathology
         WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "Enter Histopathology");
         assertNotNull("Unable to find link to enter a Histopathology", theLink);
         WebResponse theCurrentPage = theLink.click();
         assertCurrentPageContains("Tumor Incidence");
         WebForm theWebForm = theCurrentPage.getFormWithName("histopathologyForm");
 
         HistopathologyForm theForm = new HistopathologyForm();
         theForm.setOrgan("Heart");
         theForm.setOrganTissueName("Heart");
         theForm.setOrganTissueCode("C22498");
         theForm.setTumorClassification("Lymphoid_Hyperplasia_of_the_Mouse_Intestinal_Tract");
         theForm.setDiagnosisName("Lymphoid_Hyperplasia_of_the_Mouse_Intestinal_Tract");
         theForm.setDiagnosisCode("C22100");
         theForm.setVolumeOfTumor("2");
         theForm.setWeightOfTumor("30");
         theForm.setTumorIncidenceRate("20");
 
         /* Add parameters found on submit screen but not displayed on search screen  */
         List<String> theParamsToSkip = new ArrayList<String>();
         theParamsToSkip.add("organTissueCode");
         theParamsToSkip.add("organTissueName");
         theParamsToSkip.add("tumorClassification");
         theParamsToSkip.add("diagnosisCode");
         theParamsToSkip.add("diagnosisName");
 
         TestUtil.setRandomValues(theForm, theWebForm, false);
         TestUtil.setValuesOnForm(theForm, theWebForm);
 
         theCurrentPage = theWebForm.submit();
         assertCurrentPageContains("You have successfully added a Histopathology to this model!");
 
         TestUtil.moveModelToEditedApproved(myModelName);
 
         navigateToSpecificSearchPage(myModelName, "HISTOPATHOLOGY");
 
         verifyValuesOnPage(theWebForm, theParamsToSkip);
 
         /******* Adding Assoc Metas to Histopathology ***********/
         navigateToModelForEditing(myModelName);
 
         // Adding an Assoc Metastasis
         theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "Enter Assoc Metastasis");
         assertNotNull("Unable to find link to add an Assoc Metastasis to Histopathology", theLink);
         theCurrentPage = theLink.click();
         assertCurrentPageContains("Age of Metastasis");
         theWebForm = theCurrentPage.getFormWithName("associatedMetastasisForm");
 
         AssociatedMetastasisForm theAMForm = new AssociatedMetastasisForm();
         theAMForm.setOrgan("Heart");
         theAMForm.setOrganTissueName("Heart");
         theAMForm.setOrganTissueCode("C22498");
         theAMForm.setTumorClassification("Lymphoid_Hyperplasia_of_the_Mouse_Intestinal_Tract");
         theAMForm.setDiagnosisName("Lymphoid_Hyperplasia_of_the_Mouse_Intestinal_Tract");
         theAMForm.setDiagnosisCode("C22100");
         theAMForm.setVolumeOfTumor("2");
         theAMForm.setWeightOfTumor("30");
         theAMForm.setComments("Test Comments");
         theAMForm.setTumorIncidenceRate("20");
 
         /* Add parameters found on submit screen but not displayed on search screen  */
         theParamsToSkip = new ArrayList<String>();
         theParamsToSkip.add("organTissueCode");
         theParamsToSkip.add("organTissueName");
         theParamsToSkip.add("tumorClassification");
         theParamsToSkip.add("diagnosisCode");
         theParamsToSkip.add("diagnosisName");
         theParamsToSkip.add("methodOfObservation");
         theParamsToSkip.add("aHistopathologyID");
 
         TestUtil.setRandomValues(theAMForm, theWebForm, false);
         TestUtil.setValuesOnForm(theAMForm, theWebForm);
 
         theCurrentPage = theWebForm.submit();
         assertCurrentPageContains("You have successfully added an Associated Metastasis to this model!");
 
         TestUtil.moveModelToEditedApproved(myModelName);
 
         navigateToSpecificSearchPage(myModelName, "HISTOPATHOLOGY");
 
         verifyValuesOnPage(theWebForm, theParamsToSkip);
 
         /******* Adding Clinical Marker to Histopathology **********		
          navigateToModelForEditing(myModelName);
 
          // Adding an Clinical Marker		
          theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
          "Enter Clinical Marker");
          assertNotNull("Unable to find link to add a Clinical Marker to Histopathology", theLink);
          theCurrentPage = theLink.click();
          assertCurrentPageContains("Select Clinical");
          theWebForm = theCurrentPage.getFormWithName("clinicalMarkerForm");
 
          ClinicalMarkerForm theCMForm = new ClinicalMarkerForm();
 
          //TODO: add check for histopathologyID so we can remove from skip list
          // Add parameters found on submit screen but not displayed on search screen  
          theParamsToSkip = new ArrayList<String>();		
          theParamsToSkip.add("aHistopathologyID");
 
          TestUtil.setRandomValues(theCMForm, theWebForm, false);
          TestUtil.setValuesOnForm(theCMForm, theWebForm);	
          
          theCurrentPage = theWebForm.submit();
          assertCurrentPageContains("You have successfully added a Clinical Marker to this model!");
 
          TestUtil.moveModelToEditedApproved(myModelName);
 
          navigateToSpecificSearchPage(myModelName,"HISTOPATHOLOGY");
          
          verifyValuesOnPage(theWebForm, theParamsToSkip);		
          */
     }
 
 }
