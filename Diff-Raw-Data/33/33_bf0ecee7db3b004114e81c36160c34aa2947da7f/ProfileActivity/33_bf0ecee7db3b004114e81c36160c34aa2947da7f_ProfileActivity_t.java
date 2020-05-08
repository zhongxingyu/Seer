 package com.example.bacsafe;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /**
  * Profile activity for accessing user profile information in BAC Safe.
  * 
  * @author Team BAC Safe
  */
 public class ProfileActivity extends Activity {
 
         // Keyboard Access
         InputMethodManager keyboard;
 
         // User Information Instance
         private User m_userProfile;
 
         // Declared UI Objects
         TextView titleTextView;
         Spinner ageSpinner;
         EditText usernameTextField;
         EditText firstNameTextField;
         EditText lastNameTextField;
         EditText weightTextField;
         EditText height_feet_TextField;
         EditText height_inches_TextField;
         RadioButton maleRadioButton;
         RadioButton femaleRadioButton;
         Button backButton;
         Button saveButton;
 
         //-------------------------------------------------------------------------------------------------------
         //-------------------------------------------------------------------------------------------------------
         //-------------------------------------------------------------------------------------------------------
 
         /**
          * Set up Profile Activity when called
          * 
          * @Override onCreate()
          */
         @Override
         protected void onCreate(Bundle savedInstanceState){
                 super.onCreate(savedInstanceState);
 
                 setContentView(R.layout.activity_profile); // Show the Edit Button Screen Activity
 
                 try {
 					m_userProfile = new User();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}// Retrieve User Info and Prefs through m_userProfile initialization
 
                 setupObjectVariables();// Setup UI Objects
                 setUserInfo();// Put User Info into UI Objects
 
                 if(!m_userProfile.getUserName().isEmpty()){ // If a Username has already been created, do not allow the user to change it
                         usernameTextField.setCursorVisible(false);
                         usernameTextField.setFocusable(false);
                         usernameTextField.setClickable(false);
                         usernameTextField.setGravity(0x01);
                         firstNameTextField.requestFocus(); // Set tab focus to the Name Text Box on startup
                 }
                 else // If the Username needs to be created
                 {
                         titleTextView.setText(R.string.CreateYourProfile); // Change the title of the page
                         backButton.setVisibility(8); // Hide the Back button
                         usernameTextField.requestFocus(); // Set the focus to the Username text box on startup
                 }
 
         } // onCreate()
 
 
         /**
          *  Disable the back button if a user profile DNE.
          * 
          *  @Override onBackPressed()
          */
         @Override
         public void onBackPressed() {
                 if(!m_userProfile.getUserName().isEmpty())
                 {    
                         finish();        
                 }
         } // onBackPressed()
 
 
         /**
          * Sets up the UI and contains the actions for each (i.e. Save Button)
          */
         private void setupObjectVariables(){
 
                 // Setup Text Field objects
                 titleTextView = (TextView)findViewById(R.id.label_Title);
                 usernameTextField = (EditText)findViewById(R.id.textBox_Username);
                 firstNameTextField = (EditText)findViewById(R.id.textBox_FirstName);
                 lastNameTextField = (EditText)findViewById(R.id.textBox_LastName);
                 weightTextField = (EditText)findViewById(R.id.textBox_Weight);
                 height_feet_TextField = (EditText)findViewById(R.id.textBox_Height_Feet);
                 height_inches_TextField = (EditText)findViewById(R.id.textBox_Height_Inches);
                 maleRadioButton = (RadioButton)findViewById(R.id.radioGroup_Sex_Male);
                 femaleRadioButton = (RadioButton)findViewById(R.id.radioGroup_Sex_Female);
 
                 // Setup Spinner object
                 ageSpinner = (Spinner) findViewById(R.id.spinner_Edit_Age);
                 addItemsOm_nAgeSpinner();
 
                 // Setup Button objects
                 backButton = (Button)findViewById(R.id.button_Edit_Back);
                 saveButton = (Button)findViewById(R.id.button_Edit_Save);
 
                 // Back Button - Returns to Main screen (Drink Counter)
                 backButton.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                                 Intent returnIntent = new Intent();
                                 setResult(RESULT_CANCELED, returnIntent); // Send activity result of Canceled so Main does not reload User Info/Prefs
                                 finish();
                         } 
                 }); // backButton
 
                 // Save Button - Saves new information and recalculates BAC
                 saveButton.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                        	
                        		if(m_userProfile.getUserName() != ""){
                        			
                                    // Form Validation - Required fields (Height and Weight)
                                    if(requiredFieldValidation())
                                    {        
                                            saveUserInfo();
                                    } 
                        		}
                        		else{
                        			
                        			// Form Validation - Check for unique Username
                                    if(uniqueUsernameValidation())
                                    {
                                            // Form Validation - Required fields (Height and Weight)
                                            if(requiredFieldValidation())
                                            {        
                                                    saveUserInfo();
                                            } 
                                    } 
                        		}

                                
                         }
                 }); // saveButton
 
         } // setupObjectVariables()
 
 
 
 
         /**
          *        Add ages 21-100 to Spinner menu for age selection
          */
         private void addItemsOm_nAgeSpinner() {
                 List<Integer> list = new ArrayList<Integer>();
                 for(int x=21; x<=100; x++){
                         list.add(x);
                 }
 
                 ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, list);
                 dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                 ageSpinner.setAdapter(dataAdapter);
 
         } // addItemsOm_nAgeSpinner()
 
 
         /**
          * Sets up the UI for the Profile Activity
          */
         private void setUserInfo(){
 
                 // Set UI Objects from UI Variables
                 usernameTextField.setText(m_userProfile.getUserName());
                 firstNameTextField.setText(m_userProfile.getFirstName());
                 lastNameTextField.setText(m_userProfile.getLastName());
                 maleRadioButton.setChecked(m_userProfile.isMale());
                 femaleRadioButton.setChecked(!m_userProfile.isMale());
                 ageSpinner.setSelection(m_userProfile.getAge()-21);
                 weightTextField.setText(Integer.toString(m_userProfile.getWeight()));
                 height_feet_TextField.setText(Integer.toString(m_userProfile.getHeightFeet()));
                 height_inches_TextField.setText(Integer.toString(m_userProfile.getHeightInches()));
 
         } // setUserInfo()
 
         /**
          *  Sets the user's information so that it can be saved
          */
         private void saveUserInfo(){
 
                 // Form Validation will validate these fields are not NULL.
                 m_userProfile.setUserName(usernameTextField.getText().toString());
                 m_userProfile.setFirstName(firstNameTextField.getText().toString());
                 m_userProfile.setLastName(lastNameTextField.getText().toString());
                 m_userProfile.setMale(maleRadioButton.isChecked());
                 m_userProfile.setAge(Integer.parseInt(ageSpinner.getSelectedItem().toString()));
                 m_userProfile.setWeight(Integer.parseInt(weightTextField.getText().toString()));
 
                 // If the Height Feet text field is empty, set a value of 0 instead of NULL
                 if(height_feet_TextField.getText().toString().isEmpty())
                 {
                         m_userProfile.setHeightFeet(0);
                 }
                 else 
                 {
                         m_userProfile.setHeightFeet(Integer.parseInt(height_feet_TextField.getText().toString()));
                 }
 
                 // If the Height Inches text field is empty, set a value of 0 instead of NULL
                 if(height_inches_TextField.getText().toString().isEmpty())
                 {
                         m_userProfile.setHeightInches(0);
                 }
                 else 
                 {
                         m_userProfile.setHeightInches(Integer.parseInt(height_inches_TextField.getText().toString()));
                 }
 
                 // Set Activity Result to OK so that Main will load the new information from User
                 Intent returnIntent = new Intent();
                 setResult(RESULT_OK, returnIntent); 
                 finish();        
 
         } // saveUserInfo()
 
 
         //----------------------------------------------------------------------------------------
         //-- PROFILE DATA VALIDATION -------------------------------------------------------------
         //----------------------------------------------------------------------------------------
 
         /**
          * Check database for unique Username selected by user
          * 
          * @return - True if Username is unique
          */
         private boolean uniqueUsernameValidation(){
 
                 if(usernameTextField.getText().toString().isEmpty())
                 {
                         alertRequiredFieldsValidation();
                         usernameTextField.requestFocus();
                         return false;
                 }
                 
                 /*
                  * TODO:  Need Username validation so that length of username is > 4 characters long.
                  *                   We also should investigate changing the keyboard type for the username Text Field in activity_profile
                  *                   so that usernames cannot contain spaces or symbols.  Perhaps only allow alpha characters and numbers?
                  */
 
                 //TODO:  Search DATABASE for unique username
                 /*
                  * if( < Username Already Exists > )
                  * {
                  *                 alertUsernameAlreadyExists();
                  *                 return false;
                  * }
                  */
                 
                 //Check if username is unique, if so: add to database
                 
                 ServerAPI connection = new ServerAPI();
                 
                 String log = "";
                 try {
                         
                 //CANNOT Pass null?  Crash occurs here
                 if(firstNameTextField.getText().toString().compareTo("") == 0) {
                 	firstNameTextField.setText(" ");
                 } 
                 if(lastNameTextField.getText().toString().compareTo("") == 0) {
                 	lastNameTextField.setText(" ");
                 }
                 
                 log = connection.userAccountSetup(usernameTextField.getText().toString(), firstNameTextField.getText().toString(), lastNameTextField.getText().toString());
                 } catch (InterruptedException e) {
                         e.printStackTrace();
                 } catch (ExecutionException e) {
                         e.printStackTrace();
                 }
                 if(log.compareTo("Success") == 0) {
                         return true;
                 }else return false;
                 
                 //return true; //Temp until server issue is fixed
 
         } // uniqueUsernameValidation()
 
 
 
         /**
          * Verify all required user information is entered, with a minimum total height of 3ft (36in) and minimum weight of 50lbs.
          * 
          * @Return - True if all required fields have been entered and valid height and weight
          */
         private boolean requiredFieldValidation(){
 
                 String sHeight_Feet = height_feet_TextField.getText().toString();
                 String sHeight_Inches = height_inches_TextField.getText().toString();
                 String sWeight = weightTextField.getText().toString();
 
                 // Form Validation - Required fields (Height)
                 if(sHeight_Feet.isEmpty() && sHeight_Inches.isEmpty())
                 {        
                         alertRequiredFieldsValidation(); // Missing Fields alert
                         height_feet_TextField.requestFocus();
                         return false;
                 }
                 else
                 {
                         int heightFeet = 0;
                         int heightInches = 0;
 
                         if(!sHeight_Feet.isEmpty()){
                                 heightFeet = Integer.parseInt(sHeight_Feet);
                         }
                         if(!sHeight_Inches.isEmpty()){
                                 heightInches = Integer.parseInt(sHeight_Inches);
                         }
 
                         int totalHeightInches = heightInches + (heightFeet * 12);
 
                         if(totalHeightInches < 36)
                         {
                                 alertHeightValidation(); // Invalid Height alert
                                 height_feet_TextField.requestFocus();
                                 return false;
                         }
                         else
                         {        
                                 // Form Validation - Required fields (Weight)
                                 if(sWeight.isEmpty())
                                 {
                                         alertRequiredFieldsValidation(); // Missing Fields alert
                                         weightTextField.requestFocus();
                                         return false;
                                 }
                                 else
                                 {
                                         int weight = Integer.parseInt(sWeight);
 
                                         if(weight < 50)
                                         {
                                                 alertWeightValidation(); // Invalid Weight alert
                                                 weightTextField.requestFocus();
                                                 return false;
                                         }
                                 }
                         }
                 }
 
                 return true; // All checks pass
 
         } // requiredFieldValidation()
 
 
 
         /**
          *  Load alert dialog for missing required fields
          */
         private void alertUsernameAlreadyExists(){
 
                 final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
                 alert.setTitle(R.string.UsernameAlreadyExists);
                 alert.setMessage(R.string.PleaseTryAgain);
 
                 alert.setPositiveButton(R.string.Okay, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                                 keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                 keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //Open Keyboard
                         }
                 });
                 alert.show();
 
         } // alertUsernameAlreadyExists()
 
 
         /**
          *  Load alert dialog for missing required fields
          */
         private void alertRequiredFieldsValidation(){
 
                 final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
                 alert.setTitle(R.string.InvalidEntry);
                 alert.setMessage(R.string.MissingRequiredField);
 
                 alert.setPositiveButton(R.string.Okay, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                                 keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                 keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //Open Keyboard
                         }
                 });
                 alert.show();
 
         } // alertRequiredFieldsValidation()
 
         /**
          *  Requests total height greater than 3 feet
          */
         private void alertHeightValidation(){
 
                 final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
                 alert.setTitle(R.string.InvalidHeight);
                 alert.setMessage(R.string.MinimumHeight);
 
                 alert.setPositiveButton(R.string.Okay, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                                 keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                 keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //Open Keyboard
                         }
                 });
                 alert.show();
         } // alertHeightValidation()
 
 
         /**
          *  Requests total Weight greater than 50 lbs.
          */
         private void alertWeightValidation(){
 
                 final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
                 alert.setTitle(R.string.InvalidWeight);
                 alert.setMessage(R.string.MinimumWeight);
 
                 alert.setPositiveButton(R.string.Okay, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                                 keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                 keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED,0); //Open Keyboard
                         }
                 });
                 alert.show();
         } // alertWeightValidation()
 
         /**
          *  Confirm that user wishes to delete profile
          */
         private void alertConfirmProfileDelete(){
 
                 final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
                 alert.setTitle(R.string.DeleteProfile);
 
                 // Text View - User Agreement Message
                 TextView tvDeleteProfileMessage = new TextView(this);
                 TextView tvConfirmDeleteProfile = new TextView(this);
 
                 tvDeleteProfileMessage.setText(R.string.DeleteProfileMessage);
                 tvDeleteProfileMessage.setTextSize(16);
                 tvDeleteProfileMessage.setLineSpacing(5, 1);
                 tvDeleteProfileMessage.setPadding(30, 20, 30, 10);
 
                 tvConfirmDeleteProfile.setText(R.string.DoYouWishToProceed);
                 tvConfirmDeleteProfile.setTextSize(16);
                 tvConfirmDeleteProfile.setPadding(30, 10, 30, 20);
 
                 // Set Alert's View
                 ScrollView scrollView = new ScrollView(this); // Need alert context to be scrollable on small screens
                 LinearLayout alertLayout = new LinearLayout(this); // Need Linear Layout to contain more than one View
                 alertLayout.setOrientation(1); // Set View to vertical
                 alertLayout.addView(tvDeleteProfileMessage);
                 alertLayout.addView(tvConfirmDeleteProfile);
                 scrollView.addView(alertLayout);
                 alert.setView(scrollView);
 
                 // YES button
                 alert.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
 
                                 m_userProfile.deleteUserProfile();                
 
                                 // Restart App
                                 restartFirstActivity(); 
                         }
                 });
 
                 // NO button
                 alert.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                                 // Cancel
                         }
                 });
                 alert.show();
 
         } // alertConfirmProfileDelete()
 
 
         /**
          *  Restarts BAC Safe 
          */
         private void restartFirstActivity()
         {
                 Intent restartIntent = getBaseContext().getPackageManager()
                                 .getLaunchIntentForPackage(getBaseContext().getPackageName() );
 
                 restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
                 startActivity(restartIntent);
 
         } // restartFirstActivity()
         
         /**
          * Sets up the Options Menu for the Profile Page
          * @param menu - The Options Menu to be setup
          */
         @Override
         public boolean onCreateOptionsMenu(Menu menu) {
                 
                 MenuInflater inflater = getMenuInflater();
                 
                 if(!m_userProfile.getUserName().isEmpty()){
                         inflater.inflate(R.menu.menu_profile, menu); 
                         return true;
                 }
 
                 return false;
                 
         } // onPrepareOptionsMenu()
 
 
         /**
          * Determines which Options Menu was clicked and what action should be taken
          * @param item - The Options Menu item selected
          */
         @Override
         public boolean onOptionsItemSelected(MenuItem item)
         {
                 switch(item.getItemId())
                 {  
                 case R.id.menu_item_DeleteProfile:
                         // TODO:
                         alertConfirmProfileDelete();
                         return true;
 
                 default:
                         return super.onOptionsItemSelected(item);
                 }
         } // onOptionsItemSelected()
 
 } // class ProfileActivity
