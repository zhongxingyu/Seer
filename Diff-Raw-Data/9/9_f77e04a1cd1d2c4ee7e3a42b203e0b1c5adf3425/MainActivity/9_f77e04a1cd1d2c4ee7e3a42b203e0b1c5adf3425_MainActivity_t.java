 package com.wu.androidfileclient;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.webkit.MimeTypeMap;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.wu.androidfileclient.models.FileItem;
 import com.wu.androidfileclient.services.FileDownloader;
 import com.wu.androidfileclient.services.FileLister;
 import com.wu.androidfileclient.ui.FileItemsListAdapter;
 
 public class MainActivity extends ListActivity {
 	
 	private ArrayList<FileItem> filesList = new ArrayList<FileItem>();
 	private FileItem goBack               = new FileItem();
 
 	private FileItemsListAdapter filesAdapter;
 	private HashMap<String, String> previousKeys;
 	private String currentKey;
 
 	private ProgressDialog progressDialog;
 
 	@SuppressWarnings("unchecked")
     @Override
     public void onCreate(Bundle savedInstanceState) {
 		goBack.name = "Back";
 		goBack.type = "action";
 		currentKey  = "initial";
 		previousKeys = new HashMap<String, String>();
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         filesList = (ArrayList<FileItem>) getIntent().getSerializableExtra("files");
         if (filesList == null) filesList = new ArrayList<FileItem>();
         if (filesList.isEmpty()) loadFilesList("initial");
 
     	filesAdapter = new FileItemsListAdapter(this, R.layout.file_list_row, filesList);
     	setListAdapter(filesAdapter);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         FileItem file = filesAdapter.getItem(position);
         if (file.type.equals("folder") || file.type.equals("action")) {
         	loadFilesList(file.key);
         } else {
         	downloadFile(file.key, file.name);
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ((keyCode == KeyEvent.KEYCODE_BACK)) {
         	if (!currentKey.equals(goBack.key)) {
         		loadFilesList(goBack.key);
         	} else {
                 return super.onKeyDown(keyCode, event);
         	}
         }
         return true;
     }
 
 	public void downloadFile(String key, String name) {		
 		PerformFileDownloadTask task = new PerformFileDownloadTask();
 		task.execute(key, name);
 
 		progressDialog.setOnCancelListener(new CancelTaskOnCancelListener(task));
 	}
 
     public void loadFilesList(String key) {
     	if (!previousKeys.containsKey(key)) previousKeys.put(key, currentKey);
     	currentKey = key;
 
     	goBack.key = previousKeys.get(currentKey);
 
     	progressDialog = ProgressDialog.show(MainActivity.this, "Please wait...", "Retrieving data...", true, true);
 
     	PerformFileListSearchTask task = new PerformFileListSearchTask();
 		task.execute(key);
 		progressDialog.setOnCancelListener(new CancelTaskOnCancelListener(task));
 
 
     }
 
    private class PerformFileDownloadTask extends AsyncTask<String, String, String> {
     	@Override
         protected void onPreExecute() {
             super.onPreExecute();
         	progressDialog = new ProgressDialog(MainActivity.this);
         	progressDialog.setMessage("Downloading file. Please wait...");
         	progressDialog.setIndeterminate(false);
         	progressDialog.setMax(100);
         	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         	progressDialog.setCancelable(true);
         	progressDialog.show();
         }
     	
     	@Override
     	protected String doInBackground(String... params) {
     		int count;
     		Long lenghtOfFile;
     		OutputStream outputStream;
     		
 			String key          = params[0];
 			String fileName     = params[1]; 
 			String url          = new FileDownloader().constructSearchUrl(key);
 			String fileLocation = Environment.getExternalStorageDirectory().getPath() + "/wu_files/";
 			
     		HttpRetriever httpRetreiever = new HttpRetriever(url);
     		InputStream inputStream      = new BufferedInputStream(httpRetreiever.retrieveStream(), 8192);
     		
     		try {
     			File folder = new File(fileLocation);
     			if (!folder.exists()) folder.mkdir();
     			
     			lenghtOfFile = (Long) httpRetreiever.retrieveContentSize();
     			outputStream = new BufferedOutputStream(new FileOutputStream(fileLocation + fileName));
     			
     			byte data[] = new byte[1024];
     			 
                 long total = 0;
      
                 while ((count = inputStream.read(data)) != -1) {
                     total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
      
                     outputStream.write(data, 0, count);
                 }
 
                 outputStream.flush();
                 outputStream.close();
     		
     	    } catch (Exception e) {
                 Log.e("Error: ", e.getMessage());
     	    } finally {
     	    	try{
                     inputStream.close();
     	    		inputStream.close();
         	    	httpRetreiever.closeConnect();
         	    }
     	    	catch (Exception e) { Log.e("Error: ", e.getMessage()); }
     	    }
     		return fileLocation + fileName;
     	}
     	
 		protected void onProgressUpdate(String... params) {
             progressDialog.setProgress(Integer.parseInt(params[0]));
        }
 
     	@Override
     	protected void onPostExecute(String file_location) {
     		MimeTypeMap myMime = MimeTypeMap.getSingleton();
 
     		progressDialog.dismiss();
 
     		Intent fileViewIntent = new Intent();
     		fileViewIntent.setAction(android.content.Intent.ACTION_VIEW);
     		File file = new File(file_location);
 
     		String mimeType = myMime.getMimeTypeFromExtension(fileExt(file.toString()).substring(1));
     		
     	       
     		fileViewIntent.setDataAndType(Uri.fromFile(file), mimeType);
     		try {
     			startActivity(fileViewIntent);
     		} catch (android.content.ActivityNotFoundException e) {
     			longToast("No Application found to open this file");
     			Log.e(getClass().getSimpleName(), "Activity Not Found");
     		}
     	}
     }
     
     private class PerformFileListSearchTask extends AsyncTask<String, Void, ArrayList<FileItem>> {
     	private FileLister fileLister;
     	@Override
     	protected ArrayList<FileItem> doInBackground(String... params) {
     		String key = params[0];
             fileLister = new FileLister();
             return fileLister.retrieveFilesList(key);
     	}
 
     	@Override
     	protected void onPostExecute(final ArrayList<FileItem> result) {
     		runOnUiThread(new Runnable() {
     			@Override
     			public void run() {
     				if (progressDialog != null) {
     					progressDialog.dismiss();
     					progressDialog = null;
     				}
     				filesList.clear();
     				for (int i = 0; i < result.size(); i++) {
     					filesList.add(result.get(i));
     				}
     				if (!currentKey.equals(goBack.key)) filesList.add(0, goBack);
     		        filesAdapter.notifyDataSetChanged();
     			}
     		});
     	}
     }
 
 
     private class CancelTaskOnCancelListener implements OnCancelListener {
     	private AsyncTask<?, ?, ?> task;
 
     	public CancelTaskOnCancelListener(AsyncTask<?, ?, ?> task) {
     		this.task = task;
     	}
     	@Override
 		public void onCancel(DialogInterface dialog) {
     		if (task!=null) {
         		task.cancel(true);
         	}
 		}
     }
 
     private String fileExt(String url) {
         if (url.indexOf("?")>-1) {
             url = url.substring(0,url.indexOf("?"));
         }
         if (url.lastIndexOf(".") == -1) {
             return null;
         } else {
             String ext = url.substring(url.lastIndexOf(".") );
             if (ext.indexOf("%")>-1) {
                 ext = ext.substring(0,ext.indexOf("%"));
             }
             if (ext.indexOf("/")>-1) {
                 ext = ext.substring(0,ext.indexOf("/"));
             }
             return ext.toLowerCase();
         }
     }
 
 	public void longToast(CharSequence message) {
 		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
 	}
 }
