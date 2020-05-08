 package com.toycode.pwmemo;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 
 /**
  * Class that handles when open a "*.pwmemo" file by implicit intents.
  */
 public class ImplicitIntentImportActivity extends Activity implements OnClickListener {
 
     private EditText mPasswordEdittext;
     enum ReadMethod { MERGE, INSERT };
     private ReadMethod mReadMethod = ReadMethod.MERGE;
    Uri mUri = null;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.implicit_intent_import);
         findViewById(R.id.import_button).setOnClickListener(this);
         findViewById(R.id.cancel_button).setOnClickListener(this);
         mPasswordEdittext = (EditText)findViewById(R.id.password_edittext);
         String action = getIntent().getAction();
         if (Intent.ACTION_VIEW.equals(action)) {
            mUri = getIntent().getData();
            if (mUri == null) {
                App.debugLog(this, "URI=null");
                finish();
             }
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (!PasswordManager.getInstance(this).isMainPasswordExist()) {
             Intent i = new Intent(this, DeclarMasterPasswordActivity.class);
             startActivityForResult(i, Const.REQUEST_TYPE.NEW);
         }
     }
     
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.import_button:
                 if (App.isEmptyTextView(mPasswordEdittext)) {
                     App.toastMessage(this, R.string.please_set_import_password);
                     return;
                } else {
                    try{
                        final InputStream inputStream = getContentResolver().openInputStream(mUri);
                        (new MasterPasswordInput(this) {
                            public void onTureMasterPassword() {
                                new ReadFileTask().execute(inputStream);
                            }
                        }).Ask();
                    } catch (FileNotFoundException e) {
                        App.toastMessage(this, R.string.file_not_found);
                    }
                 }
                 break;
             case R.id.cancel_button:
                 finish();
                 break;
         }
     }
 
     private class ReadFileTask extends AsyncTask<InputStream, Void, Boolean> {
         ProgressDialog mProgressDialog;
         int mErrorMessageId = 0;
         private final int READ_SIZE = 1024;
         
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             mProgressDialog = ProgressDialog.show(ImplicitIntentImportActivity.this, "", "Reading...");
         }
 
         @Override
         protected Boolean doInBackground(InputStream... inputStreams) {
             SQLiteDatabase db = (new PwMemoDbOpenHelper(ImplicitIntentImportActivity.this)).getReadableDatabase();
             if (db == null) {
                 throw new RuntimeException("db==null");
             }
 
             byte[] mainPasswod = PasswordManager.getInstance(ImplicitIntentImportActivity.this).getDecryptedMainPassword();
             if (mainPasswod == null) {
                 throw new RuntimeException("mainPasswod==null");
             }
             String json = "";
             try {
                 byte[] password = mPasswordEdittext.getText().toString().getBytes();
                 InputStream is = inputStreams[0];
                 ReadableByteChannel ch = Channels.newChannel(is);
                 ByteArrayOutputStream cryptBytesStream = new ByteArrayOutputStream();
                 byte [] bufferArray = new byte[READ_SIZE];
                 ByteBuffer buffer = ByteBuffer.wrap(bufferArray);
                 buffer.rewind();
                 int readSize = 0;
                 try {
                     while( (readSize = ch.read(buffer)) >= 0) {
                         if (readSize > 0) {
                             cryptBytesStream.write(bufferArray, 0, readSize);
                             buffer.rewind();
                         }
                     }
                 } catch (IOException e) {
                     ;
                 }
                 
                 byte[] cryptBytes = cryptBytesStream.toByteArray();
                 ch.close();
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
             }
 
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
                App.toastMessage(ImplicitIntentImportActivity.this, R.string.import_str);
            } else {
                 App.toastMessage(ImplicitIntentImportActivity.this, mErrorMessageId);
             }
             super.onPostExecute(result);
         }
      }
 }
