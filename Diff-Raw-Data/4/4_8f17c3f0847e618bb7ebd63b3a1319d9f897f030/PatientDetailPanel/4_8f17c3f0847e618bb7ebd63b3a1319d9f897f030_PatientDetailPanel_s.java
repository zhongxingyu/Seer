 package org.patientview.radar.web.panels;
 
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.datetime.PatternDateConverter;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.PropertyModel;
 import org.patientview.model.Patient;
 import org.patientview.radar.web.RadarApplication;
 
 import java.util.Date;
 
 public class PatientDetailPanel extends Panel {
     public PatientDetailPanel(String id, final Patient patient, String title) {
         super(id);
 
         WebMarkupContainer details = new WebMarkupContainer("details", new CompoundPropertyModel<Object>(patient));
         details.setOutputMarkupId(true);
         details.setOutputMarkupPlaceholderTag(true);
         add(details);
 
         // Note: this panel is shown after initial enter new patient, and you may not have all patient data yet
 
         /**
          * Add components
          */
 
         // title
         details.add(new Label("title", title));
 
         // radar number
        TextField radarNumberField = null;
        if (patient.getRadarNo() == null) {
             radarNumberField = new TextField<Long>("id", new PropertyModel<Long>(patient, "radarNo"));
         } else {
             radarNumberField = new TextField<Long>("id");
         }
         radarNumberField.setOutputMarkupId(true);
         radarNumberField.setOutputMarkupPlaceholderTag(true);
         details.add(radarNumberField);
 
         // disease group
         if (patient.getDiseaseGroup() != null) {
             Label diseaseGroup = new Label("diseaseGroup", new PropertyModel<Object>(patient.getDiseaseGroup(),
                     "name"));
             details.add(diseaseGroup);
         }
 
         // forename
         Label nameLabel = new Label("nameLabel", "Patient Name") {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patient.getForename());
             }
         };
         nameLabel.setOutputMarkupId(true);
         nameLabel.setOutputMarkupPlaceholderTag(true);
         details.add(nameLabel);
 
         TextField<Long> forename = new TextField<Long>("forename") {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patient.getForename());
             }
         };
         forename.setOutputMarkupId(true);
         forename.setOutputMarkupPlaceholderTag(true);
         details.add(forename);
 
         // surname
         TextField<Long> surname = new TextField<Long>("surname") {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patient.getSurname());
             }
         };
         surname.setOutputMarkupId(true);
         surname.setOutputMarkupPlaceholderTag(true);
         details.add(surname);
 
         // date registered
         DateTextField dateRegistered = DateTextField.forDatePattern("dateReg", RadarApplication.DATE_PATTERN);
         details.add(dateRegistered);
 
         // date of birth
         Label dobLabel = new Label("dobLabel", "Patient DOB") {
             @Override
             public boolean isVisible() {
                 return patient.getDob() != null;
             }
         };
         dobLabel.setOutputMarkupId(true);
         dobLabel.setOutputMarkupPlaceholderTag(true);
         details.add(dobLabel);
 
         DateTextField dateOfBirthTextField = new DateTextField("dob",
                 new PropertyModel<Date>(patient, "dob"), new PatternDateConverter(
                 RadarApplication.DATE_PATTERN, true)) {
             @Override
             public boolean isVisible() {
                 return patient.getDob() != null;
             }
         };
         dateOfBirthTextField.setOutputMarkupId(true);
         dateOfBirthTextField.setOutputMarkupPlaceholderTag(true);
         details.add(dateOfBirthTextField);
     }
 }
