 package org.openmrs.module.htmlformentryui.page.controller.htmlform;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Patient;
 import org.openmrs.Visit;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 public class BaseHtmlFormPageControllerTest {
 
     private UiUtils uiUtils;
 
     private Patient patient = new Patient(1);
 
     private Visit visit = new Visit(1);
 
     private SimpleObject returnParams;
 
     private BaseHtmlFormPageController controller = new NonAbstractBaseHtmlPageController();
 
 
     @Before
     public void setup() {
         uiUtils = mock(UiUtils.class);
         when(uiUtils.contextPath()).thenReturn("openmrs/");
         returnParams = SimpleObject.create("patientId", patient.getId(), "visitId", visit.getId());
 
     }
 
     @Test
     public void test_shouldReturnUrlIfNoPageOrProviderSpecified() {
        assertThat(controller.determineReturnUrl("someReturnUrl.html", null, null, patient, visit, uiUtils), is("/openmrs/someReturnUrl.html"));
     }
 
     @Test
     public void test_providerAndPageShouldTakePrecedentOverReturnUrl() {
         controller.determineReturnUrl("someReturnUrl.html", "myProvider", "myPage", patient, visit, uiUtils);
         verify(uiUtils).pageLink(eq("myProvider"), eq("myPage"), eq(returnParams));
     }
 
     @Test
     public void test_providerAndPageShouldNotReturnVisitParm() {
         SimpleObject returnParamsWithoutVisit = SimpleObject.create("patientId", patient.getId());
         controller.determineReturnUrl("someReturnUrl.html", "myProvider", "myPage", patient, null, uiUtils);
         verify(uiUtils).pageLink(eq("myProvider"), eq("myPage"), eq(returnParamsWithoutVisit));
     }
 
     @Test
     public void test_shouldReturnPatientDashboard() {
         controller.determineReturnUrl(null, null, null, patient, visit, uiUtils);
         verify(uiUtils).pageLink(eq("coreapps"), eq("patientdashboard/patientDashboard"), eq(returnParams));
     }
 
     @Test
     public void test_shouldReturnIndexIfNoPatient() {
         assertThat(controller.determineReturnUrl(null, null, null, null, null, uiUtils), is("/openmrs/index.html"));
     }
 
     private class NonAbstractBaseHtmlPageController extends BaseHtmlFormPageController {
 
     }
 
 }
