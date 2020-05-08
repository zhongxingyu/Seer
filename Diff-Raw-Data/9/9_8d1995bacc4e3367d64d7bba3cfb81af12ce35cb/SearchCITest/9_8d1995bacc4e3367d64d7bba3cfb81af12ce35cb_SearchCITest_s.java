 /**
  * 
 * $Id: SearchCITest.java,v 1.3 2005-12-14 20:14:03 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * 
  */
 package web.search;
 
 import gov.nih.nci.camod.webapp.form.ChemicalDrugForm;
 import gov.nih.nci.camod.webapp.form.EnvironmentalFactorForm;
 import gov.nih.nci.camod.webapp.form.GeneDeliveryForm;
 import gov.nih.nci.camod.webapp.form.GrowthFactorForm;
 import gov.nih.nci.camod.webapp.form.HormoneForm;
 import gov.nih.nci.camod.webapp.form.NutritionalFactorForm;
 import gov.nih.nci.camod.webapp.form.RadiationForm;
 import gov.nih.nci.camod.webapp.form.SurgeryForm;
 import gov.nih.nci.camod.webapp.form.ViralTreatmentForm;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import web.util.TestUtil;
 import web.base.BaseModelNeededTest;
 
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebLink;
 import com.meterware.httpunit.WebResponse;
 
 /** This is a simple example of using HttpUnit to read and understand web pages. * */
 public class SearchCITest extends BaseModelNeededTest {
 
 	public SearchCITest(String testName) {
 		super(testName);
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
 		TestSuite suite = new TestSuite(SearchCITest.class);
 		return suite;
 	}
 
 	public void testSearchForChemicalDrug() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Chemical/Drug");
 		assertNotNull("Unable to find link to enter a Chemical/Drug", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Chemical/Drug is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("chemicalDrugForm");
 
 		ChemicalDrugForm theForm = new ChemicalDrugForm();
 		theForm.setNSCNumber("19191919");
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Chemical / Drug to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}
 	
 	public void testSearchForChemicalDrugWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Chemical/Drug");
 		assertNotNull("Unable to find link to enter a Chemical/Drug", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Chemical/Drug is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("chemicalDrugForm");
 
 		ChemicalDrugForm theForm = new ChemicalDrugForm();
 		theForm.setNSCNumber("19191919");
 		
 		TestUtil.setRandomValues(theForm, theWebForm, true, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Chemical / Drug to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}
 	
 	public void testSearchForEnvironmentalFactor() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Environmental Factor");
 		assertNotNull("Unable to find link to enter an Environmental Factor", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Enviromental Factor is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("environmentalFactorForm");
 
 		EnvironmentalFactorForm theForm = new EnvironmentalFactorForm();
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added an Environmental Factor to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}
 /*	
 	public void testSearchForEnvironmentalFactorWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Environmental Factor");
 		assertNotNull("Unable to find link to enter an Environmental Factor", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Enviromental Factor is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("environmentalFactorForm");
 
 		EnvironmentalFactorForm theForm = new EnvironmentalFactorForm();
 		
 		TestUtil.setRandomValues(theForm, theWebForm, true, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added an Environmental Factor to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}
 */
 
/*	Error:  Couldn't find link to specific search page: CARCINOGENIC INTERVENTIONS
 	public void testSearchForGeneDelivery() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Gene Delivery");
 		assertNotNull("Unable to find link to enter a Gene Delivery", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Viral Vector is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("geneDeliveryForm");
 
 		GeneDeliveryForm theForm = new GeneDeliveryForm();
 		theForm.setViralVector("Lentivirus");
 		theForm.setOrgan("Heart");
 		theForm.setOrganTissueName("Heart");		
 		theForm.setOrganTissueCode("C22498");
 		
 		// Add parameters found on submit screen but not displayed on search screen  
 		List theParamsToSkip = new ArrayList();		
 		theParamsToSkip.add("organTissueCode");
 		theParamsToSkip.add("organTissueName");		
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Gene Delivery to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}	
 */	
 	public void testSearchForGrowthFactor() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Growth Factor");
 		assertNotNull("Unable to find link to enter a Growth Factor", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Growth Factor is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("growthFactorForm");
 
 		GrowthFactorForm theForm = new GrowthFactorForm();
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Growth Factor to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}
 	
 	public void testSearchForHormone() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Hormone");
 		assertNotNull("Unable to find link to enter a Hormone", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Hormone is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("hormoneForm");
 
 		HormoneForm theForm = new HormoneForm();
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Hormone to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}
 	
 	public void testSearchForNutritionalFactor() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Nutritional Factor");
 		assertNotNull("Unable to find link to enter a Nutritional Factor", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Nutritional Factor is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("nutritionalFactorForm");
 
 		NutritionalFactorForm theForm = new NutritionalFactorForm();
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Nutritional Factor to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}
 	
 	public void testSearchForRadiation() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Radiation");
 		assertNotNull("Unable to find link to enter a Radiation", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Radiation is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("radiationForm");
 
 		RadiationForm theForm = new RadiationForm();
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Radiation to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}	
 	
 	public void testSearchForSurgeryOther() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Surgery/Other");
 		assertNotNull("Unable to find link to enter a Surgery/Other", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Surgery is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("surgeryForm");
 
 		SurgeryForm theForm = new SurgeryForm();
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Surgery / Other to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}	
 
 	public void testSearchForViralTreatment() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Viral Treatment");
 		assertNotNull("Unable to find link to enter a Viral Treatment", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("(if Virus is not listed, then please");
 		WebForm theWebForm = theCurrentPage.getFormWithName("viralTreatmentForm");
 
 		ViralTreatmentForm theForm = new ViralTreatmentForm();
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, new ArrayList());
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Viral Treatment to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CARCINOGENIC INTERVENTIONS");
 		
 		verifyValuesOnPage(theWebForm);
 	}		
 	
 }
