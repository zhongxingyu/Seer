 /*******************************************************************************
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  ******************************************************************************/
 package org.spiffyui.spiffyforms.client;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.spiffyui.client.JSUtil;
 import org.spiffyui.client.MainFooter;
 import org.spiffyui.client.MainHeader;
 import org.spiffyui.client.MessageUtil;
 import org.spiffyui.client.rest.RESTException;
 import org.spiffyui.client.rest.RESTObjectCallBack;
 import org.spiffyui.client.widgets.DatePickerTextBox;
 import org.spiffyui.client.widgets.FormFeedback;
 import org.spiffyui.client.widgets.button.FancyButton;
 import org.spiffyui.client.widgets.button.FancySaveButton;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.TextBoxBase;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * This class is the main entry point for our GWT module.
  */
 public class Index implements EntryPoint, ClickHandler, KeyPressHandler, KeyUpHandler 
 {
 
     private static final String WIDE_TEXT_FIELD = "wideTextField";
     private static final SpiffyUiHtml STRINGS = (SpiffyUiHtml) GWT.create(SpiffyUiHtml.class);
 
     private static Index g_index;
     private HTMLPanel m_panel;
     
     private TextBox m_userId;
     private FormFeedback m_userIdFeedback;
 
     private TextBox m_firstName;
     private FormFeedback m_firstNameFeedback;
 
     private TextBox m_lastName;
     private FormFeedback m_lastNameFeedback;
 
     private TextBox m_email;
     private FormFeedback m_emailFeedback;
 
     private TextBox m_password;
     private FormFeedback m_passwordFeedback;
 
     private TextBox m_passwordRepeat;
     private FormFeedback m_passwordRepeatFeedback;
 
     private DatePickerTextBox m_bDay;
     private FormFeedback m_bDayFeedback;
     
     private RadioButton m_male;
     private RadioButton m_female;
 
     private TextArea m_userDesc;
     private FormFeedback m_userDescFeedback;
 
     private FancyButton m_save;
     private FancyButton m_del;
     
     private Timer m_timer;
 
     private List<FormFeedback> m_feedbacks = new ArrayList<FormFeedback>();
     
     private Map<String, Anchor> m_anchors = new HashMap<String, Anchor>();
     
     private User m_currentUser;
 
     /**
      * The Index page constructor
      */
     public Index()
     {
         g_index = this;
     }
 
 
     @Override
     public void onModuleLoad()
     {
         /*
          This is where we load our module and create our dynamic controls.  The MainHeader
          displays our title bar at the top of our page.
          */
         MainHeader header = new MainHeader();
         header.setHeaderTitle("SpiffyForms Sample App");
         
         /*
          The main footer shows our message at the bottom of the page.
          */
         MainFooter footer = new MainFooter();
         footer.setFooterString("SpiffyForms was built with the <a href=\"http://www.spiffyui.org\">Spiffy UI Framework</a>");
 
         getUsers();
         
         buildFormUI();
         
         Anchor newUser = new Anchor("add user", "#");
         newUser.getElement().setId("newUserLink");
         newUser.addClickHandler(new ClickHandler()
             {
                 @Override
                 public void onClick(ClickEvent event)
                 {
                     event.preventDefault();
                     showUser(new User());
                     m_userId.setFocus(true);
                 }
             });
         m_panel.add(newUser, "userListTitle");
 
         
     }
     
     private void buildFormUI()
     {
         /*
          This HTMLPanel holds most of our content.
          MainPanel_html was built in the HTMLProps task from MainPanel.html, which allows you to use large passages of html
          without having to string escape them.
          */
         m_panel = new HTMLPanel(STRINGS.MainPanel_html());
         RootPanel.get("mainContent").add(m_panel);
         
         /*
          User ID
          */
         m_userId = new TextBox();
         m_userId.addKeyUpHandler(this);
         m_userId.getElement().setId("userIdTxt");
         m_userId.getElement().addClassName(WIDE_TEXT_FIELD);
         m_panel.add(m_userId, "userId");
 
         m_userIdFeedback = new FormFeedback();
         m_feedbacks.add(m_userIdFeedback);
         m_panel.add(m_userIdFeedback, "userIdRow");
         
         /*
          First name
          */
         m_firstName = new TextBox();
         m_firstName.addKeyUpHandler(this);
         m_firstName.getElement().setId("firstNameTxt");
         m_firstName.getElement().addClassName(WIDE_TEXT_FIELD);
         m_firstName.getElement().setAttribute("autofocus", "true");
         m_panel.add(m_firstName, "firstName");
 
         m_firstNameFeedback = new FormFeedback();
         m_feedbacks.add(m_firstNameFeedback);
         m_panel.add(m_firstNameFeedback, "firstNameRow");
 
         /*
          Last name
          */
         m_lastName = new TextBox();
         m_lastName.addKeyUpHandler(this);
         m_lastName.getElement().setId("lastNameTxt");
         m_lastName.getElement().addClassName(WIDE_TEXT_FIELD);
         m_panel.add(m_lastName, "lastName");
 
         m_lastNameFeedback = new FormFeedback();
         m_feedbacks.add(m_lastNameFeedback);
         m_panel.add(m_lastNameFeedback, "lastNameRow");
 
         /*
          email
          */
         m_email = new TextBox();
         m_email.addKeyUpHandler(this);
         m_email.getElement().setId("emailTxt");
         m_email.getElement().addClassName(WIDE_TEXT_FIELD);
         m_panel.add(m_email, "email");
 
         m_emailFeedback = new FormFeedback();
         m_feedbacks.add(m_emailFeedback);
         m_panel.add(m_emailFeedback, "emailRow");
 
         /*
          User's birthdate
          */
         m_bDay = new DatePickerTextBox("userBdayTxt");
         m_bDay.setMaximumDate(new Date()); //user cannot be born tomorrow
         m_bDay.addKeyUpHandler(this);
         m_bDay.getElement().addClassName("slimTextField");
         m_panel.add(m_bDay, "userBday");
 
         m_bDayFeedback = new FormFeedback();
         m_panel.add(m_bDayFeedback, "userBdayRow");
 
         /*
          User's gender
          */
         m_female = new RadioButton("userGender", "Female");
         m_panel.add(m_female, "userGender");
 
         m_male = new RadioButton("userGender", "Male");
         m_male.addStyleName("radioOption");
         m_male.setValue(true);
         m_male.getElement().setId("userMale");
         m_panel.add(m_male, "userGender");
 
         /*
          User description
          */
         m_userDesc = new TextArea();
         m_userDesc.addKeyUpHandler(this);
         m_userDesc.getElement().setId("userDescTxt");
         m_userDesc.getElement().addClassName(WIDE_TEXT_FIELD);
         m_userDesc.getElement().setAttribute("placeholder", "Tell us a little about yourself.");
         m_panel.add(m_userDesc, "userDesc");
 
         m_userDescFeedback = new FormFeedback();
         m_feedbacks.add(m_userDescFeedback);
         m_panel.add(m_userDescFeedback, "userDescRow");
 
         /*
          Password
          */
         m_password = new PasswordTextBox();
         m_password.addKeyUpHandler(this);
         m_password.getElement().setId("passwordTxt");
         m_password.getElement().addClassName("slimTextField");
         m_panel.add(m_password, "password");
 
         m_passwordFeedback = new FormFeedback();
         m_feedbacks.add(m_passwordFeedback);
         m_panel.add(m_passwordFeedback, "passwordRow");
 
         /*
          Password repeat
          */
         m_passwordRepeat = new PasswordTextBox();
         m_passwordRepeat.addKeyUpHandler(this);
         m_passwordRepeat.getElement().setId("passwordRepeatTxt");
         m_passwordRepeat.getElement().addClassName("slimTextField");
         m_panel.add(m_passwordRepeat, "passwordRepeat");
 
         m_passwordRepeatFeedback = new FormFeedback();
         m_feedbacks.add(m_passwordRepeatFeedback);
         m_panel.add(m_passwordRepeatFeedback, "passwordRepeatRow");
 
 
 
         /*
          The big save button
          */
         m_save = new FancySaveButton("Save");
         m_save.addClickHandler(new ClickHandler() {
                 public void onClick(ClickEvent event)
                 {
                    save();
                 }
             });
 
         m_panel.add(m_save, "buttons");
         
         /*
          The delete button
          */
         m_del = new DeleteButton("Delete");
         m_del.addClickHandler(new ClickHandler() {
                 public void onClick(ClickEvent event)
                 {
                    delete();
                 }
             });
 
         m_panel.add(m_del, "buttons");
         
         updateFormStatus(null);
     }
     
     // RESTObjectCallBack is a class provided by SpiffyUI.
     //  A class calling the RESTObjectCallBack only has
     //  to deal with well formed Java objects in GWT and doesn't need to parse JSON
     // or handle HTTP errors.
     private void getUsers()
     {
         User.getUsers(new RESTObjectCallBack<User[]>() {
                 public void success(User[] users)
                 {
                     showUsers(users);
                 }
     
                 public void error(String message)
                 {
                     MessageUtil.showFatalError(message);
                 }
     
                 public void error(RESTException e)
                 {
                     MessageUtil.showFatalError(e.getReason());
                 }
             });
     }
     
     /**
      * Shows the list of users.  
      * 
      * We're building our list of users as a simple set of DIVs with style to make them easy
      * to work with in our simple table.  We could use a table or another GWT widget for this,
      * but DIV tags are fine for our simple example.
      * 
      * @param users  the users to display
      */
     private void showUsers(User users[])
     {
         if (m_currentUser != null) {
             showUser(m_currentUser);
         } else if (users.length > 0) {
             showUser(users[0]);
         }
         
         for (String id : m_anchors.keySet()) {
             m_anchors.get(id).removeFromParent();
         }
         m_anchors.clear();
         
         StringBuffer userHTML = new StringBuffer();
         
         userHTML.append("<div class=\"gridlist\">");
         
         for (int i = 0; i < users.length; i++) {
             User u = users[i];
             if (i % 2 == 0) {
                 userHTML.append("<div class=\"gridlistitem evenrow\">");
             } else {
                 userHTML.append("<div class=\"gridlistitem oddrow\">");
             }
             
             String id = HTMLPanel.createUniqueId();
             
             /*
              The user id
              */
             userHTML.append("<div id=\"" + id + "\" class=\"useridcol\"></div>");
             
             /*
              The user's name
              */
             userHTML.append("<div class=\"userfullnamecol\">" + u.getFirstName() + " " + u.getLastName() + "</div>");
             
             /*
              The email address
              */
             userHTML.append("<div class=\"useremailcol\">" + u.getEmail() + "</div>");
             
             userHTML.append("</div>");
             
             Anchor a = new Anchor(u.getUserId(), "#");
             a.getElement().setPropertyObject("user", u);
             a.addClickHandler(this);
             m_anchors.put(id, a);
         }
         
         m_panel.getElementById("userListGrid").setInnerHTML(userHTML.toString());
         
         /*
          Now that we've added the elements to the DOM we can add the
          anchors
          */
         for (String id : m_anchors.keySet()) {
             m_panel.add(m_anchors.get(id), id);
         }
     }
     
     @Override
     public void onClick(ClickEvent event)
     {
         event.preventDefault();
         
         if (event.getSource() instanceof Anchor) {
             showUser((User) ((Anchor) event.getSource()).getElement().getPropertyObject("user"));
         }
     }
     
     /**
      * Show the specified user.
      * 
      * This method clears out the user form, resets the validation widgets, and populates
      * the form with the data about the specified user.
      * 
      * @param user   the user to show
      */
     private void showUser(User user)
     {
         m_currentUser = user;
         
         m_userId.setText(user.getUserId());
         m_firstName.setText(user.getFirstName());
         m_lastName.setText(user.getLastName());
         m_email.setText(user.getEmail());
         m_password.setText(user.getPassword());
         m_passwordRepeat.setText(user.getPassword());
         
         if (user.getBirthday() != null) {
             m_bDay.setDateValue(user.getBirthday());
         } else {
             m_bDay.setText("");
         }
         
         m_userDesc.setText(user.getUserDesc());
         
        m_male.setValue(user.getGender().equals("male"));
        m_female.setValue(user.getGender().equals("female"));
         
         for (FormFeedback f : m_feedbacks) {
             f.setText("");
             if (m_currentUser.isNew()) {
                 f.setStatus(FormFeedback.NONE);
             } else {
                 f.setStatus(FormFeedback.VALID);
             }
         }
         
         updateFormStatus(null);
         m_del.setEnabled(!m_currentUser.isNew());
         m_userId.setEnabled(m_currentUser.isNew());
         
         if (m_currentUser.isNew()) {
             m_panel.getElementById("userDetailsTitle").setInnerText("User Details - New User");
         } else {
             m_panel.getElementById("userDetailsTitle").setInnerText("User Details - " + user.getUserId());
         }
     }
 
     @Override
     public void onKeyPress(KeyPressEvent event)
     {
         
     }
 
     @Override
     public void onKeyUp(KeyUpEvent event)
     {
         if (event.getNativeKeyCode() != KeyCodes.KEY_TAB) {
             updateFormStatus((Widget) event.getSource());
         }
     }
     
     private void save()
     {
         if (m_currentUser == null) {
             MessageUtil.showWarning("No user selected to save.", false);
             return;
         }
         
         m_save.setInProgress(true);
         
         m_currentUser.setUserId(m_userId.getText());
         m_currentUser.setFirstName(m_firstName.getText());
         m_currentUser.setLastName(m_lastName.getText());
         m_currentUser.setEmail(m_email.getText());
         m_currentUser.setPassword(m_password.getText());
         m_currentUser.setBirthday(m_bDay.getDateValue());
         m_currentUser.setUserDesc(m_userDesc.getText());
         
         if (m_male.isChecked()) {
             m_currentUser.setGender("male");
         } else {
             m_currentUser.setGender("female");
         }
         
         m_currentUser.save(new RESTObjectCallBack<Boolean>() {
                 public void success(Boolean b)
                 {
                     MessageUtil.showMessage("The user was saved successfully");
                     getUsers();
                     m_save.setInProgress(false);
                 }
     
                 public void error(String message)
                 {
                     MessageUtil.showFatalError(message);
                     m_save.setInProgress(false);
                 }
     
                 public void error(RESTException e)
                 {
                     MessageUtil.showFatalError(e.getReason());
                     m_save.setInProgress(false);
                 }
             });
     }
     
     private void delete()
     {
         if (m_currentUser == null) {
             MessageUtil.showWarning("No user selected to delete.", false);
             return;
         }
         
         m_del.setInProgress(true);
         m_currentUser = null;
         
         m_currentUser.delete(new RESTObjectCallBack<Boolean>() {
                 public void success(Boolean b)
                 {
                     MessageUtil.showMessage("The user was deleted");
                     getUsers();
                     m_del.setInProgress(false);
                 }
     
                 public void error(String message)
                 {
                     MessageUtil.showFatalError(message);
                     m_del.setInProgress(false);
                 }
     
                 public void error(RESTException e)
                 {
                     MessageUtil.showFatalError(e.getReason());
                     m_del.setInProgress(false);
                 }
             });
     }
     
     /**
      * Validate that the specified field is filled in and valid.
      * 
      * @param tb        the field to validate
      * @param minLength the minimum character length of the field
      * @param feedback  the feedback control for this field
      * @param error     the error to show in the feedback if the field isn't valid
      */
     private void validateField(TextBoxBase tb, int minLength, FormFeedback feedback, String error)
     {
         if (tb.getText().length() > minLength) {
             feedback.setStatus(FormFeedback.VALID);
             feedback.setTitle("");
         } else {
             feedback.setStatus(FormFeedback.WARNING);
             feedback.setTitle(error);
         }
     }
     
     /**
      * Validate the username in this form.  This method uses a timer and makes a REST call
      * to the server to validate the username after the user has stopped typing for one
      * second.
      */
     private void validateUsername()
     {
         if (m_timer != null) {
             m_timer.cancel();
         }
         
         if (m_userId.getText().length() < 2) {
             m_userIdFeedback.setStatus(FormFeedback.WARNING);
             m_userIdFeedback.setTitle("Username must be more than two characters");
             return;
         }
         
         m_userIdFeedback.setText("");
         m_userIdFeedback.setStatus(FormFeedback.LOADING);
         
         m_timer = new Timer() 
             {
                 @Override
                 public void run()
                 {
                     runUserValidation();
                 }
             };
         
         m_timer.schedule(1000);
     }
     
     /**
      * Call the usernames REST endpoint and verify that this username is available.
      */
     private void runUserValidation()
     {
         m_userId.setEnabled(false);
         User.isUsernameInUse(new RESTObjectCallBack<Boolean>() {
                 public void success(Boolean b)
                 {
                     if (b.booleanValue()) {
                         m_userIdFeedback.setStatus(FormFeedback.ERROR);
                         m_userIdFeedback.setText("This username is already in use");
                         m_userId.setTitle("This username is already in use");
                         m_save.setEnabled(false);
                     } else {
                         m_userIdFeedback.setStatus(FormFeedback.VALID);
                         m_userIdFeedback.setText("This username is available");
                         m_userId.setTitle("");
                     }
                     
                     m_userId.setEnabled(true);
                 }
     
                 public void error(String message)
                 {
                     MessageUtil.showFatalError(message);
                     m_userIdFeedback.setStatus(FormFeedback.ERROR);
                     m_save.setEnabled(false);
                     m_userId.setEnabled(true);
                 }
     
                 public void error(RESTException e)
                 {
                     MessageUtil.showFatalError(e.getReason());
                     m_userIdFeedback.setStatus(FormFeedback.ERROR);
                     m_userId.setEnabled(true);
                     m_save.setEnabled(false);
                 }
             }, m_userId.getText());
     }
     
     /**
      * Enable or disable the save button based on the state of the fields.
      */
     private void enableSaveButton()
     {
         /*
          * We only want to enable the save button if every field is valid
          */
         for (FormFeedback feedback : m_feedbacks) {
             if (feedback.getStatus() != FormFeedback.VALID) {
                 m_save.setEnabled(false);
                 return;
             }
         }
 
         m_save.setEnabled(true);
     }
 
     /**
      * Validate the second password field.
      */
     private void validatePasswordRepeat()
     {
         validateField(m_passwordRepeat, 2, m_passwordRepeatFeedback, "Your passwords don't match");
         if (m_passwordRepeat.getText().equals(m_password.getText())) {
             m_passwordRepeatFeedback.setStatus(FormFeedback.VALID);
             m_passwordRepeatFeedback.setText("");
             m_passwordRepeatFeedback.setTitle("");
         } else {
             m_passwordRepeatFeedback.setStatus(FormFeedback.ERROR);
             m_passwordRepeatFeedback.setText("Your passwords don't match");
             m_passwordRepeatFeedback.setTitle("Your passwords don't match");
         }
     }
     
     /**
      *  Update the status of our form.  This method handles field validation and enabling
      *  the save button.  This method is called when the user makes any change to the form.
      *  
      * When the user types in the first field we want to validate that field, but we don't
      * want to validate the rest of them since the rest aren't filled in yet and we don't
      * want to show invalid messages for fields they haven't edited yet.  This method takes
      * the widget that was edited as an argument so it can validate just that field.
      * 
      * @param w      the widget that's being changed
      */
     private void updateFormStatus(Widget w)
     {
         if (w == m_userId) {
             validateUsername();
         } else if (w == m_firstName) {
             validateField(m_firstName, 1, m_firstNameFeedback, "First name must be more than two characters");
         } else if (w == m_lastName) {
             validateField(m_lastName, 1, m_lastNameFeedback, "Last name must be more than two characters");
         } else if (w == m_email) {
             validateEmail();
         } else if (w == m_password) {
             validateField(m_password, 2, m_passwordFeedback, "Password name must be more than two characters");
         } else if (w == m_bDay) {
             //validateBirthday();
         } else if (w == m_passwordRepeat) {
             validatePasswordRepeat();
         } else if (w == m_userDesc) {
             validateField(m_userDesc, 8, m_userDescFeedback, "The user description must be more than two characters");
         }
 
         enableSaveButton();
     }
     
     /**
      * Validate that the email field is filled in with a valid email address.
      */
     private void validateEmail()
     {
         if (JSUtil.validateEmail(m_email.getText())) {
             m_emailFeedback.setStatus(FormFeedback.VALID);
             m_emailFeedback.setTitle("");
         } else {
             m_emailFeedback.setStatus(FormFeedback.ERROR);
             m_emailFeedback.setTitle("Invalid email address");
         }
     }
 }
 
 
 /**
  * This is a little class to handle our delete button.  It mostly just handles styling.
  */
 class DeleteButton extends FancyButton
 {
     public DeleteButton(String s)
     {
         super(s);
         getElement().setClassName("spiffy-del-button");
         getElement().addClassName("spiffy-fancy-button");
     }
 }
