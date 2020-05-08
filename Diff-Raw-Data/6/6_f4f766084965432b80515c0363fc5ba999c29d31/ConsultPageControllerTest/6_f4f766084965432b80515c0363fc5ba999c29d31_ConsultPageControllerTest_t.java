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
 
 package org.openmrs.module.emr.page.controller.consult;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentMatcher;
 import org.mockito.MockitoAnnotations;
 import org.openmrs.Concept;
 import org.openmrs.ConceptName;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PersonName;
 import org.openmrs.Provider;
 import org.openmrs.api.ConceptService;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.module.emr.TestUiUtils;
 import org.openmrs.module.emr.consult.ConsultNote;
 import org.openmrs.module.emr.consult.ConsultService;
 import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
 import org.openmrs.module.emrapi.diagnosis.Diagnosis;
 import org.springframework.mock.web.MockHttpSession;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.startsWith;
 import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.Mock;
 
 /**
  *
  */
 public class ConsultPageControllerTest {
 
     @Mock
     ConsultService consultService;
 
     @Mock
     ConceptService conceptService;
 
     @Mock
     EmrProperties emrProperties;
 
     @Before
     public void initMocks() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void testSubmit() throws Exception {
         int primaryConceptNameId = 2460;
         int secondaryConceptId = 3;
         final String secondaryText = "Fatigue from too much testing";
         final String freeTextComments = "30 year old male, presenting with...";
 
         String diagnosisJson1 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"PRIMARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_NAME_PREFIX + primaryConceptNameId + "\" }";
         String diagnosisJson2 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"SECONDARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.CONCEPT_PREFIX + secondaryConceptId + "\" }";
         String diagnosisJson3 = "{ \"certainty\": \"PRESUMED\", \"diagnosisOrder\": \"SECONDARY\", \"diagnosis\": \"" + CodedOrFreeTextAnswer.NON_CODED_PREFIX + secondaryText + "\" }";
 
         Concept conceptFor2460 = new Concept();
         final ConceptName conceptName2460 = new ConceptName();
         conceptName2460.setConcept(conceptFor2460);
 
         final Concept concept3 = new Concept();
 
         when(conceptService.getConceptName(primaryConceptNameId)).thenReturn(conceptName2460);
         when(conceptService.getConcept(secondaryConceptId)).thenReturn(concept3);
 
         Patient patient = new Patient();
         patient.addName(new PersonName("Jean", "Paul", "Marie"));
         final Location sessionLocation = new Location();
         final Provider currentProvider = new Provider();
 
         EmrContext emrContext = new EmrContext();
         emrContext.setSessionLocation(sessionLocation);
         emrContext.setCurrentProvider(currentProvider);
 
         MockHttpSession httpSession = new MockHttpSession();
         ConsultPageController controller = new ConsultPageController();
         String result = controller.post(patient,
                 asList(diagnosisJson1, diagnosisJson2, diagnosisJson3),
                 freeTextComments,
                 httpSession,
                 consultService,
                 conceptService,
                 emrProperties, emrContext, new TestUiUtils());
 
         assertThat(result, startsWith("redirect:"));
         assertThat(httpSession.getAttribute(EmrConstants.SESSION_ATTRIBUTE_INFO_MESSAGE), notNullValue());
 
         verify(consultService).saveConsultNote(argThat(new ArgumentMatcher<ConsultNote>() {
             @Override
             public boolean matches(Object o) {
                 ConsultNote actual = (ConsultNote) o;
                return containsInAnyOrder(new Diagnosis(new CodedOrFreeTextAnswer(conceptName2460), Diagnosis.Order.PRIMARY),
                        new Diagnosis(new CodedOrFreeTextAnswer(concept3), Diagnosis.Order.SECONDARY),
                        new Diagnosis(new CodedOrFreeTextAnswer(secondaryText), Diagnosis.Order.SECONDARY)).matches(actual.getDiagnoses()) &&
                         actual.getComments().equals(freeTextComments) &&
                         actual.getEncounterLocation().equals(sessionLocation) &&
                         actual.getClinician().equals(currentProvider);
             }
         }));
     }
 
 }
