 package org.motechproject.ghana.national.handlers;
 
 import org.motechproject.ghana.national.bean.RegisterClientForm;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.PatientAttributes;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.ghana.national.tools.Utility;
 import org.motechproject.mobileforms.api.callbacks.FormPublishHandler;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.mrs.model.Attribute;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 @Component
 public class PatientRegistrationFormHandler implements FormPublishHandler {
 
     public static final String FORM_BEAN = "formBean";
     private final Logger log = LoggerFactory.getLogger(this.getClass());
 
     @Autowired
     private PatientService patientService;
 
     @Override
    @MotechListener(subjects = "form.validation.successful.NurseDataEntry.registerPatient-jf")
     @LoginAsAdmin
     @ApiSession
     public void handleFormEvent(MotechEvent event) {
         try {
             RegisterClientForm registerClientForm = (RegisterClientForm) event.getParameters().get(FORM_BEAN);
 
             MRSPerson mrsPerson = new MRSPerson().firstName(registerClientForm.getFirstName()).middleName(registerClientForm.getMiddleName()).
                     lastName(registerClientForm.getLastName()).preferredName(registerClientForm.getPrefferedName()).dateOfBirth(registerClientForm.getDateOfBirth()).
                     birthDateEstimated(registerClientForm.getEstimatedBirthDate()).gender(registerClientForm.getSex()).address(registerClientForm.getAddress()).attributes(getPatientAttributes(registerClientForm));
 
             MRSPatient mrsPatient = new MRSPatient(registerClientForm.getMotechId(), mrsPerson, new MRSFacility(registerClientForm.getFacilityId()));
 
             patientService.registerPatient(new Patient(mrsPatient), registerClientForm.getRegistrantType(), registerClientForm.getMotherMotechId());
         } catch (Exception e) {
             log.error("Exception while saving patient", e);
         }
     }
 
     private List<Attribute> getPatientAttributes(RegisterClientForm registerClientForm) {
         List<Attribute> attributes = new ArrayList<Attribute>();
         attributes.add(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), registerClientForm.getPhoneNumber()));
         attributes.add(new Attribute(PatientAttributes.NHIS_EXPIRY_DATE.getAttribute(), Utility.safeToString(registerClientForm.getNhisExpires())));
         attributes.add(new Attribute(PatientAttributes.NHIS_NUMBER.getAttribute(), registerClientForm.getNhis()));
         attributes.add(new Attribute(PatientAttributes.INSURED.getAttribute(), Utility.safeToString(registerClientForm.getInsured())));
         return attributes;
     }
 }
