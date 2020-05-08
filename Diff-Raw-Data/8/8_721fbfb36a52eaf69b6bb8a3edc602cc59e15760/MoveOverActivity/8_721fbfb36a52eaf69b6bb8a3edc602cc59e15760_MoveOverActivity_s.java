 package com.danesh.moveover;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class MoveOverActivity extends Activity implements OnCheckedChangeListener,OnClickListener,OnItemClickListener,OnItemLongClickListener {
     String sdcard = Environment.getExternalStorageDirectory().toString()+"/";
     String sourceFolder = "/mnt/sdcard/DCIM";
     String destFolder = "/mnt/sdcard/Test";
     EditText source,dest;
     ToggleButton service;
     ListView myList;
     Button add,chooseSource,chooseDest;
     static Map<String, ?> sharedMap;
 
     public static String getMap(String item) {
         return sharedMap.get(item).toString();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         print("------------------------------------------------");
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
         source = (EditText)findViewById(R.id.source);
         dest = (EditText)findViewById(R.id.dest);
         source.setText(sourceFolder);
         dest.setText(destFolder);
         service = (ToggleButton)findViewById(R.id.toggleService);
         service.setChecked(LocalService.serviceRunning);
         service.setOnCheckedChangeListener(this);
         myList = (ListView)findViewById(R.id.listView1);
         myList.setOnItemClickListener(this);
         myList.setOnItemLongClickListener(this);
         add = (Button)findViewById(R.id.add);
         add.setOnClickListener(this);
         chooseSource = (Button)findViewById(R.id.chooseSource);
         chooseSource.setOnClickListener(this);
         chooseDest = (Button)findViewById(R.id.chooseDest);
         chooseDest.setOnClickListener(this);
         setAdapter();
     }
 
     private void setAdapter() {
         SharedPreferences myPrefs = this.getSharedPreferences("storedArray", MODE_PRIVATE);
         sharedMap = myPrefs.getAll();
         myList.setAdapter(new IconicAdapter());
     }
 
     class IconicAdapter extends ArrayAdapter<Object> {
         IconicAdapter(){
            super(MoveOverActivity.this, android.R.layout.simple_list_item_1, sharedMap.values().toArray());
         }
 
         public View getView(int pos, View convertView, ViewGroup parent){
             LayoutInflater inf = getLayoutInflater();
             if(convertView==null){
                 convertView = inf.inflate(R.layout.row,null);
             }
             TextView label = (TextView) convertView.findViewById(R.id.source);
            label.setText(sharedMap.keySet().toArray()[0].toString());
             label = (TextView) convertView.findViewById(R.id.destination);
            label.setText(sharedMap.values().toArray()[0].toString());
             return convertView;
         }
 
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         if (sharedMap.isEmpty()){
             showToast("Nothing is being tracked");
             service.setChecked(!isChecked);
             return;
         }
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
 
     private boolean appInstalledOrNot(Intent intent)
     {
         List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
         return list.size() > 0;
     }
 
     @Override
     public void onClick(View arg0) {
         if (arg0==chooseSource || arg0 == chooseDest){
             Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
             if (appInstalledOrNot(intent)){
                 startActivityForResult(intent, arg0 == chooseSource ? 0 : 1);
             }else{
                 showToast("OI file manager not found");
                 intent = new Intent( Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:org.openintents.filemanager"));
                 startActivity(intent);
             }
         }else if (arg0 == add){
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
             modifyPreference(1,"");
         }
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (data!=null){
             if (requestCode == 0){
                 source.setText(data.getData().getPath()+"/");
             }else{
                 dest.setText(data.getData().getPath()+"/");
             }
         }
     }
 
     @Override
     public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
         TextView sourceView = (TextView) arg1.findViewById(R.id.source);
         TextView destView = (TextView) arg1.findViewById(R.id.destination);
         String sourceText = sourceView.getText().toString();
         String destText = destView.getText().toString();
         source.setText(sourceText);
         dest.setText(destText);
     }
 
     public void modifyPreference(int mode, String preference){
         SharedPreferences myPrefs = this.getSharedPreferences("storedArray", MODE_PRIVATE);
         SharedPreferences.Editor prefsEditor = myPrefs.edit();
         if (mode == 1){
             prefsEditor.putString(source.getText().toString(), dest.getText().toString());
             dest.setText("");
             source.setText("");
         }else{
             prefsEditor.remove(preference);
         }
         prefsEditor.commit();
         setAdapter();
     }
 
     @Override
     public boolean onItemLongClick(AdapterView<?> arg0, final View arg1, final int arg2, long arg3) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Are you sure you delete?")
         .setCancelable(false)
         .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 TextView sourceView = (TextView) arg1.findViewById(R.id.source);
                 String sourceText = sourceView.getText().toString();
                 modifyPreference(0,sourceText);
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
