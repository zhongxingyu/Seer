 package com.cs301w01.meatload.authentication.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.activities.Skindactivity;
 import com.cs301w01.meatload.authentication.Controllers.UserManager;
 import com.cs301w01.meatload.authentication.Model.Patient;
 import com.cs301w01.meatload.authentication.Model.Specialist;
 import com.cs301w01.meatload.authentication.Model.User;
 import com.cs301w01.meatload.authentication.querygenerator.UserQueryGenerator;
 import com.cs301w01.meatload.model.Album;
 
 import java.util.ArrayList;
 
 /**
  * This activity is used to create the user interface and logic for creating a new user
  * in the future version of our app. It talks with the UserQueryGenerator in order to put
  * the new user in the database.
  * 
  *  @author Derek Dowling
  */
 public class SignupActivity extends Skindactivity {
 
     private EditText name;
     private EditText email;
     private EditText username;
     private Spinner role;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.signup);
 
         name = (EditText) findViewById(R.id.fullNameEditText);
         email = (EditText) findViewById(R.id.emailEditText);
         role = (Spinner) findViewById(R.id.roleSpinner);
         username = (EditText) findViewById(R.id.userNameSignupEditText);
 
         createListeners();
         
         //JOEL IS COMMENTING THIS OUT BECAUSE IT IS BROKEN AND I DON'T FEEL LIKE DEBUGGING
 
 
     }
 
     private void createListeners() {
 
     	//creates the submit listener for when completing the new user form
         final Button submit = (Button) findViewById(R.id.submitNewUserButton);
         submit.setOnClickListener(new View.OnClickListener() {
             //@Override
             public void onClick(View view) {
                 createNewUser();
             }
         });
 
 
     }
 
     /**
      * Pulls the text entered into the form out and provides it to the UserQueryManager.
      * Then returns a new user back to the login screen so it can redirect to the proper screen.
      */
     private void createNewUser() {
 
         // Display select password alert, then confirm and compare password
 
         // Create user
         String fullName = name.getText().toString();
         String uEmail = email.getText().toString();
         String uRole = String.valueOf(role.getSelectedItem());
         String usrName = username.getText().toString();
       // String password = getAndConfirmPassword();
        String password = "test";
 
         User newUser;
         UserManager uM = new UserManager(this);
 
         //create new specialist if selected from spinner
         if(uRole.equals(UserQueryGenerator.SPECIALIST_ROLE)) {
 
             newUser = new Specialist(fullName, uEmail, new ArrayList<Patient>());
             uM.createNewUser(newUser, usrName, password, UserQueryGenerator.SPECIALIST_ROLE);
 
         }
         //creates a new patient if selected from spinner
         else {
 
             newUser = new Patient(fullName, uEmail, new ArrayList<Album>(), 0);
             uM.createNewUser(newUser, usrName, password, UserQueryGenerator.PATIENT_ROLE);
 
         }
 
 
         //create intent result for the login screen
         Intent resultIntent = new Intent();
         resultIntent.putExtra("user", newUser);
         this.setResult(Activity.RESULT_OK);
 
         //end activity
         finish();
 
     }
 
     /**
      * Creates a dialog window for creating a new password, then confirms the password
      * and returns its value.
      * 
      * @return pwd String
      * 
      */
     public String getPassword() {
 
         String pwd = "";
 
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
         alert.setTitle("Choose A Password");
         alert.setMessage("Enter and confirm your password.");
 
         // Set an EditText view to get user input
         final EditText input = new EditText(this);
         input.setHint("Enter New Album Name");
         alert.setView(input);
 
         alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
 
 
 
             }
         });
 
         alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
 
             }
         });
 
 
         alert.show();
 
         Log.d("Alert Dialog", "Created.");
 
         return pwd;
 
     }
 
     public void update(Object model) {
 
     }
 }
