 package com.danesh.moveover;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class MoveOverActivity extends Activity implements OnCheckedChangeListener,OnClickListener,OnItemClickListener,OnItemLongClickListener {
     String sdcard = Environment.getExternalStorageDirectory().toString()+"/";
     String sourceFolder = "/mnt/sdcard/MoveOver/from/";
     String destFolder = "/mnt/sdcard/MoveOver/to/";
     TextView source,dest;
     ToggleButton service;
     ListView myList;
     Button add;
     ArrayList<String> results;
     static Map<String, ?> sharedMap;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         print("------------------------------------------------");
         setContentView(R.layout.main);
         source = (TextView)findViewById(R.id.source);
         dest = (TextView)findViewById(R.id.dest);
         source.setText(sourceFolder);
         dest.setText(destFolder);
         service = (ToggleButton)findViewById(R.id.toggleButton1);
         service.setChecked(LocalService.serviceRunning);
         service.setOnCheckedChangeListener(this);
         myList = (ListView)findViewById(R.id.listView1);
         myList.setOnItemClickListener(this);
         myList.setOnItemLongClickListener(this);
         add = (Button)findViewById(R.id.button1);
         add.setOnClickListener(this);
         setAdapter();
     }
 
     private void setAdapter() {
         results = new ArrayList<String>();
         SharedPreferences myPrefs = this.getSharedPreferences("storedArray", MODE_PRIVATE);
         sharedMap = myPrefs.getAll();
         for (String item : sharedMap.keySet()){
             results.add("Source : " + item + "\nDestination : " + sharedMap.get(item));
         }
         myList.setAdapter(new ArrayAdapter<String>(this, R.layout.row, R.id.label, results));
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         Intent mine = new Intent(this, LocalService.class);
         if (isChecked){
             startService(mine);
         }else{
             stopService(mine);    
         }
     }
     
     public static void print(String msg){
         System.out.println("Danny "+msg);
     }
 
     public void showToast(String msg) {
         Toast pop = Toast.makeText(this, msg, Toast.LENGTH_LONG);
         pop.show();
     }
 
     @Override
     public void onClick(View arg0) {
         File testsource = new File(source.getText().toString());
         File testdest = new File(dest.getText().toString());
        if (source.getText().charAt(source.length()-1)!='/'){
             source.setText(source.getText()+"/");
         }
        if (dest.getText().charAt(dest.getText().length()-1)!='/'){
             dest.setText(dest.getText()+"/");
         }
         if (!testsource.isDirectory()){
             showToast("Source directory invalid or is not a directory");
             return;
         }
         if (!testdest.isDirectory()){
             showToast("Destination directory invalid or is not a directory");
             return;
         }
         modifyPreference(1,0);
     }
 
     @Override
     public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
         String sourceText = results.get(arg2).split("\n")[0].replaceFirst("Source : ", "").trim();
         String destText = results.get(arg2).split("\n")[1].replaceFirst("Destination : ", "").trim();
         source.setText(sourceText);
         dest.setText(destText);
     }
 
     public void modifyPreference(int mode, int arg2){
         SharedPreferences myPrefs = this.getSharedPreferences("storedArray", MODE_PRIVATE);
         SharedPreferences.Editor prefsEditor = myPrefs.edit();
         if (mode == 1){
             prefsEditor.putString(source.getText().toString(), dest.getText().toString());
             dest.setText("");
             source.setText("");
         }else{
             String sourceText = results.get(arg2).split("\n")[0].replaceFirst("Source : ", "").trim();
             prefsEditor.remove(sourceText);
         }
         prefsEditor.commit();
         setAdapter();
     }
 
     @Override
     public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Are you sure you delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         modifyPreference(0,arg2);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                    }
                });
         builder.create().show();
         return false;
     }
 }
