 /*
  * Copyright (C) 2013 Shane Francis
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package com.buzz.bee.openir;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.text.InputType;
 import android.util.*;
 
 public class IRMainActivity extends Activity {
     
     private native int startIR();
     private native int stopIR();
     private native int learnKey(String filename);
     private native int sendKey(String filename);
 
     AlertDialog.Builder altDialog;
     
     static {
         System.loadLibrary("sonyopenir");
     }
     
      static String TAG = "OpenIR";
     
       static boolean irOnline = false;
       static String currentDevice;
       static String baseDir;
     
       irButton[] buttons;
       static int NUM_KEYS;
     
       android.widget.Switch learn;
       android.widget.ProgressBar spin;
       
       public volatile boolean isLearnMode = false;
       public volatile Vector<IRcmd> keyVector = new Vector<IRcmd>();
       Thread IRdataSend;
       
       public Vector<String> devices = new Vector<String>();
       
       public static IRMainActivity cContext;
       
       AlertDialog alertDialog;
 
        public void showLernMsg() { 
             
         if(cContext == null)return;
         cContext.runOnUiThread(new Runnable() {
             @Override
             public void run(){    
                 alertDialog.setMessage("Count To 3 ... Then Press Remote Key To Learn ...");
                 alertDialog.show();
             }
             });
                 
         }
         
         public void hideLernMsg() { 
             
             if(cContext == null)return;
             cContext.runOnUiThread(new Runnable() {
                 @Override
                 public void run(){               
                     alertDialog.hide();
 
                 }
             });
                 
         }
         
     protected void onResume() {
         startIR();
         super.onResume();
     }
 
         
     protected void onPause() {
         stopIR();
         super.onPause();
     }
     
     protected void onDestroy() {
         stopIR();
         super.onDestroy();
         
     }
     
     TextView idView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.ir_activity_irmain);
         
         baseDir = (Environment.getExternalStorageDirectory().getPath() + "/openir/");
         
         Log.i(TAG,"Base dir : " + baseDir);
         
         new File(baseDir).mkdir();
         
         updateDevice();    
         
         currentDevice = devices.get(0);
         idView = (TextView) findViewById(R.id.identify);        
 
         idView.setText(currentDevice);
 
         
         IRdataSend = new Thread(new IRCommmunicator());
                 
         IRdataSend.start();
         
         cContext = this;
         
         alertDialog = new AlertDialog.Builder(this).create();
         
         irDevice device = new irDevice(1,"TV 1",this);
 
         buttons = device.getButtonMap();
         NUM_KEYS = device.getButtonCount();
 
         learn = (android.widget.Switch) findViewById(R.id.leanswitch);
         
         for(int i=0; i < NUM_KEYS; i++) {
             buttons[i].setOnClickListener(btnHandle);
             
         }        
 
         learn.setOnCheckedChangeListener(learnCheck);
         learn.setChecked(false);
         
         altDialog= new AlertDialog.Builder(this);
         
         String[] chownIR = { "su", "-c", "chown system:sdcard_rw /sys/devices/platform/ir_remote_control/enable"};
         String[] chownHSL = { "su", "-c", "chown system:sdcard_rw /dev/ttyHSL2"};
 
             
         try {
             Runtime.getRuntime().exec(chownIR);
             Runtime.getRuntime().exec(chownHSL);
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         
         int status = 0;
         
         updateAvailButtons();
 
         if(!irOnline) {
             status = startIR();
             irOnline = true;
         }
         
         if (status < 1) {
             Log.e(TAG,"Error starting IR : " + status);
         }
     }
     
     private void resetIR() {
         
         stopIR();
         startIR();      
     }
     
   public void updateAvailButtons() {
       
         for (int i = 0; i < NUM_KEYS; i++) {                
             String fileName = ((baseDir + currentDevice +"/key_"+buttons[i].getName()+".bin"));
             boolean isAvailable = checkKeyExsists(fileName);
             buttons[i].setEnabled(isAvailable);
             
         }
       
   }
     
     OnCheckedChangeListener learnCheck = new OnCheckedChangeListener() {
 
         @Override
         public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
             
             if (!checked) {
                 
                 updateAvailButtons();
                                 
             }
             else {
                 for (int i = 0; i < NUM_KEYS; i++) {
                     buttons[i].setEnabled(true);
                     
                 }
             }
             
             
                         
         }
         
     };
        
     View.OnClickListener btnHandle = new View.OnClickListener() {
         public void onClick(View v) {
             boolean learnMode = learn.isChecked();
            
             irButton thisButton = (irButton) v;
             
             String keyName =thisButton.getName();
 
             String file = (baseDir + currentDevice +"/key_"+ keyName +".bin");
             
             Log.i(TAG, "Sending key "+ file);
 
             
             if(learnMode) {
                 keyVector.add(new IRcmd(file, true));           
 
             } else {
                 keyVector.add(new IRcmd(file, false));           
             }                
                             
         }
     };
     
     public class IRcmd{
         
         String file;
         boolean learn;
         
         IRcmd (String filename, boolean isLearn){
             file = filename;
             learn = isLearn;
         }
         
         String getFileName() {
             return file;
         }
         
         boolean isLearnCmd() {
             return learn;
         }
         
     }
   
     public class IRCommmunicator implements Runnable {
         
         String TAG = "IRCommmunicator";
         
         IRCommmunicator(){
             
         }
 
         @Override
         public void run() {        
                     
             while (true){
             
                 int status = 0;
                 if (keyVector.size() > 0) {
                     IRcmd cmd = keyVector.get(0);
                     String file = cmd.getFileName();
                     boolean isLearnMode = cmd.isLearnCmd();
                     keyVector.remove(0);
             
                     if(!isLearnMode) {
                         status = sendKey(file);
                        Log.i(TAG,"Send Key: " + file);
                        
                         } else { 
                             String[] RMFile = {"rm", file};
                         try {
                             Runtime.getRuntime().exec(RMFile);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                         IRMainActivity.cContext.showLernMsg();
                         status = learnKey(file);
                         IRMainActivity.cContext.hideLernMsg();
 
                         Log.i(TAG,"Learn Key: " + file);
                         
                     }
                 
                     try {
                         Thread.sleep(250);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     
                     if (status < 20) {
                         Log.e(TAG, "IR Comms Error !");
                     }
    
                 }
             }
         }
         
     }
     
     private boolean checkKeyExsists(String fileName) {
         
         File keyFile = new File(fileName);
         
         boolean canRead = keyFile.canRead();
         
         Log.d(TAG, "Can read file " + fileName + " : " + canRead);
         
         return canRead;
     }
        
     public void addDevice() {
         
         AlertDialog.Builder addDeviceInput = new AlertDialog.Builder(this);
         final EditText input = new EditText(this);
         input.setInputType(InputType.TYPE_CLASS_TEXT);
         
         addDeviceInput.setTitle("New Device Name");
         addDeviceInput.setView(input);        
         
         addDeviceInput.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 
                 
                 new File(baseDir+input.getText().toString()).mkdir();
                 
                 Log.i(TAG,"New device : "+ devices.toString());
                 
                 updateDevice();
                                 
             }
         });
         addDeviceInput.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.cancel();
             }
         });
         
         addDeviceInput.show();
     }
     
     
     
     public void switchDevice() {
         
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Select Device");
         builder.setItems(devices.toArray(new CharSequence[devices.size()]), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 Log.i(TAG, devices.get(item));
                 currentDevice = devices.get(item);
                 idView.setText(currentDevice);
                 updateAvailButtons();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
         
     }
     
 
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.irmain, menu);
         super.onCreateOptionsMenu(menu);
         return true;
     }
     
     public void updateDevice() {
         
         devices.clear();
         File dir = new File(baseDir);
         String[] dir_contents = dir.list();
         
        if (dir_contents == null) {
             
             
             new File(baseDir+"default").mkdir();
             
         }
         
         for (int i = 0; i < dir_contents.length; i++) {
             Log.i(TAG, "file found : " + dir_contents[i]);
             devices.add(dir_contents[i]);
             
         }        
     }
     
     public void deleteDevice() {
         
             
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("Select Device To Remove");
             builder.setItems(devices.toArray(new CharSequence[devices.size()]), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     
                     if (devices.size() < 2 || devices.get(item) == currentDevice) {
                         return;
                     }
                     String devName = devices.get(item);
                     
                     File dir = new File(baseDir+devName);
                     dir.delete();
   
                     updateDevice();
                     
                     currentDevice = devices.get(0);
                  }
             });
             AlertDialog alert = builder.create();
             alert.show();
                 
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.switchDevice:
                 switchDevice();
                 return true;
             case R.id.addDevice:
                 addDevice();
                 return true;
             case R.id.resetIR:
                 resetIR();
                 return true;
             case R.id.delDevice:
                 deleteDevice();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 }
