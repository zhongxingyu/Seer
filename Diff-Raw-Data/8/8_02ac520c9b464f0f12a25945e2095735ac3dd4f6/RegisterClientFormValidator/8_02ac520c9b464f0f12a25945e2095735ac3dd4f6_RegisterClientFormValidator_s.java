 package org.motechproject.ghana.national.validator;
 
import org.drools.core.util.StringUtils;
 import org.motechproject.ghana.national.bean.RegisterClientForm;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.PatientType;
 import org.motechproject.ghana.national.domain.RegistrationType;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.ghana.national.validator.patient.*;
 import org.motechproject.mobileforms.api.domain.FormBean;
 import org.motechproject.mobileforms.api.domain.FormBeanGroup;
 import org.motechproject.mobileforms.api.domain.FormError;
 import org.motechproject.mobileforms.api.validator.FormValidator;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 import static org.motechproject.ghana.national.domain.Constants.NOT_FOUND;
 
 @Component
 public class RegisterClientFormValidator extends FormValidator<RegisterClientForm> {
 
     @Autowired
     private PatientService patientService;
     @Autowired
     private org.motechproject.ghana.national.validator.FormValidator formValidator;
 
     @Override
     @LoginAsAdmin
     @ApiSession
     public List<FormError> validate(RegisterClientForm formBean, FormBeanGroup group, List<FormBean> allForms) {
         List<FormError> formErrors = super.validate(formBean, group, allForms);
         formErrors.addAll(formValidator.validateIfStaffExists(formBean.getStaffId()));
         formErrors.addAll(formValidator.validateIfFacilityExists(formBean.getFacilityId()));
 
         String mothersMotechId = "Mothers motech Id";
 
         Patient patient = formValidator.getPatient(formBean.getMotechId());
 
         PatientValidator validators = new AlwaysValid().onSuccess(new NotExistsInDb(), RegistrationType.USE_PREPRINTED_ID.equals(formBean.getRegistrationMode()))
                 .onSuccess(new IsFormSubmittedForAChild(formBean.getDateOfBirth()), PatientType.CHILD_UNDER_FIVE.equals(formBean.getRegistrantType()))
                 .onSuccess(new IsFormSubmittedForAFemale(formBean.getSex()), PatientType.PREGNANT_MOTHER.equals(formBean.getRegistrantType()))
                 .onSuccess(new ExistsInDb(new FormError(mothersMotechId, NOT_FOUND))
                         .onFailure(new RegClientFormSubmittedInSameUploadForMotechId(formBean.getMotherMotechId())), PatientType.CHILD_UNDER_FIVE.equals(formBean.getRegistrantType()) && !StringUtils.isEmpty(formBean.getMotherMotechId()));
 
         formErrors.addAll(getDependentValidator().validate(patient, group.getFormBeans(), allForms, validators));
         return formErrors;
     }
 
     DependentValidator getDependentValidator() {
         return new DependentValidator();
     }
 }
 
