 package com.solidstategroup.radar.web.panels;
 
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.model.Ethnicity;
 import com.solidstategroup.radar.model.Sex;
 import com.solidstategroup.radar.web.RadarApplication;
 
 import com.solidstategroup.radar.web.components.RadarFormComponentFeedbackIndicator;
 import com.solidstategroup.radar.web.components.RadarRequiredDateTextField;
 import com.solidstategroup.radar.web.components.RadarRequiredDropdownChoice;
 import com.solidstategroup.radar.web.components.RadarRequiredTextField;
 import com.solidstategroup.radar.web.components.RadarTextFieldWithValidation;
 import com.solidstategroup.radar.web.pages.PatientPage;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.validation.validator.PatternValidator;
 
 <<<<<<< HEAD
 import java.util.ArrayList;
 =======
 >>>>>>> b500e94b2682c2dff215646d903405e52a2339e3
 import java.util.Arrays;
 import java.util.List;
 
 public class DemographicsPanel extends Panel {
 
     public DemographicsPanel(String id) {
         super(id);
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         Form<Demographics> form =
                 new Form<Demographics>("form", new CompoundPropertyModel<Demographics>(new Demographics()));
         add(form);
 
         final List<Component> componentsToUpdateList = new ArrayList<Component>();
 
         TextField radarNumber = new TextField("radarNumber");
         radarNumber.setEnabled(false);
 
         DateTextField dateRegistered = DateTextField.forDatePattern("dateRegistered", RadarApplication.DATE_PATTERN);
         dateRegistered.setEnabled(false);
 
         RadarRequiredDropdownChoice diagnosis = new RadarRequiredDropdownChoice("diagnosis", new Model<String>(), Arrays.asList("MPGN/DDD", "SRNS"), form, componentsToUpdateList);
 
         diagnosis.setRequired(true);
 
         // Basic fields
         RadarRequiredTextField surname = new RadarRequiredTextField("surname", form, componentsToUpdateList);
         RadarRequiredTextField forename = new RadarRequiredTextField("forename", form, componentsToUpdateList);
         RadarRequiredDateTextField dateOfBirth = new RadarRequiredDateTextField("dateOfBirth", RadarApplication.DATE_PATTERN, form, componentsToUpdateList);
         dateOfBirth.setRequired(true);
 
         form.add(diagnosis, surname, forename, dateOfBirth);
 


 
         RadarRequiredDropdownChoice sex = new RadarRequiredDropdownChoice("sex", Arrays.asList(tempSex), new ChoiceRenderer<Sex>("type"), form, componentsToUpdateList);
         DropDownChoice<Ethnicity> ethnicity = new DropDownChoice<Ethnicity>("ethnicity");
         form.add(sex, ethnicity);
 
 
         // Address fields
         TextField address1 = new TextField("address1");
         TextField address2 = new TextField("address2");
         TextField address3 = new TextField("address3");
         TextField address4 = new TextField("address4");
         RadarTextFieldWithValidation postcode = new RadarTextFieldWithValidation("postcode", new PatternValidator("[a-zA-Z]{1,2}[0-9][0-9A-Za-z]{0,1} {0,1}[0-9][A-Za-z]{2}$"), form, componentsToUpdateList);
         form.add(address1, address2, address3, address4, postcode);
 
         // Archive fields
         TextField surnameAlias = new TextField("surnameAlias");
         TextField previousPostcode = new TextField("previousPostcode");
         form.add(surnameAlias, previousPostcode);
 
         // More info
         RadarRequiredTextField hospitalNumber = new RadarRequiredTextField("hospitalNumber", form, componentsToUpdateList);
         TextField nhsNumber = new TextField("nhsNumber");
         TextField renalRegistryNumber = new TextField("renalRegistryNumber");
         TextField ukTransplantNumber = new TextField("ukTransplantNumber");
         TextField chiNumber = new TextField("chiNumber");
         form.add(hospitalNumber, nhsNumber, renalRegistryNumber, ukTransplantNumber, chiNumber);
 
 
         DropDownChoice status = new DropDownChoice("status");
         DropDownChoice consultant = new DropDownChoice("consultant");
         DropDownChoice renalUnit = new DropDownChoice("renalUnit");
         form.add(status, consultant, renalUnit);
 
         CheckBox consent = new CheckBox("consent");
         DropDownChoice renalUnitAuthorised = new DropDownChoice("renalUnitAuthorised");
         form.add(consent, renalUnitAuthorised);
 
         form.add(new AjaxSubmitLink("submit") {
             @Override
             protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 // Todo: Implement
             }
 
             @Override
             protected void onError(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 ajaxRequestTarget.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
 
             }
         });
     }
 
     @Override
     public boolean isVisible() {
         return ((PatientPage) getPage()).getCurrentTab().equals(PatientPage.CurrentTab.DEMOGRAPHICS);
     }
 }
