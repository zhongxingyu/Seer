 /**
  * 
  */
 package pl.dmcs.whatsupdoc.client.providers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 import pl.dmcs.whatsupdoc.client.ContentManager;
 import pl.dmcs.whatsupdoc.client.fields.ListItemField;
 import pl.dmcs.whatsupdoc.client.model.Recognition;
 import pl.dmcs.whatsupdoc.client.model.User;
 import pl.dmcs.whatsupdoc.client.services.AuthenticationService;
 import pl.dmcs.whatsupdoc.client.services.AuthenticationServiceAsync;
 import pl.dmcs.whatsupdoc.client.services.TreatmentService;
 import pl.dmcs.whatsupdoc.client.services.TreatmentServiceAsync;
 import pl.dmcs.whatsupdoc.shared.FormStatus;
 import pl.dmcs.whatsupdoc.shared.UserType;
 
 /**
  * 17-11-2012
  * @author Jakub Jeleński, jjelenski90@gmail.com
  * 
  * 
  */
 public class PatientRecognitionsProvider extends BodyProvider {
 	private Logger logger = Logger.getLogger("PatientRecognitionsProvider");
 	
 	private String patientKey;
 	private List<ListItemField> items;
 	private Label timeLabel, doctorLabel, sicknessLabel;
 	private Button detailsButton, editForm;
 	private UserType user;
 	private List<String> rowsCSS;
 	
 	
 	/**
 	 * @param cm - ContentManager of given BodyProvider
 	 * @param key - PatientKey
 	 */
 	public PatientRecognitionsProvider(ContentManager cm, String key) {
 		super(cm);
 		this.drawWaitContent();
 		final TreatmentServiceAsync userService = GWT.create(TreatmentService.class);
 		final AuthenticationServiceAsync auth = GWT.create(AuthenticationService.class);
 		
 		rowsCSS = Arrays.asList(new String[]{"even", "odd"});
 		
 		auth.getCurrentLoggedInUser(new AsyncCallback<User>() {
 			
 			@Override
 			public void onSuccess(User result) {
 				if(result!=null){
 					user = result.getUserType();
 				}
 				
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				logger.log(Level.SEVERE, "Exception while getCurrentLoggedInUser()");
 				
 			}
 		});
 		
 		Label timeColumn = new Label("Data rozpoznania");
 		timeColumn.setStyleName("time");
 		Label doctorColumn = new Label("Imię i nazwisko prowadzącego");
 		doctorColumn.setStyleName("doctor");
 		Label diseaseColumn = new Label("Choroba");
 		diseaseColumn.setStyleName("disease");
 		
 		ListItemField item = new ListItemField(Arrays.asList(new Widget[]{timeColumn, doctorColumn, diseaseColumn}), 
 				Arrays.asList(new String[] {"timeColumn", "doctorColumn", "diseaseColumn"}));
 		mainPanel.add(item.returnContent());
 		
 		items = new ArrayList<ListItemField>();
 		patientKey = key;
 		if(key!=null){
 			userService.getRecognitions(patientKey, new AsyncCallback<ArrayList<Recognition>>() {
 				
 				@Override
 				public void onSuccess(ArrayList<Recognition> result) {
 					if(result!=null){
						
 						int counter = 0;
 						for(Recognition recognition : result){
							List<String> css = new ArrayList<String>(Arrays.asList(new String[] {"timeDiv", "personDiv", "diseaseDiv", "buttonDiv"}));
 							ArrayList<Widget> widgets = new ArrayList<Widget>();
 							
 							timeLabel = new Label(recognition.getDate().toString()); // In future we should use some date format with user local time
 							timeLabel.setStyleName("date");
 							doctorLabel = new Label(recognition.getDoctorName());
 							doctorLabel.setStyleName("personName");
 							sicknessLabel = new Label(recognition.getDisease().name());
 							sicknessLabel.setStyleName("diseaseName");
 							detailsButton = new Button("Szczegóły");
 							detailsButton.setStyleName("button");
 							detailsButton.addClickHandler(new RecognitionDetailHandler(recognition.getRecognitionKeyString()));
 							
 							
 							widgets.add(timeLabel);
 							widgets.add(doctorLabel);
 							widgets.add(sicknessLabel);
 							if(UserType.PATIENT.equals(user)){
 								editForm = new Button("Edytuj formularz");
 								editForm.setStyleName("button");
 								if( FormStatus.NOT_APPROVED.equals(recognition.getStatusForm()) ){
 									editForm.setEnabled(true);
 								}else{
 									editForm.setEnabled(false);
 								}
 								editForm.addClickHandler(new FormEdit(recognition));
 								widgets.add(editForm);
 								css.add("buttonDiv");
 							}
 							widgets.add(detailsButton);
 							
 							ListItemField lF = new ListItemField(widgets, css);
 							lF.setMainCSS(rowsCSS.get(counter%2));
 							counter++;
 							items.add(lF);
 						}
 						
 						for(ListItemField i : items){
 							mainPanel.add(i.returnContent());
 						}
 					}
 					
 				}
 				
 				@Override
 				public void onFailure(Throwable caught) {
 					logger.log(Level.SEVERE,"Exception while get Recognition");
 					
 				}
 			});
 		}
 		
 		
 		
 		
 	}
 
 	class FormEdit implements ClickHandler{
 		private Recognition recognition;
 		
 		
 		/**
 		 * @param recognition - Recognition
 		 */
 		public FormEdit(Recognition recognition) {
 			this.recognition = recognition;
 		}
 		
 		/* (non-Javadoc)
 		 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
 		 */
 		@Override
 		public void onClick(ClickEvent event) {
 			if(FormStatus.NOT_APPROVED.equals(recognition.getStatusForm()) && UserType.PATIENT.equals(user)){
 				BodyProvider b = new QuestionnaireProvider(getCm(), recognition.getRecognitionKeyString());
 				getCm().setBody(b);
 				getCm().drawContent();
 			}
 			
 		}
 		
 	}
 	
 	class RecognitionDetailHandler implements ClickHandler{
 		
 		private String recognitionKey;
 		
 		/**
 		 * @param recognitionKey - string unique to given recognition
 		 */
 		public RecognitionDetailHandler(String recognitionKey) {
 			this.recognitionKey = recognitionKey;
 		}
 
 		/* (non-Javadoc)
 		 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
 		 */
 		@Override
 		public void onClick(ClickEvent event) {
 			BodyProvider body = new RecognitionsDetailsProvider(getCm(), recognitionKey);
 			getCm().setBody(body);
 			getCm().getBreadcrumb().addField(true, "Rozpoznanie", body);
 			getCm().drawContent();
 			
 		}
 		
 	}
 
 }
