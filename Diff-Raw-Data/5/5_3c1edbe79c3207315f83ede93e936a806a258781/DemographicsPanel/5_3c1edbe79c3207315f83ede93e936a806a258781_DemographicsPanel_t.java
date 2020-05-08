 package com.solidstategroup.radar.web.panels;
 
 import com.solidstategroup.radar.model.Centre;
 import com.solidstategroup.radar.model.Consultant;
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.model.Diagnosis;
 import com.solidstategroup.radar.model.DiagnosisCode;
 import com.solidstategroup.radar.model.Ethnicity;
 import com.solidstategroup.radar.model.Sex;
 import com.solidstategroup.radar.model.Status;
 import com.solidstategroup.radar.model.user.User;
 import com.solidstategroup.radar.service.*;
 import com.solidstategroup.radar.web.RadarApplication;
 import com.solidstategroup.radar.web.RadarSecuredSession;
 import com.solidstategroup.radar.web.components.CentreDropDown;
 import com.solidstategroup.radar.web.components.ConsultantDropDown;
 import com.solidstategroup.radar.web.components.RadarComponentFactory;
 import com.solidstategroup.radar.web.components.RadarRequiredDateTextField;
 import com.solidstategroup.radar.web.components.RadarRequiredDropdownChoice;
 import com.solidstategroup.radar.web.components.RadarRequiredTextField;
 import com.solidstategroup.radar.web.components.RadarTextFieldWithValidation;
 import com.solidstategroup.radar.web.models.RadarModelFactory;
 import com.solidstategroup.radar.web.pages.PatientPage;
 import com.solidstategroup.radar.web.pages.content.ConsentFormsPage;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.PatternValidator;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class DemographicsPanel extends Panel {
 
     @SpringBean
     private DemographicsManager demographicsManager;
     @SpringBean
     private DiagnosisManager diagnosisManager;
     @SpringBean
     private ClinicalDataManager clinicalDataManager;
     @SpringBean
     private LabDataManager labDataManager;
     @SpringBean
     private TherapyManager therapyManager;
     @SpringBean
     private UtilityManager utilityManager;
 
     public DemographicsPanel(String id, final IModel<Long> radarNumberModel) {
         super(id);
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         // Set up model - if given radar number loadable detachable getting demographics by radar number
         final CompoundPropertyModel<Demographics> model = new CompoundPropertyModel<Demographics>(new LoadableDetachableModel<Demographics>() {
             @Override
             public Demographics load() {
                 Demographics demographicsModelObject = null;
                 if (radarNumberModel.getObject() != null) {
                     Long radarNumber;
                     try {
                         radarNumber = radarNumberModel.getObject();
                     } catch (ClassCastException e) {
                         Object obj = radarNumberModel.getObject();
                         radarNumber = Long.parseLong((String) obj);
                     }
                     demographicsModelObject = demographicsManager.getDemographicsByRadarNumber(radarNumber);
                 }
 
                 if (demographicsModelObject == null) {
                     demographicsModelObject = new Demographics();
                 }
                 return demographicsModelObject;
             }
         });
 
         // Set up form
         final Form<Demographics> form = new Form<Demographics>("form", model) {
             @Override
             protected void onSubmit() {
                 Demographics demographics = getModelObject();
                 // shouldnt need to do this but for some reason the id comes back with null!
                 if (radarNumberModel.getObject() != null) {
                     demographics.setId(radarNumberModel.getObject());
                 }
                 demographicsManager.saveDemographics(demographics);
                 radarNumberModel.setObject(demographics.getId());
 
                 // create new diagnosis if it doesnt exist becuase diagnosis code is set in demographics tab
                 Diagnosis diagnosis = diagnosisManager.getDiagnosisByRadarNumber(demographics.getId());
                 if (diagnosis == null) {
                     Diagnosis diagnosis_new = new Diagnosis();
                     diagnosis_new.setRadarNumber(demographics.getId());
                     DiagnosisCode diagnosisCode = (DiagnosisCode) ((DropDownChoice) get("diagnosis")).getModelObject();
                     diagnosis_new.setDiagnosisCode(diagnosisCode);
                     diagnosisManager.saveDiagnosis(diagnosis_new);
                 }
 
             }
         };
         add(form);
 
         final List<Component> componentsToUpdateList = new ArrayList<Component>();
 
         form.add(new Label("addNewPatientLabel", "Add a New Patient") {
             @Override
             public boolean isVisible() {
                 return radarNumberModel.getObject() == null;
             }
         });
 
        TextField<Long> radarNumberField = new TextField<Long>("radarNumber", radarNumberModel);
         radarNumberField.setEnabled(false);
         form.add(radarNumberField);
 
         DateTextField dateRegistered = DateTextField.forDatePattern("dateRegistered", RadarApplication.DATE_PATTERN);
         if (radarNumberModel.getObject() == null) {
             model.getObject().setDateRegistered(new Date());
         }
         form.add(dateRegistered);
 
 
         RadarRequiredDropdownChoice diagnosis =
                 new RadarRequiredDropdownChoice("diagnosis", RadarModelFactory.getDiagnosisCodeModel(radarNumberModel,
                         diagnosisManager),
                         diagnosisManager.getDiagnosisCodes(),
                         new ChoiceRenderer("abbreviation", "id"), form, componentsToUpdateList) {
                     @Override
                     public boolean isEnabled() {
                         RadarSecuredSession securedSession = RadarSecuredSession.get();
                         if(securedSession.getRoles().hasRole(User.ROLE_PATIENT)) {
                            return false;
                         }
                         return getModelObject() == null;
                     }
                 };
 
         // Basic fields
         RadarRequiredTextField surname = new RadarRequiredTextField("surname", form, componentsToUpdateList);
         RadarRequiredTextField forename = new RadarRequiredTextField("forename", form, componentsToUpdateList);
         RadarRequiredDateTextField dateOfBirth = new RadarRequiredDateTextField("dateOfBirth", form, componentsToUpdateList);
 
         dateOfBirth.setRequired(true);
 
         form.add(diagnosis, surname, forename, dateOfBirth);
 
         // Sex
         RadarRequiredDropdownChoice sex =
                 new RadarRequiredDropdownChoice("sex", demographicsManager.getSexes(), new ChoiceRenderer<Sex>("type",
                         "id"),
                         form, componentsToUpdateList);
 
         // Ethnicity
         DropDownChoice<Ethnicity> ethnicity = new DropDownChoice<Ethnicity>("ethnicity", utilityManager.getEthnicities(),
                 new ChoiceRenderer<Ethnicity>("name", "id"));
         form.add(sex, ethnicity);
 
         // Address fields
         TextField address1 = new TextField("address1");
         TextField address2 = new TextField("address2");
         TextField address3 = new TextField("address3");
         TextField address4 = new TextField("address4");
         RadarTextFieldWithValidation postcode = new RadarTextFieldWithValidation("postcode",
                 new PatternValidator("[a-zA-Z]{1,2}[0-9][0-9A-Za-z]{0,1} {0,1}[0-9][A-Za-z]{2}$"), form,
                 componentsToUpdateList);
         form.add(address1, address2, address3, address4, postcode);
 
         // Archive fields
         TextField surnameAlias = new TextField("surnameAlias");
         TextField previousPostcode = new TextField("previousPostcode");
         form.add(surnameAlias, previousPostcode);
 
         // More info
         RadarRequiredTextField hospitalNumber =
                 new RadarRequiredTextField("hospitalNumber", form, componentsToUpdateList);
         TextField nhsNumber = new TextField("nhsNumber");
         TextField renalRegistryNumber = new TextField("renalRegistryNumber");
         TextField ukTransplantNumber = new TextField("ukTransplantNumber");
         TextField chiNumber = new TextField("chiNumber");
         form.add(hospitalNumber, nhsNumber, renalRegistryNumber, ukTransplantNumber, chiNumber);
 
         // Status, consultants and centres drop down boxes
         DropDownChoice<Status> status = new DropDownChoice<Status>("status", demographicsManager.getStatuses(),
                 new ChoiceRenderer<Status>("abbreviation", "id"));
 
         // Consultant and renal unit
         DropDownChoice<Consultant> consultant = new ConsultantDropDown("consultant");
         DropDownChoice<Centre> renalUnit = new CentreDropDown("renalUnit");
 
         form.add(status, consultant, renalUnit);
 
         CheckBox consent = new CheckBox("consent");
         DropDownChoice<Centre> renalUnitAuthorised = new CentreDropDown("renalUnitAuthorised");
         form.add(consent, renalUnitAuthorised);
 
         form.add(new BookmarkablePageLink("consentFormsLink", ConsentFormsPage.class));
 
         final Label successMessage = RadarComponentFactory.getSuccessMessageLabel("successMessage", form,
                 componentsToUpdateList);
 
         Label errorMessage = RadarComponentFactory.getErrorMessageLabel("errorMessage", form, componentsToUpdateList);
 
        AjaxSubmitLink ajaxSubmitLink = new AjaxSubmitLink("save") {
 
             @Override
             protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 ajaxRequestTarget.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
                 successMessage.setVisible(true);
                 ajaxRequestTarget.add(successMessage);
             }
 
             @Override
             protected void onError(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 ajaxRequestTarget.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
                 successMessage.setVisible(false);
                 ajaxRequestTarget.add(successMessage);
             }
         };
 
         ajaxSubmitLink.add(new AttributeModifier("value", new AbstractReadOnlyModel() {
             @Override
             public Object getObject() {
                 return radarNumberModel.getObject() == null ? "Add this patient" : "Update";
             }
         }));
         form.add(ajaxSubmitLink);
     }
 
     @Override
     public boolean isVisible() {
         return ((PatientPage) getPage()).getCurrentTab().equals(PatientPage.CurrentTab.DEMOGRAPHICS);
     }
 }
