 // LuperApp.java
 // -------------
 
 // NOTE: ActionBarSherlock is a polyfill providing the new Android 4.2 ActionBar
 //       functionality to all versions of android.  To compile this project
 //       properly, the actionbarsherlock project must also be in your
 //       workspace and/or build path in addition to the Luper project.
 //       See README.md on github for more details.
 
 package com.teamluper.luper;
 
 // imports from the core android API
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.rest.RestService;
 import com.teamluper.luper.rest.LuperRestClient;
 import org.springframework.web.client.HttpClientErrorException;
 
 import java.io.File;
 
 // imports for ActionBarSherlock dependency
 // imports for AndroidAnnotations dependency
 // @AfterViews is never used, but don't remove as we may want to use it eventually
 // imports for the SpringFramework REST dependency
 // imports for Test Suite
 
 // @EActivity = "Enhanced Activity", which turns on AndroidAnnotations features
 @EActivity
 public class LuperMainActivity extends SherlockFragmentActivity {
 
   static LuperMainActivity instance;
   @RestService
   LuperRestClient rest;
 
   ViewPager mViewPager;
   TabsAdapter mTabsAdapter;
 
   // Additional local variables
   AccountManager am;
   SQLiteDataSource dataSource;
 
   @Override
     protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     instance = this;
 
     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
     // set up the ViewPager, which we will use in conjunction with tabs.
     // this makes it possible to swipe left and right between the tabs.
     mViewPager = new ViewPager(this);
     mViewPager.setId(R.id.tabcontentpager);
     setContentView(mViewPager);
 
     dataSource = new SQLiteDataSource(this);
     dataSource.open();
 
     final ActionBar bar = getSupportActionBar();
     bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // Gives us Tabs!
 
     // FIXME this is slowing down the app launch dramatically.  Perhaps do it in background?
     //Creates a folder for Luper and associated clips and projects
     File nfile=new File(Environment.getExternalStorageDirectory()+"/LuperApp/Clips");
     File mfile=new File(Environment.getExternalStorageDirectory()+"/LuperApp/Projects");
     nfile.mkdir();
     mfile.mkdir();
 
     // now we set up the TabsAdapter, which is a special class borrowed from Google.
     // TabsAdapter.java takes care of all the guts of the Tab interactions, and
     // links it with our ViewPager for us.  The code below is all we need to
     // add some fragment content as tabs in the ActionBar!
     mTabsAdapter = new TabsAdapter(this, mViewPager);
     mTabsAdapter.addTab(bar.newTab().setText(""+"Home"),
         TabHomeFragment_.class, null);
     mTabsAdapter.addTab(bar.newTab().setText(""+"Projects"),
         TabProjectsFragment_.class, null);
     mTabsAdapter.addTab(bar.newTab().setText(""+"Friends"),
         TabFriendsFragment_.class, null);
 
     //create a directory to save in
     File testdir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/LuperApp/");
     testdir.mkdirs();
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     testAccounts();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inf = getSupportMenuInflater();
     inf.inflate(R.menu.activity_main, menu);
 
     // because one of the action items is a custom view,
     // we need the next few lines to force it to use onOptionsItemSelected
     // when it's clicked.
     final MenuItem item = menu.findItem(R.id.menu_new_project);
     item.getActionView().setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         onOptionsItemSelected(item);
       }
     });
 
     return super.onCreateOptionsMenu(menu);
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if(item.getItemId() == R.id.menu_new_project) {
       DialogFactory.prompt(this,"New Project",
         "Please type a name for your project.  You can change it later.",
         new Lambda.StringCallback() {
           public void go(String value) {
             newProject(value);
           }
         }
       );
     }
     if(item.getItemId() == R.id.menu_settings) {
       Intent intent = new Intent(this, LuperSettingsActivity_.class);
       startActivity(intent);
     }
     if(item.getItemId() == R.id.menu_login) {
      Intent intent = new Intent(this, LuperLoginActivity_.class);
       startActivity(intent);
     }
     return true;
   }
 
   public static LuperMainActivity getInstance() {
     return instance;
   }
 
   public SQLiteDataSource getDataSource() {
     return dataSource;
   }
 
   //method to navigate to the audiorecorder activity
   public void startRecording(View view) {
   	Intent intent = new Intent(this, AudioRecorderTestActivity_.class);
   	startActivity(intent);
   }
 
   public boolean deviceIsOnline() {
     // borrowed implementation from:
     // http://stackoverflow.com/questions/2789612/how-can-i-check-whether-an-android-device-is-connected-to-the-web
     ConnectivityManager cm =
       (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo ni = cm.getActiveNetworkInfo();
     if (ni == null) return false;
     return ni.isConnected();
   }
 
   // this will be removed, it's an example of how we'll access the EC2 server.
   @Background
   public void testRestAPI(View view) {
     if(!deviceIsOnline()) {
       alert("Internet Connection Required",
           "That feature requires access to the internet, and your device is " +
           "offline!  Please connect to a Wifi network or a mobile data network " +
           "and try again.");
       return;
     }
     try {
       String t = rest.getTestString();
       alert("Database Connection Test PASS!",
         "Request: GET http://teamluper.com/api/test\n" +
         "Response: '" + t + "'");
     } catch(HttpClientErrorException e) {
       alert("Database Connection Test FAIL!", e.toString());
     }
   }
 
   @UiThread
   public void alert(String title, String message) {
     DialogFactory.alert(this, title, message);
   }
 
   public void dropAllData(View view) {
     dataSource.dropAllData();
     DialogFactory.alert(this,"Done!");
   }
 
   public void newProject(String title) {
     Sequence newSequence = dataSource.createSequence(null, title);
     ListView lv = (ListView) findViewById(R.id.projectsListView);
     @SuppressWarnings("unchecked")
     ArrayAdapter<Sequence> adapter = (ArrayAdapter<Sequence>) lv.getAdapter();
     adapter.add(newSequence);
   }
 
   @Background
   public void exampleProject(View view) {
     alert("Nothing to see here",
       "This button will be removed soon.  There is no more Dummy Project. " +
       "To launch LuperProjectEditorActivity, just create and open a real project.");
   }
 
   @Background
   public void launchProjectEditor(long projectId) {
     Intent intent = new Intent(this, LuperProjectEditorActivity_.class);
     if(projectId != -1)  intent.putExtra("selectedProjectId", projectId);
 		startActivity(intent);
   }
 
   // this will be removed too, it's checking the google account that the
   // device's user is already logged in with.  We'll likely ditch this in favor
   // of a Facebook-based login solution.
   @Background
   void testAccounts() {
     if(am == null) am = AccountManager.get(this);
     Account[] accounts = am.getAccountsByType("com.google");
     System.out.println("== LUPER ACCOUNTS TESTING ==  found "+accounts.length+" accounts");
     for(int i=0; i<accounts.length; i++) {
       System.out.println(accounts[i].toString());
     }
   }
 }
