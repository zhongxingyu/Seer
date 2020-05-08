 package de.codenauts.hockeyapp;
 
 import java.util.ArrayList;
 
 import net.hockeyapp.android.CheckUpdateTask;
 import net.hockeyapp.android.CrashManager;
 import net.hockeyapp.android.UpdateActivity;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnItemClickListener {
   final static int DIALOG_LOGIN = 1;
 
   private AlertDialog alert;
   private AppsAdapter appsAdapter;
   private AppsTask appsTask;
   private AppTask appTask;
   private JSONArray apps;
   private LoginTask loginTask;
   private int selectedAppIndex;
   private View selectedAppView;
 
   private CheckUpdateTask checkUpdateTask;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main_view);
     moveViewBelowOrBesideHeader(this, R.id.content_view, R.id.header_view, 5);
 
     System.setProperty("http.keepAlive", "false");
 
     UpdateActivity.iconDrawableId = R.drawable.icon;
     UpdateActivity.packageName = this.getPackageName();
     
     if (savedInstanceState == null) {
       checkForUpdates();
     }
 
     loadApps(savedInstanceState);
   }
 
   private void checkForUpdates() {
     checkUpdateTask = new CheckUpdateTask(this, "https://rink.hockeyapp.net/", "0873e2b98ad046a92c170a243a8515f6");
     checkUpdateTask.execute();
   }
 
   @Override 
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
     
     MenuInflater inflater = getMenuInflater(); 
     inflater.inflate(R.menu.main_menu, menu);
     
     return true; 
   } 
   
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
     MenuItem refreshItem = menu.getItem(0);
     MenuItem logoutItem = menu.getItem(1);
     
     if (getAPIToken() == null) {
       refreshItem.setEnabled(false);
       logoutItem.setEnabled(false);
     }
     else {
       refreshItem.setEnabled(true);
       logoutItem.setEnabled(true);
     }
     
     return true;
   }
 
   @Override 
   public boolean onOptionsItemSelected(MenuItem item) {
     if (item.getItemId() == R.id.menu_logout) {
       setAPIToken(null);
       setStatus(getResources().getString(R.string.main_view_signed_out_label));
     }
 
     ListView listView = (ListView)findViewById(R.id.list_view);
     listView.setVisibility(View.INVISIBLE);
     
     this.apps = null;
     loadApps(null);
     
     return true;
   }
 
   private void loadApps(Bundle savedInstanceState) {
     if (savedInstanceState != null) {
       String json = savedInstanceState.getString("apps");
       try {
         this.apps = new JSONArray(json);
         didReceiveApps(this.apps);
       }
       catch (JSONException e) {
       }
     }
     
     if (this.apps == null) {
       String token = getAPIToken();
       if (token == null) {
         showDialog(DIALOG_LOGIN);
       }
       else {
         getApps(token);
       }
     }
   }
 
   private void getApps(String token) {
     appsTask = new AppsTask(this, token);
     appsTask.execute();
 
     setStatus("Searching for apps");
   }
 
   protected Dialog onCreateDialog(int id) {
     Dialog dialog = null;
     switch (id) {
     case DIALOG_LOGIN:
       dialog = createLoginDialog();
       break;
     }
 
     return dialog;
   }
 
   @Override
   public void onResume() {
     super.onResume();
 
     checkForCrashes();
 
     Object instance = getLastNonConfigurationInstance();
     if (instance instanceof LoginTask) {
       loginTask = (LoginTask)instance;
       if (loginTask != null) {
         loginTask.attach(this);
       }
     }
     else if (instance instanceof AppsTask) {
       appsTask = (AppsTask)instance;
       if (appsTask != null) {
         appsTask.attach(this);
       }
     }
     else if (instance instanceof AppTask) {
       appTask = (AppTask)instance;
       if (appTask != null) {
         appTask.attach(this);
       }
     }
   }
 
   private void checkForCrashes() {
     CrashManager.register(this, "https://rink.hockeyapp.net/", "0873e2b98ad046a92c170a243a8515f6");
   }
 
   @Override
   protected void onSaveInstanceState (Bundle outState) {
    if (this.apps != null) {
      outState.putString("apps", this.apps.toString());
    }
 ;  }
 
   @Override
   public Object onRetainNonConfigurationInstance() {
     checkUpdateTask.detach();
     
     if (loginTask != null) {
       loginTask.detach();
       return loginTask;
     }
     else if (appsTask != null) {
       appsTask.detach();
       return appsTask;
     }
     else if (appTask != null) {
       appTask.detach();
       return appTask;
     }
     else {
       return null;
     }
   }
 
   private static void moveViewBelowOrBesideHeader(Activity activity, int viewID, int headerID, float offset) {
     ViewGroup headerView = (ViewGroup)activity.findViewById(headerID); 
     View view = (View)activity.findViewById(viewID);
     float density = activity.getResources().getDisplayMetrics().density; 
     RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, activity.getWindowManager().getDefaultDisplay().getHeight() - headerView.getHeight() + (int)(offset * density));
     if (((String)view.getTag()).equalsIgnoreCase("right")) {
       layoutParams.addRule(RelativeLayout.RIGHT_OF, headerID);
       layoutParams.setMargins(-(int)(offset * density), 0, 0, (int)(10 * density));
     }
     else {
       layoutParams.addRule(RelativeLayout.BELOW, headerID);
       layoutParams.setMargins(0, -(int)(offset * density), 0, (int)(10 * density));
     }
     view.setLayoutParams(layoutParams);
   }
 
   private Dialog createLoginDialog() {
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setTitle("Sign In");
 
     builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
         String email = ((EditText)alert.findViewById(R.id.email_field)).getText().toString();
         String password = ((EditText)alert.findViewById(R.id.password_field)).getText().toString();
 
         loginTask = new LoginTask(MainActivity.this, email, password);
         loginTask.execute();
 
         setStatus("Signing in");
       }
     });
 
     builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
       }
     });
 
     alert = builder.create();
 
     LayoutInflater inflater = alert.getLayoutInflater();
     View view = inflater.inflate(R.layout.login_view, null, false);
 
     alert.setView(view);
 
     return alert;
   }
 
   private String getAPIToken() {
     SharedPreferences preferences = getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
     return preferences.getString("APIToken", null);
   }
 
   private void setAPIToken(String token) {
     SharedPreferences preferences = getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
     SharedPreferences.Editor editor = preferences.edit();
     editor.putString("APIToken", token);
     editor.commit();
   }
 
   private void setStatus(String status) {
     TextView statusLabel = (TextView)findViewById(R.id.status_label);
     statusLabel.setText(status);
   }
 
   public void loginWasSuccesful(String token) {
     loginTask = null;
     setAPIToken(token);
     getApps(token);
   }
 
   public void loginFailed() {
     loginTask = null;
     Toast.makeText(this, R.string.login_view_failed_toast, Toast.LENGTH_LONG).show();
     showDialog(DIALOG_LOGIN);
     setStatus(getResources().getString(R.string.main_view_signed_out_label));
   }
 
   public void didFailToReceiveApps() {
     setStatus("Connection failed. Please try again or check your credentials.");
   }
 
   public void didReceiveApps(JSONArray apps) {
     this.apps = apps;
     
     if (apps.length() == 0) {
       setStatus("No apps found.");
     }
     else {
       ArrayList<JSONObject> androidApps = new ArrayList<JSONObject>();
 
       int count = 0;
       for (int index = 0; index < apps.length(); index++) {
         try {
           JSONObject app = apps.getJSONObject(index);
           if (((app.has("platform")) && (app.getString("platform").equals("Android"))) &&
               ((app.has("release_type")) && (app.getInt("release_type") == 0))) {
             count++;
 
             androidApps.add(app);
           }
         }
         catch (JSONException e) {
         }
       }
 
       ListView listView = (ListView)findViewById(R.id.list_view);
       if (count == 0) {
         listView.setVisibility(View.INVISIBLE);
         setStatus("No apps found.");
       }
       else {
         appsAdapter = new AppsAdapter(this, androidApps);
 
         listView.setVisibility(View.VISIBLE);
         listView.setAdapter(appsAdapter);
         listView.setOnItemClickListener(this);
         setStatus("");
       }
     }
   }
 
   @Override
   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     if (selectedAppView != null) {
       ProgressBar progressBar = (ProgressBar)selectedAppView.findViewById(R.id.progress_bar);
       progressBar.setVisibility(View.INVISIBLE);
     }
 
     if (appTask != null) {
       appTask.cancel(true);
       appTask = null;
     }
 
     selectedAppIndex = position;
     selectedAppView = view;
     
     ProgressBar progressBar = (ProgressBar)selectedAppView.findViewById(R.id.progress_bar);
     progressBar.setVisibility(View.VISIBLE);
 
     JSONObject app = (JSONObject)appsAdapter.getItem(position);
     try {
       String identifier = app.getString("public_identifier");
       appTask = new AppTask(this, "https://rink.hockeyapp.net/", identifier);
       appTask.execute();
     }
     catch (JSONException e) {
       progressBar.setVisibility(View.INVISIBLE);
     }
   }
 
   public void didReceiveAppInfo(JSONArray updateInfo, String apkURL) {
     if (selectedAppView != null) {
       ProgressBar progressBar = (ProgressBar)selectedAppView.findViewById(R.id.progress_bar);
       progressBar.setVisibility(View.INVISIBLE);
     }
 
     if (updateInfo != null) {
       JSONObject app = (JSONObject)appsAdapter.getItem(selectedAppIndex);
       try {
         String identifier = app.getString("public_identifier");
         String title = app.getString("title");
 
         Intent intent = new Intent(this, AppActivity.class);
         intent.putExtra("identifier", identifier);
         intent.putExtra("title", title);
         intent.putExtra("json", updateInfo.toString());
         intent.putExtra("url", apkURL);
         startActivity(intent);
       }
       catch (JSONException e) {
       }
     }
   }
 }
