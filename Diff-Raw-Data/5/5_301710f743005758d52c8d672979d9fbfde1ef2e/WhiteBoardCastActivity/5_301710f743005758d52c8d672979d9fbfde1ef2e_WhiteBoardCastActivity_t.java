 package com.livejournal.karino2.whiteboardcast;
 
 import com.google.libvorbis.VorbisException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class WhiteBoardCastActivity extends Activity implements EncoderTask.ErrorListener {
 
     static final int DIALOG_ID_ABOUT = 1;
     static final int DIALOG_ID_QUERY_VIEW_SHARE = 2;
     static final int DIALOG_ID_QUERY_MERGE_AGAIN = 3;
     static final int DIALOG_ID_FILE_RENAME = 4;
     static final int DIALOG_ID_NEW = 5;
 
     private static final String AUDIO_FNAME = "temp.mkv";
 
     private Presentation presen = new Presentation();
 
     public void postErrorMessage(final String msg) {
         handler.postDelayed(new Runnable(){
 
             @Override
             public void run() {
                 showError(msg);
             }
         }, 0);
     }
 
     public void postShowMessage(final String msg) {
         handler.postDelayed(new Runnable(){
 
             @Override
             public void run() {
                 showMessage(msg);
             }
         }, 0);
     }
 
     public void showError(String msg) {
         Log.d("WhiteBoardCast", msg);
         showMessage(msg);
     }
 
     public void showMessage(String msg) {
         Toast.makeText(this, msg, Toast.LENGTH_LONG ).show();
 
     }
 
 
     Handler handler = new Handler();
 
     PageScrollAnimator animator;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
 
 
         setContentView(R.layout.activity_whiteboardcast);
         readDebuggableSetting();
         getWhiteBoardCanvas().enableDebug(debuggable);
         try {
             boolean slidesEnabled = getIntent().getBooleanExtra("slides_enabled", false);
             if(slidesEnabled) {
                 presen.enableSlide();
                 getWhiteBoardCanvas().setSlides(presen.getSlideFiles());
             }
         } catch (IOException e) {
             showMessage("Slide setup fail: " + e.getMessage());
         }
         animator = new PageScrollAnimator(presen.getScheduleExecutor(), getWhiteBoardCanvas());
 
         if(workingFileExists()) {
             showDialog(DIALOG_ID_QUERY_MERGE_AGAIN);
         }
 
     }
 
     private boolean workingFileExists() {
         try {
             File workVideo = new File(getWorkVideoPath());
             if(workVideo.exists())
                 return true;
             return false;
         } catch (IOException e) {
             showError("IO Exception while working file check: " + e.getMessage());
             return false;
         }
     }
 
     boolean debuggable = false;
     private void readDebuggableSetting() {
         PackageManager pm = getPackageManager();
         try {
             ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), 0);
             if((ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0)
             {
                 debuggable = true;
             }
         } catch (PackageManager.NameNotFoundException e) {
         }
     }
 
     public boolean slideAvailable() {
         try {
             return presen.slideAvailable();
         } catch (IOException e) {
             showError("Check slideAvailable fail with IO Exception: " + e.getMessage());
             return false;
         }
     }
 
     public boolean canUndo() {
         WhiteBoardCanvas wb = getWhiteBoardCanvas();
         if(wb == null)
             return false;
         return wb.canUndo();
     }
 
     public void undo() {
         getWhiteBoardCanvas().undo();
     }
 
     boolean duringAnimation = false;
     void setDuringAnimation(boolean animation) {
         duringAnimation = animation;
     }
 
 
     public void stopRecord() {
         if(!canStop()) {
             Log.d("WhiteBoardCast", "stop record called but not recording. " + presen.recordStatus());
             return;
         }
         // under processing.
         presen.stopRecordBegin();
         getWhiteBoardCanvas().changeRecStatus();
         showMessage("record end, start post process...");
 
         presen.stopRecord();
 
         new Thread(new Runnable() {
             @Override
             public void run() {
                 if(!presen.afterStop()) {
                     postErrorMessage("fail finalize encode: " + presen.getEncoderTask().getErrorBuf().toString());
                     return;
 
                 }
 
                 postShowMessage("post process done.");
                 handler.post(new Runnable(){
                     @Override
                     public void run() {
                         beginAudioVideoMergeTask();
                     }
                 }
                 );
 
             }
         }).start();
     }
 
 
     public boolean canStop() {
         return presen.canStopRecord();
     }
 
     public boolean canRedo() {
         WhiteBoardCanvas wb = getWhiteBoardCanvas();
         if(wb == null)
             return false;
         return wb.canRedo();
     }
 
     public void redo() {
         getWhiteBoardCanvas().redo();
     }
 
 
     public void toggleShowSlides() {
         try {
             getWhiteBoardCanvas().toggleShowSlides();
         } catch (IOException e) {
             showMessage("Toggle slides fail. " + e.getMessage());
         }
 
     }
 
     public Presentation.RecordStatus getRecStats() {
         return presen.recordStatus();
     }
 
 
     public void pauseRecord() {
         presen.pauseRecord();
         getWhiteBoardCanvas().changeRecStatus();
 
         showMessage("pause");
     }
 
     public void resumeRecord() {
         presen.resumeRecord();
 
         presen.scheduleEncodeTask();
         getWhiteBoardCanvas().changeRecStatus();
         showMessage("resume");
     }
 
 
 
     public void startRecord() {
         if(!presen.canStartRecord()) {
             Log.d("WhiteBoardCast", "record start but status is not dormant: " + presen.recordStatus());
             return;
         }
         presen.startRecordFirstPhase();
         getWhiteBoardCanvas().changeRecStatus();
         handler.post(new Runnable() {
             @Override
             public void run() {
                 // this method take some times. so get it back to UI first for update overlay.
                 startRecordSecondPhase();
             }
         });
     }
 
     public void pageUp() {
         if(!getWhiteBoardCanvas().beginPagePrev(animator)) {
             showMessage("First page, couldn't go up!");
         }
     }
 
     public void pageDown() {
         getWhiteBoardCanvas().beginPageNext(animator);
     }
 
     // TODO: move to Presentation as far as possible.
     private void startRecordSecondPhase() {
         WhiteBoardCanvas wb = getWhiteBoardCanvas();
         wb.invalWholeRegionForEncoder(); // for restart. make it a little heavy.
         try {
             presen.newEncoderTask(wb, wb.getBitmap(), getWorkVideoPath(), this);
 
             if(debuggable)
                 presen.getEncoderTask().setFpsListener(getWhiteBoardCanvas().getEncoderFpsCounter());
         } catch (IOException e) {
             showError("Fail to get workVideoPath: " + e.getMessage());
             return;
         }
         long currentMill = System.currentTimeMillis();
 
         if(!presen.getEncoderTask().initEncoder(currentMill)) {
             showError("init encode fail");
             return;
         }
 
         presen.newRecorder(currentMill);
         try {
             presen.getRecorder().setOutputFile(getWorkAudioPath());
         } catch (IOException e) {
             showError("IOException: Create WhiteBoardCast folder fail: " + e.getMessage());
             return;
         }
         try {
             presen.getRecorder().prepare();
         } catch (IOException e) {
             showError("IOException: MediaRecoder prepare fail: " + e.getMessage());
             return;
         } catch (VorbisException e) {
             showError("VorbisException: MediaRecoder prepare fail: " + e.getMessage());
             return;
         }
 
         presen.startRecord();
         wb.changeRecStatus();
         showMessage("record start");
     }
 
     public WhiteBoardCanvas getWhiteBoardCanvas() {
         return (WhiteBoardCanvas)findViewById(R.id.fullscreen_content);
     }
 
     public static void ensureDirExist(File dir) throws IOException {
         if(!dir.exists()) {
             if(!dir.mkdir()){
                 throw new IOException();
             }
         }
     }
     public static File getFileStoreDirectory() throws IOException {
         File dir = new File(Environment.getExternalStorageDirectory(), "WhiteBoardCast");
         ensureDirExist(dir);
         return dir;
     }
 
 
     public String getResultPath() throws IOException {
         return getFileStoreDirectory().getAbsolutePath() + "/result.webm";
     }
 
     private String getWorkAudioPath() throws IOException {
         return getFileStoreDirectory().getAbsolutePath() + "/" + AUDIO_FNAME;
     }
 
     private String getWorkVideoPath() throws IOException {
         return getFileStoreDirectory().getAbsolutePath() + "/temp.webm";
     }
 
 
     private void viewVideoIntent() {
         Intent i = new Intent(Intent.ACTION_VIEW);
         i.setDataAndType(Uri.fromFile(presen.getResultFile()), "video/*");
         i.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, false);
         startActivity(i);
     }
 
     private void shareVideoIntent() {
         Intent i = new Intent(Intent.ACTION_SEND);
         i.setType("video/webm");
 
         i.putExtra(Intent.EXTRA_STREAM, presen.getResultUri());
         startActivity(Intent.createChooser(i, "Share video"));
 
     }
 
     private Uri insertLastResultToContentResolver() {
         SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
         String title = timeStampFormat.format(new Date()) + " recorded";
 
         ContentValues content = new ContentValues(4);
 
 
         content.put(MediaStore.Video.VideoColumns.TITLE, title);
         content.put(MediaStore.Video.VideoColumns.DATE_ADDED,
                 System.currentTimeMillis() / 1000);
         content.put(MediaStore.Video.Media.MIME_TYPE, "video/webm");
         content.put(MediaStore.Video.Media.DATA, presen.getResultFile().getAbsolutePath());
         ContentResolver resolver = getBaseContext().getContentResolver();
         return resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, content);
     }
 
     private void beginAudioVideoMergeTask() {
         try {
             new AudioVideoMergeTask(this, new AudioVideoMergeTask.NotifyFinishListener() {
                 @Override
                 public void onFinish() {
                     handler.postDelayed(new Runnable(){
                         @Override
                         public void run() {
                             try {
                                 renameAndDeleteWorkFiles();
                                 showDialog(DIALOG_ID_QUERY_VIEW_SHARE);
                             } catch (IOException e) {
                                 showError("Rename encoded file fail: " + e.getMessage());
                             }
                         }
                     }, 500);
                 }
             }).execute(getWorkVideoPath(), getWorkAudioPath(), getResultPath());
         } catch (IOException e) {
             showError("Fail to create WhiteBoardCast folder(2). " + e.getMessage());
         }
     }
 
     private void renameAndDeleteWorkFiles() throws IOException {
         File result = new File(getResultPath());
         File workVideo = new File(getWorkVideoPath());
         File workAudio = new File(getWorkAudioPath());
         if(!result.exists())
             throw new IOException("no encoded file exists.");
         SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
         String filename = timeStampFormat.format(new Date()) + ".webm";
 
         presen.setResult(new File(getFileStoreDirectory(), filename));
         result.renameTo(presen.getResultFile());
         workVideo.delete();
         workAudio.delete();
 
         presen.setResultUri(insertLastResultToContentResolver());
 
 
     }
 
 
     @Override
     public boolean dispatchKeyEvent(KeyEvent event) {
         if(event.getAction() == KeyEvent.ACTION_DOWN)
         {
             switch(event.getKeyCode()) {
                 case KeyEvent.KEYCODE_BACK:
                     new AlertDialog.Builder(this)
                             .setIcon(android.R.drawable.ic_dialog_alert)
                         .setMessage(R.string.query_back_message)
                         .setCancelable(true)
                         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
                             public void onClick(DialogInterface dialog, int which) {
                                 finish();
                             }})
                             .setNegativeButton(android.R.string.no, null)
                             .show();
                     return true;
             }
         }
         return super.dispatchKeyEvent(event);
     }
 
     @Override
     public void openOptionsMenu() {
         // for large device, android ignore openOptionsMenu (why!?).
         // See http://stackoverflow.com/questions/9996333/openoptionsmenu-function-not-working-in-ics
         Configuration config = getResources().getConfiguration();
 
         if((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                 > Configuration.SCREENLAYOUT_SIZE_LARGE) {
 
             int originalScreenLayout = config.screenLayout;
             config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
             super.openOptionsMenu();
             config.screenLayout = originalScreenLayout;
         } else {
             super.openOptionsMenu();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         boolean canManageSlides = !canStop();
         menu.findItem(R.id.menu_id_new).setEnabled(canManageSlides);
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     public void clearCanvas() {
         getWhiteBoardCanvas().clearCanvas();
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
             case R.id.menu_id_about:
                 showDialog(DIALOG_ID_ABOUT);
                 return true;
             case R.id.menu_id_quit:
                 finish();
                 return true;
             case R.id.menu_id_new:
                 showDialog(DIALOG_ID_NEW);
                 return true;
         }
         return super.onMenuItemSelected(featureId, item);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch(id) {
             case DIALOG_ID_ABOUT:
                 return createAbout();
             case DIALOG_ID_QUERY_VIEW_SHARE:
                 return createQueryViewShareDialog();
             case DIALOG_ID_QUERY_MERGE_AGAIN:
                 return createQueryMergeWorkingFileDialog();
             case DIALOG_ID_FILE_RENAME:
                 return createFileRenameDialog();
             case DIALOG_ID_NEW:
                 return createNewDialog();
         }
         return super.onCreateDialog(id);
     }
 
     private void startNewPresentation(){
         presen = new Presentation();
         getWhiteBoardCanvas().newPresentation();
         newDialog.dismiss();
         showMessage("New");
     }
 
     private void startNewSlidesPresentation() {
         Intent intent = new Intent(this, SlideListActivity.class);
         intent.putExtra("canvas_width", getWhiteBoardCanvas().getStoredWidth());
         intent.putExtra("canvas_height", getWhiteBoardCanvas().getStoredHeight());
         startActivity(intent);
         finish();
     }
 
     Dialog newDialog;
 
     private Dialog createNewDialog() {
         View view = getLayoutInflater().inflate(R.layout.new_dialog, null);
         ((Button)view.findViewById(R.id.buttonPlane)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startNewPresentation();
             }
         });
         ((Button)view.findViewById(R.id.buttonSlides)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startNewSlidesPresentation();
             }
         });
         newDialog = new AlertDialog.Builder(this)
                 .setView(view)
                 .create();
         return newDialog;
     }
 
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch(id) {
             case DIALOG_ID_FILE_RENAME:
                 EditText et = (EditText)dialog.findViewById(R.id.edit_filename);
                // back from another app but this app is already killed. just dismiss.
                if(presen.getResultFile() == null) {
                    dialog.dismiss();
                    return;
                }
                 et.setText(presen.getResultFile().getName());
                 break;
             case DIALOG_ID_NEW:
                 newDialog = dialog;
                 break;
         }
         super.onPrepareDialog(id, dialog);
     }
 
     Dialog fileRenameDialog;
 
 
     private Dialog createFileRenameDialog() {
         LayoutInflater inflater = LayoutInflater.from(this);
         final View view = inflater.inflate(R.layout.rename, null);
         setOnClickListener(view, R.id.button_cancel, new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 fileRenameDialog.dismiss();
             }
         });
         setOnClickListener(view, R.id.button_save, new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 EditText et = (EditText)view.findViewById(R.id.edit_filename);
                 boolean success = renameFileNameTo(et.getText().toString());
                 if(success)
                     fileRenameDialog.dismiss();
             }
         });
         fileRenameDialog = new AlertDialog.Builder(this).setTitle(R.string.label_rename)
                 .setView(view)
                 .create();
         return fileRenameDialog;
     }
 
     private boolean renameFileNameTo(String newName) {
         if(newName.equals(""))
         {
             showMessage("Empty file name!");
             return false;
         }
 
         File newNameFile = new File(presen.getResultFile().getParentFile(), newName);
         if(newNameFile.exists()) {
             showMessage("This file is already exists");
             return false;
         }
 
         presen.renameResult(newNameFile);
         updateNewFIleNameToContentDB(presen.getResultFile());
         return true;
     }
 
     private void updateNewFIleNameToContentDB(File newFile) {
         ContentValues content = new ContentValues(2);
 
         long id = ContentUris.parseId(presen.getResultUri());
 
 
         content.put(MediaStore.Video.Media.DATA, newFile.getAbsolutePath());
         content.put(MediaStore.Video.Media.DISPLAY_NAME, newFile.getName());
         ContentResolver resolver = getBaseContext().getContentResolver();
         resolver.update(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, content, "_id = ?", new String[] {String.valueOf(id)});
     }
 
 
     private Dialog createQueryMergeWorkingFileDialog() {
         return new AlertDialog.Builder(this).setTitle(R.string.query_merge_title)
                 .setMessage(R.string.query_merge_body)
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         beginAudioVideoMergeTask();
                     }
                 })
                 .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
 
                     }
                 })
                 .create();
     }
 
     private Dialog createQueryViewShareDialog() {
         LayoutInflater inflater = LayoutInflater.from(this);
         View view = inflater.inflate(R.layout.query_viewsend, null);
         setOnClickListener(view, R.id.button_view, new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 viewVideoIntent();
             }
         });
         setOnClickListener(view, R.id.button_share, new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 shareVideoIntent();
             }
         });
         setOnClickListener(view, R.id.button_rename, new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 renameVideoFileName();
             }
         });
         return new AlertDialog.Builder(this).setTitle(R.string.query_title)
                 .setView(view)
                 .create();
     }
 
     private void renameVideoFileName() {
         showDialog(DIALOG_ID_FILE_RENAME);
     }
 
     private void setOnClickListener(View view, int id, View.OnClickListener onclick) {
         Button button = (Button)view.findViewById(id);
         button.setOnClickListener(onclick);
     }
 
     private AlertDialog createAbout() {
         final WebView webView = new WebView(this);
         webView.loadUrl("file:///android_asset/licenses.html");
         return new AlertDialog.Builder(this).setTitle(R.string.about_title)
                 .setView(webView)
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog, final int whichButton) {
                         dialog.dismiss();
                     }
                 }).create();
 
     }
 
 
 
     private Button findButton(int id) {
         return (Button)findViewById(id);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
 
     }
 
     public void setPenOrEraser(int penIndex) {
         getWhiteBoardCanvas().setPenOrEraser(penIndex);
     }
 }
