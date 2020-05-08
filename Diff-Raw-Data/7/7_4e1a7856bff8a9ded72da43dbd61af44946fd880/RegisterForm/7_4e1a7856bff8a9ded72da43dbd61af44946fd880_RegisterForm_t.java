 package org.hackystat.projectbrowser.authentication;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.StatelessForm;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.PropertyModel;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.client.SensorBaseClientException;
 
 /**
  * The form for providing an email for registration purposes. 
  * 
  * @author Philip Johnson
  */
 class RegisterForm extends StatelessForm {
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
   /** The email. */
   private String email;
 
   /**
    * Create this form, supplying the wicket:id.
    * 
    * @param id The wicket:id.
    */
   public RegisterForm(final String id) {
     super(id);
     setModel(new CompoundPropertyModel(this));
     add(new TextField("email"));
     add(new Label("registerFeedback", 
         new PropertyModel(ProjectBrowserSession.get(), "registerFeedback")));
 
   }
   
   /**
    * Performs the registration action upon submittal.
    */
   @Override
   public void onSubmit() {
     // Make sure there's an email address supplied.
     if ((email == null) || "".equals(email)) {
       ProjectBrowserSession.get().setRegisterFeedback("No email supplied");
       return;
     }
     // Make sure the sensorbase can be contacted.
     ProjectBrowserApplication app = (ProjectBrowserApplication)getApplication();
     String sensorbase = app.getSensorBaseHost();
     if (!SensorBaseClient.isHost(sensorbase)) {
       ProjectBrowserSession.get().setRegisterFeedback(sensorbase + " not available.");
       return;
     }
    // SensorBase is available, so register using it.
    // Currently, registration takes longer than 10 seconds, so don't indicate the error. 
     String msg;
     try {
       SensorBaseClient.registerUser(sensorbase, email);
       msg = "Registration attempted. Please check your email for password information";
       ProjectBrowserSession.get().setRegisterFeedback(msg);
     }
     catch (SensorBaseClientException e) {
      msg = "Registration attempted. Please check your email for password information";
      //msg = "Registration failed: " + e.getMessage() + " Contact your Hackystat admin.";
       ProjectBrowserSession.get().setRegisterFeedback(msg);
     }
   }
 
   /**
    * Returns the user.
    * 
    * @return The user.
    */
   public String getEmail() {
     return email;
   }
 
   /**
    * Sets the user. 
    * @param email The user email.
    */
   public void setEmail(String email) {
     this.email = email;
   }
 }
