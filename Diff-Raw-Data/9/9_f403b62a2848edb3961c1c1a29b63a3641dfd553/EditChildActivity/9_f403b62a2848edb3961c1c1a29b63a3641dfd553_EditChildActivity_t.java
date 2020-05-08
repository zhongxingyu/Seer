 package com.feedme.activity;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.feedme.R;
 import com.feedme.dao.BabyDao;
 import com.feedme.model.Baby;
 
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:27 PM
  */
 public class EditChildActivity extends Activity {
     
     public static final int EDIT_CHILD_ACTIVITY_ID = 6;
 
     private int mYear;
     private int mMonth;
     private int mDay;
 
     private Button babyDob;
 
     static final int DATE_DIALOG_ID = 0;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.add_child);
         final BabyDao babyDao = new BabyDao(getApplicationContext());
 
         String name = "";
         if (getIntent().getExtras() != null && getIntent().getExtras().getString("babyName") != null) {
             name = getIntent().getExtras().getString("babyName");
         }
 
         Baby baby = null;
         if (!name.equals("")) {
             baby = babyDao.getBabyByName(name);
         }
         final int babyId = baby.getID();
 
         TextView addChild = (TextView) findViewById(R.id.addChild);
         addChild.setText("Edit Baby");
 
         final EditText babyName = (EditText) findViewById(R.id.babyName);
         final Spinner babySex = (Spinner) findViewById(R.id.babySex);
         final EditText babyHeight = (EditText) findViewById(R.id.babyHeight);
         final EditText babyWeight = (EditText) findViewById(R.id.babyWeight);

         Button addChildButton = (Button)findViewById(R.id.addChildButton);
         Button takePicture = (Button) findViewById(R.id.takePicture);
         Button selectPicture = (Button) findViewById(R.id.pickPicture);
 
         babyName.setText(baby.getName());
         babyHeight.setText(baby.getHeight());
         babyWeight.setText(baby.getWeight());
         babyDob = (Button) findViewById(R.id.babyDob);
        babyDob.setText(baby.getDob());

         // add a click listener to the button
         babyDob.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 showDialog(DATE_DIALOG_ID);
             }
         });
 
         // get the current date
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
 
         // display the current date
        //updateDateDisplay();
 
         //populate male/female spinner
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.babySex, android.R.layout.simple_spinner_item );
         adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
         Spinner s = (Spinner) findViewById( R.id.babySex );
         s.setAdapter( adapter );
 
         //Set Spinner Value for Baby Sex
         if(baby.getSex().equals("Male")){
             babySex.setSelection(0);
         } else {
             babySex.setSelection(1);
         }
 
         //Take Picture Button
         takePicture.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), TakePictureActivity.class);
 
                 //TODO: There has GOT to be a better way to do this. Soooo Gross
                 intent.putExtra("babyName", babyName.getText().toString());
                 intent.putExtra("babySex", babySex.getSelectedItem().toString());
                 intent.putExtra("babyHeight", babyHeight.getText().toString());
                 intent.putExtra("babyWeight", babyWeight.getText().toString());
                 intent.putExtra("babyDob", babyDob.getText().toString());
                 intent.putExtra("intentId", EDIT_CHILD_ACTIVITY_ID);
 
                 startActivityForResult(intent, 1);
             }
         });
 
         //Select Picture Button
         selectPicture.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), SelectPictureActivity.class);
 
                 //TODO: There has GOT to be a better way to do this. Soooo Gross
                 intent.putExtra("babyName", babyName.getText().toString());
                 intent.putExtra("babySex", babySex.getSelectedItem().toString());
                 intent.putExtra("babyHeight", babyHeight.getText().toString());
                 intent.putExtra("babyWeight", babyWeight.getText().toString());
                 intent.putExtra("babyDob", babyDob.getText().toString());
                 intent.putExtra("intentId", EDIT_CHILD_ACTIVITY_ID);
 
                 startActivityForResult(intent, EDIT_CHILD_ACTIVITY_ID);
             }
         });
 
         // button listener for add child button
         addChildButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 
                 String picturePath = "";
                 if(getIntent().getExtras()!=null && getIntent().getExtras().get("picturePath")!=null){
                     picturePath = (String) getIntent().getExtras().get("picturePath");
                 }
 
                 /**
                  * CRUD Operations
                  * */
                 // Inserting baby
                 Log.d("Insert: ", "Inserting ..");
                 babyDao.updateBaby(new Baby(babyName.getText().toString(), babySex.getSelectedItem().toString(),
                         babyHeight.getText().toString(), babyWeight.getText().toString(), babyDob.getText().toString(), picturePath), babyId);
 
                 // Reading all babies
                 Log.d("Reading: ", "Reading all babies..");
                 List<Baby> babies = babyDao.getAllBabies();
 
                 for (Baby baby : babies) {
                     String log = "Id: "+baby.getID()+" ,Name: " + baby.getName() + " ,Sex: " + baby.getSex()
                             + " ,Height: " + baby.getHeight() + " ,Weight: " + baby.getWeight() + " ,DOB: " + baby.getDob();
 
                     // Writing babies to log
                     Log.d("Name: ", log);
                 }
 
 
                 Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                 intent.putExtra("babyName", babyName.getText().toString());
                 startActivityForResult(intent, 3);
             }
         });
     }
 
     @Override
     protected Dialog onCreateDialog(int id)
     {
         switch (id) {
             case DATE_DIALOG_ID:
                 return new DatePickerDialog(this,
                         mDateSetListener,
                         mYear, mMonth, mDay);
           }
         return null;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.home:
                 startActivity(new Intent(EditChildActivity.this,
                         HomeActivity.class));
                 break;
             case R.id.settings:
                 startActivity(new Intent(EditChildActivity.this,
                         SettingsActivity.class));
                 break;
         }
         return true;
     }
 
 
     // updates the date we display in the TextView
     private void updateDateDisplay()
     {
         babyDob.setText(
                 new StringBuilder()
                         // Month is 0 based so add 1
                         .append(mMonth + 1).append("-")
                         .append(mDay).append("-")
                         .append(mYear).append(" "));
     }
 
     // the callback received when the user "sets" the date in the dialog
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener()
             {
                 public void onDateSet(DatePicker view, int year,
                                       int monthOfYear, int dayOfMonth)
                 {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     updateDateDisplay();
                 }
             };
 
     }
 
