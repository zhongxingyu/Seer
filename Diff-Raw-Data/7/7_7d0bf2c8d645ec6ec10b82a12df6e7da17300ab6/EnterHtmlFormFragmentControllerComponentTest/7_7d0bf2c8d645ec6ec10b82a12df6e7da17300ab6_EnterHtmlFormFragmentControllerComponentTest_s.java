 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.htmlformentryui.fragment.controller.htmlform;
 
 import org.hamcrest.Matchers;
 import org.hamcrest.core.IsCollectionContaining;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Encounter;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Provider;
 import org.openmrs.Visit;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.FormService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.PersonService;
 import org.openmrs.api.ProviderService;
 import org.openmrs.api.VisitService;
 import org.openmrs.module.appui.TestUiUtils;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.module.emrapi.adt.AdtService;
 import org.openmrs.module.htmlformentry.FormEntrySession;
 import org.openmrs.module.htmlformentry.HtmlForm;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.fragment.FragmentConfiguration;
 import org.openmrs.ui.framework.fragment.FragmentModel;
 import org.openmrs.ui.framework.resource.ResourceFactory;
 import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  *
  */
 public class EnterHtmlFormFragmentControllerComponentTest extends BaseModuleWebContextSensitiveTest {
 
     @Qualifier("personService")
     @Autowired
     PersonService personService;
 
     @Qualifier("patientService")
     @Autowired
     PatientService patientService;
 
     @Qualifier("locationService")
     @Autowired
     LocationService locationService;
 
     @Qualifier("providerService")
     @Autowired
     ProviderService providerService;
 
     @Qualifier("htmlFormEntryService")
     @Autowired
     HtmlFormEntryService htmlFormEntryService;
 
     @Qualifier("formService")
     @Autowired
     FormService formService;
 
     @Qualifier("encounterService")
     @Autowired
     EncounterService encounterService;
 
     @Qualifier("adtService")
     @Autowired
     AdtService adtService;
 
     @Qualifier("visitService")
     @Autowired
     VisitService visitService;
 
     ResourceFactory resourceFactory;
 
     UiUtils ui;
 
     UiSessionContext sessionContext;
 
     public static final String FORM_DEFINITION = "<htmlform formUuid=\"form-uuid\" formName=\"Form Name\" formVersion=\"1.0\">Weight:<obs id=\"weight\" conceptId=\"5089\"/> <encounterDate showTime=\"true\"/> <encounterLocation/> <encounterProvider/></htmlform>";
     private EnterHtmlFormFragmentController controller;
 
 
     @Before
     public void before() throws Exception {
 
         executeDataSet("enterHtmlFormFragmentControllerTestDataset.xml");
 
         resourceFactory = mock(ResourceFactory.class);
         when(resourceFactory.getResourceAsString("emr", "htmlforms/vitals.xml")).thenReturn(FORM_DEFINITION);
 
         sessionContext = mock(UiSessionContext.class);
         when(sessionContext.getSessionLocation()).thenReturn(locationService.getLocation(2));
 
 
         ui = new TestUiUtils();
 
         controller = new EnterHtmlFormFragmentController();
     }
 
     @Test
     public void testDefiningAnHtmlFormInUiResource() throws Exception {
         FragmentModel model = new FragmentModel();
         Patient patient = new Patient();
         String resourcePath = "emr:htmlforms/vitals.xml";
 
         FragmentConfiguration config = new FragmentConfiguration();
         config.put("patient", patient);
         config.put("definitionResource", resourcePath);
 
         controller.controller(config, sessionContext, htmlFormEntryService, formService, resourceFactory, patient, null, null, null, null, resourcePath, null, null, null, null, true, model, null);
 
         FormEntrySession command = (FormEntrySession) model.getAttribute("command");
         String html = command.getHtmlToDisplay();
         assertThat(html, Matchers.containsString("name=\"w2\""));
         assertThat(html, Matchers.containsString("onBlur=\"checkNumber("));
     }
 
     @Test
     public void testSubmittingHtmlFormDefinedInUiResource() throws Exception {
         // first, ensure the form is created and persisted, by calling the controller display method
         testDefiningAnHtmlFormInUiResource();
         HtmlForm hf = htmlFormEntryService.getHtmlFormByForm(formService.getFormByUuid("form-uuid"));
 
         // make "Hippocrates" a provider
         Provider provider = new Provider();
         provider.setPerson(personService.getPerson(502));
         providerService.saveProvider(provider);
 
         Patient patient = patientService.getPatient(6);  // patient with a visit
         assertThat(encounterService.getEncountersByPatient(patient).size(), Matchers.is(0));
 
         String dateString = "2012-12-17";
         Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
 
         Visit visit = visitService.getVisit(4);
         // set the visit start and stop time for testing
         visit.setStartDatetime(new DateTime(2012, 12, 15, 0, 0, 0, 0).toDate());
         visit.setStopDatetime(new DateTime(2012, 12, 18, 0, 0, 0, 0).toDate());
 
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("w2", "70"); // weight in kg
         request.addParameter("w5", dateString); // date
         request.addParameter("w3hours", "0");
         request.addParameter("w3minutes", "0");
         request.addParameter("w3seconds", "0");
         request.addParameter("w7", "2"); // location = Xanadu
         request.addParameter("w9", "502"); // provider = Hippocrates
 
         SimpleObject result = controller.submit(sessionContext, patient, hf, null, visit, null, null, encounterService, adtService, resourceFactory, ui, request);
         assertThat((Boolean) result.get("success"), Matchers.is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), Matchers.is(1));
         Encounter created = encounterService.getEncountersByPatient(patient).get(0);
         assertThat(created.getEncounterDatetime(), Matchers.is(date));
         assertThat(created.getLocation().getId(), Matchers.is(2));
         assertThat(created.getProvidersByRoles().values().iterator().next(), IsCollectionContaining.hasItem(provider));
         assertThat(created.getAllObs().size(), Matchers.is(1));
         Obs weightObs = created.getAllObs().iterator().next();
         assertThat(weightObs.getConcept().getId(), Matchers.is(5089));
         assertThat(weightObs.getValueNumeric(), Matchers.is(Double.valueOf(70d)));
         assertNotNull(created.getVisit());
     }
 
     @Test
     public void testSubmittingHtmlFormDefinedInUiResourceShouldCreateOpenVisit() throws Exception {
         // first, ensure the form is created and persisted, by calling the controller display method
         testDefiningAnHtmlFormInUiResource();
         HtmlForm hf = htmlFormEntryService.getHtmlFormByForm(formService.getFormByUuid("form-uuid"));
 
         // make "Hippocrates" a provider
         Provider provider = new Provider();
         provider.setPerson(personService.getPerson(502));
         providerService.saveProvider(provider);
 
         Patient patient = patientService.getPatient(8);
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(0));
 
         Date date = new DateMidnight().toDate();
         String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
 
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("w2", "70"); // weight in kg
         request.addParameter("w5", dateString); // date
         request.addParameter("w3hours", "0");
         request.addParameter("w3minutes", "0");
         request.addParameter("w3seconds", "0");
         request.addParameter("w7", "2"); // location = Xanadu
         request.addParameter("w9", "502"); // provider = Hippocrates
 
         SimpleObject result = controller.submit(sessionContext, patient, hf, null, null, true, null, encounterService, adtService, resourceFactory, ui, request);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
         Encounter created = encounterService.getEncountersByPatient(patient).get(0);
 
         assertNotNull(created.getVisit());
        assertThat(created.getVisit().getStartDatetime(), is(date));
         assertNull(created.getVisit().getStopDatetime());
     }
 
     @Test
     public void testSubmittingHtmlFormDefinedInUiResourceShouldAssociateWithExistingVisit() throws Exception {
         // first, ensure the form is created and persisted, by calling the controller display method
         testDefiningAnHtmlFormInUiResource();
         HtmlForm hf = htmlFormEntryService.getHtmlFormByForm(formService.getFormByUuid("form-uuid"));
 
         // make "Hippocrates" a provider
         Provider provider = new Provider();
         provider.setPerson(personService.getPerson(502));
         providerService.saveProvider(provider);
 
         Patient patient = patientService.getPatient(8);
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(0));
 
         Visit visit = visitService.getVisit(1001);
 
         Date date = new DateMidnight(2012, 1, 20).toDate();
         String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
 
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("w2", "70"); // weight in kg
         request.addParameter("w5", dateString); // date
         request.addParameter("w3hours", "0");
         request.addParameter("w3minutes", "0");
         request.addParameter("w3seconds", "0");
         request.addParameter("w7", "2"); // location = Xanadu
         request.addParameter("w9", "502"); // provider = Hippocrates
 
         SimpleObject result = controller.submit(sessionContext, patient, hf, null, visit, true, null, encounterService, adtService, resourceFactory, ui, request);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
         Encounter created = encounterService.getEncountersByPatient(patient).get(0);
 
         assertNotNull(created.getVisit());
         assertThat(created.getVisit(), is(visit));
         assertThat(created.getEncounterDatetime(), is(visit.getStartDatetime()));  // make sure the encounter date has been shifted to match the visit start time of 10:10:10
     }
 
     @Test
     public void testEditingHtmlFormDefinedInUiResourceShouldNotChangeTimeOfEncounterDateIfNewDateHasNoTimeComponentAndIsNotDifferentFromCurrentDate() throws Exception {
         // first, ensure the form is created and persisted, by calling the controller display method
         testDefiningAnHtmlFormInUiResource();
         HtmlForm hf = htmlFormEntryService.getHtmlFormByForm(formService.getFormByUuid("form-uuid"));
 
         // make "Hippocrates" a provider
         Provider provider = new Provider();
         provider.setPerson(personService.getPerson(502));
         providerService.saveProvider(provider);
 
         Patient patient = patientService.getPatient(8);
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(0));
 
         Date initialEncounterDate = new DateTime(2012, 1, 20, 10, 10, 10, 0).toDate();
         String dateString = new SimpleDateFormat("yyyy-MM-dd").format(initialEncounterDate);
 
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("w2", "70"); // weight in kg
         request.addParameter("w5", dateString); // date
         request.addParameter("w3hours", "10");
         request.addParameter("w3minutes", "10");
         request.addParameter("w3seconds", "10");
         request.addParameter("w7", "2"); // location = Xanadu
         request.addParameter("w9", "502"); // provider = Hippocrates
 
         SimpleObject result = controller.submit(sessionContext, patient, hf, null, null, false, null, encounterService, adtService, resourceFactory, ui, request);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
         Encounter created = encounterService.getEncountersByPatient(patient).get(0);
 
         MockHttpServletRequest editRequest = new MockHttpServletRequest();
         editRequest.addParameter("w2", "70"); // weight in kg
         editRequest.addParameter("w5", dateString); // date
         editRequest.addParameter("w3hours", "0");  /// note that we are zeroing out the hour, minute and day component
         editRequest.addParameter("w3minutes", "0");
         editRequest.addParameter("w3seconds", "0");
         editRequest.addParameter("w7", "2"); // location = Xanadu
         editRequest.addParameter("w9", "502"); // provider = Hippocrates
 
         result = controller.submit(sessionContext, patient, hf, created, null, false, null, encounterService, adtService, resourceFactory, ui, editRequest);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
 
         // since the date we passed in the same Date as the existing encounter date, we don't want to have blown away
         // the time component
         assertThat(created.getEncounterDatetime(), is(initialEncounterDate));
 
     }
 
     @Test
     public void testEditingHtmlFormDefinedInUiResourceShouldChangeTimeOfEncounterDateIfNewDateHasTimeComponentEvenIfNotDifferentFromCurrentDate() throws Exception {
         // first, ensure the form is created and persisted, by calling the controller display method
         testDefiningAnHtmlFormInUiResource();
         HtmlForm hf = htmlFormEntryService.getHtmlFormByForm(formService.getFormByUuid("form-uuid"));
 
         // make "Hippocrates" a provider
         Provider provider = new Provider();
         provider.setPerson(personService.getPerson(502));
         providerService.saveProvider(provider);
 
         Patient patient = patientService.getPatient(8);
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(0));
 
         Date initialEncounterDate = new DateTime(2012, 1, 20, 10, 10, 10, 0).toDate();
         String dateString = new SimpleDateFormat("yyyy-MM-dd").format(initialEncounterDate);
 
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("w2", "70"); // weight in kg
         request.addParameter("w5", dateString); // date
         request.addParameter("w3hours", "10");
         request.addParameter("w3minutes", "10");
         request.addParameter("w3seconds", "10");
         request.addParameter("w7", "2"); // location = Xanadu
         request.addParameter("w9", "502"); // provider = Hippocrates
 
         SimpleObject result = controller.submit(sessionContext, patient, hf, null, null, false, null, encounterService, adtService, resourceFactory, ui, request);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
         Encounter created = encounterService.getEncountersByPatient(patient).get(0);
 
         Date updatedEncounterDate = new DateTime(2012, 1, 20, 20, 20, 20, 0).toDate();
         String updatedDateString = new SimpleDateFormat("yyyy-MM-dd").format(updatedEncounterDate);
 
         MockHttpServletRequest editRequest = new MockHttpServletRequest();
         editRequest.addParameter("w2", "70"); // weight in kg
         editRequest.addParameter("w5", updatedDateString); // date
         editRequest.addParameter("w3hours", "20");  /// note that we are zeroing out the hour, minute and day component
         editRequest.addParameter("w3minutes", "20");
         editRequest.addParameter("w3seconds", "20");
         editRequest.addParameter("w7", "2"); // location = Xanadu
         editRequest.addParameter("w9", "502"); // provider = Hippocrates
 
         result = controller.submit(sessionContext, patient, hf, created, null, false, null, encounterService, adtService, resourceFactory, ui, editRequest);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
 
         // this the date we input has an updated time component, we want to have updated the encounter date
         assertThat(created.getEncounterDatetime(), is(updatedEncounterDate));
 
     }
 
 
     @Test
     public void testEditingHtmlFormDefinedInUiResourceShouldChangeTimeOfEncounterDateIfNewDateDifferentFromOldDate() throws Exception {
         // first, ensure the form is created and persisted, by calling the controller display method
         testDefiningAnHtmlFormInUiResource();
         HtmlForm hf = htmlFormEntryService.getHtmlFormByForm(formService.getFormByUuid("form-uuid"));
 
         // make "Hippocrates" a provider
         Provider provider = new Provider();
         provider.setPerson(personService.getPerson(502));
         providerService.saveProvider(provider);
 
         Patient patient = patientService.getPatient(8);
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(0));
 
         Date initialEncounterDate = new DateTime(2012, 1, 20, 10, 10, 10, 0).toDate();
         String dateString = new SimpleDateFormat("yyyy-MM-dd").format(initialEncounterDate);
 
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("w2", "70"); // weight in kg
         request.addParameter("w5", dateString); // date
         request.addParameter("w3hours", "10");
         request.addParameter("w3minutes", "10");
         request.addParameter("w3seconds", "10");
         request.addParameter("w7", "2"); // location = Xanadu
         request.addParameter("w9", "502"); // provider = Hippocrates
 
         SimpleObject result = controller.submit(sessionContext, patient, hf, null, null, false, null, encounterService, adtService, resourceFactory, ui, request);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
         Encounter created = encounterService.getEncountersByPatient(patient).get(0);
 
         Date updatedEncounterDate = new DateTime(2012, 1, 22, 0,0, 0, 0).toDate();
         String updatedDateString = new SimpleDateFormat("yyyy-MM-dd").format(updatedEncounterDate);
 
         MockHttpServletRequest editRequest = new MockHttpServletRequest();
         editRequest.addParameter("w2", "70"); // weight in kg
         editRequest.addParameter("w5", updatedDateString); // date
         editRequest.addParameter("w3hours", "0");  /// note that we are zeroing out the hour, minute and day component
         editRequest.addParameter("w3minutes", "0");
         editRequest.addParameter("w3seconds", "0");
         editRequest.addParameter("w7", "2"); // location = Xanadu
         editRequest.addParameter("w9", "502"); // provider = Hippocrates
 
         result = controller.submit(sessionContext, patient, hf, created, null, false, null, encounterService, adtService, resourceFactory, ui, editRequest);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
 
         // this the date we input has an updated time component, we want to have updated the encounter date
         assertThat(created.getEncounterDatetime(), is(updatedEncounterDate));
 
     }
 }
