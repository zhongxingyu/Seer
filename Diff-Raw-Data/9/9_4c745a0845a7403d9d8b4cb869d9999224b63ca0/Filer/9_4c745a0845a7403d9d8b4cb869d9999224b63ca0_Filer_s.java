 package com.hlidskialf.android.filer;
 
 import android.app.ListActivity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.widget.ListView;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import android.widget.Button;
 import android.view.View;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.os.Bundle;
 import android.os.Environment;
 import android.net.Uri;
 
 import android.preference.PreferenceManager;
 import android.content.SharedPreferences;
 
 import java.util.GregorianCalendar;
 import java.util.Calendar;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.text.DecimalFormat;
 
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import java.io.File;
 import java.lang.String;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Hashtable;
 
 import android.util.Log;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 
 public class Filer extends ListActivity 
 {
     private static final int RESULT_PREFERENCES=1;
 
    private final DecimalFormat pNumFmt  = new DecimalFormat();
     private final SimpleDateFormat pDateFmt_time = new SimpleDateFormat("MMM dd HH:mm");
     private final SimpleDateFormat pDateFmt_year  = new SimpleDateFormat("MMM dd yyyy");
  
     /* broadcast receiver to check for sd card status */
     private BroadcastReceiver mReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) { fillData(mCurDir); }
     };
 
     private File mCurDir,mStartDir,mRootFile;
     private List<String> mCurFiles;
     private ArrayList<String> mYanked = new ArrayList();
 
     private boolean pIgnoreNextClick = false;
     private boolean mCreatingShortcut = false;
 
     private MenuItem mCopyItem,mMoveItem,mUnyankItem,mDeleteItem;
 
     private SharedPreferences mPrefs;
     private File mHomeFile;
     private boolean mBrowseRoot,mHideDot,mBackCd;
     private Hashtable<String,String> mMimeExt;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.filer);
 
       Intent intent = getIntent();
 
       mCreatingShortcut = Intent.ACTION_CREATE_SHORTCUT.equals(intent.getAction());
 
       if (mCreatingShortcut) {
         Toast t = Toast.makeText(this, "Longclick to create a shortcut", Toast.LENGTH_LONG);
         t.show();
       }
 
       String root_path = Environment.getExternalStorageDirectory().toString();
       mRootFile = new File(root_path);
 
       mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
       mMimeExt = MimePreference.mimesFromString( mPrefs.getString("mimes", MimePreference.DEFAULT_MIME_DATA) );
       mHomeFile = new File(mPrefs.getString("home_dir",root_path));
       mBrowseRoot = mPrefs.getBoolean("browse_root", false);
       mHideDot = mPrefs.getBoolean("hide_dot_files", false);
       mBackCd = mPrefs.getBoolean("back_cd_up", true);
 
       Uri uri = intent.getData();
       if (uri != null) {
         File f = new File(uri.getPath());
         if (f.isDirectory())
           mCurDir = f;
       }
       else {
         mCurDir = mHomeFile;
       }
 
       mStartDir = mCurDir;
 
 
       /* set up the view files dialog */
       Button b = (Button)findViewById(R.id.yank_bar_buffer);
       b.setOnClickListener(new Button.OnClickListener() {
           public void onClick(View v) {
             final Dialog dialog = new Dialog(Filer.this);
             View layout = getLayoutInflater().inflate(R.layout.yank_buffer, null);
             TextView tv;
             Button b;
 
             tv = (TextView) layout.findViewById(R.id.yank_buffer_contents);
             if (tv != null)
             tv.setText(build_yank_buffer_contents());
 
             b = (Button)layout.findViewById(R.id.button_unyank);
             if (b != null) 
             b.setOnClickListener( new Button.OnClickListener() {
               public void onClick(View v) { 
                 unyank_all(); 
                 dialog.dismiss();
               }
             });
 
             b = (Button)layout.findViewById(R.id.button_ok);
             if (b != null) 
             b.setOnClickListener( new Button.OnClickListener() {
               public void onClick(View v) { dialog.dismiss(); }
             });
 
             dialog.setContentView(layout);
             dialog.setTitle(R.string.yank_buffer_title);
             dialog.show();
           }
       });
       
 
       registerForContextMenu(getListView());  
     }
 
     @Override
     public void onResume()
     {
       super.onResume();
     
       IntentFilter filt = new IntentFilter();
       filt.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
       filt.addAction(Intent.ACTION_MEDIA_MOUNTED);
       filt.addDataScheme("file");
       registerReceiver(mReceiver, filt);
           
       fillData(mCurDir);
 
       updateYankBarVisibility();
     }
     @Override
     public void onPause()
     {
       super.onPause();
       unregisterReceiver(mReceiver);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
       super.onSaveInstanceState(outState);
       outState.putStringArrayList("mYanked", mYanked);
       outState.putString("cur_dir", mCurDir.getPath());
     }
     public void onRestoreInstanceState(Bundle inState)
     {
       super.onRestoreInstanceState(inState);
       mYanked =  inState.getStringArrayList("mYanked");
       fillData(new File(inState.getString("cur_dir")));
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) 
     {
       if (pIgnoreNextClick) { 
         // hack used for overriding context click on ..
         pIgnoreNextClick = false;
         return;
       }
       super.onListItemClick(l,v,position,id);
 
       String name = (String)mCurFiles.get(position);
       String mime = getMimetype(name);
 
       File f = new File(mCurDir, name);
 
       if (f.isDirectory()) {
         fillData(f);
         return;
       }
 
       if (mCreatingShortcut) {
         create_shortcut(f);
         return;
       }
 
       try {
         startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(f), mime));
       } catch (android.content.ActivityNotFoundException ex) { 
         Toast t = Toast.makeText(this, "No activity not found.", Toast.LENGTH_SHORT);
         t.show();
       }
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
       super.onCreateOptionsMenu(menu);
       getMenuInflater().inflate(R.menu.options, menu);
 
       //mYankBufferItem = menu.findItem(R.id.options_menu_yank_buffer);
       mCopyItem = menu.findItem(R.id.options_menu_copy);
       mMoveItem = menu.findItem(R.id.options_menu_move);
       mUnyankItem = menu.findItem(R.id.options_menu_unyank);
       //mDeleteItem = menu.findItem(R.id.options_menu_delete);
 
       boolean vis = ((mYanked != null) && (mYanked.size() > 0)); 
       mCopyItem.setVisible(vis);
       mMoveItem.setVisible(vis);
       mUnyankItem.setVisible(vis);
       //mDeleteItem.setVisible(vis);
 
       return true;
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
       switch (item.getItemId())
       {
         case R.id.options_menu_unyank:
           unyank_all();
           return true;
         case R.id.options_menu_newdir:
           return true;
         case R.id.options_menu_prefs:
             startActivityForResult(new Intent(this, FilerPreferencesActivity.class), RESULT_PREFERENCES);
           return true;
         case R.id.options_menu_help:
           new AlertDialog.Builder(this)
                 .setMessage(R.string.help_message)
                 .setTitle(R.string.help_title)
                 .setPositiveButton(R.string.ok, null)
                 .show();
           return true;
       }
       return false;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
     {
       super.onCreateContextMenu(menu, v, menuInfo);
       AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
 
       File f = new File(mCurDir, mCurFiles.get(info.position));
 
       if (mCreatingShortcut) {
         create_shortcut(f);
         return;
       } 
 
       if (mCurFiles.get(info.position).equals("..")) {
         pIgnoreNextClick = true;
         fillData(mHomeFile);
         return;
       }
 
       getMenuInflater().inflate(R.menu.files_context, menu);
 
       MenuItem unyankItem = menu.findItem(R.id.file_context_menu_unyank);
       MenuItem yankItem = menu.findItem(R.id.file_context_menu_yank);
       if (unyankItem != null && yankItem != null) {
         boolean vis = mYanked.contains(f.getPath());
         unyankItem.setVisible(vis);
         yankItem.setVisible(!vis);
       }
 
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
       AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
       String name = mCurFiles.get(info.position);
       File f = new File(mCurDir, name);
       View v = info.targetView;
       if (f == null || v == null) return true;
 
       switch (item.getItemId()) {
         case R.id.file_context_menu_open:
           return true;
         case R.id.file_context_menu_yank:
           mYanked.add(f.getPath());
           v.setBackgroundResource(R.drawable.file_list_item_yanked);
           updateYankBarVisibility();
           return true;
         case R.id.file_context_menu_unyank:
           mYanked.remove(f.getPath());
           v.setBackgroundResource(R.drawable.file_list_item_normal);
           updateYankBarVisibility();
           return true;
         case R.id.file_context_menu_rename:
           return true;
         case R.id.file_context_menu_delete:
           return true;
       }
       return super.onContextItemSelected(item);
 
     }
     public boolean onKeyDown(int keyCode, KeyEvent event) {
       if(mBackCd && (keyCode == KeyEvent.KEYCODE_BACK)) {
         if (! mCurDir.getPath().equals(mStartDir.getPath())) {
           fillData(mCurDir.getParentFile());
           return true;
         }
       }
       return super.onKeyDown(keyCode, event);
     }
     
 
 
 
 
 
 
     private void fillData(File new_dir)
     {
       try {
         mCurDir = new File(new_dir.getCanonicalPath());
         setTitle(mCurDir.getPath());
       } catch (Exception e) {
         return;
       }
 
       View empty = findViewById(R.id.empty);
 
       mCurFiles = new ArrayList<String>();
 
       String state = Environment.getExternalStorageState();
       String[] ls = mCurDir.list();
       if (Environment.MEDIA_MOUNTED.equals(state) && ls != null)  {
         int i;
         for (i=0; i < ls.length; i++) {
           if (ls[i].startsWith(".") && mHideDot)
             continue;
           mCurFiles.add(ls[i]);
         }
         empty.setVisibility(View.INVISIBLE);
       }
       else {
         empty.setVisibility(View.VISIBLE);
       }
 
       Collections.sort(mCurFiles, new Comparator() {
         public int compare(Object a, Object b) {
           File fa = new File(mCurDir, (String)a); 
           File fb = new File(mCurDir, (String)b); 
           if (fa == null || fb == null) return 0;
           if (fa.isDirectory()) {
             if (fb.isDirectory()) 
               return fa.getName().compareTo( fb.getName() );
             return -1;
           }
           if (fb.isDirectory()) 
             return 1;
           return 0;
         }
       });
 
       if (!mCurDir.getPath().equals(mRootFile.getPath()) || mBrowseRoot)
         mCurFiles.add(0,"..");
 
       final ArrayAdapter<String> file_adapter = new ArrayAdapter(this, R.layout.file_list_item, mCurFiles) {
         public View getView(int position, View v, ViewGroup parent)
         {
           String name = (String)mCurFiles.get(position);
           LayoutInflater li = getLayoutInflater();
           View ret = li.inflate(R.layout.file_list_item, parent, false);
 
           TextView txt = (TextView)ret.findViewById(R.id.row_text);
           txt.setText(name);
 
           ImageView img = (ImageView)ret.findViewById(R.id.row_icon);
           File f = new File(mCurDir, name);
 
           if (mYanked.contains(f.getPath())) {
             ret.setBackgroundResource(R.drawable.file_list_item_yanked);
           }
 
           if (name.equals("..")) {
             img.setImageResource(android.R.drawable.ic_menu_revert);
           }
           else
           if (f.isDirectory()) {
             img.setImageResource(android.R.drawable.ic_menu_more);
 
           }
           else {
             txt = (TextView)ret.findViewById(R.id.row_type);
 
 
             String ext = getMimetype(name);
             txt.setText(ext != null ? ext : "text/*");
 
             txt = (TextView)ret.findViewById(R.id.row_size);
             txt.setText( format_size(f.length()) );
 
             txt = (TextView)ret.findViewById(R.id.row_mtime);
             txt.setText( format_date(f.lastModified()) );
           }
 
           return ret;
 
         }
       };
 
   
       setListAdapter(file_adapter);
     }
 
 
 
 
     private String format_size(long size)
     {
       String ret;
      if (size > 1024*1024*1024) ret = pNumFmt.format(size / 1024*1024*1024) + "G";
      else if (size > 1024*1024) ret = pNumFmt.format(size / 1024*1024) + "M";
      else if (size > 1024) ret = pNumFmt.format(size / 1024) + "k";
       else ret = pNumFmt.format(size) + "b";
       return ret;
     }
     private String format_date(long when)
     {
       Date last = new Date(when);
       GregorianCalendar now = new GregorianCalendar();
       GregorianCalendar then = new GregorianCalendar();
       then.setTime(last);
 
       if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR))
         return pDateFmt_time.format(last);
       else
         return pDateFmt_year.format(last);
     }
 
     private void create_shortcut(File f)
     {
       int draw;
       String type;
       String action = Intent.ACTION_VIEW;
 
       if (f.isDirectory()) {
         type = "text/directory";
         action = Intent.ACTION_RUN;
         draw = R.drawable.mime_folder;
       }
       else {
         type = "text/*";
         draw = R.drawable.mime_generic;
       }
 
       Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, draw);
 
       Intent i = new Intent(action);
       i.setDataAndType(Uri.fromFile(f), type);
 
       Intent short_i = new Intent();
       short_i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
       short_i.putExtra(Intent.EXTRA_SHORTCUT_NAME, f.getName());
       short_i.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
       setResult(RESULT_OK, short_i);
       finish();
     }
 
     private void updateYankBarVisibility()
     {
       View yank_bar = findViewById(R.id.yank_bar);
       if (mYanked == null) return;
       boolean vis = (mYanked.size() > 0);
       if (yank_bar != null) yank_bar.setVisibility(vis ? View.VISIBLE : View.GONE);
 
       if (mCopyItem != null) mCopyItem.setVisible(vis);
       if (mMoveItem != null) mMoveItem.setVisible(vis);
       if (mUnyankItem != null) mUnyankItem.setVisible(vis);
       //if (mDeleteItem != null) mDeleteItem.setVisible(vis);
 
     }
     private String getMimetype(String filename)
     {
       int ext_pos = filename.lastIndexOf('.');
       if (ext_pos == -1) return null;
       String ext = filename.substring(ext_pos+1);
       String mime = mMimeExt.get(ext);
       return mime;
     }
     private void unyank_all()
     {
       mYanked.clear();
       updateYankBarVisibility();
     }
     private String build_yank_buffer_contents()
     {
 
       if (mYanked == null) return "";
       int len = mYanked.size();
       int i;
       StringBuilder sb = new StringBuilder();
       for (i=0; i < len; i++) {
         String path = (String)mYanked.get(i);
         if (path == null) continue;
         File f = new File(path);
         if (f == null) continue;
 
         sb.append(path);
         if (f.isDirectory()) {
           sb.append("/\n");
           sb = build_yank_buffer_contents_append_dir(sb, f,1);
         }
         sb.append("\n");
       }
 
       return sb.toString();
     }
     private static StringBuilder build_yank_buffer_contents_append_dir(StringBuilder sb, File dir, int level) {
       String[] files = dir.list();
       int i;
       for (i=0; i < files.length; i++) {
         int l;
         for (l=0; l < level; l++) sb.append("    ");
         sb.append("|- ").append(files[i]) .append("\n") ;
         File f = new File(dir, files[i]);
         if (f.isDirectory()) {
           sb = build_yank_buffer_contents_append_dir(sb, f, level+1);
         }
       }
       return sb;
     };
 }
