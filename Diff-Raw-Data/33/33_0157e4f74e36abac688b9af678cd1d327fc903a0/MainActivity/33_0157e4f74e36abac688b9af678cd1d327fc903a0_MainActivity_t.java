 package org.jitu.wagtail;
 
 import java.io.File;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
     private final static int ACTIVITY_FILE_CHOOSER = 1;
     private final static int ACTIVITY_FILE_SAVER   = 2;
 
     private FileControl fileControl = new FileControl();
     private EditControl editControl = new EditControl();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         editControl.addTextWatcher(getEdit());
         setVolumeControlStream(AudioManager.STREAM_MUSIC);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_file:
             showFileMenu();
             return true;
         case R.id.menu_edit:
             showEditMenu();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void showFileMenu() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setItems(R.array.file_menu_items,
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         onClickFile(dialog, which);
                     }
                 });
         builder.create().show();
     }
 
     private void showEditMenu() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setItems(R.array.edit_menu_items,
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         onClickEdit(dialog, which);
                     }
                 });
         builder.create().show();
     }
 
     public void onClickFile(DialogInterface dialog, int which) {
         Resources r = getResources();
         String[] items = r.getStringArray(R.array.file_menu_items);
         String item = items[which];
         if (r.getString(R.string.menu_item_new).equals(item)) {
             onNew();
         } else if (r.getString(R.string.menu_item_open).equals(item)) {
             onOpen();
         } else if (r.getString(R.string.menu_item_save).equals(item)) {
             onSave();
         } else if (r.getString(R.string.menu_item_save_as).equals(item)) {
             onSaveAs();
         }
     }
 
     public void onClickEdit(DialogInterface dialog, int which) {
         Resources r = getResources();
         String[] items = r.getStringArray(R.array.edit_menu_items);
         String item = items[which];
         if (r.getString(R.string.menu_item_cut).equals(item)) {
             editControl.cut(this, getEdit());
         } else if (r.getString(R.string.menu_item_copy).equals(item)) {
             editControl.copy(this, getEdit());
         } else if (r.getString(R.string.menu_item_paste).equals(item)) {
             editControl.paste(this, getEdit());
         } else if (r.getString(R.string.menu_item_undo).equals(item)) {
             editControl.undo(getEdit());
         } else if (r.getString(R.string.menu_item_redo).equals(item)) {
             editControl.redo(getEdit());
         }
     }
 
     private void onNew() {
         editControl.clear(getEdit());
         fileControl.newFile();
         setTitle(R.string.app_name);
     }
 
     private EditText getEdit() {
         return (EditText)findViewById(R.id.edit);
     }
 
     private void onOpen() {
         Intent intent = new Intent(this, FileChooser.class);
         String home = fileControl.getHomePath();
         intent.putExtra(FileChooser.ARG_PATH, home);
         startActivityForResult(intent, ACTIVITY_FILE_CHOOSER);
     }
 
     private void onSave() {
         File file = fileControl.getCurrentFile();
         if (file == null) {
             onSaveAs();
             return;
         }
         saveFile(file);
     }
 
     private void onSaveAs() {
         Intent intent = new Intent(this, FileSaver.class);
         String path = fileControl.getAbsolutePath();
         intent.putExtra(FileSaver.ARG_PATH, path);
         startActivityForResult(intent, ACTIVITY_FILE_SAVER);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode < 0) {
             return;
         }
         switch (requestCode) {
         case ACTIVITY_FILE_CHOOSER:
             onFileChooserResult(data);
             break;
         case ACTIVITY_FILE_SAVER:
             onFileSaverResult(data);
             break;
         }
     }
 
     private void onFileChooserResult(Intent data) {
         String path = data.getStringExtra(FileChooser.RESULT_PATH);
         if (path.isEmpty()) {
             return;
         }
         openFile(new File(path));
     }
 
     private void openFile(File file) {
         String text = fileControl.readFile(file);
         if (text == null) {
             String msg = fileControl.getErrorMessage();
             Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
             return;
         }
         editControl.setText(getEdit(), text);
         setTitle(file.getName());
     }
 
     private void onFileSaverResult(Intent data) {
         String path = data.getStringExtra(FileChooser.RESULT_PATH);
         if (path.isEmpty()) {
             return;
         }
         saveFile(new File(path));
     }
 
     private void saveFile(File file) {
         String text = editControl.getText(getEdit());
         if (!fileControl.saveFile(file, text)) {
             String msg = fileControl.getErrorMessage();
             Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
         }
         setTitle(file.getName());
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         File file = fileControl.getCurrentFile();
         if (file == null) {
             return;
         }
         saveFile(file);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             return true;
         } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
             onVolumeDown();
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     private void onVolumeDown() {
         onSave();
         showSavedFileName();
     }
 
     private void showSavedFileName() {
         File file = fileControl.getCurrentFile();
         if (file == null) {
             return;
         }
         String msg = "Save " + file.getName();
         Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
     }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fileControl.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        fileControl.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }
 }
