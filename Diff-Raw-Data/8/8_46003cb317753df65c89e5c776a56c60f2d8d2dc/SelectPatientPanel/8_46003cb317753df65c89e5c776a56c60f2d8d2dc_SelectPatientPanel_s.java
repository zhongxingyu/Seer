 package org.patientview.radar.web.panels;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.patientview.model.Patient;
 import org.patientview.radar.exception.PatientLinkException;
 import org.patientview.radar.model.generic.AddPatientModel;
 import org.patientview.radar.service.DemographicsManager;
 import org.patientview.radar.service.PatientManager;
 import org.patientview.radar.service.UserManager;
 import org.patientview.radar.util.RadarUtility;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 
 /**
  * User: james@solidstategroup.com
  * Date: 06/11/13
  * Time: 15:40
  */
 public class SelectPatientPanel extends Panel {
 
 
     private final static Logger LOGGER = LoggerFactory.getLogger(SelectPatientPanel.class);
 
     @SpringBean
     private DemographicsManager demographicsManager;
 
     @SpringBean
     private UserManager userManager;
 
     @SpringBean
     private PatientManager patientManager;
 
     private IModel<List<Patient>> model;
     private AddPatientModel patientModel;
 
     public SelectPatientPanel(String id,  IModel<List<Patient>> model) {
         super(id);
         this.model = model;
         init();
     }
 
 
     private void init() {
 
         add(new FeedbackPanel("patientSelection"));
         // Create the form that select patients to link with
         add(createSelectionForm());
 
     }
 
 
     private Form createSelectionForm() {
 
         // Form that displays the potential patient records to link with from the Patient table
 
         final IModel<Patient> selected = new Model<Patient>();
 
 
         final RadioGroup group = new RadioGroup("group", selected);
 
 
         Form<?> form = new Form<Patient>("patientSelectionForm") {
             @Override
             protected void onSubmit()  {
 
                 Patient sourcePatient = (Patient) group.getDefaultModelObject();
                 // Requery the patient object to include all the fields from the patient table
                 try {
                     Patient linkedPatient = patientManager.createLinkPatient(
                             patientManager.getById(sourcePatient.getId()));
 
                     linkedPatient.setDiseaseGroup(patientModel.getDiseaseGroup());
 
                     setResponsePage(RadarUtility.getDiseasePage(linkedPatient, this.getPage().getPageParameters()));
 
                 } catch (PatientLinkException ple)  {
                     LOGGER.error("Error creating a link patient", ple);
                 }
 
 
             }
         };
 
         // submit link
         AjaxSubmitLink submit = new AjaxSubmitLink("submit") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
 
             }
         };
 
 
         // Construct a radio button and patient record with the nhs number
         group.add(new ListView<Patient>("choice", model) {
 
             private boolean setSelected = false;
 
             protected void populateItem(ListItem<Patient> it) {
 
 
                 Patient patient = it.getModelObject();
 
                 if (setSelected == false) {
                     selected.setObject(patient);
                     setSelected = true;
                 }
 
 
                 it.add(new Radio("radio", it.getModel()));
                 it.add(new Label("id", Long.toString(patient.getId())));
                 it.add(new Label("nhsNo", patient.getNhsno()));
                 it.add(new Label("forename", patient.getForename()));
                 it.add(new Label("surname", patient.getSurname()));
                 it.add(new Label("dateOfBirth", patient.getDateofbirth()));
                it.add(new Label("unitCode", patient.getUnitcode()));
                 String dateResultsLastReceivedLabel = "";
                 if (it.getModelObject().getMostRecentTestResultDateRangeStopDate() != null) {
                     dateResultsLastReceivedLabel
                             = it.getModelObject().getMostRecentTestResultDateRangeStopDate().toString();
                 }
                 it.add(new Label("dateResultsLastReceived", dateResultsLastReceivedLabel));
 
                 this.setStartIndex(0);
             }
         });
 
         form.add(submit);
         form.add(group);
 
         return form;
 
     }
 
 
     public void setPatientModel(AddPatientModel patientModel) {
         this.patientModel = patientModel;
     }
 }
