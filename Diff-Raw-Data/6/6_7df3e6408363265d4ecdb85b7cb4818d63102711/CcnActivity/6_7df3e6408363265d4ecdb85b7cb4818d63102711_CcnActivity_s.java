 
 package org.tsinghua.omedia.activity;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.tsinghua.omedia.R;
 import org.tsinghua.omedia.event.CcnFilesUpdateEvent;
 import org.tsinghua.omedia.event.Event;
 import org.tsinghua.omedia.form.DeleteCcnFileForm;
 import org.tsinghua.omedia.serverAPI.DeleteCcnFileAPI;
 import org.tsinghua.omedia.tool.FileUtils;
 import org.tsinghua.omedia.tool.Logger;
 import org.tsinghua.omedia.ui.fileBrowser.FileInfoAdapter;
 import org.tsinghua.omedia.ui.fileBrowser.FileInfoDataSet;
 import org.tsinghua.omedia.worker.CcnDownloadWorker;
 import org.tsinghua.omedia.worker.MultipartWorker;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 
 /**
  * @author chenzhuwei
  */
 public class CcnActivity extends BaseActivity {
     private static final Logger logger = Logger.getLogger(CcnActivity.class);
 
     private GridView gridView;
 
     private FileInfoAdapter gridAdapter;
 
     private int depth = 0;
 
     private int currentDir = 0; // 0 -- ROOT
 
     private ArrayList<FileInfoDataSet> rootDirectory = new ArrayList<FileInfoDataSet>();
 
     private String[] virtualDirName = {
             "My Doc", "Friends Doc", "Groups Doc"
     };
 
     private final int MY_DOC = 1;
 
     // private final int FRIENDS_DOC = 2;
 
     // private final int GROUPS_DOC = 3;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.ccn_activity);
         gridView = (GridView) findViewById(R.id.gridview);
         initRoot();
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.ccn_activity_context_menu, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
             case R.id.delete_file:
                 String ccnName = gridAdapter.getFile(info.position).getFileName();
                 doDeleteFile(ccnName);
                 return true;
             default:
                 return super.onContextItemSelected(item);
         }
     }
    
    @Override
    public void onBackPressed() {
        depth = 0;
        updateUI(currentDir);
    }
 
     @Override
     public void onBackPressed() {
         if (depth == 0) {
             super.onBackPressed();
         } else {
             depth = 0;
             updateUI(currentDir);
         }
     }
 
     private void initRoot() {
         for (int i = 0; i < virtualDirName.length; i++) {
             FileInfoDataSet tmpF = new FileInfoDataSet(virtualDirName[i],
                     R.drawable.filebrower_folder, true);
             rootDirectory.add(tmpF);
         }
     }
 
     private void doDeleteFile(final String ccnName) {
         long accountId = dataSource.getAccountId();
         long token = dataSource.getToken();
         DeleteCcnFileForm form = new DeleteCcnFileForm();
         form.setAccountId(accountId);
         form.setToken(token);
         form.setCcnName(ccnName);
         showWaitingDialog();
         new DeleteCcnFileAPI(form, this) {
             @Override
             protected void onSuccess() {
                 dataSource.deleteCcnFile(ccnName);
                 checkDataUpdate();
             }
 
             @Override
             protected void onStop() {
                 super.onStop();
                 dismissWaitingDialog();
             }
 
         }.call();
     }
 
     private void initListener() {
         registerForContextMenu(gridView);
         gridView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                 if (depth == 1) {
                     String ccnFile = gridAdapter.getFile(position).getFileName();
                     CcnDownloadWorker worker = new CcnDownloadWorker(ccnFile) {
                         @Override
                         protected void onSuccess(File file) {
                             dismissWaitingDialog();
                             openFile(file);
                         }
 
                         @Override
                         protected void onFailed(Exception e) {
                             dismissWaitingDialog();
                             logger.error(e);
                         }
                     };
                     showWaitingDialog();
                     runWorker(worker);
                 } else if (depth == 0) {
                     depth = 1;
                     currentDir = position + 1;
                     updateUI(currentDir);
                 }
             }
         });
     }
 
     @Override
     public void onResume() {
         super.onResume();
         updateUI(currentDir);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.ccn_activity_menu, menu);
         return super.onCreateOptionsMenu(menu);
 
     }
 
     private void updateUI(int position) {
         if (depth == 0) {
             gridAdapter = new FileInfoAdapter(this, rootDirectory);
             gridView.setAdapter(gridAdapter);
             initListener();
         } else if (depth == 1) {
             ArrayList<FileInfoDataSet> files = new ArrayList<FileInfoDataSet>();
             if (position == MY_DOC) {
                 int length = dataSource.getCcnFiles().length;
                 String[] fileNames = new String[length];
                 for (int i = 0; i < length; i++) {
                     fileNames[i] = dataSource.getCcnFiles()[i].getCcnname();
                     FileInfoDataSet fileInfo = new FileInfoDataSet(fileNames[i],
                             FileUtils.getImageIdByType(FileUtils.getMIMEType(fileNames[i])));
                     files.add(fileInfo);
                 }
             }
             gridAdapter = new FileInfoAdapter(this, files);
             gridView.setAdapter(gridAdapter);
             initListener();
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.select_file) {
             Intent intent = new Intent(CcnActivity.this, FileBrowerAcitvity.class);
             startActivityForResult(intent, REQUEST_SELECT_FILE);
             return true;
         } else if (item.getItemId() == R.id.back_file) {
             depth = 0;
             updateUI(currentDir);
             return true;
         } else {
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onEventCatch(Event event) {
         if (event instanceof CcnFilesUpdateEvent) {
             updateUI(currentDir);
             dismissWaitingDialog();
         }
         super.onEventCatch(event);
     }
 
     private static final int REQUEST_SELECT_FILE = 1;
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         if (requestCode == REQUEST_SELECT_FILE && resultCode == RESULT_OK) {
             Uri uri = intent.getData();
             ccnPutFile(uri);
         } else {
             super.onActivityResult(requestCode, resultCode, intent);
         }
     }
 
     private void ccnPutFile(Uri fileUri) {
         File file = new File(fileUri.getPath());
         MultipartWorker worker = new MultipartWorker(file, file.getName()) {
 
             @Override
             protected void onSuccess() {
                 dismissWaitingDialog();
                 checkDataUpdate();
             }
 
             @Override
             protected void onFailed(Exception e) {
                 dismissWaitingDialog();
                 logger.error(e);
             }
         };
         showWaitingDialog();
         runWorker(worker);
     }
 }
