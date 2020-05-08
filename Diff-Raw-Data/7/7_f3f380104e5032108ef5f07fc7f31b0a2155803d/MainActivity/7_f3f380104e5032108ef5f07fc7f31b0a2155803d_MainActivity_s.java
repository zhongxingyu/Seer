 /**
 Copyright 2010 Roy Sindre Norangshol <roy.sindre@norangshol.no>
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  */
 package no.norrs.projects.andronary.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView.BufferType;
 import android.widget.Toast;
 import java.io.Serializable;
 import java.util.List;
 import no.norrs.projects.andronary.DirectoryListener;
 import no.norrs.projects.andronary.Globals;
 import no.norrs.projects.andronary.R;
 import no.norrs.projects.andronary.SearchResult;
 import no.norrs.projects.andronary.tasks.DictListTask;
 import no.norrs.projects.andronary.tasks.DictLookupTask;
 
 /**
  * MainActivity , as the name says..
  *
  * @author Roy Sindre Norangshol <roy.sindre@norangshol.no>
  */
 public class MainActivity extends Activity implements OnClickListener, DirectoryListener {
 
     private static final int MENU_SETTINGS = Menu.FIRST;
     private static final int MENU_ABOUT = Menu.FIRST + 1;
     private static final int DIALOG_SETTINGS = 0;
     private static final int DIALOG_ABOUT = 1;
     private static final int DIALOG_DICTIONARIES = 2;
     private EditText query = null;
     private ProgressDialog dialog = null;
     private DictLookupTask lookup = null;
     private DictListTask dictlist = null;
     private String lastQuery = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         query = (EditText) findViewById(R.id.query);
         findViewById(R.id.button_query).setOnClickListener(this);
         findViewById(R.id.button_dictonary).setOnClickListener(this);
         findViewById(R.id.button_clear).setOnClickListener(this);
 
         lookup = new DictLookupTask(this);
         dictlist = new DictListTask(this);
 
         lastQuery = (savedInstanceState == null) ? null : (String) savedInstanceState.getSerializable(Globals.FSEARCH);
         if (lastQuery == null) {
             Bundle extras = getIntent().getExtras();
             lastQuery = extras != null ? extras.getString(Globals.FSEARCH) : null;
         }
 
         updateDictionaryButton();
     }
 
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.button_clear: {
                 query.setText("");
                 return;
             }
 
             case R.id.button_dictonary: {
                 getDictionaries();
                 return;
             }
 
             case R.id.button_query: {
                 search(query.getText().toString());
                 return;
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, "Settings");
         menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, "About");
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_SETTINGS:
                 super.showDialog(DIALOG_SETTINGS);
                 return true;
             case MENU_ABOUT:
                 super.showDialog(DIALOG_ABOUT);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * @todo Fix this
      * @param e
      * @param title
      */
     public void showError(Exception e, String title) {
         dialog.dismiss();
     }
 
     /**
      * Receives SearchResult's from DictLookupTask
      *
      * @param results Results.
      * @see no.norrs.projects.dict.DirectoryListener
      * @see no.norrs.projects.dict.tasks.DictLookupTask
      */
     public void lookupResults(List<SearchResult> results) {
         lookup = null;
         dialog.dismiss();
         Intent intent = new Intent(this, SearchResultListView.class);
         intent.putExtra(Globals.SEARCHWORD, query.getText().toString());
         intent.putExtra(Globals.SEARCHRESULTS,
                 (Serializable) results);
         startActivity(intent);
     }
 
     /**
      * Receives available directories from Dictionary task service.
      * @param directories available directories for selected service
      * @see no.norrs.projects.dict.DirectoryListener
      * @see no.norrs.projects.dict.tasks.DictListTask
      */
     public void directoriesAvailable(String[] directories) {
         dictlist = null;
         dialog.dismiss();
        Bundle b = getIntent().getExtras();
        b.putStringArray(Globals.FDICTIONARIES, directories);
         super.showDialog(DIALOG_DICTIONARIES);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Dialog d = null;
 
         switch (id) {
             case DIALOG_SETTINGS: {
                 d = showSettingsDialog();
                 break;
             }
             case DIALOG_ABOUT: {
                 d = showAboutDialog();
                 break;
             }
             case DIALOG_DICTIONARIES: {
                String[] dicts = getIntent().getExtras().getStringArray(Globals.FDICTIONARIES);
                 d = showDictionariesDialog(dicts);
                 break;
             }
 
         }
         return d;
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         saveState();
         outState.putSerializable(Globals.FSEARCH, query.getText().toString());
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         saveState();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         loadState();
     }
 
     /**
      * Helper method for updating text for which directory is selected.
      */
     protected void updateDictionaryButton() {
         SharedPreferences settings = getSharedPreferences(Globals.PREFS, 0);
         ((Button) findViewById(R.id.button_dictonary)).setText(settings.getString(Globals.DICT, "Dict N/A"));
     }
 
     /**
      * Creates a settings dialog.
      */
     protected Dialog showSettingsDialog() {
         final SharedPreferences settings = getSharedPreferences(Globals.PREFS, 0);
         final SharedPreferences.Editor editor = settings.edit();
 
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         final View view = (View) inflater.inflate(R.layout.settings, null, true);
 
 
         Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Settings");
         builder.setView(view);
 
         ((EditText) view.findViewById(R.id.url)).setText(settings.getString(Globals.SERVICE, ""));
         ((EditText) view.findViewById(R.id.username)).setText(settings.getString(Globals.SUSERNAME, ""));
         ((EditText) view.findViewById(R.id.password)).setText(settings.getString(Globals.SPASSWORD, ""));
 
 
         builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
 
             public void onClick(DialogInterface dialog, int which) {
 
                 editor.putString(Globals.SERVICE, ((EditText) view.findViewById(R.id.url)).getText().toString().trim());
                 editor.putString(Globals.SUSERNAME, ((EditText) view.findViewById(R.id.username)).getText().toString().trim());
                 editor.putString(Globals.SPASSWORD, ((EditText) view.findViewById(R.id.password)).getText().toString().trim());
                 editor.commit();
                 Toast.makeText(MainActivity.this, "Settings saved!", 3000).show();
 
 
             }
         });
 
         builder.setNegativeButton("Cancel", null);
         return builder.create();
 
 
     }
 
     /**
      * Creates a dialog for picking a dictionary
      */
     protected Dialog showDictionariesDialog(final String directories[]) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Pick a dictonary");
         builder.setItems(directories, new DialogInterface.OnClickListener() {
 
             public void onClick(DialogInterface dialog, int item) {
                 SharedPreferences settings = getSharedPreferences(Globals.PREFS, 0);
                 SharedPreferences.Editor editor = settings.edit();
                 editor.putString(Globals.DICT, directories[item]);
                 editor.commit();
                 updateDictionaryButton();
             }
         });
 
         return builder.create();
     }
 
     protected Dialog showAboutDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("About Andronary");
         builder.setMessage("By Roy Sindre Norangshol\nhttp://www.roysindre.no\n\nProject website:\n http://github.com/norrs/Andronary\nLicence:\n http://www.apache.org/licenses/LICENSE-2.0.html");
         return builder.create();
     }
 
     private void getDictionaries() {
         dialog = ProgressDialog.show(MainActivity.this, null, "Loading available dictionaries. Please wait...", true);
         SharedPreferences settings = getSharedPreferences(Globals.PREFS, 0);
         dictlist = new DictListTask(this);
         dictlist.execute(settings.getString(Globals.SERVICE, null), settings.getString(Globals.SUSERNAME, null), settings.getString(Globals.SPASSWORD, null));
     }
 
     private void search(String query) {
         dialog = ProgressDialog.show(MainActivity.this, String.format("Lookup for %s", query), "Loading. Please wait...", true);
         SharedPreferences settings = getSharedPreferences(Globals.PREFS, 0);
         lookup = new DictLookupTask(this);
         lookup.execute(settings.getString(Globals.SERVICE, null), settings.getString(Globals.SUSERNAME, null), settings.getString(Globals.SPASSWORD, null), query, settings.getString(Globals.DICT, null));
 
     }
 
     private void saveState() {
     }
 
     private void loadState() {
         if (lastQuery != null) {
             query.setText(lastQuery);
         }
     }
 }
