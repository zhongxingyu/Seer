 /*******************************************************************************
  * Copyright 2012 Cian Mc Govern
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.cianmcgovern.android.ShopAndShare;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.cianmcgovern.android.ShopAndShare.Camera.TakePhoto;
 import com.cianmcgovern.android.ShopAndShare.Comparison.LoadFile;
 import com.cianmcgovern.android.ShopAndShare.DisplayResults.ListCreator;
 
 public class ShopAndShare extends Activity {
 
     private Button mCallPhoto, mCallLoad, mManual, mBrowser;
     public static Context sContext;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         // Static application wide context
         sContext = this.getApplicationContext();
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         try {
             prepareFiles();
         } catch (IOException e) {
             Log.e(Constants.LOG_TAG, "Unable to execute prepareFiles()");
             e.printStackTrace();
         }
 
         mCallPhoto = (Button) findViewById(R.id.callPhotoButton);
         mCallPhoto.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 if (!CheckFeatures.haveCamera())
                     new AlertDialog.Builder(sContext)
                             .setTitle(R.string.cameraRequired)
                             .setMessage(
                                     sContext.getText(R.string.cameraRequiredMessage))
                             .show();
                 else {
                     Intent photo = new Intent(ShopAndShare.sContext,
                             TakePhoto.class);
                     startActivity(photo);
                 }
             }
 
         });
         mCallLoad = (Button) findViewById(R.id.callLoadButton);
         mCallLoad.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Intent x = new Intent(sContext, LoadResults.class);
                 startActivity(x);
             }
 
         });
 
         mManual = (Button) findViewById(R.id.manualButton);
         mManual.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Intent in = new Intent(ShopAndShare.sContext, ListCreator.class);
                 startActivity(in);
             }
 
         });
 
         mBrowser = (Button) findViewById(R.id.webBrowser);
         mBrowser.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Intent in = new Intent(Intent.ACTION_VIEW, Uri
                         .parse(Constants.URL));
                 startActivity(in);
             }
 
         });
     }
 
     /**
      * Deletes any files that may conflict and creates the necessary files
      * 
      */
     private void prepareFiles() throws IOException {
 
         Constants.FILES_DIR = getFilesDir().toString();
 
         Constants.SAVE_DIR = Constants.FILES_DIR.concat("/saves");
         File saveDir = new File(Constants.SAVE_DIR);
         // Creates the saves directory if it doesn't exist
         if (!saveDir.exists()) {
             saveDir.mkdir();
             Log.w(Constants.LOG_TAG,
                     "Saves directory doesn't exists, creating!");
         }
 
         InputStream is;
         OutputStream os;
         BufferedInputStream bis;
         BufferedOutputStream bos;
         byte[] buf = new byte[1024];
         int n = 0;
         int o = 0;
 
         // Delete the wordlist if it exists then create and save it
         File wordList = new File(Constants.FILES_DIR.concat("/wordlist"));
         if (wordList.exists()) {
             wordList.delete();
             Log.w(Constants.LOG_TAG, "Wordlist exists, deleting!");
         }
         is = getResources().openRawResource(R.raw.wordlist);
         os = new FileOutputStream(wordList);
         bis = new BufferedInputStream(is);
         bos = new BufferedOutputStream(os);
         buf = new byte[1024];
         n = 0;
         o = 0;
         while ((n = bis.read(buf, o, buf.length)) > 0) {
             bos.write(buf, 0, n);
         }
         bis.close();
         bos.close();
         Constants.WORDLIST = LoadFile.fileToList(Constants.FILES_DIR
                 .concat("/wordlist"));
 
         // Delete the tesseract training data if it exists then create it
         File tessDir = new File(Constants.FILES_DIR + "/tessdata");
         if (tessDir.exists()) {
             tessDir.delete();
             Log.w(Constants.LOG_TAG,
                     "Tesseract data directory exists, deleting!");
         }
         tessDir.mkdir();
         File tessData = new File(Constants.FILES_DIR
                 + "/tessdata/eng.traineddata");
         is = getResources().openRawResource(R.raw.eng);
         os = new FileOutputStream(tessData);
         bis = new BufferedInputStream(is);
         bos = new BufferedOutputStream(os);
         buf = new byte[1024];
         n = 0;
         o = 0;
         while ((n = bis.read(buf, o, buf.length)) > 0) {
             bos.write(buf, 0, n);
         }
         bis.close();
         bos.close();
 
         // Delete any image files that may exist from previous uses
         File image = new File(Constants.FILES_DIR + "/image.jpg");
         if (image.exists()) {
             image.delete();
             Log.w(Constants.LOG_TAG, "Image file found, deleting!");
         }
         image = new File(Constants.FILES_DIR + "/bgimage.tiff");
         if (image.exists()) {
             image.delete();
             Log.w(Constants.LOG_TAG, "TIFF image file found, deleting!");
         }
 
         Constants.UPLOADS = Constants.FILES_DIR + "/uploads";
         // Delete uploads directory if it exists and create it
         File uploadsDir = new File(Constants.UPLOADS);
         if (uploadsDir.exists()) uploadsDir.delete();
         uploadsDir.mkdir();
     }
 
     // Create options menu
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inf = getMenuInflater();
         inf.inflate(R.layout.default_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.exit:
             System.runFinalizersOnExit(true);
             System.exit(0);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
