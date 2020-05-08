 package com.playlist.playlist_generator;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Extensions extends ListActivity {
     private MyDB mydb;
    private ArrayList<String> ExtList;
     private String LOG_TAG = "Extensions class:";
     private final String[] EXTENSIONS = { ".mp3", ".mid", ".wav", ".ogg", ".mp4", ".aac", ".flac", ".m4a" }; //Playable Extensions
     private ArrayList<File_types> file_types_container = new ArrayList<File_types>();
     private ArrayList<ArrayList<String>> Extensions_list = new ArrayList<ArrayList<String>>();
     private FileTypes_adapter ftArapter;
     private Dialog dialog;
     Context mcontext;
     final int MENU_DELETE = 1;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)  {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_file_extensions);
         mcontext = this;
 
         CreateListView();
     }
 
     public ArrayList<String> getExtensions(MyDB extdb){
         ArrayList<String> Extensions = new ArrayList<String>();
         SQLiteDatabase db = extdb.getWritableDatabase();
         try{
             Cursor c = db.query("Extensions",null,null,null,null,null,null);
 
             if (c.moveToFirst()) {
                 int Extensions_ColIndex = c.getColumnIndex("file_extensions");
                 int Check_field_ColIndex = c.getColumnIndex("check_field");
                 do {
                     Log.d(LOG_TAG, "Extension = " + c.getString(Extensions_ColIndex));
                     int check_value = c.getInt(Check_field_ColIndex);
                     Log.d(LOG_TAG, "Check value = " + check_value);
                     if(check_value==1){
                         Extensions.add(c.getString(Extensions_ColIndex));
                     }
                 } while (c.moveToNext());
                 c.close();
             }
         }catch (NullPointerException e){
             Log.d(LOG_TAG, "Table Extensions wasn't found, details:");
             e.printStackTrace();
         }
         if(Extensions.size() == 0) Extensions = ExtFirstLaunch(extdb);
 
         return Extensions;
     }
 
     public ArrayList<String> ExtFirstLaunch(MyDB extdb){
         Long rowID;
         ArrayList<String> Extensions = new ArrayList<String>();
         SQLiteDatabase db = extdb.getWritableDatabase();
 
         try{
             Cursor c = db.query("Extensions",null,null,null,null,null,null);
             ContentValues cv = new ContentValues();
             for(String ext : EXTENSIONS){
                 Extensions.add(ext);
                 cv.put("file_extensions", ext);
                 cv.put("check_field", 1);
                 rowID = db.insert("Extensions", null, cv);
                 Log.d(LOG_TAG, "row inserted, ID = " + rowID);
             }
         }catch (NullPointerException e){
             Log.d(LOG_TAG, "Table Extensions wasn't found, details:");
             e.printStackTrace();
         }
 
         return Extensions;
     }
 
     private ArrayList<ArrayList<String>> fill_ext_list(){
         mydb = new MyDB(this);
         ArrayList<ArrayList<String>> Extensions = new ArrayList<ArrayList<String>>();
         SQLiteDatabase db = mydb.getWritableDatabase();
         try{
             Cursor c = db.query("Extensions",null,null,null,null,null,null);
             if (c.moveToFirst()) {
                 int Extensions_ColIndex = c.getColumnIndex("file_extensions");
                 int Check_Field_ColIndex = c.getColumnIndex("check_field");
                 int i = 0;
                 do {
                     Log.d(LOG_TAG, "Extension = " + c.getString(Extensions_ColIndex));
                     Extensions.add(new ArrayList<String>());
                     Extensions.get(i).add(c.getString(Extensions_ColIndex));
                     if(c.getInt(Check_Field_ColIndex)==1)
                         Extensions.get(i).add("True");
                     else
                         Extensions.get(i).add("False");
                     i++;
                 } while (c.moveToNext());
                 c.close();
             }
         }catch (NullPointerException e){
             Log.d(LOG_TAG, "Table Extensions wasn't found, details:");
             e.printStackTrace();
         }
         if(Extensions.size() == 0) {
             ArrayList<String> TempExtensions;
 
             TempExtensions = ExtFirstLaunch(mydb);
             for(int i=0; i < TempExtensions.size(); i++){
                 Extensions.add(new ArrayList<String>());
                 Extensions.get(i).add(TempExtensions.get(i));
                 Extensions.get(i).add("True");
             }
         }
 
         return Extensions;
     }
 
     public void Add_file_type(View v){
         final EditText etFileType;
         Button btnCancel;
 
         dialog = new Dialog(this);
         dialog.setTitle(this.getResources().getString(R.string.File_extension));
         dialog.setContentView(R.layout.dialog_file_extesions);
         LinearLayout ll = (LinearLayout) dialog.findViewById(R.id.ll_file_extensions);
 
 
         etFileType = (EditText)ll.findViewById(R.id.dialog_etFileType);
         Button btnOK = (Button) ll.findViewById(R.id.dialog_ext_add);
         btnCancel = (Button) ll.findViewById(R.id.dialog_ext_cancel);
 
         btnOK.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String file_ext;
                 mydb = new MyDB(mcontext);
                 SQLiteDatabase db = mydb.getWritableDatabase();
                 try{
                     String columns[]={"file_extensions"};
                     String selection1;
                     file_ext = etFileType.getText().toString().toLowerCase();
                     selection1 = "file_extensions = ?";
                     String[] selection2 = {"." + file_ext};
                     Cursor c = db.query("Extensions",columns,selection1,selection2,null,null,null);
                     if (c.moveToFirst()) {
                         //Extension exists
                         Toast.makeText(getBaseContext(), getResources().getString(R.string.Ext_exist), Toast.LENGTH_LONG).show();
                     }
                     else {
                         if(check_file_type(file_ext)){
                             //add new extension
                             long rowID;
                             ContentValues cv = new ContentValues();
 
                             cv.put("file_extensions", "." + file_ext);
                             cv.put("check_field", 1);
                             rowID = db.insert("Extensions", null, cv);
                             Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                             CreateListView();
                         }
                         else{
                             //error alert
                             Toast.makeText(getBaseContext(), getResources().getString(R.string.Ext_wrong), Toast.LENGTH_LONG).show();
                         }
                         dialog.hide();
                     }
 
                 }
                 catch (NullPointerException e){
                     Log.d(LOG_TAG, "Table Extensions wasn't found, details:");
                     e.printStackTrace();
                 }
             }
         });
 
         btnCancel.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 dialog.hide();
             }
         }
         );
 
         dialog.show();
     }
 
     private boolean check_file_type(String s){
         if(s.equals("")) return false;
         Pattern p = Pattern.compile("^[A-Za-z0-9]+");
         Matcher m = p.matcher(s);
         return m.matches();
     }
 
     private void CreateListView(){
         Extensions_list.clear();
         file_types_container.clear();
 
         Extensions_list = fill_ext_list();
         for(int i=0;i<Extensions_list.size();i++){
             if (Extensions_list.get(i).get(1).equals("True")){
                 String file_type = Extensions_list.get(i).get(0);
                 file_types_container.add(new File_types(file_type,R.drawable.ic_launcher,true));
             }
             else {
                 String file_type = Extensions_list.get(i).get(0);
                 file_types_container.add(new File_types(file_type,R.drawable.ic_launcher,false));
             }
         }
         ftArapter = new FileTypes_adapter(this,file_types_container);
         ListView lvMain = (ListView) findViewById(android.R.id.list);
         lvMain.setAdapter(ftArapter);
         registerForContextMenu(lvMain);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         if (v.getId() == android.R.id.list) {
             AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
             menu.setHeaderTitle(Extensions_list.get(info.position).get(0));
             menu.add(Menu.NONE, MENU_DELETE, 0, getResources().getString(R.string.Delete));
 
         }
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
        //TODO
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         switch (item.getItemId()) {
             case MENU_DELETE:
                 String FileExt;
                 String selection1;
                 String columns[]={"file_extensions", "id_ext"};
 
                 if(Extensions_list.size()==1){
                     break;
                 }
 
                 FileExt = Extensions_list.get(info.position).get(0);
                 mydb = new MyDB(mcontext);
                 SQLiteDatabase db = mydb.getWritableDatabase();
                 selection1 = "file_extensions = ?";
                 String[] selection2 = {FileExt,};
                 try{
                     Cursor c = db.query("Extensions",columns,selection1,selection2,null,null,null);
                     if(c.moveToFirst()){
                         int idColIndex = c.getColumnIndex("id_ext");
                         int ExtRowId = c.getInt(idColIndex);
                         int delCount = db.delete("Extensions",selection1, selection2);
                         Log.d(LOG_TAG, "deleted rows = " + delCount);
                         Log.d(LOG_TAG, "deleted rowId = " + ExtRowId);
                     }
                 }
                 catch (Exception e){
                     e.printStackTrace();
                 }
                 CreateListView();
 
                 break;
         }
         return super.onContextItemSelected(item);
     }
 }
