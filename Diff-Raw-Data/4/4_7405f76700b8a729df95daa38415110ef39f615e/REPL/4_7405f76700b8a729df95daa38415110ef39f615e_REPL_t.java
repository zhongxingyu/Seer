 package com.folone.replcore;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.folone.Evaluator;
 
 public class REPL extends Activity {
 
     private static final String REPL = "repl";
     private static final String PREFS = "repl.prefs";
 
     private TextView result;
     private Spinner env;
     private Button eval;
     private EditText script;
     private ScrollView scroller;
 
     private Evaluator evaluator;
     private List<ResolveInfo> services;
     private List<String> serviceNames;
     
    private Resources res;
     protected final String TAG = "REPL-core";
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // Using app rater.
         AppRater.app_launched(this);
 
        res = getResources();
         result = (TextView) findViewById(R.id.result);
         env = (Spinner) findViewById(R.id.environment);
         eval = (Button) findViewById(R.id.eval);
         script = (EditText) findViewById(R.id.entry);
         scroller = (ScrollView) findViewById(R.id.scroller);
 
         try {
             scanAvaliableEnvs();
         } catch (Exception e) {
             result.append(e.getMessage());
         }
         try {
             bindToSelectedService();
         } catch (Exception e) {
             Toast.makeText(getApplicationContext(),
                     res.getString(R.string.no_packages_error),
                     Toast.LENGTH_LONG).show();
         }
 
         eval.setOnClickListener(new OnClickListener() {
             public void onClick(View arg0) {
                 SharedPreferences prefs = getSharedPreferences(PREFS, 0);
                 if (prefs.getBoolean("save_history", true)) {
                     DBHelper db = new DBHelper(getApplicationContext());
                     try {
                         db.persistScript(script.getText().toString().trim(),
                                 serviceNames.get((int) env.getSelectedItemId()));
                         Toast.makeText(getApplicationContext(),  res.getString(R.string.saved),
                                 Toast.LENGTH_SHORT).show();
                     } catch (IndexOutOfBoundsException e) {
                         Log.e(TAG,
                                 "No services installed, not persisting code.");
                         Toast
                                 .makeText(
                                         getApplicationContext(),
                                         res.getString(R.string.no_packages_error),
                                         Toast.LENGTH_LONG).show();
                     }
                 }
                 evaluate();
 
                 // Scroll down to the bottom
                 scroller.post(new Runnable() {
                     public void run() {
                         scroller.fullScroll(ScrollView.FOCUS_DOWN);
                     }
                 });
             }
         });
 
         env.setOnItemSelectedListener(new OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> arg0, View arg1,
                     int arg2, long arg3) {
                 try {
                     unbindService(connection);
                     bindToSelectedService();
                 } catch (Exception e) {
                     // XXX WTF
                 }
             }
 
             public void onNothingSelected(AdapterView<?> arg0) {
                 // XXX Dunno, when this happens
             }
         });
 
         SharedPreferences prefs = getSharedPreferences(REPL, 0);
         env.setSelection(serviceNames.indexOf(prefs
                 .getString("environment", "")));
         script.setText(prefs.getString("script", ""));
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onPause() {
         super.onPause();
     }
 
     public void onDestroy() {
         super.onDestroy();
         try {
             unbindService(connection);
         } catch (Exception e) {
             // Do nothing
         }
     }
 
     /**
      * Menu
      */
     public boolean onCreateOptionsMenu(Menu menu) {
         boolean result = super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.repl_menu, menu);
 
         return result;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.history_menu:
             Intent history = new Intent(getApplicationContext(), History.class);
             startActivity(history);
             return true;
         case R.id.menu_prefs:
             Intent preferences = new Intent(getApplicationContext(),
                     Preferences.class);
             startActivity(preferences);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * Evaluation
      */
     private void evaluate() {
         String scr = script.getText().toString().trim();
         if (scr != null && scr != "" && scr.length() > 0) {
             String helpMsg = res.getString(R.string.helpMsg);
             CharSequence lambda = getText(R.string.lambda);
             result.append(res.getString(R.string.input) + scr + res.getString(R.string.result));
             if (scr != null && !scr.equalsIgnoreCase("")) {
                 if (scr.equalsIgnoreCase("help")) {
                     result.append(helpMsg);
                 } else if (scr.equalsIgnoreCase("cls")
                         || scr.equalsIgnoreCase("clrscr")
                         || scr.equalsIgnoreCase("clear")) {
                     result.setText("");
                 } else {
                     try {
                         result.append(evaluator.evaluate(scr) + "\n");
                     } catch (RemoteException e) {
                         Toast.makeText(getApplicationContext(),
                                 res.getString(R.string.service_error),
                                 Toast.LENGTH_SHORT).show();
                         result.setText("");
                     } catch (NullPointerException e) {
                         Toast.makeText(
                                 getApplicationContext(),
                                 res.getString(R.string.no_packages_error),
                                 Toast.LENGTH_LONG).show();
                         result.setText("");
                     } catch (Exception e) {
                         // Probably no packages are installed
                         Toast.makeText(
                                 getApplicationContext(),
                                 res.getString(R.string.no_packages_error),
                                 Toast.LENGTH_LONG).show();
                         result.setText("");
                     }
                 }
                 result.append(lambda);
                 script.setText("");
             } else {
                 Toast.makeText(getApplicationContext(), res.getString(R.string.no_code_error),
                         Toast.LENGTH_SHORT).show();
             }
         }
     }
 
     /**
      * Binding to selected service
      * 
      * @return
      */
     private boolean bindToSelectedService() {
         final ResolveInfo selected = services
                 .get((int) env.getSelectedItemId());
         final Intent intent = new Intent() {
             {
                 setClassName(selected.serviceInfo.packageName,
                         selected.serviceInfo.name);
             }
         };
         return bindService(intent, connection, Context.BIND_AUTO_CREATE);
     }
 
     /**
      * Connection to service
      */
     private ServiceConnection connection = new ServiceConnection() {
 
         public void onServiceDisconnected(ComponentName name) {
             evaluator = null;
         }
 
         public void onServiceConnected(ComponentName name, IBinder service) {
             evaluator = Evaluator.Stub.asInterface(service);
         }
     };
 
     /**
      * Scanning available services
      */
     private void scanAvaliableEnvs() {
         Intent myIntent = new Intent() {
             {
                 addCategory("foloneREPL");
                 setAction("foloneREPL");
             }
         };
         PackageManager manager = getPackageManager();
         services = manager.queryIntentServices(myIntent,
                 PackageManager.GET_SERVICES);
 
         serviceNames = new ArrayList<String>();
         for (ResolveInfo service : services) {
             int index = service.serviceInfo.name.lastIndexOf(".") + 1;
             serviceNames.add(service.serviceInfo.name.substring(index)
                     .replaceAll("REPL", ""));
         }
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_spinner_item, serviceNames);
         adapter
                 .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         env.setAdapter(adapter);
     }
 }
