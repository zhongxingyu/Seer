 /*
  * Copyright (c) 2011 Hiroshi Okada (http://toycode.com/hiroshi/)
  * 
  * This software is provided 'as-is', without any express or implied
  * warranty. In no event will the authors be held liable for any damages
  * arising from the use of this software.
  * 
  * Permission is granted to anyone to use this software for any purpose,
  * including commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  * 
  *   1.The origin of this software must not be misrepresented; you must
  *     not claim that you wrote the original software. If you use this
  *     software in a product, an acknowledgment in the product
  *     documentation would be appreciated but is not required.
  *   
  *   2.Altered source versions must be plainly marked as such, and must
  *     not be misrepresented as being the original software.
  *   
  *   3.This notice may not be removed or altered from any source
  *     distribution.
  */
 
 package com.toycode.pwmemo;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.List;
 
 /**
  * The Activity for import data.
  * 
  * @author Hiroshi Okada
  *
  */
 public class ImportActivity extends Activity implements OnClickListener {
 
     public static final String OI_ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";
     public static final String OI_TITLE_EXTRA = "org.openintents.extra.TITLE";
     public static final String OI_BUTTON_TEXT_EXTRA = "org.openintents.extra.BUTTON_TEXT";
     public static final int REQUEST_FILENAME = 0;
 
     private boolean mUseFileManeger;
     private EditText mPasswordEdittext;
     private EditText mFileNameEdittext;
     private TextView mReadMethodInfoTextview;
     
     enum ReadMethod { MERGE, INSERT };
     private ReadMethod mReadMethod = ReadMethod.MERGE;
     
     private App mApp;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mApp = App.GetApp(this);
         // If locked then finish
         if (PasswordManager.getInstance(this).isMainPasswordDecrypted() == false) {
             setResult(RESULT_CANCELED, new Intent());
             finish();
             return;
         }
         if (checkOIActionPickFile()) {
             setContentView(R.layout.import_layout);
             mUseFileManeger = true;
         } else {
             setContentView(R.layout.import_input_filename);
             mUseFileManeger = false;
             mFileNameEdittext = (EditText) findViewById(R.id.filename_edittext);
             mFileNameEdittext.setText(mApp.getDefaultInputFile().toString());
         }
         mPasswordEdittext = (EditText) findViewById(R.id.import_password_edittext);
         findViewById(R.id.read_file_button).setOnClickListener(this);
         findViewById(R.id.merge_radio).setOnClickListener(this);
         findViewById(R.id.insert_radio).setOnClickListener(this);
         mReadMethodInfoTextview = (TextView)findViewById(R.id.read_method_info_textview);
     }
 
     private boolean checkOIActionPickFile() {
         Intent intent = new Intent(OI_ACTION_PICK_FILE);
         PackageManager pm = getPackageManager();
         List<ResolveInfo> list = pm
                 .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
         return list.size() > 0;
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.read_file_button:
                 if (App.isEmptyTextView(mPasswordEdittext)) {
                     mApp.toastMessage(R.string.please_set_import_password);
                     return;
                 }
                 if (mUseFileManeger) {
                     startReadFileManager();
                 } else {
                     File file = new File(mFileNameEdittext.getText().toString());
                     if (file != null) {
                         new ReadFileTask().execute(file);
                     }
                 }
                 break;
             case R.id.merge_radio:
                 mReadMethodInfoTextview.setText(R.string.merge_info);
                 mReadMethod = ReadMethod.MERGE;
                 break;
             case R.id.insert_radio:
                 mReadMethodInfoTextview.setText(R.string.insert_info);
                 mReadMethod = ReadMethod.INSERT;
                 break;
             default:
                 // did not come
                 break;
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_FILENAME && resultCode == RESULT_OK && data != null) {
             Uri uri = data.getData();
             if (uri != null) {
                 File file = new File(uri.getPath());
                 if (file != null) {
                     new ReadFileTask().execute(file);
                 }
             }
         }
     }
     
     
     private class ReadFileTask extends AsyncTask<File, Void, Boolean> {
         ProgressDialog mProgressDialog;
         int mErrorMessageId = 0;
         
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             mProgressDialog = ProgressDialog.show(ImportActivity.this, "", "Reading...");
         }
 
         @Override
         protected Boolean doInBackground(File... files) {
             SQLiteDatabase db = (new PwMemoDbOpenHelper(ImportActivity.this)).getReadableDatabase();
             if (db == null) {
                 throw new RuntimeException("db==null");
             }
 
             byte[] mainPasswod = PasswordManager.getInstance(ImportActivity.this).getDecryptedMainPassword();
             if (mainPasswod == null) {
                 throw new RuntimeException("mainPasswod==null");
             }
             String json = "";
             try {
                 byte[] password = mPasswordEdittext.getText().toString().getBytes();
                 FileInputStream fis = new FileInputStream(files[0]);
                 FileChannel ch = fis.getChannel();
                 int size = (int) ch.size();
                 byte[] cryptBytes = new byte[size];
                 ch.read(ByteBuffer.wrap(cryptBytes));
                 ch.close();
                 fis.close();
                 byte[] bytesData = OpenSSLAES128CBCCrypt.INSTANCE.decrypt(password, cryptBytes);
                 if (bytesData == null) {
                     mErrorMessageId = R.string.password_does_not_match;
                     return false;
                 }
                 json = new String(bytesData);
             } catch (FileNotFoundException e) {
                 mErrorMessageId = R.string.file_not_found;
                 return false;
             } catch (IOException e) {
                 mErrorMessageId = R.string.file_read_error;
                 return false;
             } catch (CryptException e){
                 mErrorMessageId = e.GetMsgId();
                 return false;
             }
 
            mApp.setDefaultOutputFile(files[0]);
             DbRw dbrw = new DbRw(db, mainPasswod);
             switch (mReadMethod) {
                 case MERGE:
                     dbrw.insertRecords(json);
                     break;
                 case INSERT:
                     dbrw.mergeRecords(json);
                     break;
                 default:
                     break;
             }
             dbrw.cleanup();
             return true;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             mProgressDialog.dismiss();
             if (result == false) {
                 mApp.toastMessage(mErrorMessageId);
             }
             super.onPostExecute(result);
             if (result == true) {
                 finish();
             }
         }
         
      }
  
     private void startReadFileManager() {
         Intent intent = new Intent(OI_ACTION_PICK_FILE);
        intent.setData(Uri.fromFile(mApp.getDefaultInputFile()));
         intent.putExtra(OI_TITLE_EXTRA, getString(R.string.select_folder));
         intent.putExtra(OI_BUTTON_TEXT_EXTRA, getString(R.string.read));
         try {
             startActivityForResult(intent, REQUEST_FILENAME);
         } catch (ActivityNotFoundException e) {
             throw new RuntimeException(e.getMessage());
         }
     }
 }
