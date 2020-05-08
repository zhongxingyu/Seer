 package org.jsc.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTMLTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A screen for collecting user information to create a new account.
  * 
  * @author Matthew Jones
  */
 public class SettingsScreen extends BaseScreen {
 
     private static final String NEW_INSTRUCTIONS = "Please fill in the form below with all required fields.  After clicking 'Create Account', you will be able to sign in with your new username and password.";
     private static final String UPDATE_INSTRUCTIONS = "You may update your account settings, including changing your password.  Any fields left unchanged (or blank in the case of the password fields) will remain unchanged when you Save.";
     private HorizontalPanel screen;
     private Label instructions;
     
     private TextBox fnameField;
     private TextBox mnameField;
     private TextBox lnameField;
     private TextBox emailField;
     private TextBox birthdayField;
     private TextBox homephoneField;
 
     private TextBox cellphoneField;
     private TextBox workphoneField;
     private TextBox street1Field;
     private TextBox street2Field;
     private TextBox cityField;
     private TextBox stateField;
     private TextBox zipField;
     private TextBox usfsaidField;
     private TextBox parentLastnameField;
     private TextBox parentFirstnameField;
     private TextBox parentEmailField;
 
     private TextBox usernameField;
     private PasswordTextBox password1Field;
     private PasswordTextBox password2Field;
     
     private Label membershipLabel;
     private Button accountButton;
     private Grid leftGrid;
     private Grid rightGrid;
     
     // TODO: move regService to a separate class, one instance for the client
     private SkaterRegistrationServiceAsync regService;
 
     
     /**
      * Construct the screen.
      *
      */
     public SettingsScreen(LoginSession loginSession, HandlerManager eventBus) {
         super(loginSession, eventBus);
         layoutScreen();
         this.setContentPanel(screen);
         regService = GWT.create(SkaterRegistrationService.class);
     }
     
     /**
      * Lay out the user interface widgets on the screen.
      */
     private void layoutScreen() {
         this.setScreenTitle("Account Details");
         this.setStyleName("jsc-onepanel-screen");
         
         screen = new HorizontalPanel();
         
         VerticalPanel accountPanel = new VerticalPanel();
         accountPanel.addStyleName("jsc-rightpanel");
                 
         instructions = new Label(" ");
         accountPanel.addStyleName("jsc-text");
         accountPanel.add(instructions);
         HorizontalPanel horizontal = new HorizontalPanel();
         accountPanel.add(horizontal);
         Label footnote1 = new Label("* Required field");
         Label footnote2 = new Label("** Required field if skater is younger than 18 yrs.");
         accountPanel.add(footnote1);
         accountPanel.add(footnote2);
 
         VerticalPanel leftVertical = new VerticalPanel();
         VerticalPanel rightVertical = new VerticalPanel();
         horizontal.add(leftVertical);
         Label spacer = new Label(" ");
         spacer.addStyleName("jsc-panel-spacer");
         horizontal.add(spacer);
         horizontal.add(rightVertical);
 
         leftGrid = new Grid(0, 2);
         layoutLeftPanel();
         leftVertical.add(leftGrid);
         
         rightGrid = new Grid(0, 2);
         layoutRightPanel();
         rightVertical.add(rightGrid);
         
         screen.add(accountPanel);
     }
 
     private void layoutLeftPanel() {
         fnameField = new TextBox();
         addToLeftGrid("Skater's First Name*:", fnameField);
         mnameField = new TextBox();
         addToLeftGrid("Skater's Middle Name:", mnameField);
         lnameField = new TextBox();
         addToLeftGrid("Skater's Last Name*:", lnameField);
         emailField = new TextBox();
         addToLeftGrid("Contact Email*:", emailField);
         birthdayField = new TextBox();
         addToLeftGrid("Skater's Birthdate*:", birthdayField);
         
         addToLeftGrid(" ", new Label(" "));
         addToLeftGrid(" ", new Label("Account information:"));
         usernameField = new TextBox();
         addToLeftGrid("Username*:", usernameField);
         password1Field = new PasswordTextBox();
         addToLeftGrid("Password:", password1Field);
         password2Field = new PasswordTextBox();
         addToLeftGrid("Re-type Password:", password2Field);
         membershipLabel = new Label("false");
         addToLeftGrid("Membership paid:", membershipLabel);
         
     }
     
     private void layoutRightPanel() {
         homephoneField = new TextBox();
         addToRightGrid("Home Phone*:", homephoneField);
         cellphoneField = new TextBox();
         addToRightGrid("Cell Phone:", cellphoneField);
         workphoneField = new TextBox();
         addToRightGrid("Work Phone:", workphoneField);
         street1Field = new TextBox();
         addToRightGrid("Street 1*:", street1Field);
         street2Field = new TextBox();
         addToRightGrid("Street 2:", street2Field);
         cityField = new TextBox();
         addToRightGrid("City*:", cityField);
         stateField = new TextBox();
         addToRightGrid("State*:", stateField);
         zipField = new TextBox();
         addToRightGrid("Zip*:", zipField);
 
         parentFirstnameField = new TextBox();
         addToRightGrid("Parent First Name**:", parentFirstnameField);
         parentLastnameField = new TextBox();
         addToRightGrid("Parent Last Name**:", parentLastnameField);
         parentEmailField = new TextBox();
         addToRightGrid("Parent Email**:", parentEmailField);
         
         addToRightGrid(" ", new Label(" "));
 
         accountButton = new Button("Create Account");
         accountButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 createAccount();
             }
         });
         addToRightGrid(" ", accountButton);
 
     }
     
     private void addToLeftGrid(String label, Widget widget) {
         int newRow = leftGrid.insertRow(leftGrid.getRowCount());
         leftGrid.setWidget(newRow, 0, new Label(label));
         leftGrid.setWidget(newRow, 1, widget);
         HTMLTable.CellFormatter fmt = leftGrid.getCellFormatter();
         fmt.addStyleName(newRow, 0,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 1,  "jsc-field");
     }
     
     private void addToRightGrid(String label, Widget widget) {
         int newRow = rightGrid.insertRow(rightGrid.getRowCount());
         rightGrid.setWidget(newRow, 0, new Label(label));
         rightGrid.setWidget(newRow, 1, widget);
         HTMLTable.CellFormatter fmt = rightGrid.getCellFormatter();
         fmt.addStyleName(newRow, 0,  "jsc-fieldlabel");
         fmt.addStyleName(newRow, 1,  "jsc-field");
     }
     
     /**
      * When we switch to the settings screen, update our form to reflect our login session.
      */
     protected void updateScreen() {
         if (loginSession.isAuthenticated()) {
             populateFields();
             accountButton.setText("Save");
             instructions.setText(UPDATE_INSTRUCTIONS);
         } else {
             populateFields();
             accountButton.setText("Create Account");
             instructions.setText(NEW_INSTRUCTIONS);
         }
         //clearMessage();
     }
     
     /**
      * Initialize the fields with proper values, or clear them, as appropriate.
      */
     private void populateFields() {
         Person person;
         if (loginSession.isAuthenticated()) {
             person = loginSession.getPerson();
             fnameField.setText(person.getFname());
             mnameField.setText(person.getMname());
             lnameField.setText(person.getLname());
             emailField.setText(person.getEmail());
             birthdayField.setText(person.getBday());
             
             homephoneField.setText(person.getHomephone());
             cellphoneField.setText(person.getCellphone());
             workphoneField.setText(person.getWorkphone());
             street1Field.setText(person.getStreet1());
             street2Field.setText(person.getStreet2());
             cityField.setText(person.getCity());
             stateField.setText(person.getState());
             zipField.setText(person.getZip());
             parentFirstnameField.setText(person.getParentFirstname());
             parentLastnameField.setText(person.getParentLastname());
             parentEmailField.setText(person.getParentEmail());
             
             usernameField.setText(person.getUsername());
             password1Field.setText("");
             password2Field.setText("");
             membershipLabel.setText(Boolean.toString(person.isMember()));
 
         } else {
             fnameField.setText("");
             mnameField.setText("");
             lnameField.setText("");
             emailField.setText("");
             birthdayField.setText("");
             
             homephoneField.setText("");
             cellphoneField.setText("");
             workphoneField.setText("");
             street1Field.setText("");
             street2Field.setText("");
             cityField.setText("");
             stateField.setText("");
             zipField.setText("");
             parentFirstnameField.setText("");
             parentLastnameField.setText("");
             parentEmailField.setText("");
             
             usernameField.setText("");
             password1Field.setText("");
             password2Field.setText("");
             membershipLabel.setText("");
         }
     }
     
     /**
      * Called when the create account button is pressed, and contacts the server
      * to update the account information in the database.
      */
     private void createAccount() {
         GWT.log("Creating account...", null);
     
         // Gather information from the form
         String fname = fnameField.getText();
         String mname = mnameField.getText();
         String lname = lnameField.getText();
         String email = emailField.getText();
         String birthday = birthdayField.getText();
 
         String homephone = homephoneField.getText();
         String cellphone = cellphoneField.getText();
         String workphone = workphoneField.getText();
         String street1 = street1Field.getText();
         String street2 = street2Field.getText();
         String city = cityField.getText();
         String state = stateField.getText();
         String zip = zipField.getText();
         String parentFirstname = parentFirstnameField.getText();
         String parentLastname = parentLastnameField.getText();
         String parentEmail = parentEmailField.getText();
         
         String username = usernameField.getText();
         String pw1 = password1Field.getText();
         String pw2 = password2Field.getText();
         
         // Validate necessary input, making sure required fields are included
         boolean isValid = true;
         String[] fields = {fname, lname, email, birthday, homephone, street1, city, state, zip, username};
         if (fieldMissing( fields )) {
             isValid = false;
             setMessage("Missing required information. Please fill in all required fields.");
             return;
         }
 
         // We only need a password if its a new account or the user is
         // providing a new one; in either case, the retyped password must match
         if (!loginSession.isAuthenticated() || (pw1 != null && pw1.length() > 0)) {
             String pwfields[] = {pw1, pw2};
             if (fieldMissing(pwfields) || !pw1.equals(pw2)) {
                 isValid = false;
                 setMessage("Password missing or passwords don't match.");
                 return;
             }
         }
         
         // create Person object
         Person person = null;
         if (isValid) {
             person = new Person(fname, mname, lname);
             if (loginSession.isAuthenticated()) {
                 person.setPid(loginSession.getPerson().getPid());
                 person.setPassword(loginSession.getPerson().getPassword());
             } else {
                 // Set the PID to 0 to indicate this is an update
                 person.setPid(0);
             }
             person.setEmail(email);
             person.setBday(birthday);
             person.setHomephone(homephone);
             person.setCellphone(cellphone);
             person.setWorkphone(workphone);
             person.setStreet1(street1);
             person.setStreet2(street2);
             person.setCity(city);
             person.setState(state);
             person.setZip(zip);
             person.setParentFirstname(parentFirstname);
             person.setParentLastname(parentLastname);
             person.setParentEmail(parentEmail);
 
             if (loginSession.isAuthenticated() && !username.equals(loginSession.getPerson().getUsername())) {
                 person.setUsername(loginSession.getPerson().getUsername());
                 person.setNewUsername(username);
             } else {
                 person.setUsername(username);
             }
             if (pw1 != null) {
                 person.setNewPassword(pw1);
             }
         } else {
             GWT.log("Account NOT created: " + fname + " " + mname + " " + lname, null);
             return;
         }
         
         // Initialize the service proxy.
         if (regService == null) {
             regService = GWT.create(SkaterRegistrationService.class);
         }
 
         // Set up the callback object.
         AsyncCallback<Person> callback = new AsyncCallback<Person>() {
             public void onFailure(Throwable caught) {
                 GWT.log("Failed to create account.", null);
                 GWT.log("Got error from remote service: ", caught);
                 if (caught.getMessage().contains("duplicate key")) {
                     String uiMessage = "Username already exists. Please try a different username.";
                     GWT.log(uiMessage, null);
                     setMessage(uiMessage);
                 } else if (caught.getMessage().contains("invalid input syntax for type date") ||
                         caught.getMessage().contains("date/time field value out of range")) {
                     String uiMessage = "Wrong format for date. Please use MM-DD-YYYY.";
                     GWT.log(uiMessage, null);
                     setMessage(uiMessage);
                } else {
                    String uiMessage = "Unexpected error while creating account. Please contact registrar.";
                    GWT.log(uiMessage, null);
                    setMessage(uiMessage);
                 }
             }
 
             public void onSuccess(Person newPerson) {
                 if (newPerson == null) {
                     // Failure on the remote end.
                     setMessage("Failed to create or update account.");
                     return;
                 }
                 
                 GWT.log("Account created: " + newPerson.getPid(), null);
                 if (loginSession.isAuthenticated() &&
                         loginSession.getPerson().getPid() == newPerson.getPid()) {
                     setMessage("Settings saved.");
                     // Update the loginSession with the new Person object
                     loginSession.setPerson(newPerson);
                 } else {
                     // Change our application state to the login screen
                     setMessage("Account created. Please sign in.");
                     History.newItem("signin");
                 }
             }
         };
 
         // Make the call to the registration service.
         regService.createAccount(loginSession, person, callback);
     }
     
     /**
      * Check if each String in the array is non-null and has length > 0.
      * @param fields the array of fields to be checked
      * @return true if any field is null or zero length, otherwise false
      */
     private boolean fieldMissing(String[] fields) {
         boolean isMissing = false;
         for (String field : fields) {
             if (field == null || field.length() == 0) {
                 isMissing = true;
                 return isMissing;
             }
         }
         return isMissing;
     }
 }
