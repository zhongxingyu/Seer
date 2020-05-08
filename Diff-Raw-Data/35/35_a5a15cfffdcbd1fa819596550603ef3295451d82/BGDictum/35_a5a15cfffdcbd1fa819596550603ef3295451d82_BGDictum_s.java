 package org.apelikecoder.bgdictum;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnDismissListener;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.CursorAdapter;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
public class BGDictum extends Activity implements DB, OnItemClickListener {
 
     private SQLiteDatabase db;
     private AutoCompleteTextView searchField;
     private WordView translation;
     private String dataPath;
 
     private int TRANSLATION_COLUMN_INDEX = -1;
 
     private static final int DLG_CONFIRM_DOWNLOAD = 0;
 
    Handler h = new Handler() {
        public void handleMessage(android.os.Message msg) {
            InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromInputMethod(searchField.getWindowToken(), 0);
            mgr.hideSoftInputFromInputMethod(searchField.getApplicationWindowToken(), 0);
        };
    };

     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         searchField = (AutoCompleteTextView) findViewById(R.id.search_edit_query);
         translation = (WordView) findViewById(R.id.description_text);

         initDataPath();
         setupDB();
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         Uri uri = intent.getData();
         if (uri != null) {
             System.out.println(uri.toString());
             String word = uri.getHost();
             searchField.setText(word);
             searchField.setSelection(word.length());
         }
     }
 
     private void setupDB() {
         boolean start_again = false;
         File f = new File(dataPath, DATABASE);
         if (!f.exists()) {
             showDialog(DLG_CONFIRM_DOWNLOAD);
             return;
         }
         try {
             db = initDB(f.getAbsolutePath());
             start_again = (db == null);
             if (!start_again) {
                 Cursor c = db.query(TABLE_WORDS, null, null, null, null, null, null);
                 CursorAdapter ca = new WordAdapter(this, c, db);
                 searchField.setAdapter(ca);
                 searchField.setOnItemClickListener(this);
 
                 c = db.query(TABLE_TRANSLATIONS, null, null, null, null, null, null);
                 TRANSLATION_COLUMN_INDEX = c.getColumnIndexOrThrow(COLUMN_TRANSLATION);
                 c.close();
             }
         } catch (SQLiteException ex) {
             ex.printStackTrace();
             start_again = true;
         }
         if (start_again) {
             String [] files = new File(dataPath).list();
             for (String s : files) {
                 f = new File(dataPath + File.separatorChar + s);
                 if (f.delete())
                     System.out.println(f.getAbsolutePath() + " is GONE");
             }
             setupDB();
         }
     }
 
     private void initDataPath() {
         char sep = File.separatorChar;
         String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                 + sep + "data" + sep + getPackageName() + sep;
         File f = new File(path);
         if (!f.exists())
             f.mkdirs();
         if (!f.canWrite()) {
             Toast.makeText(this, R.string.no_sdcard_warning, Toast.LENGTH_SHORT);
             f = getDir("dict", MODE_PRIVATE);
         }
         dataPath = f.getAbsolutePath();
         System.out.println("DATAPATH: " + dataPath);
     }
 
     private SQLiteDatabase initDB(String path) {
         System.out.println("openind db file " + path);
         SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
 
         StringBuilder query = new StringBuilder(CREATE_TABLE_QUERY);
 
         query.append(TABLE_GLOBAL_SETTINGS);
         query.append(START_TABLE);
         query.append(COLUMN_ID);
         query.append(TYPE_ID);
         query.append(SEPARATE_COLUMN);
         query.append(COLUMN_GLOBAL_SETTINGS_KEY);
         query.append(TYPE_TEXT);
         query.append(SEPARATE_COLUMN);
         query.append(COLUMN_GLOBAL_SETTINGS_VALUE);
         query.append(TYPE_TEXT);
         query.append(END_TABLE);
 
         db.execSQL(query.toString());
 
         return db;
     }
 
     @Override
     public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
         Cursor c = db.query(TABLE_TRANSLATIONS, null, COLUMN_WORD_ID + "='" + id + "'", null, null, null, null);
         if (c.moveToFirst()) {
             String s = c.getString(TRANSLATION_COLUMN_INDEX);
             translation.setText(s);
         }
         c.close();
        h.sendEmptyMessage(0);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Dialog dlg = null;
         switch (id) {
             case DLG_CONFIRM_DOWNLOAD:
                 dlg = new AlertDialog.Builder(this)
                     .setMessage(R.string.download_question)
                     .setPositiveButton(R.string.download, new OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                             startDownload();
                         }
                     })
                     .setNegativeButton(R.string.quit, new OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                             BGDictum.this.finish();
                         }
                     })
                     .create();
         }

         return dlg;
     }
 
     private void startDownload() {
         final Downloader dl = new Downloader(this, getString(R.string.dictionary_url),
                 dataPath + File.separatorChar + DATABASE);
         dl.start();
         dl.setOnDismissListener(new OnDismissListener() {
             @Override
             public void onDismiss(DialogInterface dialog) {
 
                 if (!dl.getResult()) {
                     finish("Download unsuccessfall");
                 } else {
                     setupDB();
                 }
             }
         });
     }
 
     private void finish(String reason) {
         Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
         finish();
     }
 
 }
