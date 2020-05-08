 package org.vcs.medmanage;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 
 import db.DatabaseHelper;
 
 import entities.RecentResidentUtils;
 import entities.Resident;
 import entities.ResidentUtils;
 
 
 /* This class will be the Resident's profile page with a tab for the medications the residents are on
  * 
  * */
 public class ResidentMedicineActivity extends FragmentActivity {
 
 	private DatabaseHelper databaseHelper = null;
 	private RuntimeExceptionDao<Resident, Integer> residentDao;
 	private List<Resident> residentList = new ArrayList<Resident>();
 	private List<MedicationAppointment> medApts = new ArrayList<MedicationAppointment>();
 	private CalendarService calendar;
 	
     Resident currentResident;
     String residentName;
     LinearLayout layout = null;
     
     private ArrayAdapter<Resident> residentAdapter;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(UI_MODE_SERVICE, "Entered Resident Medicine Activity");
         setContentView(R.layout.resident_medicine);
         
         // Get the resident database
         residentDao =
 				getHelper().getResidentDataDao();
         
         Log.d(UI_MODE_SERVICE, "Created residentDao");
         
       //Set the action bar back button because it's nice
   		getActionBar().setHomeButtonEnabled(true);
   		getActionBar().setDisplayHomeAsUpEnabled(true);
         
         // Get the variables passed from the previous screen...
         Intent inIntent = getIntent();
		if(inIntent.hasExtra("resident")){
 			// If we got here during normal application usage, there will be 
 			// a resident attached as an extra, which we should get from 
 			// the database.
 			Bundle extras = inIntent.getExtras();
 			Log.d(UI_MODE_SERVICE, "Got Resident Name");
			residentName = extras.getString("resident");
 		}else{// If there wasn't a matching key in the intent, then this page 
 			//    was probably navigated to during testing. In that case, we
 			//    just use a default Resident.
 			Log.d(UI_MODE_SERVICE, "Setting to Default Resident: James Cooper");
 			residentName = "James Cooper";
 		}
         
 		
         residentList = getResident(residentDao, residentName);
 		if(residentList.size() == 1){
 			currentResident = residentList.get(0);
 			Log.d(UI_MODE_SERVICE, "Got Resident: " + residentName);
 		}
 		else{
 			Log.d(UI_MODE_SERVICE, "Invalid number of Residents!");
 		}
         
 		
 		displayPatientPicture(currentResident);	
 		//debuglogRes(currentResident);
         displayPatientProfile(currentResident);
         calendar = new CalendarService(this);
         layout = (LinearLayout)findViewById(R.id.list_medapts);
         displayCalendar(currentResident, calendar);
         
         
         //Changes on click :)
         final Button button = (Button) findViewById(R.id.button1);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
             	setTxtViews();
             }
         });
         
         
         }
     
     private void debuglogRes(Resident res) {
 		Log.d("logRes", res.getName());
 		Log.d("logRes", "Age" +Integer.toString(res.getAge()));
 		Log.d("logRes", "Gender" + String.valueOf(res.isGender()));
 	}
 
 	public void displayPatientPicture(Resident res){
     	// Make this so that you can first display a picture from a given path
     	// Then when that is done, check if you have a picture from this resident
     	// If you don't have a picture from the resident, take a picture and 
     	//  store it in memory and store the path in the database?
     	// Then whenever this resident is called, make it so that you update 
     	//  it to the correct picture for this resident!
     }
     
     // Show the Patient Information in a normal fashion
     public void displayPatientProfile(Resident res){
     	
     	// Change the patient profile picture
     	//ImageView iv = (ImageView) findViewById(R.id.patientPicture);
     	//iv.setImageBitmap(bitmap);
     	Log.d(UI_MODE_SERVICE, "Entered displayPatientProfile");
     	    	
     	updateTextView(res.getName(), "txtPatientName");
     	if (res.isGender()){
     		updateTextView("Female", "txtPatientGender");
     	}
     	else{
     		updateTextView("Male", "txtPatientGender");
     	}
     	updateTextView(Integer.toString(res.getRoomNumber()), "txtPatientRoom");
     	updateTextView(res.getDiagnosis(), "txtPatientDiagnosis");
     	// What is "Other Diagnosis considered as?"
     	updateTextView(Integer.toString(res.getAge()), "txtPatientAge");
     	// Add weight to the database!
     	//updateTextView(Integer.toString(res.getWeight()), "txtPatientWeight");
     	updateTextView(res.getRecentActions(), "txtPatientRecentActions");
     	updateTextView(res.getNotes(), "txtPatientNotes");
     	
     	
     	// Check if any of these need to be put somewhere?
     	Log.d(UI_MODE_SERVICE, "Primary Diagnosis?: "+ res.getPrimaryDiagnosis());
     	Log.d(UI_MODE_SERVICE, "Other Diagnosis: "+ res.getOtherDiagnoses());
     	Log.d(UI_MODE_SERVICE, "Allergies: "+ res.getAllergies());
     	Log.d(UI_MODE_SERVICE, "Picture Path: "+ res.getPicturePath());
     	Log.d(UI_MODE_SERVICE, "Preferences: "+ res.getPrefs());
     	
     }
     
     // List view of the information 
     public void displayCalendar(Resident res, CalendarService cal){
     	Log.d(UI_MODE_SERVICE, "Entered displayCalendar");
     	
     	// Get the list of times that each medication needs to be taken at...
     	medApts = cal.getResidentMedications(res);
     	
     	// Sort medApts by time...
     	sortMedApts(medApts);
     	
     	// Lets just get the names from the medApts and put it into an ArrayList?
     	ArrayList<String> strMedApts = new ArrayList<String>();
     	for(int i = 0; i < medApts.size(); i++){
     		strMedApts.add(medApts.toString());
     		Log.d(UI_MODE_SERVICE, "MedApt" + i + ": " + strMedApts.get(i));
     	}
     	
     	
     	Log.d(UI_MODE_SERVICE, "Updating the listView?");
     	// Send this to the list view to see it
     	//ListView listview = (ListView) findViewById(R.id.residentListView);
 
     	Log.d(UI_MODE_SERVICE, "Updating Adapter?");
     	addList(strMedApts);
     }
     
     private void sortMedApts(List<MedicationAppointment> medApts2) {
 		// For now, lets just print the medicines in the list view
 	}
     
 
 
 	// Update all the Text Views for the Patient
     public int updateTextView(String toThis, String name) {
     	String finalString = "";
     	TextView t;
      	
     	if(name.equals("txtPatientName")){
     		t = (TextView) this.findViewById(R.id.txtPatientName);
     	}   	
      	else if(name.equals("txtPatientGender")){
     		t = (TextView) this.findViewById(R.id.txtPatientGender);
     		finalString = "Gender: ";
     	}
     	else if(name.equals("txtPatientRoom")){
     		t = (TextView) this.findViewById(R.id.txtPatientRoom);
     		finalString = "Room ";
     	}
     	else if(name.equals("txtPatientDiagnosis")){
     		t = (TextView) this.findViewById(R.id.txtPatientDiagnosis);
     		finalString = "Diagnosis: ";
     	}
     	else if(name.equals("txtPatientAge")){
     		t = (TextView) findViewById(R.id.txtPatientAge);
     		finalString = "Age: ";
     	}
     	else if(name.equals("txtPatientWeight")){
     		t = (TextView) findViewById(R.id.txtPatientWeight);
     		finalString = "Weight: ";
     	}
     	else if(name.equals("txtPatientRecentActions")){
     		t = (TextView) findViewById(R.id.txtPatientRecentActions);
     		finalString = "Recent Activity: \n";
     	}
     	else if(name.equals("txtPatientNotes")){
     		t = (TextView) findViewById(R.id.txtPatientNotes);
     		finalString = "Nurse Notes: \n";
     	}
     	else{
     		// Didn't find any text view
     		return -1;
     	}
     	
     	finalString = finalString + toThis;
     	Log.d("UpdateTextView ", finalString);
        
     	t.setText(finalString);
         return 1;
     }
     
     // This was for testing purposes... to make sure we can change it
     public void setTxtViews(){
     	updateTextView("Phil Simms", "txtPatientName");
     	updateTextView("Male", "txtPatientGender");
     	updateTextView("24", "txtPatientAge");
     	updateTextView("13", "txtPatientRoom");
     	updateTextView("Melanoma", "txtPatientDiagnosis");
     	updateTextView("165", "txtPatientWeight");
     	updateTextView("10am: Gave tylenol for headache.", "txtPatientRecentActions");
     	updateTextView("Beach", "txtPatientNotes");
     }
     
 	/**
 	 * Gets a reference to the DB. If it fails, it returns null instead.
 	 */
 	protected DatabaseHelper getHelper(){
 		if(databaseHelper == null){
 			databaseHelper = 
 					OpenHelperManager.getHelper(this.getBaseContext(), DatabaseHelper.class);
 		}
 		return databaseHelper;
 	}
 	
 	// Use the ResidentUtils class
 	public List<Resident> getResident(RuntimeExceptionDao<Resident, Integer> dao, String resName){
 		ResidentUtils resUtils = new ResidentUtils();
 		
 		return resUtils.findResident(dao, resName);	
 	}
 
 		@Override
 		public void onDestroy(){
 			super.onDestroy();
 			if(databaseHelper != null){
 				OpenHelperManager.releaseHelper();
 				databaseHelper = null;
 			}
 		}
 		
 		// Adds medication fragments 
 		public void addList(ArrayList<String> list){
 
 			for(int i = 0; i < list.size(); i++){
                 Bundle residentArgs = new Bundle();
                 // put in the resident name
                 residentArgs.putString("resName", residentName);
 
 				android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
 				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 				residentArgs.putString("medName", list.get(i));
 
 				Log.d("main", "Adding" + list.get(i));
 				
 				MedicationFragment recentFragment = new MedicationFragment();
 				recentFragment.setArguments(residentArgs);
 				fragmentTransaction.add(layout.getId(), recentFragment);
 				Log.d("main", "Added to Fragment");
 				fragmentTransaction.commit();
 			}
 		}
 
 
 }
