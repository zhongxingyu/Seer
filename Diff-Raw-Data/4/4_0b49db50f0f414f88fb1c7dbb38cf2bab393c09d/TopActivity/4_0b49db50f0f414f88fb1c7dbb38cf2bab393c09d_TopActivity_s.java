 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.widget.ArrayAdapter;
 import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.Course;
 import no.hials.muldvarp.v2.domain.Domain;
 import no.hials.muldvarp.v2.domain.Programme;
 import no.hials.muldvarp.v2.domain.TimeEdit;
 import no.hials.muldvarp.v2.fragments.MuldvarpFragment;
 import no.hials.muldvarp.v2.utility.FragmentUtils;
 
 /**
  * This class defines a top-level activity for a given level. This activity-class
  * contains an ArrayList of Fragment classes named fragmentList which contains the
  * types and various fragments that comprise the main content of the Activity.
  *
  * @author johan
  */
 public class TopActivity extends MuldvarpActivity {
     private Activity thisActivity = this;
     SpinnerAdapter mSpinnerAdapter = null;
     
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
         
         
         mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
         IntentFilter filter = new IntentFilter();
         filter.addAction(MuldvarpService.ACTION_UPDATE_FAILED);
         mReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 System.out.println("TopActivity Got onReceive in BroadcastReceiver " + intent.getAction());
                 if(progress != null) {
                     postUpdateStuff();
                     Toast t = Toast.makeText(getApplicationContext(), "Kunne ikke koble til serveren. \nEr du koblet til internett?", Toast.LENGTH_LONG);
                     t.show();
                 }
             }
         };
         mLocalBroadcastManager.registerReceiver(mReceiver, filter);
         
         //See if the Activity was started with an Intent that included a Domain object
         if(getIntent().hasExtra("Domain")) {
             domain = (Domain) getIntent().getExtras().get("Domain");
             activityName = domain.getName();
             
             setUpFragmentList();
             TimeEditContent();
             
             // We use this to send broadcasts within our local process.
             mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
              // We are going to watch for interesting local broadcasts.
             filter = new IntentFilter();
             if(domain instanceof Programme) {
                 filter.addAction(MuldvarpService.ACTION_PROGRAMMES_UPDATE);
             } else if(domain instanceof Course) {
                 filter.addAction(MuldvarpService.ACTION_COURSE_UPDATE);
             } else {
                 filter.addAction(MuldvarpService.ACTION_ALL_UPDATE);
             }
             mReceiver = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     System.out.println("TopActivity Got onReceive in BroadcastReceiver " + intent.getAction());
                     if(domain instanceof Course) {
                         domain = mService.selectedCourse;
                     } else if(domain instanceof Programme) {
                         domain = mService.selectedProgramme;
                     } else {
                         domain = mService.getFrontpage();
                     }
                     setUpFragmentList();
                     postUpdateStuff();
                 }
             };
             mLocalBroadcastManager.registerReceiver(mReceiver, filter);
             TimeEditImpl();
         } else {
             setUpFrontpage();
         }
     }
     
     public void setUpFrontpage() {
         progress = new ProgressDialog(this);
         progress.show();
         System.out.println("Setting up frontpage");
         isFrontpage = true;
         TimeEditContent();
         
         activityName = getResources().getString(R.string.app_logo_top);
         mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
         IntentFilter filter = new IntentFilter();
         filter.addAction(MuldvarpService.ACTION_FRONTPAGE_UPDATE);
         mReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 domain = mService.getFrontpage();
                 setUpFragmentList();
                 getIntent().putExtra("Domain", domain);
                 postUpdateStuff();
             }
         };
         mLocalBroadcastManager.registerReceiver(mReceiver, filter);
         
         TimeEditImpl();
     }
     
     public void TimeEditImpl() {
         // TimeEdit example
         mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
         IntentFilter filter = new IntentFilter();
         filter.addAction(MuldvarpService.ACTION_TIMEEDIT_UPDATE);
         mReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 TimeEditContent();
             }
         };
         mLocalBroadcastManager.registerReceiver(mReceiver, filter);
     }
     
     public void TimeEditContent() {
         if(mService != null) {
             TimeEdit t = null;
             for(int i = 0; i < mService.getTimeEdit().size(); i++ ) {
                 TimeEdit tt = (TimeEdit)mService.getTimeEdit().get(i);
                 Date currentDate = new Date();
                if(Integer.parseInt(tt.getDate().substring(0, 2)) == currentDate.getDate()) {
                     t = (TimeEdit)mService.getTimeEdit().get(i);
                     break;
                 }
             }
             String s = "";
             if(t != null) {
                 s = "\n"
                     + "Dagens timer:"
                     + "\n"
                     + t.getDay() + " "
                     + t.getDate()
                     + "\n";
                 for(TimeEdit.Course c : t.getCourses()) {
                     s += "\n" 
                         + c.getCourse()
                         + "\n" 
                         + c.getTime()
                         + "\n"
                         + c.getRoom()
                         + "\n"
                         + c.getTeacher()
                         + "\n"
                         ;
                 }
             }
 
             timeeditText.setText(s);
         } else {
             timeeditText.setText("Oppdater for å se timeplan");
         }
     }
     
     public void setUpFragmentList() {
         if(fragmentList != null){
             System.out.println("fragmentList size: " + fragmentList.size());
         } else {
             System.out.println("FANE ASODFAOFJDSIFIODSJFIOSIODJFIOSDJFIOSDJFIOSDJ");
         }
         domain.constructList(fragmentList, domain.getFragments());
 
         //Get dropdown menu using standard menu
         mSpinnerAdapter = new ArrayAdapter(getBaseContext(),
         android.R.layout.simple_spinner_dropdown_item,
         getDropDownMenuOptions(fragmentList));
 
         ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
             @Override
             public boolean onNavigationItemSelected(int position, long itemId) {
                 getIntent().putExtra("tab", position);
                 return FragmentUtils.changeFragment(thisActivity, fragmentList, position);
             }
         };
         getActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
     }
 
     /**
      * This function generates a List of String values based on the titles of
      * a List of MuldvapFragments.
      *
      * @param fragmentList List of MuldvarpFragments
      * @return List
      */
     public List getDropDownMenuOptions(List<MuldvarpFragment> fragmentList){
         List retVal = new ArrayList();
         for (int i = 0; i < fragmentList.size(); i++) {
             retVal.add(fragmentList.get(i).getFragmentTitle());
         }
         return retVal;
     }
 }
