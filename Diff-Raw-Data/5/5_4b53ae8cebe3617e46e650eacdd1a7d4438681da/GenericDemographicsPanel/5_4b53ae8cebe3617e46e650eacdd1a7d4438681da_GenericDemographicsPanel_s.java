 package org.patientview.radar.web.panels.generic;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.util.parse.metapattern.MetaPattern;
 import org.apache.wicket.validation.validator.PatternValidator;
 import org.patientview.model.Centre;
 import org.patientview.model.Ethnicity;
 import org.patientview.model.Patient;
 import org.patientview.model.Sex;
 import org.patientview.radar.exception.RegisterException;
 import org.patientview.radar.model.user.ProfessionalUser;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.service.DemographicsManager;
 import org.patientview.radar.service.UserManager;
 import org.patientview.radar.service.UtilityManager;
 import org.patientview.radar.service.generic.GenericDiagnosisManager;
 import org.patientview.radar.util.RadarUtility;
 import org.patientview.radar.web.RadarApplication;
 import org.patientview.radar.web.RadarSecuredSession;
 import org.patientview.radar.web.components.CentreDropDown;
 import org.patientview.radar.web.components.ClinicianDropDown;
 import org.patientview.radar.web.components.ComponentHelper;
 import org.patientview.radar.web.components.LabelMessage;
 import org.patientview.radar.web.components.RadarComponentFactory;
 import org.patientview.radar.web.components.RadarRequiredCheckBox;
 import org.patientview.radar.web.components.RadarRequiredDateTextField;
 import org.patientview.radar.web.components.RadarRequiredDropdownChoice;
 import org.patientview.radar.web.components.RadarRequiredTextField;
 import org.patientview.radar.web.components.RadarTextFieldWithValidation;
 import org.patientview.radar.web.panels.PatientDetailPanel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 public class GenericDemographicsPanel extends Panel {
 
     @SpringBean
     private DemographicsManager demographicsManager;
 
     @SpringBean
     private UtilityManager utilityManager;
 
     @SpringBean
     private GenericDiagnosisManager genericDiagnosisManager;
 
     @SpringBean
     private UserManager userManager;
 
     private MarkupContainer dateOfGenericDiagnosisContainer;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(GenericDemographicsPanel.class);
 
 
 
     public GenericDemographicsPanel(String id, Patient patient) {
         super(id);
         init(patient);
     }
 
     private void init(Patient patient) {
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         List<Component> nonEditableComponents = new ArrayList<Component>();
 
         final ProfessionalUser user = (ProfessionalUser) RadarSecuredSession.get().getUser();
 
         if (patient.getDateReg() == null) {
             patient.setDateReg(new Date());
         }
 
         // components to update on ajax refresh
         final List<Component> componentsToUpdateList = new ArrayList<Component>();
 
         // add form
         final IModel<Patient> model = new Model(patient);
 
         //Error Message
         String message = "Please complete all mandatory fields";
         final LabelMessage labelMessage = new LabelMessage();
         labelMessage.setMessage(message);
         final PropertyModel<LabelMessage> messageModel =
                 new PropertyModel<LabelMessage>(labelMessage, "message");
 
         // no exist data in patient table, then use the user name to populate.
         if (patient.getSurname() == null || patient.getForename() == null) {
 
             String name = utilityManager.getUserName(patient.getNhsno());
 
             if (name != null && !"".equals(name)) {
                 // split the user name with a space
                 String[] names = name.split(" ");
                 if (names != null && names.length >= 2) {
                     patient.setForename(name.substring(0,
                             name.indexOf(names[names.length - 1])));
                     patient.setSurname(names[names.length - 1]);
 
                 } else {
                     patient.setForename(name);
                 }
             }
         }
 
         Form<Patient> form = new Form<Patient>("form", new CompoundPropertyModel(model)) {
             @Override
             protected void onSubmit() {
                 Patient patient = getModel().getObject();
 
                 // make sure diagnosis date is after dob
                 if (patient.getDateOfGenericDiagnosis() != null
                         && patient.getDateOfGenericDiagnosis().compareTo(patient.getDob()) < 0) {
                     get("dateOfGenericDiagnosisContainer:dateOfGenericDiagnosis")
                             .error("Your diagnosis date cannot be before your date of birth");
                 }
 
                 patient.setGeneric(true);
                 patient.setRadarConsentConfirmedByUserId(user.getUserId());
 
                 try {
 
                      userManager.savePatientUser(patient);
 
                 } catch (RegisterException re) {
                     LOGGER.error("Registration Exception", re);
                     String message = "Failed to register patient: " + re.getMessage();
                     labelMessage.setMessage(message);
                     error(message);
                 } catch (Exception e) {
                     String message = "Error registering new patient to accompany this demographic";
                     LOGGER.error("{}, message {}", message, e.getMessage());
                     error(message);
                 }
             }
         };
 
         add(form);
 
         WebMarkupContainer patientDetail = new PatientDetailPanel("patientDetail", patient, "Demographics");
         patientDetail.setOutputMarkupId(true);
         patientDetail.setOutputMarkupPlaceholderTag(true);
         form.add(patientDetail);
         componentsToUpdateList.add(patientDetail);
 
         RadarRequiredTextField surname = new RadarRequiredTextField("surname", form, componentsToUpdateList);
         RadarRequiredTextField forename = new RadarRequiredTextField("forename", form, componentsToUpdateList);
         TextField alias = new TextField("surnameAlias");
         RadarRequiredDateTextField dateOfBirth = new RadarRequiredDateTextField("dob", form,
                 componentsToUpdateList);
 
         form.add(surname, forename, alias, dateOfBirth);
         nonEditableComponents.add(surname);
         nonEditableComponents.add(forename);
         nonEditableComponents.add(dateOfBirth);
         // Sex
         RadarRequiredDropdownChoice sex =
                 new RadarRequiredDropdownChoice("sexModel", demographicsManager.getSexes(),
                         new ChoiceRenderer<Sex>("type", "id"), form, componentsToUpdateList);
 
         nonEditableComponents.add(sex);
 
         // Ethnicity
         DropDownChoice<Ethnicity> ethnicity = new DropDownChoice<Ethnicity>("ethnicity", utilityManager.
                 getEthnicities(), new ChoiceRenderer<Ethnicity>("name", "id"));
         form.add(sex, ethnicity);
 
         nonEditableComponents.add(ethnicity);
 
         // Address fields
         TextField address1 = new TextField("address1");
         TextField address2 = new TextField("address2");
         TextField address3 = new TextField("address3");
         TextField address4 = new TextField("address4");
         RadarTextFieldWithValidation postcode = new RadarTextFieldWithValidation("postcode",
                 new PatternValidator("[a-zA-Z]{1,2}[0-9][0-9A-Za-z]{0,1} {0,1}[0-9][A-Za-z]{2}$"), form,
                 componentsToUpdateList);
         form.add(address1, address2, address3, address4, postcode);
 
         nonEditableComponents.add(address1);
         nonEditableComponents.add(address2);
         nonEditableComponents.add(address3);
         nonEditableComponents.add(address4);
         nonEditableComponents.add(postcode);
         // More info
         Label nhsNumber = new Label("nhsno");
 
         WebMarkupContainer nhsNumberContainer = new WebMarkupContainer("nhsNumberContainer");
 
         nhsNumberContainer.add(nhsNumber);
 
         // add new ids section
         final List<Component> addIdComponentsToUpdate = new ArrayList<Component>();
 
         IModel<AddIdModel> addIdModel = new Model<AddIdModel>(new AddIdModel());
         Form<AddIdModel> addIdForm = new Form<AddIdModel>("addIdForm", new CompoundPropertyModel(addIdModel)) {
             @Override
             protected void onSubmit() {
                 AddIdModel idModel = getModel().getObject();
                 Patient patient = model.getObject();
                 String id = idModel.getId();
                 if (idModel.getIdType() != null) {
                     if (idModel.getIdType().equals(IdType.CHANNELS_ISLANDS)) {
                         patient.setChannelIslandsId(id);
                     }
                     if (idModel.getIdType().equals(IdType.HOSPITAL_NUMBER)) {
                         patient.setHospitalnumber(id);
                     }
                     if (idModel.getIdType().equals(IdType.INDIA)) {
                         patient.setIndiaId(id);
                     }
                     if (idModel.getIdType().equals(IdType.RENAL_REGISTRY_NUMBER)) {
                         patient.setRrNo(id);
                     }
                     if (idModel.getIdType().equals(IdType.REPUBLIC_OF_IRELAND)) {
                         patient.setRepublicOfIrelandId(id);
                     }
                     if (idModel.getIdType().equals(IdType.UK_TRANSPLANT_NUMBER)) {
                         patient.setUktNo(id);
                     }
                 }
             }
 
         };
 
         AjaxSubmitLink addIdSubmit = new AjaxSubmitLink("addIdSubmit") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 ComponentHelper.updateComponentsIfParentIsVisible(target, addIdComponentsToUpdate);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 ComponentHelper.updateComponentsIfParentIsVisible(target, addIdComponentsToUpdate);
             }
         };
 
         TextField addIdValue = new TextField("id");
 
         DropDownChoice addIdType = null;
 
         // Link patients should not be able to add hospital numbers
         if (patient.isLink()) {
             addIdType =
                 new DropDownChoice("idType", Arrays.asList(IdType.HOSPITAL_NUMBER,
                         IdType.RENAL_REGISTRY_NUMBER, IdType.UK_TRANSPLANT_NUMBER, IdType.REPUBLIC_OF_IRELAND,
                         IdType.CHANNELS_ISLANDS, IdType.INDIA), new ChoiceRenderer());
         } else {
             addIdType =
                     new DropDownChoice("idType", Arrays.asList(
                             IdType.RENAL_REGISTRY_NUMBER, IdType.UK_TRANSPLANT_NUMBER, IdType.REPUBLIC_OF_IRELAND,
                             IdType.CHANNELS_ISLANDS, IdType.INDIA), new ChoiceRenderer());
         }
 
         addIdForm.add(addIdValue, addIdType, addIdSubmit);
         form.add(addIdForm);
 
         TextField hospitalNumber = new TextField("hospitalnumber");
         WebMarkupContainer hospitalNumberContainer = new WebMarkupContainer("hospitalNumberContainer") {
             @Override
             public boolean isVisible() {
                 if (model.getObject().getHospitalnumber() != null) {
                     if (!model.getObject().getHospitalnumber().isEmpty()) {
                         return true;
                     }
                 }
                 return false;
             }
         };
 
         hospitalNumberContainer.add(hospitalNumber);
         nonEditableComponents.add(hospitalNumber);
 
         TextField renalRegistryNumber = new TextField("rrNo");
         WebMarkupContainer renalRegistryNumberContainer = new WebMarkupContainer("renalRegistryNumberContainer") {
             @Override
             public boolean isVisible() {
                 if (model.getObject().getRrNo() != null) {
                     if (!model.getObject().getRrNo().isEmpty()) {
                         return true;
                     }
                 }
                 return false;
             }
         };
         renalRegistryNumberContainer.add(renalRegistryNumber);
 
         TextField ukTransplantNumber = new TextField("uktNo");
 
         WebMarkupContainer ukTransplantNumberContainer = new WebMarkupContainer("ukTransplantNumberContainer") {
             @Override
             public boolean isVisible() {
                 if (model.getObject().getUktNo() != null) {
                     if (!model.getObject().getUktNo().isEmpty()) {
                         return true;
                     }
                 }
                 return false;
             }
         };
         ukTransplantNumberContainer.add(ukTransplantNumber);
 
         // add other generic ids
         TextField republicOfIrelandId = new TextField("republicOfIrelandId");
 
         WebMarkupContainer republicOfIrelandIdContainer = new WebMarkupContainer("republicOfIrelandIdContainer") {
             @Override
             public boolean isVisible() {
                 if (model.getObject().getRepublicOfIrelandId() != null) {
                     if (!model.getObject().getRepublicOfIrelandId().isEmpty()) {
                         return true;
                     }
                 }
                 return false;
             }
         };
         republicOfIrelandIdContainer.add(republicOfIrelandId);
 
         TextField isleOfManId = new TextField("isleOfManId");
 
         WebMarkupContainer isleOfManIdContainer = new WebMarkupContainer("isleOfManIdContainer") {
             @Override
             public boolean isVisible() {
                 if (model.getObject().getIsleOfManId() != null) {
                     if (!model.getObject().getIsleOfManId().isEmpty()) {
                         return true;
                     }
                 }
                 return false;
             }
         };
 
         isleOfManIdContainer.add(isleOfManId);
 
         TextField channelIslandsId = new TextField("channelIslandsId");
 
         WebMarkupContainer channelIslandsIdContainer = new WebMarkupContainer("channelIslandsIdContainer") {
             @Override
             public boolean isVisible() {
                 if (model.getObject().getChannelIslandsId() != null) {
                     if (!model.getObject().getChannelIslandsId().isEmpty()) {
                         return true;
                     }
                 }
                 return false;
             }
         };
         channelIslandsIdContainer.add(channelIslandsId);
 
         TextField indiaId = new TextField("indiaId");
 
         WebMarkupContainer indiaIdContainer = new WebMarkupContainer("indiaIdContainer") {
             @Override
             public boolean isVisible() {
                 if (model.getObject().getIndiaId() != null) {
                     if (!model.getObject().getIndiaId().isEmpty()) {
                         return true;
                     }
                 }
                 return false;
             }
         };
         indiaIdContainer.add(indiaId);
 
         addIdComponentsToUpdate.add(hospitalNumberContainer);
         addIdComponentsToUpdate.add(renalRegistryNumberContainer);
         addIdComponentsToUpdate.add(ukTransplantNumberContainer);
         addIdComponentsToUpdate.add(republicOfIrelandIdContainer);
         addIdComponentsToUpdate.add(isleOfManIdContainer);
         addIdComponentsToUpdate.add(channelIslandsIdContainer);
         addIdComponentsToUpdate.add(indiaIdContainer);
 
         for (Component component : Arrays.asList(hospitalNumberContainer, renalRegistryNumberContainer,
                 ukTransplantNumberContainer, republicOfIrelandIdContainer, isleOfManIdContainer,
                 channelIslandsIdContainer, indiaIdContainer)) {
             component.setOutputMarkupPlaceholderTag(true);
         }
 
         form.add(hospitalNumberContainer, nhsNumberContainer, renalRegistryNumberContainer,
                 ukTransplantNumberContainer);
         form.add(republicOfIrelandIdContainer, isleOfManIdContainer, channelIslandsIdContainer, indiaIdContainer);
 
 
         // Consultant and renal unit
         final IModel<String> centreNumber = new Model<String>();
         Centre renalUnitSelected = form.getModelObject().getRenalUnit();
         centreNumber.setObject(renalUnitSelected != null ? renalUnitSelected.getUnitCode() : null);
 
         final ClinicianDropDown clinician = new ClinicianDropDown("clinician", centreNumber);
         form.add(clinician);
 
 
         Label sourceUnitCode = new Label("sourceUnitCode", patient.getUnitcode()) ;
         form.add(sourceUnitCode);
 
 
         DropDownChoice<Centre> renalUnit;
 
         // if its a super user then the drop down will let them change renal units
         // if its a normal user they can only add to their own renal unit
         if (user.getSecurityRole().equals(User.ROLE_SUPER_USER)) {
             renalUnit = new CentreDropDown("renalUnit", patient.getNhsno());
 
             renalUnit.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                 @Override
                 protected void onUpdate(AjaxRequestTarget target) {
                     Patient patient = model.getObject();
                     if (patient != null) {
                         centreNumber.setObject(patient.getRenalUnit() != null ?
                                 patient.getRenalUnit().getUnitCode() :
                                 null);
                     }
 
                     clinician.clearInput();
                     target.add(clinician);
                 }
             });
         } else if (user.getSecurityRole().equals(User.ROLE_PROFESSIONAL)) {
 
             List<Centre> centres = new ArrayList<Centre>();
             for (String unitCode : userManager.getUnitCodes(user)) {
                 Centre centre = new Centre();
                 centre.setUnitCode(unitCode);
                 centre.setName(unitCode);
                 centres.add(centre);
             }
             renalUnit = new CentreDropDown("renalUnit", centres);
 
         } else {
             List<Centre> centres = new ArrayList<Centre>();
             centres.add(form.getModelObject().getRenalUnit());
 
             renalUnit = new CentreDropDown("renalUnit", centres);
         }
 
 
         form.add(renalUnit);
 
         final IModel<String> consentUserModel = new Model<String>(utilityManager.getUserName(
                 patient.getRadarConsentConfirmedByUserId()));
 
 
 
         final Label tickConsentUser = new Label("radarConsentConfirmedByUserId",
                 consentUserModel) {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotEmpty(consentUserModel.getObject());
             }
         };
         tickConsentUser.setOutputMarkupId(true);
         tickConsentUser.setOutputMarkupPlaceholderTag(true);
         form.add(tickConsentUser);
 
         final RadarRequiredCheckBox consent = new RadarRequiredCheckBox("consent", form, componentsToUpdateList);
 
         consent.add(new AjaxFormComponentUpdatingBehavior("onclick") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
 
                 target.add(tickConsentUser);
 
                 if (consent.getModel().getObject().equals(Boolean.TRUE)) {
                     consentUserModel.setObject(RadarSecuredSession.get().getUser().getName());
                     tickConsentUser.setVisible(true);
 
                 } else {
                     tickConsentUser.setVisible(false);
                 }
             }
         });
 
         form.add(consent);
 
         form.add(new ExternalLink("consentFormsLink", "http://www.rarerenal.org/join/criteria-and-consent/"));
 
 
 
         // add generic fields
         TextField emailAddress = new TextField("emailAddress");
         TextField phone1 = new TextField("telephone1");
         TextField phone2 = new TextField("telephone2");
 
         nonEditableComponents.add(phone1);
 
         RadarTextFieldWithValidation mobile = new RadarTextFieldWithValidation("mobile",
                 new PatternValidator(MetaPattern.DIGITS), form,
                 componentsToUpdateList);
 
         RadarRequiredDropdownChoice genericDiagnosis =
                 new RadarRequiredDropdownChoice("genericDiagnosisModel", genericDiagnosisManager.getByDiseaseGroup(
                         patient.getDiseaseGroup()), new ChoiceRenderer("term", "id"), form,
                         componentsToUpdateList);
 
         final IModel<Boolean> diagnosisDateVisibility =
                 new Model<Boolean>(Boolean.FALSE);
 
        CheckBox diagnosisDateSelect = new CheckBox("diagnosisDateSelect");
         model.getObject().setDiagnosisDateSelect(model.getObject().getDiagnosisDateSelect() == Boolean.TRUE);
 
         diagnosisDateSelect.add(new AjaxFormComponentUpdatingBehavior("onClick") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                diagnosisDateVisibility.setObject(model.getObject().getDiagnosisDateSelect());
                 target.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
             }
         });
 
         RadarRequiredDateTextField dateOfGenericDiagnosis = new RadarRequiredDateTextField("dateOfGenericDiagnosis",
                 form, componentsToUpdateList);
 
         form.add(diagnosisDateSelect);
 
         MarkupContainer dateOfGenericDiagnosisContainer = new WebMarkupContainer("dateOfGenericDiagnosisContainer") {
             @Override
             public boolean isVisible() {
                 if (diagnosisDateVisibility.getObject()) {
                     return false;
                 } else {
                     return true;
                 }
             }
         };
         dateOfGenericDiagnosisContainer.add(dateOfGenericDiagnosis);
         componentsToUpdateList.add(dateOfGenericDiagnosisContainer);
         dateOfGenericDiagnosisContainer.setOutputMarkupId(true);
         dateOfGenericDiagnosisContainer.setOutputMarkupPlaceholderTag(true);
 
         this.dateOfGenericDiagnosisContainer = dateOfGenericDiagnosisContainer;
 
         TextArea otherClinicianAndContactInfo = new TextArea("otherClinicianAndContactInfo");
         TextArea comments = new TextArea("comments");
 
         form.add(emailAddress, phone1, phone2, mobile, genericDiagnosis, dateOfGenericDiagnosisContainer,
                 otherClinicianAndContactInfo, comments);
 
         RadioGroup<Patient.RRTModality> rrtModalityRadioGroup = new RadioGroup<Patient.RRTModality>(
                 "rrtModalityEunm");
         rrtModalityRadioGroup.add(new Radio("hd", new Model(Patient.RRTModality.HD)));
         rrtModalityRadioGroup.add(new Radio("pd", new Model(Patient.RRTModality.PD)));
         rrtModalityRadioGroup.add(new Radio("tx", new Model(Patient.RRTModality.Tx)));
         rrtModalityRadioGroup.add(new Radio("none", new Model(Patient.RRTModality.NONE)));
 
         form.add(rrtModalityRadioGroup);
 
         RadarComponentFactory.getSuccessMessageLabel("successMessage", form,
                 componentsToUpdateList);
 
         RadarComponentFactory.getSuccessMessageLabel("successMessageUp", form,
                 componentsToUpdateList);
 
         RadarComponentFactory.getMessageLabel("errorMessage", form,
                 messageModel, componentsToUpdateList);
         RadarComponentFactory.getMessageLabel("errorMessageUp", form,
                 messageModel, componentsToUpdateList);
 
 
 
         AjaxSubmitLink ajaxSubmitLinkTop = new AjaxSubmitLink("saveTop") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 ComponentHelper.updateComponentsIfParentIsVisible(target, componentsToUpdateList);
                 target.appendJavaScript(RadarApplication.FORM_IS_DIRTY_FALSE_SCRIPT);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 ComponentHelper.updateComponentsIfParentIsVisible(target, componentsToUpdateList);
             }
         };
 
         AjaxSubmitLink ajaxSubmitLinkBottom = new AjaxSubmitLink("saveBottom") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 ComponentHelper.updateComponentsIfParentIsVisible(target, componentsToUpdateList);
                 target.appendJavaScript(RadarApplication.FORM_IS_DIRTY_FALSE_SCRIPT);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 ComponentHelper.updateComponentsIfParentIsVisible(target, componentsToUpdateList);
             }
         };
 
         form.add(ajaxSubmitLinkTop);
         form.add(ajaxSubmitLinkBottom);
 
         if (!patient.isEditableDemographics()) {
             for (Component component : nonEditableComponents) {
                 component.setEnabled(false);
             }
         }
 
     }
 
     private static class AddIdModel implements Serializable {
         String id;
         IdType idType;
 
         public String getId() {
             return id;
         }
 
         public void setId(String id) {
             this.id = id;
         }
 
         public IdType getIdType() {
             return idType;
         }
 
         public void setIdType(IdType idType) {
             this.idType = idType;
         }
     }
 
     enum IdType {
         HOSPITAL_NUMBER,
         RENAL_REGISTRY_NUMBER,
         UK_TRANSPLANT_NUMBER,
         REPUBLIC_OF_IRELAND,
         CHANNELS_ISLANDS,
         INDIA;
 
         @Override
         public String toString() {
             return RadarUtility.getLabelFromEnum(super.toString());
         }
     }
 }
