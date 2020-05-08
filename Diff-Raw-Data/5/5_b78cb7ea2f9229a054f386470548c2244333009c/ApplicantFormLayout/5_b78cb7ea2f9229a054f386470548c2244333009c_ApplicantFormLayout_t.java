 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vaadinForm;
 
 import com.google.gwt.i18n.server.testing.Gender;
 import com.vaadin.data.fieldgroup.BeanFieldGroup;
 import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.server.UserError;
 import com.vaadin.shared.ui.MarginInfo;
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.OptionGroup;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * Layout containing a form for applicant data. Including a heading and an
  * error field.
  * @author Juho
  */
 public class ApplicantFormLayout extends VerticalLayout {
     
     private Applicant applicant;
     private BeanFieldGroup fieldGroup;
     private FormLayout form;
     
     private Button button;
     private Label errorField;
     private TextField firstNameField;
     private TextField lastNameField;
     
     private final int maxTextFieldLength = 50;
     private final int maxTextAreaLength = 2500;
     
     /**
      * Creates a new ApplicantFormLayout.
      * @param headingText Text for a heading 1 element.
      */
     public ApplicantFormLayout(String headingText){
         
         this.setDefaultComponentAlignment(Alignment.TOP_CENTER);
         
         Label heading = new Label(headingText);
         heading.setStyleName("h1");
         this.addComponent(heading);
         
         applicant = new Applicant();
         BeanItem item = new BeanItem(applicant);
         
         fieldGroup = new BeanFieldGroup<Applicant>(Applicant.class);
         fieldGroup.setItemDataSource(item);
         
         form = new FormLayout();
        this.addComponent(form);
        this.setComponentAlignment(form, Alignment.TOP_CENTER);
         form.setDefaultComponentAlignment(Alignment.TOP_CENTER);
         form.setMargin(new MarginInfo(false, true, false, true));
         form.setWidth("50%");
         
         firstNameField = getBuildAndBindTextField(fieldGroup, "First name", 
                 "firstName", "First name is missing", maxTextFieldLength);     
         
         lastNameField = getBuildAndBindTextField(fieldGroup, "Last name", 
                 "lastName", "Last name is missing", maxTextFieldLength);
         
         OptionGroup genderOption = fieldGroup.buildAndBind("Gender", "gender", OptionGroup.class);
         genderOption.removeItem(Gender.UNKNOWN);
         genderOption.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
         genderOption.setItemCaption(Gender.MALE, "Male");
         genderOption.setItemCaption(Gender.FEMALE, "Female");
         
         TextArea argumentsArea = fieldGroup.buildAndBind("Why are you applying for this job?", "arguments", TextArea.class);
         argumentsArea.setNullRepresentation("");
         argumentsArea.setMaxLength(maxTextAreaLength);
         
         form.addComponent(firstNameField);
         form.addComponent(lastNameField);
         form.addComponent(genderOption);
         form.addComponent(argumentsArea);
         
         button = new Button("Send");
         form.addComponent(button);
         
         errorField = new Label();
         errorField.setVisible(false);
         errorField.addStyleName("errorlabel");
         form.addComponent(errorField);
     }
     
     /**
      * Function to create required text fields binding them to BeanFieldGroup.
      * @param fg BeanFieldGroup where data is bind.
      * @param caption The caption for the field.
      * @param propertyId The id of the property to bind to the field.
      * @param requiredErrorMessage Error message to the required field.
      * @param maxLength Maximum text length of the field.
      * @return Returns the created text field.
      */
     private static TextField getBuildAndBindTextField(BeanFieldGroup fg, 
             String caption, String propertyId, String requiredErrorMessage, int maxLength){
         TextField field = fg.buildAndBind(caption, propertyId, TextField.class);
         field.setNullRepresentation("");
         field.setRequiredError(requiredErrorMessage);
         field.setRequired(true);
         field.setMaxLength(maxLength);
         field.setValidationVisible(false);
         return field;
     }
     
     public Applicant getApplicant(){
         return this.applicant;
     }
     
     public Button getSendButton(){
         return this.button;
     }
     
     public void commit() throws CommitException{
         fieldGroup.commit();
     }
     
     /**
      * Sets an error to the layout.
      * @param error The error message.
      */
     public void setError(String error){
         errorField.setValue(error);
         errorField.setVisible(true);
         
         if(firstNameField.getValue() == null){
             firstNameField.setComponentError(new UserError(firstNameField.getRequiredError()));
         }
         if(lastNameField.getValue() == null){
             lastNameField.setComponentError(new UserError(lastNameField.getRequiredError()));
         }
     }
     
     /**
      * Clears errors from the layout.
      */
     public void clearErrors(){
         errorField.setValue(null);
         errorField.setVisible(false);
         
         firstNameField.setComponentError(null);
         lastNameField.setComponentError(null);
         button.setComponentError(null);
     }
     
 }
