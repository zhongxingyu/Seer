 package ru.securelayer.rad.ando;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.content.SharedPreferences;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.Window;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.gesture.Gesture;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureOverlayView;
 import android.gesture.Prediction;
 import android.gesture.GestureOverlayView.OnGesturePerformedListener;
 
 import com.kaloer.filepicker.FilePickerActivity;
 import com.google.ads.AdView;
 import com.google.ads.AdSize;
 import com.google.ads.AdRequest;
 import org.fedorahosted.tennera.jgettext.Catalog;
 import org.fedorahosted.tennera.jgettext.Message;
 import org.fedorahosted.tennera.jgettext.catalog.parse.ExtendedCatalogParser;
 import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
 import org.fedorahosted.tennera.jgettext.PoWriter;
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 
 import ru.securelayer.rad.ando.R;
 
 public class GettextActivity extends Activity
     implements OnGesturePerformedListener, TextWatcher
 {
     private static final String PREFERENCE_FILE = "AndoGettextResourceEditor";
     private static final String RESOURCE_FILENAME_KEY = "RESOURCE_FILENAME";
     private static final String RESOURCE_POSITION_KEY = "RESOURCE_POSITION";
     private static final String STATE_POSITION_KEY = "MSG_POSITION";
     private static final String STATE_RESOURCE_KEY = "MSG_RESOURCE";
 
 
     private static final int REQUEST_PICK_FILE = 1;
 
     private static final int MSG_CURRENT = 0;
     private static final int MSG_NEXT = 1;
     private static final int MSG_NEXT_FUZZY = 2;
     private static final int MSG_NEXT_TRANS = 3;
 
     private TextView widgetMsgId = null;
     private EditText widgetMsgStr = null;
     private AdView adView = null;
 
     private String resourceFileName = null;
     private Catalog catalog = null;
     private ArrayList<Message> messages = null;
     private Message token = null;
     private boolean isCatalogReady = false;
 
     private Boolean procTextChanged = false;
 
     private GestureLibrary mLibrary;
     private GestureOverlayView gestures;
 
     private int index = 0;
 
     private int msgTotal = 0;
     private int msgTrans = 0;
     private int msgFuzzy = 0;
     private int msgTrash = 0;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle inState) {
         super.onCreate(inState);
 
         requestWindowFeature(Window.FEATURE_LEFT_ICON);
         requestWindowFeature(Window.FEATURE_PROGRESS);
 
         setContentView(R.layout.main);
 
         getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
 
         widgetMsgId = (TextView) findViewById(R.id.wMsgId);
         widgetMsgStr = (EditText) findViewById(R.id.wMsgStr);
         widgetMsgStr.addTextChangedListener(this);
 
         final View activityRootView = findViewById(R.id.main_layout);
         activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
             new OnGlobalLayoutListener() {
 
                 @Override
                 public void onGlobalLayout() {
                     Context ctx = getApplicationContext();
                     int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                     if (heightDiff > 100) {
                         hideAdv();
                     } else {
                         showAdv();
                     }
                 }
             }
         );
 
         this.messages = new ArrayList<Message>();
 
         this.mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
         if (!this.mLibrary.load()) {
             finish();
         }
         this.gestures = (GestureOverlayView) findViewById(R.id.gestures_overlay);
         this.gestures.addOnGesturePerformedListener(this);
 
         String title = (String) getTitle() + ": " + getString(R.string.resource_choose);
         setTitle(title);
     }
 
     protected void showAdv() {
         if (this.adView == null) {
             this.adView = new AdView(this, AdSize.BANNER, getString(R.string.admob_publisher_id));
             LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
             layout.addView(adView);
             adView.loadAd(new AdRequest());
         }
     }
 
     protected void hideAdv() {
         if (this.adView != null) {
             this.adView.destroy();
             this.adView.setVisibility(View.GONE);
             this.adView = null;
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle state) {
         if (isCatalogReady) {
             state.putString(STATE_RESOURCE_KEY, this.resourceFileName);
             state.putInt(STATE_POSITION_KEY, this.index);
             this.saveCatalog();
         }
         super.onSaveInstanceState(state);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle state) {
         if (! isCatalogReady) {
             this.resourceFileName = state.getString(STATE_RESOURCE_KEY);
             this.index = state.getInt(STATE_POSITION_KEY, 0);
             this.loadCatalogSeek(this.resourceFileName, this.index);
         }
         super.onRestoreInstanceState(state);
     }
 
     public boolean writeInstanceState(Context c) {
         this.saveCatalog();
         SharedPreferences pref = c.getSharedPreferences(GettextActivity.PREFERENCE_FILE, MODE_WORLD_READABLE);
         SharedPreferences.Editor editor = pref.edit();
         editor.putString(RESOURCE_FILENAME_KEY, this.resourceFileName);
         editor.putInt(RESOURCE_POSITION_KEY, this.index);
         return (editor.commit());
     }
 
     public boolean readInstanceState(Context c) {
         SharedPreferences pref = c.getSharedPreferences(GettextActivity.PREFERENCE_FILE, MODE_WORLD_READABLE);
         if (pref.contains(RESOURCE_FILENAME_KEY) && pref.contains(RESOURCE_POSITION_KEY)) {
             this.resourceFileName = pref.getString(RESOURCE_FILENAME_KEY, "");
             this.index = pref.getInt(RESOURCE_POSITION_KEY, 0);
             if (! this.resourceFileName.equals("")) {
                 this.loadCatalogSeek(this.resourceFileName, this.index);
             }
             return true;
         }
         return false;
     }
 
     @Override
     public void onDestroy() {
         this.hideAdv();
         super.onDestroy();
     }
 
     /**
      * Saves current state of UI and variables.
      */
     @Override
     protected void onPause() {
         super.onPause();
         if (isCatalogReady) {
             this.writeInstanceState(this);
         }
     }
 
     /**
      * Loads last state of UI and variables.
      */
     @Override
     protected void onResume() {
         super.onResume();
         if (! isCatalogReady) {
             readInstanceState(this);
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         final android.content.Intent intent = getIntent();
 
         if (intent != null) {
             final android.net.Uri data = intent.getData();
             if (data != null) {
                 resourceFileName = data.getEncodedPath();
                 loadCatalog(resourceFileName);
             }
         }
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
             // landscape orientation
         } else {
             // portrait orientation
         }
         if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
             // keyboard is active
         } else {
             // keyboard is hidden
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_open:
                 this.chooseAndOpen();
                 return true;
             case R.id.menu_copy:
                 this.msgstrCopy();
                 return true;
             case R.id.menu_fuzzy:
                 this.msgstrFuzzy();
                 return true;
             case R.id.menu_help:
                 this.showHelp();
                 return true;
         }
         return false;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             switch(requestCode) {
             case REQUEST_PICK_FILE:
                 if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
                     resourceFileName = data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH);
                     loadCatalog(resourceFileName);
                 }
             }
         }
     }
 
     protected void chooseAndOpen() {
         // create an intent for the file picker activity
         Intent intent = new Intent(this, FilePickerActivity.class);
         // set the initial directory to be the sdcard
         intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
         // only make .po files visible
         ArrayList<String> extensions = new ArrayList<String>();
         extensions.add(".po");
         intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, extensions);
         // start the activity
         startActivityForResult(intent, REQUEST_PICK_FILE);
     }
 
     protected void showHelp() {
         Intent intent = new Intent(this, HelpActivity.class);
         startActivity(intent);
     }
 
     public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
         ArrayList predictions = this.mLibrary.recognize(gesture);
 
         // We want at least one prediction
         if (predictions.size() > 0) {
             Prediction prediction = (Prediction) predictions.get(0);
             if (prediction.score > 1.0) {
                 Message msg = null;
                 String action = prediction.name;
                 if ("next".equals(action)) {
                     msg = this.getNext(+1, MSG_NEXT);
                 } else if ("prev".equals(action)) {
                     msg = this.getNext(-1, MSG_NEXT);
                 } else if ("next_fuzzy".equals(action)) {
                     msg = this.getNext(+1, MSG_NEXT_FUZZY);
                 } else if ("prev_fuzzy".equals(action)) {
                     msg = this.getNext(-1, MSG_NEXT_FUZZY);
                 } else if ("next_untranslated".equals(action)) {
                     msg = this.getNext(+1, MSG_NEXT_TRANS);
                 } else if ("prev_untranslated".equals(action)) {
                     msg = this.getNext(-1, MSG_NEXT_TRANS);
                 }
                 if (msg != null) {
                     this.fillMsgWidgets(msg);
                 }
             }
         }
     }
 
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
     public void onTextChanged(CharSequence s, int start, int before, int count) {}
     public void afterTextChanged(Editable s) {
         if (this.isCatalogReady) {
             if (! this.procTextChanged) {
                 this.procTextChanged = true;
                 this.token.setMsgstr(s.toString());
                 this.procTextChanged = false;
             }
         }
     }
 
     /**
      * Loads the messages catalog.
      *
      * @param fileName The full path to resource file on filesystem.
      */
     protected void loadCatalog(String fileName) {
         this.loadCatalogSeek(fileName, 0);
     }
     protected void loadCatalogSeek(String fileName, int position) {
         this.catalog = null;
         this.messages.clear();
         try {
             try {
                 File poFile = new File(fileName);
                 // Parse a file
                 ExtendedCatalogParser parser = new ExtendedCatalogParser(poFile);
                 try {
                     parser.catalog();
                 } catch (RecognitionException ex) {
                 } catch (TokenStreamException ex) {
                 }
                 this.catalog = parser.getCatalog();
                 // Iterate of file's items.
                 for (Message m : this.catalog) {
                     if (! m.isHeader()) {
                         this.messages.add(m);
                     }
                 }
                 this.isCatalogReady = true;
                // Position in catalog
                if (position > this.messages.size()) {
                    this.index = 0;
                } else {
                    this.index = position;
                }
                 this.fillMsgWidgets(this.getNext(0, MSG_CURRENT));
             } catch(FileNotFoundException ex) {}
         } catch(IOException ex) {}
     }
 
     /**
      * Saves the messages catalog.
      *
      * @param fileName The full path to resource file on filesystem.
      */
     protected void saveCatalog() {
         CharSequence msg;
         if (! this.isCatalogReady) {
             msg = getString(R.string.resource_ready_not);
         } else {
             PoWriter writer = new PoWriter();
             try {
                 File poFile = new File(this.resourceFileName);
                 writer.write(catalog, poFile);
                 msg = getString(R.string.resource_saved);
             } catch(IOException ex) {
                 msg = getString(R.string.resource_saved_not);
             }
         }
         showNotification(msg);
     }
 
     protected int cycleIndex(int current, int increment, int size) {
         int pointer = current;
         pointer += increment;
         if (0 > pointer)
             pointer = size - 1;
         if (size == pointer)
             pointer = 0;
         return pointer;
     }
 
     protected Message getNext(int increment, int action) {
         int notify = 0;
         int size = this.messages.size();
         boolean found = false;
 
         if (! this.isCatalogReady) {
             this.showNotification("No Messages!");
             return null;
         }
 
         if (MSG_CURRENT == action) {}
         if (MSG_NEXT == action) {
             this.index = this.cycleIndex(this.index, increment, size);
             notify = (-1 == increment) ? R.string.prev_token : R.string.next_token;
         }
         if (MSG_NEXT_FUZZY == action) {
             int pointer = this.cycleIndex(this.index, increment, size);
             do {
                 Message msg = this.messages.get(pointer);
                 if (msg.isFuzzy()) {
                     break;
                 }
                 pointer = this.cycleIndex(pointer, increment, size);
             } while (pointer != this.index);
             this.index = pointer;
             notify = (-1 == increment) ? R.string.prev_token_fuzzy : R.string.next_token_fuzzy;
         }
         if (MSG_NEXT_TRANS == action) {
             int pointer = this.cycleIndex(this.index, increment, size);
             do {
                 Message msg = this.messages.get(pointer);
                 if ("".equals(msg.getMsgstr())) {
                     break;
                 }
                 pointer = this.cycleIndex(pointer, increment, size);
             } while (pointer != this.index);
             this.index = pointer;
             notify = (-1 == increment) ? R.string.prev_token_trans : R.string.next_token_trans;
         }
         if (0 < notify)
             this.showNotification(getString(notify));
         return this.messages.get(this.index);
     }
 
     protected void fillMsgWidgets(Message message) {
         this.token = message;
         this.widgetMsgId.setText(message.getMsgid());
         this.widgetMsgStr.setText(message.getMsgstr());
         this.updateTitle();
     }
 
     /**
      * Copies the content from original widget into translated one.
      */
     protected void msgstrCopy() {
         if (this.token != null) {
             this.widgetMsgStr.setText(this.widgetMsgId.getText());
         }
     }
 
     protected void msgstrFuzzy() {
         if (this.token != null) {
             boolean state = this.token.isFuzzy();
             this.token.setFuzzy(! state);
             this.updateTitle();
             if (state) {
                 showNotification(getString(R.string.fuzzy_unset));
             } else {
                 showNotification(getString(R.string.fuzzy_set));
             }
         }
     }
 
     /**
      * Refreshes total/translated/fuzzy/obsolete counters.
      */
     protected void refreshCounters() {
         int total = 0;
         int trans = 0;
         int fuzzy = 0;
         int trash = 0;
 
         for (Message m : this.messages){
             total += 1;
             if (m.isFuzzy()) {
                 fuzzy += 1;
             }
             if (m.isObsolete()) {
                 trash += 1;
             }
             if (! m.getMsgstr().equals("")) {
                 trans += 1;
             }
         }
         this.msgTotal = total;
         this.msgTrans = trans;
         this.msgFuzzy = fuzzy;
         this.msgTrash = trash;
     }
 
     /**
      * Updates activity's title.
      */
     protected void updateTitle() {
         this.refreshCounters();
         setProgressBarVisibility(true);
         setProgress(this.msgTrans * 10000 / this.msgTotal);
         String fileName = new File(this.resourceFileName).getName();
         String msg = "[" +
             this.index + ":" +
             this.msgTotal + "/" +
             this.msgTrans + "/" +
             this.msgFuzzy + "/" +
             this.msgTrash + "] " +
             fileName;
         setTitle(msg);
     }
 
     /**
      * Shows the visible notification on device's screen.
      *
      * @param msg The message to be shown.
      */
     protected void showNotification(CharSequence msg) {
         Context ctx = getApplicationContext();
         int duration = Toast.LENGTH_SHORT;
         Toast toast = Toast.makeText(ctx, msg, duration);
         toast.show();
     }
 }
